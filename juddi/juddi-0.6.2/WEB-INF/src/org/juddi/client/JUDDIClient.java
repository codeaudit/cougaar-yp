/*
 * jUDDI - An open source Java implementation of UDDI v2.0
 * http://juddi.org/
 *
 * Copyright (c) 2002, InflexionPoint and contributors
 * All rights reserved.
 */

package org.juddi.client;

import org.juddi.error.JUDDIException;

import org.juddi.service.*;
import org.uddi4j.request.*;
import org.uddi4j.response.*;
import org.uddi4j.util.*;

import java.util.Vector;

/**
 * Represents a vesion 2.0 UDDI registry and implements all services as specified
 * in the UDDI version 2.0 specification.
 *
 * @author Steve Viens (steve@inflexionpoint.com)
 */
public class JUDDIClient
{
  /**
   * Default constructor.
   */
  public JUDDIClient()
  {
  }

  /**
   * "Used to locate specific bindings within a registered
   *  businessService. Returns a bindingDetail message."
   *
   * @exception JUDDIException
   */
  public BindingDetail find_binding(String serviceKey,TModelBag tModelBag,FindQualifiers findQualifiers,int maxRows)
    throws JUDDIException
  {
    FindBinding findBinding = new FindBinding();
    findBinding.setServiceKey(serviceKey);
    findBinding.setTModelBag(tModelBag);
    findBinding.setFindQualifiers(findQualifiers);
    findBinding.setMaxRows(maxRows);

    return (BindingDetail)new FindBindingService().invoke(findBinding);
  }

  /**
   * Used to locate information about one or more businesses. Returns a
   * businessList message that matches the conditions specified.
   *
   * @exception JUDDIException
   */
  public BusinessList find_business(Vector names,DiscoveryURLs discoveryURLs,IdentifierBag identifierBag,CategoryBag categoryBag,TModelBag tModelBag,FindQualifiers findQualifiers,int maxRows)
    throws JUDDIException
  {
    FindBusiness findBusiness = new FindBusiness();
    findBusiness.setNameVector(names);
    findBusiness.setDiscoveryURLs(discoveryURLs);
    findBusiness.setIdentifierBag(identifierBag);
    findBusiness.setCategoryBag(categoryBag);
    findBusiness.setTModelBag(tModelBag);
    findBusiness.setFindQualifiers(findQualifiers);
    findBusiness.setMaxRows(maxRows);

    return (BusinessList)new FindBusinessService().invoke(findBusiness);
  }

  /**
   * @exception JUDDIException
   */
  public RelatedBusinessesList find_relatedBusinesses(String businessKey,KeyedReference keyedReference,FindQualifiers findQualifiers,int maxRows)
    throws JUDDIException
  {
    FindRelatedBusinesses findRelatedBusinesses = new FindRelatedBusinesses();
    findRelatedBusinesses.setBusinessKey(businessKey);
    findRelatedBusinesses.setKeyedReference(keyedReference);
    findRelatedBusinesses.setFindQualifiers(findQualifiers);
    findRelatedBusinesses.setMaxRows(maxRows);

    return (RelatedBusinessesList)new FindRelatedBusinessesService().invoke(findRelatedBusinesses);
  }

  /**
   * "Used to locate specific services within a registered
   *  businessEntity. Return a serviceList message." From the
   *  XML spec (API, p18) it appears that the name, categoryBag,
   *  and tModelBag arguments are mutually exclusive.
   *
   * @exception JUDDIException
   */
  public ServiceList find_service(String businessKey,Vector names,CategoryBag categoryBag,TModelBag tModelBag,FindQualifiers findQualifiers,int maxRows)
    throws JUDDIException
  {
    FindService findService = new FindService();
    findService.setBusinessKey(businessKey);
    findService.setNameVector(names);
    findService.setCategoryBag(categoryBag);
    findService.setTModelBag(tModelBag);
    findService.setFindQualifiers(findQualifiers);
    findService.setMaxRows(maxRows);

    return (ServiceList)new FindServiceService().invoke(findService);
  }

  /**
   * "Used to locate one or more tModel information structures. Returns a
   *  tModelList structure."
   *
   * @exception JUDDIException
   */
  public TModelList find_tModel(String name,CategoryBag categoryBag,IdentifierBag identifierBag,FindQualifiers findQualifiers,int maxRows)
    throws JUDDIException
  {
    FindTModel findTModel = new FindTModel();
    findTModel.setName(name);
    findTModel.setCategoryBag(categoryBag);
    findTModel.setIdentifierBag(identifierBag);
    findTModel.setFindQualifiers(findQualifiers);
    findTModel.setMaxRows(maxRows);

    return (TModelList)new FindTModelService().invoke(findTModel);
  }

