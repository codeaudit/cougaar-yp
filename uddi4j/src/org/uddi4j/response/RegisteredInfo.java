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
 * This structure is used in the resynch process and is a response
 * to a get_registeredInfo message.
 *
 * <p>
 *
 * @author David Melgar (dmelgar@us.ibm.com)
 * @author Ozzy (ozzy@hursley.ibm.com)
 */
public class RegisteredInfo extends UDDIElement {
   public static final String UDDI_TAG = "registeredInfo";

   protected Element base = null;

   String operator = null;
   String truncated = null;
   BusinessInfos businessInfos = null;
   TModelInfos tModelInfos = null;

   /**
    * Default constructor.
    * Avoid using the default constructor for validation. It does not validate
    * required fields. Instead, use the required fields constructor to perform
    * validation.
    */

   public RegisteredInfo() {
   }

   /**
    * Construct the object with required fields.
    *
    * @param operator String
    * @param BusinessInfos  BusinessInfos object
    * @param TModelInfos  TModelInfos object
    */
   public RegisteredInfo(String operator,
      BusinessInfos businessInfos,
      TModelInfos tModelInfos) {
      this.operator = operator;
      this.businessInfos = businessInfos;
      this.tModelInfos = tModelInfos;
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

   public RegisteredInfo(Element base) throws UDDIException {
      // Check if it is a fault. Throws an exception if it is.
      super(base);
      operator = base.getAttribute("operator");
      truncated = base.getAttribute("truncated");
      NodeList nl = null;
      nl = getChildElementsByTagName(base, BusinessInfos.UDDI_TAG);
      if (nl.getLength() > 0) {
         businessInfos = new BusinessInfos((Element)nl.item(0));
      }
      nl = getChildElementsByTagName(base, TModelInfos.UDDI_TAG);
      if (nl.getLength() > 0) {
         tModelInfos = new TModelInfos((Element)nl.item(0));
      }
   }

   public void setOperator(String s) {
      operator = s;
   }

   public void setTruncated(String s) {
      truncated = s;
   }
   public void setTruncated(boolean s) {
      if (s) {
         truncated = "true";
      } else {
         truncated = "false";
      }
   }

   public void setBusinessInfos(BusinessInfos s) {
      businessInfos = s;
   }

   public void setTModelInfos(TModelInfos s) {
      tModelInfos = s;
   }

   public String getOperator() {
      return operator;
   }


   public String getTruncated() {
      return truncated;
   }

   public boolean getTruncatedBoolean() {
      return "true".equals(truncated);
   }

   public BusinessInfos getBusinessInfos() {
      return businessInfos;
   }


   public TModelInfos getTModelInfos() {
      return tModelInfos;
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
      base.setAttribute("generic", UDDIElement.GENERIC);
      base.setAttribute("xmlns", UDDIElement.XMLNS);
      if (operator!=null) {
         base.setAttribute("operator", operator);
      }
      if (truncated!=null) {
         base.setAttribute("truncated", truncated);
      }
      if (businessInfos!=null) {
         businessInfos.saveToXML(base);
      }
      if (tModelInfos!=null) {
         tModelInfos.saveToXML(base);
      }
      parent.appendChild(base);
   }
}
