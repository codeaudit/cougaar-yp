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
 * Service Element: accessor for one or more discoveryURL elements
 *
 * <p>
 *
 * @author David Melgar (dmelgar@us.ibm.com)
 * @author Vivek Chopra (vivek@soaprpc.com)
 */
public class DiscoveryURLs extends UDDIElement {
   public static final String UDDI_TAG = "discoveryURLs";

   protected Element base = null;

   // Vector of DiscoveryURL objects
   Vector discoveryURL = new Vector();

   /**
    * Default constructor.
    * Avoid using the default constructor for validation. It does not validate
    * required fields. Instead, use the required fields constructor to perform
    * validation.
    */
   public DiscoveryURLs() {
   }

   /**
    * Construct the object with required fields.
    *
    * @param discoveryURL Vector of DiscoveryURL objects.
    */
   public DiscoveryURLs(Vector discoveryURL) {
      Vector objects;
      this.discoveryURL = discoveryURL;
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
   public DiscoveryURLs(Element base) throws UDDIException {
      // Check if it is a fault. Throws an exception if it is.
      super(base);
      NodeList nl = null;
      nl = getChildElementsByTagName(base, DiscoveryURL.UDDI_TAG);
      for (int i=0; i < nl.getLength(); i++) {
         discoveryURL.addElement(new DiscoveryURL((Element)nl.item(i)));
      }
   }

   /**
    * Set discoveryURL vector.
    *
    * @param s  Vector of <I>DiscoveryURL</I> objects.
    */
   public void setDiscoveryURLVector(Vector s) {
      discoveryURL = s;
   }

   /**
    * Get discoveryURL.
    *
    * @return s Vector of <I>DiscoveryURL</I> objects.
    */
   public Vector getDiscoveryURLVector() {
      return discoveryURL;
   }

  /**
    * Add a DiscoveryURL object to the collection
    * @param d Discovery URL to be added
    */
   public void add (DiscoveryURL d) {
      discoveryURL.add (d);
   }

   /**
    * Remove a DiscoveryURL object from the collection
    * @param d Discovery URL to be removed
    * @return True if object was removed, false if it
    *         was not found in the collection.
    */
   public boolean remove (DiscoveryURL d) {
      return discoveryURL.remove (d);
   }

   /**
    * Retrieve the DiscoveryURL at the specified index within the collection.
    * @param index Index to retrieve from.
    * @return DiscoveryURL at that index
    */
   public DiscoveryURL get (int index) {
      return (DiscoveryURL) discoveryURL.get (index);
   }

   /**
    * Return current size of the collection.
    * @return Number of Discovery URLs in the collection
    */
   public int size () {
      return discoveryURL.size ();
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
      if (discoveryURL!=null) {
         for (int i=0; i < discoveryURL.size(); i++) {
            ((DiscoveryURL)(discoveryURL.elementAt(i))).saveToXML(base);
     }
      }
      parent.appendChild(base);
   }
}
