/*
 * The source code contained herein is licensed under the IBM Public License
 * Version 1.0, which has been approved by the Open Source Initiative.
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
 * This class returns all complete and incomplete assertions. It also provides
 * some administrative functions, such as, determining if there are
 * outstanding, or incomplete assertions about relationships between
 * the publisher account and associated businesses.
 *
 * <p>
 *
 * @author Mahesh C S (csmahesh@india.hp.com)
 * @author Ozzy (ozzy@hursley.ibm.com)
 */
public class GetAssertionStatusReport extends UDDIElement {
   public static final String UDDI_TAG = "get_assertionStatusReport";

   protected Element base = null;

   AuthInfo authInfo = null;

   CompletionStatus completionStatus = null;

   /**
    * Default constructor.
    * Avoid using the default constructor for validation. It does not validate
    * required fields. Instead, use the required fields constructor to perform
    * validation.
    */
   public GetAssertionStatusReport() {
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
   public GetAssertionStatusReport(Element base) throws UDDIException {
       // Check if it is a fault. Throws an exception if it is.
       super(base);
       NodeList nl = null;
       nl = getChildElementsByTagName(base, AuthInfo.UDDI_TAG);
       if (nl.getLength() > 0) {
          authInfo = new AuthInfo((Element)nl.item(0));
       }
       if (nl.getLength() > 0) {
          completionStatus = new CompletionStatus((Element)nl.item(0));
       }
   }

   /**
    * Construct the object with required fields.
    *
    * @param authInfo   String
    */
   public GetAssertionStatusReport(String authInfo) {
       this.authInfo = new AuthInfo(authInfo);
   }

   /**
    * Construct the object with required fields.
    *
    * @param authInfo   String
    * @param completion String
    */
   public GetAssertionStatusReport(String authInfo,
                                   String completion) {
       this.authInfo = new AuthInfo(authInfo);
       this.completionStatus = new CompletionStatus();
       this.completionStatus.setText(completion);
   }

   /**
    * Construct the object with required fields.
    *
    * @param authInfo   String
    * @param completion CompletionStatus
    */
   public GetAssertionStatusReport(String authInfo,
                                   CompletionStatus completion) {
       this.authInfo = new AuthInfo(authInfo);
       this.completionStatus = completion;
   }

   public CompletionStatus getCompletionStatus() {
       return completionStatus;
   }

   public void setCompletionStatus(CompletionStatus  status) {
       completionStatus = status;
   }

   public String getCompletionStatusString() {
       if(completionStatus!=null)
         return completionStatus.getText();
       else
         return null;
   }

   public void setCompletionStatusString(String s) {
       completionStatus = new CompletionStatus();
       completionStatus.setText(s);
   }

   public AuthInfo getAuthInfo() {
       return authInfo;
   }

   public void setAuthInfo(AuthInfo s) {
       authInfo = s;
   }

   public String getAuthInfoString() {
       if(authInfo != null)
         return authInfo.getText();
       else
         return null;
   }

   public void setAuthInfo(String s) {
       authInfo = new AuthInfo();
       authInfo.setText(s);
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
       if (completionStatus!=null) {
          completionStatus.saveToXML(base);
       }

       parent.appendChild(base);
   }
}
