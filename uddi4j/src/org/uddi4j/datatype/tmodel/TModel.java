/*
 * The source code contained herein is licensed under the IBM Public License
 * Version 1.0, which has been approved by the Open Source Initiative.
 * Copyright (C) 2001, International Business Machines Corporation
 * Copyright (C) 2001, Hewlett-Packard Company
 * All Rights Reserved.
 *
 */

package org.uddi4j.datatype.tmodel;

import java.util.Vector;
import org.w3c.dom.*;
import org.uddi4j.*;
import org.uddi4j.datatype.*;
import org.uddi4j.datatype.binding.*;
import org.uddi4j.datatype.business.*;
import org.uddi4j.datatype.service.*;
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
 * responses from, methods in the UDDIProxy class.<BR>
 * In addition, this class contains the tModel keys for the UDDI core tModels.
 * See <a href="http://www.uddi.org/taxonomies/Core_Taxonomy_OverviewDoc.htm">
 * http://www.uddi.org/taxonomies/Core_Taxonomy_OverviewDoc.htm</a> for a
 * complete list of these core tModels.
 * <p><b>Element description:</b><p>
 * This structure defines metadata about a technology, specification
 * or namespace qualified list (e.g., taxonomy, organizaton, etc.)
 *
 * <p>
 *
 * @author David Melgar (dmelgar@us.ibm.com)
 * @author Ravi Trivedi (ravi_trivedi@hp.com)
 * @author Vivek Chopra (vivek_chopra2@non.hp.com)
 */
public class TModel extends UDDIElement {
   public static final String UDDI_TAG = "tModel";


   //UDDI Core tModels - built-in taxonomies, identifier systems, and relationships

   /**
    * UDDI Type Taxonomy [uddi-org:types]<BR>
    * This taxonomy assists in general categorization of the tModels themselves.
    */
   public static final String TYPES_TMODEL_KEY = "UUID:C1ACF26D-9672-4404-9D70-39B756E62AB4";

   /**
    * Business Taxonomy: NAICS (1997 Release) [ntis-gov:naics:1997]<BR>
    * This tModel defines the NAICS (North American Industry Classification
    * System) 1997 Release industry taxonomy.
    *
    */
   public static final String NAICS_TMODEL_KEY = "UUID:C0B9FE13-179F-413D-8A5B-5004DB8E5BB2";

   /**
    * Product Taxonomy: UNSPSC (Version 3.1) [unspsc-org:unspsc:3-1]<BR>
    * This tModel defines the UNSPSC (United Nations Standard Products and
    * Services Code System) version 3.1 product taxonomy.<BR>
    * <B>This taxonomy has been superceeded by the Universal Standard Products
    * and Services Classification (see {@link #UNSPSC_73_TMODEL_KEY}) taxonomy.</B>
    */
   public static final String UNSPSC_TMODEL_KEY = "UUID:DB77450D-9FA8-45D4-A7BC-04411D14E384";

   /**
    * Product and Services Taxonomy:UNSPSC (Version 7.3) [unspsc-org:unspsc]<BR>
    * This tModel defines the UNSPSC (Universal Standard Products and Services
    * Classification) version 7.3 product and services taxonomy.<BR>
    */
   public static final String UNSPSC_73_TMODEL_KEY = "UUID:CD153257-086A-4237-B336-6BDCBDCC6634";

   /**
    * ISO 3166 Geographic Taxonomy [uddi-org:iso-ch:3166-1999]<BR>
    * This tModel defines the ISO 3166 geographic classification taxonomy.
    */
   public static final String ISO_CH_TMODEL_KEY = "UUID:4E49A8D6-D5A2-4FC2-93A0-0411D8D19E88";

   /**
    * UDDI Other Taxonomy [uddi-org:misc-taxomony]<BR>
    * This tModel defines an unidentified taxonomy.
    */
   public static final String GENERAL_KEYWORDS_TMODEL_KEY = "UUID:A035A07C-F362-44dd-8F95-E2B134BF43B4";

