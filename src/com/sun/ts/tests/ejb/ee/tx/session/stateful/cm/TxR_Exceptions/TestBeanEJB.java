/*
 * Copyright (c) 2007, 2020 Oracle and/or its affiliates. All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v. 2.0, which is available at
 * http://www.eclipse.org/legal/epl-2.0.
 *
 * This Source Code may also be made available under the following Secondary
 * Licenses when the conditions for such availability set forth in the
 * Eclipse Public License v. 2.0 are satisfied: GNU General Public License,
 * version 2 with the GNU Classpath Exception, which is available at
 * https://www.gnu.org/software/classpath/license.html.
 *
 * SPDX-License-Identifier: EPL-2.0 OR GPL-2.0 WITH Classpath-exception-2.0
 */

/*
 * @(#)TestBeanEJB.java	1.21 03/05/16
 */

package com.sun.ts.tests.ejb.ee.tx.session.stateful.cm.TxR_Exceptions;

import java.rmi.NoSuchObjectException;
import java.rmi.RemoteException;
import java.util.Properties;
import java.util.Vector;

import com.sun.ts.lib.util.RemoteLoggingInitException;
import com.sun.ts.lib.util.TSNamingContext;
import com.sun.ts.lib.util.TestUtil;
import com.sun.ts.tests.ejb.ee.tx.txbean.AppException;
import com.sun.ts.tests.ejb.ee.tx.txbean.TxBean;
import com.sun.ts.tests.ejb.ee.tx.txbean.TxBeanEJB;
import com.sun.ts.tests.ejb.ee.tx.txbean.TxBeanHome;

import jakarta.ejb.CreateException;
import jakarta.ejb.EJBException;
import jakarta.ejb.SessionBean;
import jakarta.ejb.SessionContext;

public class TestBeanEJB implements SessionBean {

  // testProps represent the test specific properties passed in
  // from the test harness.
  private Properties testProps = null;

  // The TSNamingContext abstracts away the underlying distribution protocol.
  private TSNamingContext jctx = null;

  private SessionContext sctx = null;

  private String tName1 = null;

  private Integer tSize = null;

  private Integer fromKey1 = null;

  // The TxBean variables
  private static final String txBeanRequired = "java:comp/env/ejb/TxRequired";

  private TxBeanHome beanHome = null;

  private TxBean beanRef = null;

  private TxBean beanRef2 = null;

  // The requiredEJB methods
  public void ejbCreate() throws CreateException {
    TestUtil.logTrace("ejbCreate");
    try {
      TestUtil.logMsg("Getting Naming Context");
      jctx = new TSNamingContext();

      this.tSize = (Integer) jctx.lookup("java:comp/env/size");
      TestUtil.logTrace("tSize: " + this.tSize);
      this.fromKey1 = (Integer) jctx.lookup("java:comp/env/fromKey1");
      TestUtil.logTrace("fromKey1: " + this.fromKey1);

      TestUtil
          .logMsg("Looking up the TxBean Home interface of " + txBeanRequired);
      beanHome = (TxBeanHome) jctx.lookup(txBeanRequired, TxBeanHome.class);

    } catch (Exception e) {
      TestUtil.logErr("Create exception: " + e.getMessage());
      TestUtil.printStackTrace(e);
    }
  }

  public void setSessionContext(SessionContext sc) {
    TestUtil.logTrace("setSessionContext");
    this.sctx = sc;
  }

  public void ejbRemove() {
    TestUtil.logTrace("ejbRemove");
  }

  public void ejbActivate() {
    TestUtil.logTrace("ejbActivate");
  }

  public void ejbPassivate() {
    TestUtil.logTrace("ejbPassivate");
  }

  // ===========================================================
  // TestBean interface (our business methods)

  public boolean test1() {
    TestUtil.logTrace("test1");
    TestUtil.logTrace("AppException from EJB");

    Vector dbResults = new Vector();
    boolean testResult = false;
    boolean b1, b2, b3;
    b1 = b2 = b3 = false;
    String tName = this.tName1;
    int size = this.tSize.intValue();
    int tRng = this.fromKey1.intValue();

    try {
      TestUtil.logTrace("Creating EJB instance of " + txBeanRequired);
      beanRef = (TxBean) beanHome.create();

      TestUtil.logTrace("Logging data from server");
      beanRef.initLogging(testProps);

      beanRef.createData(tName);

      try {
        beanRef.delete(tName, tRng, tRng, TxBeanEJB.FLAGAPPEXCEPTION);
        TestUtil.logTrace("Expected AppException did not occur");
      } catch (AppException ae) {
        TestUtil.logTrace("AppException received as expected");
        b1 = true;
      }

      TestUtil.logTrace("Getting the test results");
      dbResults = beanRef.getResults(tName);
      // beanRef.listTableData(dbResults);

      TestUtil.logTrace("Verifying the test results");
      if (!dbResults.contains(new Integer(tRng)))
        b2 = true;

      for (int i = 1; i <= size; i++) {
        if (i == tRng)
          continue;
        else {
          if (dbResults.contains(new Integer(i)))
            b3 = true;
          else {
            b3 = false;
            break;
          }
        }
      }
      beanRef.destroyData(tName);

      if (b1 && b2 && b3)
        testResult = true;

    } catch (Exception e) {
      try {
        beanRef.destroyData(tName);
      } catch (RemoteException re) {
      }
      ;
      TestUtil.logErr("Caught exception: " + e.getMessage());
      TestUtil.printStackTrace(e);
    } finally {
      // cleanup the bean
      try {
        beanRef.destroyData(tName);
        beanRef.remove();
      } catch (Exception e) {
        TestUtil.printStackTrace(e);
      }
    }
    return testResult;
  }

