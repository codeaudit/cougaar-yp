/*
 * <copyright>
 *  
 *  Copyright 2002-2004 BBNT Solutions, LLC
 *  under sponsorship of the Defense Advanced Research Projects
 *  Agency (DARPA).
 * 
 *  You can redistribute this software and/or modify it under the
 *  terms of the Cougaar Open Source License as published on the
 *  Cougaar Open Source Website (www.cougaar.org).
 * 
 *  THIS SOFTWARE IS PROVIDED BY THE COPYRIGHT HOLDERS AND CONTRIBUTORS
 *  "AS IS" AND ANY EXPRESS OR IMPLIED WARRANTIES, INCLUDING, BUT NOT
 *  LIMITED TO, THE IMPLIED WARRANTIES OF MERCHANTABILITY AND FITNESS FOR
 *  A PARTICULAR PURPOSE ARE DISCLAIMED. IN NO EVENT SHALL THE COPYRIGHT
 *  OWNER OR CONTRIBUTORS BE LIABLE FOR ANY DIRECT, INDIRECT, INCIDENTAL,
 *  SPECIAL, EXEMPLARY, OR CONSEQUENTIAL DAMAGES (INCLUDING, BUT NOT
 *  LIMITED TO, PROCUREMENT OF SUBSTITUTE GOODS OR SERVICES; LOSS OF USE,
 *  DATA, OR PROFITS; OR BUSINESS INTERRUPTION) HOWEVER CAUSED AND ON ANY
 *  THEORY OF LIABILITY, WHETHER IN CONTRACT, STRICT LIABILITY, OR TORT
 *  (INCLUDING NEGLIGENCE OR OTHERWISE) ARISING IN ANY WAY OUT OF THE USE
 *  OF THIS SOFTWARE, EVEN IF ADVISED OF THE POSSIBILITY OF SUCH DAMAGE.
 *  
 * </copyright>
 */

package org.cougaar.yp;

import java.util.Vector;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;

import org.cougaar.core.mts.MessageAddress;
import org.cougaar.core.service.community.Community;
import org.cougaar.util.log.Logger;
import org.cougaar.util.log.Logging;
import org.uddi4j.UDDIElement;
import org.uddi4j.UDDIException;
import org.uddi4j.datatype.assertion.PublisherAssertion;
import org.uddi4j.request.AddPublisherAssertions;
import org.uddi4j.request.DeleteBinding;
import org.uddi4j.request.DeleteBusiness;
import org.uddi4j.request.DeletePublisherAssertions;
import org.uddi4j.request.DeleteService;
import org.uddi4j.request.DeleteTModel;
import org.uddi4j.request.DiscardAuthToken;
import org.uddi4j.request.FindBinding;
import org.uddi4j.request.FindBusiness;
import org.uddi4j.request.FindRelatedBusinesses;
import org.uddi4j.request.FindService;
import org.uddi4j.request.FindTModel;
import org.uddi4j.request.GetAssertionStatusReport;
import org.uddi4j.request.GetAuthToken;
import org.uddi4j.request.GetBindingDetail;
import org.uddi4j.request.GetBusinessDetail;
import org.uddi4j.request.GetBusinessDetailExt;
import org.uddi4j.request.GetPublisherAssertions;
import org.uddi4j.request.GetRegisteredInfo;
import org.uddi4j.request.GetServiceDetail;
import org.uddi4j.request.GetTModelDetail;
import org.uddi4j.request.SaveBinding;
import org.uddi4j.request.SaveBusiness;
import org.uddi4j.request.SaveService;
import org.uddi4j.request.SaveTModel;
import org.uddi4j.request.SetPublisherAssertions;
import org.uddi4j.request.ValidateValues;
import org.uddi4j.response.AssertionStatusReport;
import org.uddi4j.response.AuthToken;
import org.uddi4j.response.BindingDetail;
import org.uddi4j.response.BusinessDetail;
import org.uddi4j.response.BusinessDetailExt;
import org.uddi4j.response.BusinessList;
import org.uddi4j.response.CompletionStatus;
import org.uddi4j.response.DispositionReport;
import org.uddi4j.response.PublisherAssertions;
import org.uddi4j.response.RegisteredInfo;
import org.uddi4j.response.RelatedBusinessesList;
import org.uddi4j.response.ServiceDetail;
import org.uddi4j.response.ServiceList;
import org.uddi4j.response.TModelDetail;
import org.uddi4j.response.TModelList;
import org.uddi4j.util.AuthInfo;
import org.uddi4j.util.BindingKey;
import org.uddi4j.util.BusinessKey;
import org.uddi4j.util.CategoryBag;
import org.uddi4j.util.DiscoveryURLs;
import org.uddi4j.util.FindQualifiers;
import org.uddi4j.util.IdentifierBag;
import org.uddi4j.util.KeyedReference;
import org.uddi4j.util.ServiceKey;
import org.uddi4j.util.TModelBag;
import org.uddi4j.util.TModelKey;
import org.w3c.dom.DOMException;
import org.w3c.dom.Element;

