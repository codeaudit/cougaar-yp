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
 * The container/accessor for findQualifiers
 *
 * <p>
 *
 * @author David Melgar (dmelgar@us.ibm.com)
 * @author Vivek Chopra (vivek@soaprpc.com)
 */
public class FindQualifiers extends UDDIElement {
   public static final String UDDI_TAG = "findQualifiers";

   protected Element base = null;

   // Vector of FindQualifier objects
   Vector findQualifier = new Vector();

   /**
    * Default constructor.
    * Avoid using the default constructor for validation. It does not validate
    * required fields. Instead, use the required fields constructor to perform
    * validation.
    */
   public FindQualifiers() {
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
   public FindQualifiers(Element base) throws UDDIException {
      // Check if it is a fault. Throws an exception if it is.
      super(base);
      NodeList nl = null;
      nl = getChildElementsByTagName(base, FindQualifier.UDDI_TAG);
      for (int i=0; i < nl.getLength(); i++) {
         findQualifier.addElement(new FindQualifier((Element)nl.item(i)));
      }
   }

   /**
    * Set findQualifier vector.
    *
    * @param s  Vector of <I>FindQualifier</I> objects.
    */
   public void setFindQualifierVector(Vector s) {
      findQualifier = s;
   }

   /**
    * Add a FindQualifier object to the collection
    * @param f Find qualifier to be added
    */
   public void add (FindQualifier f) {
      findQualifier.add (f);
   }

   /**
    * Remove a FindQualifier object from the collection
    * @param f Find qualifier to be removed
    * @return True if object was removed, false if it
    *         was not found in the collection.
    */
   public boolean remove (FindQualifier f) {
      return findQualifier.remove (f);
   }

   /**
    * Retrieve the findQualfier at the specified index within the collection.
    * @param index Index to retrieve from.
    * @return FindQualifier at that index
    */
   public FindQualifier get (int index) {
      return (FindQualifier) findQualifier.get (index);
   }

   /**
    * Return current size of the collection.
    * @return Number of find qualifiers in the collection
    */
   public int size () {
      return findQualifier.size ();
   }

   /**
    * Get findQualifier
    *
    * @return s Vector of <I>FindQualifier</I> objects.
    */
   public Vector getFindQualifierVector() {
      return findQualifier;
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
      if (findQualifier!=null) {
         for (int i=0; i < findQualifier.size(); i++) {
            ((FindQualifier)(findQualifier.elementAt(i))).saveToXML(base);
     }
      }
      parent.appendChild(base);
   }
}
