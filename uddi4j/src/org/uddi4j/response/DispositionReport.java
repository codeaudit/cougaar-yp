/*
 * The source code contained herein is licensed under the IBM Public License
 * Version 1.0, which has been approved by the Open Source Initiative.
 * Copyright (C) 2001, International Business Machines Corporation
 * Copyright (C) 2001, Hewlett-Packard Company
 * All Rights Reserved.
 *
 */

package org.uddi4j.response;

import java.util.Vector;
import org.w3c.dom.Element;
import org.w3c.dom.NodeList;
import org.uddi4j.UDDIElement;
import org.uddi4j.UDDIException;


/**
 * Represents an UDDI defined error. This object is returned from certain
 * UDDI API calls. Other UDDI APIs return a data object if success and
 * generate a fault message if failure. The API returns the fault
 * by throwing a UDDIException.<P>
 *
 * UDDIException usually contains a Disposition report that has detailed
 * information about the UDDI error as defined by the UDDI specification.
 * If the response is a SOAP fault, but does not contains a disposition
 * report, a UDDIException may be thrown without containing a
 * DispositionReport.<P>
 *
 * This class contains defines for the various error values
 * that can be returned.<P>
 *
 * @author David Melgar (dmelgar@us.ibm.com)
 * @author Ravi Trivedi (ravi_trivedi@hp.com)
 */
public class DispositionReport extends UDDIElement {

   public static String UDDI_TAG = "dispositionReport";
   // XML attributes. Looks common to all message responses
   String genericAttrib  = null;
   String operatorAttrib = null;
   Vector errno          = new Vector();
   Vector errCode        = new Vector();
   Vector errInfoText    = new Vector();
   Vector keyType        = new Vector();

   /* Defines of possible error codes that can be returned */


   /**
    * Signifies that a particular publisher assertion (consisting of two businessKey values, and
    * a keyed reference with three components) cannot be identified in a save or delete operation.
    */
   public static final String E_assertionNotFound = "E_assertionNotFound";

   /**
    * Signifies that the authentication token value passed in the authInfo argument
    * is no longer valid because the token has expired.
    */
   public static final String E_authTokenExpired = "E_authTokenExpired";


   /**
    * Signifies that the authentication token value passed in the authInfo argument
    * is either missing or is not valid.
    */
   public static final String E_authTokenRequired = "E_authTokenRequired";

   /**
    * Signifies that user account limits have been exceeded.
    */
   public static final String E_accountLimitExceeded = "E_accountLimitExceeded";

   /**
    * Signifies that the request cannot be processed at the current time.
    */
   public static final String E_busy = "E_busy";

   /**
    * Restrictions have been placed by the taxonomy provider on the types of
    * information that should be included at that location within a specific
    * taxonomy.  The validation routine chosen by the Operator Site has
    * rejected this tModel for at least one specified category.
    */
   public static final String E_categorizationNotAllowed = "E_categorizationNotAllowed";

   /**
    * Signifies that a serious technical error has occurred while processing
    * the request.
    */
   public static final String E_fatalError= "E_fatalError";

   /**
    * Signifies that the request cannot be satisfied because one or more uuid_key
    * values specified is not a valid key value.  This will occur if a uuid_key
    * value is passed in a tModel that does not match with any known tModel key.
    */
   public static final String E_invalidKeyPassed = "E_invalidKeyPassed";

   /**
    * Signifies that an attempt was made to save a businessEntity containing a service projection
    * that does not match the businessService being projected. The serviceKey of at least one such
    * businessService will be included in the dispositionReport
    */
   public static final String E_invalidProjection = "E_invalidProjection";

   /**
    * Signifies that the given keyValue did not correspond to a category within
    * the taxonomy identified by a tModelKey value within one of the categoryBag elements provided.
    */
   public static final String E_invalidCategory = "E_invalidCategory";

   /**
    *  Signifies that one of assertion status values passed is unrecognized.  The completion status
    *  that caused the problem will be clearly indicated in the error text.
    */
   public static final String E_invalidCompletionStatus = "E_invalidCompletionStatus";

   /**
    * An error occurred with one of the uploadRegister URL values.
    */
   public static final String E_invalidURLPassed = "E_invalidURLPassed";

   /**
    * A value that was passed in a keyValue attribute did not pass validation.  This applies to
    * checked categorizations, identifiers and other validated code lists. The error text will
    * clearly indicate the key and value combination that failed validation.
    */
   public static final String E_invalidValue = "E_invalidValue";

