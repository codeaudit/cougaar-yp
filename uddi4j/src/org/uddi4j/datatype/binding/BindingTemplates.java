/*
 * The source code contained herein is licensed under the IBM Public License
 * Version 1.0, which has been approved by the Open Source Initiative.
 * Copyright (C) 2001, International Business Machines Corporation
 * All Rights Reserved.
 *
 */

package org.uddi4j.datatype.binding;

import java.util.Vector;
import org.w3c.dom.*;
import org.uddi4j.*;
import org.uddi4j.datatype.*;
import org.uddi4j.datatype.business.*;
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
 * responses from, methods in the UDDIProxy class.
 *
 * <p><b>Element description:</b><p>
 * Service element.  Collection accessor for bindingTemplate information.
 *
 * <p>
 *
 * @author David Melgar (dmelgar@us.ibm.com)
 * @author Vivek Chopra (vivek@soaprpc.com)
 */
public class BindingTemplates extends UDDIElement {
   public static final String UDDI_TAG = "bindingTemplates";

   protected Element base = null;

   // Vector of BindingTemplate objects
   Vector bindingTemplate = new Vector();

   /**
    * Default constructor.
    *
    */
   public BindingTemplates() {
   }

   /**
    * Construct the object from a DOM tree. Used by
    * UDDIProxy to construct an object from a received UDDI
    * message.
    *
    * @param base   Element with name appropriate for this class.
    *
    * @exception UDDIException Thrown if DOM tree contains a SOAP fault or
    *  disposition report indicating a UDDI error.
    */
   public BindingTemplates(Element base) throws UDDIException {
      // Checks for a fault. Throws exception if it is a fault.
      super(base);
      NodeList nl = null;
      nl = getChildElementsByTagName(base, BindingTemplate.UDDI_TAG);
      for (int i=0; i < nl.getLength(); i++) {
         bindingTemplate.addElement(new BindingTemplate((Element)nl.item(i)));
      }
   }

   /**
    * Set bindingTemplate vector.
    *
    * @param s  Vector of <I>BindingTemplate</I> objects.
    */
   public void setBindingTemplateVector(Vector s) {
      bindingTemplate = s;
   }

   /**
    * Get bindingTemplate
    *
    * @return s Vector of <I>BindingTemplate</I> objects.
    */
   public Vector getBindingTemplateVector() {
      return bindingTemplate;
   }

   /**
    * Add a BindingTemplate object to the collection
    * @param b BindingTemplate to be added
    */
   public void add (BindingTemplate b) {
      bindingTemplate.add (b);
   }

   /**
    * Remove a BindingTemplate object from the collection
    * @param b BindingTemplate to be removed
    * @return True if object was removed, false if it
    *         was not found in the collection.
    */
   public boolean remove (BindingTemplate b) {
      return bindingTemplate.remove (b);
   }

   /**
    * Retrieve the BindingTemplate at the specified index within the collection.
    * @param index Index to retrieve from.
    * @return BindingTemplate at that index
    */
   public BindingTemplate get (int index) {
      return (BindingTemplate) bindingTemplate.get (index);
   }

   /**
    * Return current size of the collection.
    * @return Number of BindingTemplates in the collection
    */
   public int size () {
      return bindingTemplate.size ();
   }

   /**
    * Save object to DOM tree. Used to serialize object
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
      if (bindingTemplate!=null) {
        for (int i=0; i < bindingTemplate.size(); i++) {
           ((BindingTemplate)(bindingTemplate.elementAt(i))).saveToXML(base);
        }
      }
      parent.appendChild(base);
   }
}
