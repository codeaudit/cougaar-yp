/*
 * The source code contained herein is licensed under the IBM Public License
 * Version 1.0, which has been approved by the Open Source Initiative.
 * Copyright (C) 2001, International Business Machines Corporation
 * All Rights Reserved.
 *
 */

package org.uddi4j.datatype.business;

import java.util.Vector;
import org.w3c.dom.*;
import org.uddi4j.*;
import org.uddi4j.datatype.*;
import org.uddi4j.datatype.binding.*;
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
 *   <li>Constructor passing required fields.
 *   <li>Constructor that will instantiate the object from an appropriate XML
 *       DOM element.
 *   <li>Get/set methods for each attribute that this element can contain.
 *   <li>A get/setVector method is provided for sets of attributes.
 *   <li>SaveToXML method. Serializes this class within a passed in element.
 *
 * </ul>
 * Typically, this class is used to construct parameters for, or interpret
 * responses from methods in the UDDIProxy class.
 *
 * <p><b>Element description:</b><p>
 * Data: Can contain a contact’s name, address, phone, e-mail, and a description
 * of the contact.
 * <p>
 *
 * @author David Melgar (dmelgar@us.ibm.com)
 */
public class Contact extends UDDIElement {
   public static final String UDDI_TAG = "contact";

   protected Element base = null;

   String useType = null;
   PersonName personName = null;
   // Vector of Description objects
   Vector description = new Vector();
   // Vector of Phone objects
   Vector phone = new Vector();
   // Vector of Email objects
   Vector email = new Vector();
   // Vector of Address objects
   Vector address = new Vector();

   /**
    * Default constructor.
    * Avoid using the default constructor for validation. It does not validate
    * required fields. Instead, use the required fields constructor to perform
    * validation.
    */
   public Contact() {
   }

   /**
    * Construct the object with required fields.
    *
    * @param personName String
    */
   public Contact(String personName) {
      this.personName = new PersonName( personName );
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
   public Contact(Element base) throws UDDIException {
      // Check if its a fault. Throws an exception if it is.
      super(base);
      useType = base.getAttribute("useType");
      NodeList nl = null;
      nl = getChildElementsByTagName(base, PersonName.UDDI_TAG);
      if (nl.getLength() > 0) {
         personName = new PersonName((Element)nl.item(0));
      }
      nl = getChildElementsByTagName(base, Description.UDDI_TAG);
      for (int i=0; i < nl.getLength(); i++) {
         description.addElement(new Description((Element)nl.item(i)));
      }
      nl = getChildElementsByTagName(base, Phone.UDDI_TAG);
      for (int i=0; i < nl.getLength(); i++) {
         phone.addElement(new Phone((Element)nl.item(i)));
      }
      nl = getChildElementsByTagName(base, Email.UDDI_TAG);
      for (int i=0; i < nl.getLength(); i++) {
         email.addElement(new Email((Element)nl.item(i)));
      }
      nl = getChildElementsByTagName(base, Address.UDDI_TAG);
      for (int i=0; i < nl.getLength(); i++) {
         address.addElement(new Address((Element)nl.item(i)));
      }
   }

   public void setUseType(String s) {
      useType = s;
   }

   public void setPersonName(PersonName s) {
      personName = s;
   }
   public void setPersonName(String s) {
      personName = new PersonName();
      personName.setText(s);
   }

   /**
    * Set description vector.
    *
    * @param s  Vector of <I>Description</I> objects.
    */
   public void setDescriptionVector(Vector s) {
      description = s;
   }

   /**
    * Set default (english) description string.
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

   /**
    * Set phone vector.
    *
    * @param s  Vector of <I>Phone</I> objects.
    */
   public void setPhoneVector(Vector s) {
      phone = s;
   }

   /**
    * Set email vector.
    *
    * @param s  Vector of <I>Email</I> objects.
    */
   public void setEmailVector(Vector s) {
      email = s;
   }

   /**
    * Set address vector.
    *
    * @param s  Vector of <I>Address</I> objects.
    */
   public void setAddressVector(Vector s) {
      address = s;
   }

   public String getUseType() {
      return useType;
   }


   public PersonName getPersonName() {
      return personName;
   }

   public String getPersonNameString() {
      return personName.getText();
   }

   /**
    * Get description.
    *
    * @return s Vector of <I>Description</I> objects.
    */
   public Vector getDescriptionVector() {
      return description;
   }

   /**
    * Get default description string.
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
    * Get phone.
    *
    * @return s Vector of <I>Phone</I> objects.
    */
   public Vector getPhoneVector() {
      return phone;
   }

   /**
    * Get email.
    *
    * @return s Vector of <I>Email</I> objects.
    */
   public Vector getEmailVector() {
      return email;
   }

   /**
    * Get address.
    *
    * @return s Vector of <I>Address</I> objects.
    */
   public Vector getAddressVector() {
      return address;
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
      if (useType!=null) {
         base.setAttribute("useType", useType);
      }
      if (description!=null) {
        for (int i=0; i < description.size(); i++) {
           ((Description)(description.elementAt(i))).saveToXML(base);
        }
      }
      if (personName!=null) {
         personName.saveToXML(base);
      }
      if (phone!=null) {
        for (int i=0; i < phone.size(); i++) {
           ((Phone)(phone.elementAt(i))).saveToXML(base);
        }
      }
      if (email!=null) {
        for (int i=0; i < email.size(); i++) {
           ((Email)(email.elementAt(i))).saveToXML(base);
        }
      }
      if (address!=null) {
        for (int i=0; i < address.size(); i++) {
           ((Address)(address.elementAt(i))).saveToXML(base);
        }
      }
      parent.appendChild(base);
   }
}
