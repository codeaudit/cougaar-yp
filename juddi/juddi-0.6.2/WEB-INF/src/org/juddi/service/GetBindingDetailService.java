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
import org.uddi4j.request.GetBindingDetail;
import org.uddi4j.response.BindingDetail;
import org.apache.log4j.Logger;

import java.util.Vector;

/**
 * @author Steve Viens (steve@inflexionpoint.com)
 * @version 0.6.2
 */
public class GetBindingDetailService extends UDDIService
{
  // private reference to jUDDI Logger
  private static Logger log = Logger.getLogger(GetBindingDetailService.class);

  // private reference to jUDDI Logger
  private static DataStoreFactory factory = DataStoreFactory.getInstance();

  /**
   *
   */
  public UDDIElement invoke(UDDIElement element)
    throws JUDDIException
  {
    GetBindingDetail request = (GetBindingDetail)element;
    Vector keyVector = request.getBindingKeyStrings();
    BindingDetail detail = null;

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
      // grab the next key from the vector
      String key = (String)keyVector.elementAt(i);

      // check that this binding template really exists.
      // If not then throw an InvalidKeyPassedException.
      if ((key == null) || (key.length() == 0) ||
          (!dataStore.isValidBindingKey(key)))
        throw new InvalidKeyPassedException("BindingKey: "+key);
    }
  }

  /**
   *
   */
  private BindingDetail execute(Vector keyVector,DataStore dataStore)
    throws JUDDIException
  {
    Vector bindingVector = new Vector();

    for (int i=0; i<keyVector.size(); i++)
    {
      String key = (String)keyVector.elementAt(i);
      bindingVector.add(dataStore.fetchBinding(key));
    }

    // create a new BindingDetail and stuff the new bindingVector into it.
    BindingDetail detail = new BindingDetail();
    detail.setBindingTemplateVector(bindingVector);
    detail.setOperator(operator);
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
    GetBindingDetail request = new GetBindingDetail();
    // todo ... need more here!

    try
    {
      // invoke the service
      BindingDetail response = (BindingDetail)(new GetBindingDetailService().invoke(request));
      // todo ... need more here!
    }
    finally
    {
      // terminate all jUDDI Subsystems
      org.juddi.util.SysManager.shutdown();
    }
  }
}