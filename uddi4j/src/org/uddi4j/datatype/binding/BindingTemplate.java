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
 *   <li>Constructor that will instantiate the object from an XML DOM element
 *       that is the appropriate element for this object.
 *   <li>Get/set methods for each attribute that this element can contain.
 *   <li>For sets of attributes, a get/setVector method is provided.
 *   <li>SaveToXML method. Serialized this class within a passed in element.
 *
 * </ul>
 * Typically, this class is used to construct parameters for, or interpret
 * responses from methods in the UDDIProxy class.
 *
 * <p><b>Element description:</b><p>
 * Primary Data type: Describes an instance of a web service in technical terms.
 *
 * <p>
 *
 * @author David Melgar (dmelgar@us.ibm.com)
 */
public class BindingTemplate extends UDDIElement {
   public static final String UDDI_TAG = "bindingTemplate";

   protected Element base = null;

   String bindingKey = null;
   String serviceKey = null;
   AccessPoint accessPoint = null;
   HostingRedirector hostingRedirector = null;
   TModelInstanceDetails tModelInstanceDetails = null;
   // Vector of Description objects
   Vector description = new Vector();

   /**
    * Default constructor.
    * Use of this constructor should be avoided. Use the required fields
    * constructor to provide validation. No validation of required
    * fields is performed when using the default constructor.
    *
    */
   public BindingTemplate() {
   }

   /**
    * Construct the object with required fields.
    *
    * @param bindingKey String
    * @param TModelInstanceDetails  TModelInstanceDetails object
    */
   public BindingTemplate(String bindingKey,
      TModelInstanceDetails tModelInstanceDetails) {
      this.bindingKey = bindingKey;
      this.tModelInstanceDetails = tModelInstanceDetails;
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
   public BindingTemplate(Element base) throws UDDIException {
      // Checks if it is a fault. Throw exception if it is a fault.
      super(base);
      bindingKey = base.getAttribute("bindingKey");
      serviceKey = base.getAttribute("serviceKey");
      NodeList nl = null;
      nl = getChildElementsByTagName(base, AccessPoint.UDDI_TAG);
      if (nl.getLength() > 0) {
         accessPoint = new AccessPoint((Element)nl.item(0));
      }
      nl = getChildElementsByTagName(base, HostingRedirector.UDDI_TAG);
      if (nl.getLength() > 0) {
         hostingRedirector = new HostingRedirector((Element)nl.item(0));
      }
      nl = getChildElementsByTagName(base, TModelInstanceDetails.UDDI_TAG);
      if (nl.getLength() > 0) {
         tModelInstanceDetails = new TModelInstanceDetails((Element)nl.item(0));
      }
      nl = getChildElementsByTagName(base, Description.UDDI_TAG);
      for (int i=0; i < nl.getLength(); i++) {
         description.addElement(new Description((Element)nl.item(i)));
      }
   }

   public void setBindingKey(String s) {
      bindingKey = s;
   }

   public void setServiceKey(String s) {
      serviceKey = s;
   }

   public void setAccessPoint(AccessPoint s) {
      accessPoint = s;
   }

   public void setHostingRedirector(HostingRedirector s) {
      hostingRedirector = s;
   }

   public void setTModelInstanceDetails(TModelInstanceDetails s) {
      tModelInstanceDetails = s;
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

   public String getBindingKey() {
      return bindingKey;
   }


   public String getServiceKey() {
      return serviceKey;
   }


   public AccessPoint getAccessPoint() {
      return accessPoint;
   }


   public HostingRedirector getHostingRedirector() {
      return hostingRedirector;
   }


   public TModelInstanceDetails getTModelInstanceDetails() {
      return tModelInstanceDetails;
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
      if (bindingKey!=null) {
         base.setAttribute("bindingKey", bindingKey);
      }
      if (serviceKey!=null) {
         base.setAttribute("serviceKey", serviceKey);
      }
      if (description!=null) {
        for (int i=0; i < description.size(); i++) {
           ((Description)(description.elementAt(i))).saveToXML(base);
        }
      }
      if (accessPoint!=null) {
         accessPoint.saveToXML(base);
      }
      if (hostingRedirector!=null) {
         hostingRedirector.saveToXML(base);
      }
      if (tModelInstanceDetails!=null) {
         tModelInstanceDetails.saveToXML(base);
      }
      parent.appendChild(base);
   }
}
