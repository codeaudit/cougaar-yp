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

public final class YPQueryImpl implements YPQuery, Serializable {
  private Properties properties = null;

  /** Package private **/
  YPQueryImpl() {
  }

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

  public BulkResponse findAssociations(Collection findQualifiers, String sourceObjectId, String targetObjectId, Collection associationTypes) {
    return null;
  }

  public BulkResponse findCallerAssociations(Collection findQualifiers, Boolean confirmedByCaller, Boolean confirmedByOtherParty, Collection associationTypes) {
    return null;
  }

  public ClassificationScheme findClassificationSchemeByName(Collection findQualifiers, String namePattern) {
    return null;
  }

  public BulkResponse findClassificationSchemes(Collection findQualifiers, Collection namePatterns, Collection classifications, Collection externalLinks) {
    return null;
  }

  public Concept findConceptByPath(String path) {
    return null;
  }

  public BulkResponse findConcepts(Collection findQualifiers, Collection namePatterns, Collection classifications, Collection externalIdentifiers, Collection externalLinks) {
    return null;
  }

  public BulkResponse findOrganizations(Collection findQualifiers, Collection namePatterns, Collection classifications, Collection specifications, Collection externalIdentifiers, Collection externalLinks) {
    return null;
  }

  public BulkResponse findRegistryPackages(Collection findQualifiers, Collection namePatterns, Collection classifications, Collection externalLinks) {
    return null;
  }

  public BulkResponse findServiceBindings(Key serviceKey, Collection findQualifiers, Collection classifications, Collection specifications) {
    return null;
  }

  public BulkResponse findServices(Key orgKey, Collection findQualifiers, Collection namePatterns, Collection classifications, Collection specifications) {
    return null;
  }
 
  //
  // QueryManager API
  //

  public RegistryObject getRegistryObject(String id) {
    return null;
  }

  public RegistryObject getRegistryObject(String id, String objectType) {
    return null;
  }

  public BulkResponse getRegistryObjects() {
    return null;
  }

  public BulkResponse getRegistryObjects(Collection objectKeys) {
    return null;
  }

  public BulkResponse getRegistryObjects(Collection objectKeys, String objectTypes) {
    return null;
  }

  public BulkResponse getRegistryObjects(String objectType) {
    return null;
  }

  public RegistryService getRegistryService() {
    return null;
  }

}
