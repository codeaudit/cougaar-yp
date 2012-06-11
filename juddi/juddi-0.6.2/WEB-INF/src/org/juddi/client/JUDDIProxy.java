/*
 * jUDDI - An open source Java implementation of UDDI v2.0
 * http://juddi.org/
 *
 * Copyright (c) 2002, InflexionPoint and contributors
 * All rights reserved.
 */

package org.juddi.client;

import org.juddi.error.*;
import org.juddi.service.*;

import org.uddi4j.UDDIElement;
import org.uddi4j.UDDIException;
import org.uddi4j.client.UDDIProxy;
import org.uddi4j.request.*;
import org.uddi4j.response.*;
import org.uddi4j.transport.TransportException;
import org.uddi4j.util.*;
import org.w3c.dom.Document;
import org.w3c.dom.Element;

import java.net.MalformedURLException;
import java.net.URL;
import java.util.Properties;
import java.util.Vector;
import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;

/**
 *
 * @author Lance Waterman
 * @author Steve Viens (steve@inflexionpoint.com)
 */
public class JUDDIProxy extends UDDIProxy
{
	/**
	 * Constructor for JUDDIProxy.
	 */
	public JUDDIProxy()
  {
		super();
	}

	/**
	 * Constructor for JUDDIProxy.
	 * @param p
	 */
	public JUDDIProxy(Properties p)
    throws MalformedURLException
  {
		super(p);
	}

  /**
   * This method is not used by JUDDIProxy. Since the JUDDIProxy
   * does not connect to a SOAP service, there are no configuration
   * properties to set.
   */
  public void setConfiguration(Properties p)
    throws MalformedURLException
  {
    // Quietly disregard
  }

  /**
   * This method is not used by JUDDIProxy. Since the JUDDIProxy
   * does not connect to a SOAP service, there are no configuration
   * properties to get.
   */
  public Properties getConfiguration()
  {
    // Quietly disregard
    return null;
  }

  /**
   * This method is not used by JUDDIProxy. Since the JUDDIProxy
   * does not connect to a SOAP service, there is no URL to set
   */
  public void setInquiryURL(String url)
    throws MalformedURLException
  {
    // Quietly disregard
  }

  /**
   * This method is not used by JUDDIProxy. Since the JUDDIProxy
   * does not connect to a SOAP service, there is no URL to set
   */
  public void setInquiryURL(URL url)
  {
    // Quietly diregard
  }

  /**
   * This method is not used by JUDDIProxy. Since the JUDDIProxy
   * does not connect to a SOAP service, there is no URL to set
   */
  public void setPublishURL(String url)
    throws java.net.MalformedURLException
  {
    // Quietly disregard
  }

  /**
   * This method is not used by JUDDIProxy. Since the JUDDIProxy
   * does not connect to a SOAP service, there is no URL to set
   */
  public void setPublishURL(URL url)
  {
    // Quietly disregard
  }

  /**
   * @see org.uddi4j.client.UDDIProxy#find_binding(FindQualifiers, String, TModelBag, int)
   */
  public BindingDetail find_binding(  FindQualifiers findQualifiers,
                                      String serviceKey,
                                      TModelBag tModelBag,
                                      int maxRows)
    throws UDDIException, TransportException
  {
    FindBinding request = new FindBinding();
    request.setFindQualifiers(findQualifiers);
    request.setServiceKey(serviceKey);
    request.setTModelBag(tModelBag);

    if (maxRows>0)
      request.setMaxRows(maxRows);

    return (BindingDetail)send(request);
  }

  /**
   * @see org.uddi4j.client.UDDIProxy#find_business(Vector, DiscoveryURLs, IdentifierBag, CategoryBag, TModelBag, FindQualifiers, int)
   */
  public BusinessList find_business( Vector names,
                                     DiscoveryURLs discoveryURLs,
                                     IdentifierBag identifierBag,
                                     CategoryBag categoryBag,
                                     TModelBag tModelBag,
                                     FindQualifiers findQualifiers,
                                     int maxRows)
    throws UDDIException, TransportException
  {
    FindBusiness request = new FindBusiness();
    request.setNameVector(names);
    request.setDiscoveryURLs(discoveryURLs);
    request.setIdentifierBag(identifierBag);
    request.setCategoryBag(categoryBag);
    request.setTModelBag(tModelBag);
    request.setFindQualifiers(findQualifiers);

    if (maxRows>0)
      request.setMaxRows(maxRows);

    return (BusinessList)send(request);
  }

