/*
 * The source code contained herein is licensed under the IBM Public License
 * Version 1.0, which has been approved by the Open Source Initiative.
 * Copyright (C) 2001, Hewlett-Packard Company
 * All Rights Reserved.
 *
 */

package org.uddi4j.response;

import java.util.Vector;
import org.w3c.dom.*;
import org.uddi4j.*;
import org.uddi4j.datatype.*;
import org.uddi4j.datatype.binding.*;
import org.uddi4j.datatype.business.*;
import org.uddi4j.datatype.service.*;
import org.uddi4j.datatype.tmodel.*;
import org.uddi4j.request.*;
import org.uddi4j.util.*;

/**
 * <p><b>General information:</b><p>
 *
 * This class represents an element within the UDDI version 2.0 schema.
 * This class contains the following types of methods:<ul>
 *
 *   <li>A constructor that passes the required fields.
 *   <li>A Constructor that will instantiate the object from an appropriate XML
 *       DOM element.
 *   <li>Get/set methods for each attribute that this element can contain.
 *   <li>A get/setVector method is provided for sets of attributes.
 *   <li>A SaveToXML method that serializes this class within a passed in
 *       element.
 * </ul>
 * Typically, this class is used to construct parameters for, or interpret
 * responses from, methods in the UDDIProxy class.
 *
 * <p><b>Element description:</b><p>
 *
 * This class returns all complete and incomplete assertions. It also provides
 * some administrative functions, such as, determining if there are outstanding
 * or incomplete assertions about relationships between the publisher account
 * and associated businesses.
 * <p>
 *
 * @author Mahesh C S (csmahesh@india.hp.com)
 */
public class AssertionStatusReport extends UDDIElement {
   public static final String UDDI_TAG = "assertionStatusReport";

   protected Element base = null;

   String operator = null;

   // Vector of AssertionStatusItem objects
   Vector assertionStatusItem = new Vector();


   /**
    * Default constructor.
    * Avoid using the default constructor for validation. It does not validate
    * required fields. Instead, use the required fields constructor to perform
    * validation.
    */

   public AssertionStatusReport() {
   }


   /**
    * Construct the object from a DOM tree. Used by
    * UDDIProxy to construct an object from a received UDDI
    * message.
    *
    * @param base   Element with the name appropriate for this class.
    *
    * @exception UDDIException Thrown if DOM tree contains a SOAP fault
    *  or a disposition report indicating a UDDI error.
    */

   public AssertionStatusReport(Element base) throws UDDIException {
       // Check if it is a fault. Throws an exception if it is.
       super(base);
       operator = base.getAttribute("operator");
       NodeList nl = null;
       nl = getChildElementsByTagName(base, AssertionStatusItem.UDDI_TAG);
       for (int i=0; i < nl.getLength(); i++) {
           assertionStatusItem.addElement(new AssertionStatusItem((Element)nl.item(i)));
       }
   }

   public String getOperator() {
       return operator;
   }

   public void setOperator(String s) {
       operator = s;
   }

   /**
    * Get assertionStatusItem vector
    *
    * @return Vector of <I>AssertionStatusItem</I> objects.
    */
   public Vector getAssertionStatusItemVector() {
       return assertionStatusItem;
   }

   /**
    * Set assertionStatusItem vector
    *
    * @param v  Vector of <I>AssertionStatusItem</I> objects.
    */
   public void setAssertionStatusItemVector(Vector v) {
       assertionStatusItem = v;
   }


   /**
    * Save an object to the DOM tree. Used to serialize an object
    * to a DOM tree, usually to send a UDDI message.
    *
    * <BR>Used by UDDIProxy.
    *
    * @param parent Object will serialize as a child element under the
    *  passed in parent element.
    */

   public void saveToXML(Element parent) {
       base = parent.getOwnerDocument().createElement(UDDI_TAG);
       // Save attributes
       base.setAttribute("generic", UDDIElement.GENERIC);
       base.setAttribute("xmlns", UDDIElement.XMLNS);
       if (operator!=null) {
          base.setAttribute("operator", operator);
       }
       if (assertionStatusItem!=null) {
          for (int i=0; i < assertionStatusItem.size(); i++) {
             ((AssertionStatusItem)(assertionStatusItem.elementAt(i))).saveToXML(base);
          }
	   }
       parent.appendChild(base);
   }
}
