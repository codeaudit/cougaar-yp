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
 * An accessor container for one or more serviceInfo structures.
 *
 * <p>
 *
 * @author David Melgar (dmelgar@us.ibm.com)
 */
public class ServiceInfos extends UDDIElement {
   public static final String UDDI_TAG = "serviceInfos";

   protected Element base = null;

   // Vector of ServiceInfo objects
   Vector serviceInfo = new Vector();

   /**
    * Default constructor.
    *
    */
   public ServiceInfos() {
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

   public ServiceInfos(Element base) throws UDDIException {
      // Check if it is a fault. Throws an exception if it is.
      super(base);
      NodeList nl = null;
      nl = getChildElementsByTagName(base, ServiceInfo.UDDI_TAG);
      for (int i=0; i < nl.getLength(); i++) {
         serviceInfo.addElement(new ServiceInfo((Element)nl.item(i)));
      }
   }

   /**
    * Set serviceInfo vector
    *
    * @param s  Vector of <I>ServiceInfo</I> objects.
    */
   public void setServiceInfoVector(Vector s) {
      serviceInfo = s;
   }

   /**
    * Get serviceInfo
    *
    * @return s Vector of <I>ServiceInfo</I> objects.
    */
   public Vector getServiceInfoVector() {
      return serviceInfo;
   }

   /**
    * Add a ServiceInfo object to the collection
    * @param s ServiceInfo to be added
    */
   public void add (ServiceInfo s) {
      serviceInfo.add (s);
   }

   /**
    * Remove a ServiceInfo object from the collection
    * @param s ServiceInfo to be removed
    * @return True if object was removed, false if it
    *         was not found in the collection.
    */
   public boolean remove (ServiceInfo s) {
      return serviceInfo.remove (s);
   }

   /**
    * Retrieve the ServiceInfo at the specified index within the collection.
    * @param index Index to retrieve from.
    * @return ServiceInfo at that index
    */
   public ServiceInfo get (int index) {
      return (ServiceInfo) serviceInfo.get (index);
   }

   /**
    * Return current size of the collection.
    * @return Number of ServiceInfos in the collection
    */
   public int size () {
      return serviceInfo.size ();
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
      if (serviceInfo!=null) {
         for (int i=0; i < serviceInfo.size(); i++) {
            ((ServiceInfo)(serviceInfo.elementAt(i))).saveToXML(base);
		 }
      }
      parent.appendChild(base);
   }
}
