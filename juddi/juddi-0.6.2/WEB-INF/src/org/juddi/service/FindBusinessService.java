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
import org.uddi4j.datatype.Name;
import org.uddi4j.request.FindBusiness;
import org.uddi4j.response.BusinessList;
import org.uddi4j.response.BusinessInfos;
import org.uddi4j.util.*;
import org.apache.log4j.Logger;

import java.util.Vector;

/**
 * "This [FindBusiness] API call returns a businessList on success. This
 * structure contains information about each matching business, and
 * summaries of the businessServices exposed by the individual businesses.
 * If a tModelBag was used in the search, the resulting serviceInfos
 * structure will only reflect data for the businessServices that
 * actually contained a matching bindingTemplate. In the event that
 * no matches were located for the specified criteria, a businessList
 * structure with zero businessInfo structures is returned. If no
 * arguments are passed, a zero-match result set will be returned."
 *
 * "In the event of a large number of matches, (as determined by each
 * Operator Site), or if the number of matches exceeds the value of the
 * 'maxRows' attribute, the Operator Site will truncate the result set.
 * If this occurs, the businessList will contain the 'truncated' attribute
 * with the value 'true'".
 *
 * From UDDI Version 2 Programmers API Specification (Pg. 18)
 *
 * @author Steve Viens (steve@inflexionpoint.com)
 * @version 0.6.2
 */
public class FindBusinessService extends UDDIService
{
  // private reference to jUDDI Logger
  private static Logger log = Logger.getLogger(FindBusinessService.class);

  // private reference to jUDDI DataStoreFactory
  private static DataStoreFactory factory = DataStoreFactory.getInstance();

