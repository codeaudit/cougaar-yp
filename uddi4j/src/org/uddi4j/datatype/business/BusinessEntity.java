/*
 * The source code contained herein is licensed under the IBM Public License
 * Version 1.0, which has been approved by the Open Source Initiative.
 * Copyright (C) 2001, International Business Machines Corporation
 * Copyright (C) 2001, Hewlett-Packard Company
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
 * Primary Data type: Describes an instance of a business or business unit.
 *
 * <p>
 *
 * @author David Melgar (dmelgar@us.ibm.com)
 * @author Ravi Trivedi (ravi_trivedi@hp.com)
 * @author Vivek Chopra (vivek@soaprpc.com)
 */
public class BusinessEntity extends UDDIElement {
   public static final String UDDI_TAG = "businessEntity";

   protected Element base = null;

   String businessKey = null;
   String operator = null;
   String authorizedName = null;
   DiscoveryURLs discoveryURLs = null;
   Contacts contacts = null;
   BusinessServices businessServices = null;
   IdentifierBag identifierBag = null;
   CategoryBag categoryBag = null;
   // Vector of Description objects
   Vector description = new Vector();
   Vector nameVector = new Vector();

   /**
    * Default constructor.
    * Avoid using the default constructor for validation. It does not validate
    * required fields. Instead, use the required fields constructor to perform
    * validation.
    */
   public BusinessEntity() {
   }

   /**
    * Construct the object with required fields.
    * Publishing several names (e.g., for romanization purposes) is supported.
    * To indicate the language that the names are expressed in, a unique
    * xml:lang value is associated with each name.
    * <b>Only one name (default language) can omit its unique language
    * identifier.</b> Other names passed without an xml:lang value associated,
    * are assigned the default language code of the registering party.
    * The default language code is established at the time the publishing
    * credentials are established with an individual Operator Site.
    * If no default language is provisioned at the time a publisher signs up,
    * the operator can adopt an appropriate default language code.
    *
    * @param businessKey    String
    * @param name   String
    */
   public BusinessEntity(String businessKey,
            String name) {
      this.businessKey = businessKey;
      nameVector.addElement(new Name(name));
   }

    /**
    * Construct the object with required fields.
    *
    * @param businessKey    String
    * @param name   String
    * @param lang   String
    */
   public BusinessEntity(String businessKey,
            String name, String lang) {
      this.businessKey = businessKey;
      nameVector.addElement(new Name(name,lang));
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
   public BusinessEntity(Element base) throws UDDIException {
      // Check if its a fault. Throws an exception if it is.
      super(base);
      businessKey = base.getAttribute("businessKey");
      operator = base.getAttribute("operator");
      authorizedName = base.getAttribute("authorizedName");
      NodeList nl = null;
      nl = getChildElementsByTagName(base, DiscoveryURLs.UDDI_TAG);
      if (nl.getLength() > 0) {
         discoveryURLs = new DiscoveryURLs((Element)nl.item(0));
      }
      nl = getChildElementsByTagName(base, Name.UDDI_TAG);
      for (int i=0; i < nl.getLength(); i++) {
          nameVector.addElement(new Name((Element)nl.item(i)));
      }
      nl = getChildElementsByTagName(base, Contacts.UDDI_TAG);
      if (nl.getLength() > 0) {
         contacts = new Contacts((Element)nl.item(0));
      }
      nl = getChildElementsByTagName(base, BusinessServices.UDDI_TAG);
      if (nl.getLength() > 0) {
         businessServices = new BusinessServices((Element)nl.item(0));
      }
      nl = getChildElementsByTagName(base, IdentifierBag.UDDI_TAG);
      if (nl.getLength() > 0) {
         identifierBag = new IdentifierBag((Element)nl.item(0));
      }
      nl = getChildElementsByTagName(base, CategoryBag.UDDI_TAG);
      if (nl.getLength() > 0) {
         categoryBag = new CategoryBag((Element)nl.item(0));
      }
      nl = getChildElementsByTagName(base, Description.UDDI_TAG);
      for (int i=0; i < nl.getLength(); i++) {
         description.addElement(new Description((Element)nl.item(i)));
      }
   }

   public void setBusinessKey(String s) {
      businessKey = s;
   }

   public void setOperator(String s) {
      operator = s;
   }

   public void setAuthorizedName(String s) {
      authorizedName = s;
   }

   public void setDiscoveryURLs(DiscoveryURLs s) {
      discoveryURLs = s;
   }

   /**
    * @deprecated This method has been deprecated. Use
    * {@link #setNameVector(Vector)} or
    * {@link #setDefaultName(Name)} instead
    */
    public void setName(Name s) {
      setDefaultName(s);
    }

   /**
    * @deprecated This method has been deprecated. Use
    * {@link #setNameVector(Vector)} or
    * {@link #setDefaultNameString(String, String)} instead
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

   public void setContacts(Contacts s) {
      contacts = s;
   }

   public void setBusinessServices(BusinessServices s) {
      businessServices = s;
   }

   public void setIdentifierBag(IdentifierBag s) {
      identifierBag = s;
   }

   public void setCategoryBag(CategoryBag s) {
      categoryBag = s;
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
    * Set default description string.
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


   public String getOperator() {
      return operator;
   }


   public String getAuthorizedName() {
      return authorizedName;
   }


   public DiscoveryURLs getDiscoveryURLs() {
      return discoveryURLs;
   }

  /**
   * @deprecated This method has been deprecated. Use
   * {@link #getNameVector()} or
   * {@link #getDefaultName()} instead
   */
   public Name getName() {
      return getDefaultName();
   }

  /**
   * @deprecated This method has been deprecated. Use
   * {@link #getNameVector()} or
   * {@link #getDefaultNameString()} instead
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

   public Contacts getContacts() {
      return contacts;
   }


   public BusinessServices getBusinessServices() {
      return businessServices;
   }


   public IdentifierBag getIdentifierBag() {
      return identifierBag;
   }


   public CategoryBag getCategoryBag() {
      return categoryBag;
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
      if (operator!=null) {
         base.setAttribute("operator", operator);
      }
      if (authorizedName!=null) {
         base.setAttribute("authorizedName", authorizedName);
      }
      if (discoveryURLs!=null) {
         discoveryURLs.saveToXML(base);
      }
      if (nameVector!=null) {
        for (int i=0; i < nameVector.size(); i++) {
           ((Name)(nameVector.elementAt(i))).saveToXML(base);
        }
      }
      if (description!=null) {
        for (int i=0; i < description.size(); i++) {
           ((Description)(description.elementAt(i))).saveToXML(base);
        }
      }
      if (contacts!=null) {
         contacts.saveToXML(base);
      }
      if (businessServices!=null) {
         businessServices.saveToXML(base);
      }
      if (identifierBag!=null) {
         identifierBag.saveToXML(base);
      }
      if (categoryBag!=null) {
         categoryBag.saveToXML(base);
      }
      parent.appendChild(base);
   }
}

