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

import org.uddi4j.client.*;
import org.uddi4j.transport.*;

import org.w3c.dom.Element;

import java.io.*;
import java.net.*;
import java.util.*;

import org.cougaar.core.component.*;

import org.cougaar.core.mts.*;
import org.cougaar.core.agent.*;
import org.cougaar.core.agent.service.MessageSwitchService;

import org.cougaar.core.blackboard.*;
import org.cougaar.core.service.BlackboardService;
import org.cougaar.core.service.ThreadService;
import org.cougaar.core.thread.*;
import org.cougaar.util.*;


import org.cougaar.util.log.*;

/** An Agent-level Component which implements the client-side of the Cougaar
 * YellowPages Application.
 * @note this version supports only Component-model Service style access,
 * and only synchronous queries.  Future versions will supply asynchronous
 * modes and blackboard-style interaction as well.
 **/

public class YPClientComponent extends ComponentSupport {
  private static final Logger logger = Logging.getLogger(YPClientComponent.class);  

  private MessageSwitchService mss = null;
  private YPServiceProvider ypsp;
  private MessageAddress originMA;
  private BlackboardService blackboard;;
  private YPLP lp;
  private ThreadService threadService;

  public void setThreadService(ThreadService ts) { this.threadService = ts; }

  public void initialize() {
    super.initialize();

    // this should probably go into load

    ServiceBroker sb = getServiceBroker();
    mss = (MessageSwitchService) sb.getService(this,MessageSwitchService.class, null);
    this.originMA = mss.getMessageAddress();

    // need to hook into the Agent MessageHandler protocol
    MessageHandler mh = new MessageHandler() {
        public boolean handleMessage(Message message) {
          if (message instanceof YPResponseMessage) {
            dispatchResponse((YPResponseMessage) message);
            return true;
          }
          return false;
        }
      };

    mss.addMessageHandler(mh);

    lp = new YPLP();
    blackboard = (BlackboardService) sb.getService(lp,BlackboardService.class, null);
    lp.start();

    ypsp = new YPServiceProvider();
    sb.addService(YPService.class, ypsp);

    System.err.println("YPClientComponent/"+originMA+" initialized.");

  }


  //
  // YPService
  //

  private class YPServiceProvider implements ServiceProvider {
    public YPServiceProvider() {}

    public Object getService(ServiceBroker sb, Object requestor, Class serviceClass) {
      if (YPService.class.isAssignableFrom(serviceClass)) {
        return new YPServiceImpl();
      } else {
        return null;
      }
    }

    public void releaseService(ServiceBroker sb, Object requestor, Class serviceClass, Object service) {
      // drop the service.
    }
  }

  private class YPServiceImpl implements YPService {
    /** Get a UDDIProxy for YP Queryies **/
    public YPProxy getYP(String context) {
      return new YPProxyImpl(context, this, false);
    }
    public YPProxy getAutoYP(String context) {
      return new YPProxyImpl(context, this, true);
    }
    public YPFuture submit(YPFuture r) {
      try {
        YPClientComponent.this.submit(r);
        return r;
      } catch (TransportException te) {
        throw new RuntimeException("submit nested exception", te);
      }
    }
  }


  //
  // blackboard client
  // 
  
  private class YPLP implements BlackboardClient {
    public String getBlackboardClientName() { return "YPClient"; }
    public long currentTimeMillis() { return System.currentTimeMillis(); }
    
    IncrementalSubscription futures;
    SubscriptionWatcher watcher;
    Schedulable thread = null;

    private void signal() {
      thread.start();
    }

    void start() {
      thread = threadService.getThread(this, new Runnable() {
          public void run() {
              cycle();
          }}, originMA.toString()+"YP");
      init();
      signal();
    }

    void init() {
      watcher = new SubscriptionWatcher() {
          public void signalNotify(int event) {
            //logger.warn("YPLP signalNotify");
            requestCycle();
          }
          public String toString() {
            return "YPWatcher("+originMA+")";
          }
        };

      blackboard.registerInterest(watcher);

      try {
        //logger.warn("YPLP subscribe");
        blackboard.openTransaction();

        futures = (IncrementalSubscription) blackboard.subscribe(new UnaryPredicate() {
            public boolean execute(Object o) { return (o instanceof YPFuture); }
          });
        scan();
      } finally {
        blackboard.closeTransaction();
      }
      signal();
    }

    void requestCycle() {
      //logger.warn("YPLP requestCycle"); 
      signal();
    }

    void cycle() {
      //logger.warn("YPLP cycle");
      try {
        blackboard.openTransaction();
        scan();
      } finally {
        blackboard.closeTransaction();
      }
    }      

    void scan() {
      for (Iterator it = futures.getAddedCollection().iterator(); it.hasNext(); ) {
        YPFuture fut = (YPFuture) it.next();
        try {
          //logger.warn("YPLP submitting "+fut);
          YPClientComponent.this.submit(fut);
        } catch (TransportException te) {
          logger.warn("YPFuture submit failed ("+fut+")", te);
          blackboard.publishChange(fut);
        }
      }
    }