  /**
   *
   */
  public UDDIElement invoke(UDDIElement element)
    throws JUDDIException
  {
    FindBusiness request = (FindBusiness)element;
    Vector nameVector = request.getNameVector();
    DiscoveryURLs discoveryURLs = request.getDiscoveryURLs();
    IdentifierBag identifierBag = request.getIdentifierBag();
    CategoryBag categoryBag = request.getCategoryBag();
    TModelBag tModelBag = request.getTModelBag();
    FindQualifiers qualifiers = request.getFindQualifiers();
    String maxRowsString = request.getMaxRows();
    BusinessList list = null;

    // first make sure we need to continue with this request. If
    // no arguments were passed in then we'll simply return
    // an empty BusinessList (aka "a zero match result set").
    if (((nameVector == null)    || (nameVector.size() == 0))    &&
        ((discoveryURLs == null) || (discoveryURLs.size() == 0)) && 
        ((identifierBag == null) || (identifierBag.size() == 0)) && 
        ((categoryBag == null)   || (categoryBag.size() == 0))   &&
        ((tModelBag == null)     || (tModelBag.size() == 0)))
    {
      list = new BusinessList();
      list.setBusinessInfos(new BusinessInfos());
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

    Vector infoVector = null;
    boolean truncatedResults = false;

    try
    {
      dataStore.beginTrans();
      verify(nameVector,discoveryURLs,identifierBag,categoryBag,tModelBag,qualifiers,maxRows,dataStore);
      list = execute(nameVector,discoveryURLs,identifierBag,categoryBag,tModelBag,qualifiers,maxRows,dataStore);
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
  private void verify(Vector nameVector,DiscoveryURLs discoveryURLs,IdentifierBag identifierBag,CategoryBag categoryBag,TModelBag tModelBag,FindQualifiers qualifiers,int maxRows,DataStore dataStore)
    throws JUDDIException
  {
    // validate the 'name' parameters as much as possible up-front before
    // calling into the data layer for relational validation.
    if (nameVector != null)
    {
      // only allowed to specify a maximum of 5 names (implementation
      // dependent).  This value is configurable in jUDDI.
      int maxNameElementsAllowed = Config.getMaxNameElementsAllowed();
      if ((nameVector != null) && (nameVector.size() > maxNameElementsAllowed))
        throw new TooManyOptionsException(
          "A maximum of " +  maxNameElementsAllowed + " Names may be specified in a find_business request.");

      // names can not exceed the maximum character length specified by the
      // UDDI specification (v2.0 specifies a max character length of 255). This
      // value is configurable in jUDDI.
      int maxNameLength = Config.getMaxNameLength();
      for (int i=0; i<nameVector.size(); i++)
      {
        String name = ((Name)nameVector.elementAt(i)).getText();
         if (name.length() > maxNameLength)
          throw new NameTooLongException(
            "Business Entity name '"+name+"' is longer than the maximum allowed (max="+maxNameLength+").");
      }
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
  private BusinessList execute(Vector nameVector,DiscoveryURLs discoveryURLs,IdentifierBag identifierBag,CategoryBag categoryBag,TModelBag tModelBag,FindQualifiers qualifiers,int maxRows,DataStore dataStore)
    throws JUDDIException
  {
    Vector infoVector = null;
    boolean truncatedResults = false;

    // perform the search for matching business entities (returns only business keys in the order requested)
    Vector keyVector = dataStore.findBusiness(nameVector,discoveryURLs,identifierBag,categoryBag,tModelBag,qualifiers);
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

      // iterate through the business entity keys fetching
      // each associated BusinessInfo in sequence.
      infoVector = new Vector(rowCount);
      for (int i=0; i<rowCount; i++)
        infoVector.addElement(dataStore.fetchBusinessInfo((String)keyVector.elementAt(i)));
    }

    // create a new BusinessInfos instance and stuff
    // the new Vector of BusinessInfos into it.
    BusinessInfos infos = new BusinessInfos();
    infos.setBusinessInfoVector(infoVector);

    // create a new BusinessList instance and stuff
    // the new businessInfos instance into it.
    BusinessList list = new BusinessList();
    list.setBusinessInfos(infos);
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
    Vector nameVector = new Vector(5);
    nameVector.addElement(new Name("InflexionPoint"));
    nameVector.addElement(new Name("SteveViens.com"));
    nameVector.addElement(new Name("Liberty Mutual"));
    nameVector.addElement(new Name("Bowstreet"));
    nameVector.addElement(new Name("CMGi"));
    //nameVector.addElement(new Name("BusinessName #6 (1 over the maximum)"));

    Vector qualifierVector = new Vector(1);
    qualifierVector.add(new FindQualifier(FindQualifier.exactNameMatch));
    //qualifierVector.add(new FindQualifier("anInvalidFindQualifier"));

    FindQualifiers qualifiers = new FindQualifiers();
    qualifiers.setFindQualifierVector(qualifierVector);

    Vector categoryVector = new Vector();
    categoryVector.addElement(new KeyedReference("name1","value1"));
    categoryVector.addElement(new KeyedReference("name2","value2"));
    categoryVector.addElement(new KeyedReference("name3","value3"));

    CategoryBag categoryBag = new CategoryBag();
    categoryBag.setKeyedReferenceVector(categoryVector);

    Vector identifierVector = new Vector();
    identifierVector.addElement(new KeyedReference("name1","value1"));
    identifierVector.addElement(new KeyedReference("name1","value1"));
    identifierVector.addElement(new KeyedReference("name1","value1"));

    IdentifierBag identifierBag = new IdentifierBag();
    identifierBag.setKeyedReferenceVector(identifierVector);

    Vector tModelKeyVector = new Vector();
    tModelKeyVector.addElement(new String("6240b6f0-d4dd-4091-851b-d59fedbd0491"));
    tModelKeyVector.addElement(new String("ee0a154b-43ed-47be-b24f-878ab2956a31"));

    TModelBag tModelBag = new TModelBag();
    tModelBag.setTModelKeyStrings(tModelKeyVector);

    Vector discoveryURLVector = new Vector();
    discoveryURLVector.addElement(new DiscoveryURL());
    discoveryURLVector.addElement(new DiscoveryURL());
    discoveryURLVector.addElement(new DiscoveryURL());

    DiscoveryURLs discoveryURLs = new DiscoveryURLs();
    discoveryURLs.setDiscoveryURLVector(discoveryURLVector);

    FindBusiness request = new FindBusiness();
    request.setNameVector(nameVector);
    request.setMaxRows(10);
    request.setCategoryBag(categoryBag);
    request.setIdentifierBag(identifierBag);
    request.setTModelBag(tModelBag);
    request.setDiscoveryURLs(discoveryURLs);
    request.setFindQualifiers(qualifiers);

    try
    {
      // invoke the service
      BusinessList response = (BusinessList)(new FindBusinessService().invoke(request));
      // todo ... need more here!
    }
    finally
    {
      // terminate all jUDDI Subsystems
      org.juddi.util.SysManager.shutdown();
    }
  }
}