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

  public void load() {
    super.load();

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
    //    Element qel = r.getElement();
    Element rel = null;
    //    boolean isInquiry = r.isInquiry();
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