  public boolean test2() {
    TestUtil.logTrace("test2");
    TestUtil.logTrace("SystemException from EJB");

    Vector dbResults = new Vector();
    boolean testResult = false;
    boolean b1, b2, b3;
    b1 = b2 = b3 = false;
    String tName = this.tName1;
    int size = this.tSize.intValue();
    int tRng = this.fromKey1.intValue();

    try {
      TestUtil.logTrace("Creating EJB instance of " + txBeanRequired);
      beanRef = (TxBean) beanHome.create();

      TestUtil.logTrace("Logging data from server");
      beanRef.initLogging(testProps);

      TestUtil.logTrace("Creating the table");
      beanRef.createData(tName);

      TestUtil.logTrace("Delete a row and throw SystemException");

      try {
        beanRef.delete(tName, tRng, tRng, TxBeanEJB.FLAGSYSEXCEPTION);
        TestUtil.logTrace("Expected RemoteException did not occur");
      } catch (RemoteException re) {
        TestUtil.logTrace("RemoteException received as expected");
        b1 = true;
      }

      TestUtil.logTrace("Check that bean instance was discarded");
      try {
        int level = beanRef.getDefaultTxIsolationLevel(tName);
        TestUtil.logTrace("Bean instance not discarded as expected!");
        b2 = false;
      } catch (NoSuchObjectException ne) {
        b2 = true;
        TestUtil.logTrace("Bean instance was discarded as expected");
      } catch (RemoteException e) {
        b2 = true;
        TestUtil.logTrace("Bean instance was discarded as expected");
      }

      // beanRef is now gone
      // Check the table results for the rollback && Clean up the table
      beanRef2 = (TxBean) beanHome.create();
      beanRef2.initLogging(testProps);

      TestUtil.logTrace("Checking table results for actual rollback occurance");
      // Check the table results, did the rollback really occur?
      dbResults = beanRef2.getResults(tName);
      // beanRef2.listTableData(dbResults);

      TestUtil.logTrace("Verifying the test results");
      for (int i = 1; i <= size; i++) {
        if (dbResults.contains(new Integer(i)))
          b3 = true;
        else {
          b3 = false;
          break;
        }
      }
      TestUtil.logTrace("Cleaning up the table");
      beanRef2.destroyData(tName);

      if (b1 && b2 && b3)
        testResult = true;

    } catch (Exception e) {
      try {
        beanRef.destroyData(tName);
      } catch (RemoteException re) {
      }
      ;
      TestUtil.logErr("Caught exception: " + e.getMessage());
      TestUtil.printStackTrace(e);
    } finally {
      // cleanup the bean
      try {
        beanRef2.remove();
      } catch (Exception e) {
        TestUtil.printStackTrace(e);
      }
      ;
    }
    return testResult;
  }

