/*
 * <copyright>
 *  Copyright 2002 BBNT Solutions, LLC
 *  under sponsorship of the Defense Advanced Research Projects Agency (DARPA).
 * 
 *  This program is free software; you can redistribute it and/or modify
 *  it under the terms of the Cougaar Open Source License as published by
 *  DARPA on the Cougaar Open Source Website (www.cougaar.org).
 * 
 *  THE COUGAAR SOFTWARE AND ANY DERIVATIVE SUPPLIED BY LICENSOR IS
 *  PROVIDED 'AS IS' WITHOUT WARRANTIES OF ANY KIND, WHETHER EXPRESS OR
 *  IMPLIED, INCLUDING (BUT NOT LIMITED TO) ALL IMPLIED WARRANTIES OF
 *  MERCHANTABILITY AND FITNESS FOR A PARTICULAR PURPOSE, AND WITHOUT
 *  ANY WARRANTIES AS TO NON-INFRINGEMENT.  IN NO EVENT SHALL COPYRIGHT
 *  HOLDER BE LIABLE FOR ANY DIRECT, SPECIAL, INDIRECT OR CONSEQUENTIAL
 *  DAMAGES WHATSOEVER RESULTING FROM LOSS OF USE OF DATA OR PROFITS,
 *  TORTIOUS CONDUCT, ARISING OUT OF OR IN CONNECTION WITH THE USE OR
 *  PERFORMANCE OF THE COUGAAR SOFTWARE.
 * </copyright>
 */

package org.cougaar.yp;

// jaxr
import javax.xml.registry.*;
import javax.xml.registry.infomodel.*;

import java.io.*;
import java.net.*;
import java.util.*;

public final class YPQueryImpl implements YPQuery {
  /** Any additional properties required (or understood) by the server. **/
  private Properties properties = null;
  
  static final int Q_invalid = -1;
  // BusinessQueryManager
  static final int Q_findAssociations = 10;
  static final int Q_findCallerAssociations = 11;
  static final int Q_findClassificationSchemeByName = 12;
  static final int Q_findClassificationSchemes = 13;
  static final int Q_findConceptByPath = 14;
  static final int Q_findConcepts = 15;
  static final int Q_findOrganizations = 16;
  static final int Q_findRegistryPackages = 17;
  static final int Q_findServiceBindings = 18;
  static final int Q_findServices = 19;
  // QueryManager
  static final int Q_getRegistryObject_String = 50;
  static final int Q_getRegistryObject_StringString = 51;
  static final int Q_getRegistryObjects = 52;
  static final int Q_getRegistryObjects_Collection = 53;
  static final int Q_getRegistryObjects_CollectionString = 54;
  static final int Q_getRegistryObjects_String = 55;
  static final int Q_getRegistryService = 56;
  
  /** This is the actual query key, one for each query method **/
  private int query = Q_invalid;

  /** arguments to the JAXR query **/
  private Object[] args = null;

  /** Package private **/
  YPQueryImpl() { }

  //
  // YPQuery-specific accessors
  //

  public void addProperties(Properties props) {
    synchronized (this) {
      if (properties == null) {
        properties = new Properties(props);
      } else {
        properties.putAll(props);
      }
    }
  }

  public Properties getProperties() {
    return properties;
  }

  //
  // BusinessQueryManager API
  //

  public BulkResponse findAssociations(Collection findQualifiers, String sourceObjectId, String targetObjectId, Collection associationTypes) throws JAXRException {
    query = Q_findAssociations;
    throw new JAXRException("Operation unimplemented");
  }

  public BulkResponse findCallerAssociations(Collection findQualifiers, Boolean confirmedByCaller, Boolean confirmedByOtherParty, Collection associationTypes) throws JAXRException {
    query = Q_findCallerAssociations;
    throw new JAXRException("Operation unimplemented");
  }

  public ClassificationScheme findClassificationSchemeByName(Collection findQualifiers, String namePattern) throws JAXRException {
    query = Q_findClassificationSchemeByName;
    throw new JAXRException("Operation unimplemented");
  }

  public BulkResponse findClassificationSchemes(Collection findQualifiers, Collection namePatterns, Collection classifications, Collection externalLinks) throws JAXRException {
    query = Q_findClassificationSchemes;
    throw new JAXRException("Operation unimplemented");
  }

  public Concept findConceptByPath(String path) throws JAXRException {
    query = Q_findConceptByPath;
    throw new JAXRException("Operation unimplemented");
  }

  public BulkResponse findConcepts(Collection findQualifiers, Collection namePatterns, Collection classifications, Collection externalIdentifiers, Collection externalLinks) throws JAXRException {
    query = Q_findConcepts;
    throw new JAXRException("Operation unimplemented");
  }

  public BulkResponse findOrganizations(Collection findQualifiers, Collection namePatterns, Collection classifications, Collection specifications, Collection externalIdentifiers, Collection externalLinks) {
    query = Q_findOrganizations;
    args = new Object[6];
    args[0]=findQualifiers;
    args[1]=namePatterns;
    args[2]=classifications;
    args[3]=specifications;
    args[4]=externalIdentifiers;
    args[5]=externalLinks;
    return null;
  }

  public BulkResponse findRegistryPackages(Collection findQualifiers, Collection namePatterns, Collection classifications, Collection externalLinks) throws JAXRException {
    query = Q_findRegistryPackages;
    throw new JAXRException("Operation unimplemented");
  }

