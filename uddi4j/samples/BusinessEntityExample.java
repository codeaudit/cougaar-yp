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
 * to save a businessEntity , then finds the saved businessEntity and
 * then deletes the saved businessEntity.
 *
 * <OL>
 * <LI>Sets up an UDDIProxy object
 * <LI>Requests an authorization token
 * <LI>Saves a businessEntity
 * <LI>Finds businessEntity saved in first step.
 * <LI>Deletes the businessEntity saved in first step.
 * </OL>
 *
 * @author David Melgar (dmelgar@us.ibm.com)
 * @author Rajesh Sumra (rajesh_sumra@hp.com)
 */
public class BusinessEntityExample {

    Properties config = null;

    public static void main (String args[]) {
        BusinessEntityExample app = new BusinessEntityExample();
        System.out.println("\n*********** Running BusinessEntityExample ***********");
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

            System.out.println("Returned authToken:" + token.getAuthInfoString());

            System.out.println("\nSave '" + config.getProperty("businessName") + "'");

            // Create minimum required data objects
            Vector entities = new Vector();

            // Create a new business entity using required elements constructor
            // Name is the business name. BusinessKey must be "" to save a new
            // business
            BusinessEntity be = new BusinessEntity("", config.getProperty("businessName"));
            entities.addElement(be);

            // **** First Save a  business
            BusinessDetail bd = proxy.save_business(token.getAuthInfoString(),entities);

            // Process returned BusinessDetail object
            Vector businessEntities = bd.getBusinessEntityVector();
            BusinessEntity returnedBusinessEntity = (BusinessEntity)(businessEntities.elementAt(0));
			System.out.println("Business Name : " + returnedBusinessEntity.getDefaultNameString());
			System.out.println("Business Key  : " + returnedBusinessEntity.getBusinessKey());

            System.out.println("\nFinding business saved");

            //creating vector of Name Object
            Vector names = new Vector();
            names.add(new Name(config.getProperty("businessName")));

            // Setting FindQualifiers to 'exactNameMatch'
            FindQualifiers findQualifiers = new FindQualifiers();
            Vector qualifier = new Vector();
            qualifier.add(new FindQualifier("exactNameMatch"));
            findQualifiers.setFindQualifierVector(qualifier);

            // **** Find business saved.
            // And setting the maximum rows to be returned as 5.
            BusinessList businessList = proxy.find_business(names, null, null, null,null,findQualifiers,5);
            Vector businessInfoVector  = businessList.getBusinessInfos().getBusinessInfoVector();
			for (int i = 0; i < businessInfoVector.size(); i++) {
				BusinessInfo bi = (BusinessInfo)businessInfoVector.elementAt(i);
				System.out.println("Business Name : " + bi.getNameString());
				System.out.println("Business Key  : " + bi.getBusinessKey());
			}

			System.out.println("\nDeleting the saved Business");
         	// Try to delete business saved in first step.
			// Delete will fail for businesses not created by this id

			// **** Having the business key, delete Business Entity using the authToken
			DispositionReport dr = proxy.delete_business (
										token.getAuthInfoString(),
										 returnedBusinessEntity.
										 getBusinessKey());

			if (dr.success()) {
			   System.out.println("Business successfully deleted");
			} else {
			   System.out.println("Errno:" + dr.getErrno() +
								  "\n ErrCode:" + dr.getErrCode() +
								  "\n ErrText:" + dr.getErrInfoText() );
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
