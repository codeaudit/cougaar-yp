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
import org.juddi.util.Config;

import org.uddi4j.UDDIElement;
import org.uddi4j.response.TModelList;
import org.uddi4j.response.TModelInfos;
import org.uddi4j.request.FindTModel;
import org.uddi4j.util.CategoryBag;
import org.uddi4j.util.FindQualifiers;
import org.uddi4j.util.IdentifierBag;
import org.uddi4j.util.FindQualifier;
import org.apache.log4j.Logger;

import java.util.Vector;

/**
 * @author Steve Viens (steve@inflexionpoint.com)
 * @version 0.6.2
 */
public class FindTModelService extends UDDIService
{
  // private reference to jUDDI Logger
  private static Logger log = Logger.getLogger(FindTModelService.class);

  // private reference to jUDDI DataStoreFactory
  private static DataStoreFactory factory = DataStoreFactory.getInstance();

  /**
   *
   */
  public UDDIElement invoke(UDDIElement element)
    throws JUDDIException
  {
    FindTModel request = (FindTModel)element;
    CategoryBag categoryBag = request.getCategoryBag();
    IdentifierBag identifierBag = request.getIdentifierBag();
    FindQualifiers qualifiers = request.getFindQualifiers();
    String maxRowsString = request.getMaxRows();
    TModelList list = null;

    // LLW: make sure name is not null before asking for the string
    // the request object will throw a NULL pointer exception.
    // This should really be done within FindTModel.getNameString().
    String name = null;
    if ( request.getName() != null )
      name = request.getNameString();

    // first make sure we need to continue with this request. If
    // no arguments were passed in then we'll simply return
    // an empty TModelList (aka "a zero match result set").
    if (((name == null)          || (name.length() == 0))    &&
        ((identifierBag == null) || (identifierBag.size() == 0)) &&
        ((categoryBag == null)   || (categoryBag.size() == 0)))
    {
      list = new TModelList();
      list.setTModelInfos(new TModelInfos());
      list.setOperator(operator);
      list.setTruncated(false);
      return list;
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
      verify(name,identifierBag,categoryBag,qualifiers,maxRows,dataStore);
      list = execute(name,identifierBag,categoryBag,qualifiers,maxRows,dataStore);
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
  private void verify(String name,IdentifierBag identifierBag,CategoryBag categoryBag,FindQualifiers qualifiers,int maxRows,DataStore dataStore)
    throws JUDDIException
  {
    // validate the 'name' parameters as much as possible up-front before
    // calling into the data layer for relational validation.
    if (name != null)
    {
      // names can not exceed the maximum character length specified by the
      // UDDI specification (v2.0 specifies a max character length of 255). This
      // value is configurable in jUDDI.
      int maxNameLength = Config.getMaxNameLength();
      if (name.length() > maxNameLength)
        throw new NameTooLongException(
          "TModel Name element '"+name+"' is longer than the maximum allowed (max="+maxNameLength+").");
    }

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
  private TModelList execute(String name,IdentifierBag identifierBag,CategoryBag categoryBag,FindQualifiers qualifiers,int maxRows,DataStore dataStore)
    throws JUDDIException
  {
    Vector infoVector = null;
    boolean truncatedResults = false;

    // perform the search for matching technical models (return only keys in requested order)
    Vector keyVector = dataStore.findTModel(name,categoryBag,identifierBag,qualifiers);
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

      // iterate through the technical model keys fetching
      // each associated TModelInfo in sequence.
      infoVector = new Vector(rowCount);
      for (int i=0; i<rowCount; i++)
        infoVector.addElement(dataStore.fetchTModelInfo((String)keyVector.elementAt(i)));
    }

    // create a new ServiceInfos instance and stuff
    // the new Vector of ServiceInfos into it.
    TModelInfos infos = new TModelInfos();
    infos.setTModelInfoVector(infoVector);

    // create a new TModelList instance and
    // stuff the new tModelInfoVector into it.
    TModelList list = new TModelList();
    list.setTModelInfos(infos);
    list.setOperator(operator);
    list.setTruncated(truncatedResults);

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
    FindTModel request = new FindTModel();
    // todo ... need more here!

    try
    {
      // invoke the service
      TModelList response = (TModelList)(new FindTModelService().invoke(request));
      // todo ... need more here!
    }
    finally
    {
      // terminate all jUDDI Subsystems
      org.juddi.util.SysManager.shutdown();
    }
  }
}

