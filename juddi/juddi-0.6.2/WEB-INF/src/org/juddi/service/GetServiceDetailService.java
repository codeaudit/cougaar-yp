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
import org.uddi4j.request.GetServiceDetail;
import org.uddi4j.response.ServiceDetail;
import org.apache.log4j.Logger;

import java.util.Vector;

/**
 * @author Steve Viens (steve@inflexionpoint.com)
 * @version 0.6.2
 */
public class GetServiceDetailService extends UDDIService
{
  // private reference to jUDDI Logger
  private static Logger log = Logger.getLogger(GetServiceDetailService.class);

  // private reference to jUDDI DataStoreFactory
  private static DataStoreFactory factory = DataStoreFactory.getInstance();

  /**
   *
   */
  public UDDIElement invoke(UDDIElement element)
    throws JUDDIException
  {
    GetServiceDetail request = (GetServiceDetail)element;
    Vector keyVector = request.getServiceKeyStrings();
    ServiceDetail detail = null;

    // aquire a jUDDI datastore instance
    DataStore dataStore = factory.aquireDataStore();

    try
    {
      dataStore.beginTrans();
      verify(keyVector,dataStore);
      detail = execute(keyVector,dataStore);
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

    return detail;
  }

  /**
   *
   */
  private void verify(Vector keyVector,DataStore dataStore)
    throws JUDDIException
  {
    for (int i=0; i<keyVector.size(); i++)
    {
      String serviceKey = (String)keyVector.elementAt(i);

      // If the a BusinessService doesn't exist hrow an InvalidKeyPassedException.
      if ((serviceKey == null) || (serviceKey.length() == 0) ||
          (!dataStore.isValidServiceKey(serviceKey)))
        throw new InvalidKeyPassedException("ServiceKey: "+serviceKey);
    }
  }

  /**
   *
   */
  private ServiceDetail execute(Vector keyVector,DataStore dataStore)
    throws JUDDIException
  {
    Vector serviceVector = new Vector();

    for (int i=0; i<keyVector.size(); i++)
    {
      String serviceKey = (String)keyVector.elementAt(i);
      serviceVector.add(dataStore.fetchService(serviceKey));
    }

    // create a new ServiceDetail and stuff the new serviceVector into it.
    ServiceDetail detail = new ServiceDetail();
    detail.setOperator(operator);
    detail.setBusinessServiceVector(serviceVector);
    return detail;
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
    GetServiceDetail request = new GetServiceDetail();
    // todo ... need more here!

    try
    {
      // invoke the service
      ServiceDetail response = (ServiceDetail)(new GetServiceDetailService().invoke(request));
      // todo ... need more here!
    }
    finally
    {
      // terminate all jUDDI Subsystems
      org.juddi.util.SysManager.shutdown();
    }
  }
}

