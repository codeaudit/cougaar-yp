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
import org.uddi4j.request.SaveBinding;
import org.uddi4j.response.BindingDetail;
import org.uddi4j.datatype.binding.BindingTemplate;
import org.apache.log4j.Logger;
import org.uddi4j.util.AuthInfo;

import java.util.Vector;

/**
 * @author Steve Viens (steve@inflexionpoint.com)
 * @version 0.6.2
 */
public class SaveBindingService extends PublishService
{
  // private reference to jUDDI Logger
  private static Logger log = Logger.getLogger(SaveBindingService.class);

  // private reference to jUDDI DataStoreFactory
  private static DataStoreFactory factory = DataStoreFactory.getInstance();

  /**
   *
   */
  public UDDIElement invoke(UDDIElement element)
    throws JUDDIException
  {
    SaveBinding request = (SaveBinding)element;
    Vector bindingVector = request.getBindingTemplateVector();
    AuthInfo authInfo = request.getAuthInfo();
    BindingDetail detail = null;

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

    if ((bindingVector != null) && (bindingVector.size() > 0))
    {
      try
      {
        dataStore.beginTrans();
        verify(publisherID,bindingVector,dataStore);
        detail = execute(publisherID,bindingVector,dataStore);
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
    }

    return detail;
  }

  /**
   *
   */
  private void verify(String publisherID,Vector bindingVector,DataStore dataStore)
    throws JUDDIException
  {
    for (int i=0; i<bindingVector.size(); i++)
    {
      // move the BindingTemplate into a form we can work with easily
      BindingTemplate binding = (BindingTemplate)bindingVector.elementAt(i);
      String serviceKey = binding.getServiceKey();
      String bindingKey = binding.getBindingKey();

      // Confirm that the 'BusinessService' that this binding belongs to
      // really exists. If not then throw an InvalidKeyPassedException.
     if ((serviceKey == null) || (serviceKey.length() == 0) || (!dataStore.isValidServiceKey(serviceKey)))
        throw new InvalidKeyPassedException("ServiceKey: "+serviceKey);

      // Confirm that 'publisherID' controls the BusinessService that this
      // binding template belongs to.  If not then throw a UserMismatchException.
      if (!dataStore.isServicePublisher(serviceKey,publisherID))
        throw new UserMismatchException("ServiceKey: "+serviceKey);

      // If a BindingKey was specified then make sure it's a valid one.
      if ((bindingKey != null) && (bindingKey.length() > 0) && (!dataStore.isValidBindingKey(bindingKey)))
        throw new InvalidKeyPassedException("BindingKey: "+bindingKey);
    }
  }

  /**
   *
   */
  private BindingDetail execute(String publisherID,Vector bindingVector,DataStore dataStore)
    throws JUDDIException
  {
    for (int i=0; i<bindingVector.size(); i++)
    {
      // move the BindingTemplate data into a form we can work with easily
      BindingTemplate binding = (BindingTemplate)bindingVector.elementAt(i);
      String bindingKey = binding.getBindingKey();

      // If the new BindingTemplate has a BindingKey then it must already
      // exists so delete the old one. It a BindingKey isn't specified then
      // this is a new BindingTemplate so create a new BindingKey for it.
      if ((bindingKey != null) && (bindingKey.length() > 0))
        dataStore.deleteBinding(bindingKey);
      else
        binding.setBindingKey(UUID.nextID());

      // everything checks out so let's save it.
      dataStore.saveBinding(binding);
    }

    BindingDetail detail = new BindingDetail();
    detail.setOperator(operator);
    detail.setTruncated(false);
    detail.setBindingTemplateVector(bindingVector);
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
    SaveBinding request = new SaveBinding();
    // todo ... need more here!

    try
    {
      // invoke the service
      BindingDetail response = (BindingDetail)(new SaveBindingService().invoke(request));
      // todo ... need more here!
    }
    finally
    {
      // terminate all jUDDI Subsystems
      org.juddi.util.SysManager.shutdown();
    }
  }
}

