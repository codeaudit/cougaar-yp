/*
 * The source code contained herein is licensed under the IBM Public License
 * Version 1.0, which has been approved by the Open Source Initiative.
 * Copyright (C) 2001, International Business Machines Corporation
 * Copyright (C) 2001, Hewlett-Packard Company
 * All Rights Reserved.
 *
 */

package org.uddi4j.datatype.business;

import java.util.Vector;
import org.w3c.dom.*;
import org.uddi4j.*;
import org.uddi4j.datatype.*;
import org.uddi4j.datatype.binding.*;
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
 * Typically, this class is used to construct parameters for, or interpret
 * responses from methods in the UDDIProxy class.
 *
 * <p><b>Element description:</b><p>
 * Data: a printable, free form address.  Typed by convention.  Sort not used.
 *
 * <p>
 *
 * @author David Melgar (dmelgar@us.ibm.com)
 * @author Ravi Trivedi (ravi_trivedi@hp.com)
 */

public class Address extends UDDIElement {
   public static final String UDDI_TAG = "address";

   protected Element base = null;

   String useType = null;
   String sortCode = null;
   String tModelKey = null;
   // Vector of AddressLine objects
   Vector addressLine = new Vector();

   /**
    * Default constructor.
    * Avoid using the default constructor for validation. It does not validate
    * required fields. Instead, use the required fields constructor to perform
    * validation.
    */
   public Address() {
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

   public Address(Element base) throws UDDIException {
      // Check if its a fault. Throw exception if it is
      super(base);
      useType = base.getAttribute("useType");
      sortCode = base.getAttribute("sortCode");
      NodeList nl = null;
      nl = getChildElementsByTagName(base, AddressLine.UDDI_TAG);
      for (int i=0; i < nl.getLength(); i++) {
         addressLine.addElement(new AddressLine((Element)nl.item(i)));
      }
   }

   public void setUseType(String s) {
      useType = s;
   }

   public void setSortCode(String s) {
      sortCode = s;
   }

   /**
    * Set addressLine vector
    *
    * @param s  Vector of <I>AddressLine</I> objects.
    */
   public void setAddressLineVector(Vector s) {
      addressLine = s;
   }

   /**
    * Set addressLine
    *
    * @param s  Vector of <I>String</I> objects.
    */
   public void setAddressLineStrings(Vector s) {
      addressLine = new Vector();
      for (int i = 0; i < s.size(); i++) {
         addressLine.addElement( new AddressLine((String)s.elementAt(i)) );
      }
   }

   public void setTModelKey(String key) {
       tModelKey=key;
   }

   public String getUseType() {
      return useType;
   }


   public String getSortCode() {
      return sortCode;
   }


   /**
    * Get addressLine
    *
    * @return s Vector of <I>AddressLine</I> objects.
    */
   public Vector getAddressLineVector() {
      return addressLine;
   }

   /**
    * Get addressLine
    *
    * @return s Vector of <I>String</I> objects.
    */
   public Vector getAddressLineStrings() {
      Vector strings = new Vector();
      for (int i = 0; i < addressLine.size(); i++) {
         strings.addElement( ((AddressLine)addressLine.elementAt(i)).getText());
      }
      return strings;
   }

   public String getTModelKey() {
       return tModelKey;
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
      if (useType!=null) {
         base.setAttribute("useType", useType);
      }
      if (sortCode!=null) {
         base.setAttribute("sortCode", sortCode);
      }
      if (addressLine!=null) {
        for (int i=0; i < addressLine.size(); i++) {
           ((AddressLine)(addressLine.elementAt(i))).saveToXML(base);
        }
      }
      parent.appendChild(base);
   }
}
