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
import org.uddi4j.request.SetPublisherAssertions;
import org.uddi4j.response.PublisherAssertions;
import org.uddi4j.util.AuthInfo;
import org.apache.log4j.Logger;

import java.util.Vector;

/**
 * @author Steve Viens (steve@inflexionpoint.com)
 * @version 0.6.2
 */
public class SetPublisherAssertionsService extends PublishService
{
  // private reference to jUDDI Logger
  private static Logger log = Logger.getLogger(SetPublisherAssertionsService.class);

  // private reference to jUDDI DataStoreFactory
  private static DataStoreFactory factory = DataStoreFactory.getInstance();

  /**
   *
   */
  public UDDIElement invoke(UDDIElement element)
    throws JUDDIException
  {
    SetPublisherAssertions request = (SetPublisherAssertions)element;
    AuthInfo authInfo = request.getAuthInfo();
    Vector assertionVector = request.getPublisherAssertionVector();
    PublisherAssertions assertions = null;

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
      verify(publisherID,assertionVector,dataStore);  // verify every aspect of the request
      assertions = execute(publisherID,assertionVector,dataStore);  // passed verification, let's execute
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
    return assertions;
  }

  /**
   *
   */
  private void verify(String publisherID,Vector assertionVector,DataStore dataStore)
    throws JUDDIException
  {
    return; // nothing to verify
  }

  /**
   *
   */
  private PublisherAssertions execute(String publisherID,Vector assertionVector,DataStore dataStore)
    throws JUDDIException
  {
    PublisherAssertions assertions = new PublisherAssertions();
    assertions.setOperator(operator);
    assertions.setPublisherAssertionVector(dataStore.setAssertions(publisherID,assertionVector));
    return assertions;
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
    SetPublisherAssertions request = new SetPublisherAssertions();
    // todo ... need more here!

    try
    {
      // invoke the service
      PublisherAssertions response = (PublisherAssertions)(new SetPublisherAssertionsService().invoke(request));
      // todo ... need more here!
    }
    finally
    {
      // terminate all jUDDI Subsystems
      org.juddi.util.SysManager.shutdown();
    }
  }
}

