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
import org.uddi4j.datatype.assertion.*;
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
 * This message is used to manage all of the tracked relationship assertions
 * associated with an individual publisher account.The full set of assertions
 * associated with a publisher is effectively replaced whenever this message is used.
 *
 * When this message is processed, the UDDI registry examines the publisher assertions
 * for a  given publisher account. It examines the assertions that were active prior to
 * this API call and adds any new assertions that were not present to the assertions
 * of the publisher account. Consequently, new relationships may be activated
 * (e.g., status changed to complete), and existing relationships may be deactivated.
 *
 * <p>
 *
 * @author Ravi Trivedi (ravi_trivedi@hp.com)
 */
public class SetPublisherAssertions extends UDDIElement {

   public static final String UDDI_TAG = "set_publisherAssertions";
   protected Element base = null;

   AuthInfo authInfo = null;
   Vector pubAssertion  = new Vector();

   /**
    * Default constructor.
    * Avoid using the default constructor for validation. It does not validate
    * required fields. Instead, use the required fields constructor to perform
    * validation.
    */
   public SetPublisherAssertions() {
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
   public SetPublisherAssertions(Element base) throws UDDIException {
        // Check if it is a fault. Throws an exception if it is.
        super(base);
        NodeList nl = null;
        nl = getChildElementsByTagName(base, AuthInfo.UDDI_TAG);
        if (nl.getLength() > 0) {
             authInfo = new AuthInfo((Element)nl.item(0));
        }
        nl = getChildElementsByTagName(base, PublisherAssertion.UDDI_TAG);

        for (int i=0; i < nl.getLength(); i++) {
          pubAssertion.addElement(new PublisherAssertion((Element)nl.item(i)));
       }
   }

   /**
    * Construct the object with required fields.
    *
    * @param authInfo   String
    * @param pubAssertVector Vector
    */
   public SetPublisherAssertions(String authInfo, Vector pubAssertVector) {
      this.authInfo = new AuthInfo();
      this.authInfo.setText(authInfo);
      this.pubAssertion    = pubAssertVector;
   }

   public void setPublisherAssertionVector(Vector pubAssertVector ) {
      this.pubAssertion    = pubAssertVector;
   }

   public Vector  getPublisherAssertionVector() {
      return this.pubAssertion;
   }

   public AuthInfo getAuthInfo() {
      return this.authInfo;
   }

   public void setAuthInfo(AuthInfo s) {
      this.authInfo = s;
   }

   public String getAuthInfoString() {
      return this.authInfo.getText();
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
       if (pubAssertion!=null) {
         for (int i=0; i < pubAssertion.size(); i++) {
            ((PublisherAssertion)(pubAssertion.elementAt(i))).saveToXML(base);
         }
       }
       parent.appendChild(base);
   }
}
