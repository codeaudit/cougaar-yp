/*
 * jUDDI - An open source Java implementation of UDDI v2.0
 * http://juddi.org/
 *
 * Copyright (c) 2002, InflexionPoint and contributors
 * All rights reserved.
 */

package org.juddi.service;

import org.juddi.datastore.*;
import org.juddi.error.JUDDIException;
import org.juddi.util.Auth;

import org.uddi4j.UDDIElement;
import org.uddi4j.request.GetRegisteredInfo;
import org.uddi4j.response.RegisteredInfo;
import org.uddi4j.util.AuthInfo;
import org.uddi4j.response.BusinessInfos;
import org.uddi4j.response.TModelInfos;
import org.apache.log4j.Logger;

import java.util.Vector;

/**
 * @author Steve Viens (steve@inflexionpoint.com)
 * @version 0.6.2
 */
public class GetRegisteredInfoService extends PublishService
{
  // private reference to jUDDI Logger
  private static Logger log = Logger.getLogger(GetRegisteredInfoService.class);

  // private reference to jUDDI DataStoreFactory
  private static DataStoreFactory factory = DataStoreFactory.getInstance();

  /**
   *
   */
  public UDDIElement invoke(UDDIElement element)
    throws JUDDIException
  {
    GetRegisteredInfo request = (GetRegisteredInfo)element;
    AuthInfo authInfo = request.getAuthInfo();
    RegisteredInfo info = null;

    // 1. Check that an authToken was actually included in the
    //    request. If not then throw an AuthTokenRequiredException.
    // 2. Check that the authToken passed in is a valid one.
    //    If not then throw an AuthTokenRequiredException.
    // 3. Check that the authToken passed in has not yet.
    //    expired. If so then throw an AuthTokenExpiredException.
    Auth.validateAuthToken(authInfo);

    // lookup and validate authentication info (publish only)
    String publisherID = Auth.getPublisherID(authInfo);

    // aquire a jUDDI datastore instance
    DataStore dataStore = factory.aquireDataStore();

    try
    {
      dataStore.beginTrans();
      verify(publisherID,dataStore);
      info = execute(publisherID,dataStore);
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

    return info;
  }

  /**
   *
   */
  private void verify(String publisherID,DataStore dataStore)
  {
    return;
  }

  /**
   *
   */
  private RegisteredInfo execute(String publisherID,DataStore dataStore)
    throws JUDDIException
  {
    BusinessInfos businessInfos = new BusinessInfos();
    TModelInfos tModelInfos = new TModelInfos();
    Vector keyVector = null;
    Vector infoVector = null;

    // perform the search for BusinessEntities registered to publisherID
    keyVector = dataStore.findRegisteredBusinesses(publisherID);
    if ((keyVector != null) && (keyVector.size() > 0))
    {
      int rowCount = keyVector.size();

      // iterate through the business entity keys fetching each associated BusinessInfo.
      infoVector = new Vector(rowCount);
      for (int i=0; i<rowCount; i++)
        infoVector.addElement(dataStore.fetchBusinessInfo((String)keyVector.elementAt(i)));

      businessInfos.setBusinessInfoVector(infoVector);
    }

    // perform the search for TModels registered to publisherID
    keyVector = dataStore.findRegisteredTModels(publisherID);
    if ((keyVector != null) && (keyVector.size() > 0))
    {
      int rowCount = keyVector.size();

      // iterate through the tModel keys fetching each associated TModelInfo.
      infoVector = new Vector(rowCount);
      for (int i=0; i<rowCount; i++)
        infoVector.addElement(dataStore.fetchTModelInfo((String)keyVector.elementAt(i)));

      tModelInfos.setTModelInfoVector(infoVector);
    }

    // create a new BusinessInfos instance and stuff
    // the new Vector of BusinessInfos into it.
    RegisteredInfo info = new RegisteredInfo();
    info.setOperator(operator);
    info.setBusinessInfos(businessInfos);
    info.setTModelInfos(tModelInfos);
    return info;
  }


  /***************************************************************************/
  /***************************** TEST DRIVER *********************************/
  /***************************************************************************/


  public static void main(String[] args)
    throws Exception
  {
    // initialize all jUDDI Subsystems
    org.juddi.util.SysManager.startup();

    // create a request
    GetRegisteredInfo request = new GetRegisteredInfo();
    // todo ... need more here!

    try
    {
      // invoke the service
      RegisteredInfo response = (RegisteredInfo)(new GetRegisteredInfoService().invoke(request));
      // todo ... need more here!
    }
    finally
    {
      // terminate all jUDDI Subsystems
      org.juddi.util.SysManager.shutdown();
    }
  }
}