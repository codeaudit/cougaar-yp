/*
 * The source code contained herein is licensed under the IBM Public License
 * Version 1.0, which has been approved by the Open Source Initiative.
 * Copyright (C) 2001, International Business Machines Corporation
 * Copyright (C) 2001, Hewlett-Packard Company
 * All Rights Reserved.
 *
 */

package org.uddi4j;

import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;
import java.util.Vector;

/**
 * Base class for an object representing UDDI XML
 * elements.<p>
 *
 * @author David Melgar (dmelgar@us.ibm.com)
 * @author Ravi Trivedi (ravi_trivedi@hp.com)
 */
public abstract class UDDIElement {

   public static String GENERIC = "2.0";
   public static String XMLNS = "urn:uddi-org:api_v2";
   public static String SOAPNS = "http://schemas.xmlsoap.org/soap/envelope/";

   public UDDIElement() {
   }

   public UDDIElement(Element el) throws UDDIException {
      // Check that if an exception should be thrown
      if (UDDIException.isValidElement(el)) {
         // Looks like a fault, process and throw
         throw new UDDIException(el, true);
      }
   }

   abstract public void saveToXML(Element base);

   /**
    * Performs a utility function.
    * Returns text contained in child elements of the
    * passed in element.
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
      // Trim result, to remove whitespace.
      return result.trim();
   }

   public NodeList getChildElementsByTagName(Element el, String tag) {
       // Do NOT traverse the tree. Only search within the immediate
       // child nodes.
       NodeList children = el.getChildNodes();
       Vector result = new Vector();
       for (int i = 0; i < children.getLength(); i++) {
           Node node = children.item(i);
           if (node.getNodeType() == node.ELEMENT_NODE &&
               node.getNodeName().equals(tag)) {
               // This is a valid node
               result.addElement(node);
           }
       }
       return new VectorNodeList(result);
   }

   protected Element base = null;

}
