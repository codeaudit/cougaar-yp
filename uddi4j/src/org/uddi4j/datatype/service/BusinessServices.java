/*
 * The source code contained herein is licensed under the IBM Public License
 * Version 1.0, which has been approved by the Open Source Initiative.
 * Copyright (C) 2001, International Business Machines Corporation
 * All Rights Reserved.
 *
 */

package org.uddi4j.datatype.service;

import java.util.Vector;
import org.w3c.dom.*;
import org.uddi4j.*;
import org.uddi4j.datatype.*;
import org.uddi4j.datatype.binding.*;
import org.uddi4j.datatype.business.*;
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
 * Service element.  Accessor collection point for businessService data.
 *
 * <p>
 *
 * @author David Melgar (dmelgar@us.ibm.com)
 * @author Vivek Chopra (vivek@soaprpc.com)
 */
public class BusinessServices extends UDDIElement {
   public static final String UDDI_TAG = "businessServices";

   protected Element base = null;

   // Vector of BusinessService objects
   Vector businessService = new Vector();

   /**
    * Default constructor.
    * Avoid using the default constructor for validation. It does not validate
    * required fields. Instead, use the required fields constructor to perform
    * validation.
    */
   public BusinessServices() {
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
   public BusinessServices(Element base) throws UDDIException {
      // Check if its a fault. Throws an exception if it is.
      super(base);
      NodeList nl = null;
      nl = getChildElementsByTagName(base, BusinessService.UDDI_TAG);
      for (int i=0; i < nl.getLength(); i++) {
         businessService.addElement(new BusinessService((Element)nl.item(i)));
      }
   }

   /**
    * Set businessService vector
    *
    * @param s  Vector of <I>BusinessService</I> objects.
    */
   public void setBusinessServiceVector(Vector s) {
      businessService = s;
   }

   /**
    * Get businessService
    *
    * @return s Vector of <I>BusinessService</I> objects.
    */
   public Vector getBusinessServiceVector() {
      return businessService;
   }

  /**
    * Add a BusinessService object to the collection
    * @param b BusinessService to be added
    */
   public void add (BusinessService b) {
      businessService.add (b);
   }

   /**
    * Remove a BusinessService object from the collection
    * @param b BusinessService to be removed
    * @return True if object was removed, false if it
    *         was not found in the collection.
    */
   public boolean remove (BusinessService b) {
      return businessService.remove (b);
   }

   /**
    * Retrieve the BusinessService at the specified index within the collection.
    * @param index Index to retrieve from.
    * @return BusinessService at that index
    */
   public BusinessService get (int index) {
      return (BusinessService) businessService.get (index);
   }

   /**
    * Return current size of the collection.
    * @return Number of BusinessServices in the collection
    */
   public int size () {
      return businessService.size ();
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
      if (businessService!=null) {
        for (int i=0; i < businessService.size(); i++) {
           ((BusinessService)(businessService.elementAt(i))).saveToXML(base);
        }
      }
      parent.appendChild(base);
   }
}
