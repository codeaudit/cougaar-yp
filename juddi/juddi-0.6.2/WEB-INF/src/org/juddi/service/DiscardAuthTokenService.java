/* 
 * jUDDI - An open source Java implementation of UDDI v2.0
 * http://juddi.org/
 *
 * Copyright (c) 2002, InflexionPoint and contributors
 * All rights reserved.
 */

package org.juddi.service;

import org.juddi.error.*;
import org.juddi.util.Auth;

import org.uddi4j.UDDIElement;
import org.uddi4j.request.GetAuthToken;
import org.uddi4j.request.DiscardAuthToken;
import org.uddi4j.response.AuthToken;
import org.uddi4j.response.DispositionReport;
import org.uddi4j.util.AuthInfo;
import org.apache.log4j.Logger;

/**
 * @author Steve Viens (steve@inflexionpoint.com)
 * @version 0.6.2
 */
public class DiscardAuthTokenService extends PublishService
{
  // private reference to jUDDI logger
  private static Logger log = Logger.getLogger(DiscardAuthTokenService.class);

  /**
   *
   */
  public UDDIElement invoke(UDDIElement element)
    throws JUDDIException
  {
    DiscardAuthToken request = (DiscardAuthToken)element;
    AuthInfo authInfo = request.getAuthInfo();

    // 1. Check that an authToken was actually included in the
    //    request. If not then throw an AuthTokenRequiredException.
    // 2. Check that the authToken passed in is a valid one.
    //    If not then throw an AuthTokenRequiredException.
    // 3. Check that the authToken passed in has not yet.
    //    expired. If so then throw an AuthTokenExpiredException.
    Auth.validateAuthToken(authInfo);

    try
    {
      verify(authInfo);
      execute(authInfo);
    }
    catch(Exception ex)
    {
      // write to the log
      log.error(ex);

      // prep JUDDIException to throw
      if (ex instanceof JUDDIException)
        throw (JUDDIException)ex;
      else
        throw new JUDDIException(ex);
    }

    // didn't encounter an exception so let's return
    // the pre-created successful DispositionReport
    return this.success;
  }

  /**
   * Verify every aspect of the request before proceeding.
   */
  private void verify(AuthInfo authInfo)
    throws JUDDIException
  {
    // nothing yet to verify yet
  }

  /**
   *
   */
  private void execute(AuthInfo authInfo)
    throws JUDDIException
  {
     Auth.discardAuthToken(authInfo);
  }


  /***************************************************************************/
  /***************************** TEST DRIVER *********************************/
  /***************************************************************************/


  public static void main(String[] args)
    throws Exception
  {
    // initialize all jUDDI Subsystems
    org.juddi.util.SysManager.startup();

    try
    {
      // generate the request object
      GetAuthToken getRequest = new GetAuthToken("sviens","password");

      // invoke the service
      AuthToken getResponse = (AuthToken)(new GetAuthTokenService().invoke(getRequest));

      // create a request
      DiscardAuthToken discardRequest1 = new DiscardAuthToken(getResponse.getAuthInfoString());
      // invoke the service with a valid AuthToken value
      DispositionReport discardResponse = (DispositionReport)(new DiscardAuthTokenService().invoke(discardRequest1));
      System.out.println("errno: "+discardResponse.toString());

      // create a request
      DiscardAuthToken discardRequest2 = new DiscardAuthToken("**-BadAuthToken-**");
      // invoke the service with an invalid AuthToken value
      DispositionReport discardResponse2 = (DispositionReport)(new DiscardAuthTokenService().invoke(discardRequest2));
      System.out.println("errno: "+discardResponse2.toString());
    }
    finally
    {
      // terminate all jUDDI Subsystems
      org.juddi.util.SysManager.shutdown();
    }
  }
}

