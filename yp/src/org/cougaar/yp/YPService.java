/*
 * <copyright>
 *  Copyright 2002-2003 BBNT Solutions, LLC
 *  under sponsorship of the Defense Advanced Research Projects Agency (DARPA).
 * 
 *  This program is free software; you can redistribute it and/or modify
 *  it under the terms of the Cougaar Open Source License as published by
 *  DARPA on the Cougaar Open Source Website (www.cougaar.org).
 * 
 *  THE COUGAAR SOFTWARE AND ANY DERIVATIVE SUPPLIED BY LICENSOR IS
 *  PROVIDED 'AS IS' WITHOUT WARRANTIES OF ANY KIND, WHETHER EXPRESS OR
 *  IMPLIED, INCLUDING (BUT NOT LIMITED TO) ALL IMPLIED WARRANTIES OF
 *  MERCHANTABILITY AND FITNESS FOR A PARTICULAR PURPOSE, AND WITHOUT
 *  ANY WARRANTIES AS TO NON-INFRINGEMENT.  IN NO EVENT SHALL COPYRIGHT
 *  HOLDER BE LIABLE FOR ANY DIRECT, SPECIAL, INDIRECT OR CONSEQUENTIAL
 *  DAMAGES WHATSOEVER RESULTING FROM LOSS OF USE OF DATA OR PROFITS,
 *  TORTIOUS CONDUCT, ARISING OUT OF OR IN CONNECTION WITH THE USE OR
 *  PERFORMANCE OF THE COUGAAR SOFTWARE.
 * </copyright>
 */

package org.cougaar.yp;

import org.cougaar.core.component.Service;

public interface YPService extends Service {
  /** Get a Proxy for YP Queries in a specific context.  Typically, you would
   * invoke a method on the returned YPProxy, publish the resulting YPFuture to the
   * blackboard, and wait until the future has been publishChanged to get the value.
   * @param context The context is a name to be looked up in the WP.  If null, will default
   * to the set of communities this agent is a member of.
   **/
  YPProxy getYP(String context);

  /** List #getYP(String), except submits the request before returning the 
   * YPFuture value.  This is a convenient mechanism if you do not have access
   * to a blackboard for blackboard-based publish/subscribe of YPFuture requests.
   * Keep in mind that the YPFuture values returned are still <em>futures</em>, e.g. a call to get()
   * will block until the answer has been recieved.
   * @note Access to this method is likely to be more tightly constrained by security
   * mechanisms, as it is easier to write broken code using this mechanism.
   */
  YPProxy getAutoYP(String context);

  /** Submit a YPFuture to be transmitted.
   * @note This method is only for use by clients which can afford to block their thread - 
   * most clients with blackboard access should publish/subscribe the YPFuture instead.
   * @return argument for convenience.
   **/
  YPFuture submit(YPFuture ypr);

}
