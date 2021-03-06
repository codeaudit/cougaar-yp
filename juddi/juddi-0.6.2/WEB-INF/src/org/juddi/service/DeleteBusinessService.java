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
import org.uddi4j.request.DeleteBusiness;
import org.uddi4j.response.DispositionReport;
import org.uddi4j.util.AuthInfo;
import org.apache.log4j.Logger;

import java.util.Vector;

/**
 * @author Steve Viens (steve@inflexionpoint.com)
 * @version 0.6.2
 */
public class DeleteBusinessService extends PublishService
{
  // private reference to jUDDI Logger
  private static Logger log = Logger.getLogger(DeleteBusinessService.class);

  // private reference to jUDDI DataStoreFactory
  private static DataStoreFactory factory = DataStoreFactory.getInstance();

  /**
   *
   */
  public UDDIElement invoke(UDDIElement element)
    throws JUDDIException
  {
    DeleteBusiness request = (DeleteBusiness)element;
    Vector businessKeyVector = request.getBusinessKeyStrings();
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
      verify(publisherID,businessKeyVector,dataStore);  // verify every aspect of the request
      execute(publisherID,businessKeyVector,dataStore); // passed verification, let's execute
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
  private void verify(String publisherID,Vector businessKeyVector,DataStore dataStore)
    throws JUDDIException
  {
    for (int i=0; i<businessKeyVector.size(); i++)
    {
      // grab the next key from the vector
      String businessKey = (String)businessKeyVector.elementAt(i);

      // check that this business entity really exists.
      // If not then throw an InvalidKeyPassedException.
      if ((!dataStore.isValidBusinessKey(businessKey)))
        throw new InvalidKeyPassedException("BusinessKey: "+businessKey);

      // check to make sure that 'authorizedName' controls this
      // business entity. If not then throw a UserMismatchException.
      if (!dataStore.isBusinessPublisher(businessKey,publisherID))
        throw new UserMismatchException("BusinessKey: "+businessKey);
    }
  }

  /**
   *
   */
  private void execute(String publisherID,Vector businessKeyVector,DataStore dataStore)
    throws JUDDIException
  {
    for (int i=0; i<businessKeyVector.size(); i++)
    {
      String businessKey = (String)businessKeyVector.elementAt(i);
      dataStore.deleteBusiness(businessKey);

      log.info("Publisher '"+publisherID+"' deleted BusinessEntity with key: "+businessKey);
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
    DeleteBusiness request = new DeleteBusiness();
    // todo ... need more here!

    try
    {
      // invoke the service
      DispositionReport response = (DispositionReport)(new DeleteBusinessService().invoke(request));
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

