/*
 * The source code contained herein is licensed under the IBM Public License
 * Version 1.0, which has been approved by the Open Source Initiative.
 * Copyright (C) 2001, International Business Machines Corporation
 * All Rights Reserved.
 *
 */

package org.uddi4j.transport;

import org.uddi4j.UDDIElement;
import org.uddi4j.UDDIException;
import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import org.w3c.dom.Element;

import java.net.URL;


/**
 * Partial implementation of Transport interface.
 * This class provides a default implementation of the
 * send UDDIElement method. This converts the UDDIElement
 * to a DOM element and invokes the send DOM Element
 * method.
 * In the future, if the data representation is not
 * DOM based, transports will need to implement their
 * own send UDDIElement methods.
 *
 * @author David Melgar (dmelgar@us.ibm.com)
 */
abstract public class TransportBase implements Transport {

   /**
    * Sends a UDDIElement to URL.
    *
    * @param el     UDDIElement to send
    * @param url    Destination URL
    * @return An element representing a XML DOM tree containing the UDDI response.
    * @exception TransportException
    *                   Thrown if a problem occurs during transmission
    */
   public Element send(UDDIElement el, URL url) throws TransportException {
      Element base = null;
      try {
         DocumentBuilder docBuilder = DocumentBuilderFactory.newInstance().newDocumentBuilder();
         base = docBuilder.newDocument().createElement("tmp");
      } catch (Exception e) {
         e.printStackTrace();
      }

      el.saveToXML(base);
      return send((Element)base.getFirstChild(), url);
   }

   public boolean logEnabled() {
      boolean logEnabled = false;
      String value = System.getProperty("org.uddi4j.logEnabled");
      if (value!=null && value.equalsIgnoreCase("true")) {
         logEnabled = true;
      }
      return logEnabled;
   }
}
