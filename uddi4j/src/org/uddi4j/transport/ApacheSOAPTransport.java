/*
 * The source code contained herein is licensed under the IBM Public License
 * Version 1.0, which has been approved by the Open Source Initiative.
 * Copyright (C) 2001, International Business Machines Corporation
 * All Rights Reserved.
 *
 */

package org.uddi4j.transport;

import org.apache.soap.Body;
import org.apache.soap.Envelope;
import org.apache.soap.SOAPException;
import org.apache.soap.messaging.Message;
import org.apache.soap.transport.http.SOAPHTTPConnection;
import org.apache.soap.util.xml.DOMWriter;

import java.net.URL;
import java.util.Vector;
import java.net.URL;

import org.w3c.dom.Element;

/**
 * Transport implementation for Apache SOAP stack.
 *
 * @author David Melgar (dmelgar@us.ibm.com)
 */
public class ApacheSOAPTransport extends TransportBase {

   private boolean debug = false;
   private SOAPHTTPConnection connection;

   /**
    * Default constructor
   */
   public ApacheSOAPTransport() {
      // Initialize variables based on system properties
      connection = new SOAPHTTPConnection();
      connection.setProxyHost(System.getProperty("http.proxyHost"));
      connection.setProxyUserName(System.getProperty("http.proxyUserName"));
      connection.setProxyPassword(System.getProperty("http.proxyPassword"));

      String proxyPortString = System.getProperty("http.proxyPort");
      if (proxyPortString!=null) {
         try {
            connection.setProxyPort(new Integer(proxyPortString).intValue());
         } catch (Exception e) {
         }
      }
   }

   /**
    * Sends a Element to URL.
    *
    * @param el     Element to send
    * @param url    Destination URL
    * @return An element representing a XML DOM tree containing the UDDI response.
    * @exception TransportException
    *                   Thrown if a problem occurs during transmission
    */
   public Element send(Element el, URL url) throws TransportException {
      debug = logEnabled();

      Envelope sendEnv = new Envelope();
      Body sendBody = new Body();

      Vector bodyEntry = new Vector();
      bodyEntry.add(el);
      sendBody.setBodyEntries(bodyEntry);

      sendEnv.setBody(sendBody);

      Message soapMessage = new Message();

      soapMessage.setSOAPTransport(connection);

      Element base = null;

      try {
         if (debug) {
            System.err.println("\nRequest body:\n" + DOMWriter.nodeToString(el));
         }
         soapMessage.send(url, "", sendEnv);
         Envelope responseEnv = soapMessage.receiveEnvelope();

         Body responseBody = responseEnv.getBody();
         base = (Element)responseBody.getBodyEntries().firstElement();
         if (debug) {
            System.err.println("\nResponse body:\n" + DOMWriter.nodeToString(base));
         }
      } catch (SOAPException e) {
         throw new TransportException(e);
      }

      return base;
   }
}
