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
import org.apache.axis.AxisFault;
import org.apache.axis.client.Service;
import org.apache.axis.client.Call;
import org.apache.axis.Message;
import org.apache.axis.message.SOAPEnvelope;
import org.apache.axis.message.SOAPBodyElement;
import org.apache.axis.message.SOAPFaultElement;
import org.apache.axis.encoding.SerializationContext;
import org.apache.axis.transport.http.HTTPConstants;
import org.w3c.dom.Element;

import java.net.URL;
import java.util.Vector;

/**
 * Transport implementation for Apache AXIS SOAP stack.
 *
 * @author David Melgar (dmelgar@us.ibm.com)
 */
public class ApacheAxisTransport extends TransportBase {

   /**
    * Sends a UDDIElement to URL.
    *
    * @param el     UDDIElement to send
    * @param url    Destination URL
    * @return An element representing a XML DOM tree containing the UDDI response.
    * @exception TransportException
    *                   Thrown if a problem occurs during transmission
    */
   public Element send(Element el, URL url) throws TransportException{

      Element base = null;
      boolean debug = logEnabled();
      Call call = null;

      try {
         Service  service = new Service();
         call    = (Call) service.createCall();

         call.setTargetEndpointAddress( url );
         // call.setProperty(HTTPConstants.MC_HTTP_SOAPACTION, "");

         Vector result = null ;

         // Rebuild the body. This convoluted process lets Axis handle the level 1 DOM tree
         // from UDDI4J. When UDDI4J moves to a level 2 DOM tree, the more obvious method
         // can be tried.
         String str = null ;

         str = org.apache.axis.utils.XMLUtils.ElementToString(el);
         SOAPBodyElement body = new SOAPBodyElement(new java.io.ByteArrayInputStream(str.getBytes()));


         // if DOM level 2 use this more obvious approach
         // SOAPBodyElement body = new SOAPBodyElement(el);

         Object[] params = new Object[] { body };

         if (debug) {
            System.err.println("\nRequest message:\n" + params[0]);
         }

         result = (Vector) call.invoke( params );

         base = ((SOAPBodyElement)result.elementAt(0)).getAsDOM();
      } catch (AxisFault fault) {
         try {
            Message m = call.getResponseMessage();
            base = ((SOAPBodyElement)(m.getSOAPPart().getAsSOAPEnvelope().getBodyElements().elementAt(0))).getAsDOM();
         } catch (Exception e) {
            throw new TransportException(e);
         }
      } catch (Exception e) {
         throw new TransportException(e);
      }
      if (debug && base!=null) {
         System.err.println("\nResponse message:\n" + org.apache.axis.utils.XMLUtils.ElementToString(base));
      }

      return base;
   }
}
