/*
 * The source code contained herein is licensed under the IBM Public License
 * Version 1.0, which has been approved by the Open Source Initiative.
 * Copyright (C) 2001, International Business Machines Corporation
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
 * This message is used to save added/updated information describing
 * one or more businessEntity structures.  Either a businessEntity or an
 * uploadRegister object is required.  A message is invalid if contains both
 * types or neither type.
 *
 * <p>
 *
 * @author David Melgar (dmelgar@us.ibm.com)
 */
public class SaveBusiness extends UDDIElement {
   public static final String UDDI_TAG = "save_business";

   protected Element base = null;

   AuthInfo authInfo = null;
   // Vector of BusinessEntity objects
   Vector businessEntity = new Vector();
   // Vector of UploadRegister objects
   Vector uploadRegister = new Vector();

   /**
    * Default constructor.
    * Avoid using the default constructor for validation. It does not validate
    * required fields. Instead, use the required fields constructor to perform
    * validation.
    */
   public SaveBusiness() {
   }

   /**
    * Construct the object with required fields.
    *
    * @param authInfo   String
    */
   public SaveBusiness(String authInfo) {
      this.authInfo = new AuthInfo( authInfo );
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
   public SaveBusiness(Element base) throws UDDIException {
      // Check if it is a fault. Throws an exception if it is.
      super(base);
      NodeList nl = null;
      nl = getChildElementsByTagName(base, AuthInfo.UDDI_TAG);
      if (nl.getLength() > 0) {
         authInfo = new AuthInfo((Element)nl.item(0));
      }
      nl = getChildElementsByTagName(base, BusinessEntity.UDDI_TAG);
      for (int i=0; i < nl.getLength(); i++) {
         businessEntity.addElement(new BusinessEntity((Element)nl.item(i)));
      }
      nl = getChildElementsByTagName(base, UploadRegister.UDDI_TAG);
      for (int i=0; i < nl.getLength(); i++) {
         uploadRegister.addElement(new UploadRegister((Element)nl.item(i)));
      }
   }

   public void setAuthInfo(AuthInfo s) {
      authInfo = s;
   }
   public void setAuthInfo(String s) {
      authInfo = new AuthInfo();
      authInfo.setText(s);
   }

   /**
    * Set businessEntity vector.
    *
    * @param s  Vector of <I>BusinessEntity</I> objects.
    */
   public void setBusinessEntityVector(Vector s) {
      businessEntity = s;
   }

   /**
    * Set uploadRegister vector.
    *
    * @param s  Vector of <I>UploadRegister</I> objects.
    * @deprecated
    */
   public void setUploadRegisterVector(Vector s) {
      uploadRegister = s;
   }

   /**
    * Set uploadRegister.
    *
    * @param s  Vector of <I>String</I> objects.
    * @deprecated
    */
   public void setUploadRegisterStrings(Vector s) {
      uploadRegister = new Vector();
      for (int i = 0; i < s.size(); i++) {
         uploadRegister.addElement( new UploadRegister((String)s.elementAt(i)) );
      }
   }

   public AuthInfo getAuthInfo() {
      return authInfo;
   }

   public String getAuthInfoString() {
      return authInfo.getText();
   }

   /**
    * Get businessEntity vector.
    *
    * @return s Vector of <I>BusinessEntity</I> objects.
    */
   public Vector getBusinessEntityVector() {
      return businessEntity;
   }

   /**
    * Get uploadRegister vector.
    *
    * @return s Vector of <I>UploadRegister</I> objects.
    * @deprecated
    */
   public Vector getUploadRegisterVector() {
      return uploadRegister;
   }

   /**
    * Get uploadRegister.
    *
    * @return s Vector of <I>String</I> objects.
    * @deprecated
    */
   public Vector getUploadRegisterStrings() {
      Vector strings = new Vector();
      for (int i = 0; i < uploadRegister.size(); i++) {
         strings.addElement( ((UploadRegister)uploadRegister.elementAt(i)).getText());
      }
      return strings;
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
      if (authInfo!=null) {
         authInfo.saveToXML(base);
      }
      if (businessEntity!=null) {
        for (int i=0; i < businessEntity.size(); i++) {
           ((BusinessEntity)(businessEntity.elementAt(i))).saveToXML(base);
        }
      }
      if (uploadRegister!=null) {
        for (int i=0; i < uploadRegister.size(); i++) {
           ((UploadRegister)(uploadRegister.elementAt(i))).saveToXML(base);
        }
      }
      parent.appendChild(base);
   }
}