// this could easily be code generated from uddi4j sources.  sigh.

/** This is the primary mechanism for constructing and executing YP queries.
 * The interface is essentially a clone of the UDDI4J UDDIProxy API
 * except that it takes the approach of asynchronous operations rather
 * than synchronous operations.
 **/

class YPProxyImpl implements YPProxy {
  private static final Logger logger = Logging.getLogger(YPProxy.class);
  private final Object context;
  private final YPService yps;
  private final boolean autosubmit;
  private final int searchMode;

  YPProxyImpl(MessageAddress ypAgent, YPService yps, boolean autosubmit) {
    this.context = ypAgent;
    this.yps = yps;
    this.autosubmit = autosubmit;
    this.searchMode = YPProxy.SearchMode.NO_COMMUNITY_SEARCH;
  }

  YPProxyImpl(Community community, YPService yps, boolean autosubmit, 
	      int searchMode) {
    this.context = community;
    this.yps = yps;
    this.autosubmit = autosubmit;

    if (YPProxy.SearchMode.validCommunitySearchMode(searchMode)) {
      this.searchMode = searchMode;
    } else {
      throw new IllegalArgumentException("Invalid search mode specified - " + 
					 searchMode + 
					 " - must be one of YPProxy.SearchMode ");
    }
  }

  YPProxyImpl(YPService yps, boolean autosubmit, int searchMode) {
    this.context = null;
    this.yps = yps;
    this.autosubmit = autosubmit;

    if (YPProxy.SearchMode.validCommunitySearchMode(searchMode)) {
      this.searchMode = searchMode;
    } else {
      throw new IllegalArgumentException("Invalid search mode specified - " + 
					 searchMode + 
					 " - must be one of YPProxy.SearchMode ");
    }
  }

  public int getSearchMode() {
    return searchMode;
  }

  /**
   * @note The returned YPFuture will contain a BindingDetail when completed.
   */
  public YPFuture find_binding(FindQualifiers findQualifiers,
                         String serviceKey,
                         TModelBag tModelBag,
                          int maxRows) {
    FindBinding fb = new FindBinding(serviceKey, tModelBag);
    if (findQualifiers != null) fb.setFindQualifiers(findQualifiers);
    if (maxRows>0) fb.setMaxRows(maxRows);
    return pkg(fb,true, BindingDetail.class);
  }

  /**
   * @note The returned YPFuture will contain a BusinessList when completed.
   */
  public YPFuture find_business(Vector names,
                          DiscoveryURLs discoveryURLs,
                          IdentifierBag identifierBag,
                          CategoryBag categoryBag,
                          TModelBag tModelBag,
                          FindQualifiers findQualifiers,
                                  int maxRows) {
    FindBusiness o = new FindBusiness();
    if (maxRows>0) o.setMaxRows(maxRows);
    o.setFindQualifiers(findQualifiers);
    o.setNameVector(names);
    o.setIdentifierBag(identifierBag);
    o.setCategoryBag(categoryBag);
    o.setTModelBag(tModelBag);
    o.setDiscoveryURLs(discoveryURLs);
    return pkg(o,true, BusinessList.class);
  }

