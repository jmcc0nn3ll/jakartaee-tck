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
 * $Id$
 */

package com.sun.ts.tests.ejb30.common.generics;

import jakarta.annotation.security.RolesAllowed;
import jakarta.ejb.Stateless;

@Stateless
public class GreetingBean extends GreetingBeanBase
    implements RemoteIntGreetingIF, LocalIntGreetingIF, RemoteParameterizedIF,
    LocalParameterizedIF {

  // inherit business methods from GreetingBeanBase, along with their
  // @TransactionAttribute

  /*
   * (non-Javadoc)
   * 
   * @see
   * com.sun.ts.tests.ejb30.common.generics.GenericGreetingIF#rolesAllowed()
   */
  @RolesAllowed("Administrator")
  public Integer rolesAllowed(Integer i) {
    return i;
  }

  /*
   * (non-Javadoc)
   * 
   * @see
   * com.sun.ts.tests.ejb30.common.generics.GenericGreetingIF#rolesAllowedNoArg(
   * )
   */
  @RolesAllowed("Manager")
  public Integer rolesAllowedNoArg() {
    return 0;
  }
}
