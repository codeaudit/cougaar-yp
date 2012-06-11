/*
 * The source code contained herein is licensed under the IBM Public License
 * Version 1.0, which has been approved by the Open Source Initiative.
 * Copyright (C) 2001, International Business Machines Corporation
 * Copyright (C) 2001, Hewlett-Packard Company
 * All Rights Reserved.
 *
 */

package org.uddi4j.util;

import java.util.Vector;
import org.w3c.dom.*;
import org.uddi4j.*;
import org.uddi4j.datatype.*;
import org.uddi4j.datatype.binding.*;
import org.uddi4j.datatype.business.*;
import org.uddi4j.datatype.service.*;
import org.uddi4j.datatype.tmodel.*;
import org.uddi4j.request.*;
import org.uddi4j.response.*;

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
 * This structure is provided to signal the behavior of the find
 * operations.  See appropriate appendix in API specification.
 *
 * <p>
 *
 * @author David Melgar (dmelgar@us.ibm.com)
 * @author Ravi Trivedi (ravi_trivedi@hp.com)
 */
public class FindQualifier extends UDDIElement {
   public static final String UDDI_TAG = "findQualifier";

   /**
    * Valid values for a find qualifier
    */
   public static final String exactNameMatch = "exactNameMatch";
   public static final String caseSensitiveMatch = "caseSensitiveMatch";
   public static final String sortByNameAsc  = "sortByNameAsc";
   public static final String sortByNameDesc = "sortByNameDesc";
   public static final String sortByDateAsc  = "sortByDateAsc";
   public static final String sortByDateDesc = "sortByDateDesc";
   //V2 qualifiers
   public static final String orLikeKeys     = "orLikeKeys";
   public static final String orAllKeys      = "orAllKeys";
   public static final String combineCategoryBags  = "combineCategoryBags";
   public static final String serviceSubset  = "serviceSubset";
   public static final String andAllKeys     = "andAllKeys";

   // Removed in v2 errata 3
   // public static final String soundex        = "soundex";


   protected Element base = null;

   String text = null;

   /**
    * Default constructor.
    * Avoid using the default constructor for validation. It does not validate
    * required fields. Instead, use the required fields constructor to perform
    * validation.
    */
   public FindQualifier() {
   }

   /**
    * Construct the object with required fields.
    *
    * @param value  String value
    */
   public FindQualifier(String value) {
      setText(value);
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
   public FindQualifier(Element base) throws UDDIException {
      // Check if it is a fault. Throws an exception if it is.
      super(base);
      text = getText(base);
   }

   public void setText(String s) {
      text = s;
   }

   public String getText() {
      return text;
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
      if (text!=null) {
         base.appendChild(parent.getOwnerDocument().createTextNode(text));
      }
      parent.appendChild(base);
   }
}