  /**
   * @see org.uddi4j.client.UDDIProxy#find_relatedBusinesses(String, KeyedReference, FindQualifiers, int)
   */
  public RelatedBusinessesList find_relatedBusinesses( String businessKey,
                                                       KeyedReference keyedReference,
                                                       FindQualifiers findQualifiers,
                                                       int maxRows)
    throws UDDIException, TransportException
  {
    FindRelatedBusinesses request = new FindRelatedBusinesses();
    request.setBusinessKey(businessKey);
    request.setKeyedReference(keyedReference);
    request.setFindQualifiers(findQualifiers);

    if (maxRows>0)
      request.setMaxRows(maxRows);

    return (RelatedBusinessesList)send(request);
  }

	/**
	 * @see org.uddi4j.client.UDDIProxy#find_service(String, Vector, CategoryBag, TModelBag, FindQualifiers, int)
	 */
  public ServiceList find_service(  String businessKey,
                                    Vector names,
                                    CategoryBag categoryBag,
                                    TModelBag  tModelBag,
                                    FindQualifiers findQualifiers,
                                    int maxRows)
    throws UDDIException, TransportException
  {
    FindService request = new FindService();
    request.setBusinessKey(businessKey);
    request.setNameVector(names);
    request.setCategoryBag(categoryBag);
    request.setTModelBag(tModelBag);
    request.setFindQualifiers(findQualifiers);

    if (maxRows>0)
      request.setMaxRows(maxRows);

    return (ServiceList)send(request);
  }

  /**
   * @see org.uddi4j.client.UDDIProxy#find_tModel(String, CategoryBag, IdentifierBag, FindQualifiers, int)
   */
  public TModelList find_tModel(  String name,
                                  CategoryBag categoryBag,
                                  IdentifierBag identifierBag,
                                  FindQualifiers findQualifiers,
                                  int maxRows)
    throws UDDIException, TransportException
  {
    FindTModel request = new FindTModel();
    request.setName(name);
    request.setCategoryBag(categoryBag);
    request.setIdentifierBag(identifierBag);
    request.setFindQualifiers(findQualifiers);

    if (maxRows>0)
      request.setMaxRows(maxRows);

    return (TModelList)send(request);
  }

  /**
   * @see org.uddi4j.client.UDDIProxy#get_bindingDetail(String)
   */
  public BindingDetail get_bindingDetail(String bindingKey)
    throws UDDIException, TransportException
  {
    Vector keys = new Vector();
    keys.addElement(bindingKey);

	  return get_bindingDetail(keys);
  }

  /**
   * @see org.uddi4j.client.UDDIProxy#get_bindingDetail(Vector)
   */
  public BindingDetail get_bindingDetail(Vector bindingKeyStrings)
    throws UDDIException, TransportException
  {
    GetBindingDetail request = new GetBindingDetail();
    request.setBindingKeyStrings(bindingKeyStrings);

    return (BindingDetail)send(request);
  }

  /**
   * @see org.uddi4j.client.UDDIProxy#get_businessDetail(String)
   */
  public BusinessDetail get_businessDetail(String businessKey)
    throws UDDIException, TransportException
  {
    Vector keys = new Vector();
    keys.addElement(businessKey);

	  return get_businessDetail(keys);
  }

  /**
   * @see org.uddi4j.client.UDDIProxy#get_businessDetail(Vector)
   */
  public BusinessDetail get_businessDetail(Vector businessKeyStrings)
    throws UDDIException, TransportException
  {
    GetBusinessDetail request = new GetBusinessDetail();
    request.setBusinessKeyStrings(businessKeyStrings);

    return (BusinessDetail)send(request);
  }

  /**
   * @see org.uddi4j.client.UDDIProxy#get_businessDetailExt(String)
   */
  public BusinessDetailExt get_businessDetailExt(String businessKey)
    throws UDDIException, TransportException
  {
    Vector keys = new Vector();
    keys.addElement(businessKey);

    return get_businessDetailExt(keys);
  }

  /**
   * @see org.uddi4j.client.UDDIProxy#get_businessDetailExt(Vector)
   */
  public BusinessDetailExt get_businessDetailExt(Vector businessKeyStrings)
    throws UDDIException, TransportException
  {
    GetBusinessDetailExt request = new GetBusinessDetailExt();
    request.setBusinessKeyStrings(businessKeyStrings);

    return (BusinessDetailExt)send(request);
  }