  /**
   * @note The returned YPFuture will contain a RelatedBusinessesList when completed.
   */
  public YPFuture find_relatedBusinesses(String businessKey,
                                   KeyedReference keyedReference,
                                   FindQualifiers findQualifiers,
                                           int maxRows) {
    FindRelatedBusinesses o = new FindRelatedBusinesses(businessKey);
    o.setKeyedReference(keyedReference);
    o.setFindQualifiers(findQualifiers);
    if (maxRows>0) o.setMaxRows(maxRows);
    return pkg(o,true, RelatedBusinessesList.class);
  }

  /**
   * @note The returned YPFuture will contain a ServiceList when completed.
   */
  public YPFuture find_service (String businessKey,
                          Vector names,
                          CategoryBag categoryBag,
                          TModelBag  tModelBag,
                          FindQualifiers findQualifiers,
                                  int maxRows) {
    FindService o = new FindService(businessKey);
    o.setNameVector(names);
    o.setCategoryBag(categoryBag);
    o.setTModelBag(tModelBag);
    o.setFindQualifiers(findQualifiers);
    if (maxRows>0) o.setMaxRows(maxRows);
    return pkg(o,true, ServiceList.class);
  }
    
  /**
   * @note The returned YPFuture will contain a TModelList when completed.
   */
  public YPFuture find_tModel (String name,
                         CategoryBag categoryBag,
                         IdentifierBag identifierBag,
                         FindQualifiers findQualifiers,
                                 int maxRows) {
    FindTModel o = new FindTModel();
    o.setName(name);
    o.setCategoryBag(categoryBag);
    o.setIdentifierBag(identifierBag);
    o.setFindQualifiers(findQualifiers);
    if (maxRows>0) o.setMaxRows(maxRows);
    return pkg(o,true, TModelList.class);
  }

  /**
   * @note The returned YPFuture will contain a BindingDetail when completed.
   */
  public YPFuture get_bindingDetail(String bindingKey) {
    GetBindingDetail o = new GetBindingDetail();
    Vector v = new Vector(1);
    v.addElement(new BindingKey(bindingKey));
    o.setBindingKeyVector(v);
    return pkg(o,true, BindingDetail.class);
  }

  /**
   * @note The returned YPFuture will contain a BindingDetail when completed.
   */
  public YPFuture get_bindingDetail(Vector bindingKeyStrings) {
    GetBindingDetail o = new GetBindingDetail(bindingKeyStrings);
    return pkg(o,true, BindingDetail.class);
  }

  /**
   * @note The returned YPFuture will contain a BusinessDetail when completed.
   */
  public YPFuture get_businessDetail(String businessKey) {
    GetBusinessDetail o = new GetBusinessDetail();
    Vector v = new Vector(1);
    v.addElement(new BusinessKey(businessKey));
    o.setBusinessKeyVector(v);
    return pkg(o,true, BusinessDetail.class);
  }

  /**
   * @note The returned YPFuture will contain a BusinessDetail when completed.
   */
  public YPFuture get_businessDetail(Vector businessKeyStrings) {
    GetBusinessDetail o = new GetBusinessDetail(businessKeyStrings);
    return pkg(o,true, BusinessDetail.class);
  }

  /**
   * @note The returned YPFuture will contain a BusinessDetailExt when completed.
   */
  public YPFuture get_businessDetailExt(String businessKey) {
    GetBusinessDetailExt o = new GetBusinessDetailExt();
    Vector v = new Vector(1);
    v.addElement(new BusinessKey(businessKey));
    o.setBusinessKeyVector(v);
    return pkg(o,true, BusinessDetailExt.class);
  }

  /**
   * @note The returned YPFuture will contain a BusinessDetailExt when completed.
   */
  public YPFuture get_businessDetailExt(Vector businessKeyStrings) {
    GetBusinessDetailExt o = new GetBusinessDetailExt(businessKeyStrings);
    return pkg(o,true, BusinessDetailExt.class);
  }

