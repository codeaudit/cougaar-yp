/*
 * <copyright>
 *  Copyright 2003 BBNT Solutions, LLC
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

import java.util.Properties;

import org.cougaar.core.service.ThreadService;
import org.cougaar.core.thread.Schedulable;
import org.cougaar.util.log.Logger;
import org.cougaar.util.log.Logging;

/**
 * Using OneShotMachine is similar to directly setting a callback on a YPFuture, except
 * that the invocation of the callback is done in a pooled thread rather than
 * in the thread of the YPService.
 **/

public class OneShotMachine {
  private static final Logger log = Logging.getLogger(YPStateMachine.class);

  private static String UDDI_USERID = "cougaar";
  private static String UDDI_PASSWORD = "cougaarPass";

  static {
    UDDI_USERID = System.getProperty("org.cougaar.yp.juddi-users.username", YPProxy.DEFAULT_UDDI_USERNAME);
    UDDI_PASSWORD = System.getProperty("org.cougaar.yp.juddi-users.password", YPProxy.DEFAULT_UDDI_PASSWORD);
  }

  private final Properties ypproperties = new Properties();

  private final YPService yps;
  protected final YPService getYPService() { return yps; }

  private final ThreadService threads;
  protected final ThreadService getThreadService() { return threads; }

  private final YPFuture fut;
  protected final YPFuture getYPFuture() { return fut; }

  private final YPFuture.ResponseCallback rcallback;
  protected final YPFuture.ResponseCallback getResponseCallback() { return rcallback; }

  public OneShotMachine(YPFuture fut, YPFuture.ResponseCallback rcallback, YPService yps, ThreadService threads) {
    this.fut = fut;
    this.rcallback = rcallback;
    this.yps = yps;
    this.threads = threads;
    ypproperties.put("username", UDDI_USERID);
    ypproperties.put("password", UDDI_PASSWORD);
    if (log.isInfoEnabled()) {
      log.info("OneShotMachine constructor="+this);
    }
  }

  /** set a property for just this instance of YPStateMachine.
   * Current properties are "username" and "password".
   * @note that it is undefined to change existing values after initialization.
   **/
  public void setProperty(String name, String value) {
    ypproperties.put(name, value);
  }

  /** Sets the machine in motion.  Will continue until DONE (or an error state) is achieved **/
  public void start() {
    submit();
  }

  private YPFuture.Callback ypcallback = new YPFuture.Callback() {
      public void ready(YPFuture r) {
        if (r != fut) {
          log.warn("In StateMachine YPCallback, expected "+fut+" instead of "+r);
        }
        if (fut.isReady()) {
          kick();
        } else {
          log.error("YPCallback invoked without YPFuture.isReady()!"); 
        }
      }
    };

  // utility for submitting ypqueries and waiting for response
  protected void submit() {
    if (log.isInfoEnabled()) {
      log.info("OneShotMachine("+this+").submit("+ypcallback+")");
    }
    fut.setCallback(ypcallback);
    yps.submit(fut);
  }

  // only kicked when ready by the callback!
  private void kick() {
    Schedulable thread = getThreadService().getThread(this, new Runnable() {
        public void run() {
          if (log.isInfoEnabled()) {
            log.info("OneShotMachine("+(OneShotMachine.this)+").run()");
          }
          try {
            Object r = fut.get();
            rcallback.invoke(r);
          } catch (Exception e) {
            rcallback.handle(e);
          }
        }
      });
    thread.start();
  }
}