  /**
   * @see org.uddi4j.client.UDDIProxy#get_serviceDetail(String)
   */
  public ServiceDetail get_serviceDetail(String serviceKey)
    throws UDDIException, TransportException
  {
    Vector keys = new Vector();
    keys.addElement(serviceKey);

    return get_serviceDetail(keys);
  }

  /**
   * @see org.uddi4j.client.UDDIProxy#get_serviceDetail(Vector)
   */
  public ServiceDetail get_serviceDetail(Vector serviceKeyStrings)
    throws UDDIException, TransportException
  {
    GetServiceDetail request = new GetServiceDetail();
    request.setServiceKeyStrings(serviceKeyStrings);

    return (ServiceDetail)send(request);
  }

  /**
   * @see org.uddi4j.client.UDDIProxy#get_tModelDetail(String)
   */
  public TModelDetail get_tModelDetail(String tModelKey)
    throws UDDIException, TransportException
  {
    Vector keys = new Vector();
    keys.addElement(tModelKey);

    return get_tModelDetail(keys);
  }

  /**
   * @see org.uddi4j.client.UDDIProxy#get_tModelDetail(Vector)
   */
  public TModelDetail get_tModelDetail(Vector tModelKeyStrings)
   throws UDDIException, TransportException
  {
    GetTModelDetail request = new GetTModelDetail();
    request.setTModelKeyStrings(tModelKeyStrings);

    return (TModelDetail)send(request);
  }

  /**
   * @see org.uddi4j.client.UDDIProxy#add_publisherAssertions(String, Vector)
   */
  public  DispositionReport add_publisherAssertions(String authInfo,Vector publisherAssertion)
    throws UDDIException, TransportException
  {
    AddPublisherAssertions request = new  AddPublisherAssertions();
    request.setAuthInfo(authInfo);
    request.setPublisherAssertionVector(publisherAssertion);

	  return (DispositionReport)send(request);
  }

  /**
   * @see org.uddi4j.client.UDDIProxy#get_assertionStatusReport(String, CompletionStatus)
   */
  public AssertionStatusReport get_assertionStatusReport(String authInfo,CompletionStatus completionStatus)
    throws UDDIException, TransportException
  {
    GetAssertionStatusReport request = new GetAssertionStatusReport();
    request.setAuthInfo(authInfo);
    request.setCompletionStatus(completionStatus);

    return (AssertionStatusReport)send(request);
  }

  /**
   * @see org.uddi4j.client.UDDIProxy#get_publisherAssertions(String)
   */
  public PublisherAssertions get_publisherAssertions(String authInfo)
    throws UDDIException, TransportException
  {
    GetPublisherAssertions request = new GetPublisherAssertions();
    request.setAuthInfo(authInfo);

    return (PublisherAssertions)send(request);
  }

  /**
   * @see org.uddi4j.client.UDDIProxy#delete_binding(String, Vector)
   */
  public DispositionReport delete_binding(String authInfo,String bindingKey)
    throws UDDIException, TransportException
  {
    Vector keys = new Vector();
    keys.addElement(bindingKey);

    return delete_binding(authInfo,keys);
  }

  /**
   * @see org.uddi4j.client.UDDIProxy#delete_binding(String, Vector)
   */
  public DispositionReport delete_binding(String authInfo,Vector bindingKeyStrings)
    throws UDDIException, TransportException
  {
    DeleteBinding request = new DeleteBinding();
    request.setAuthInfo(authInfo);
    request.setBindingKeyStrings(bindingKeyStrings);

    return (DispositionReport)send(request);
  }

  /**
   * @see org.uddi4j.client.UDDIProxy#delete_business(String, String)
   */
  public DispositionReport delete_business(String authInfo,String businessKey)
    throws UDDIException, TransportException
  {
    Vector keys = new Vector();
    keys.addElement(businessKey);

    return delete_business(authInfo,keys);
  }

  /**
   * @see org.uddi4j.client.UDDIProxy#delete_business(String, Vector)
   */
  public DispositionReport delete_business(String authInfo,Vector businessKeyStrings)
    throws UDDIException, TransportException
  {
    DeleteBusiness request = new DeleteBusiness();
    request.setAuthInfo(authInfo);
    request.setBusinessKeyStrings(businessKeyStrings);

    return (DispositionReport)send(request);
  }

  /**
   * @see org.uddi4j.client.UDDIProxy#delete_service(String, String)
   */
  public DispositionReport delete_service(String authInfo,String serviceKey)
    throws UDDIException, TransportException
  {
    Vector keys = new Vector();
    keys.addElement(serviceKey);

    return delete_service(authInfo,keys);
  }

