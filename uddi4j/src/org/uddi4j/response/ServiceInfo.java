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
 * This structure contains the abbreviated information about registered
 * businessService information.
 *
 * <p>
 *
 * @author David Melgar (dmelgar@us.ibm.com)
 * @author Ozzy (ozzy@hursley.ibm.com)
 */
public class ServiceInfo extends UDDIElement {
   public static final String UDDI_TAG = "serviceInfo";

   protected Element base = null;

   String serviceKey = null;
   String businessKey = null;
   // Vector of name
   Vector nameVector = new Vector();

   /**
    * Default constructor.
    * Avoid using the default constructor for validation. It does not validate
    * required fields. Instead, use the required fields constructor to perform
    * validation.
    */

   public ServiceInfo() {
   }

   /**
    * Construct the object with required fields.
    *
    * @param serviceKey String
    * @param name   String
    */
   public ServiceInfo(String serviceKey,
            String name) {
      this.serviceKey = serviceKey;
      this.nameVector.addElement(new Name(name));
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

   public ServiceInfo(Element base) throws UDDIException {
      // Check if it is a fault. Throws an exception if it is.
      super(base);
      serviceKey = base.getAttribute("serviceKey");
      businessKey = base.getAttribute("businessKey");
      NodeList nl = null;
      nl = getChildElementsByTagName(base, Name.UDDI_TAG);
      for (int i=0; i<nl.getLength(); i++) {
         nameVector.addElement(new Name((Element)nl.item(0)));
      }
   }

   public void setServiceKey(String s) {
      serviceKey = s;
   }

   public void setBusinessKey(String s) {
      businessKey = s;
   }

   /**
    * @deprecated This method has been deprecated. Use
    * {@link #setNameVector (Vector)} or
    * {@link #setDefaultName (Name)} instead
    */
    public void setName(Name s) {
      setDefaultName(s);
    }

   /**
    * @deprecated This method has been deprecated. Use
    * {@link #setNameVector (Vector)} or
    * {@link #setDefaultNameString (String, String)} instead
    */
    public void setName(String s) {
       setDefaultNameString(s, null);
    }

  /**
   * This method stores this name as the Default Name (i.e., places it in the first
   * location in the Vector).
   */
   public void setDefaultName(Name name) {
     if (nameVector.size() > 0) {
      nameVector.setElementAt(name,0);
     } else {
      nameVector.addElement(name);
     }
   }

  /**
   * This method stores this String, in the given language as the Default Name
   * (i.e., places it in the first location in the Vector).
   */
   public void setDefaultNameString(String value, String lang) {
      Name name = new Name(value, lang);
       if (nameVector.size() > 0) {
         nameVector.setElementAt(name,0);
       } else {
         nameVector.addElement(name);
       }
   }

  /**
   * @param s  Vector of <I> Name </I> objects
   */
   public void setNameVector(Vector s) {
      nameVector = s;
   }

   public String getServiceKey() {
      return serviceKey;
   }


   public String getBusinessKey() {
      return businessKey;
   }

  /**
   * @deprecated This method has been deprecated. Use
   * {@link #getNameVector ()} or
   * {@link #getDefaultName ()} instead
   */
   public Name getName() {
      return getDefaultName();
   }

  /**
   * @deprecated This method has been deprecated. Use
   * {@link #getNameVector ()} or
   * {@link #getDefaultNameString ()} instead
   */
   public String getNameString() {
      return getDefaultNameString();
   }


   public Name getDefaultName() {
      return (Name) nameVector.elementAt(0);
   }

  /**
    * Get default name string.
    *
    * @return  String
    */
   public String getDefaultNameString() {
       if ((nameVector).size() > 0) {
        return ((Name)nameVector.elementAt(0)).getText();
       }  else {
           return null;
       }
   }

   /**
    * Get all names.
    *
    * @return  Vector of <I>Name</I> objects.
    */
   public Vector  getNameVector() {
      return nameVector ;
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
      if (serviceKey!=null) {
         base.setAttribute("serviceKey", serviceKey);
      }
      if (businessKey!=null) {
         base.setAttribute("businessKey", businessKey);
      }
      if (nameVector!=null && nameVector.size()>0) {
         for(int i=0;i<nameVector.size();i++) {
           ((Name)nameVector.elementAt(i)).saveToXML(base);
         }
      }
      parent.appendChild(base);
   }
}
