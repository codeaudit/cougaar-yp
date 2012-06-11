/*
 * jUDDI - An open source Java implementation of UDDI v2.0
 * http://juddi.org/
 *
 * Copyright (c) 2002, InflexionPoint and contributors
 * All rights reserved.
 */

package org.juddi.datastore;

import org.juddi.error.JUDDIException;

import org.uddi4j.datatype.binding.*;
import org.uddi4j.datatype.business.*;
import org.uddi4j.datatype.service.*;
import org.uddi4j.datatype.tmodel.*;
import org.uddi4j.util.*;
import org.uddi4j.response.*;

import java.util.Vector;

/**
 * @author Steve Viens (steve@inflexionpoint.com)
 * @version 0.6.2
 */
public interface DataStore
{  
 /**
  * begin transaction
  */
  public void beginTrans()
    throws org.juddi.error.JUDDIException;

 /**
  * commit transaction
  */
  public void commit()
    throws org.juddi.error.JUDDIException;
  
 /**
  * rollback transaction
  */
  public void rollback()
    throws org.juddi.error.JUDDIException;

  /**
   *
   */
  public void saveBusiness(BusinessEntity business,String publisherID)
    throws org.juddi.error.JUDDIException;

  /**
   *
   */
  public BusinessEntity fetchBusiness(String businessKey)
    throws org.juddi.error.JUDDIException;

  /**
   *
   */
  public void deleteBusiness(String businessKey)
    throws org.juddi.error.JUDDIException;

  /**
   *
   */
  public boolean isBusinessPublisher(String businessKey,String publisherID)
    throws org.juddi.error.JUDDIException;

  /**
   *
   */
  public boolean isValidBusinessKey(String businessKey)
    throws org.juddi.error.JUDDIException;

  /**
   *
   */
  public void saveService(BusinessService service)
    throws org.juddi.error.JUDDIException;


  /**
   *
   */
  public BusinessService fetchService(String serviceKey)
    throws org.juddi.error.JUDDIException;

  /**
   *
   */
  public void deleteService(String serviceKey)
    throws org.juddi.error.JUDDIException;

  /**
   *
   */
  public boolean isValidServiceKey(String serviceKey)
    throws org.juddi.error.JUDDIException;

  /**
   *
   */
  public boolean isServicePublisher(String serviceKey,String publisherID)
    throws org.juddi.error.JUDDIException;

  /**
   *
   */
  public void saveBinding(BindingTemplate binding)
    throws org.juddi.error.JUDDIException;

  /**
   *
   */
  public BindingTemplate fetchBinding(String bindingKey)
    throws org.juddi.error.JUDDIException;

  /**
   *
   */
  public void deleteBinding(String bindingKey)
    throws org.juddi.error.JUDDIException;

  /**
   *
   */
  public boolean isValidBindingKey(String bindingKey)
    throws org.juddi.error.JUDDIException;

  /**
   *
   */
  public boolean isBindingPublisher(String bindingKey,String publisherID)
    throws org.juddi.error.JUDDIException;

  /**
   *
   */
  public void saveTModel(TModel tModel,String publisherID)
    throws org.juddi.error.JUDDIException;

  /**
   *
   */
  public TModel fetchTModel(String tModelKey)
    throws org.juddi.error.JUDDIException;

  /**
   *
   */
  public void deleteTModel(String tModelKey)
    throws org.juddi.error.JUDDIException;

  /**
   *
   */
  public boolean isValidTModelKey(String tModelKey)
    throws org.juddi.error.JUDDIException;

  /**
   *
   */
  public boolean isTModelPublisher(String tModelKey,String publisherID)
    throws org.juddi.error.JUDDIException;

  /**
   *
   */
  public BusinessInfo fetchBusinessInfo(String businessKey)
    throws org.juddi.error.JUDDIException;

  /**
   *
   */
  public ServiceInfo fetchServiceInfo(String serviceKey)
    throws JUDDIException;

  /**
   *
   */
  public TModelInfo fetchTModelInfo(String tModelKey)
    throws JUDDIException;

  /**
   *
   */
  public Vector findBusiness( Vector nameVector,
                              DiscoveryURLs discoveryURLs,
                              IdentifierBag identifierBag,
                              CategoryBag categoryBag,
                              TModelBag tModelBag,
                              FindQualifiers findQualifiers)
    throws org.juddi.error.JUDDIException;

  /**
   *
   */
  public Vector findService(String businessKey,
                            Vector names,
                            CategoryBag categoryBag,
                            TModelBag tModelBag,
                            FindQualifiers findQualifiers)
    throws org.juddi.error.JUDDIException;

  /**
   *
   */
  public Vector findBinding(String serviceKey,
                            TModelBag tModelbag,
                            FindQualifiers findQualifiers)
    throws org.juddi.error.JUDDIException;

  /**
   *
   */
  public Vector findTModel( String name,
                            CategoryBag categoryBag,
                            IdentifierBag identifierBag,
                            FindQualifiers findQualifiers)
    throws org.juddi.error.JUDDIException;

  /**
   *
   */
  public Vector findRelatedBusinesses(String businessKey,KeyedReference keyedReference,FindQualifiers findQualifiers)
    throws org.juddi.error.JUDDIException;

  /**
   *
   */
  public Vector findRegisteredBusinesses(String publisherID)
    throws org.juddi.error.JUDDIException;

  /**
   *
   */
  public Vector findRegisteredTModels(String publisherID)
    throws org.juddi.error.JUDDIException;

  /**
   *
   */
  public void saveAssertions(String publisherID,Vector assertionVector)
    throws org.juddi.error.JUDDIException;

  /**
   *
   */
  public void deleteAssertions(String publisherID,Vector assertionVector)
    throws org.juddi.error.JUDDIException;

  /**
   *
   */
  public Vector getAssertions(String publisherID)
    throws org.juddi.error.JUDDIException;

  /**
   *
   */
  public Vector setAssertions(String publisherID,Vector assertionVector)
    throws org.juddi.error.JUDDIException;

  /**
   *
   */
  public Vector getAssertionStatusItems(String publisherID,String completionStatus)
    throws org.juddi.error.JUDDIException;
}