  /**
   * @see org.uddi4j.client.UDDIProxy#delete_service(String, Vector)
   */
  public DispositionReport delete_service(String authInfo,Vector serviceKeyStrings)
    throws UDDIException, TransportException
  {
    DeleteService request = new DeleteService();
    request.setAuthInfo(authInfo);
    request.setServiceKeyStrings(serviceKeyStrings);

    return (DispositionReport)send(request);
  }

  /**
   * @see org.uddi4j.client.UDDIProxy#delete_tModel(String, String)
   */
  public DispositionReport delete_tModel(String authInfo,String tModelKey)
    throws UDDIException, TransportException
  {
    Vector keys = new Vector();
    keys.addElement(tModelKey);

    return delete_tModel(authInfo,keys);
  }

  /**
   * @see org.uddi4j.client.UDDIProxy#delete_tModel(String, Vector)
   */
  public DispositionReport delete_tModel(String authInfo,Vector tModelKeyStrings)
    throws UDDIException, TransportException
  {
    DeleteTModel request = new DeleteTModel();
    request.setAuthInfo(authInfo);
    request.setTModelKeyStrings(tModelKeyStrings);

    return (DispositionReport)send(request);
  }

  /**
   * @see org.uddi4j.client.UDDIProxy#delete_publisherAssertions(String, Vector)
   */
  public DispositionReport delete_publisherAssertions(String authInfo,Vector publisherAssertion)
     throws UDDIException, TransportException
  {
    DeletePublisherAssertions request = new DeletePublisherAssertions();
    request.setAuthInfo(authInfo);
    request.setPublisherAssertionVector(publisherAssertion);

 	  return (DispositionReport)send(request);
  }

  /**
   * @see org.uddi4j.client.UDDIProxy#discard_authToken(String)
   */
  public DispositionReport discard_authToken(String authInfo)
    throws UDDIException, TransportException
  {
    return discard_authToken(new AuthInfo(authInfo));
  }

  /**
   * @see org.uddi4j.client.UDDIProxy#discard_authToken(AuthInfo)
   */
  public DispositionReport discard_authToken(AuthInfo authInfo)
    throws UDDIException, TransportException
  {
    DiscardAuthToken request = new DiscardAuthToken();
    request.setAuthInfo(authInfo);

    return (DispositionReport)send(request);
  }

  /**
   * @see org.uddi4j.client.UDDIProxy#get_authToken(String, String)
   */
  public AuthToken get_authToken(String userid,String cred)
    throws UDDIException, TransportException
  {
    GetAuthToken request = new GetAuthToken();
    request.setUserID(userid);
    request.setCred(cred);

    return (AuthToken)send(request);
  }

  /**
   * @see org.uddi4j.client.UDDIProxy#get_registeredInfo(String)
   */
  public RegisteredInfo get_registeredInfo(String authInfo)
    throws UDDIException, TransportException
  {
    GetRegisteredInfo request = new GetRegisteredInfo();
    request.setAuthInfo(authInfo);

    return (RegisteredInfo)send(request);
  }

  /**
   * @see org.uddi4j.client.UDDIProxy#save_binding(String, Vector)
   */
  public BindingDetail save_binding(String authInfo,Vector bindingTemplates)
    throws UDDIException, TransportException
  {
    SaveBinding request = new SaveBinding();
    request.setAuthInfo(authInfo);
    request.setBindingTemplateVector(bindingTemplates);

    return (BindingDetail)send(request);
  }

  /**
   * @see org.uddi4j.client.UDDIProxy#save_business(String, Vector)
   */
  public BusinessDetail save_business(String authInfo,Vector businessEntities)
    throws UDDIException, TransportException
  {
    SaveBusiness request = new SaveBusiness();
    request.setAuthInfo(authInfo);
    request.setBusinessEntityVector(businessEntities);

    return (BusinessDetail)send(request);
  }

  /**
   * @see org.uddi4j.client.UDDIProxy#save_service(String, Vector)
   */
  public ServiceDetail save_service(String authInfo,Vector businessServices)
    throws UDDIException, TransportException
  {
    SaveService request = new SaveService();
    request.setAuthInfo(authInfo);
    request.setBusinessServiceVector(businessServices);

    return (ServiceDetail)send(request);
  }

