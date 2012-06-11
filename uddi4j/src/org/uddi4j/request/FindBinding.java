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
 * This message is used to search for summary results listing registered
 * bindingTemplate data within a businessService matching specific criteria.
 *
 * <p>
 *
 * @author David Melgar  (dmelgar@us.ibm.com)
 */
public class FindBinding extends UDDIElement {
   public static final String UDDI_TAG = "find_binding";

   protected Element base = null;

   String maxRows = null;
   String serviceKey = null;
   FindQualifiers findQualifiers = null;
   TModelBag tModelBag = null;

   /**
    * Default constructor.
    * Avoid using the default constructor for validation. It does not validate
    * required fields. Instead, use the required fields constructor to perform
    * validation.
    */
   public FindBinding() {
   }

   /**
    * Construct the object with required fields.
    *
    * @param serviceKey String
    * @param TModelBag  TModelBag object
    */
   public FindBinding(String serviceKey,
      TModelBag tModelBag) {
      this.serviceKey = serviceKey;
      this.tModelBag = tModelBag;
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
   public FindBinding(Element base) throws UDDIException {
      // Check if it is a fault. Throws an exception if it is.
      super(base);
      maxRows = base.getAttribute("maxRows");
      serviceKey = base.getAttribute("serviceKey");
      NodeList nl = null;
      nl = getChildElementsByTagName(base, FindQualifiers.UDDI_TAG);
      if (nl.getLength() > 0) {
         findQualifiers = new FindQualifiers((Element)nl.item(0));
      }
      nl = getChildElementsByTagName(base, TModelBag.UDDI_TAG);
      if (nl.getLength() > 0) {
         tModelBag = new TModelBag((Element)nl.item(0));
      }
   }

   public void setMaxRows(String s) {
      maxRows = s;
   }
   public void setMaxRows(int s) {
      maxRows = Integer.toString(s);
   }

   public void setServiceKey(String s) {
      serviceKey = s;
   }

   public void setFindQualifiers(FindQualifiers s) {
      findQualifiers = s;
   }

   public void setTModelBag(TModelBag s) {
      tModelBag = s;
   }

   public String getMaxRows() {
      return maxRows;
   }

   public int getMaxRowsInt() {
      return Integer.parseInt(maxRows);
   }

   public String getServiceKey() {
      return serviceKey;
   }


   public FindQualifiers getFindQualifiers() {
      return findQualifiers;
   }


   public TModelBag getTModelBag() {
      return tModelBag;
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
      if (serviceKey!=null) {
         base.setAttribute("serviceKey", serviceKey);
      }
      if (findQualifiers!=null) {
         findQualifiers.saveToXML(base);
      }
      if (tModelBag!=null) {
         tModelBag.saveToXML(base);
      }
      parent.appendChild(base);
   }
}
