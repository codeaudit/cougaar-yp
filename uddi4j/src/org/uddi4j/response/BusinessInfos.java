/*
 * The source code contained herein is licensed under the IBM Public License
 * Version 1.0, which has been approved by the Open Source Initiative.
 * Copyright (C) 2001, International Business Machines Corporation
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
 * An accessor container for one or more businessInfo structures.
 *
 * <p>
 *
 * @author David Melgar (dmelgar@us.ibm.com)
 * @author Vivek Chopra (vivek@soaprpc.com)
 */
public class BusinessInfos extends UDDIElement {
   public static final String UDDI_TAG = "businessInfos";

   protected Element base = null;

   // Vector of BusinessInfo objects
   Vector businessInfo = new Vector();

   /**
    * Default constructor.
    *
    */
   public BusinessInfos() {
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

   public BusinessInfos(Element base) throws UDDIException {
      // Check if it is a fault. Throws an exception if it is.
      super(base);
      NodeList nl = null;
      nl = getChildElementsByTagName(base, BusinessInfo.UDDI_TAG);
      for (int i=0; i < nl.getLength(); i++) {
         businessInfo.addElement(new BusinessInfo((Element)nl.item(i)));
      }
   }

   /**
    * Set businessInfo vector
    *
    * @param s  Vector of <I>BusinessInfo</I> objects.
    */
   public void setBusinessInfoVector(Vector s) {
      businessInfo = s;
   }

   /**
    * Get businessInfo
    *
    * @return s Vector of <I>BusinessInfo</I> objects.
    */
   public Vector getBusinessInfoVector() {
      return businessInfo;
   }

   /**
    * Add a BusinessInfo object to the collection
    * @param b BusinessInfo to be added
    */
   public void add (BusinessInfo b) {
      businessInfo.add (b);
   }

   /**
    * Remove a BusinessInfo object from the collection
    * @param b BusinessInfo to be removed
    * @return True if object was removed, false if it
    *         was not found in the collection.
    */
   public boolean remove (BusinessInfo b) {
      return businessInfo.remove (b);
   }

   /**
    * Retrieve the BusinessInfo at the specified index within the collection.
    * @param index Index to retrieve from.
    * @return BusinessInfo at that index
    */
   public BusinessInfo get (int index) {
      return (BusinessInfo) businessInfo.get (index);
   }

   /**
    * Return current size of the collection.
    * @return Number of BusinessInfos in the collection
    */
   public int size () {
      return businessInfo.size ();
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
      if (businessInfo!=null) {
         for (int i=0; i < businessInfo.size(); i++) {
            ((BusinessInfo)(businessInfo.elementAt(i))).saveToXML(base);
		 }
      }
      parent.appendChild(base);
   }
}
