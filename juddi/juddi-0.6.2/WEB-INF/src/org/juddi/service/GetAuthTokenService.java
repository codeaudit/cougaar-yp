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
import org.uddi4j.response.AuthToken;
import org.apache.log4j.Logger;

/**
 * @author Steve Viens (steve@inflexionpoint.com)
 * @version 0.6.2
 */
public class GetAuthTokenService extends PublishService
{
  // private reference to jUDDI Logger
  private static Logger log = Logger.getLogger(GetAuthTokenService.class);

  /**
   *
   */
  public UDDIElement invoke(UDDIElement element)
    throws JUDDIException
  {
    GetAuthToken request = (GetAuthToken)element;
    String publisherID = request.getUserID();
    String credential = request.getCred();
    AuthToken authToken = null;

    try
    {
      verify(publisherID,credential);
      authToken = execute(publisherID,credential);
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

    // return the authToken
    return authToken;
  }

  /**
   * Verify every aspect of the request before proceeding.
   */
  private void verify(String publisherID,String credential)
    throws JUDDIException
  {
    // nothing yet to verify yet
  }

  /**
   *
   */
  private AuthToken execute(String publisherID,String credential)
    throws JUDDIException
  {
    AuthToken authToken = Auth.getAuthToken(publisherID,credential);
    authToken.setOperator(operator);
    return authToken;
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
      GetAuthToken request = new GetAuthToken("sviens","password");

      // invoke the service
      AuthToken response = (AuthToken)(new GetAuthTokenService().invoke(request));

      // write response to the console
      System.out.println("UDDIService: getAuthToken");
      System.out.println(" AuthInfo: "+response.getAuthInfoString());
    }
    finally
    {
      // terminate all jUDDI Subsystems
      org.juddi.util.SysManager.shutdown();
    }
  }
}