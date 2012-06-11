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
import java.net.URL;
import org.w3c.dom.Element;

/**
 * Interface for a SOAP transport to be used by UDDI4J.
 *
 * Transport implementations should use the following
 * system properties:
 * <UL><LI>
 * http.proxyHost  = Hostname of http proxy<LI>
 * https.proxyHost = Hostname of https proxy<LI>
 * http.proxyPort  = Portname of http proxy<LI>
 * https.proxyPort = Portname of https proxy<LI>
 *
 * org.uddi4j.logEnabled  If set, indicates that transport
 *   should log message sent and received.
 * </UL>
 * These values may also be specified in a property
 * file TBD.
 *
 * @author David Melgar (dmelgar@us.ibm.com)
 */
public interface Transport {

   /**
    * Sends a UDDIElement to URL.
    *
    * @param el     UDDIElement to send
    * @param url    Destination URL
    * @return An element representing a XML DOM tree containing the UDDI response.
    * @exception TransportException
    *                   Thrown if a problem occurs during transmission
    */
   public Element send(UDDIElement el, URL url) throws TransportException;

   /**
    * Sends a DOM Element to URL.
    *
    * @param el     UDDIElement to send
    * @param url    Destination URL
    * @return An element representing a XML DOM tree containing the UDDI response.
    * @exception TransportException
    *                   Thrown if a problem occurs during transmission
    */
   public Element send(Element el, URL url) throws TransportException ;
}
