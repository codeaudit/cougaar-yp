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

import java.net.URL;
import java.util.List;

import org.cougaar.core.agent.service.MessageSwitchService;
import org.cougaar.core.component.ComponentLoadFailure;
import org.cougaar.core.component.ComponentSupport;
import org.cougaar.core.component.ServiceBroker;
import org.cougaar.core.mts.Message;
import org.cougaar.core.mts.MessageAddress;
import org.cougaar.core.mts.MessageHandler;
import org.w3c.dom.Element;

/**
 * This is a trivial YP gateway which translates 
 * in-band Cougaar YP queries into SOAP comms using the
 * apache backend from uddi4j
 * The component requires a parameter which specifies the URL to which 
 * queries should be sent.  
 * @note This gateway only supports queries, not updates.
 **/

public class YPSoapGateway extends ComponentSupport {
  
  private MessageSwitchService mss = null;
  private WaitQueue wq = new WaitQueue();
  private MessageAddress originMA;
//  private ApacheSOAPTransport transport = null;
  private URL iURL = null;
  private URL pURL = null;


  public void setParameter(Object o) {
    if (o instanceof List) {
      List l = (List)o;
      if (l.size() > 1) {
        Object o1 = l.get(0);
        if (o1 instanceof URL) {
          iURL = (URL) o1;
        } else if (o1 instanceof String) {
          try {
            iURL = new URL((String) o1);
          } catch (Exception e) {
            throw new ComponentLoadFailure("Bad inquiry URL parameter specified "+o1, this, e);
          }
        }

        Object o2 = l.get(1);
        if (o2 instanceof URL) {
          pURL = (URL) o2;
        } else if (o2 instanceof String) {
          try {
            pURL = new URL((String) o2);
          } catch (Exception e) {
            throw new ComponentLoadFailure("Bad post URL parameter specified "+o2, this, e);
          }
        }
      }
    }
    if (iURL == null || pURL == null) {
      throw new ComponentLoadFailure("Not enough URL parameters specified", this);
    }
  }

  public void initialize() {
    super.initialize();

    // this should probably go into load

    // need to hook into the Agent MessageHandler protocol
    MessageHandler mh = new MessageHandler() {
        public boolean handleMessage(Message message) {
          if (message instanceof YPQueryMessage) {
            dispatchQuery((YPQueryMessage) message);
            return true;
          }
          return false;
        }
      };
    ServiceBroker sb = getServiceBroker();
    mss = (MessageSwitchService) sb.getService(this,MessageSwitchService.class, null);
    mss.addMessageHandler(mh);
    originMA = mss.getMessageAddress();

//    transport = new ApacheSOAPTransport();
  }

  /** forward to apache soap **/
  private void dispatchQuery(YPQueryMessage r) {
    Object key = r.getKey();
    Element qel = r.getElement();
    Element rel = null;
    boolean isInquiry = r.isInquiry();
//    try {
//      rel = transport.send(qel, isInquiry?iURL:pURL);
//    } catch (TransportException te) { 
//      // probably should bundle up the exception in a response element
//    }
    YPResponseMessage m = new YPResponseMessage(originMA, r.getOriginator(), rel, key);
    sendMessage(m);
  }

  protected void sendMessage(Message m) {
    mss.sendMessage(m);
  }
}
