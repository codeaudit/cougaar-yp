/*
 * <copyright>
 *  
 *  Copyright 2002-2004 BBNT Solutions, LLC
 *  under sponsorship of the Defense Advanced Research Projects
 *  Agency (DARPA).
 * 
 *  You can redistribute this software and/or modify it under the
 *  terms of the Cougaar Open Source License as published on the
 *  Cougaar Open Source Website (www.cougaar.org).
 * 
 *  THIS SOFTWARE IS PROVIDED BY THE COPYRIGHT HOLDERS AND CONTRIBUTORS
 *  "AS IS" AND ANY EXPRESS OR IMPLIED WARRANTIES, INCLUDING, BUT NOT
 *  LIMITED TO, THE IMPLIED WARRANTIES OF MERCHANTABILITY AND FITNESS FOR
 *  A PARTICULAR PURPOSE ARE DISCLAIMED. IN NO EVENT SHALL THE COPYRIGHT
 *  OWNER OR CONTRIBUTORS BE LIABLE FOR ANY DIRECT, INDIRECT, INCIDENTAL,
 *  SPECIAL, EXEMPLARY, OR CONSEQUENTIAL DAMAGES (INCLUDING, BUT NOT
 *  LIMITED TO, PROCUREMENT OF SUBSTITUTE GOODS OR SERVICES; LOSS OF USE,
 *  DATA, OR PROFITS; OR BUSINESS INTERRUPTION) HOWEVER CAUSED AND ON ANY
 *  THEORY OF LIABILITY, WHETHER IN CONTRACT, STRICT LIABILITY, OR TORT
 *  (INCLUDING NEGLIGENCE OR OTHERWISE) ARISING IN ANY WAY OUT OF THE USE
 *  OF THIS SOFTWARE, EVEN IF ADVISED OF THE POSSIBILITY OF SUCH DAMAGE.
 *  
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
          try {
            cycle();
          } catch (Throwable e) {
            logger.error("Uncaught exception for "+callback, e);
          }
        }}, name);
  }

  // useful counters to tell if everything is getting dealt with
  private int ic = 0;           // sync on inQ
  private int oc = 0;           // sync on inQ

  /** Used to (re)start the service thread to handle queued YP messages **/
  private void wake() {
    thread.start();
  }

  /** queue an incoming message for later handling **/
  public void addMessage(Message m) {
    synchronized(inQ) {
      ic++;
      if (logger.isDebugEnabled()) {
        logger.debug(name+" Queuing Message "+ic);
      }

      inQ.add(m);
    }
    // should this be in the sync?
    wake();
  }

  private final static boolean batchRequests = false;

  private void cycle() {
    if (batchRequests) {
      // service everything we can
      while (true) {
        if (! dispatchNext()) {
          return;
        }
      }
    } else {
      if (! dispatchNext() ) {
        return;
      }
      wake();
    }
  }

  private boolean dispatchNext() {
    Message m;
    // get the next message
    synchronized (inQ) {
      m = (Message) inQ.next();
      if (m == null) {
        // exit the loop if we are done
        return false;
      }
      
      oc++;
      if (logger.isDebugEnabled()) {
        logger.debug(name+" Handling Message "+oc);
      }
    }
    
    // handle it outside the inQ lock
    callback.dispatch(m);
    return true;
  }
}
