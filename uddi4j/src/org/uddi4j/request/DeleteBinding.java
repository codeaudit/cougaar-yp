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
 * This message is used to delete information about a previously
 * registered bindingTemplate structure.
 *
 * <p>
 *
 * @author David Melgar (dmelgar@us.ibm.com)
 * @author Ozzy (ozzy@hursley.ibm.com)
 */
public class DeleteBinding extends UDDIElement {
   public static final String UDDI_TAG = "delete_binding";

   protected Element base = null;

   AuthInfo authInfo = null;
   // Vector of BindingKey objects
   Vector bindingKey = new Vector();

   /**
    * Default constructor.
    * Avoid using the default constructor for validation. It does not validate
    * required fields. Instead, use the required fields constructor to perform
    * validation.
    */
   public DeleteBinding() {
   }

   /**
    * Construct the object with required fields.
    *
    * @param authInfo String
    * @param bindingKey Vector of BindingKey Strings.
    */
   public DeleteBinding(String authInfo,
      Vector bindingKeyStrings) {
      this.authInfo = new AuthInfo( authInfo );
      Vector objects;
      objects = new Vector();
      for (int i = 0; i < bindingKeyStrings.size(); i++) {
         objects.addElement( new BindingKey((String)bindingKeyStrings.elementAt(i)) );
      }

      this.bindingKey = objects;
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
   public DeleteBinding(Element base) throws UDDIException {
      // Check if it is a fault. Throws an exception if it is.
      super(base);
      NodeList nl = null;
      nl = getChildElementsByTagName(base, AuthInfo.UDDI_TAG);
      if (nl.getLength() > 0) {
         authInfo = new AuthInfo((Element)nl.item(0));
      }
      nl = getChildElementsByTagName(base, BindingKey.UDDI_TAG);
      for (int i=0; i < nl.getLength(); i++) {
         bindingKey.addElement(new BindingKey((Element)nl.item(i)));
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
    * Set bindingKey vector
    *
    * @param s  Vector of <I>BindingKey</I> objects.
    */
   public void setBindingKeyVector(Vector s) {
      bindingKey = s;
   }

   /**
    * Set bindingKey
    *
    * @param s  Vector of <I>String</I> objects.
    */
   public void setBindingKeyStrings(Vector s) {
      bindingKey = new Vector();
      for (int i = 0; i < s.size(); i++) {
         bindingKey.addElement( new BindingKey((String)s.elementAt(i)) );
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
    * Get bindingKey
    *
    * @return s Vector of <I>BindingKey</I> objects.
    */
   public Vector getBindingKeyVector() {
      return bindingKey;
   }

   /**
    * Get bindingKey
    *
    * @return s Vector of <I>String</I> objects.
    */
   public Vector getBindingKeyStrings() {
      Vector strings = new Vector();
      for (int i = 0; i < bindingKey.size(); i++) {
         strings.addElement( ((BindingKey)bindingKey.elementAt(i)).getText());
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
      if (bindingKey!=null) {
        for (int i=0; i < bindingKey.size(); i++) {
           ((BindingKey)(bindingKey.elementAt(i))).saveToXML(base);
        }
      }
      parent.appendChild(base);
   }
}
