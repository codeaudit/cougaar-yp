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

/** An Agent-level Component which implements the client-side of the Cougaar
 * YellowPages Application.
 * @note this version supports only Component-model Service style access,
 * and only synchronous queries.  Future versions will supply asynchronous
 * modes and blackboard-style interaction as well.
 **/

public class YPClientComponent extends ComponentSupport {
  
  private MessageSwitchService mss = null;
  private YPTransport transport;
  private WaitQueue wq = new WaitQueue();
  private MessageAddress originMA;
  private YPServiceProvider ypsp;


  protected void sendMessage(Message m) {
    mss.sendMessage(m);
  }

  public void initialize() {
    super.initialize();

    transport = new YPTransport();

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
    wq.trigger(key, el);
  }

  private Element soapSend(Element el, URL url) throws TransportException {
    Object key = wq.getKey();
    try {
      YPQueryMessage m = new YPQueryMessage(originMA, convertURLtoMA(url), el, key);
      mss.sendMessage(m);
      while (true) {
        try {
          return (Element) wq.waitFor(key);
        } catch (InterruptedException ie) {
          // should probably log here...
          Thread.interrupted();
        }
      }
    } catch (RuntimeException re) {
      throw new TransportException(re);
    }
  }
  
  private MessageAddress convertURLtoMA(URL url) {
    return MessageAddress.getMessageAddress(URI.create(url.toString()));
  }

  class YPServiceProvider implements ServiceProvider {
    public YPServiceProvider() {}

    public Object getService(ServiceBroker sb, Object requestor, Class serviceClass) {
      if (YPService.class.isAssignableFrom(serviceClass)) {
        return new YPServiceImpl();
      } else {
        return null;
      }
    }

    public void releaseService(ServiceBroker sb, Object requestor, Class serviceClass, Object service) {
    }
  }

  class YPServiceImpl implements YPService {
    /** Get a UDDIProxy for YP Queryies **/
    public UDDIProxy getUDDIProxy() {
      return new UDDIProxy(transport); // BBN Extension to uddi4j
    }
  }

  class YPTransport extends TransportBase {
    /** Send the DOM element specified to the URL as interpreted by the MTS **/
    public Element send(Element el, java.net.URL url) throws TransportException {
      return soapSend(el, url);
    }
  }
}
