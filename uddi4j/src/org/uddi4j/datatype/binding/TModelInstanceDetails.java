/*
 * The source code contained herein is licensed under the IBM Public License
 * Version 1.0, which has been approved by the Open Source Initiative.
 * Copyright (C) 2001, International Business Machines Corporation
 * All Rights Reserved.
 *
 */

package org.uddi4j.datatype.binding;

import java.util.Vector;
import org.w3c.dom.*;
import org.uddi4j.*;
import org.uddi4j.datatype.*;
import org.uddi4j.datatype.business.*;
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
 * Support element used as a container for tModel references within a
 * web service bindingTemplate metadata set.
 *
 * <p>
 *
 * @author David Melgar (dmelgar@us.ibm.com)
 * @author Vivek Chopra (vivek@soaprpc.com)
 */
public class TModelInstanceDetails extends UDDIElement {
   public static final String UDDI_TAG = "tModelInstanceDetails";

   protected Element base = null;

   // Vector of TModelInstanceInfo objects
   Vector tModelInstanceInfo = new Vector();

   /**
    * Default constructor.
    *
    */
   public TModelInstanceDetails() {
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
   public TModelInstanceDetails(Element base) throws UDDIException {
      // Check if it is a fault. Throw exception if it is.
      super(base);
      NodeList nl = null;
      nl = getChildElementsByTagName(base, TModelInstanceInfo.UDDI_TAG);
      for (int i=0; i < nl.getLength(); i++) {
         tModelInstanceInfo.addElement(new TModelInstanceInfo((Element)nl.item(i)));
      }
   }

   /**
    * Set tModelInstanceInfo vector.
    *
    * @param s  Vector of <I>TModelInstanceInfo</I> objects.
    */
   public void setTModelInstanceInfoVector(Vector s) {
      tModelInstanceInfo = s;
   }

   /**
    * Get tModelInstanceInfo.
    *
    * @return s Vector of <I>TModelInstanceInfo</I> objects.
    */
   public Vector getTModelInstanceInfoVector() {
      return tModelInstanceInfo;
   }

   /**
    * Add a TModelInstanceInfo object to the collection
    * @param t TModelInstanceInfo to be added
    */
   public void add (TModelInstanceInfo t) {
      tModelInstanceInfo.add (t);
   }

   /**
    * Remove a TModelInstanceInfo object from the collection
    * @param t TModelInstanceInfo to be removed
    * @return True if object was removed, false if it
    *         was not found in the collection.
    */
   public boolean remove (TModelInstanceInfo t) {
      return tModelInstanceInfo.remove (t);
   }

   /**
    * Retrieve the TModelInstanceInfo at the specified index within the collection.
    * @param index Index to retrieve from.
    * @return TModelInstanceInfo at that index
    */
   public TModelInstanceInfo get (int index) {
      return (TModelInstanceInfo) tModelInstanceInfo.get (index);
   }

   /**
    * Return current size of the collection.
    * @return Number of TModelInstanceInfos in the collection
    */
   public int size () {
      return tModelInstanceInfo.size ();
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
      if (tModelInstanceInfo!=null) {
        for (int i=0; i < tModelInstanceInfo.size(); i++) {
           ((TModelInstanceInfo)(tModelInstanceInfo.elementAt(i))).saveToXML(base);
        }
      }
      parent.appendChild(base);
   }
}
