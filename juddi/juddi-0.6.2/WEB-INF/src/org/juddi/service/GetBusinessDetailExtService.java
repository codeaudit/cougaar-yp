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
import org.uddi4j.datatype.business.BusinessEntity;
import org.uddi4j.response.BusinessEntityExt;
import org.uddi4j.request.GetBusinessDetailExt;
import org.uddi4j.response.BusinessDetailExt;
import org.apache.log4j.Logger;

import java.util.Vector;

/**
 * @author Steve Viens (steve@inflexionpoint.com)
 * @version 0.6.2
 */
public class GetBusinessDetailExtService extends UDDIService
{
  // private reference to jUDDI Logger
  private static Logger log = Logger.getLogger(GetBusinessDetailExtService.class);

  // private reference to jUDDI DataStoreFactory
  private static DataStoreFactory factory = DataStoreFactory.getInstance();

  /**
   *
   */
  public UDDIElement invoke(UDDIElement element)
    throws JUDDIException
  {
    GetBusinessDetailExt request = (GetBusinessDetailExt)element;
    Vector businessKeyVector = request.getBusinessKeyStrings();
    BusinessDetailExt detailExt = null;

    // aquire a jUDDI datastore instance
    DataStore dataStore = factory.aquireDataStore();

    try
    {
      dataStore.beginTrans();
      verify(businessKeyVector,dataStore);
      detailExt = execute(businessKeyVector,dataStore);
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

    return detailExt;
  }

  /**
   *
   */
  private void verify(Vector businessKeyVector,DataStore dataStore)
    throws JUDDIException
  {
    for (int i=0; i<businessKeyVector.size(); i++)
    {
      String businessKey = (String)businessKeyVector.elementAt(i);

      // If the a BusinessEntity doesn't exist hrow an InvalidKeyPassedException.
      if ((businessKey == null) || (businessKey.length() == 0) ||
          (!dataStore.isValidBusinessKey(businessKey)))
        throw new InvalidKeyPassedException("BusinessKey: "+businessKey);
    }
  }

  /**
   *
   */
  private BusinessDetailExt execute(Vector businessKeyVector,DataStore dataStore)
    throws JUDDIException
  {
    Vector businessEntityExtVector = new Vector();

    for (int i=0; i<businessKeyVector.size(); i++)
    {
      String businessKey = (String)businessKeyVector.elementAt(i);
      BusinessEntity business = dataStore.fetchBusiness(businessKey);
      BusinessEntityExt businessExt = new BusinessEntityExt(business);
      businessEntityExtVector.addElement(businessExt);
    }

    // create a new BusinessDetailExt and stuff the Vector of
    // new BusinessEntityExt instances into it.
    BusinessDetailExt detailExt = new BusinessDetailExt();
    detailExt.setOperator(operator);
    detailExt.setBusinessEntityExtVector(businessEntityExtVector);
    return detailExt;
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
    GetBusinessDetailExt request = new GetBusinessDetailExt();
    // todo ... need more here!

    try
    {
      // invoke the service
      BusinessDetailExt response = (BusinessDetailExt)(new GetBusinessDetailExtService().invoke(request));
      // todo ... need more here!
    }
    finally
    {
      // terminate all jUDDI Subsystems
      org.juddi.util.SysManager.shutdown();
    }
  }
}