  public boolean test4() {
    TestUtil.logTrace("test4");
    TestUtil.logTrace("EJBException from EJB");

    Vector dbResults = new Vector();
    boolean testResult = false;
    boolean b1, b2, b3;
    b1 = b2 = b3 = false;
    String tName = this.tName1;
    int size = this.tSize.intValue();
    int tRng = this.fromKey1.intValue();

    try {
      TestUtil.logTrace("Creating EJB instance of " + txBeanRequired);
      beanRef = (TxBean) beanHome.create();

      TestUtil.logTrace("Logging data from server");
      beanRef.initLogging(testProps);

      TestUtil.logTrace("Creating the table");
      beanRef.createData(tName);

      TestUtil.logTrace("Delete a row and throw EJBException");

      try {
        beanRef.delete(tName, tRng, tRng, TxBeanEJB.FLAGEJBEXCEPTION);
        TestUtil.logTrace("Expected RemoteException did not occur");
      } catch (RemoteException re) {
        TestUtil.logTrace("RemoteException received as expected");
        b1 = true;
      }

      TestUtil.logTrace("Check that bean instance was discarded");
      try {
        int level = beanRef.getDefaultTxIsolationLevel(tName);
        TestUtil.logTrace("Bean instance not discarded as expected!");
        b2 = false;
      } catch (NoSuchObjectException ne) {
        b2 = true;
        TestUtil.logTrace("Bean instance was discarded as expected");
      } catch (RemoteException e) {
        b2 = true;
        TestUtil.logTrace("Bean instance was discarded as expected");
      }

      // beanRef is now gone
      // Check the table results for the rollback && Clean up the table
      beanRef2 = (TxBean) beanHome.create();
      beanRef2.initLogging(testProps);

      TestUtil.logTrace("Checking table results for actual rollback occurance");
      // Check the table results, did the rollback really occur?
      dbResults = beanRef2.getResults(tName);
      // beanRef2.listTableData(dbResults);

      TestUtil.logTrace("Verifying the test results");
      for (int i = 1; i <= size; i++) {
        if (dbResults.contains(new Integer(i)))
          b3 = true;
        else {
          b3 = false;
          break;
        }
      }
      TestUtil.logTrace("Cleaning up the table");
      beanRef2.destroyData(tName);

      if (b1 && b2 && b3)
        testResult = true;

    } catch (Exception e) {
      try {
        beanRef.destroyData(tName);
      } catch (RemoteException re) {
      }
      ;
      TestUtil.logErr("Caught exception: " + e.getMessage());
      TestUtil.printStackTrace(e);
    } finally {
      // cleanup the bean
      try {
        beanRef2.remove();
      } catch (Exception e) {
        TestUtil.printStackTrace(e);
      }
      ;
    }
    return testResult;
  }

  public boolean test5() {
    TestUtil.logTrace("test5");
    TestUtil.logTrace("Error from EJB");

    Vector dbResults = new Vector();
    boolean testResult = false;
    boolean b1, b2, b3;
    b1 = b2 = b3 = false;
    String tName = this.tName1;
    int size = this.tSize.intValue();
    int tRng = this.fromKey1.intValue();

    try {
      TestUtil.logTrace("Creating EJB instance of " + txBeanRequired);
      beanRef = (TxBean) beanHome.create();

      TestUtil.logTrace("Logging data from server");
      beanRef.initLogging(testProps);

      TestUtil.logTrace("Creating the table");
      beanRef.createData(tName);

      TestUtil.logTrace("Delete a row and throw Error");

      try {
        beanRef.delete(tName, tRng, tRng, TxBeanEJB.FLAGERROR);
        TestUtil.logTrace("Expected RemoteException did not occur");
      } catch (RemoteException re) {
        TestUtil.logTrace("RemoteException received as expected");
        b1 = true;
      }

      TestUtil.logTrace("Check that bean instance was discarded");
      try {
        int level = beanRef.getDefaultTxIsolationLevel(tName);
        TestUtil.logTrace("Bean instance not discarded as expected!");
        b2 = false;
      } catch (NoSuchObjectException ne) {
        b2 = true;
        TestUtil.logTrace("Bean instance was discarded as expected");
      } catch (RemoteException e) {
        b2 = true;
        TestUtil.logTrace("Bean instance was discarded as expected");
      }

      // beanRef is now gone
      // Check the table results for the rollback && Clean up the table
      beanRef2 = (TxBean) beanHome.create();
      beanRef2.initLogging(testProps);

      TestUtil.logTrace("Checking table results for actual rollback occurance");
      // Check the table results, did the rollback really occur?
      dbResults = beanRef2.getResults(tName);
      // beanRef2.listTableData(dbResults);

      TestUtil.logTrace("Verifying the test results");
      for (int i = 1; i <= size; i++) {
        if (dbResults.contains(new Integer(i)))
          b3 = true;
        else {
          b3 = false;
          break;
        }
      }
      TestUtil.logTrace("Cleaning up the table");
      beanRef2.destroyData(tName);

      if (b1 && b2 && b3)
        testResult = true;

    } catch (Exception e) {
      try {
        beanRef.destroyData(tName);
      } catch (RemoteException re) {
      }
      ;
      TestUtil.logErr("Caught exception: " + e.getMessage());
      TestUtil.printStackTrace(e);
    } finally {
      // cleanup the bean
      try {
        beanRef2.remove();
      } catch (Exception e) {
        TestUtil.printStackTrace(e);
      }
      ;
    }
    return testResult;
  }

  public void initLogging(Properties p) {
    TestUtil.logTrace("initLogging");
    this.testProps = p;
    try {
      TestUtil.init(p);
      // Get the table names
      this.tName1 = TestUtil
          .getTableName(TestUtil.getProperty("TxBean_Tab1_Delete"));
      TestUtil.logTrace("tName1: " + this.tName1);

    } catch (RemoteLoggingInitException e) {
      TestUtil.printStackTrace(e);
      throw new EJBException(e.getMessage());
    }
  }

}
