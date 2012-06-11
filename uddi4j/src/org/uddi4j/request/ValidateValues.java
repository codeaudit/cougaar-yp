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
 * Validate Values is  used is to verify that specific categories or identifiers
 * (checking the keyValue attribute values supplied) exist within the given
 * taxonomy or identifier system.
 *
 * <p>
 *
 * @author Rajesh Sumra (rajesh_sumra@hp.com)
 * @author Arulazi D    (arulazi_d@hp.com)
 */
public class ValidateValues extends UDDIElement {
   public static final String UDDI_TAG = "validate_values";

   protected Element base = null;

   Vector businessServiceVector = new Vector();
   Vector businessEntityVector = new Vector();
   Vector tModelVector = new Vector();

   /**
    * Default constructor.
    * Avoid using the default constructor for validation. It does not validate
    * required fields. Instead, use the required fields constructor to perform
    * validation.
    */
   public ValidateValues() {
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
   public ValidateValues(Element base) throws UDDIException {
      // Check if its a fault. Throw exception if it is
      super(base);
      NodeList nl = null;

      nl = getChildElementsByTagName(base, BusinessEntity.UDDI_TAG);
      for (int i=0; i < nl.getLength(); i++) {
          businessEntityVector.addElement(new BusinessEntity((Element)nl.item(i)));
      }

      nl = getChildElementsByTagName(base, BusinessService.UDDI_TAG);
      for (int i=0; i < nl.getLength(); i++) {
          businessServiceVector.addElement(new BusinessService((Element)nl.item(i)));
      }

      nl = getChildElementsByTagName(base, TModel.UDDI_TAG);
      for (int i=0; i < nl.getLength(); i++) {
          tModelVector.addElement(new TModel((Element)nl.item(i)));
      }
   }

   public Vector getBusinessServiceVector() {
       return this.businessServiceVector;
   }

   public void setBusinessServiceVector(Vector businessService) {
       this.businessServiceVector = businessService;
   }

   public Vector getBusinessEntityVector() {
       return this.businessEntityVector;
   }

   public void setBusinessEntityVector(Vector businessEntity) {
       this.businessEntityVector = businessEntity;
   }

   public Vector getTModelVector() {
       return this.tModelVector;
   }

   public void setTModelVector(Vector tModel) {
       this.tModelVector = tModel;
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
       if (businessEntityVector!=null) {
         for (int i=0; i < businessEntityVector.size(); i++) {
            ((BusinessEntity)(businessEntityVector.elementAt(i))).saveToXML(base);
         }
       }
       if (businessServiceVector!=null) {
         for (int i=0; i < businessServiceVector.size(); i++) {
            ((BusinessService)(businessServiceVector.elementAt(i))).saveToXML(base);
         }
       }
       if (tModelVector!=null) {
         for (int i=0; i < tModelVector.size(); i++) {
            ((TModel)(tModelVector.elementAt(i))).saveToXML(base);
         }
       }

       parent.appendChild(base);
   }
}
