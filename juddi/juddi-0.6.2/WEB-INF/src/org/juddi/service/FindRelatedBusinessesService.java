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
import org.uddi4j.request.FindRelatedBusinesses;
import org.uddi4j.response.RelatedBusinessesList;
import org.uddi4j.response.RelatedBusinessInfos;
import org.uddi4j.util.FindQualifiers;
import org.uddi4j.util.KeyedReference;
import org.apache.log4j.Logger;

import java.util.Vector;

/**
 * @author Steve Viens (steve@inflexionpoint.com)
 * @version 0.6.2
 */
public class FindRelatedBusinessesService extends UDDIService
{
  // private reference to jUDDI Logger
  private static Logger log = Logger.getLogger(FindRelatedBusinessesService.class);

  // private reference to jUDDI DataStoreFactory
  private static DataStoreFactory factory = DataStoreFactory.getInstance();

  /**
   *
   */
  public UDDIElement invoke(UDDIElement element)
    throws JUDDIException
  {
    FindRelatedBusinesses request = (FindRelatedBusinesses)element;
    String businessKey = request.getBusinessKey();
    KeyedReference keyedRef = request.getKeyedReference();
    FindQualifiers findQualifiers = request.getFindQualifiers();
    String maxRowsString = request.getMaxRows();
    RelatedBusinessesList list = null;

    // if a 'maxRows' value has been passed then we'll need
    // to convert it to a primitive int type before using it
    int maxRows = 0;
    if ((maxRowsString != null) && (maxRowsString.trim().length()>0))
      maxRows = Integer.parseInt(maxRowsString);

    // aquire a jUDDI datastore instance
    DataStore dataStore = factory.aquireDataStore();

    try
    {
      dataStore.beginTrans();
      verify(businessKey,keyedRef,findQualifiers,maxRows,dataStore);  // verify every aspect of the request
      list = execute(businessKey,keyedRef,findQualifiers,maxRows,dataStore);  // passed verification, let's execute
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

    return list;
  }

  /**
   *
   */
  private void verify(String businessKey,KeyedReference keyedReference,FindQualifiers findQualifiers,int maxRows,DataStore dataStore)
    throws JUDDIException
  {
    return; // nothing to verify yet
  }

  /**
   *
   */
  private RelatedBusinessesList execute(String businessKey,KeyedReference keyedReference,FindQualifiers findQualifiers,int maxRows,DataStore dataStore)
    throws JUDDIException
  {
    Vector infoVector = null;
    boolean truncatedResults = false;

    // perform the search for matching business entities (return only keys in requested order)
    Vector keyVector = dataStore.findRelatedBusinesses(businessKey,keyedReference,findQualifiers);
    if ((keyVector != null) && (keyVector.size() > 0))
    {
      // if the number of keys returned is greater than maxRows then truncate the results.
      int rowCount = keyVector.size();
      if ((maxRows > 0) && (maxRows < rowCount))
      {
        rowCount = maxRows;
        truncatedResults = true;
      }

      // iterate through the business entity keys fetching each associated BusinessInfo.
      infoVector = new Vector(rowCount);
      for (int i=0; i<rowCount; i++)
        infoVector.addElement(dataStore.fetchBusinessInfo((String)keyVector.elementAt(i)));
    }

    // create a new BusinessInfos instance and stuff
    // the new Vector of BusinessInfos into it.
    RelatedBusinessInfos infos = new RelatedBusinessInfos();
    infos.setRelatedBusinessInfoVector(infoVector);

    // create a new RelatedBusinessesList instance and
    // stuff the new relatedBusinessInfoVector into it.
    RelatedBusinessesList list = new RelatedBusinessesList();
    list.setOperator(operator);
    list.setTruncated(truncatedResults);
    list.setRelatedBusinessInfos(infos);

    return list;
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
    FindRelatedBusinesses request = new FindRelatedBusinesses();
    // todo ... need more here!

    try
    {
      // invoke the service
      RelatedBusinessesList response = (RelatedBusinessesList)(new FindRelatedBusinessesService().invoke(request));
      // todo ... need more here!
    }
    finally
    {
      // terminate all jUDDI Subsystems
      org.juddi.util.SysManager.shutdown();
    }
  }
}

