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
 * This structure supports the dispositionReport structure.
 *
 * <p>
 *
 * @author David Melgar (dmelgar@us.ibm.com)
 */
public class Result extends UDDIElement {
   public static final String UDDI_TAG = "result";

   protected Element base = null;

   String keyType = null;
   String errno = null;
   ErrInfo errInfo = null;

   /**
    * Default constructor.
    * Avoid using the default constructor for validation. It does not validate
    * required fields. Instead, use the required fields constructor to perform
    * validation.
    */

   public Result() {
   }

   /**
    * Construct the object with required fields.
    *
    * @param errno  String
    */
   public Result(String errno) {
      this.errno = errno;
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

   public Result(Element base) throws UDDIException {
      // Check if it is a fault. Throws an exception if it is.
      super(base);
      keyType = base.getAttribute("keyType");
      errno = base.getAttribute("errno");
      NodeList nl = null;
      nl = getChildElementsByTagName(base, ErrInfo.UDDI_TAG);
      if (nl.getLength() > 0) {
         errInfo = new ErrInfo((Element)nl.item(0));
      }
   }

   public void setKeyType(String s) {
      keyType = s;
   }

   public void setErrno(String s) {
      errno = s;
   }

   public void setErrInfo(ErrInfo s) {
      errInfo = s;
   }

   public String getKeyType() {
      return keyType;
   }


   public String getErrno() {
      return errno;
   }


   public ErrInfo getErrInfo() {
      return errInfo;
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
      if (keyType!=null) {
         base.setAttribute("keyType", keyType);
      }
      if (errno!=null) {
         base.setAttribute("errno", errno);
      }
      if (errInfo!=null) {
         errInfo.saveToXML(base);
      }
      parent.appendChild(base);
   }
}