  /**
   * @see org.uddi4j.client.UDDIProxy#save_tModel(String, Vector)
   */
  public TModelDetail save_tModel(String authInfo,Vector tModels)
    throws UDDIException, TransportException
  {
    SaveTModel request = new SaveTModel();
    request.setAuthInfo(authInfo);
    request.setTModelVector(tModels);

    return (TModelDetail)send(request);
  }

  /**
   * @see org.uddi4j.client.UDDIProxy#set_publisherAssertions(String, Vector)
   */
  public  PublisherAssertions set_publisherAssertions(String  authInfo,Vector publisherAssertion)
    throws UDDIException, TransportException
  {
    SetPublisherAssertions request = new SetPublisherAssertions();
    request.setAuthInfo(authInfo);
    request.setPublisherAssertionVector(publisherAssertion);

    return (PublisherAssertions)send(request);
  }

  /**
   * @see org.uddi4j.client.UDDIProxy#validate_values_businessEntity(Vector)
   */
  public DispositionReport validate_values_businessEntity(Vector businessEntity)
    throws UDDIException, TransportException
  {
     ValidateValues request = new ValidateValues();
     request.setBusinessEntityVector(businessEntity);

     return (DispositionReport)send(request);
   }

  /**
   * @see org.uddi4j.client.UDDIProxy#validate_values_businessService(Vector)
   */
  public DispositionReport validate_values_businessService(Vector businessService)
    throws UDDIException, TransportException
  {
    ValidateValues request = new ValidateValues();
    request.setBusinessServiceVector(businessService);

    return (DispositionReport)send(request);
  }

  /**
   * @see org.uddi4j.client.UDDIProxy#validate_values_tModel(Vector)
   */
  public DispositionReport validate_values_tModel(Vector tModel)
    throws UDDIException, TransportException
  {
    ValidateValues request = new ValidateValues();
    request.setTModelVector(tModel);

    return (DispositionReport)send(request);
  }

  /**
   * This method is not supported for the JUDDIProxy implementation
   */
  public Element send(UDDIElement el, boolean inquiry)
    throws TransportException
  {
    return null;
  }

   /**
    * This methid is not supported for the JUDDIProxy implementation
    */
  public Element send(Element el, boolean inquiry)
    throws TransportException
  {
	  return null;
  }

  /**
   *
   * @param request
   * @return UDDIElement
   * @throws UDDIException
   */
  public UDDIElement send(UDDIElement request)
    throws UDDIException
  {
    UDDIElement retval = null;
    UDDIService service = null;

		try
    {
      service = ServiceFactory.getService(request.getClass().getName());
			retval = service.invoke(request);
		}
    catch (JUDDIException je)
    {
		  throw getUDDIException(je);
		}

		return retval;
   }

  /**
   * Converts a JUDDIException into a UDDIException.
   *
   * @param e
   * @return UDDIException
   */
  private UDDIException getUDDIException(JUDDIException e)
  {
   	UDDIException retval = null;
		DocumentBuilderFactory factory = DocumentBuilderFactory.newInstance();

		try
    {
      Element faultElement = null;
      Element rootElement = null;

      // 2. build up a response element for UDDI4j to 'saveToXML()' into
      DocumentBuilder builder = factory.newDocumentBuilder();
      Document faultDoc = builder.newDocument();
      rootElement = faultDoc.createElement("Fault");
      faultDoc.appendChild(rootElement);

      faultElement = faultDoc.createElement("faultcode");
      faultElement.appendChild(faultDoc.createTextNode(e.getFaultCode()));
      rootElement.appendChild(faultElement);

      faultElement = faultDoc.createElement("faultstring");
      if ( e.getFaultString() != null ) {
        faultElement.appendChild(faultDoc.createTextNode(e.getFaultString()));
      } else {
        faultElement.appendChild(faultDoc.createTextNode(e.getMessage()));
      }
      rootElement.appendChild(faultElement);

      faultElement = faultDoc.createElement("faultactor");
      faultElement.appendChild(faultDoc.createTextNode(e.getFaultActor()));
      rootElement.appendChild(faultElement);

      DispositionReport dispRpt = e.getDispositionReport();
      if (dispRpt != null) {
        faultElement = faultDoc.createElement("detail");
        dispRpt.saveToXML(faultElement);
        rootElement.appendChild(faultElement);
      }

      retval = new UDDIException(rootElement, true);
    }
    catch(Exception pcex)
    {
      System.out.println(pcex.getMessage());
    }

    return retval;
  }
}