    void kickFuture(YPFuture fut) {
      //logger.warn("YPLP kicking "+fut);
      try {
        blackboard.openTransaction();
        blackboard.publishChange(fut);
      } finally {
        blackboard.closeTransaction();
      }
    }      
  }

  // 
  // resolver
  //


  void sendMessage(Message m) {
    mss.sendMessage(m);
  }


  /** Convert a context to a MessageAddress supporting the YP application **/
  MessageAddress lookup(String context) {
    // this should be a WP lookup like:
    // return wp.lookupName(context, "YP");
    return MessageAddress.getMessageAddress(context);
  }

  /** Find the next context to search.
   * @return null if we're out of contexts.
   **/
  String nextContext(YPFuture query, String currentContext) {
    // no nesting right now, but Community should provide this information,
    // or maybe it is built in.
    //return communityService.getParentCommunity(context);
    return null;
  }

  /** return true IFF the element represents an actual answer (or positive failure) **/
  boolean isResponseComplete(YPFuture r, Element e) {
    return (e != null);
  }

  /** (maybe) Tell the appropriate LogicProvider that the query is complete
   * and any subscribers need to be told to wake up
   **/
  void kickLP(YPFuture r) {
    lp.kickFuture(r);
  }

  /** Key counter.  Access locked by selects.  **/
  private long counter = System.currentTimeMillis();
  /** Keep track of outstanding requests.  Also used as sync lock for counter and selects itself. **/
  private final HashMap selects = new HashMap(11); // assume not too many at a time
    
  /** Submit a request.
   */
  void submit(YPFuture r) throws TransportException {
    ((YPFutureImpl) r).submitted();
    track(r, r.getInitialContext());
  }

  /** Track a single message, implicitly watching the whole resolver chain **/
  Tracker track(YPFuture r, String context) throws TransportException {
    Tracker t;

    synchronized (selects) {
      Object key = new Long(counter++);
      t = new Tracker(r, context, key);
      selects.put(key, t);
    }
    
    t.send();
    return t;
  }

  /** dispatch the response to the appropriate listener **/
  private void dispatchResponse(YPResponseMessage r) {
    Object key = r.getKey();
    Element el = r.getElement();

    Tracker tracker;
    synchronized (selects) {
      tracker = (Tracker) selects.remove(key);
    }

    if (tracker == null) {
      logger.warn("dispatchResponse(): Cannot find tracker for key "+ key +
                  " el " + r.getElement() +
                  " source " + r.getOriginator() + 
                  " destination " + r.getTarget());
    } else {
      if (logger.isDebugEnabled()) {
        logger.debug("dispatchResponse(): YPResonseMessage - key " + key +
                     " el " + r.getElement() +
                     " source " + r.getOriginator() + 
                     " destination " + r.getTarget() +
                     " = "+el);
      }

      tracker.receiveResponse(el);
    }
  }

  /** 
   *
   **/
  class Tracker {
    private final YPFutureImpl query;
    private final String context;
    private final Object key;

    Tracker(YPFuture r, String context, Object key) {
      this.query = (YPFutureImpl) r; // shouldn't be possible to get to this point, but you never know
      this.context = context;
      this.key = key;
    }
    
    void send() throws TransportException {
      try {
        MessageAddress ma = lookup(context);
        Element el = query.getElement();
        boolean iqp = query.isInquiry();
        YPQueryMessage m = new YPQueryMessage(originMA, ma, el, iqp, key);
        if (logger.isDebugEnabled()) {
          logger.debug("Tracker.send: sending YPQueryMessage - origin " + originMA +
                       " target " + ma +
                       " el " + el + 
                       " key "  + key);
        }
        sendMessage(m);
      } catch (RuntimeException re) {
        throw new TransportException(re);
      }
    }

    void receiveResponse(Element result) {
      if (isResponseComplete(query, result)) {
        if (logger.isDebugEnabled()) {
          logger.debug("Tracker "+key+" waking with "+result);
        }
        // we got THE answer.  deal with it.
        query.setFinalContext(context); // where did the answer come from?
        query.set(result);
        kickLP(query);
      } else {
        if (logger.isDebugEnabled()) {
          logger.debug("Tracker "+key+" continuing resolver search");
        }
        // keep going
        String nc = nextContext(query, context);
        if (nc != null) {
          if (logger.isDebugEnabled()) {
            logger.debug("Tracker "+key+" continuing resolver search, next context = "+nc);
          }
          try {
            track(query, nc);
          } catch (TransportException ex) {
            if (logger.isDebugEnabled()) {
              logger.debug("Tracker "+key+" failing resolver search, next context = "+nc, ex);
            }
            // really shouldn't happen unless there is something broken with the context tree
            query.setFinalContext(context);
            query.setException(ex);
            kickLP(query);
          } 
        } else {
          if (logger.isDebugEnabled()) {
            logger.debug("Tracker "+key+" failed search with no result");
          }
          query.setFinalContext(context); // last queried context
          query.set(null);      // nobody answered
          kickLP(query);
        }
      }
    }
  }
}
