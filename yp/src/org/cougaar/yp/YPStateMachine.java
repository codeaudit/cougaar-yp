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

import java.util.Properties;
import java.util.Vector;

import org.cougaar.core.service.ThreadService;
import org.cougaar.core.thread.Schedulable;
import org.cougaar.util.StackMachine;
import org.cougaar.util.log.Logger;
import org.cougaar.util.log.Logging;
import org.uddi4j.datatype.tmodel.TModel;
import org.uddi4j.response.AuthToken;
import org.uddi4j.response.TModelDetail;

/**
 **/

public class YPStateMachine extends StackMachine {
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
  private final YPProxy yp;
  protected final YPProxy getYPProxy() { return yp; }
  private final ThreadService threads;
  protected final ThreadService getThreadService() { return threads; }

  public YPStateMachine(YPService yps, YPProxy yp, ThreadService threads) {
    this.yps = yps;
    this.yp = yp;
    this.threads = threads;
    ypproperties.put("username", UDDI_USERID);
    ypproperties.put("password", UDDI_PASSWORD);
    initThread();
    init();
    reset();
  }

  /** set a property for just this instance of YPStateMachine.
   * Current properties are "username" and "password".
   * @note that it is undefined to change existing values after initialization.
   **/
  public void setProperty(String name, String value) {
    ypproperties.put(name, value);
  }

  private Schedulable thread = null;

  private AuthToken token = null;
  protected AuthToken getAuthToken() { return token; }

  /** restart the thread **/
  protected synchronized void kick() {
    thread.start();
  }

  /** Sets the machine in motion.  Will continue until DONE (or an error state) is achieved **/
  public void start() {
    kick();
  }

  /** Set/Reset the machine to the starting state.
   * By default, sets the state to "START", but subclasses may override.
   **/
  public void reset() {
    set("START");
  }

  /** initialize the states of the machine.
   * called near the end of the constructor, followed by reset().
   **/
  protected void init() {
    // called with call("saveTModel", tModels, nexttag);
    // result (at nexttag) will be in Object SM.retval;
    addYPQ("saveTModel", "POP", new YPQ() {
        public YPFuture get(Frame f) {
          Object arg = f.getArgument();
          Vector tModels;
          if (arg instanceof Vector) {
            tModels = (Vector) arg;
          } else {
            tModels = new Vector(1);
            tModels.addElement(arg);
          }
          
          return yp.save_tModel(token.getAuthInfoString(), tModels);
        }
        public void set(Frame f, Object r) {
          f.setRetval((TModelDetail) r);
        }
        public void handle(Frame f, Exception e) {
	  f.setVar("YPErrorException", e);
	  f.setVar("YPErrorText", "Error in saveTModel");
	  transit("YPError");
        }
      });

    addYPQ("getAuthToken", "POP", new YPQ() {
        public YPFuture get(Frame f) { 
          Properties p = (Properties) f.getArgument();
          if (p == null) p = ypproperties;
          String username = p.getProperty("username");
          String password = p.getProperty("password");
          return yp.get_authToken(username, password); 
        }
        public void set(Frame f, Object r) {
          token = (AuthToken) r; 
          f.setRetval(token);
        }
        public void handle(Frame f, Exception e) {
	  f.setVar("YPErrorException", e);
	  f.setVar("YPErrorText", "Error in getAuthToken");
	  transit("YPError");
        }
      });

    addYPQ("discardAuthToken", "POP", new YPQ() {
        public YPFuture get(Frame f) {
          AuthToken t = (AuthToken) f.getArgument();
          if (t == null) t = token;
          return yp.discard_authToken(t.getAuthInfoString()); 
        }
        public void set(Frame f, Object r) { 
          AuthToken t = (AuthToken) f.getArgument();
          if (t == token || t == null) token = null;
        }

        public void handle(Frame f, Exception e) {
          AuthToken t = (AuthToken) f.getArgument();
          if (t == null) t = token;
          log.error("Exception in discardAuthToken("+t+")", e);
          if (t == token || t == null) token = null;
          transit("POP");
        }
      });

    // this just sends logs the error, but extenders may override
    addLink("YPError", "handleYPError");
    add(new SState("handleYPError") {
      public void invoke() {
	log.error("Exception in " + getVar("YPErrorText"), (Exception) getVar("YPErrorException"));
      }
    });


  }

  public synchronized void set(State s) { // sync to make sure we aren't still cleaning up
    super.set(s); 
  }

  private void initThread() {
    thread = getThreadService().getThread(this, new Runnable() {
        public void run() {
          try {
            //log.warn("running YPStateMachine.go()");
            go();
          } catch (RuntimeException e) {
            handleException(e);
          }
        }
      });
  }

  protected void handleException(Exception e) {
    /*
    log.error("YPStateMachine caught Exception. Will reset.", e);
    reset();
    */
    log.error("Caught Exception - machine is dead", e);
  }
    

  /** abstraction of an asynchronous YP Query **/
  public interface YPQ {
    /** return a YP Query to submit, in the form of a YPFuture **/
    YPFuture get(Frame f);
    /** consume the result of a complete (isReady) YP query **/
    void set(Frame f, Object result);
    /** consume an exception if one happens.  Must do the proper transit **/
    void handle(Frame f, Exception e);
  }

  protected void addYPQ(String startTag, final String nextTag, final YPQ ypq) {
    add(new SState(startTag) { public void invoke() {
      final Frame frame = getFrame();
      final YPFuture fut;
      try {
        fut = ypq.get(frame);
      } catch (Exception e) {
        if (log.isInfoEnabled()) {
          log.info("Caught exception in YPQ.get() "+ypq, e);
        }
        ypq.handle(frame, e);
        return;
      }

      YPFuture.Callback cab = new YPFuture.Callback() { public void ready(YPFuture r) {
        try {
          if (r != fut) {
            log.error(this.toString()+" expected "+fut+" instead of "+r);
          }
          if (r.isReady()) {
            ypq.set(frame, r.get());
            transit(nextTag);
            kick();
          } else {
            ypq.handle(frame, new RuntimeException("YPQ notified but not really ready!"));
          }
        } catch (Exception re) {
          log.error("Caught exception from YP during kick() in "+(YPStateMachine.this)+
                    " with YPFuture "+r, re);
          ypq.handle(frame, re);
        }
      }};
      fut.setCallback(cab);
      try {
        yps.submit(fut);
      } catch (RuntimeException e) {
        if (log.isInfoEnabled()) {
          log.info("Caught exception in YPQ.submit() "+ypq, e);
        }
        ypq.handle(frame, e);
      }
    }});
  }


  /** Standard TModel publish interaction **/
  public interface TModelThunk {
    TModel make(Frame f);
    TModel update(Frame f, TModelDetail tmd);
  }

  protected void addTModelPush(String tag, final String exit, final TModelThunk thunk) {
    final String t0 = tag;
    final String t1 = tag+" (update)";
    
    add(new SState(t0) {
        public void invoke() {
          TModel tm = thunk.make(getFrame());
          call("saveTModel", tm, t1);
        }});
    add(new SState(t1) {
        public void invoke() {
          TModelDetail tModelDetail = (TModelDetail) getResult();
          try {
            TModel nt = thunk.update(getFrame(),tModelDetail);
            call("saveTModel", nt, exit);
          } catch (RuntimeException re) {
            log.error("Caught exception", re);
            transit("ERROR");
          }
        }});
  }
}









