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
  private YPTransport transport;
  private WaitQueue wq = new WaitQueue();
  private MessageAddress originMA;
  private YPServiceProvider ypsp;
  private YPResolver resolver;

  protected void sendMessage(Message m) {
    mss.sendMessage(m);
  }

  public void initialize() {
    super.initialize();

    transport = new YPTransport();
    resolver = new YPResolver();

    // this should probably go into load

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
    ServiceBroker sb = getServiceBroker();
    mss = (MessageSwitchService) sb.getService(this,MessageSwitchService.class, null);
    mss.addMessageHandler(mh);
    originMA = mss.getMessageAddress();

    ypsp = new YPServiceProvider();
    sb.addService(YPService.class, ypsp);
  }


  /** dispatch the response to the appropriate listener **/
  private void dispatchResponse(YPResponseMessage r) {
    Object key = r.getKey();
    Element el = r.getElement();
    if (logger.isDebugEnabled()) {
      logger.debug("dispatchResponse(): YPResonseMessage - key " + key +
		   " el " + r.getElement() +
		   " source " + r.getOriginator() + 
		   " destination " + r.getTarget());
    }
    wq.trigger(key, el);
  }

  private Element resolverSend(Element el, URL url) throws TransportException {
    return resolver.send(el, url);
  }
  
  private MessageAddress convertURLtoMA(URL url) {
    return MessageAddress.getMessageAddress(URI.create(url.toString()));
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
      // no worries
    }
  }

  private final static String F_INQUIRY = "inquiry";
  private final static String F_PUBLISH = "publish";
  
  private class YPServiceImpl implements YPService {
    /** Get a UDDIProxy for YP Queryies **/
    public UDDIProxy getYP(String context) {
      // the Proxy returned should be a proxy which supports asynchronous operations
      UDDIProxy proxy = new UDDIProxy(transport); // BBN Extension to uddi4j

      // this is wrong, but what I've got until we've got a wp installed
      if (context == null) {
        context = "default";
      }
      try {
        URL iurl = new URL("http",context,F_INQUIRY);
        URL purl = new URL("https",context,F_PUBLISH);
        proxy.setInquiryURL(iurl);
        proxy.setPublishURL(purl);
      } catch (MalformedURLException e) { 
        // cannot happen
      }

      return proxy;
    }
  }

  // 
  // UDDI4J Connection
  //

  /** Stub UDDI4J transport to hook into our resolver backend **/
  private class YPTransport extends TransportBase {
    /** Send the DOM element specified to the URL as interpreted by the MTS **/
    public Element send(Element el, java.net.URL url) throws TransportException {
      return resolverSend(el, url);
    }
  }

  //
  // resolver
  //

  /** The resolver posts multiple queries to various YP servers until we 
   * get an answer or run out of servers.
   **/
  private class YPResolver {
    Element send(Element el, URL url) throws TransportException {
      String context = url.getHost();
      boolean isInquiry;
      {
        String f = url.getFile();
        if (F_INQUIRY.equals(f)) {
          isInquiry = true;
        } else if (F_PUBLISH.equals(f)) {
          isInquiry = false;
        } else {
          throw new RuntimeException("Couldn't reparse the URL \""+url+"\"");
        }
      }

      // resolver main loop
      String currentContext = context;
      while (currentContext != null) {
        MessageAddress ma = lookup(currentContext);

        Element response = sendOne(el, isInquiry, ma);
        if (isResponseComplete(response)) {
          return response;
        }

        currentContext = nextContext(currentContext);
      }
      
      return null;
    }

    /** Convert a context to a MessageAddress supporting the YP application **/
    MessageAddress lookup(String context) {
      // this should be a WP lookup like:
      // return wp.lookupName(context, "YP");
      return MessageAddress.getMessageAddress(context);
    }

    /** return the next higher-level context from the specified one or null **/
    String nextContext(String context) {
      // no nesting right now, but Community should provide this information,
      // or maybe it is built in.
      //return communityService.getParentCommunity(context);
      return null;
    }


    /** return true IFF the element represents an actual answer (or positive failure) **/
    boolean isResponseComplete(Element e) {
      return (e != null);
    }

    
    /** Send a single in-band YP request to the destination point **/
    Element sendOne(Element el, boolean iqp, MessageAddress ma) throws TransportException {
      Object key = wq.getKey();
      try {
        YPQueryMessage m = new YPQueryMessage(originMA, ma, el, iqp, key);
	if (logger.isDebugEnabled()) {
	  logger.debug("sendOne: sending YPQueryMessage - origin " + originMA +
		       " target " + ma +
		       " el " + el + 
		       " key "  + key);
	}
        mss.sendMessage(m);
        while (true) {
          try {
	    if (logger.isDebugEnabled()) {
	      logger.debug("Calling WaitQueue.waitFor() - key " + key);
	    }
            return (Element) wq.waitFor(key);
          } catch (InterruptedException ie) {
            // should probably log here...
	    if (logger.isDebugEnabled()) {
	      logger.debug("InterrupedException while in WaitQueue.waitFor()" +
			   " key - " + key);
	      ie.printStackTrace();
	    }
            Thread.interrupted();
          }
        }
      } catch (RuntimeException re) {
        throw new TransportException(re);
      }
    }
  }

}
