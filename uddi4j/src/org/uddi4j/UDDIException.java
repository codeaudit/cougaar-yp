/*
 * The source code contained herein is licensed under the IBM Public License
 * Version 1.0, which has been approved by the Open Source Initiative.
 * Copyright (C) 2001, International Business Machines Corporation
 * All Rights Reserved.
 *
 */

package org.uddi4j;

import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;
import org.uddi4j.response.DispositionReport;
import java.util.StringTokenizer;

/**
 * Represents a UDDI defined error. This exception is thrown in cases
 * where the return value on the UDDI API cannot indicate an error condition.<P>
 *
 * UDDIException usually contains a disposition report that has detailed
 * information about the UDDI error, as defined by the UDDI specification.
 * If the response is a SOAP fault, but does not contain a disposition
 * report, this exception may still be thrown.<P>
 *
 * The DispositionReport class contains definitions for the various error values
 * that can be returned.<P>
 *
 * @author David Melgar (dmelgar@us.ibm.com)
 */
public class UDDIException extends Exception {

   static final String UDDI_TAG = "Fault";

   String faultCode     = null;
   String faultString   = null;
   String faultActor    = null;
   String detail        = null;

   Element detailElement= null;
   DispositionReport dispositionReport = null;

   public UDDIException() {
   }

   /**
    * Constructer that parses the XML dom tree and extracts
    * useful attributes.
    *
    * @param el     Root element of the tree within the SOAP body.
    */
   public UDDIException(Element el, boolean createDispositionReport) {
      if (isValidElement(el)) {
         // Extract useful attributes
         NodeList nl;
         Element tmp;
         nl = el.getElementsByTagName("faultcode");
         if (nl.getLength()==0) {       // Handle possible DOM level 2 response
            nl = el.getElementsByTagNameNS(UDDIElement.SOAPNS, "faultcode");
         }
         if (nl!=null && nl.getLength()>0) {
            tmp = (Element)nl.item(0);
            faultCode = getText(tmp);
         }
         nl = el.getElementsByTagName("faultstring");
         if (nl.getLength()==0) {       // Handle possible DOM level 2 response
            nl = el.getElementsByTagNameNS(UDDIElement.SOAPNS, "faultstring");
         }
         if (nl!=null && nl.getLength()>0) {
            tmp = (Element)nl.item(0);
            faultString = getText(tmp);
         }
         nl = el.getElementsByTagName("faultactor");
         if (nl.getLength()==0) {       // Handle possible DOM level 2 response
            nl = el.getElementsByTagNameNS(UDDIElement.SOAPNS, "faultactor");
         }
         if (nl!=null && nl.getLength()>0) {
            tmp = (Element)nl.item(0);
            faultActor = getText(tmp);
         }
         nl = el.getElementsByTagName("detail");
         if (nl.getLength()==0) {       // Handle possible DOM level 2 response
            nl = el.getElementsByTagNameNS(UDDIElement.SOAPNS, "detail");
         }
         // Try to create a disposition report
         if (nl!=null && nl.getLength()>0) {
            tmp = (Element)nl.item(0);
            detailElement = tmp;
            if (createDispositionReport) {
               try {
                  nl = el.getElementsByTagName(DispositionReport.UDDI_TAG);
                  if (nl!=null && nl.getLength()>0) {
                     tmp = (Element)nl.item(0);
                     dispositionReport = new DispositionReport(tmp);
                  }
               } catch (UDDIException e) {
                  // Ignore exception, we're handling it already
               }
            }
         }
      }
   }

   /**
    * Tests the passed in element to determine if the
    * element is a serialized version of this object.
    *
    * @param el     Root element for this object
    */
   public static boolean isValidElement(Element el) {
      // This method can be in the base class even if this class sets the value
      // Handle soapFault if it returns a dom tree. Parse out values
      // Fault DOES show up as a "Fault" element within the body
        String name = el.getNodeName();
      // Take care of the Namespaces qualifier being present
      // (IDOOX server returns it).
        StringTokenizer strtok = new StringTokenizer(name ,":");
        String value = "";
        while (strtok.hasMoreTokens()) {
             value = strtok.nextToken();
        }
        return UDDI_TAG.equals(value);
   }

   // Getters
   public String getFaultCode() {
      return faultCode;
   }

   public String getFaultString() {
      return faultString;
   }

   public String getFaultActor() {
      return faultActor;
   }

   public String getDetail() {
      return detail;
   }

   public Element getDetailElement() {
      return detailElement;
   }

   public DispositionReport getDispositionReport() {
      return dispositionReport;
   }

   /**
    * Utility function.
    * Returns text contained in child elements of the
    * passed in element
    *
    * @param el     Element
    * @return java.lang.String
    */
   protected String getText(Node el) {
      NodeList nl = el.getChildNodes();
      String result = "";
      for (int i = 0; i < nl.getLength(); i++) {
         if (nl.item(i).getNodeType()==Element.TEXT_NODE) {
            result += nl.item(i).getNodeValue();
         }
      }
      // Trim result to remove whitespace
      return result.trim();
   }

   /**
   * Provide exception message
   * @return java.lang.String
   */
   public String toString() {
      if (dispositionReport != null) {
         return dispositionReport.getErrInfoText();
      }
      return getFaultString();
   }
}
