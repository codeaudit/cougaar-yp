/*
 * The source code contained herein is licensed under the IBM Public License
 * Version 1.0, which has been approved by the Open Source Initiative.
 * Copyright (C) 2001, Hewlett-Packard Company
 * All Rights Reserved.
 *
 */

package org.uddi4j.request;

import java.util.Vector;
import org.w3c.dom.*;
import org.uddi4j.*;
import org.uddi4j.datatype.*;
import org.uddi4j.datatype.binding.*;
import org.uddi4j.datatype.business.*;
import org.uddi4j.datatype.service.*;
import org.uddi4j.datatype.tmodel.*;
import org.uddi4j.datatype.assertion.*;
import org.uddi4j.response.*;
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
 * This message returns zero or more relatedBusinessInfo structures. For the
 * businessEntity specified in the find_relatedBusinesses class, the response
 * reports that business relationships with other businessEntity registrations
 * are complete. Business relationships are complete between two businessEntity
 * registrations when the publishers controlling each of the businessEntity
 * structures involved in the relationship set assertions affirming that
 * relationship.
 *
 * <p>
 *
 * @author Ravi Trivedi (ravi_trivedi@hp.com)
 */
public class FindRelatedBusinesses extends UDDIElement {

   public static final String UDDI_TAG = "find_relatedBusinesses";

   protected Element base = null;

   String maxRows = null;
   FindQualifiers findQualifiers = null;
   KeyedReference keyRef    = null;
   String businessKey = null;

   /**
    * Default constructor.
    * Avoid using the default constructor for validation. It does not validate
    * required fields. Instead, use the required fields constructor to perform
    * validation.
    */
   public FindRelatedBusinesses() {
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
   public FindRelatedBusinesses(Element base) throws UDDIException {
     // Check if it is a fault. Throws an exception if it is.
     super(base);
     maxRows = base.getAttribute("maxRows");

     NodeList nl = null;
      nl = getChildElementsByTagName(base, BusinessKey.UDDI_TAG);
      if (nl.getLength() > 0) {
         businessKey = new BusinessKey((Element)nl.item(0)).getText();
      }

     nl = getChildElementsByTagName(base, FindQualifiers.UDDI_TAG);
     if (nl.getLength() > 0) {
        findQualifiers = new FindQualifiers((Element)nl.item(0));
     }

     nl = getChildElementsByTagName(base, KeyedReference.UDDI_TAG);
     if (nl.getLength() > 0) {
         keyRef = new KeyedReference((Element)nl.item(0));
     }

   }

   /**
    * Construct the object with required fields.
    *
    * @param businessKey    String
    */
   public FindRelatedBusinesses(String businessKey) {
       this.businessKey = businessKey;
   }

   public void setBusinessKey(String newBusinessKey) {
       this.businessKey = newBusinessKey;
   }

   public String getBusinessKey() {
       return this.businessKey;
   }

   public void setFindQualifiers(FindQualifiers fqs) {
       this.findQualifiers = fqs;
   }

   public FindQualifiers getFindQualifiers() {
       return this.findQualifiers;
   }

   public void setKeyedReference (KeyedReference newKeyedReference) {
       this.keyRef = newKeyedReference;
   }

   public KeyedReference getKeyedReference() {
       return this.keyRef;
   }

   public String getMaxRows() {
       return this.maxRows;
   }

   public void setMaxRows(String rows) {
       this.maxRows = rows;
   }

   public int getMaxRowsInt() {
      return Integer.parseInt(this.maxRows);
   }

   public void setMaxRows(int s) {
      maxRows = Integer.toString(s);
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
         base.setAttribute("generic", UDDIElement.GENERIC);
         base.setAttribute("xmlns", UDDIElement.XMLNS);

         if (maxRows!=null) {
            base.setAttribute("maxRows", maxRows);
         }
         if (businessKey!=null) {
             (new BusinessKey(businessKey)).saveToXML(base);
         }
         if (findQualifiers!=null) {
             findQualifiers.saveToXML(base);
         }
         if(keyRef != null ) {
            keyRef.saveToXML(base);
         }

        parent.appendChild(base);
   }
}
