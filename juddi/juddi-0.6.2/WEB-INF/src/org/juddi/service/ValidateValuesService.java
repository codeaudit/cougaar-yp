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

import org.uddi4j.UDDIElement;
import org.uddi4j.request.ValidateValues;
import org.uddi4j.response.DispositionReport;
import org.apache.log4j.Logger;

import java.util.Vector;

/**
 * @author Steve Viens (steve@inflexionpoint.com)
 * @version 0.6.2
 */
public class ValidateValuesService extends UDDIService
{
  // private reference to jUDDI Logger
  private static Logger log = Logger.getLogger(ValidateValuesService.class);

  // private reference to jUDDI DataStoreFactory
  private static DataStoreFactory factory = DataStoreFactory.getInstance();

  /**
   *
   */
  public UDDIElement invoke(UDDIElement element)
    throws JUDDIException
  {
    ValidateValues request = (ValidateValues)element;
    Vector businessVector = request.getBusinessEntityVector();
    Vector serviceVector = request.getBusinessServiceVector();
    Vector tModelVector = request.getTModelVector();

    // if we got any Business Entities then let's check'm
    if ((businessVector != null) && (businessVector.size() > 0))
      validateBusinessVector(businessVector);

    // if we got any Services then let's check'm
    if ((serviceVector != null) && (serviceVector.size() > 0))
      validateServiceVector(serviceVector);

    // if we got any TModels then let's check'm
    if ((tModelVector != null) && (tModelVector.size() > 0))
      validateTtModelVector(tModelVector);

    // didn't encounter an exception so let's return
    // the pre-created successful DispositionReport
    return this.success;
  }

  /**
   *
   */
  private void validateBusinessVector(Vector businessVector)
    throws JUDDIException
  {
    throw new org.juddi.error.UnsupportedException(
		"The ValidateValues service is not yet supported.");
  }

  /**
   *
   */
  private void validateServiceVector(Vector serviceVector)
    throws JUDDIException
  {
    throw new org.juddi.error.UnsupportedException(
		"The ValidateValues service is not yet supported.");
  }

  /**
   *
   */
  private void validateTtModelVector(Vector tModelVector)
    throws JUDDIException
  {
    throw new org.juddi.error.UnsupportedException(
		"The ValidateValues service is not yet supported.");
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
    ValidateValues request = new ValidateValues();
    // todo ... need more here!

    try
    {
      // invoke the service
      DispositionReport response = (DispositionReport)(new ValidateValuesService().invoke(request));
      // todo ... need more here!
    }
    finally
    {
      // terminate all jUDDI Subsystems
      org.juddi.util.SysManager.shutdown();
    }
  }
}

