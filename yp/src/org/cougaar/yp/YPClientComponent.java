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

import javax.naming.directory.Attribute;
import javax.naming.directory.Attributes;

import org.cougaar.core.component.*;


import org.cougaar.core.agent.*;
import org.cougaar.core.agent.service.MessageSwitchService;
import org.cougaar.core.blackboard.*;
import org.cougaar.core.mts.Message;
import org.cougaar.core.mts.MessageAddress;
import org.cougaar.core.mts.MessageHandler;
import org.cougaar.core.service.BlackboardService;
import org.cougaar.core.service.ThreadService;
import org.cougaar.core.service.community.Community;
import org.cougaar.core.service.community.CommunityResponse;
import org.cougaar.core.service.community.CommunityResponseListener;
import org.cougaar.core.service.community.CommunityService;
import org.cougaar.core.service.community.Entity;
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
  private CommunityService communityService;

  public void setThreadService(ThreadService ts) { this.threadService = ts; }
  public void setCommunityService(CommunityService cs) { this.communityService = cs; }

  public void initialize() {
    super.initialize();

    // this should probably go into load

    ServiceBroker sb = getServiceBroker();
    mss = (MessageSwitchService) sb.getService(this,MessageSwitchService.class, null);
    this.originMA = mss.getMessageAddress();

    startServiceThread();

    // need to hook into the Agent MessageHandler protocol
    MessageHandler mh = new MessageHandler() {
        public boolean handleMessage(Message message) {
          if (message instanceof YPResponseMessage) {
            getServiceThread().addMessage(message);
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
    //System.err.println("YPClientComponent/"+originMA+" initialized.");
  }

  //
  // Service thread for incoming (response) messages
  //
  private ServiceThread serviceThread = null;
  private void startServiceThread() {
    serviceThread = new ServiceThread(
                                      new ServiceThread.Callback() {
                                        public void dispatch(Message m) {
                                          dispatchResponse((YPResponseMessage)m);
                                        }},
                                      logger,
                                      "YPClient("+originMA+")");
    serviceThread.start(threadService);
  }

  protected ServiceThread getServiceThread() { 
    return serviceThread;
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
    /** Get a UDDIProxy for YP Queries **/
    public YPProxy getYP(String ypAgent) {
      return new YPProxyImpl(MessageAddress.getMessageAddress(ypAgent), this, 
			     false);
    }
    /** Get a UDDIProxy for YP Queries **/
    public YPProxy getYP(MessageAddress ypAgent) {
      return new YPProxyImpl(ypAgent, this, false);
    }
    /** Get a UDDIProxy for YP Queries **/
    public YPProxy getYP(Community community) {
      return new YPProxyImpl(community, this, false, 
			     YPProxy.SearchMode.HIERARCHICAL_COMMUNITY_SEARCH);
    }

    /** Get a UDDIProxy for YP Queries **/
    public YPProxy getYP(Community community, int searchMode) {
      return new YPProxyImpl(community, this, false, searchMode);
    }

    /** Get a UDDIProxy for YP Queries **/
    public YPProxy getYP() {
      return new YPProxyImpl(this, false,
			     YPProxy.SearchMode.HIERARCHICAL_COMMUNITY_SEARCH);
    }

    /** Get a UDDIProxy for YP Queries **/
    public YPProxy getYP(int searchMode) {
      return new YPProxyImpl(this, false, searchMode);
    }

    public YPProxy getAutoYP(String ypAgent) {
      return new YPProxyImpl(MessageAddress.getMessageAddress(ypAgent), this, 
			     true);
    }
    public YPProxy getAutoYP(MessageAddress ypAgent) {
      return new YPProxyImpl(ypAgent, this, 
			     true);
    }
    public YPProxy getAutoYP(Community community) {
      return new YPProxyImpl(community, this, 
			     true, 
			     YPProxy.SearchMode.HIERARCHICAL_COMMUNITY_SEARCH);
    }

    public YPFuture submit(YPFuture r) {
      try {
        YPClientComponent.this.submitFromService(r);
        return r;
      } catch (TransportException te) {
        throw new RuntimeException("submit nested exception", te);
      }
    }

    public void nextYPServerContext(final Object currentContext,
				      final NextContextCallback callback) {
      YPClientComponent.this.nextYPServerContext(currentContext,
						 callback);
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
      //System.err.println("YPLP ping");
      thread.start();
    }

    void start() {
      thread = threadService.getThread(this,
                                       new Runnable() {
                                         public void run() { cycle(); }},
                                       "YPLP("+originMA.toString()+")");
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
      //System.err.println("YPLP pong");
      try {
        blackboard.openTransaction();
        scan();
      } finally {
        blackboard.closeTransaction();
      }
    }      

    // must be called within transaction - e.g. only from cycle or init
    void scan() {
      //System.err.println("YPLP scan");
      for (Iterator it = futures.getAddedCollection().iterator(); it.hasNext(); ) {
        YPFuture fut = (YPFuture) it.next();
        try {
          //logger.warn("YPLP submitting "+fut);
          //System.err.println("YPLP submitting "+fut);
          YPClientComponent.this.submitFromBlackboard(fut);
        } catch (TransportException te) {
          logger.error("YPFuture submit failed ("+fut+")", te);
          blackboard.publishChange(fut);
        }
      }
    }

    void kickFuture(YPFuture fut) {
      //logger.warn("YPLP kicking "+fut);
      //System.err.println("YPLP kicking "+fut);
      try {
        blackboard.openTransaction();
        //System.err.println("YPLP changing "+fut);
        blackboard.publishChange(fut);
      } finally {
        blackboard.closeTransaction();
      }
      //System.err.println("YPLP change closed "+fut);
    }      
  }

  // 
  // resolver
  //


  void sendMessage(Message m) {
    mss.sendMessage(m);
  }

  private static final String YP_AGENT_FILTER = "(Role=YPServer)";

  /** Convert a context to a MessageAddress supporting the YP application **/
  protected MessageAddress lookup(Object context) {
    if (context instanceof MessageAddress) {
      return ((MessageAddress) context);
    } else if (context instanceof Community) {
      Set ypAgents = 
	((Community) context).search(YP_AGENT_FILTER, Community.AGENTS_ONLY);
      if (logger.isDebugEnabled()) {
	logger.debug("lookup: ypAgents " + ypAgents + " size = " + ypAgents.size());
      }
      
      
      for (Iterator iterator = ypAgents.iterator();
	   iterator.hasNext();) {
	MessageAddress ma = 
	  MessageAddress.getMessageAddress(((Entity) (iterator.next())).getName());
	if ((iterator.hasNext()) && (logger.isDebugEnabled())) {
	  logger.debug(context + " Community has multiple YP servers. Using " +
			 ma.toString());
	}
	return ma;
      }

      // If we got to here => no YPServer
      if (logger.isDebugEnabled()) {
	logger.debug(context + " Community does not have any YP servers.");
      } 
      throw new NoYPServerException(originMA + 
				    ": unable to find YPServer community for " + 
				    context);
    } else {
      throw new IllegalArgumentException("Unrecognized context type " + 
					 context.getClass() + 
					 ". Must be either MessageAddress or Community.");
    }
  }


  /** Find the next context to search.
   * @param currentContext current YP context
   * @param callback callback.invoke(Object) called with the next context. 
   * Next context will be null if there is no next context.
   * @note callback.invoke may be called from within nextYPServerContext
   **/
  private void nextYPServerContext(final Object currentContext,
				   final YPService.NextContextCallback callback) {
    
    if ((currentContext != null) &&
        (!(currentContext instanceof Community))) {
      // nowhere to go if not using a community context
      callback.setNextContext(null);
      return;
    }

    
    CommunityResponseListener crl = new CommunityResponseListener() {
      public void getResponse(CommunityResponse resp){
	// Found the parents so reenter with the same context
	nextYPServerContext(currentContext,
			    callback);
      }
    };
    
    Collection parents;

    if (currentContext == null) {
      String [] parentNames = communityService.getParentCommunities(false);

      if (parentNames != null) {
	parents = Arrays.asList(parentNames);
      } else {
	parents = null;
      }
    } else {
      if (logger.isDebugEnabled()) {
	logger.debug("nextYPServerContext: attributes for " +
		     currentContext + " " + 
		     ((Community) currentContext).getAttributes());
      }

      parents =
	communityService.listParentCommunities(((Community) currentContext).getName(), 
					       crl);
    }
    
    if (logger.isDebugEnabled()) {
      logger.debug("nextYPServerContext: listParentCommunities(" + 
		   currentContext + ") returned " +
		   parents);
    };

    if (parents == null) { 
      // waiting on community callback so let callbacks handle it
      return;
    } else if (parents.size() == 0) {
      // No more parents
      callback.setNextContext(null);
      return;
    }
    
    for (Iterator iterator = parents.iterator();
	 iterator.hasNext();) {
      String parentName = (String) iterator.next();
      
      crl = new CommunityResponseListener() {
	public void getResponse(CommunityResponse resp){
	  Community parent = (Community) resp.getContent();
	  
	  if (ypCommunity(parent)) {
	    if (ypServer(parent)) {
	      callback.setNextContext(parent);
	    } else {
	      nextYPServerContext(parent, callback);
	    }
	  } 
	}
      };
      
      Community parent = communityService.getCommunity(parentName, crl);

      if (logger.isDebugEnabled()) {
	logger.debug("nextYPServerContext: getCommunity(" + 
		     parentName + ") returned " +
		     parent);
      };
      
      if (parent != null) {
	if (ypCommunity(parent)) {
	  if (ypServer(parent)) {
	    callback.setNextContext(parent);
	    return;
	  } else {	
	    if (logger.isDebugEnabled()) {
	      logger.debug("nextYPServerContext for " + 
			   currentContext + " recursing to  " + parent);
	    }   
	    nextYPServerContext(parent, callback);
	    return;
	  }
	}
      } else {
	if (logger.isDebugEnabled()) {
	  logger.debug("nextYPServerContext: waiting on community  for " + 
		       parentName);
	}
      }
    }

    // BOZO - if there are errors in the nesting this may give weird results.
    // Multiple ypCommunity parents with different YPServerAgents will mean 
    // multiple setNextContext callbacks. 
    // Will never get a callback if ypCommunity chain does not have a 
    // YPServerAgent and ends with a non ypCommunity.
  }

  protected void handleNextContext(YPFutureImpl query, Object currentContext,
				   Object nextContext) {
    if (nextContext != null) {
      if (logger.isDebugEnabled()) {
	logger.debug("Continuing resolver search, next context = " + 
		     nextContext);
      }
      try {
	track(query, nextContext);
      } catch (TransportException ex) {
	if (logger.isDebugEnabled()) {
	  logger.debug("Failing resolver search, next context = " + 
		       nextContext, ex);
	}
	// really shouldn't happen unless there is something broken with the context tree
	query.setFinalContext(currentContext);
	query.setException(ex);
	kickLP(query);
      } 
    } else {
      if (logger.isDebugEnabled()) {
	logger.debug("Failed search with no result");
      }
      query.setFinalContext(currentContext); // last queried context
      query.set(null);      // nobody answered
      kickLP(query);
    }
  }

  private static final String YP_COMMUNITY_FILTER = "(CommunityType=YPCommunity)";
  
  public static boolean ypCommunity(Community community) {
    Attributes attributes = community.getAttributes();
    Attribute communityType = attributes.get("CommunityType");


    if (communityType == null) {
      logger.error("ypCommunity: " + community + 
		   " does not have a CommunityType attribute");
      logger.error(community + " attributes " + attributes);
      return false;
    }

    if (logger.isDebugEnabled()) {
      logger.debug("ypCommunity: returning " + 
		   communityType.contains("YPCommunity") +
		   " for " + community);
    }
				 
    return communityType.contains("YPCommunity");
  }

  public static boolean ypServer(Community community) {
    if (ypCommunity(community)) {
      Attributes attributes = community.getAttributes();
      Attribute ypServerAgent = attributes.get("YPServerAgent");

      if (ypServerAgent == null) {
	if (logger.isDebugEnabled()) {
	  logger.debug("ypServer: returning false for " + 
		       community);
	}

	return false;
      }

      if (logger.isDebugEnabled()) {
	logger.debug("ypServer: returning " + 
		     (ypServerAgent.size() > 0) +
		     " for " + community);
      }

      return ypServerAgent.size() > 0;
    } 
    return false;
  }




  /** Return true IFF the element represents an actual answer (or positive failure) **/
  boolean isResponseComplete(YPFuture r, Element e) {
    return ((r.getSearchMode() != 
	     YPProxy.SearchMode.HIERARCHICAL_COMMUNITY_SEARCH) ||
	    (e != null));
  }

  /** (maybe) Tell the appropriate LogicProvider that the query is complete
   * and any subscribers need to be told to wake up
   **/
  void kickLP(YPFuture r) {
    //System.err.println("YPCLient KICK "+r);
    if (((YPFutureImpl) r).isFromBlackboard()) {
      // only invoke the subscription kicker if it was submitted that way.
      lp.kickFuture(r);
    }
  }

  /** Key counter.  Access locked by selects.  **/
  private long counter = System.currentTimeMillis();
  /** Keep track of outstanding requests.  Also used as sync lock for counter and selects itself. **/
  private final HashMap selects = new HashMap(11); // assume not too many at a time
    
  void submitFromBlackboard(YPFuture r) throws TransportException {
    ((YPFutureImpl) r).setIsFromBlackboard(true);
    submit(r);
  }
  void submitFromService(YPFuture r) throws TransportException {
    submit(r);
  }

  /** Submit a request.
   */
  private void submit(final YPFuture r) throws TransportException {
    ((YPFutureImpl) r).submitted();

    
    if (!(r.getInitialContext() == null)) {
      // Assume we know where to start
      track(r, r.getInitialContext());
    } else {
      YPService.NextContextCallback callback = 
	new YPService.NextContextCallback() {
	public void setNextContext(Object context){
	  if (context != null) {
	    try {
	      // Found the ypserver community so go on
	      if (logger.isDebugEnabled()) {
		logger.debug("YPService.NextClientCallback.setNextContext- query - " + r +
			     " context - " + context);
	      }
	      track(r, context);
	    } catch (TransportException te) {
	      logger.error("Unable to submit YP interaction to " + context, 
			   te);
	      ((YPFutureImpl) r).setFinalContext(r.getInitialContext());
	      ((YPFutureImpl) r).setException(te);
	      kickLP(r);
	    } 
	  } else {
	    NoYPServerException nypse = 
	      new NoYPServerException(originMA + 
				      ": unable to find YPServer community");
	    ((YPFutureImpl) r).setFinalContext(r.getInitialContext());
	    ((YPFutureImpl) r).setException(nypse);
	    kickLP(r);
	  }
	}
      };

      nextYPServerContext(r.getInitialContext(), 
			  callback);
    }
  }

  /** Track a single message, implicitly watching the whole resolver chain **/
  void track(YPFuture r, Object context) throws TransportException {
    Tracker t;

    synchronized (selects) {
      Object key = new Long(counter++);
      t = new Tracker(r, context, key);
      selects.put(key, t);
    }
    
    t.send();
  }

  private int rc = 0;

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
    rc++;
    //System.err.println("YPClient RES="+rc);
    
  }


  /** 
   *
   **/
  class Tracker {
    private final YPFutureImpl query;
    private final Object context;
    private final Object key;

    Tracker(YPFuture r, Object context, Object key) {
      this.query = (YPFutureImpl) r; // always an impl
      this.context = context;
      this.key = key;
    }
    
    void send() throws TransportException {
      try {
        if (logger.isDebugEnabled()) {
          logger.debug("Tracker.send: sending YPQueryMessage - origin " + originMA +
                       " context " + context);
	  if (context == null) {
	    RuntimeException re = 
	      new RuntimeException("Null context in Tracker.send.");
	    re.printStackTrace();
	  }
        }
        MessageAddress ma = lookup(context);

	if (logger.isDebugEnabled()) {
	  logger.debug(originMA + " lookup(" + context +
		       " return ma ");
	}
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
	if (logger.isDebugEnabled()) {
	  re.printStackTrace();
	}
        throw new TransportException(re);
      }
    }

    void receiveResponse(Element result) {
      //System.err.println("YPCLient GOT");

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

	YPService.NextContextCallback callback = 
	  new YPService.NextContextCallback() {
	  public void setNextContext(Object nextContext) {
	    handleNextContext(query, context, nextContext);
	  }
	};

        // keep going
        nextYPServerContext(context, callback);
      }
    }

  }
}