   /**
    * UDDI Owning Business [uddi-org:owningBusiness]<BR>
    * This tModel identifies the businessEntity that published or owns the
    * tagged information. Used with tModels to establish an 'owned'
    * relationship with a registered businessEntity.
    */
   public static final String OWNING_BUSINESS_TMODEL_KEY = "UUID:4064C064-6D14-4F35-8953-9652106476A9";

   /**
    * UDDI businessEntity relationship [uddi-org:relationships]<BR>
    * This tModel is used to describe business relationships. Used in the
    * publisher assertion messages.
    */
   public static final String RELATIONSHIPS_TMODEL_KEY = "UUID:807A2C6A-EE22-470D-ADC7-E0424A337C03";

   /**
    * UDDI Operators [uddi-org:operators]<BR>
    * This checked value set is used to identify UDDI operators.
    */
   public static final String OPERATORS_TMODEL_KEY = "UUID:327A56F0-3299-4461-BC23-5CD513E95C55";

   /**
    * D-U-N-S® Number Identifier System [dnb-com:D-U-N-S]<BR>
    * This tModel is used for the Dun & Bradstreet D-U-N-S® Number identifier.
    */
   public static final String D_U_N_S_TMODEL_KEY = "UUID:8609C81E-EE1F-4D5A-B202-3EB13AD01823";

   /**
    * Thomas Register Supplier Identifier Code System
    *     [thomasregister-com:supplierID]<BR>
    * This tModel is used for the Thomas Register supplier identifier codes.
    */
   public static final String THOMAS_REGISTER_TMODEL_KEY = "UUID:B1B1BAF5-2329-43E6-AE13-BA8E97195039";

   /**
    * UDDI IsReplacedBy [uddi-org:isReplacedBy]<BR>
    * An identifier system used to point (using UDDI keys) to the tModel
    * (or businessEntity) that is the logical replacement for the one in
    * which isReplacedBy is used.
    */
   public static final String IS_REPLACED_BY_TMODEL_KEY = "UUID:E59AE320-77A5-11D5-B898-0004AC49CC1E";

   /**
    * Email based web service [uddi-org:smtp]<BR>
    * This tModel is used to describe a web service that is invoked through
    * SMTP email transmissions. These transmissions may be between people or
    * applications.
    */
   public static final String SMTP_TMODEL_KEY = "UUID:93335D49-3EFB-48A0-ACEA-EA102B60DDC6";

   /**
    * Fax based web service [uddi-org:fax]<BR>
    * This tModel is used to describe a web service that is invoked through
    * fax transmissions. These transmissions may be between people or
    * applications.
    */
   public static final String FAX_TMODEL_KEY = "UUID:1A2B00BE-6E2C-42F5-875B-56F32686E0E7";

   /**
    * FTP based web service [uddi-org:ftp]<BR>
    * This tModel is used to describe a web service that is invoked through
    * file transfers via the ftp protocol.
    */
   public static final String FTP_TMODEL_KEY = "UUID:5FCF5CD0-629A-4C50-8B16-F94E9CF2A674";

   /**
    * Telephone based web service [uddi-org:telephone]<BR>
    * This tModel is used to describe a web service that is invoked through a
    * telephone call and interaction by voice and/or touch-tone.
    */
   public static final String TELEPHONE_TMODEL_KEY = "UUID:38E12427-5536-4260-A6F9-B5B530E63A07";

   /**
    * Web browser or HTTP based web service [uddi-org:http]<BR>
    * This tModel is used to describe a web service that is invoked through a
    * web browser and/or the HTTP protocol.
    */
   public static final String HTTP_TMODEL_KEY = "UUID:68DE9E80-AD09-469D-8A37-088422BFBC36";

   /**
    * HTTP Web Home Page URL [uddi-org:homepage]<BR>
    * This tModel is used as the bindingTemplate fingerprint for a web home
    * page reference.
    */
   public static final String HOMEPAGE_TMODEL_KEY = "UUID:4CEC1CEF-1F68-4B23-8CB7-8BAA763AEB89";

   protected Element base = null;

