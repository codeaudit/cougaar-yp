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
 * Support element used to contain optional information about the way an
 * instance of a web service is implemented, or varies from the general
 * specifications outlined in a specific tModel.
 *
 * <p>
 *
 * @author David Melgar (dmelgar@us.ibm.com)
 */
public class InstanceDetails extends UDDIElement {
   public static final String UDDI_TAG = "instanceDetails";

   protected Element base = null;

   OverviewDoc overviewDoc = null;
   InstanceParms instanceParms = null;
   // Vector of Description objects
   Vector description = new Vector();

   /**
    * Default constructor.
    * Avoid using the default constructor for validation. It does not validate
    * required fields. Instead, use the required fields constructor to perform
    * validation.
    */
   public InstanceDetails() {
   }

   /**
    * Construct the object from a DOM tree. Used by
    * UDDIProxy to construct object from received UDDI
    * message.
    *
    * @param base   Element with name appropriate for this class.
    * @exception UDDIException
    *                   Thrown if DOM tree contains a SOAP fault or
    *                   disposition report indicating a UDDI error.
    */
   public InstanceDetails(Element base) throws UDDIException {
      // Checks if it is a fault. Throw exception if it is a fault.
      super(base);
      NodeList nl = null;
      nl = getChildElementsByTagName(base, OverviewDoc.UDDI_TAG);
      if (nl.getLength() > 0) {
         overviewDoc = new OverviewDoc((Element)nl.item(0));
      }
      nl = getChildElementsByTagName(base, InstanceParms.UDDI_TAG);
      if (nl.getLength() > 0) {
         instanceParms = new InstanceParms((Element)nl.item(0));
      }
      nl = getChildElementsByTagName(base, Description.UDDI_TAG);
      for (int i=0; i < nl.getLength(); i++) {
         description.addElement(new Description((Element)nl.item(i)));
      }
   }

   public void setOverviewDoc(OverviewDoc s) {
      overviewDoc = s;
   }

   public void setInstanceParms(InstanceParms s) {
      instanceParms = s;
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

   public OverviewDoc getOverviewDoc() {
      return overviewDoc;
   }


   public InstanceParms getInstanceParms() {
      return instanceParms;
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
      if (description!=null) {
        for (int i=0; i < description.size(); i++) {
           ((Description)(description.elementAt(i))).saveToXML(base);
        }
      }
      if (overviewDoc!=null) {
         overviewDoc.saveToXML(base);
      }
      if (instanceParms!=null) {
         instanceParms.saveToXML(base);
      }
      parent.appendChild(base);
   }
}
