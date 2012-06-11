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
import org.uddi4j.request.GetPublisherAssertions;
import org.uddi4j.response.PublisherAssertions;
import org.uddi4j.util.AuthInfo;
import org.apache.log4j.Logger;

/**
 * @author Steve Viens (steve@inflexionpoint.com)
 * @version 0.6.2
 */
public class GetPublisherAssertionsService extends PublishService
{
  // private reference to jUDDI Logger
  private static Logger log = Logger.getLogger(GetPublisherAssertionsService.class);

  // private reference to jUDDI DataStoreFactory
  private static DataStoreFactory factory = DataStoreFactory.getInstance();

  /**
   *
   */
  public UDDIElement invoke(UDDIElement element)
    throws JUDDIException
  {
    GetPublisherAssertions request = (GetPublisherAssertions)element;
    AuthInfo authInfo = request.getAuthInfo();
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
      verify(publisherID,dataStore);  // verify every aspect of the request
      assertions = execute(publisherID,dataStore);  // passed verification, let's execute
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

    return assertions;
  }

  /**
   *
   */
  private void verify(String publisherID,DataStore dataStore)
    throws JUDDIException
  {
    return; // nothing to verify
  }

  /**
   *
   */
  private PublisherAssertions execute(String publisherID,DataStore dataStore)
    throws JUDDIException
  {
    PublisherAssertions assertions = new PublisherAssertions();
    assertions.setOperator(operator);
    assertions.setPublisherAssertionVector(dataStore.getAssertions(publisherID));
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
    GetPublisherAssertions request = new GetPublisherAssertions();
    // todo ... need more here!

    try
    {
      // invoke the service
      PublisherAssertions response = (PublisherAssertions)(new GetPublisherAssertionsService().invoke(request));
      // todo ... need more here!
    }
    finally
    {
      // terminate all jUDDI Subsystems
      org.juddi.util.SysManager.shutdown();
    }
  }
}

