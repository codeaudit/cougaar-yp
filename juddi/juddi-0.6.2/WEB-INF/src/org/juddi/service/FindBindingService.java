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

import org.apache.log4j.Logger;
import org.uddi4j.UDDIElement;
import org.uddi4j.util.TModelBag;
import org.uddi4j.util.FindQualifiers;
import org.uddi4j.util.FindQualifier;
import org.uddi4j.response.BindingDetail;
import org.uddi4j.request.FindBinding;

import java.util.Vector;

/**
 * @author Steve Viens (steve@inflexionpoint.com)
 * @version 0.6.2
 */
public class FindBindingService extends UDDIService
{
  // private reference to jUDDI Logger
  private static Logger log = Logger.getLogger(FindBindingService.class);

  // private reference to jUDDI DataStoreFactory
  private static DataStoreFactory factory = DataStoreFactory.getInstance();

  /**
   *
   */
  public UDDIElement invoke(UDDIElement element)
    throws JUDDIException
  {
    FindBinding request = (FindBinding)element;
    String serviceKey = request.getServiceKey();
    TModelBag tModelBag = request.getTModelBag();
    FindQualifiers qualifiers = request.getFindQualifiers();
    String maxRowsString = request.getMaxRows();
    BindingDetail detail = null;

    // first make sure we need to continue with this request. If
    // no arguments were passed in then we'll simply return
    // an empty ServiceList (aka "a zero match result set").
    if ((tModelBag == null) || (tModelBag.size() == 0))
    {
      detail = new BindingDetail();
      detail.setBindingTemplateVector(null);
      detail.setOperator(operator);
      detail.setTruncated(false);
      return detail;
    }

    // if a 'maxRows' value has been passed then we'll
    // need to parse/convert it and use it.
    int maxRows = 0;
    if ((maxRowsString != null) && (maxRowsString.trim().length()>0))
      maxRows = Integer.parseInt(maxRowsString);

    // aquire a jUDDI datastore instance
    DataStore dataStore = factory.aquireDataStore();

    try
    {
      dataStore.beginTrans();
      verify(serviceKey,tModelBag,qualifiers,maxRows,dataStore);
      detail = execute(serviceKey,tModelBag,qualifiers,maxRows,dataStore);
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
  private void verify(String serviceKey,TModelBag tModelBag,FindQualifiers qualifiers,int maxRows,DataStore dataStore)
    throws JUDDIException
  {
    // a find_binding request MUST include a service_key attribute
    if ((serviceKey == null) || (serviceKey.length() == 0))
      throw new InvalidKeyPassedException("All find_biding requests must specify a valid ServiceKey attribute.");

    // validate the 'qualifiers' parameter as much as possible up-front before
    // calling into the data layer for relational validation.
    if (qualifiers != null)
    {
      for (int i=0; i<qualifiers.size(); i++)
      {
        FindQualifier qualifier = (FindQualifier)qualifiers.get(i);
        String qValue = qualifier.getText();

        if ((!qValue.equals(FindQualifier.exactNameMatch)) &&
            (!qValue.equals(FindQualifier.caseSensitiveMatch)) &&
            (!qValue.equals(FindQualifier.orAllKeys)) &&
            (!qValue.equals(FindQualifier.orLikeKeys)) &&
            (!qValue.equals(FindQualifier.andAllKeys)) &&
            (!qValue.equals(FindQualifier.sortByNameAsc)) &&
            (!qValue.equals(FindQualifier.sortByNameDesc)) &&
            (!qValue.equals(FindQualifier.sortByDateAsc)) &&
            (!qValue.equals(FindQualifier.sortByDateDesc)) &&
            (!qValue.equals(FindQualifier.serviceSubset)) &&
            (!qValue.equals(FindQualifier.combineCategoryBags)))
          throw new org.juddi.error.UnsupportedException(
            "The FindQualifier '"+qValue+"' is not supported.");
      }
    }
  }

  /**
   *
   */
  private BindingDetail execute(String serviceKey,TModelBag tModelBag,FindQualifiers qualifiers,int maxRows,DataStore dataStore)
    throws JUDDIException
  {
    Vector bindingVector = null;
    boolean truncatedResults = false;

    // perform the search for matching binding templates (return only keys in requested order)
    Vector keyVector = dataStore.findBinding(serviceKey,tModelBag,qualifiers);
    if ((keyVector != null) && (keyVector.size() > 0))
    {
      // if a maxRows value has been specified and it's less than
      // the number of rows we are about to return then only return
      // maxRows specified.
      int rowCount = keyVector.size();
      if ((maxRows > 0) && (maxRows < rowCount))
      {
        rowCount = maxRows;
        truncatedResults = true;
      }

      // iterate through the binding templates keys fetching
      // each associated BindingTemplate in sequence.
      bindingVector = new Vector(rowCount);
      for (int i=0; i<rowCount; i++)
        bindingVector.addElement(dataStore.fetchBinding((String)keyVector.elementAt(i)));
    }

    // create a new BindingDetail instance and stuff
    // the new bindingTemplateVector into it.
    BindingDetail detail = new BindingDetail();
    detail.setBindingTemplateVector(bindingVector);
    detail.setOperator(operator);
    detail.setTruncated(truncatedResults);
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
    FindBinding request = new FindBinding();
    // todo ... need more here!

    try
    {
      // invoke the service
      BindingDetail response = (BindingDetail)(new FindBindingService().invoke(request));
      // todo ... need more here!
    }
    finally
    {
      // terminate all jUDDI Subsystems
      org.juddi.util.SysManager.shutdown();
    }
  }
}