   String tModelKey = null;
   String operator = null;
   String authorizedName = null;
   Name name = null;
   OverviewDoc overviewDoc = null;
   IdentifierBag identifierBag = null;
   CategoryBag categoryBag = null;
   // Vector of Description objects
   Vector description = new Vector();

   /**
    * Default constructor.
    * Avoid using the default constructor for validation. It does not validate
    * required fields. Instead, use the required fields constructor to perform
    * validation.
    */
   public TModel() {
   }

   /**
    * Construct the object with required fields.
    *
    * @param tModelKey  String
    * @param name   String
    */
   public TModel(String tModelKey,
            String name) {
      this.tModelKey = tModelKey;
      this.name = new Name(name);
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
   public TModel(Element base) throws UDDIException {
      // Check if it is a fault. Throws an exception if it is.
      super(base);
      tModelKey = base.getAttribute("tModelKey");
      operator = base.getAttribute("operator");
      authorizedName = base.getAttribute("authorizedName");
      NodeList nl = null;
      nl = getChildElementsByTagName(base, Name.UDDI_TAG);
      if (nl.getLength() > 0) {
         name = new Name((Element)nl.item(0));
         // xml:lang  for Name  should not be present in TModel
         if( null != name )
                name.setLang(null);
      }
      nl = getChildElementsByTagName(base, OverviewDoc.UDDI_TAG);
      if (nl.getLength() > 0) {
         overviewDoc = new OverviewDoc((Element)nl.item(0));
      }
      nl = getChildElementsByTagName(base, IdentifierBag.UDDI_TAG);
      if (nl.getLength() > 0) {
         identifierBag = new IdentifierBag((Element)nl.item(0));
      }
      nl = getChildElementsByTagName(base, CategoryBag.UDDI_TAG);
      if (nl.getLength() > 0) {
         categoryBag = new CategoryBag((Element)nl.item(0));
      }
      nl = getChildElementsByTagName(base, Description.UDDI_TAG);
      for (int i=0; i < nl.getLength(); i++) {
         description.addElement(new Description((Element)nl.item(i)));
      }
   }

   public void setTModelKey(String s) {
      tModelKey = s;
   }

   public void setOperator(String s) {
      operator = s;
   }

   public void setAuthorizedName(String s) {
      authorizedName = s;
   }

   public void setName(Name s) {
      name = s;
   }
   public void setName(String s) {
      name = new Name();
      name.setText(s);
   }

   public void setOverviewDoc(OverviewDoc s) {
      overviewDoc = s;
   }

   public void setIdentifierBag(IdentifierBag s) {
      identifierBag = s;
   }

   public void setCategoryBag(CategoryBag s) {
      categoryBag = s;
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

   public String getTModelKey() {
      return tModelKey;
   }

   public String getOperator() {
      return operator;
   }

   public String getAuthorizedName() {
      return authorizedName;
   }

   public Name getName() {
      return name;
   }

   public String getNameString() {
      return name.getText();
   }

   public OverviewDoc getOverviewDoc() {
      return overviewDoc;
   }


   public IdentifierBag getIdentifierBag() {
      return identifierBag;
   }


   public CategoryBag getCategoryBag() {
      return categoryBag;
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
      if (tModelKey!=null) {
         base.setAttribute("tModelKey", tModelKey);
      }
      if (operator!=null) {
         base.setAttribute("operator", operator);
      }
      if (authorizedName!=null) {
         base.setAttribute("authorizedName", authorizedName);
      }
      if (name!=null) {
        // xml:lang  for Name  should not be present in TModel
         name.setLang(null);
         name.saveToXML(base);
      }
      if (description!=null) {
        for (int i=0; i < description.size(); i++) {
           ((Description)(description.elementAt(i))).saveToXML(base);
        }
      }
      if (overviewDoc!=null) {
         overviewDoc.saveToXML(base);
      }
      if (identifierBag!=null) {
         identifierBag.saveToXML(base);
      }
      if (categoryBag!=null) {
         categoryBag.saveToXML(base);
      }
      parent.appendChild(base);
   }
}
