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
 * Service element.  Used in searching and categorization.
 *
 * <p>
 *
 * @author David Melgar (dmelgar@us.ibm.com)
 */
public class CategoryBag extends UDDIElement {
   public static final String UDDI_TAG = "categoryBag";

   protected Element base = null;

   // Vector of KeyedReference objects
   Vector keyedReference = new Vector();

   /**
    * Default constructor.
    *
    */
   public CategoryBag() {
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
   public CategoryBag(Element base) throws UDDIException {
      // Check if it is a fault. Throws an exception if it is.
      super(base);
      NodeList nl = null;
      nl = getChildElementsByTagName(base, KeyedReference.UDDI_TAG);
      for (int i=0; i < nl.getLength(); i++) {
         keyedReference.addElement(new KeyedReference((Element)nl.item(i)));
      }
   }

   /**
    * Set keyedReference vector
    *
    * @param s  Vector of <I>KeyedReference</I> objects.
    */
   public void setKeyedReferenceVector(Vector s) {
      keyedReference = s;
   }

   /**
    * Get keyedReference
    *
    * @return s Vector of <I>KeyedReference</I> objects.
    */
   public Vector getKeyedReferenceVector() {
      return keyedReference;
   }

   /**
    * Add a keyed reference to this collection
    * 
    * @param kr     KeyedReference
    */
   public void add(KeyedReference kr) {
      keyedReference.add(kr);
   }

   /**
    * Remove the specified object from the collection.
    * 
    * @param kr     Keyed reference to remove
    * @return True if object was removed, false if it
    *         was not found in the collection.
    */
   public boolean remove(KeyedReference kr) {
      return keyedReference.remove(kr);
   }

   /**
    * Retrieve the object at the specified index within the collection.
    * 
    * @param index
    * @return 
    */
   public KeyedReference get(int index) {
      return (KeyedReference)keyedReference.get(index);
   }

   /**
    * Return current size of the collection.
    * 
    * @return 
    */
   public int size() {
      return keyedReference.size();
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
      if (keyedReference!=null) {
         for (int i=0; i < keyedReference.size(); i++) {
            ((KeyedReference)(keyedReference.elementAt(i))).saveToXML(base);
     }
      }
      parent.appendChild(base);
   }
}
