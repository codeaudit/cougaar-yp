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
 * This element designates the businessKeys (FromKey , ToKey) that the publisher
 * manages.
 *
 * <p>
 *
 * @author Mahesh C S (csmahesh@india.hp.com)
 */
public class KeysOwned extends UDDIElement {
   public static final String UDDI_TAG = "keysOwned";

   protected Element base = null;

   FromKey fromKey = null;
   ToKey toKey = null;

   /**
    * Default constructor.
    * Avoid using the default constructor for validation. It does not validate
    * required fields. Instead, use the required fields constructor to perform
    * validation.
    */

   public KeysOwned() {
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

   public KeysOwned(Element base) throws UDDIException {
       // Check if it is a fault. Throws an exception if it is.
       super(base);
       NodeList nl = null;
       nl = getChildElementsByTagName(base, FromKey.UDDI_TAG);
       if (nl.getLength() > 0) {
          fromKey = new FromKey((Element)nl.item(0));
       }
       nl = getChildElementsByTagName(base, ToKey.UDDI_TAG);
       if (nl.getLength() > 0) {
          toKey = new ToKey((Element)nl.item(0));
       }
   }

   public void setFromKeyString(String s) {
       fromKey = new FromKey();
       fromKey.setText(s);
   }

   public String getFromKeyString() {
       return fromKey.getText();
   }

   public void setFromKey(FromKey key) {
       fromKey = key;
   }

   public FromKey getFromKey() {
       return fromKey;
   }

   public void setToKey(ToKey key) {
       toKey = key;
   }

   public ToKey getToKey() {
       return toKey;
   }

   public void setToKeyString(String s) {
       toKey = new ToKey();
       toKey.setText(s);
   }

   public String getToKeyString() {
       return toKey.getText();
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

       if (fromKey!=null) {
          fromKey.saveToXML(base);
       }
       if (toKey!=null) {
          toKey.saveToXML(base);
       }
       parent.appendChild(base);
   }
}
