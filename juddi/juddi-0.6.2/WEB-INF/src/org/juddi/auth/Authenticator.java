/*
 * jUDDI - An open source Java implementation of UDDI v2.0
 * http://juddi.org/
 *
 * Copyright (c) 2002, InflexionPoint and contributors
 * All rights reserved.
 */

package org.juddi.auth;

import org.juddi.error.JUDDIException;

import org.uddi4j.response.AuthToken;
import org.uddi4j.util.AuthInfo;

/**
 * @author Steve Viens (steve@inflexionpoint.com)
 * @version 0.6.2
 */
public interface Authenticator
{
 /**
  *
  */
  void init();

 /**
  *
  */
  void destroy();

 /**
  *
  */
  public AuthToken getAuthToken(String authorizedName,String credential)
    throws JUDDIException;

 /**
  *
  */
  public void discardAuthToken(AuthInfo authInfo)
    throws JUDDIException;

 /**
  *
  */
  public void validateAuthToken(AuthInfo authInfo)
    throws JUDDIException;

 /**
  *
  */
  public String getPublisherName(AuthInfo authInfo)
    throws JUDDIException;

  /**
   *
   */
   public String getPublisherID(AuthInfo authInfo)
     throws JUDDIException;
}