  /**
   * "Used to get full bindingTemplate information suitable for make one
   *  or more service requests. Returns a bindingDetail message."
   *
   * @exception JUDDIException
   */
  public BindingDetail get_bindingDetail(Vector bindingKeyStrings)
    throws JUDDIException
  {
    GetBindingDetail getBindingDetail = new GetBindingDetail();
    getBindingDetail.setBindingKeyStrings(bindingKeyStrings);

    return (BindingDetail)new GetBindingDetailService().invoke(getBindingDetail);
  }

  /**
   * "Used to get the full businessEntity information for one or more
   *  businesses. Returns a businessDetail message."
   *
   * @exception JUDDIException
   */
  public BusinessDetail get_businessDetail(Vector businessKeyStrings)
    throws JUDDIException
  {
    GetBusinessDetail getBusinessDetail = new GetBusinessDetail();
    getBusinessDetail.setBusinessKeyStrings(businessKeyStrings);

    return (BusinessDetail)new GetBusinessDetailService().invoke(getBusinessDetail);
  }

  /**
   * "Used to get extended businessEntity information. Returns a
   *  businessDetailExt message."
   *
   * @exception JUDDIException
   */
  public BusinessDetailExt get_businessDetailExt(Vector businessKeyStrings)
    throws JUDDIException
  {
    GetBusinessDetailExt getBusinessDetailExt = new GetBusinessDetailExt();
    getBusinessDetailExt.setBusinessKeyStrings(businessKeyStrings);

    return (BusinessDetailExt)new GetBusinessDetailExtService().invoke(getBusinessDetailExt);
  }

  /**
   * "Used to get full details for a given set of registered
   *  businessService data. Returns a serviceDetail message."
   *
   * @exception JUDDIException
   */
  public ServiceDetail get_serviceDetail(Vector serviceKeyStrings)
    throws JUDDIException
  {
    GetServiceDetail getServiceDetail = new GetServiceDetail();
    getServiceDetail.setServiceKeyStrings(serviceKeyStrings);

    return (ServiceDetail)new GetServiceDetailService().invoke(getServiceDetail);
  }

  /**
   * "Used to get full details for a given set of registered tModel
   *  data. Returns a tModelDetail message."
   *
   * @exception JUDDIException
   */
  public TModelDetail get_tModelDetail(Vector tModelKeyStrings)
    throws JUDDIException
  {
    GetTModelDetail getTModelDetail = new GetTModelDetail();
    getTModelDetail.setTModelKeyStrings(tModelKeyStrings);

    return (TModelDetail)new GetTModelDetailService().invoke(getTModelDetail);
  }

  /**
   * @exception JUDDIException
   */
  public  DispositionReport add_publisherAssertions(AuthInfo authInfo,Vector publisherAssertions)
    throws JUDDIException
  {
    AddPublisherAssertions addPublisherAssertions = new AddPublisherAssertions();
    addPublisherAssertions.setAuthInfo(authInfo);
    addPublisherAssertions.setPublisherAssertionVector(publisherAssertions);

    return (DispositionReport)new AddPublisherAssertionsService().invoke(addPublisherAssertions);
  }

  /**
   * @exception JUDDIException
   */
  public AssertionStatusReport get_assertionStatusReport(AuthInfo authInfo,String completionStatus)
    throws JUDDIException
  {
    GetAssertionStatusReport getAssertionStatusReport = new GetAssertionStatusReport();
    getAssertionStatusReport.setAuthInfo(authInfo);
    getAssertionStatusReport.setCompletionStatusString(completionStatus);

    return (AssertionStatusReport)new GetAssertionStatusReportService().invoke(getAssertionStatusReport);
  }

  /**
   * @exception JUDDIException
   */
  public PublisherAssertions get_publisherAssertions(AuthInfo authInfo)
    throws JUDDIException
  {
    GetPublisherAssertions getPublisherAssertions = new GetPublisherAssertions();
    getPublisherAssertions.setAuthInfo(authInfo);

    return (PublisherAssertions)new GetPublisherAssertionsService().invoke(getPublisherAssertions);
  }

