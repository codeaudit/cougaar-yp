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
import org.cougaar.core.mts.MessageAddress;
import org.cougaar.core.service.community.Community;

public interface YPService extends Service {
  /** Get a Proxy for YP Queries from a specific YPServer.  Typically, you would
   * invoke a method on the returned YPProxy, publish the resulting YPFuture to the
   * blackboard, and wait until the future has been publishChanged to get the value.
   * @param ypAgent The name of the agent running the YPServer - used to construct
   * a MessageAddress. 
   * @note The YP resolver will NOT look beyond the specified agent.
   * @deprecated
   **/
  YPProxy getYP(String ypAgent);

  /** Get a Proxy for YP Queries from a specific YPServer.  Typically, you would
   * invoke a method on the returned YPProxy, publish the resulting YPFuture to the
   * blackboard, and wait until the future has been publishChanged to get the value.
   * @param ypAgent MessageAddress of the agent running the YPServer.
   * @note The YP resolver will NOT look beyond the specified agent.
   **/
  YPProxy getYP(MessageAddress ypAgent);

  /** Get a Proxy for YP Queries in a specific community context.  Typically, you 
   * would invoke a method on the returned YPProxy, publish the resulting 
   * YPFuture to the blackboard, and wait until the future has been 
   * publishChanged to get the value.
   * @param community Initial community for all YP interactions
   * @note The YP resolver will use the community hierarchy to resolve queries
   * which have no match in the initial commununity.
   **/
  YPProxy getYP(Community community);

  /** Get a Proxy for YP Queries in a specific community context.  Typically, you 
   * would invoke a method on the returned YPProxy, publish the resulting 
   * YPFuture to the blackboard, and wait until the future has been 
   * publishChanged to get the value.
   * @param community Initial community for all YP interactions
   * @param searchMode Defines who/whether the YP resolver will use the 
   * community hierarchy to resolve queries which have no match in the 
   * initial commununity.
   * @note searchMode should be one of values defined by YPProxy.SearchMode
   **/
  YPProxy getYP(Community community, int searchMode);

  /** Get a Proxy for YP Queries in the default community context - i.e. 
   * starting with the lowest level YPCommunity for which the agent is
   * a member.  Typically, you 
   * would invoke a method on the returned YPProxy, publish the resulting 
   * YPFuture to the blackboard, and wait until the future has been 
   * publishChanged to get the value.
   * @note The YP resolver will use the community hierarchy to resolve queries
   * which have no match in the initial commununity.
   **/
  YPProxy getYP();

  /** Get a Proxy for YP Queries in the default community context - i.e. 
   * starting with the lowest level YPCommunity for which the agent is
   * a member.  Typically, you 
   * would invoke a method on the returned YPProxy, publish the resulting 
   * YPFuture to the blackboard, and wait until the future has been 
   * publishChanged to get the value.
   * @param searchMode Defines who/whether the YP resolver will use the 
   * community hierarchy to resolve queries which have no match in the 
   * local commununity.
   * @note searchMode should be one of values defined by YPProxy.SearchMode
   **/
  YPProxy getYP(int searchMode);

  /** List #getYP(String), except submits the request before returning the 
   * YPFuture value.  This is a convenient mechanism if you do not have access
   * to a blackboard for blackboard-based publish/subscribe of YPFuture requests.
   * Keep in mind that the YPFuture values returned are still <em>futures</em>, e.g. a call to get()
   * will block until the answer has been received.
   * @param ypAgent The name of the agent running the YPServer - used to construct
   * a MessageAddress.
   * @note The YP resolver will NOT look beyond the specified agent.
   * @note Access to this method is likely to be more tightly constrained by security
   * mechanisms, as it is easier to write broken code using this mechanism.
   * @deprecated
   */
  YPProxy getAutoYP(String ypAgent);

  /** List #getYP(String), except submits the request before returning the 
   * YPFuture value.  This is a convenient mechanism if you do not have access
   * to a blackboard for blackboard-based publish/subscribe of YPFuture requests.
   * Keep in mind that the YPFuture values returned are still <em>futures</em>, e.g. a call to get()
   * will block until the answer has been recieved.
   * @param ypAgent MessageAddress of the agent running the YPServer
   * @note The YP resolver will NOT look beyond the specified agent.
   * @note Access to this method is likely to be more tightly constrained by security
   * mechanisms, as it is easier to write broken code using this mechanism.
   */
  YPProxy getAutoYP(MessageAddress ypAgent);


  /** List #getYP(String), except submits the request before returning the 
   * YPFuture value.  This is a convenient mechanism if you do not have access
   * to a blackboard for blackboard-based publish/subscribe of YPFuture requests.
   * Keep in mind that the YPFuture values returned are still <em>futures</em>, e.g. a call to get()
   * will block until the answer has been recieved.
   * @param community Initial community for all YP interactions
   * @note The YP resolver will use the community hierarchy to resolve queries
   * which have no match in the initial commununity.

   * @note Access to this method is likely to be more tightly constrained by security
   * mechanisms, as it is easier to write broken code using this mechanism.
   */
  YPProxy getAutoYP(Community community);

  /** Submit a YPFuture to be transmitted.
   * @note This method is only for use by clients which can afford to block their thread - 
   * most clients with blackboard access should publish/subscribe the YPFuture instead.
   * @return argument for convenience.
   **/
  YPFuture submit(YPFuture ypr);


  /** Find the next context to search.
   * @param currentContext current YP context
   * @param callback callback.invoke(Object) called with the next context. 
   * Next context will be null if there is no next context.
   * @note callback.invoke may be called from within nextYPServerContext
   **/
  public void nextYPServerContext(final Object currentContext,
				  final NextContextCallback callback);

  
  /** Invoked when the next YP context has been resolved
   **/
  interface NextContextCallback {
    void setNextContext(Object context);
  }

   
}