  public BulkResponse findServiceBindings(Key serviceKey, Collection findQualifiers, Collection classifications, Collection specifications) throws JAXRException {
    query = Q_findServiceBindings;
    throw new JAXRException("Operation unimplemented");
  }

  public BulkResponse findServices(Key orgKey, Collection findQualifiers, Collection namePatterns, Collection classifications, Collection specifications) throws JAXRException {
    query = Q_findServices;
    throw new JAXRException("Operation unimplemented");
  }
 
  //
  // QueryManager API
  //

  public RegistryObject getRegistryObject(String id) throws JAXRException {
    query = Q_getRegistryObject_String;
    throw new JAXRException("Operation unimplemented");
  }

  public RegistryObject getRegistryObject(String id, String objectType) throws JAXRException {
    query = Q_getRegistryObject_StringString;
    throw new JAXRException("Operation unimplemented");
  }

  public BulkResponse getRegistryObjects() throws JAXRException {
    query = Q_getRegistryObjects;
    throw new JAXRException("Operation unimplemented");
  }

  public BulkResponse getRegistryObjects(Collection objectKeys) throws JAXRException {
    query = Q_getRegistryObjects_Collection;
    throw new JAXRException("Operation unimplemented");
  }

  public BulkResponse getRegistryObjects(Collection objectKeys, String objectTypes) throws JAXRException {
    query = Q_getRegistryObjects_CollectionString;
    throw new JAXRException("Operation unimplemented");
  }

  public BulkResponse getRegistryObjects(String objectType) throws JAXRException {
    query = Q_getRegistryObjects_String;
    throw new JAXRException("Operation unimplemented");
  }

  public RegistryService getRegistryService() throws JAXRException {
    query = Q_getRegistryService;
    throw new JAXRException("Operation unimplemented");
  }

  //
  // api for executing a query against a real service
  //

  /** Execute a YPQuery against a real JAXR query manager **/
  public YPResponse execute(BusinessQueryManager bqm) {
    try {
      switch (query) {
        // ugh - if ever there was a good argument for macros...
      case Q_findAssociations:
        return YPResponseWrapper.wrapBulkResponse(bqm.findAssociations((Collection)args[0], (String)args[1], (String)args[2], (Collection)args[3]));
      case Q_findCallerAssociations:
        return YPResponseWrapper.wrapBulkResponse(bqm.findCallerAssociations((Collection)args[0], (Boolean)args[1], (Boolean)args[2], (Collection)args[3]));
      case Q_findClassificationSchemeByName:
        return YPResponseWrapper.wrapClassificationScheme(bqm.findClassificationSchemeByName((Collection)args[0], (String)args[1]));
      case Q_findClassificationSchemes:
        return YPResponseWrapper.wrapBulkResponse(bqm.findClassificationSchemes((Collection)args[0], (Collection)args[1], (Collection)args[2], (Collection)args[3]));
      case Q_findConceptByPath:
        return YPResponseWrapper.wrapConcept(bqm.findConceptByPath((String)args[0]));
      case Q_findConcepts:
        return YPResponseWrapper.wrapBulkResponse(bqm.findConcepts((Collection)args[0], (Collection)args[1], (Collection)args[2], (Collection)args[3], (Collection)args[4]));
      case Q_findOrganizations:
        return YPResponseWrapper.wrapBulkResponse(bqm.findOrganizations((Collection)args[0], (Collection)args[1], (Collection)args[2], (Collection)args[3], (Collection)args[4], (Collection)args[5]));
      case Q_findRegistryPackages:
        return YPResponseWrapper.wrapBulkResponse(bqm.findRegistryPackages((Collection)args[0], (Collection)args[1], (Collection)args[2], (Collection)args[3]));
      case Q_findServiceBindings:
        return YPResponseWrapper.wrapBulkResponse(bqm.findServiceBindings((Key)args[0], (Collection)args[1], (Collection)args[2], (Collection)args[3]));
      case Q_findServices:
        return YPResponseWrapper.wrapBulkResponse(bqm.findServices((Key)args[0], (Collection)args[1], (Collection)args[2], (Collection)args[3], (Collection)args[4]));
      case Q_getRegistryObject_String:
        return YPResponseWrapper.wrapRegistryObject(bqm.getRegistryObject((String)args[0]));
      case Q_getRegistryObject_StringString:
        return YPResponseWrapper.wrapRegistryObject(bqm.getRegistryObject((String)args[0], (String)args[1]));
      case Q_getRegistryObjects:
        return YPResponseWrapper.wrapBulkResponse(bqm.getRegistryObjects());
      case Q_getRegistryObjects_Collection:
        return YPResponseWrapper.wrapBulkResponse(bqm.getRegistryObjects((Collection)args[0]));
      case Q_getRegistryObjects_CollectionString:
        return YPResponseWrapper.wrapBulkResponse(bqm.getRegistryObjects((Collection)args[0], (String)args[1]));
      case Q_getRegistryObjects_String:
        return YPResponseWrapper.wrapBulkResponse(bqm.getRegistryObjects((String)args[0]));
      case Q_getRegistryService:
        return YPResponseWrapper.wrapRegistryService(bqm.getRegistryService());
      default:
        throw new JAXRException("Unknown query type "+query);
      }
    } catch (JAXRException e) {
      return YPResponseWrapper.wrapJAXRException(e);
    }
  }

}
