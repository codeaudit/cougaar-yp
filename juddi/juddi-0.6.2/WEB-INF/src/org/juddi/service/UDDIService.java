/*
 * jUDDI - An open source Java implementation of UDDI v2.0
 * http://juddi.org/
 *
 * Copyright (c) 2002, InflexionPoint and contributors
 * All rights reserved.
 */

package org.juddi.service;

import org.juddi.error.*;

import org.uddi4j.UDDIElement;
import org.uddi4j.response.DispositionReport;
import org.juddi.util.Config;
import org.uddi4j.response.Result;

import java.util.Vector;

/**
 * @author Steve Viens (steve@inflexionpoint.com)
 * @version 0.6.2
 */
public abstract class UDDIService
{
  /**
   *  Valid UDDI Version 2.0 SOAPMessage 'generic' attribute
   */
  public static final String GENERIC = "2.0";

  /**
   *  Valid UDDI Version 2.0 SOAPMessage 'xmlns' attribute
   */
  public static final String XMLNS = "urn:uddi-org:api_v2";

  /**
   *  Private reference to the name of this UDDI Operator Site
   */
  static final String operator = Config.getOperatorURI();

  /**
   *
   */
  static final DispositionReport success = getSuccessfullDispRpt();

  /**
   *
   */
  public abstract UDDIElement invoke(UDDIElement request)
    throws JUDDIException;

  /**
   *
   */
  protected static void checkVersionInfo(UDDIElement request)
    throws UnrecognizedVersionException
  {
    if (request.GENERIC == null)
      throw new UnrecognizedVersionException("The UDDI request received did " +
        "not include a 'generic' attribute. This attribute must be present " +
        "in all UDDI requests in order to process the request successfully. " +
        "Currently only version 2.0 (generic=\"2.0\") of the UDDI specification " +
        "is supported.");

    if (request.XMLNS == null)
      throw new UnrecognizedVersionException("The UDDI request received did " +
        "not include a 'xmlns' attribute. This attribute must be present " +
        "in all UDDI requests in order to process the request successfully. " +
        "Currently only version 2.0 (xmlns=\"urn:uddi-org:api_v2\") of the " +
        "UDDI specification is supported.");

//	SMV*10-11-02*commented out to allow version 1 UDDI clients
//  to access a jUDDI registry (a <service> entry was also added
//  to the Axis deployment descriptor "server-config.wsdd".
//
//    if (!request.GENERIC.equalsIgnoreCase(GENERIC))
//      throw new UnrecognizedVersionException("Currently only version 2.0 " +
//        "(generic=\"2.0\") of the UDDI specification is supported. The UDDI " +
//        "request received included a 'generic' attribute value of '" +
//        request.GENERIC+"'");

    if (!request.XMLNS.equalsIgnoreCase(XMLNS))
      throw new UnrecognizedVersionException("Currently only version 2.0 " +
        "(xmlns=\"urn:uddi-org:api_v2\") of the UDDI specification is supported. " +
        "The UDDI request received included an 'xmlns' attribute value of '" +
        request.XMLNS+"'");
  }

  /**
   *
   */
  private synchronized static DispositionReport getSuccessfullDispRpt()
  {
    if (success != null)
      return success;

    DispositionReport dispRpt = new DispositionReport();
    dispRpt.setGeneric(DispositionReport.GENERIC);
    dispRpt.setOperator(operator);

    Result result = new Result();
    result.setErrno("0");

    Vector results = new Vector();
    results.add(result);

    return dispRpt;
  }
}
