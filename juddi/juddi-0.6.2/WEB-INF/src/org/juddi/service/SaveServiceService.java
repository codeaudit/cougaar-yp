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
import org.uddi4j.request.SaveService;
import org.uddi4j.datatype.service.BusinessService;
import org.uddi4j.response.ServiceDetail;
import org.uddi4j.util.AuthInfo;
import org.apache.log4j.Logger;

import java.util.Vector;

/**
 * @author Steve Viens (steve@inflexionpoint.com)
 * @version 0.6.2
 */
public class SaveServiceService extends PublishService
{
  // private reference to jUDDI Logger
  private static Logger log = Logger.getLogger(SaveServiceService.class);

  // private reference to jUDDI DataStoreFactory
  private static DataStoreFactory factory = DataStoreFactory.getInstance();

  /**
   *
   */
  public UDDIElement invoke(UDDIElement element)
    throws JUDDIException
  {
    SaveService request = (SaveService)element;
    Vector serviceVector = request.getBusinessServiceVector();
    AuthInfo authInfo = request.getAuthInfo();
    ServiceDetail detail = null;

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
      verify(publisherID,serviceVector,dataStore);
      detail = execute(publisherID,serviceVector,dataStore);
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
  private void verify(String publisherID,Vector serviceVector,DataStore dataStore)
    throws JUDDIException
  {
    for (int i=0; i<serviceVector.size(); i++)
    {
      // move the BusinessService data into a form we can work with easily
      BusinessService service = (BusinessService)serviceVector.elementAt(i);
      String businessKey = service.getBusinessKey();
      String serviceKey = service.getServiceKey();

      // If a BusinessKey wasn't included or it is an invalid BusinessKey then
      // throw an InvalidKeyPassedException
      if ((businessKey == null) || (businessKey.length() == 0) || (!dataStore.isValidBusinessKey(businessKey)))
        throw new InvalidKeyPassedException("BusinessKey: "+businessKey);

      // Confirm that 'publisherID' controls the BusinessEntity that this
      // BusinessService belongs to.  If not then throw a UserMismatchException.
      if (!dataStore.isBusinessPublisher(businessKey,publisherID))
        throw new UserMismatchException("BusinessKey: "+serviceKey);

      // If a ServiceKey was specified then make sure it's a valid one.
      if (((serviceKey != null) && (serviceKey.length() > 0)) && (!dataStore.isValidServiceKey(serviceKey)))
        throw new InvalidKeyPassedException("ServiceKey: "+serviceKey);
    }
  }

  /**
   *
   */
  private ServiceDetail execute(String publisherID,Vector serviceVector,DataStore dataStore)
    throws JUDDIException
  {
    for (int i=0; i<serviceVector.size(); i++)
    {
      // move the BusinessService data into a form we can work with easily
      BusinessService service = (BusinessService)serviceVector.elementAt(i);
      String serviceKey = service.getServiceKey();

      // If the new BusinessService has a ServiceKey then it must already
      // exists so delete the old one. It a ServiceKey isn't specified then
      // this is a new BusinessService so create a new ServiceKey for it.
      if ((serviceKey != null) && (serviceKey.length() > 0))
        dataStore.deleteService(serviceKey);
      else
        service.setServiceKey(UUID.nextID());

      // everything checks out so let's save it.
      dataStore.saveService(service);
    }

    // create a new ServiceDetail and stuff the
    // original but 'updated' serviceVector in.
    ServiceDetail detail = new ServiceDetail();
    detail.setOperator(operator);
    detail.setTruncated(false);
    detail.setBusinessServiceVector(serviceVector);
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
    SaveService request = new SaveService();
    // todo ... need more here!

    try
    {
      // invoke the service
      ServiceDetail response = (ServiceDetail)(new SaveServiceService().invoke(request));
      // todo ... need more here!
    }
    finally
    {
      // terminate all jUDDI Subsystems
      org.juddi.util.SysManager.shutdown();
    }
  }
}