  /**
   * @note The returned YPFuture will contain a ServiceDetail when completed.
   */
  public YPFuture get_serviceDetail(String serviceKey) {
    GetServiceDetail o = new GetServiceDetail();
    o.setServiceKeyVector(v(new ServiceKey(serviceKey)));
    return pkg(o,true, ServiceDetail.class);
  }

  /**
   * @note The returned YPFuture will contain a ServiceDetail when completed.
   */
  public YPFuture get_serviceDetail(Vector serviceKeyStrings) {
    GetServiceDetail o = new GetServiceDetail(serviceKeyStrings);
    return pkg(o,true, ServiceDetail.class);
  }

  /**
   * @note The returned YPFuture will contain a TModelDetail when completed.
   */
  public YPFuture get_tModelDetail(String tModelKey) {
    GetTModelDetail o = new GetTModelDetail();
    o.setTModelKeyVector(v(new TModelKey(tModelKey)));
    return pkg(o,true, TModelDetail.class);
  }

  /**
   * @note The returned YPFuture will contain a TModelDetail when completed.
   */
  public YPFuture get_tModelDetail(Vector tModelKeyStrings) {
    GetTModelDetail o = new GetTModelDetail(tModelKeyStrings);
    return pkg(o,true, TModelDetail.class);
  }    

  /**
   * @note The returned YPFuture will contain a  DispositionReport when completed.
   */
  public YPFuture add_publisherAssertions (String authInfo,
                                             PublisherAssertion publisherAssertion) {
    AddPublisherAssertions o = new AddPublisherAssertions(authInfo, v(publisherAssertion));
    return pkg(o,false,  DispositionReport.class);
  }

  /**
   * @note The returned YPFuture will contain a  DispositionReport when completed.
   */
  public YPFuture add_publisherAssertions (String authInfo,
                                             Vector publisherAssertion) {
    AddPublisherAssertions o = new AddPublisherAssertions(authInfo, publisherAssertion);
    return pkg(o,false,  DispositionReport.class);
  }

  /**
   * @note The returned YPFuture will contain a AssertionStatusReport when completed.
   */
  public YPFuture get_assertionStatusReport(String authInfo,
                                              String completionStatus) {
    GetAssertionStatusReport o = new GetAssertionStatusReport(authInfo, completionStatus);
    return pkg(o,true, AssertionStatusReport.class);
  }

  /**
   * @note The returned YPFuture will contain a AssertionStatusReport when completed.
   */
  public YPFuture get_assertionStatusReport(String authInfo,
                                              CompletionStatus completionStatus) {
    GetAssertionStatusReport o = new GetAssertionStatusReport(authInfo, completionStatus);
    return pkg(o,true, AssertionStatusReport.class);
  }

  /**
   * @note The returned YPFuture will contain a PublisherAssertions when completed.
   */
  public YPFuture get_publisherAssertions(String authInfo) {
    GetPublisherAssertions o = new GetPublisherAssertions(authInfo);
    return pkg(o,true, PublisherAssertions.class);
  }

  /**
   * @note The returned YPFuture will contain a DispositionReport when completed.
   */
  public YPFuture delete_binding(String authInfo,
                                   String bindingKey) {
    DeleteBinding o = new DeleteBinding(authInfo, v(bindingKey));
    return pkg(o,false, DispositionReport.class);
  }

  /**
   * @note The returned YPFuture will contain a DispositionReport when completed.
   */
  public YPFuture delete_binding(String authInfo,
                                   Vector bindingKeyStrings) {
    DeleteBinding o = new DeleteBinding(authInfo, bindingKeyStrings);
    return pkg(o,false, DispositionReport.class);
  }

  /**
   * @note The returned YPFuture will contain a DispositionReport when completed.
   */
  public YPFuture delete_business(String authInfo,
                                    String businessKey) {
    return pkg(new DeleteBusiness(authInfo, v(businessKey)),false, DispositionReport.class);
  }

