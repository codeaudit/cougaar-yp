/*
 * jUDDI - An open source Java implementation of UDDI v2.0
 * http://juddi.org/
 *
 * Copyright (c) 2002, InflexionPoint and contributors
 * All rights reserved.
 */

package org.juddi.transport.axis;

import org.juddi.error.JUDDIException;
import org.apache.log4j.Logger;
import org.w3c.dom.Element;
import org.uddi4j.UDDIElement;

import java.lang.reflect.Constructor;
import java.util.Hashtable;

/**
 * The RequestFactory's sole responsibility is to transform
 * incoming requests from a raw XML Element into a Java request
 * object.
 *
 * @author Steve Viens (steve@inflexionpoint.com)
 * @version 0.6.2
 */
public class RequestFactory
{
  // reference to the jUDDI logger
  static Logger log = Logger.getLogger(RequestFactory.class);

  // uddi request type registry (note: keys are in lower case)
  static Hashtable classTable = new Hashtable();
  static
  {
    // IMPORTANT: We change the case of the value used to lookup values in this
    // table to lower case before performing a 'get' on the hashtable so we can
    // be sure that we return the correct UDDIElement subclass name regardless
    // of the case of the value passed in. - Steve

    try
    {
      // uddi inquiry api
      classTable.put("find_binding",                org.uddi4j.request.FindBinding.class);
      classTable.put("find_business",               org.uddi4j.request.FindBusiness.class);;
      classTable.put("find_relatedbusinesses",      org.uddi4j.request.FindRelatedBusinesses.class);
      classTable.put("find_service",                org.uddi4j.request.FindService.class);
      classTable.put("find_tmodel",                 org.uddi4j.request.FindTModel.class);
      classTable.put("get_bindingdetail",           org.uddi4j.request.GetBindingDetail.class);
      classTable.put("get_businessdetailext",       org.uddi4j.request.GetBusinessDetailExt.class);
      classTable.put("get_businessdetail",          org.uddi4j.request.GetBusinessDetail.class);
      classTable.put("get_servicedetail",           org.uddi4j.request.GetServiceDetail.class);
      classTable.put("get_tmodeldetail",            org.uddi4j.request.GetTModelDetail.class);
      classTable.put("validate_values",             org.uddi4j.request.ValidateValues.class);

      // uddi publish api
      classTable.put("add_publisherassertions",     org.uddi4j.request.AddPublisherAssertions.class);
      classTable.put("delete_binding",              org.uddi4j.request.DeleteBinding.class);
      classTable.put("delete_business",             org.uddi4j.request.DeleteBusiness.class);
      classTable.put("delete_publisherassertions",  org.uddi4j.request.DeletePublisherAssertions.class);
      classTable.put("delete_service",              org.uddi4j.request.DeleteService.class);
      classTable.put("delete_tmodel",               org.uddi4j.request.DeleteTModel.class);
      classTable.put("discard_authtoken",           org.uddi4j.request.DiscardAuthToken.class);
      classTable.put("get_assertionstatusreport",   org.uddi4j.request.GetAssertionStatusReport.class);
      classTable.put("get_authtoken",               org.uddi4j.request.GetAuthToken.class);
      classTable.put("get_publisherassertions",     org.uddi4j.request.GetPublisherAssertions.class);
      classTable.put("get_registeredinfo",          org.uddi4j.request.GetRegisteredInfo.class);
      classTable.put("save_binding",                org.uddi4j.request.SaveBinding.class);
      classTable.put("save_business",               org.uddi4j.request.SaveBusiness.class);
      classTable.put("save_service",                org.uddi4j.request.SaveService.class);
      classTable.put("save_tmodel",                 org.uddi4j.request.SaveTModel.class);
      classTable.put("set_publisherassertions",     org.uddi4j.request.SetPublisherAssertions.class);
      classTable.put("validate_values",             org.uddi4j.request.ValidateValues.class);
    }
    catch(Exception ex)
    {
      log.error(ex.getMessage(),ex);
    }
  }

  /**
   *
   */
  public static synchronized Object getRequest(Element requestDOM)
    throws JUDDIException
  {
    String requestName = requestDOM.getTagName();
    if ((requestName == null) || ((requestName.length() == 0)))
    {
      String msg = "A null or zero-length requestName was passed to PublishRequestFactory.getRequest: "+requestName;
      log.error(msg);
      throw new JUDDIException(msg);
    }

    // look up the UDDIElement subclass
    Class requestClass = (Class)classTable.get(requestName.toLowerCase());

    // verify that we found a corresponding UDDIElement subclass
    if (requestClass == null)
    {
      String msg = "A class was not found for the requestName: " + requestName;
      log.error(msg);
      throw new JUDDIException(msg);
    }

    return instantiateClass(requestClass,requestDOM);
  }

  /**
   *
   */
  private static synchronized Object instantiateClass(Class requestClass,Element requestDOM)
    throws JUDDIException
  {
    Object instance = null;

    try
    {
      // what's this doing?
      Class parameters[] = new Class[1];
      parameters[0] = org.w3c.dom.Element.class;

      // find a constructor that takes a DOM Element as a parameter
      Constructor requestClassConstructor = requestClass.getConstructor(parameters);

      // what's this doing?
      Object constructorParam[] = new Object[1];
      constructorParam[0] = requestDOM;

      // create a new UDDIElement instance using the 'Element' constructor
      instance = (Object)requestClassConstructor.newInstance(constructorParam);
    }
    catch (InstantiationException ex)
    {
      String msg = "Exception while instantiating the specified class: " + requestClass.getName();
      log.error(msg);
      throw new JUDDIException(msg);
    }
    catch (IllegalAccessException ex)
    {
      ex.printStackTrace();
    }
    catch (NoSuchMethodException ex)
    {
      String msg = "Exception finding necessary constructor for specified class: " + requestClass.getName();
      log.error(msg);
      throw new JUDDIException(msg);
    }
    catch (java.lang.reflect.InvocationTargetException ex)
    {
      String msg = "Exception while instantiating the specified class: " + requestClass.getName();
      log.error(msg);
      throw new JUDDIException(msg);
    }

    return instance;
  }
}