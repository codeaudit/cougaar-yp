/*
 * The source code contained herein is licensed under the IBM Public License
 * Version 1.0, which has been approved by the Open Source Initiative.
 * Copyright (C) 2001, Hewlett-Packard Company
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
 * This class is used to express the status of publisher assertions.
 * For example   <I> status:complete: </I> means the publisher assertions are complete.
 *
 * <p>
 *
 * @author Mahesh C S (csmahesh@india.hp.com)
 */
public class CompletionStatus extends UDDIElement {
   public static final String UDDI_TAG = "completionStatus";

   public static final String COMPLETE = "status:complete";
   public static final String TOKEY_INCOMPLETE= "status:toKey_incomplete";
   public static final String FROMKEY_INCOMPLETE = "status:fromKey_incomplete";

   protected Element base = null;

   String text = null;


   /**
    * Default constructor.
    * Avoid using the default constructor for validation. It does not validate
    * required fields. Instead, use the required fields constructor to perform
    * validation.
    */

   public CompletionStatus() {
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

   public CompletionStatus(Element base) throws UDDIException {
       super(base);
       text = getText(base);
   }

   /**
    * Construct the object with required fields.
    *
    * @param status String
    */
   public CompletionStatus(String status) {
       setText(status);
   }

   public String getText() {
       return text;
   }

   public void setText(String status) {
       text = status;
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

       if (text!=null) {
          base.appendChild(parent.getOwnerDocument().createTextNode(text));
       }
       parent.appendChild(base);
   }
}
