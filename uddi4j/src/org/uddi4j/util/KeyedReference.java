/*
 * The source code contained herein is licensed under the IBM Public License
 * Version 1.0, which has been approved by the Open Source Initiative.
 * Copyright (C) 2001, International Business Machines Corporation
 * All Rights Reserved.
 *
 */

package org.uddi4j.util;

import java.util.Vector;
import org.w3c.dom.*;
import org.uddi4j.*;
import org.uddi4j.datatype.*;
import org.uddi4j.datatype.binding.*;
import org.uddi4j.datatype.business.*;
import org.uddi4j.datatype.service.*;
import org.uddi4j.datatype.tmodel.*;
import org.uddi4j.request.*;
import org.uddi4j.response.*;

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
 * Service element.  Represents a namespace qualified name-value
 * pair.  Depending upon the context in which it is used, this structure can be
 * utilized within different convention frameworks.
 *
 * <p>
 *
 * @author David Melgar (dmelgar@us.ibm.com)
 */
public class KeyedReference extends UDDIElement {
   public static final String UDDI_TAG = "keyedReference";

   protected Element base = null;

   String tModelKey = null;
   String keyName = null;
   String keyValue = null;

   /**
    * Default constructor.
    * Avoid using the default constructor for validation. It does not validate
    * required fields. Instead, use the required fields constructor to perform
    * validation.
    */
   public KeyedReference() {
   }

   /**
    * Construct the object with required fields.
    *
    * @param keyName  String
    * @param keyValue String
    */
   public KeyedReference(String keyName,
      String keyValue) {
      this.keyName = keyName;
      this.keyValue = keyValue;
   }

   public KeyedReference(String keyName, String keyValue, String tModelKey) {
      this(keyName, keyValue);
      this.tModelKey = tModelKey;
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
   public KeyedReference(Element base) throws UDDIException {
      // Check if it is a fault. Throws an exception if it is.
      super(base);
      tModelKey = base.getAttribute("tModelKey");
      keyName = base.getAttribute("keyName");
      keyValue = base.getAttribute("keyValue");
   }

   public void setTModelKey(String s) {
      tModelKey = s;
   }

   public void setKeyName(String s) {
      keyName = s;
   }

   public void setKeyValue(String s) {
      keyValue = s;
   }

   public String getTModelKey() {
      return tModelKey;
   }


   public String getKeyName() {
      return keyName;
   }


   public String getKeyValue() {
      return keyValue;
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
      // Save attributes.
      if (tModelKey!=null) {
         base.setAttribute("tModelKey", tModelKey);
      }
      if (keyName!=null) {
         base.setAttribute("keyName", keyName);
      }
      if (keyValue!=null) {
         base.setAttribute("keyValue", keyValue);
      }
      parent.appendChild(base);
   }
}
