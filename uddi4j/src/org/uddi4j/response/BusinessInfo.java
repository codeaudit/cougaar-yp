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
 * This element is used as a short form of the BusinessEntity
 * element. It is used as a first pass result set for "find businesses" queries.
 *
 * <p>
 *
 * @author David Melgar (dmelgar@us.ibm.com)
 * @author Ozzy (ozzy@hursley.ibm.com)
 */
public class BusinessInfo extends UDDIElement {
   public static final String UDDI_TAG = "businessInfo";

   protected Element base = null;

   String businessKey = null;
   ServiceInfos serviceInfos = null;
   // Vector of Name objects
   Vector name = new Vector();
   // Vector of Description objects
   Vector description = new Vector();

   /**
    * Default constructor.
    * Avoid using the default constructor for validation. It does not validate
    * required fields. Instead, use the required fields constructor to perform
    * validation.
    */

   public BusinessInfo() {
   }

   /**
    * Construct the object with required fields.
    *
    * @param businessKey    String
    * @param name   String
    * @param ServiceInfos   ServiceInfos object
    */
   public BusinessInfo(String businessKey,
            String name,
            ServiceInfos serviceInfos) {
      this.businessKey = businessKey;
      this.name.addElement(new Name(name));
      this.serviceInfos = serviceInfos;
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

   public BusinessInfo(Element base) throws UDDIException {
      // Check if it is a fault. Throws an exception if it is.
      super(base);
      businessKey = base.getAttribute("businessKey");
      NodeList nl = null;
      nl = getChildElementsByTagName(base, Name.UDDI_TAG);
      for(int i=0; i<nl.getLength() ; i++) {
         name.addElement(new Name((Element)nl.item(i)));
      }
      nl = getChildElementsByTagName(base, ServiceInfos.UDDI_TAG);
      if (nl.getLength() > 0) {
         serviceInfos = new ServiceInfos((Element)nl.item(0));
      }
      nl = getChildElementsByTagName(base, Description.UDDI_TAG);
      for (int i=0; i < nl.getLength(); i++) {
         description.addElement(new Description((Element)nl.item(i)));
      }
   }

   public void setBusinessKey(String s) {
      businessKey = s;
   }
   /**
    * @deprecated This method has been deprecated. Use
    * {@link #setNameVector( Vector )} or
    * {@link #setDefaultName( Name )} instead
    */
   public void setName(Name s) {
     setDefaultName(s);
   }
   /**
    * @deprecated This method has been deprecated. Use
    * {@link #setNameVector( Vector )} or
    * {@link #setDefaultNameString(String, String)} instead.
    */
   public void setName(String s) {
     setDefaultNameString(s,null);
   }
   /**
    * This method stores this name as the Default Name (i.e., places it in the 
    * first location in the Vector).
    */
   public void setDefaultName(Name name) {
      if (this.name.size() > 0) {
        this.name.setElementAt(name,0);
      }else{
        this.name.addElement(name);
      }
   }
   /**
    * This method stores this String, in the given language as the Default Name
    * (i.e., places it in the first location in the Vector).
    */
   public void setDefaultNameString(String value, String lang) {
      Name n = new Name(value, lang);
      if(this.name.size() > 0) {
        name.setElementAt(n,0);
      }else{
        name.addElement(n);
      }
   }
   /**
    * @param s Vector of <i>Name</i> objects
    */
   public void setNameVector(Vector s) {
     name = s;
   }

   public void setServiceInfos(ServiceInfos s) {
      serviceInfos = s;
   }

   /**
    * Set description vector
    *
    * @param s  Vector of <I>Description</I> objects.
    */
   public void setDescriptionVector(Vector s) {
      description = s;
   }

   /**
    * Set default (english) description string
    *
    * @param s  String
    */
   public void setDefaultDescriptionString(String s) {
      if (description.size() > 0) {
         description.setElementAt(new Description(s), 0);
      } else {
         description.addElement(new Description(s));
      }
   }

   public String getBusinessKey() {
      return businessKey;
   }

   /**
    * @deprecated This method has been deprecated. Use
    * {@link #getNameVector ()} or 
    * {@link #getDefaultName ()} instead.
    */
   public Name getName() {
     return getDefaultName();
   }
   
   /**
    * @deprecated This method has been deprecated. Use
    * {@link #getNameVector ()} or 
    * {@link #getDefaultNameString ()} instead.
    */
   public String getNameString() {
      return getDefaultNameString();
   }

   /**
    * Get default name
    * @return Name
    */
   public Name getDefaultName() {
     if(name.size() > 0) {
       return (Name) name.elementAt(0);
     }else{
       return null;
     }
   }

   /** 
    * Get default name string
    * @return String
    */
   public String getDefaultNameString() {
     if( name.size() > 0) {
       return ((Name)name.elementAt(0)).getText();
     }else{
       return null;
     }
   }

   /**
    * Get all Names.
    * @return Vector of <i>Name</i> objects.
    */
   public Vector getNameVector() {
     return name;
   }

   public ServiceInfos getServiceInfos() {
      return serviceInfos;
   }


   /**
    * Get description
    *
    * @return s Vector of <I>Description</I> objects.
    */
   public Vector getDescriptionVector() {
      return description;
   }

   /**
    * Get default description string
    *
    * @return s String
    */
   public String getDefaultDescriptionString() {
      if ((description).size() > 0) {
         Description t = (Description)description.elementAt(0);
         return t.getText();
      } else {
         return null;
      }
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
         base.setAttribute("businessKey", businessKey);
      }
      if (name!=null) {
         for (int i=0; i < name.size(); i++) {
           ((Name)(name.elementAt(i))).saveToXML(base);
         }
      }
      if (description!=null) {
         for (int i=0; i < description.size(); i++) {
            ((Description)(description.elementAt(i))).saveToXML(base);
	 }
      }
      if (serviceInfos!=null) {
         serviceInfos.saveToXML(base);
      }
      parent.appendChild(base);
   }
}
