/*
 * jUDDI - An open source Java implementation of UDDI v2.0
 * http://juddi.org/
 *
 * Copyright (c) 2002, InflexionPoint and contributors
 * All rights reserved.
 */

package org.juddi.service;

import org.juddi.datastore.*;
import org.juddi.error.*;
import org.juddi.util.Auth;

import org.uddi4j.UDDIElement;
import org.uddi4j.request.AddPublisherAssertions;
import org.uddi4j.request.GetAuthToken;
import org.uddi4j.request.SaveBusiness;
import org.uddi4j.response.DispositionReport;
import org.uddi4j.response.AuthToken;
import org.uddi4j.response.BusinessDetail;
import org.uddi4j.datatype.assertion.PublisherAssertion;
import org.uddi4j.datatype.tmodel.TModel;
import org.uddi4j.datatype.business.BusinessEntity;
import org.uddi4j.util.AuthInfo;
import org.uddi4j.util.KeyedReference;
import org.apache.log4j.Logger;

import java.util.Vector;

/**
 * @author Steve Viens (steve@inflexionpoint.com)
 * @version 0.6.2
 */
public class AddPublisherAssertionsService extends PublishService
{
  // private reference to jUDDI Logger
  private static Logger log = Logger.getLogger(AddPublisherAssertionsService.class);

  // private reference to jUDDI DataStoreFactory
  private static DataStoreFactory factory = DataStoreFactory.getInstance();

  /**
   *
   */
  public UDDIElement invoke(UDDIElement element)
    throws JUDDIException
  {
    AddPublisherAssertions request = (AddPublisherAssertions)element;
    Vector assertionsVector = request.getPublisherAssertionVector();
    AuthInfo authInfo = request.getAuthInfo();

    // 1. Check that an authToken was actually included in the
    //    request. If not then throw an AuthTokenRequiredException.
    // 2. Check that the authToken passed in is a valid one.
    //    If not then throw an AuthTokenRequiredException.
    // 3. Check that the authToken passed in has not yet.
    //    expired. If so then throw an AuthTokenExpiredException.
    Auth.validateAuthToken(authInfo);

    // get the ID of the publisher
    String publisherID = Auth.getPublisherID(authInfo);

    // aquire a jUDDI datastore instance
    DataStore dataStore = factory.aquireDataStore();

    try
    {
      dataStore.beginTrans();
      verify(publisherID,assertionsVector,dataStore);  // verify every aspect of the request
      execute(publisherID,assertionsVector,dataStore); // passed verification, let's execute
      dataStore.commit();
    }
    catch(Exception ex)
    {
      // we must rollback for *any* exception
      try { dataStore.rollback(); }
      catch(Exception e) { }

      // write to the log
      log.error(ex);

      // prep JUDDIException to throw
      if (ex instanceof JUDDIException)
        throw (JUDDIException)ex;
      else
        throw new JUDDIException(ex);
    }
    finally
    {
      factory.releaseDataStore(dataStore);
    }

    // didn't encounter an exception so let's return
    // the pre-constructed successful DispositionReport
    return this.success;
  }