  /**
   * "Used to remove an existing bindingTemplate from the bindingTemplates
   *  collection that is part of a specified businessService structure."
   *
   * @exception JUDDIException
   */
  public DispositionReport delete_binding(AuthInfo authInfo,Vector bindingKeyStrings)
    throws JUDDIException
  {
    DeleteBinding deleteBinding = new DeleteBinding();
    deleteBinding.setAuthInfo(authInfo);
    deleteBinding.setBindingKeyStrings(bindingKeyStrings);

    return (DispositionReport)new DeleteBindingService().invoke(deleteBinding);
  }

  /**
   * "Used to delete registered businessEntity information from the registry."
   *
   * @exception JUDDIException
   */
  public DispositionReport delete_business(AuthInfo authInfo,Vector businessKeyStrings)
    throws JUDDIException
  {
    DeleteBusiness deleteBusiness = new DeleteBusiness();
    deleteBusiness.setAuthInfo(authInfo);
    deleteBusiness.setBusinessKeyStrings(businessKeyStrings);

    return (DispositionReport)new DeleteBusinessService().invoke(deleteBusiness);
  }

  /**
   * "Used to delete an existing businessService from the businessServices
   *  collection that is part of a specified businessEntity."
   *
   * @exception JUDDIException
   */
  public DispositionReport delete_service(AuthInfo authInfo,Vector serviceKeyStrings)
    throws JUDDIException
  {
    DeleteService deleteService = new DeleteService();
    deleteService.setAuthInfo(authInfo);
    deleteService.setServiceKeyStrings(serviceKeyStrings);

    return (DispositionReport)new DeleteServiceService().invoke(deleteService);
  }

  /**
   * "Used to delete registered information about a tModel.  If there
   *  are any references to a tModel when this call is made, the tModel
   *  will be marked deleted instead of being physically removed."
   *
   * @exception JUDDIException
   */
  public DispositionReport delete_tModel(AuthInfo authInfo,Vector tModelKeyStrings)
    throws JUDDIException
  {
    DeleteTModel deleteTModel = new DeleteTModel();
    deleteTModel.setAuthInfo(authInfo);
    deleteTModel.setTModelKeyStrings(tModelKeyStrings);

    return (DispositionReport)new DeleteServiceService().invoke(deleteTModel);
  }

  /**
   * @exception JUDDIException
   */
  public DispositionReport delete_publisherAssertions(AuthInfo authInfo,Vector publisherAssertions)
    throws JUDDIException
  {
    DeletePublisherAssertions deletePublisherAssertions = new DeletePublisherAssertions();
    deletePublisherAssertions.setAuthInfo(authInfo);
    deletePublisherAssertions.setPublisherAssertionVector(publisherAssertions);

    return (DispositionReport)new DeletePublisherAssertionsService().invoke(deletePublisherAssertions);
  }

  /**
   * "Used to inform an Operator Site that a previously provided
   *  authentication token is no longer valid.  See get_authToken."
   *
   * @exception JUDDIException
   */
  public DispositionReport discard_authToken(AuthInfo authInfo)
    throws JUDDIException
  {
    DiscardAuthToken discardAuthToken = new DiscardAuthToken();
    discardAuthToken.setAuthInfo(authInfo);

    return (DispositionReport)new DiscardAuthTokenService().invoke(discardAuthToken);
  }

  /**
   * "Used to request an authentication token from an Operator Site.
   *  Authentication tokens are required to use all other APIs defined
   *  in the publishers API.  This function serves as the program's
   *  equivalent of a login request."
   *
   * @exception JUDDIException
   */
  public AuthToken get_authToken(String userID,String credentials)
    throws JUDDIException
  {
    GetAuthToken getAuthToken = new GetAuthToken();
    getAuthToken.setUserID(userID);
    getAuthToken.setCred(credentials);

    return (AuthToken)new GetAuthTokenService().invoke(getAuthToken);
  }

  /**
   * "Used to request an abbreviated synopsis of all information currently
   *  managed by a given individual."
   *
   * @exception JUDDIException
   */
  public RegisteredInfo get_registeredInfo(AuthInfo authInfo)
    throws JUDDIException
  {
    GetRegisteredInfo getRegisteredInfo = new GetRegisteredInfo();
    getRegisteredInfo.setAuthInfo(authInfo);

    return (RegisteredInfo)new GetRegisteredInfoService().invoke(getRegisteredInfo);
  }

