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
 * Support element - accessor container for tModelInfo.
 *
 * <p>
 *
 * @author David Melgar (dmelgar@us.ibm.com)
 * @author Vivek Chopra (vivek@soaprpc.com)
 */
public class TModelInfos extends UDDIElement {
   public static final String UDDI_TAG = "tModelInfos";

   protected Element base = null;

   // Vector of TModelInfo objects
   Vector tModelInfo = new Vector();

   /**
    * Default constructor.
    *
    */
   public TModelInfos() {
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

   public TModelInfos(Element base) throws UDDIException {
      // Check if it is a fault. Throws an exception if it is.
      super(base);
      NodeList nl = null;
      nl = getChildElementsByTagName(base, TModelInfo.UDDI_TAG);
      for (int i=0; i < nl.getLength(); i++) {
         tModelInfo.addElement(new TModelInfo((Element)nl.item(i)));
      }
   }

   /**
    * Set tModelInfo vector
    *
    * @param s  Vector of <I>TModelInfo</I> objects.
    */
   public void setTModelInfoVector(Vector s) {
      tModelInfo = s;
   }

   /**
    * Get tModelInfo
    *
    * @return s Vector of <I>TModelInfo</I> objects.
    */
   public Vector getTModelInfoVector() {
      return tModelInfo;
   }

   /**
    * Add a TModelInfo object to the collection
    * @param t TModelInfo to be added
    */
   public void add (TModelInfo t) {
      tModelInfo.add (t);
   }

   /**
    * Remove a TModelInfo object from the collection
    * @param t TModelInfo to be removed
    * @return True if object was removed, false if it
    *         was not found in the collection.
    */
   public boolean remove (TModelInfo t) {
      return tModelInfo.remove (t);
   }

   /**
    * Retrieve the TModelInfo at the specified index within the collection.
    * @param index Index to retrieve from.
    * @return TModelInfo at that index
    */
   public TModelInfo get (int index) {
      return (TModelInfo) tModelInfo.get (index);
   }

   /**
    * Return current size of the collection.
    * @return Number of TModelInfos in the collection
    */
   public int size () {
      return tModelInfo.size ();
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
      if (tModelInfo!=null) {
         for (int i=0; i < tModelInfo.size(); i++) {
            ((TModelInfo)(tModelInfo.elementAt(i))).saveToXML(base);
		 }
      }
      parent.appendChild(base);
   }
}
