/*
 * The source code contained herein is licensed under the IBM Public License
 * Version 1.0, which has been approved by the Open Source Initiative.
 * Copyright (C) 2001, International Business Machines Corporation
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
 * This message is used to search for summary results listing registered
 * businessEntity data matching specific criteria.
 *
 * <p>
 *
 * @author David Melgar (dmelgar@us.ibm.com)
 * @author Ravi Trivedi (ravi_trivedi@hp.com)
 * @author Vivek Chopra (vivek@soaprpc.com)
 */
public class FindBusiness extends UDDIElement {
   public static final String UDDI_TAG = "find_business";

   protected Element base = null;

   String maxRows = null;
   FindQualifiers findQualifiers = null;
   IdentifierBag identifierBag = null;
   CategoryBag categoryBag = null;
   TModelBag tModelBag = null;
   DiscoveryURLs discoveryURLs = null;
   Vector nameVector = new Vector();

   /**
    * Default constructor.
    * Avoid using the default constructor for validation. It does not validate
    * required fields. Instead, use the required fields constructor to perform
    * validation.
    */
   public FindBusiness() {
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
   public FindBusiness(Element base) throws UDDIException {
      // Check if it is a fault. Throws an exception if it is.
      super(base);
      maxRows = base.getAttribute("maxRows");
      NodeList nl = null;
      nl = getChildElementsByTagName(base, FindQualifiers.UDDI_TAG);
      if (nl.getLength() > 0) {
         findQualifiers = new FindQualifiers((Element)nl.item(0));
      }
      nl = getChildElementsByTagName(base, Name.UDDI_TAG);
      for (int i=0; i < nl.getLength(); i++) {
          nameVector.addElement(new Name((Element)nl.item(i)));
      }
      nl = getChildElementsByTagName(base, IdentifierBag.UDDI_TAG);
      if (nl.getLength() > 0) {
         identifierBag = new IdentifierBag((Element)nl.item(0));
      }
      nl = getChildElementsByTagName(base, CategoryBag.UDDI_TAG);
      if (nl.getLength() > 0) {
         categoryBag = new CategoryBag((Element)nl.item(0));
      }
      nl = getChildElementsByTagName(base, TModelBag.UDDI_TAG);
      if (nl.getLength() > 0) {
         tModelBag = new TModelBag((Element)nl.item(0));
      }
      nl = getChildElementsByTagName(base, DiscoveryURLs.UDDI_TAG);
      if (nl.getLength() > 0) {
         discoveryURLs = new DiscoveryURLs((Element)nl.item(0));
      }
   }

   public void setMaxRows(String s) {
      maxRows = s;
   }
   public void setMaxRows(int s) {
      maxRows = Integer.toString(s);
   }

   public void setFindQualifiers(FindQualifiers s) {
      findQualifiers = s;
   }

   /**
    * @deprecated This method has been deprecated. Use
    * {@link #setNameVector(Vector)} or
    * {@link #setDefaultName(Name)}
    */
   public void setName(Name s) {
      setDefaultName(s);
   }

   /**
    * @deprecated This method has been deprecated. Use
    * {@link #setNameVector(Vector)} or
    * {@link #setDefaultNameString(String, String)}
    */
   public void setName(String s) {
      setDefaultNameString(s,null);
   }

   /**
    * This method stores this name as the Default Name
    * (i.e., places it in the first location in the Vector)
    */
   public void setDefaultName(Name name) {
      if (nameVector.size() > 0) {
       nameVector.setElementAt(name,0);
      } else {
       nameVector.addElement(name);
      }
   }

   /**
    * This stores this String, in the given language, as the Default Name
    * (i.e., places it in the first location in the Vector.)
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

   public void setIdentifierBag(IdentifierBag s) {
      identifierBag = s;
   }

   public void setCategoryBag(CategoryBag s) {
      categoryBag = s;
   }

   public void setTModelBag(TModelBag s) {
      tModelBag = s;
   }

   public void setDiscoveryURLs(DiscoveryURLs s) {
      discoveryURLs = s;
   }

   public String getMaxRows() {
      return maxRows;
   }

   public int getMaxRowsInt() {
      return Integer.parseInt(maxRows);
   }

   public FindQualifiers getFindQualifiers() {
      return findQualifiers;
   }

   /**
    * @deprecated This method has been deprecated. Use
    * {@link #getNameVector()} or
    * {@link #getDefaultName()}
    */
   public Name getName() {
      return getDefaultName();
   }

   /**
    * @deprecated This method has been deprecated. Use
    * {@link #getNameVector()} or
    * {@link #getDefaultNameString()}
    */
   public String getNameString() {
      return getDefaultNameString();
   }

   public Name getDefaultName() {
      return (Name) nameVector.elementAt(0);
   }

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

   public IdentifierBag getIdentifierBag() {
      return identifierBag;
   }


   public CategoryBag getCategoryBag() {
      return categoryBag;
   }


   public TModelBag getTModelBag() {
      return tModelBag;
   }


   public DiscoveryURLs getDiscoveryURLs() {
      return discoveryURLs;
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
      if (maxRows!=null) {
         base.setAttribute("maxRows", maxRows);
      }
      if (findQualifiers!=null) {
         findQualifiers.saveToXML(base);
      }
      if (nameVector!=null) {
        for (int i=0; i < nameVector.size(); i++) {
           ((Name)(nameVector.elementAt(i))).saveToXML(base);
        }
      }
      if (identifierBag!=null) {
         identifierBag.saveToXML(base);
      }
      if (categoryBag!=null) {
         categoryBag.saveToXML(base);
      }
      if (tModelBag!=null) {
         tModelBag.saveToXML(base);
      }
      if (discoveryURLs!=null) {
         discoveryURLs.saveToXML(base);
      }
      parent.appendChild(base);
   }
}
