/*
 * The source code contained herein is licensed under the IBM Public License
 * Version 1.0, which has been approved by the Open Source Initiative.
 * Copyright (C) 2001, International Business Machines Corporation
 * All Rights Reserved.
 *
 */

package org.uddi4j.datatype.binding;

import java.util.Vector;
import org.w3c.dom.*;
import org.uddi4j.*;
import org.uddi4j.datatype.*;
import org.uddi4j.datatype.business.*;
import org.uddi4j.datatype.service.*;
import org.uddi4j.datatype.tmodel.*;
import org.uddi4j.request.*;
import org.uddi4j.response.*;
import org.uddi4j.util.*;

/**
 * <p><b>General information:</b><p>
 *
 * This class represents an element within the UDDI version 2.0 schema.
 * This class contains the following types of methods:<ul>
 *
 *   <li>Constructor passing required fields.
 *   <li>Constructor that will instantiate the object from an appropriate XML
 *       DOM element.
 *   <li>Get/set methods for each attribute that this element can contain.
 *   <li>A get/setVector method is provided for sets of attributes.
 *   <li>SaveToXML method. Serializes this class within a passed in element.
 *
 * </ul>
 * Typically this class is used to construct parameters for, or interpret
 * responses from, methods in the UDDIProxy class.
 *
 * <p><b>Element description:</b><p>
 * Data: present when a service is directly accessible at a particular
 * address (e.g., URL, etc).  Mutually exclusive with hostingRedirector.
 *
 * <p>
 *
 * @author David Melgar (dmelgar@us.ibm.com)
 */
public class AccessPoint extends UDDIElement {
   public static final String UDDI_TAG = "accessPoint";

   protected Element base = null;

   String text = null;
   String URLType = null;

   /**
    * Default constructor.
    * Avoid using the default constructor for validation. It does not validate
    * required fields. Instead, use the required fields constructor to perform
    * validation.
    */
   public AccessPoint() {
   }

   /**
    * Construct the object using the required fields.
    *
    * @param value  String value
    * @param URLType  String
    */
   public AccessPoint(String value,
      String URLType) {
      setText(value);
      this.URLType = URLType;
   }

   /**
    * Construct the object from a DOM tree. Used by
    * UDDIProxy to construct an object from a received UDDI
    * message.
    *
    * @param base   Element with name appropriate for this class.
    *
    * @exception UDDIException Thrown if DOM tree contains a SOAP fault or
    *  disposition report indicating a UDDI error.
    */
   public AccessPoint(Element base) throws UDDIException {
      // Checks for a fault. Throws exception if it is a fault.      super(base);
      text = getText(base);
      URLType = base.getAttribute("URLType");
   }

   public void setText(String s) {
      text = s;
   }

   public void setURLType(String s) {
      URLType = s;
   }

   public String getText() {
      return text;
   }

   public String getURLType() {
      return URLType;
   }


   /**
    * Save object to DOM tree. Used to serialize object
    * to a DOM tree, usually to send a UDDI message.
    *
    * <BR>Used by UDDIProxy.
    *
    * @param parent Object will serialize as a child element under the
    *               passed in parent element.
    */
   public void saveToXML(Element parent) {
      base = parent.getOwnerDocument().createElement(UDDI_TAG);
      // Save attributes
      if (text!=null) {
         base.appendChild(parent.getOwnerDocument().createTextNode(text));
      }
      if (URLType!=null) {
         base.setAttribute("URLType", URLType);
      }
      parent.appendChild(base);
   }
}
