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
import org.uddi4j.datatype.assertion.*;
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
 * This response message contains a set of one or more PublisherAssertion
 * structures. It returns all publisherAssertion structures that were authenticated
 * in the preceding set_publisherAssertions or get_publisherAssertions messages.
 *
 * <p>
 *
 * @author Mahesh C S (csmahesh@india.hp.com)
 */
public class PublisherAssertions extends UDDIElement {
   public static final String UDDI_TAG = "publisherAssertions";

   protected Element base = null;

   String operator = null;
   String authorizedName = null;
   // Vector of PublisherAssertion objects
   Vector publisherAssertion = new Vector();


   /**
    * Default constructor.
    * Avoid using the default constructor for validation. It does not validate
    * required fields. Instead, use the required fields constructor to perform
    * validation.
    */

   public PublisherAssertions() {
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

   public PublisherAssertions(Element base) throws UDDIException {
       // Check if it is a fault. Throws an exception if it is.
       super(base);
       operator = base.getAttribute("operator");
       authorizedName = base.getAttribute("authorizedName");
       NodeList nl = null;
       nl = getChildElementsByTagName(base, PublisherAssertion.UDDI_TAG);
       for (int i=0; i < nl.getLength(); i++) {
           publisherAssertion.addElement(new PublisherAssertion((Element)nl.item(i)));
       }
   }

   /**
    * Construct the object with required fields.
    *
    * @param operator   String
    * @param name       String
    * @param publishAssertion    Vector of publisherAssertion objects.
    */
   public PublisherAssertions(String operator, String name,
                              Vector publishAssertion) throws UDDIException {
       this.operator = operator;
       this.authorizedName = name;
       this.publisherAssertion = publishAssertion;
   }

   public String getAuthorizedName() {
       return authorizedName;
   }

   public void setAuthorizedName(String name) {
       authorizedName = name;
   }

   public String getOperator() {
       return operator;
   }

   public void setOperator(String s) {
       operator = s;
   }

   /**
    * Set PublisherAssertion vector
    *
    * @param s Vector of <I>PublisherAssertion</I> objects.
    */
   public void setPublisherAssertionVector(Vector s) {
       publisherAssertion = s;
   }

   /**
    * Get PublisherAssertion vector
    *
    * @return Vector of <I>PublisherAssertion</I> objects.
    */
   public Vector getPublisherAssertionVector() {
       return publisherAssertion;
   }

   /**
    * Add a PublisherAssertion object to the collection
    * @param p PublisherAssertion to be added
    */
   public void add (PublisherAssertion p) {
      publisherAssertion.add (p);
   }

   /**
    * Remove a PublisherAssertion object from the collection
    * @param p PublisherAssertion to be removed
    * @return True if object was removed, false if it
    *         was not found in the collection.
    */
   public boolean remove (PublisherAssertion p) {
      return publisherAssertion.remove (p);
   }

   /**
    * Retrieve the PublisherAssertion at the specified index within the collection.
    * @param index Index to retrieve from.
    * @return PublisherAssertion at that index
    */
   public PublisherAssertion get (int index) {
      return (PublisherAssertion) publisherAssertion.get (index);
   }

   /**
    * Return current size of the collection.
    * @return Number of PublisherAssertions in the collection
    */
   public int size () {
      return publisherAssertion.size ();
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
       base.setAttribute("generic", UDDIElement.GENERIC);
       base.setAttribute("xmlns", UDDIElement.XMLNS);
       if (operator!=null) {
          base.setAttribute("operator", operator);
       }
       if (authorizedName!=null) {
          base.setAttribute("authorizedName", authorizedName);
       }
       if (publisherAssertion!=null) {
          for (int i=0; i < publisherAssertion.size(); i++) {
             ((PublisherAssertion)(publisherAssertion.elementAt(i))).saveToXML(base);
		  }
       }
       parent.appendChild(base);
   }
}
