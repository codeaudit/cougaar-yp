/*
 * jUDDI - An open source Java implementation of UDDI v2.0
 * http://juddi.org/
 *
 * Copyright (c) 2002, InflexionPoint and contributors
 * All rights reserved.
 */

package org.juddi.service;

import org.juddi.error.JUDDIException;

import org.apache.log4j.Logger;

import java.util.Hashtable;

/**
 * Implementation of Factory pattern used to create one of the UDDIService
 * subclasses.
 *
 * The name of the UDDI4j request class to handle is passed to the getService
 * method which is used locate and instantiate the appropriate UDDIService
 * to handle the particular request.  If a null value or empty String is
 * passed then the default An error is logged and a JUDDIException is thrown.
 *
 * @author Steve Viens (steve@inflexionpoint.com)
 * @version 0.6.2
 */
public abstract class ServiceFactory 
{
  // reference to the jUDDI logger
  static Logger log = Logger.getLogger(ServiceFactory.class);

  // In memory database of UDDI element names to jUDDI class mappings
  static Hashtable serviceTable= new Hashtable();
  static
  {
    // IMPORTANT: We change the case of the value used to lookup values in this
    // table to lower case before performing a 'get' on the hashtable so we can
    // be sure that we return the correct UDDIService subclass name regardless
    // of the case of the value passed in. - Steve

    try
    {
      // use to obtain the service class via the UDDI request class name (read IMPORTANT above regarding case)
      serviceTable.put("org.uddi4j.request.addpublisherassertions",     org.juddi.service.AddPublisherAssertionsService.class);
      serviceTable.put("org.uddi4j.request.deletebinding",              org.juddi.service.DeleteBindingService.class);
      serviceTable.put("org.uddi4j.request.deletebusiness",             org.juddi.service.DeleteBusinessService.class);
      serviceTable.put("org.uddi4j.request.deletepublisherassertions",  org.juddi.service.DeletePublisherAssertionsService.class);
      serviceTable.put("org.uddi4j.request.deleteservice",              org.juddi.service.DeleteServiceService.class);
      serviceTable.put("org.uddi4j.request.deletetmodel",               org.juddi.service.DeleteTModelService.class);
      serviceTable.put("org.uddi4j.request.discardauthtoken",           org.juddi.service.DiscardAuthTokenService.class);
      serviceTable.put("org.uddi4j.request.findbinding",                org.juddi.service.FindBindingService.class);
      serviceTable.put("org.uddi4j.request.findbusiness",               org.juddi.service.FindBusinessService.class);
      serviceTable.put("org.uddi4j.request.findrelatedbusinesses",      org.juddi.service.FindRelatedBusinessesService.class);
      serviceTable.put("org.uddi4j.request.findservice",                org.juddi.service.FindServiceService.class);
      serviceTable.put("org.uddi4j.request.findtmodel",                 org.juddi.service.FindTModelService.class);
      serviceTable.put("org.uddi4j.request.getassertionstatusreport",   org.juddi.service.GetAssertionStatusReportService.class);
      serviceTable.put("org.uddi4j.request.getauthtoken",               org.juddi.service.GetAuthTokenService.class);
      serviceTable.put("org.uddi4j.request.getbindingdetail",           org.juddi.service.GetBindingDetailService.class);
      serviceTable.put("org.uddi4j.request.getbusinessdetailext",       org.juddi.service.GetBusinessDetailExtService.class);
      serviceTable.put("org.uddi4j.request.getbusinessdetail",          org.juddi.service.GetBusinessDetailService.class);
      serviceTable.put("org.uddi4j.request.getpublisherassertions",     org.juddi.service.GetPublisherAssertionsService.class);
      serviceTable.put("org.uddi4j.request.getregisteredinfo",          org.juddi.service.GetRegisteredInfoService.class);
      serviceTable.put("org.uddi4j.request.getservicedetail",           org.juddi.service.GetServiceDetailService.class);
      serviceTable.put("org.uddi4j.request.gettmodeldetail",            org.juddi.service.GetTModelDetailService.class);
      serviceTable.put("org.uddi4j.request.savebinding",                org.juddi.service.SaveBindingService.class);
      serviceTable.put("org.uddi4j.request.savebusiness",               org.juddi.service.SaveBusinessService.class);
      serviceTable.put("org.uddi4j.request.saveservice",                org.juddi.service.SaveServiceService.class);
      serviceTable.put("org.uddi4j.request.savetmodel",                 org.juddi.service.SaveTModelService.class);
      serviceTable.put("org.uddi4j.request.setpublisherassertions",     org.juddi.service.SetPublisherAssertionsService.class);
      serviceTable.put("org.uddi4j.request.validatevalues",             org.juddi.service.ValidateValuesService.class);
    }
    catch(Exception ex)
    {
      log.error(ex.getMessage(),ex);
    }
  }

  /**
   *
   */
  public static synchronized UDDIService getService(String serviceName)
    throws JUDDIException
  {
    // validate that we've got a valid serviceName
    if ((serviceName == null) || (serviceName.length() == 0))
    {
      String msg = "A null or zero length serviceName was passed to ServiceFactory.getService: "+serviceName;
      log.error(msg);
      throw new JUDDIException(msg);
    }

    // look up the UDDIService subclass
    Class serviceClass = (Class)serviceTable.get(serviceName.toLowerCase());

    // verify that we found a corresponding UDDIService subclass
    if (serviceClass == null)
    {
      String msg = "An invalid or unknown serviceName was passed to ServiceFactory.getService: "+serviceName;
      log.error(msg);
      throw new JUDDIException(msg);
    }

    // create an instance of the UDDIService subclass
    UDDIService service = null;
    try {
      service = (UDDIService)serviceClass.newInstance();
    }
    catch(Exception ex)
    {
      log.error(ex.getMessage());
      throw new JUDDIException(ex);
    }

    return service;
  }


  /***************************************************************************/
  /***************************** TEST DRIVER *********************************/
  /***************************************************************************/


  public static void main(String[] args)
    throws Exception
  {
    try
    {
      // start up the jUDDI sub-systems (logging, authentication, etc.) - Steve
      org.juddi.util.SysManager.startup();

      // generate the request object
      org.uddi4j.request.GetAuthToken request = new org.uddi4j.request.GetAuthToken("sviens","password");

      // obtain the service object
      //UDDIService service = ServiceFactory.getService("get_authToken");
      UDDIService service = ServiceFactory.getService(request.getClass().getName());

      // call the service's 'invoke' method (invoke the service)
      org.uddi4j.response.AuthToken response = (org.uddi4j.response.AuthToken)service.invoke(request);

      // write response to the console
      System.out.println("UDDIService: get_authToken");
      System.out.println(" AuthInfo: "+response.getAuthInfoString());
    }
    finally
    {
      // shutdown the jUDDI sub-systems (logging, authentication, etc.). These
      // may have started up background threads or may have some other
      // external resource tied up (ie database connections). - Steve
      org.juddi.util.SysManager.shutdown();
    }
  }
}