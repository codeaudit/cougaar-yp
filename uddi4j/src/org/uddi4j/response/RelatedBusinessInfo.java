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
 * This structure contains information about one or more relationships between
 * two businessEntitys. The information can be a businessKey, name and optional
 * description data, and a collection element named sharedRelationships.
 *
 * The sharedRelationships element can contain zero or more keyedReference
 * elements. The information in the keyedReference and businessKey elements,
 * for a specific businessEntity, represent complete relationships when they
 * match publisher assertions made by the publisher for each businessEntity.
 *
 * <p>
 *
 * @author Ravi Trivedi (ravi_trivedi@hp.com)
 */
public class RelatedBusinessInfo extends UDDIElement {

   public static final String UDDI_TAG = "relatedBusinessInfo";
   protected Element base = null;
   BusinessKey businessKey = null;
   Name name = null;
   Description description = null;
   SharedRelationships sharedRelation = null;

   /**
    * Default constructor.
    * Avoid using the default constructor for validation. It does not validate
    * required fields. Instead, use the required fields constructor to perform
    * validation.
    */

   public RelatedBusinessInfo() {
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

   public RelatedBusinessInfo(Element base) throws UDDIException {
      // Check if it is a fault. Throws an exception if it is.
         super(base);
         NodeList nl = null;
         nl = getChildElementsByTagName(base, BusinessKey.UDDI_TAG);
         if (nl.getLength() > 0) {
            businessKey = new BusinessKey((Element)nl.item(0));
         }

         nl = getChildElementsByTagName(base, Name.UDDI_TAG);
         if (nl.getLength() > 0) {
            name = new Name((Element)nl.item(0));
         }

        nl = getChildElementsByTagName(base, Description.UDDI_TAG);
        if (nl.getLength() > 0) {
           description = new Description((Element)nl.item(0));
        }

        nl = getChildElementsByTagName(base, SharedRelationships.UDDI_TAG);
        if (nl.getLength() > 0) {
           sharedRelation = new SharedRelationships((Element)nl.item(0));
        }

   }


   public Name getName() {
       return this.name;
   }

   public void setName(Name name) {
       this.name = name;
   }

   public void setName(String name) {
    this.name = new Name( name );
   }

   public String getNameString() {
       return name.getText();
   }

   public void setBusinessKey(String s) {
       businessKey = new BusinessKey(s);
   }

   public String getBusinessKey() {
       return this.businessKey.getText();
   }

   public void setDescriptionString(String s) {
       this.description = new Description(s);
   }

   public String getDescriptionString() {
       return this.description.getText();
   }

   public SharedRelationships getSharedRelationships() {
       return this.sharedRelation;
   }

   public void setSharedRelationships(SharedRelationships s) {
       this.sharedRelation = s;
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
         if (businessKey!=null) {
            businessKey.saveToXML(base);
         }

         if (name!=null) {
            name.saveToXML(base);
         }

         if (description!=null) {
            description.saveToXML(base);
         }

         if (sharedRelation!=null) {
             sharedRelation.saveToXML(base);
         }
         parent.appendChild(base);
   }
}