  /**
   * Verify every aspect of the request before proceeding.
   */
  private void verify(String publisherID,Vector assertionVector,DataStore dataStore)
    throws JUDDIException
  {
    for (int i=0; i<assertionVector.size(); i++)
    {
      // transform each PublisherAssertion data into a form we can work with easily
      PublisherAssertion assertion = (PublisherAssertion)assertionVector.elementAt(i);

      // make sure we've got a 'fromKey'
      String fromKey = assertion.getFromKeyString();
      if ((fromKey == null) || (fromKey.length() == 0))
        throw new JUDDIException("attempt to save a PublisherAssertion without specifying a from key");

      // make sure we've got a 'toKey'
      String toKey = assertion.getToKeyString();
      if ((toKey == null) || (toKey.length() == 0))
        throw new JUDDIException("attempt to save a PublisherAssertion without specifying a to key");

      // make sure we've got a 'KeyedRefernce'
      KeyedReference keyedRef = assertion.getKeyedReference();
      if (keyedRef == null)
        throw new JUDDIException("attempt to insert a PublisherAssertion without specifying a TModel key");

      // make sure the 'KeyedRefernce' contains a 'TModelKey'
      String tModelKey = keyedRef.getTModelKey();
      if ((tModelKey == null) || (tModelKey.length() == 0))
        throw new JUDDIException("attempt to insert a PublisherAssertion without specifying a TModel key");

      // verify that the BusinessEntitys or tModel identified by the 'fromKey'
      // really exists. If not then throw an InvalidKeyPassedException.
      if ((!dataStore.isValidBusinessKey(fromKey)) && (!dataStore.isValidTModelKey(fromKey)))
        throw new InvalidKeyPassedException("fromKey: "+fromKey);

      // verify that the BusinessEntitys or tModel identified by the 'fromKey'
      // really exists. If not then throw an InvalidKeyPassedException.
      if ((!dataStore.isValidBusinessKey(toKey)) && (!dataStore.isValidTModelKey(toKey)))
        throw new InvalidKeyPassedException("toKey: "+toKey);

      // verify that the 'publisherID' controls at least one of the BusinessEntities
      // or TModels that are identified in this assertion. If not then throw a
      // UserMismatchException.
      if (((!dataStore.isBusinessPublisher(fromKey,publisherID)) && (!dataStore.isTModelPublisher(fromKey,publisherID))) ||
          ((!dataStore.isBusinessPublisher(toKey,publisherID)) && (!dataStore.isTModelPublisher(toKey,publisherID))))
        throw new UserMismatchException("fromKey: "+fromKey+" or toKey: "+toKey);
    }
  }

  /**
   * Save all PublisherAssertions
   */
  private void execute(String publisherID,Vector assertionVector,DataStore dataStore)
    throws JUDDIException
  {
    dataStore.saveAssertions(publisherID,assertionVector);
  }


  /***************************************************************************/
  /***************************** TEST DRIVER *********************************/
  /***************************************************************************/


  public static void main(String[] args)
    throws Exception
  {
    // initialize all jUDDI Subsystems
    org.juddi.util.SysManager.startup();

    // generate an AuthToken
    GetAuthToken authTokenRequest = new GetAuthToken("sviens","password");
    GetAuthTokenService authTokenService = new GetAuthTokenService();
    AuthToken authToken = (AuthToken)authTokenService.invoke(authTokenRequest);
    String authInfo = authToken.getAuthInfoString();

    // create a couple of BusinessEntities
    Vector businessVector = new Vector(2);
    businessVector.addElement(new BusinessEntity(null,"Blockbuster","en"));
    businessVector.addElement(new BusinessEntity(null,"PopSecret","en"));

    try
    {
      // create a SaveBusiness request & invoke the service
      SaveBusiness sbReq = new SaveBusiness();
      sbReq.setAuthInfo(authInfo);
      sbReq.setBusinessEntityVector(businessVector);
      BusinessDetail detail = (BusinessDetail)(new SaveBusinessService().invoke(sbReq));
      Vector detailVector = detail.getBusinessEntityVector();
      BusinessEntity b1 = (BusinessEntity)detailVector.elementAt(0);
      BusinessEntity b2 = (BusinessEntity)detailVector.elementAt(1);

      // create a new PublisherAssertion
      String fromKey = b1.getBusinessKey();
      String toKey = b2.getBusinessKey();
      KeyedReference keyedReference = new KeyedReference ("Partner Company","peer-peer");
      keyedReference.setTModelKey(TModel.RELATIONSHIPS_TMODEL_KEY);
      PublisherAssertion assertion = new PublisherAssertion(fromKey,toKey,keyedReference);

      // create a PublisherAssertion Vector
      Vector assertionVector = new Vector();
      assertionVector.addElement(assertion);

      // create an AddPublisherAssertions request & invoke the service
      AddPublisherAssertions apaReq = new AddPublisherAssertions();
      apaReq.setAuthInfo(authInfo);
      apaReq.setPublisherAssertionVector(assertionVector);
      DispositionReport dspRpt1 = (DispositionReport)(new AddPublisherAssertionsService().invoke(apaReq));
      System.out.println("errno: "+dspRpt1.toString());
      DispositionReport dspRpt2 = (DispositionReport)(new AddPublisherAssertionsService().invoke(apaReq));
      System.out.println("errno: "+dspRpt2.toString());
    }
    finally
    {
      // terminate all jUDDI Subsystems
      org.juddi.util.SysManager.shutdown();
    }
  }
}

