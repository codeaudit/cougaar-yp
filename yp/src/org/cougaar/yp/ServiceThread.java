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

import org.cougaar.core.mts.Message;
import org.cougaar.core.service.ThreadService;
import org.cougaar.core.thread.Schedulable;
import org.cougaar.util.CircularQueue;
import org.cougaar.util.log.Logger;

/** A handler of messages via a queue **/
public class ServiceThread {
  public interface Callback {
    void dispatch(Message m);
  }

  private final Logger logger;
  private final String name;
  private final Callback callback;

  public ServiceThread(Callback callback, Logger logger, String name) {
    this.callback = callback;
    this.logger = logger;
    this.name = name;
  }

  /** queue for incoming YP Messages - dequeued by the service pseudo-thread **/
  private final CircularQueue inQ = new CircularQueue(11);

  /** The service thread **/
  private Schedulable thread = null;

  public void start(ThreadService threadService) {
    // set up the service thread
    thread = threadService.getThread(this, new Runnable() {
        public void run() {
          //System.err.println(name+" RUN");
          try {
            cycle();
          } catch (Throwable e) {
            logger.error("Uncaught exception for "+callback, e);
          }
        }}, name);
  }

  private int ic = 0;
  private int oc = 0;

  /** Used to (re)start the service thread to handle queued YP messages **/
  private void wake() {
    //System.err.println(name+" WAKE");
    thread.start();
  }

  public void addMessage(Message m) {
    synchronized(inQ) {
      ic++;
      if (logger.isDebugEnabled()) {
        logger.debug(name+" Queuing Message "+ic);
      }
      //System.err.println(name+" IN="+ic);
      inQ.add(m);
    }
    // should this be in the sync?
    wake();
  }

  private final static boolean batchRequests = false;

  private void cycle() {
    Message m;
      
    if (batchRequests) {
      // service everything we can
      while (true) {
        // get the next message
        synchronized (inQ) {
          m = (Message) inQ.next();
          if (m == null) return;  // exit the loop if we are done
        }

        // handle it outside the inQ lock
        callback.dispatch(m);
      }
    } else {
      // service just the next message
      synchronized (inQ) {
        m = (Message) inQ.next();
        if (logger.isDebugEnabled()) {
          logger.debug(name+" handling Request "+oc);
        }
        if (m == null) {
          return;  // exit the loop if we are done
        }
        oc++;
      }
      //System.err.println(name+" OUT="+oc);
      callback.dispatch(m);
      // could check again, but why bother?
      wake();
    }
  }
}
