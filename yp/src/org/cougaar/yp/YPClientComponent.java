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

import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Set;

import javax.naming.directory.Attribute;
import javax.naming.directory.Attributes;

import org.cougaar.core.agent.service.MessageSwitchService;
import org.cougaar.core.blackboard.BlackboardClient;
import org.cougaar.core.blackboard.IncrementalSubscription;
import org.cougaar.core.blackboard.SubscriptionWatcher;
import org.cougaar.core.component.ComponentSupport;
import org.cougaar.core.component.ServiceBroker;
import org.cougaar.core.component.ServiceProvider;
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
import org.cougaar.core.thread.Schedulable;
import org.cougaar.util.UnaryPredicate;
import org.cougaar.util.log.Logger;
import org.cougaar.util.log.Logging;
import org.uddi4j.transport.TransportException;
import org.w3c.dom.Element;

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

  public void load() {
    super.load();

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

    public void getYPServerContext(final String agentName,
				   final NextContextCallback callback) {
      YPClientComponent.this.getYPServerContext(agentName,
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
      //if (logger.isDebugEnabled()) { logger.debug("LogicProvider signal()"); }
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
            super.signalNotify(event);
            //if (logger.isDebugEnabled()) { logger.debug("LogicProvider signalNotify()"); }
            requestCycle();
          }
          public String toString() {
            return "YPWatcher("+originMA+")";
          }
        };

      blackboard.registerInterest(watcher);

      try {
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
      //if (logger.isDebugEnabled()) { logger.debug("LogicProvider requestCycle()"); }
      signal();
    }

    void cycle() {
      //if (logger.isDebugEnabled()) { logger.debug("LogicProvider cycle()"); }
      try {
        blackboard.openTransaction();
        scan();
      } finally {
        blackboard.closeTransaction();
      }
    }      

    // total hack to see if we're missing anything
    // ...
    private HashMap bogon = new java.util.HashMap(11);
    void see(Object o) { 
      synchronized(bogon) { 
        if (bogon.get(o) != null) { logger.warn("detected duplicate add of "+o); }
        bogon.put(o,o);
      }
    }
    void deja() {
      synchronized (bogon) {
        for (Iterator it = futures.iterator(); it.hasNext(); ) {
          Object o = it.next();
          if (bogon.get(o) != o) { logger.warn("detected missed add of "+o); bogon.put(o,o);}
        }
      }
    }
    // ...
      

    // must be called within transaction - e.g. only from cycle or init
    void scan() {
      for (Iterator it = futures.getAddedCollection().iterator(); it.hasNext(); ) {
        YPFuture fut = (YPFuture) it.next();
        try {
          if (logger.isDebugEnabled()) { logger.debug("LogicProvider scan() submitting "+fut); }
          see(fut);             // bogon
          YPClientComponent.this.submitFromBlackboard(fut);
        } catch (TransportException te) {
          logger.error("YPFuture submit failed ("+fut+")", te);
          blackboard.publishChange(fut);
        }
      }
      deja();                   // bogon
    }

    void kickFuture(YPFuture fut) {
      if (logger.isDebugEnabled()) { logger.debug("LogicProvider kickFuture("+fut+")"); }
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
  protected MessageAddress lookup(Object context) {
    if (context instanceof MessageAddress) {
      return ((MessageAddress) context);
    } else if (context instanceof Community) {
      Set ypAgents = ypServers((Community) context);

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


  /** Find the YP server context for the specified agent
   * @param agentName agent
   * @param callback callback.invoke(Object) called with the YP server context
   * for the specified agent
   * Next context will be null if there is no next context.
   * @note callback.invoke may be called from within getYPServerContext
   **/
  private void getYPServerContext(final String agentName,
				  final YPService.NextContextCallback callback) {
    
    if ((agentName == null) || (agentName.equals(""))) {
      // nowhere to go 
      callback.setNextContext(null);
      return;
    }

    
    CommunityResponseListener crl = new CommunityResponseListener() {
      public void getResponse(CommunityResponse resp){
	// Found the parents so reenter with the same context
	getYPServerContext(agentName,
			   callback);
      }
    };

    Collection parents = communityService.listParentCommunities(agentName, crl); 
    
    if (logger.isDebugEnabled()) {
      logger.debug("getYPServerContext: listParentCommunities(" + 
		   agentName + ") returned " +
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
    
    boolean waiting = false;
    boolean ypCommunity = false;

    for (Iterator iterator = parents.iterator();
	 iterator.hasNext();) {
      String parentName = (String) iterator.next();
      
      crl = new CommunityResponseListener() {
	public void getResponse(CommunityResponse resp){
	  Community parent = (Community) resp.getContent();
	  
	  if (ypCommunity(parent)) {
	    if (!ypServers(parent).isEmpty()) {
	      callback.setNextContext(parent);
	    }
	  } 
	}
      };
      
      Community parent = communityService.getCommunity(parentName, crl);

      if (logger.isDebugEnabled()) {
	logger.debug("getYPServerContext: getCommunity(" + 
		     parentName + ") returned " +
		     parent);
      };
      
      if (parent != null) {
	if (ypCommunity(parent)) {
	  ypCommunity = true;
	  if (!ypServers(parent).isEmpty()) {
	    callback.setNextContext(parent);
	    return;
	  } 
	}
      } else {
	waiting = true;
	if (logger.isDebugEnabled()) {
	  logger.debug("getYPServerContext: waiting on community  for " + 
		       parentName);
	}
      }
    }

    // List of parents did not include any yp communities
    if (!waiting && !ypCommunity) {
      if (logger.isDebugEnabled()) {
	logger.debug("getYPServerContext: no parent YPCommunity for " +
		     agentName);
      }
      callback.setNextContext(null);
      return;
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
      parents = communityService.listParentCommunities(null, crl); 
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
    
    boolean waiting = false;
    boolean ypCommunity = false;

    for (Iterator iterator = parents.iterator();
	 iterator.hasNext();) {
      String parentName = (String) iterator.next();
      
      crl = new CommunityResponseListener() {
	public void getResponse(CommunityResponse resp){
	  Community parent = (Community) resp.getContent();
	  
	  if (ypCommunity(parent)) {
	    if (!ypServers(parent).isEmpty()) {
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
	  ypCommunity = true;
	  if (!ypServers(parent).isEmpty()) {
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
	waiting = true;
	if (logger.isDebugEnabled()) {
	  logger.debug("nextYPServerContext: waiting on community  for " + 
		       parentName);
	}
      }
    }

    // List of parents did not include any yp communities
    if (!waiting && !ypCommunity) {
      if (logger.isDebugEnabled()) {
	logger.debug("nextYPServerContext: no parent YPCommunity for " +
		     currentContext);
      }
      callback.setNextContext(null);
      return;
    }
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

  private static final String YP_AGENT_FILTER = "(Role=YPServer)";

  public static Set ypServers(Community community) {
    if (!ypCommunity(community)) {
      return Collections.EMPTY_SET;
    }

    Set ypAgents = 
      (community).search(YP_AGENT_FILTER, Community.AGENTS_ONLY);
    if (logger.isDebugEnabled()) {
      logger.debug("ypServers: " + ypAgents + " size = " + ypAgents.size());
    }
    
    return ypAgents;
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
    if (logger.isDebugEnabled()) { logger.debug("kickLP("+r+")"); }
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
    if (logger.isDebugEnabled()) { logger.debug("submitFromBlackboard("+r+")");}
    ((YPFutureImpl) r).setIsFromBlackboard(true);
    submit(r);
  }
  void submitFromService(YPFuture r) throws TransportException {
    if (logger.isDebugEnabled()) { logger.debug("submitFromService("+r+")"); }
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
        logger.debug("dispatchResponse(): YPResponseMessage - key " + key +
                     " el " + r.getElement() +
                     " source " + r.getOriginator() + 
                     " destination " + r.getTarget() +
                     " = "+el);
      }

      tracker.receiveResponse(el);
    }
    rc++;
    
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
            logger.error("Null context in Tracker.send.", new Throwable());
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