  /**
   * "Used to register new bindingTemplate information or update existing
   *  bindingTemplate information.  Use this to control information about
   *  technical capabilities exposed by a registered business."
   *
   * @exception JUDDIException
   */
  public BindingDetail save_binding(AuthInfo authInfo,Vector bindingTemplates)
    throws JUDDIException
  {
    SaveBinding saveBinding = new SaveBinding();
    saveBinding.setAuthInfo(authInfo);
    saveBinding.setBindingTemplateVector(bindingTemplates);

    return (BindingDetail)new SaveBindingService().invoke(saveBinding);
  }

  /**
   * "Used to register new businessEntity information or update existing
   *  businessEntity information.  Use this to control the overall
   *  information about the entire business.  Of the save_x APIs this one
   *  has the broadest effect."
   *
   * @exception JUDDIException
   */
  public BusinessDetail save_business(AuthInfo authInfo,Vector businessEntities)
    throws JUDDIException
  {
    SaveBusiness saveBusiness = new SaveBusiness();
    saveBusiness.setAuthInfo(authInfo);
    saveBusiness.setBusinessEntityVector(businessEntities);

    return (BusinessDetail)new SaveBusinessService().invoke(saveBusiness);
  }

  /**
   * "Used to register new businessEntity information or update existing
   *  businessEntity information.  Use this to control the overall
   *  information about the entire business.  Of the save_x APIs this one
   *  has the broadest effect."
   *
   * @exception JUDDIException
   */
  public BusinessDetail save_business(AuthInfo authInfo,UploadRegister[] uploadRegisters)
    throws JUDDIException
  {
    throw new org.juddi.error.UnsupportedException("The save_business service that expects an UploadRegisters " +
      "parameter has been depricated in UDDI version 2.0");
  }

  /**
   * "Used to register or update complete information about a businessService
   *  exposed by a specified businessEntity."
   *
   * @exception JUDDIException
   */
  public ServiceDetail save_service(AuthInfo authInfo,Vector businessServices)
    throws JUDDIException
  {
    SaveService saveService = new SaveService();
    saveService.setAuthInfo(authInfo);
    saveService.setBusinessServiceVector(businessServices);

    return (ServiceDetail)new SaveBusinessService().invoke(saveService);
  }

  /**
   * "Used to register or update complete information about a tModel."
   *
   * @exception JUDDIException
   */
  public TModelDetail save_tModel(AuthInfo authInfo,Vector tModels)
    throws JUDDIException
  {
    SaveTModel saveTModel = new SaveTModel();
    saveTModel.setAuthInfo(authInfo);
    saveTModel.setTModelVector(tModels);

    return (TModelDetail)new SaveTModelService().invoke(saveTModel);
  }

  /**
   * "Used to register or update complete information about a tModel."
   *
   * @exception JUDDIException
   */
  public TModelDetail save_tModel(AuthInfo authInfo,UploadRegister[] uploadRegisters)
    throws JUDDIException
  {
    throw new org.juddi.error.UnsupportedException("The save_tModel service that expects an " +
      "UploadRegisters parameter has been depricated in UDDI version 2.0");
  }

  /**
   * @exception JUDDIException
   */
  public PublisherAssertions set_publisherAssertions(AuthInfo authInfo,Vector publisherAssertions)
    throws JUDDIException
  {
    SetPublisherAssertions setPublisherAssertions = new SetPublisherAssertions();
    setPublisherAssertions.setAuthInfo(authInfo);
    setPublisherAssertions.setPublisherAssertionVector(publisherAssertions);

    return (PublisherAssertions)new SetPublisherAssertionsService().invoke(setPublisherAssertions);
  }

  /**
   *  "Used to..."
   *
   * @exception JUDDIException
   */
  public DispositionReport validate_values(Vector businesses,Vector services,Vector tModels)
    throws JUDDIException
  {
    ValidateValues validateValues = new ValidateValues();
    validateValues.setBusinessEntityVector(businesses);
    validateValues.setBusinessServiceVector(services);
    validateValues.setTModelVector(tModels);

    return (DispositionReport)new ValidateValuesService().invoke(validateValues);
  }

  // test driver
  public static void main(String[] args)
    throws Exception
  {
    // initialize all jUDDI Subsystems
    org.juddi.util.SysManager.startup();

    try
    {
      JUDDIClient client = new JUDDIClient();
      AuthToken authToken = client.get_authToken("sviens","password");
      System.out.println("AuthToken = "+authToken.getAuthInfoString());
    }
    finally
    {
      // terminate all jUDDI Subsystems
      org.juddi.util.SysManager.shutdown();
    }
  }
}