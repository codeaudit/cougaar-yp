/*
 * The source code contained herein is licensed under the IBM Public License
 * Version 1.0, which has been approved by the Open Source Initiative.
 * Copyright (C) 2001, International Business Machines Corporation
 * Copyright (C) 2001, Hewlett-Packard Company
 * All Rights Reserved.
 *
 */

import org.uddi4j.*;
import org.uddi4j.client.*;
import org.uddi4j.datatype.*;
import org.uddi4j.datatype.assertion.*;
import org.uddi4j.datatype.binding.*;
import org.uddi4j.datatype.business.*;
import org.uddi4j.datatype.service.*;
import org.uddi4j.datatype.tmodel.*;
import org.uddi4j.request.*;
import org.uddi4j.response.*;
import org.uddi4j.util.*;

import org.w3c.dom.Element;
import org.w3c.dom.*;
import javax.xml.parsers.*;
import java.util.Vector;
import java.util.Properties;
import java.io.*;

/**
 * Sample code that exercises the publish API. Attempts
 * to deletes a TModel.
 *
 * <OL>
 * <LI>Sets up an UDDIProxy object
 * <LI>Requests an authorization token
 * <LI>Finds a TModel.
 * <LI>Deletes a TModel.
 * </OL>
 *
 * @author Rajesh Sumra (rajesh_sumra@hp.com)
 * @author Vivek Chopra (vivek_chopra2@non.hp.com)
 */
public class DeleteTModelExample {

   Properties config = null;

   public static void main (String args[]) {
      DeleteTModelExample app = new DeleteTModelExample ();
      System.out.println("\n*********** Running DeleteTModelExample ***********");
      app.run();
      System.exit(0);
   }

   public void run() {
      // Load samples configuration
      config = Configurator.load();

      // Construct a UDDIProxy object
      UDDIProxy proxy = new UDDIProxy();

      try {
         // Select the desired UDDI server node
         proxy.setInquiryURL(config.getProperty("inquiryURL"));
         proxy.setPublishURL(config.getProperty("publishURL"));

         // Get an authorization token
         System.out.println("\nGet authtoken");

         // Pass in userid and password registered at the UDDI site
         AuthToken token = proxy.get_authToken(config.getProperty("userid"),
                                               config.getProperty("password"));

         System.out.println("Returned authToken: " + token.getAuthInfoString());

         // Setting FindQualifiers to 'exactNameMatch'
         FindQualifiers findQualifiers = new FindQualifiers();
         Vector qualifier = new Vector();
         qualifier.add(new FindQualifier("exactNameMatch"));
         findQualifiers.setFindQualifierVector(qualifier);

	 // **** Find the  TModel
	 // And setting the maximum rows to be returned as 5.
	 TModelList tModelList = proxy.find_tModel(config.getProperty("tmodelName"), null,null,findQualifiers,5);

	 Vector tModelInfoVector  = tModelList.getTModelInfos().getTModelInfoVector();

	 // Try to delete any tModel by this name. Multiple tModels
	 // with the same name may have been created with different
	 // UDDI userids. Deletes will fail for tModels not created
	 // by this UDDI userid.
	 for (int i = 0; i < tModelInfoVector.size(); i++) {
	    TModelInfo tModelInfo = (TModelInfo)tModelInfoVector.elementAt(i);

		// Print name for each business
		System.out.println("Found:\nThe TModel Name: " +
		                    tModelInfo.getNameString());
		System.out.println("The TModel Key : " + tModelInfo.getTModelKey());

		// Having found the tModel key, delete the tModel.

		DispositionReport dr = proxy.delete_tModel(token.getAuthInfoString(),
 							tModelInfo.getTModelKey());
		if (dr.success()) {
			System.out.println("TModel name: " + tModelInfo.getNameString());
			System.out.println("TModel Key  : " + tModelInfo.getTModelKey());
		 	System.out.println("TModel successfully deleted");
		} else {
			System.out.println("Errno:" + dr.getErrno() +
					"\n ErrCode:" + dr.getErrCode() +
					"\n ErrText:" + dr.getErrInfoText() );
		}


	 }


      // Handle possible errors
      } catch (UDDIException e) {
         DispositionReport dr = e.getDispositionReport();
         if (dr!=null) {
            System.out.println("UDDIException faultCode:" + e.getFaultCode() +
                               "\n operator:" + dr.getOperator() +
                               "\n generic:"  + dr.getGeneric() +
                               "\n errno:"    + dr.getErrno() +
                               "\n errCode:"  + dr.getErrCode() +
                               "\n errInfoText:" + dr.getErrInfoText());
         }
         e.printStackTrace();

         // Catch any other exception that may occur
      } catch (Exception e) {
         e.printStackTrace();
      }
   }
}
