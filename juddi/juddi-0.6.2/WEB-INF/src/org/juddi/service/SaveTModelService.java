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

import org.uddi4j.UDDIElement;
import org.uddi4j.request.SaveTModel;
import org.uddi4j.datatype.tmodel.TModel;
import org.uddi4j.response.TModelDetail;
import org.apache.log4j.Logger;
import org.uddi4j.util.AuthInfo;

import java.util.Vector;

/**
 * @author Steve Viens (steve@inflexionpoint.com)
 * @version 0.6.2
 */
public class SaveTModelService extends PublishService
{
  // private reference to jUDDI Logger
  private static Logger log = Logger.getLogger(SaveTModelService.class);

  // private reference to jUDDI DataStoreFactory
  private static DataStoreFactory factory = DataStoreFactory.getInstance();

  /**
   *
   */
  public UDDIElement invoke(UDDIElement element)
    throws JUDDIException
  {
    SaveTModel request = (SaveTModel)element;
    Vector tModelVector = request.getTModelVector();
    AuthInfo authInfo = request.getAuthInfo();
    TModelDetail detail = null;

    // 1. Check that an authToken was actually included in the
    //    request. If not then throw an AuthTokenRequiredException.
    // 2. Check that the authToken passed in is a valid one.
    //    If not then throw an AuthTokenRequiredException.
    // 3. Check that the authToken passed in has not yet.
    //    expired. If so then throw an AuthTokenExpiredException.
    Auth.validateAuthToken(authInfo);

    // lookup and validate authentication info (publish only)
    String publisherID = Auth.getPublisherID(authInfo);
    String authorizedName = Auth.getPublisherName(authInfo);

    // aquire a jUDDI datastore instance
    DataStore dataStore = factory.aquireDataStore();
    
    try
    {
      dataStore.beginTrans();
      verify(publisherID,tModelVector,dataStore);
      detail = execute(publisherID,authorizedName,tModelVector,dataStore);
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
  private void verify(String publisherID,Vector tModelVector,DataStore dataStore)
    throws JUDDIException
  {
    for (int i=0; i<tModelVector.size(); i++)
    {
      // move the TModel into a form we can work with easily
      TModel tModel = (TModel)tModelVector.elementAt(i);
      String tModelKey = tModel.getTModelKey();

      // If a TModelKey was specified then make sure it's a valid one.
      if (((tModelKey != null) && (tModelKey.length() > 0)) && (!dataStore.isValidTModelKey(tModelKey)))
        throw new InvalidKeyPassedException("TModelKey: "+tModelKey);

      // If a TModelKey was specified then make sure 'publisherID' controls it.
      if (((tModelKey != null) && (tModelKey.length() > 0)) && !dataStore.isTModelPublisher(tModelKey,publisherID))
        throw new UserMismatchException("TModelKey: "+tModelKey);
    }
  }

  /**
   *
   */
  private TModelDetail execute(String publisherID,String authorizedName,Vector tModelVector,DataStore dataStore)
    throws JUDDIException
  {
    for (int i=0; i<tModelVector.size(); i++)
    {
      // move the TModel into a form we can work with easily
      TModel tModel = (TModel)tModelVector.elementAt(i);
      String tModelKey = tModel.getTModelKey();

      // If the new TModel has a TModelKey then it must already exists
      // so delete the old one. It a TModelKey isn't specified then
      // this is a new TModel so create a new TModelKey for it.
      if ((tModelKey != null) && (tModelKey.length() > 0))
        dataStore.deleteTModel(tModelKey);
      else
        tModel.setTModelKey("uuid:"+UUID.nextID());

      // Everything checks out so let's save it. First store
      // 'authorizedName' and 'operator' values in each TModel.
      tModel.setAuthorizedName(publisherID);
      tModel.setOperator(operator);
      dataStore.saveTModel(tModel,publisherID);
    }

    TModelDetail detail = new TModelDetail();
    detail.setOperator(operator);
    detail.setTruncated(false);
    detail.setTModelVector(tModelVector);
    return detail;
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
    SaveTModel request = new SaveTModel();
    // todo ... need more here!

    try
    {
      // invoke the service
      TModelDetail response = (TModelDetail)(new SaveTModelService().invoke(request));
      // todo ... need more here!
    }
    finally
    {
      // terminate all jUDDI Subsystems
      org.juddi.util.SysManager.shutdown();
    }
  }
}