  /**
   * @note The returned YPFuture will contain a DispositionReport when completed.
   */
  public YPFuture delete_business(String authInfo,
                                    Vector businessKeyStrings) {
    return pkg(new DeleteBusiness(authInfo, businessKeyStrings),false, DispositionReport.class);
  }

  /**
   * @note The returned YPFuture will contain a DispositionReport when completed.
   */
  public YPFuture delete_service(String authInfo,
                                   String serviceKey) {
    return pkg(new DeleteService(authInfo, v(serviceKey)),false, DispositionReport.class);
  }

  /**
   * @note The returned YPFuture will contain a DispositionReport when completed.
   */
  public YPFuture delete_service(String authInfo,
                                   Vector serviceKeyStrings) {
    return pkg(new DeleteService(authInfo, serviceKeyStrings),false, DispositionReport.class);
  }

  /**
   * @note The returned YPFuture will contain a DispositionReport when completed.
   */
  public YPFuture delete_tModel(String authInfo,
                                  String tModelKey) {
    return pkg(new DeleteTModel(authInfo, v(tModelKey)),false, DispositionReport.class);
  }

  /**
   * @note The returned YPFuture will contain a DispositionReport when completed.
   */
  public YPFuture delete_tModel(String authInfo,
                                  Vector tModelKeyStrings) {
    return pkg(new DeleteTModel(authInfo, tModelKeyStrings),false, DispositionReport.class);
  }

  /**
   * @note The returned YPFuture will contain a DispositionReport when completed.
   */
  public YPFuture delete_publisherAssertions(String authInfo,
                                               PublisherAssertion publisherAssertion) 
    throws UDDIException
  {
    return pkg(new DeletePublisherAssertions(authInfo, v(publisherAssertion)),false, DispositionReport.class);
  }

  /**
   * @note The returned YPFuture will contain a DispositionReport when completed.
   */
  public YPFuture delete_publisherAssertions(String authInfo,
                                               Vector publisherAssertion) 
    throws UDDIException
  {
    return pkg(new DeletePublisherAssertions(authInfo, publisherAssertion),false, DispositionReport.class);
  }

  /**
   * @note The returned YPFuture will contain a DispositionReport when completed.
   */
  public YPFuture discard_authToken(String authInfo) {
    return pkg(new DiscardAuthToken(authInfo),false, DispositionReport.class);
  }

  /**
   * @note The returned YPFuture will contain a DispositionReport when completed.
   */
  public YPFuture discard_authToken(AuthInfo authInfo) {
    DiscardAuthToken o = new DiscardAuthToken();
    o.setAuthInfo(authInfo);
    return pkg(o,false, DispositionReport.class);
  }

  /**
   * @note The returned YPFuture will contain a AuthToken when completed.
   */
  public YPFuture get_authToken(String userid,
                                  String cred) {
    return pkg(new GetAuthToken(userid, cred),true, AuthToken.class);
  }

  /**
   * @note The returned YPFuture will contain a RegisteredInfo when completed.
   */
  public YPFuture get_registeredInfo(String authInfo) {
    return pkg(new GetRegisteredInfo(authInfo),true, RegisteredInfo.class);
  }

  /**
   * @note The returned YPFuture will contain a BindingDetail when completed.
   */
  public YPFuture save_binding(String authInfo,
                                 Vector bindingTemplates) {
    return pkg(new SaveBinding(authInfo, bindingTemplates),true, BindingDetail.class);
  }

  /**
   * @note The returned YPFuture will contain a BusinessDetail when completed.
   */
  public YPFuture save_business(String authInfo,
                                  Vector businessEntities) {
    SaveBusiness o = new SaveBusiness(authInfo);
    o.setBusinessEntityVector(businessEntities);
    return pkg(o,true, BusinessDetail.class);
  }

  /**
   * @note The returned YPFuture will contain a ServiceDetail when completed.
   */
  public YPFuture save_service(String authInfo,
                                 Vector businessServices) {
    return pkg(new SaveService(authInfo, businessServices),true, ServiceDetail.class);
  }

