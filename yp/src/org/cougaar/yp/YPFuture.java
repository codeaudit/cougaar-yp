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

import org.w3c.dom.Element;
import org.uddi4j.UDDIException;


// we could base the implementation on concurrent.FutureResult

/** An outstanding YP response object, returned from all of the YPProxy
 * methods.
 * A consumer of the YP information would issue a query, then 
 * watch the returned YPFuture object until
 * isReady returns true.
 */

public interface YPFuture {
  /** Indicate when a response has been recieved (or a failure indicated).
   * If true, then the get methods will return immediately.
   **/
  boolean isReady();

  /** gets the response value as an object, blocking if need be.
   * @note implemented as get(0L);
   **/
  Object get() throws UDDIException;

  /** gets the response value as an object, blocking if need be for up to the
   * specified millis.
   * If still not ready at the end, will return null, e.g. if finished due to
   * timeout or thread interruption.
   * May also throw a runtimeException if the query returns an exception. 
   * The actual exception is the value of getCause of the RuntimeException thrown.
   * If msecs is specified as 0 then it will wait forever.
   **/
  Object get(long msecs) throws UDDIException;

  // Consider casted getters, e.g. DispositionReport getDispositionReport()

  /** Clients may set a callback here to be invoked when
   * the response is ready.  Most clients will use the Blackboard
   * interation pattern and look for publishChange events rather
   * than registering for explicit callbacks.
   * @note If the response is already ready, then the callback
   * will be invoked immediately in the thread of the caller.
   * @note Only one Callback may be attached.
   **/
  void setCallback(Callback callable);

  /** Access the XML element describing the query **/
  Element getElement();

  /** Is the message a query? **/
  boolean isInquiry();

  /** In which context was this query issued **/
  String getInitialContext();

  /** In which context was this query resolved **/
  String getFinalContext();
  
  /** The interface which must be implemented by the argument to #setCallback(Callback) **/
  interface Callback {
    /** The result posting mechanism will invoke this method as soon as #isReady() will return
     * true.
     **/
    void ready(YPFuture response);
  }
}