   /**
    * Signifies that the request cannot be satisfied because one or more uuid_key
    * values specified has previously been hidden or removed by the requester.
    * This specifically applies to the tModelKey values passed.
    */
   public static final String E_keyRetired = "E_keyRetired";

   /**
    * Signifies that an error was detected while processing elements that were annotated with
    * xml:lang qualifiers. Presently, only the description and name elements support xml:lang
    * qualifications.
    */
   public static final String E_languageError = "E_languageError";

   /**
    * Signifies that the message it too large.  The upper limit will be clearly indicated in the
    * error text.
    */
   public static final String E_messageTooLarge = "E_messageTooLarge";

   /**
    * Signifies that the partial name value passed exceeds the maximum name length designated by
    * the policy of an implementation or Operator Site.
    */
   public static final String E_nameTooLong = "E_nameTooLong";

   /**
    * Signifies that one or more of the uuid_key values passed refers to data
    * that is not controlled by the Operator Site that received the request for processing.
    */
   public static final String E_operatorMismatch = "E_operatorMismatch";

   /**
    * Signifies that the target publisher cancelled the custody transfer operation.
    */
   public static final String E_publisherCancelled = "E_publisherCancelled";

   /**
    * Signifies that a custody transfer request has been refused.
    */
   public static final String E_requestDenied = "E_requestDenied";

   /**
    * Signifies that the request could not be carried out because a needed validate_values service did not
    * respond in a reasonable amount of time.
    */
   public static final String E_requestTimeout = "E_requestTimeout";

   /**
    * Signifies that the target publisher was unable to match the shared secret and the five (5)
    * attempt limit was exhausted. The target operator automatically cancelled the transfer
    * operation.
    */
   public static final String E_secretUnknown = "E_secretUnknown";

   /**
    * Signifies no failure occurred. This return code is used with the dispositionReport for
    * reporting results from requests with no natural response document.
    */
   public static final String E_success = "E_success";

   /**
    * Signifies that too many or incompatible arguments were passed. The error text will clearly
    * indicate the nature of the problem.
    */
   public static final String E_tooManyOptions = "E_tooManyOptions";

   /**
    * Signifies that a custody transfer request will not succeed.
    */
   public static final String E_transferAborted = "E_transferAborted";

   /**
    * Signifies that the value of the generic attribute passed is unsupported by the Operator
    * Instance being queried.
    */
   public static final String E_unrecognizedVersion = "E_unrecognizedVersion";

   /**
    * Signifies that the user ID and password pair passed in a get_authToken message is not known
    * to the Operator Site or is not valid.
    */
   public static final String E_unknownUser = "E_unknownUser";

   /**
    * Signifies that the implementer does not support a feature or API.
    */
   public static final String E_unsupported = "E_unsupported";

   /**
    * Signifies that an attempt was made to reference a taxonomy or identifier system in a
    * keyedReference whose tModel is categorized with the unvalidatable categorization.
    */
   public static final String E_unvalidatable = "E_unvalidatable";

   /**
    * Signifies that one or more of the uuid_key values passed refers to data
    * that is not controlled by the individual who is represented by the authentication token.
    */
   public static final String E_userMismatch = "E_userMismatch";

   /**
    * Signifies that a value did not pass validation because of contextual issues. The value may
    * be valid in some contexts, but not in the context used. The error text may contain information
    * about the contextual problem.
    */
   public static final String E_valueNotAllowed = "E_valueNotAllowed";


   /** Default constructor */
   public DispositionReport() {
   }

   /**
    * Constructer that parses the XML dom tree and extracts
    * useful attributes.
    *
    * @param el     Root element of the tree within the SOAP body.
    */
   public DispositionReport(Element el) throws UDDIException {
       boolean fault = false ;
       UDDIException exception = null;

      if (UDDIException.isValidElement(el)) {
          //If it is a Fault, then there most likely is a Disposition report.
          //If it is not present then the UDDIException constructor will
          //handle it.
         fault = true;
         exception = new UDDIException(el, true);
         NodeList nl = exception.getDetailElement().getElementsByTagName(UDDI_TAG);
         if (nl.getLength()>0) {
            base = (Element)nl.item(0);
         } else {
            // Not a disposition report, some other error, throw it
            throw exception;
         }
      } else {
         base = el;
      }

      if (isValidElement(base)) {
         // Extract useful attributes
         NodeList nl;
         Element tmp;
         // Extract attribute values
         genericAttrib = el.getAttribute("generic");
         operatorAttrib = el.getAttribute("operator");

         // Process embedded elements
         nl = el.getElementsByTagName("result");
         for (int i = 0; i<nl.getLength(); i++) {
            tmp = (Element)nl.item(i);

            String errnoString = tmp.getAttribute("errno");
            Integer errnoInt = new Integer(errnoString);
            errno.add(errnoInt);

            nl = tmp.getElementsByTagName("errInfo");
            if (nl.getLength()>i) {
               tmp = (Element)nl.item(i);
               errCode.add(tmp.getAttribute("errCode"));
               keyType.add(tmp.getAttribute("keyType"));
               errInfoText.add(getText(tmp));
            }
         }
      }
      if(fault) {
          throw exception;
      }
   }

