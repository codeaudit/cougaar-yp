/*
 * <copyright>
 *  Copyright 2002 BBNT Solutions, LLC
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

// jaxr
import javax.xml.registry.*;
import javax.xml.registry.infomodel.*;

import java.io.*;
import java.net.*;
import java.util.*;

/** Extend JAXR BulkResponse with some asynchronous response variations **/
public interface YPResponse extends BulkResponse, Serializable {
  /** Suspend the current thread until response.isAvailable() will return true **/
  void waitForIsAvailable() throws InterruptedException;

  /** Suspend the current thread until response.isAvailable() will return true
   * @param timeout How long to wait.
   **/
  void waitForIsAvailable(long timeout) throws InterruptedException;

  /** install a callback to be invoked when the response is available.
   * If the response is already available when this method is called, 
   * the callback my be invoked in the calling thread immediately.
   * @note The behavior of this method is undefined if it is called more than
   * once on a single YPResponse instance.
   * @param callback A runnable to be executed when a result is available.  This will
   * be called exactly once.  The callback.run() method should execute quickly - under
   * no circumstances should it ever block or perform any non-trivial tasks.
   **/
  void addCallback(Runnable callback);
}
