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
import org.uddi4j.request.*;
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
 * This message is used to delete information about a previously
 * registered businessEntity.
 *
 * <p>
 *
 * @author David Melgar (dmelgar@us.ibm.com)
 * @author Ozzy (ozzy@hursley.ibm.com)
 */
public class DeleteBusiness extends UDDIElement {
   public static final String UDDI_TAG = "delete_business";

   protected Element base = null;

   AuthInfo authInfo = null;
   // Vector of BusinessKey objects
   Vector businessKey = new Vector();

   /**
    * Default constructor.
    * Avoid using the default constructor for validation. It does not validate
    * required fields. Instead, use the required fields constructor to perform
    * validation.
    */
   public DeleteBusiness() {
   }

   /**
    * Construct the object with required fields.
    *
    * @param authInfo   String
    * @param businessKey    Vector of BusinessKey Strings.
    */
   public DeleteBusiness(String authInfo,
                    Vector businessKeyStrings) {
      this.authInfo = new AuthInfo( authInfo );
      Vector objects;
      objects = new Vector();
      for (int i = 0; i < businessKeyStrings.size(); i++) {
         objects.addElement( new BusinessKey((String)businessKeyStrings.elementAt(i)) );
      }

      this.businessKey = objects;
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
   public DeleteBusiness(Element base) throws UDDIException {
      // Check if it is a fault. Throws an exception if it is.
      super(base);
      NodeList nl = null;
      nl = getChildElementsByTagName(base, AuthInfo.UDDI_TAG);
      if (nl.getLength() > 0) {
         authInfo = new AuthInfo((Element)nl.item(0));
      }
      nl = getChildElementsByTagName(base, BusinessKey.UDDI_TAG);
      for (int i=0; i < nl.getLength(); i++) {
         businessKey.addElement(new BusinessKey((Element)nl.item(i)));
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
    * Set businessKey vector
    *
    * @param s  Vector of <I>BusinessKey</I> objects.
    */
   public void setBusinessKeyVector(Vector s) {
      businessKey = s;
   }

   /**
    * Set businessKey
    *
    * @param s  Vector of <I>String</I> objects.
    */
   public void setBusinessKeyStrings(Vector s) {
      businessKey = new Vector();
      for (int i = 0; i < s.size(); i++) {
         businessKey.addElement( new BusinessKey((String)s.elementAt(i)) );
      }
   }

   public AuthInfo getAuthInfo() {
      return authInfo;
   }

   public String getAuthInfoString() {
      if(authInfo!=null)
        return authInfo.getText();
      else
        return null;
   }

   /**
    * Get businessKey
    *
    * @return s Vector of <I>BusinessKey</I> objects.
    */
   public Vector getBusinessKeyVector() {
      return businessKey;
   }

   /**
    * Get businessKey
    *
    * @return s Vector of <I>String</I> objects.
    */
   public Vector getBusinessKeyStrings() {
      Vector strings = new Vector();
      for (int i = 0; i < businessKey.size(); i++) {
         strings.addElement( ((BusinessKey)businessKey.elementAt(i)).getText());
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
      if (businessKey!=null) {
        for (int i=0; i < businessKey.size(); i++) {
           ((BusinessKey)(businessKey.elementAt(i))).saveToXML(base);
        }
      }
      parent.appendChild(base);
   }
}
