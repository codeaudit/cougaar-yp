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
 * This is a response message that contains a set of one or more RelatedBusinessInfo
 * structures.
 *
 * <p>
 *
 * @author Ravi Trivedi (ravi_trivedi@hp.com)
 * @author Vivek Chopra (vivek@soaprpc.com)
 */
public class RelatedBusinessInfos extends UDDIElement {

   public static final String UDDI_TAG = "relatedBusinessInfos";

   protected Element base = null;

   // Vector of RelatedBusinessInfo objects
   Vector relatedBusinessInfo = new Vector();

   /**
    * Default constructor.
    * Avoid using the default constructor for validation. It does not validate
    * required fields. Instead, use the required fields constructor to perform
    * validation.
    */

   public RelatedBusinessInfos() {
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

   public RelatedBusinessInfos(Element base) throws UDDIException {
        // Check if it is a fault. Throws an exception if it is.
         super(base);
         NodeList nl = null;
         nl = getChildElementsByTagName(base, RelatedBusinessInfo.UDDI_TAG);
         for (int i=0; i < nl.getLength(); i++) {
            relatedBusinessInfo.addElement(new RelatedBusinessInfo((Element)nl.item(i)));
         }
   }

   /**
    * Construct the object with required fields.
    *
    * @param relatedBusinessInfo    Vector
    */
   public RelatedBusinessInfos(Vector relatedBusinessInfo) {
       this.relatedBusinessInfo = relatedBusinessInfo;
   }

   public Vector getRelatedBusinessInfoVector() {
       return this.relatedBusinessInfo;
   }

   public void setRelatedBusinessInfoVector(Vector relatedBusinessInfo) {
       this.relatedBusinessInfo = relatedBusinessInfo;
   }

   /**
    * Add a RelatedBusinessInfo object to the collection
    * @param r RelatedBusinessInfo to be added
    */
   public void add (RelatedBusinessInfo r) {
      relatedBusinessInfo.add (r);
   }

   /**
    * Remove a RelatedBusinessInfo object from the collection
    * @param r RelatedBusinessInfo to be removed
    * @return True if object was removed, false if it
    *         was not found in the collection.
    */
   public boolean remove (RelatedBusinessInfo r) {
      return relatedBusinessInfo.remove (r);
   }

   /**
    * Retrieve the RelatedBusinessInfo at the specified index within the collection.
    * @param index Index to retrieve from.
    * @return RelatedBusinessInfo at that index
    */
   public RelatedBusinessInfo get (int index) {
      return (RelatedBusinessInfo) relatedBusinessInfo.get (index);
   }

   /**
    * Return current size of the collection.
    * @return Number of RelatedBusinessInfos in the collection
    */
   public int size () {
      return relatedBusinessInfo.size ();
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
         if (relatedBusinessInfo!=null) {
            for (int i=0; i < relatedBusinessInfo.size(); i++) {
               ((RelatedBusinessInfo)(relatedBusinessInfo.elementAt(i))).saveToXML(base);
		    }
         }
         parent.appendChild(base);

   }
}
