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
import org.juddi.util.UUID;
import org.juddi.util.Auth;

import org.apache.log4j.Logger;
import org.uddi4j.UDDIElement;
import org.uddi4j.datatype.Name;
import org.uddi4j.datatype.business.BusinessEntity;
import org.uddi4j.request.GetAuthToken;
import org.uddi4j.request.SaveBusiness;
import org.uddi4j.response.BusinessDetail;
import org.uddi4j.util.AuthInfo;
import org.uddi4j.response.AuthToken;

import java.util.Vector;

/**
 * @author Steve Viens (steve@inflexionpoint.com)
 * @version 0.6.2
 */
public class SaveBusinessService extends PublishService
{
  // private reference to jUDDI Logger
  private static Logger log = Logger.getLogger(SaveBusinessService.class);

  // private reference to jUDDI DataStoreFactory
  private static DataStoreFactory factory = DataStoreFactory.getInstance();

  /**
   *
   */
  public UDDIElement invoke(UDDIElement element)
    throws JUDDIException
  {
    SaveBusiness request = (SaveBusiness)element;
    Vector businessVector = request.getBusinessEntityVector();
    AuthInfo authInfo = request.getAuthInfo();
    BusinessDetail detail = null;

    // 1. Check that an authToken was actually included in the
    //    request. If not then throw an AuthTokenRequiredException.
    // 2. Check that the authToken passed in is a valid one.
    //    If not then throw an AuthTokenRequiredException.
    // 3. Check that the authToken passed in has not yet.
    //    expired. If so then throw an AuthTokenExpiredException.
    Auth.validateAuthToken(authInfo);

    // lookup and validate authentication info (publish only)
    String authorizedName = Auth.getPublisherName(authInfo);
    String publisherID = Auth.getPublisherID(authInfo);

    // aquire a jUDDI datastore instance
    DataStore dataStore = factory.aquireDataStore();
    
    try
    {
      dataStore.beginTrans();
      verify(publisherID,businessVector,dataStore);
      detail = execute(publisherID,authorizedName,businessVector,dataStore);
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

    return detail;
  }

  /**
   *
   */
  private void verify(String publisherID,Vector businessVector,DataStore dataStore)
    throws JUDDIException
  {
    for (int i=0; i<businessVector.size(); i++)
    {
      // move the BusinessEntity into a form we can work with easily
      BusinessEntity business = (BusinessEntity)businessVector.elementAt(i);
      String businessKey = business.getBusinessKey();

      // If a BusinessKey was specified then make sure it's a valid one.
      if ((businessKey != null) && (businessKey.length() > 0) && (!dataStore.isValidBusinessKey(businessKey)))
        throw new InvalidKeyPassedException("BusinessKey: "+businessKey);

      // If a BusinessKey was specified then make sure 'publisherID' controls it.
      if ((businessKey != null) && (businessKey.length() > 0) && (!dataStore.isBusinessPublisher(businessKey,publisherID)))
        throw new UserMismatchException("BusinessKey: "+businessKey);
    }
  }

  /**
   *
   */
  private BusinessDetail execute(String publisherID,String authorizedName,Vector businessVector,DataStore dataStore)
    throws JUDDIException
  {
    for (int i=0; i<businessVector.size(); i++)
    {
      // move the BusinessEntity into a form we can work with easily
      BusinessEntity business = (BusinessEntity)businessVector.elementAt(i);
      String businessKey = business.getBusinessKey();

      // If the new BusinessEntity has a BusinessKey then it must already
      // exists so delete the old one. It a BusinessKey isn't specified then
      // this is a new BusinessEntity so create a new BusinessKey for it.
      if ((businessKey != null) && (businessKey.length() > 0))
        dataStore.deleteBusiness(businessKey);
      else
        business.setBusinessKey(UUID.nextID());

      // Everything checks out so let's save it. First store 'authorizedName'
      // and 'operator' values in each BusinessEntity.
      business.setAuthorizedName(authorizedName);
      business.setOperator(operator);
      dataStore.saveBusiness(business,publisherID);
    }

    BusinessDetail detail = new BusinessDetail();
    detail.setOperator(operator);
    detail.setTruncated(false);
    detail.setBusinessEntityVector(businessVector);
    return detail;
  }


  /***************************************************************************/
  /***************************** TEST DRIVER *********************************/
  /***************************************************************************/


  public static void main(String[] args)
    throws JUDDIException
  {
    // initialize all jUDDI Subsystems
    org.juddi.util.SysManager.startup();

    // generate an AuthToken
    GetAuthToken authTokenRequest = new GetAuthToken("sviens","password");
    GetAuthTokenService authTokenService = new GetAuthTokenService();
    AuthToken authToken = (AuthToken)authTokenService.invoke(authTokenRequest);
    String authInfo = authToken.getAuthInfoString();

    // generate a Name Vector
    Vector nameVector = new Vector();
    nameVector.add(new Name("Met Life Insurance"));
    nameVector.add(new Name("Fidelity Investments"));

    // generate a BusinessEntity
    BusinessEntity businessEntity = new BusinessEntity();
    businessEntity.setBusinessKey(null);
    businessEntity.setAuthorizedName("sviens");
    businessEntity.setOperator("SteveViens.com");
    businessEntity.setNameVector(nameVector);

    // generate a BusinessEntity Vector
    Vector businessEntityVector = new Vector();
    businessEntityVector.add(businessEntity);

    // create a request
    SaveBusiness request = new SaveBusiness();
    request.setAuthInfo(authInfo);
    request.setBusinessEntityVector(businessEntityVector);

    try
    {
      // invoke the service
      SaveBusinessService saveBizSvc = new SaveBusinessService();
      BusinessDetail response = (BusinessDetail)saveBizSvc.invoke(request);
      System.out.println(response.toString());
      // todo ... need more here!
    }
    finally
    {
      // terminate all jUDDI Subsystems
      org.juddi.util.SysManager.shutdown();
    }
  }
}