  /**
   * @note The returned YPFuture will contain a TModelDetail when completed.
   */
  public YPFuture save_tModel(String authInfo,
                                Vector tModels) {
    SaveTModel o = new SaveTModel(authInfo);
    o.setTModelVector(tModels);
    return pkg(o,true, TModelDetail.class);
  }

  /**
   * @note The returned YPFuture will contain a PublisherAssertions when completed.
   */
  public YPFuture set_publisherAssertions (String  authInfo,
                                             PublisherAssertion  pub) {
    return pkg(new SetPublisherAssertions(authInfo, v(pub)),false, PublisherAssertions.class);
  }

  /**
   * @note The returned YPFuture will contain a  PublisherAssertions when completed.
   */
  public YPFuture set_publisherAssertions (String  authInfo,
                                             Vector publisherAssertion) {
    return pkg(new SetPublisherAssertions(authInfo, publisherAssertion),false,  PublisherAssertions.class);
  }    

  /**
   * @note The returned YPFuture will contain a DispositionReport when completed.
   */
  public YPFuture validate_values_businessEntity(Vector businessEntity) {
    ValidateValues o = new ValidateValues();
    o.setBusinessEntityVector(businessEntity);
    return pkg(o,false, DispositionReport.class);
  }

  /**
   * @note The returned YPFuture will contain a DispositionReport when completed.
   */
  public YPFuture validate_values_businessService(Vector businessService) {
    ValidateValues o = new ValidateValues();
    o.setBusinessServiceVector(businessService);
    return pkg(o,false, DispositionReport.class);
  }

  /**
   * @note The returned YPFuture will contain a DispositionReport when completed.
   */
  public YPFuture validate_values_tModel(Vector tModel) {
    ValidateValues o = new ValidateValues();
    o.setTModelVector(tModel);
    return pkg(o,false, DispositionReport.class);
  }

  /**
   * @note The returned YPFuture will contain a Element when completed.
   */
  public YPFuture send(UDDIElement el, boolean inquiry) {
    return pkg(el,inquiry, null);
  }

  /**
   * @note The returned YPFuture will contain a Element when completed.
   */
  public YPFuture send(Element el, boolean inquiry) {
    return pkg(el,inquiry, null);
  }


  //
  //
  //


  private YPFuture pkg(UDDIElement el, boolean qp, Class rc) {
    return pkg(toXML(el), qp, rc);
  }

  private YPFuture pkg(Element el, boolean qp, Class rc) {
    YPFuture fut = new YPFutureImpl(context, el, qp, rc, getSearchMode());
    if (autosubmit) {
      return yps.submit(fut);
    } else {
      return fut;
    }
  }

  // Extra method for sending queries without blackboard involvement.
  public YPFuture execute(YPFuture pendingQuery) {
    // not yet implemented.  needs something like:
    return yps.submit(pendingQuery);
  }


  //
  // utilities
  //

  private Vector v(Object o) {
    Vector v = new Vector(1);
    v.addElement(o);
    return v;
  }

  private Element toXML(UDDIElement el) {
    Element base = createTmpElement();
    el.saveToXML(base);
    Element e = (Element) base.getFirstChild();
    return e;
  }
  
  private static DocumentBuilder _docBuilder = null;
  private static synchronized DocumentBuilder getDocBuilder() {
    if (_docBuilder == null) {
      try {
        _docBuilder = DocumentBuilderFactory.newInstance().newDocumentBuilder();
      } catch (ParserConfigurationException pce) {
        logger.error("Unexpected failure creating DocumentBuilder", pce);
        throw new Error("Could not create DocumentBuilder", pce);
      }
    }
    return _docBuilder;
  }

  private Element createTmpElement() {
    DocumentBuilder docBuilder = getDocBuilder();
    synchronized (docBuilder) {
      try {
        // YECH!! javax.* isnt thread safe... Lets see if this is a bottleneck.
        return docBuilder.newDocument().createElement("tmp");
      } catch (DOMException e) {
        // probably cannot happen
        logger.error("Failure while building tmp element", e);
        throw new RuntimeException(e);
      }
    }
  }
}
