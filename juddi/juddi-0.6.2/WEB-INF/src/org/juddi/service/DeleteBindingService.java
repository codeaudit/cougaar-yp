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
import org.uddi4j.request.DeleteBinding;
import org.uddi4j.response.DispositionReport;
import org.uddi4j.util.AuthInfo;
import org.apache.log4j.Logger;

import java.util.Vector;

/**
 * @author Steve Viens (steve@inflexionpoint.com)
 * @version 0.6.2
 */
public class DeleteBindingService extends PublishService
{
  // private reference to jUDDI Logger
  private static Logger log = Logger.getLogger(DeleteBindingService.class);

  // private reference to jUDDI DataStoreFactory
  private static DataStoreFactory factory = DataStoreFactory.getInstance();

  /**
   *
   */
  public UDDIElement invoke(UDDIElement element)
    throws JUDDIException
  {
    DeleteBinding request = (DeleteBinding)element;
    Vector bindingKeyVector = request.getBindingKeyStrings();
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
      verify(publisherID,bindingKeyVector,dataStore);  // verify every aspect of the request
      execute(publisherID,bindingKeyVector,dataStore); // passed verification, let's execute
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
   *
   */
  private void verify(String publisherID,Vector bindingKeyVector,DataStore dataStore)
    throws JUDDIException
  {
    for (int i=0; i<bindingKeyVector.size(); i++)
    {
      // grab the next key from the vector
      String bindingKey = (String)bindingKeyVector.elementAt(i);

      // check that this binding template really exists.
      // If not then throw an InvalidKeyPassedException.
      if ((bindingKey == null) || (bindingKey.length() == 0) ||
          (!dataStore.isValidBindingKey(bindingKey)))
        throw new InvalidKeyPassedException("BindingKey: "+bindingKey);

      // check to make sure that 'authorizedName' controls the
      // business entity that this binding belongs to. If not
      // then throw a UserMismatchException.
      if (!dataStore.isBindingPublisher(bindingKey,publisherID))
        throw new UserMismatchException("BindingKey: "+bindingKey);
    }
  }

  /**
   *
   */
  private void execute(String publisherID,Vector bindingKeyVector,DataStore dataStore)
    throws JUDDIException
  {
    for (int i=0; i<bindingKeyVector.size(); i++)
    {
      String bindingKey = (String)bindingKeyVector.elementAt(i);
      dataStore.deleteBinding(bindingKey);

      log.info("Publisher '"+publisherID+"' deleted BindingTemplate with key: "+bindingKey);
    }
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
    DeleteBinding request = new DeleteBinding();
    // todo ... need more here!

    try
    {
      // invoke the service
      DispositionReport response = (DispositionReport)(new DeleteBindingService().invoke(request));
      System.out.println("errno: "+response.toString());
      // todo ... need more here!
    }
    finally
    {
      // terminate all jUDDI Subsystems
      org.juddi.util.SysManager.shutdown();
    }
  }
}