   // Getters
   public String getGeneric() {
      return this.genericAttrib;
   }

   public String getOperator() {
      return this.operatorAttrib;
   }

   public boolean success() {
      // Did all results return successful
      boolean success = true;
      for (int i = 0; i < errno.size(); i++) {
         int errnoInt = ((Integer)errno.elementAt(i)).intValue();
         success = success && (errnoInt == 0);
      }
      return success;
   }

   public int getErrno() {
      return getErrno(0);
   }

   public int getErrno(int index) {
      return (int)((Integer)errno.elementAt(index)).intValue();
   }

   public String getErrCode() {
      return getErrCode(0);
   }

   public String getErrCode(int index) {
      return (String)errCode.elementAt(index);
   }

   public String getErrInfoText() {
      return getErrInfoText(0);
   }

   public String getErrInfoText(int index) {
      return (String)(errInfoText.elementAt(index));
   }

   public String getKeyType() {
      return getKeyType(0);
   }

   public String getKeyType(int index) {
      return (String)keyType.elementAt(index);
   }

   public int getNumResults() {
      return errno.size();
   }

   // Setters

   public void setGeneric(String gen) {
      genericAttrib = gen;
   }

   public void setOperator(String oper) {
      operatorAttrib = oper;
   }

   public void setErrno(int errno) {
      setErrno(0, errno);
   }

   public void setErrno(int errno, int index) {
      if (index > this.errno.size()) {
         this.errno.setSize(index);
      }
      this.errno.setElementAt( new Integer(errno),index);
   }

   public void setErrCode(String errCode) {
      setErrCode(errCode, 0);
   }

   public void setErrCode(String errCode, int index) {
      if (index > this.errCode.size()) {
         this.errCode.setSize(index);
      }
      this.errCode.setElementAt(errCode,index);
   }

   public void setErrInfoText(String errInfoText) {
      setErrInfoText(errInfoText, 0);
   }

   public void setErrInfoText(String errInfoText, int index) {
      if (index > this.errInfoText.size()) {
         this.errInfoText.setSize(index);
      }
      this.errInfoText.setElementAt(errInfoText,index);
   }

   public void setKeyType(String keyType) {
      setKeyType(keyType, 0);
   }

   public void setKeyType(String keyType, int index) {
      if (index > this.keyType.size()) {
         this.keyType.setSize(index);
      }
      this.keyType.setElementAt(keyType,index);
   }


   /**
    * Tests the passed in element to determine if the
    * element is a serialized version of this object.
    *
    * @param el     Root element for this object
    */
   public boolean isValidElement(Element el) {
      return el.getNodeName().equals(UDDI_TAG);
   }

   public void saveToXML(Element parent) {
      base = parent.getOwnerDocument().createElement(UDDI_TAG);
      // Save attributes
      if (genericAttrib!=null) {
         base.setAttribute("generic", genericAttrib);
      }
      base.setAttribute("xmlns", UDDIElement.XMLNS);
      if (operatorAttrib!=null) {
         base.setAttribute("operator", operatorAttrib);
      }
      for (int i = 0; i < errno.size(); i++) {
         Element result = parent.getOwnerDocument().createElement("result");
         result.setAttribute( "errno", ((Integer)errno.elementAt(i)).toString());

         Element errInfo = parent.getOwnerDocument().createElement("errInfo");
         errInfo.setAttribute("errCode", (String)errCode.elementAt(i));
         if ((keyType.elementAt(i)!=null) && (!"".equals((String)keyType.elementAt(i)))) {
            // Keytype is optional and has restricted values.
            errInfo.setAttribute("keyType", (String)keyType.elementAt(i));
         }
         errInfo.setNodeValue((String)errInfoText.elementAt(i));

         result.appendChild( errInfo );
         base.appendChild(result);
      }
      parent.appendChild(base);
   }
}


