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

import org.uddi4j.UDDIElement;
import org.uddi4j.UDDIException;
import org.uddi4j.datatype.assertion.PublisherAssertion;
import org.uddi4j.response.CompletionStatus;
import org.uddi4j.util.AuthInfo;
import org.uddi4j.util.CategoryBag;
import org.uddi4j.util.DiscoveryURLs;
import org.uddi4j.util.FindQualifiers;
import org.uddi4j.util.IdentifierBag;
import org.uddi4j.util.KeyedReference;
import org.uddi4j.util.TModelBag;
import org.w3c.dom.Element;

// this could easily be code generated from uddi4j sources.  sigh.

/** This is the primary mechanism for constructing and executing YP queries.
 * The interface is essentially a clone of the UDDI4J UDDIProxy API
 * except that it takes the approach of asynchronous operations rather
 * than synchronous operations.
 **/

public interface YPProxy {
  public static final String DEFAULT_UDDI_USERNAME = "cougaar";
  public static final String DEFAULT_UDDI_PASSWORD = "cougaarPass";

  public class SearchMode {
    public static final int NO_COMMUNITY_SEARCH = 0;
    public static final int HIERARCHICAL_COMMUNITY_SEARCH = 1;
    public static final int SINGLE_COMMUNITY_SEARCH = 2;

    /* Change to highest value if more added */
    public static final int MAX = SINGLE_COMMUNITY_SEARCH;

    static public boolean validSearchMode(int searchMode) {
      return (searchMode >= 0) && (searchMode <= SearchMode.MAX);
    }
    
    static public boolean validCommunitySearchMode(int searchMode) {
      return ((searchMode == HIERARCHICAL_COMMUNITY_SEARCH) ||
	      (searchMode == SINGLE_COMMUNITY_SEARCH));
    }
  }

  /**
   * Describes the search mode used to find YPServers - 
   * NO_SEARCH - YPProxy does not use the community structure to 
   * find the YPServer.
   * HIERARCHICAL_COMMUNITY_SEARCH - Query progresses
   * up the community structure of YP servers until either a match is found or
   * search has reached the topmost community. 
   * SINGLE_COMMUNITY_SEARCH - Query is applied only to the
   * YP server in the specified YP community context.
   */
  public int getSearchMode();



  /**
   * The find_binding method returns a bindingDetail message that contains
   * a bindingTemplates structure with zero or more bindingTemplate structures
   * matching the criteria specified in the argument list.
   *
   * @param findQualifiers
   *                   This collection of findQualifier elements can be used to alter the default
   *                   behavior of search functionality.
   * @param serviceKey Used to specify a particular instance of a businessService element
   *                   in the registered data.  Only bindings in the specific businessService
   *                   data identified by the serviceKey passed will be searched.
   * @param tModelBag  This is a list of tModel uuid_key values that represent the technical
   *                   fingerprint to locate in a bindingTemplate structure contained within
   *                   the businessService instance specified by the serviceKey value.  If more
   *                   than one tModel key is specified in this structure, only bindingTemplate
   *                   information that exactly matches all of the tModel keys specified will
   *                   be returned (logical AND).  The order of the keys in the tModelBag is
   *                   not relevant.  All tModelKey values begin with a uuid URN qualifier
   *                   (e.g. "uuid:" followed by a known tModel UUID value.
   * @param maxRows    This optional integer value allows the requesting program to limit
   *                   the number of results returned.
   * @return This function returns a YPFuture wrapping a bindingDetail message on success.  In the event that no matches were
   * located for the specified criteria, the bindingDetail structure returned in the response the will be
   * empty (e.g. contain no bindingTemplate data.)
   *         In the even of a large number of matches, an Operator Site may truncate the result set.  If
   * this occurs, the response message will contain the truncated attribute with the value of this attribute
   * set to true.
   *         Searching using tModelBag will also return any bindingTemplate information that matches due to
   * hostingRedirector references.  The resolved bindingTemplate structure will be returned, even if that
   * bindingTemplate is owned by a different businessService structure.
   * @note The returned YPFuture will contain a BindingDetail when completed.
   */
  YPFuture find_binding(FindQualifiers findQualifiers,
                         String serviceKey,
                         TModelBag tModelBag,
                         int maxRows);

  /**
   * The find_business message returns a businessList message that matches
   * the conditions specified in the arguments.
   * 
   * @param names     vector of Name objects .
   * @param discoveryURLs
   *                  This is a list of URL's to be matched against the data associated
   *                  with the discoveryURL's contents of registered businessEntity information.
   *                  To search for URL without regard to useType attribute values, pass
   *                  the useType component of the discoveryURL elements as empty attributes.
   *                  If useType values are included, then the match will be made only on
   *                  registered information that match both the useType and URL value.
   *                  The returned businessList contains businessInfo structures matching
   *                  any of the URL's passed (logical OR).
   * @param identifierBag
   *                  This is a list of business identifier references. The returned businessList
   *                  contains businessInfo structures matching any of the identifiers passed
   *                  (logical OR).
   * @param categoryBag
   *                  This is a list of category references.  The returned businessList
   *                  contains businessInfo structures matching all of the categories
   *                  passed (logical AND).
   * @param tModelBag The registered businessEntity data contains bindingTemplates that in turn
   *                  contain specific tModel references.  The tModelBag argument lets you
   *                  search for businesses that have bindings that are compatible with a
   *                  specific tModel pattern.  The returned businessList contains businessInfo
   *                  structures that match all of the tModel keys passed (logical AND).
   *                  tModelKey values must be formatted as URN qualified UUID values
   *                  (e.g. prefixed with "uuid:")
   * @param findQualifiers
   *                  can be used to alter the default behavior of search functionality.
   * @param maxRows   allows the requesting program to limit the number of results returned.
   * @return This function returns a YPFuture wrapping a businessList on success.  In the event that no
   *         matches were located for the specified criteria, a businessList
   *         structure with zero businessInfo structures is returned.
   * @note The returned YPFuture will contain a BusinessList when completed.
   */
  YPFuture find_business(Vector names,
                          DiscoveryURLs discoveryURLs,
                          IdentifierBag identifierBag,
                          CategoryBag categoryBag,
                          TModelBag tModelBag,
                          FindQualifiers findQualifiers,
                          int maxRows);

  /**
   * The find_relatedBusinesses API call is used to locate information about businessEntity
   * registrations that are related to a specific business entity whose key is passed in the
   * inquiry. The Related Businesses feature is used to manage registration of business units and
   * subsequently relate them based on organizational hierarchies or business partner relationships.
   *
   * This  returns zero or more relatedBusinessInfo structures .For the businessEntity specified in the
   * the response reports complete business relationships with other businessEntity
   * registrations. Business relationships are complete between two businessEntity registrations when the
   * publishers controlling each of the businessEntity structures involved in the relationship set
   * assertions affirming that relationship.
   *
   * @param businessKey      This is used to specify a particular  BusinessEntity instance.
   *
   * @param keyedReference   This is a single, optional keyedReference element that is used to
   *                         specify that  only businesses that are related to the focal point
   *                         in a specific way should be included in the results.
   * @param findQualifiers
   *                         Can be used to alter the default behavior of search functionality.
   * @param maxRows          allows the requesting program to limit the number of results returned.
   * @return This function returns a YPFuture wrapping a RelatedBusinessesList on success.
   * @note The returned YPFuture will contain a RelatedBusinessesList when completed.
   */
  YPFuture find_relatedBusinesses(String businessKey,
                                   KeyedReference keyedReference,
                                   FindQualifiers findQualifiers,
                                   int maxRows);
  /**
   * This function returns a YPFuture wrapping a serviceList on success.  In the event that no
   * matches were located for the specified criteria, the serviceList
   * structure returned will contain an empty businessServices structure.
   *
   * @param businessKey
   *                 This optional uuid_key is used to specify a particular
   *                 BusinessEntity instance. This argument may be used to
   *                 specify an existing businessEntity in the registry or
   *                 may be specified as null or "" (empty string) to indicate
   *                 that all businessEntities are to be searched.
   * @param names    This optional Vector of Name objects represents one or more partial names qualified
   *                 with xml:lang attributes.  Any businessService data contained in the specified
   *                 businessEntity with a matching partial name value gets returned. A wildcard character %
   *                 may be used to signify any number of any characters.  Up to 5 name values may be
   *                 specified.  If multiple name values are passed, the match occurs on a logical OR basis
   *                 within any names supplied (e.g. any match on name/language pairs will cause a
   *                 registered service to be included in the final result set).
   * @param categoryBag
   *               : This is a list of category references.  The returned serviceList contains
   *                 businessInfo structures matching all of the categories passed (logical AND by
   *                 default).
   * @param tModelBag
   *                 This is a list of tModel uuid_key values that represent the technical fingerprint of
   *                 a bindingTemplate structure to find. Version 2.0 defines a  way  to  associate
   *                 businessService structures with more than one businessEntity. All bindingTemplate
   *                 structures within any businessService associated with the businessEntity specified by
   *                 the businessKey argument will be searched.  If more than one tModel key is specified
   *                 in this structure, only businessService structures that contain bindingTemplate
   *                 structures with fingerprint information that matches all of the tModel keys specified
   *                 will be returned (logical AND only).
   * @param findQualifiers
   *                 used to alter the default behavior of search functionality.
   * @param maxRows  allows the requesting program to limit the number of results returned.
   * @return         This function returns a YPFuture wrapping a serviceList on success.  In the event that no
   *                 matches were located for the specified criteria, the serviceList
   *                 structure returned will contain an empty businessServices structure.
   * @note The returned YPFuture will contain a ServiceList when completed.
   */
  YPFuture find_service (String businessKey,
                          Vector names,
                          CategoryBag categoryBag,
                          TModelBag  tModelBag,
                          FindQualifiers findQualifiers,
                          int maxRows); 
  /**
   * This find_tModel message is for locating a list of tModel entries
   * that match a set of specific criteria. The response will be a list
   * of abbreviated information about tModels that match the criteria (tModelList).
   *
   * @param name    This string value  represents a partial name.  Since tModel data only has a single name,
   *                only a single name may be passed.  A wildcard character % may be used to signify any
   *                number of any characters. The returned tModelList contains tModelInfo elements for
   *                tModels whose name matches the value passed (via lexical-order - i.e., leftmost in
   *                left-to-right languages - partial match or wild card treatment).
   *
   * @param categoryBag
   *                This is a list of category references.  The returned tModelList contains tModelInfo
   *                elements matching all of the categories passed (logical AND by default).  FindQualifier
   *                can be used to alter this logical AND behavior.
   * @param identifierBag
   *                This is a list of business identifier references. The returned tModelList
   *                contains tModelInfo structures matching any of the identifiers
   *                passed (logical OR).
   * @param findQualifiers
   *                used to alter the default behavior of search functionality.
   * @param maxRows allows the requesting program to limit the number of results returned.
   * @return This function returns a YPFuture wrapping a tModelList on success.  In the event that no
   *         matches were located for the specified criteria, an empty tModelList
   *         object will be returned (e.g. will contain zero tModelInfo objects).
   *         This signifies zero matches.
   * @note The returned YPFuture will contain a TModelList when completed.
   */
  YPFuture find_tModel (String name,
                         CategoryBag categoryBag,
                         IdentifierBag identifierBag,
                         FindQualifiers findQualifiers,
                         int maxRows);
  /**
   * The get_bindingDetail message is for requesting the run-time
   * bindingTemplate information location information for the purpose of
   * invoking a registered business API.
   *
   * @param bindingKey uuid_key string that represent specific instance
   *                   of known bindingTemplate data.
   * @return This function returns a YPFuture wrapping a bindingDetail message on successful match
   * @note The returned YPFuture will contain a BindingDetail when completed.
   */
  YPFuture get_bindingDetail(String bindingKey);

  /**
   * The get_bindingDetail message is for requesting the run-time
   * bindingTemplate information location information for the purpose of
   * invoking a registered business API.
   *
   * @param bindingKeyStrings Vector of uuid_key strings that represent specific instances
   *                   of known bindingTemplate data.
   * @return This function returns a YPFuture wrapping a bindingDetail message on successful match of one
   *         or more bindingKey values.  If multiple bindingKey values were passed, the
   *         results will be returned in the same order as the keys passed.
   * @note The returned YPFuture will contain a BindingDetail when completed.
   */
  YPFuture get_bindingDetail(Vector bindingKeyStrings);

  /**
   * The get_businessDetail message returns complete businessEntity information
   * for one or more specified businessEntitys
   *
   * @param businessKey
   *               A uuid_key string that represents a specific instance of known
   *               businessEntity data.
   * @return This function returns a YPFuture wrapping a businessDetail object on successful match
   *         of one or more businessKey values.  If multiple businessKey values
   *         were passed, the results will be returned in the same order as the
   *         keys passed.
   * @note The returned YPFuture will contain a BusinessDetail when completed.
   */
  YPFuture get_businessDetail(String businessKey);

  /**
   * The get_businessDetail message returns complete businessEntity information
   * for one or more specified businessEntitys
   *
   * @param businessKeyStrings
   *               Vector of uuid_key strings that represent specific instances of known
   *               businessEntity data.
   * @return This function returns a YPFuture wrapping a businessDetail message on successful match
   *         of one or more businessKey values.  If multiple businessKey values
   *         were passed, the results will be returned in the same order as the
   *         keys passed.
   * @note The returned YPFuture will contain a BusinessDetail when completed.
   */
  YPFuture get_businessDetail(Vector businessKeyStrings);

  /**
   * The get_businessDetailExt message returns extended businessEntity
   * information for one or more specified businessEntitys.  This
   * message returns exactly the same information as the get_businessDetail
   * message, but may contain additional attributes if the source is
   * an external registry (not an Operator Site) that is compatible
   * with this API specification.
   *
   * @param businessKey
   *               A uuid_key string that represents a specific instance of known
   *               businessEntity data.
   * @return This function returns a YPFuture wrapping a businessDetailExt message on successful match
   *         of one or more businessKey values.  If multiple businessKey values
   *         were passed, the results will be returned in the same order as the
   *         keys passed.
   * @note The returned YPFuture will contain a BusinessDetailExt when completed.
   */
  YPFuture get_businessDetailExt(String businessKey);
  /**
   * The get_businessDetailExt message returns extended businessEntity
   * information for one or more specified businessEntitys.  This
   * message returns exactly the same information as the get_businessDetail
   * message, but may contain additional attributes if the source is
   * an external registry (not an Operator Site) that is compatible
   * with this API specification.
   *
   * @param businessKeyStrings
   *               Vector of uuid_key strings that represent specific instances of known
   *               businessEntity data.
   * @return This function returns a YPFuture wrapping a businessDetailExt message on successful match
   *         of one or more businessKey values.  If multiple businessKey values
   *         were passed, the results will be returned in the same order as the
   *         keys passed.
   * @note The returned YPFuture will contain a BusinessDetailExt when completed.
   */
  YPFuture get_businessDetailExt(Vector businessKeyStrings);

  /**
   * The get_serviceDetail message is used to request full information
   * about a known businessService structure.
   *
   * @param serviceKey A uuid_key string that represents a specific instance of
   *                   known businessService data.
   * @return This function returns a YPFuture wrapping a serviceDetail message on successful match
   *         of one or more serviceKey values.  If multiple serviceKey values
   *         were passed, the results will be returned in the same order as the
   *         keys passed.
   * @note The returned YPFuture will contain a ServiceDetail when completed.
   */
  YPFuture get_serviceDetail(String serviceKey);

  /**
   * The get_serviceDetail message is used to request full information
   * about a known businessService structure.
   *
   * @param serviceKeyStrings
   *               A vector of uuid_key strings that represent specific instances of
   *               known businessService data.
   * @return This function returns a YPFuture wrapping a serviceDetail message on successful match
   *         of one or more serviceKey values.  If multiple serviceKey values
   *         were passed, the results will be returned in the same order as the
   *         keys passed.
   * @note The returned YPFuture will contain a ServiceDetail when completed.
   */
  YPFuture get_serviceDetail(Vector serviceKeyStrings);

  /**
   * The get_tModelDetail message is used to request full information
   * about a known tModel structure.
   *
   * @param tModelKey A URN qualified uuid_key string that represent a specific
   *                  instance of known tModel data.  All tModelKey values begin with a
   *                  uuid URN qualifier (e.g. "uuid:" followed by a known tModel UUID value.)
   * @return This function returns a YPFuture wrapping a tModelDetail message on successful match
   *         of one or more tModelKey values.  If multiple tModelKey values
   *         were passed, the results will be returned in the same order as
   *         the keys passed.
   * @note The returned YPFuture will contain a TModelDetail when completed.
   */
  YPFuture get_tModelDetail(String tModelKey);

  /**
   * The get_tModelDetail message is used to request full information
   * about a known tModel structure.
   *
   * @param tModelKeyStrings
   *               A Vector of URN qualified uuid_key strings that represent specific
   *               instances of known tModel data.  All tModelKey values begin with a
   *               uuid URN qualifier (e.g. "uuid:" followed by a known tModel UUID value.)
   * @return This function returns a YPFuture wrapping a tModelDetail message on successful match
   *         of one or more tModelKey values.  If multiple tModelKey values
   *         were passed, the results will be returned in the same order as
   *         the keys passed.
   * @note The returned YPFuture will contain a TModelDetail when completed.
   */
  YPFuture get_tModelDetail(Vector tModelKeyStrings);

  /**
   * The add_publisherAssertions message is used to add relationship assertions to the
   * existing set of assertions.
   *
   * @param authInfo     Contains an authentication token. Authentication tokens are obtained
   *                     using the get_authToken method.
   * @param publisherAssertion    Contains a relationship assertion.
   * @return    This function returns a YPFuture wrapping a  DispositionReport with a single success indicator.
   * @note The returned YPFuture will contain a  DispositionReport when completed.
   */
  YPFuture add_publisherAssertions (String authInfo,
                                     PublisherAssertion publisherAssertion);

  /**
   * The add_publisherAssertions message is used to add relationship assertions to the
   * existing set of assertions.
   *
   * @param authInfo     Contains an authentication token. Authentication tokens are obtained
   *                     using the get_authToken method.
   * @param publisherAssertion    Vector of publisherAssertion object. Each publisherAssertion
   *                              contains a relationship assertion.
   * @return   This function returns a YPFuture wrapping a  DispositionReport with a single success indicator.
   * @note The returned YPFuture will contain a  DispositionReport when completed.
   */
  YPFuture add_publisherAssertions (String authInfo,
                                     Vector publisherAssertion);

  /**
   * The get_assertionStatusReport message is used to request a status
   * report containing publisher assertions and status information.  This contains
   * all complete and incomplete  assertions and serves an administrative use including
   * the determination if there are any outstanding, incomplete assertions about relationships
   * involving businesses the publisher account is associated with.
   *
   * @param authInfo    Contains an authentication token. Authentication tokens are obtained
   *                    using the get_authToken method.
   * @param completionStatus    This argument (String) lets the publisher restrict the result set
   *                            to only those relationships that have the status value specified.
   * @return    Upon successful completion, an assertionStatusReport message is returned
   *            containing assertion status information.
   * @note The returned YPFuture will contain a AssertionStatusReport when completed.
   */
  YPFuture get_assertionStatusReport(String authInfo,
                                      String completionStatus);

  /**
   * The get_assertionStatusReport message is used to request a status
   * report containing publisher assertions and status information. This contains
   * all complete and incomplete  assertions and serves an administrative use including
   * the determination if there are any outstanding, incomplete assertions about relationships
   * involving businesses the publisher account is associated with.
   *
   * @param authInfo    Contains an authentication token. Authentication tokens are obtained
   *                    using the get_authToken method.
   * @param completionStatus   This argument lets the publisher restrict the result set to
   *                           only those relationships that have the status value specified.
   * @return    Upon successful completion, an assertionStatusReport message is returned
   *            containing assertion status information.
   * @note The returned YPFuture will contain a AssertionStatusReport when completed.
   */
  YPFuture get_assertionStatusReport(String authInfo,
                                      CompletionStatus completionStatus);

  /**
   * The get_publisherAssertions message is used to get a list of active
   * publisher assertions that are controlled by an individual publisher account.
   *
   * @param authInfo    Contains an authentication token. Authentication tokens are obtained
   *                    using the get_authToken method.
   * @return This function returns a YPFuture wrapping a PublisherAssertions message that contains a
   *         publisherAssertion element for each publisher assertion registered by the
   *         publisher account associated with the authentication information.
   * @note The returned YPFuture will contain a PublisherAssertions when completed.
   */
  YPFuture get_publisherAssertions(String authInfo);

  /**
   * The delete_binding message causes one or more bindingTemplate to be deleted.
   *
   * @param authInfo   Contains an authentication token. Authentication tokens are obtained
   *                   using the get_authToken method.
   * @param bindingKey A uuid_key value that represents a specific instance of
   *                   known bindingTemplate data.
   * @return Upon successful completion, a dispositionReport is returned with a
   *         single success indicator.
   * @note The returned YPFuture will contain a DispositionReport when completed.
   */
  YPFuture delete_binding(String authInfo,
                           String bindingKey);

  /**
   * The delete_binding message causes one or more bindingTemplate to be deleted.
   *
   * @param authInfo Contains an authentication token. Authentication tokens are obtained
   *                 using the get_authToken method.
   * @param bindingKeyStrings
   *                 A vector of uuid_key strings that represents specific instances of
   *                 known bindingTemplate data.
   * @return Upon successful completion, a dispositionReport is returned with a
   *         single success indicator
   * @note The returned YPFuture will contain a DispositionReport when completed.
   */
  YPFuture delete_binding(String authInfo,
                           Vector bindingKeyStrings);

  /**
   * The delete_business message is used to remove one or more
   * businessEntity structures.
   *
   * @param authInfo Contains an authentication token. Authentication tokens are obtained
   *                 using the get_authToken method.
   * @param businessKey
   *                 Uuid_key string that represents specific instance of known
   *                 businessEntity data.
   * @return Upon successful completion, a dispositionReport is returned with a
   *         single success indicator
   * @note The returned YPFuture will contain a DispositionReport when completed.
   */
  YPFuture delete_business(String authInfo,
                            String businessKey);

  /**
   * The delete_business message is used to remove one or more
   * businessEntity structures.
   *
   * @param authInfo Contains an authentication token. Authentication tokens are obtained
   *                 using the get_authToken method.
   * @param businessKeyStrings
   *                 Vector of uuid_key strings that represent specific instances of known
   *                 businessEntity data.
   * @return Upon successful completion, a dispositionReport is returned with a
   *         single success indicator
   * @note The returned YPFuture will contain a DispositionReport when completed.
   */
  YPFuture delete_business(String authInfo,
                            Vector businessKeyStrings);

  /**
   * The delete_service message is used to remove one or more
   * businessService structures.
   *
   * @param authInfo   Contains an authentication token. Authentication tokens are obtained
   *                   using the get_authToken method.
   * @param serviceKey uuid_key string that represents specific instance of known
   *                   businessService data.
   * @return Upon successful completion, a dispositionReport is returned with a
   *         single success indicator
   * @note The returned YPFuture will contain a DispositionReport when completed.
   */
  YPFuture delete_service(String authInfo,
                           String serviceKey);

  /**
   * The delete_service message is used to remove one or more
   * businessService structures.
   *
   * @param authInfo Contains an authentication token. Authentication tokens are obtained
   *                 using the get_authToken method.
   * @param serviceKeyStrings
   *                 Vector of uuid_key strings that represent specific instances of known
   *                 businessService data.
   * @return Upon successful completion, a dispositionReport is returned with a
   *         single success indicator
   * @note The returned YPFuture will contain a DispositionReport when completed.
   */
  YPFuture delete_service(String authInfo,
                           Vector serviceKeyStrings);

  /**
   * The delete_tModel message is used to remove or retire one or more
   * tModel structures.
   *
   * @param authInfo  Contains an authentication token. Authentication tokens are obtained
   *                  using the get_authToken method.
   * @param tModelKey uuid_key string that represents specific instance of known
   *                  tModel data.
   * @return Upon successful completion, a dispositionReport is returned with a
   *         single success indicator
   * @note The returned YPFuture will contain a DispositionReport when completed.
   */
  YPFuture delete_tModel(String authInfo,
                          String tModelKey);

  /**
   * The delete_tModel message is used to remove or retire one or more
   * tModel structures.
   *
   * @param authInfo Contains an authentication token. Authentication tokens are obtained
   *                 using the get_authToken method.
   * @param tModelKeyStrings
   *                 Vector of uuid_key strings that represent specific instances of known
   *                 tModel data.
   * @return Upon successful completion, a dispositionReport is returned with a
   *         single success indicator
   * @note The returned YPFuture will contain a DispositionReport when completed.
   */
  YPFuture delete_tModel(String authInfo,
                          Vector tModelKeyStrings);

  /**
   * The delete_publisherAssertions message is used to delete specific publisher assertions
   * from the assertion collection controlled by a particular publisher account. Deleting assertions from
   * the assertion collection will affect the visibility of business relationships.  Deleting an assertion
   * will cause any relationships based on that assertion to be invalidated.
   *
   * @param authInfo    Contains an authentication token. Authentication tokens are obtained
   *                    using the get_authToken method.
   * @param publisherAssertion   Contains a relationship assertion.
   *
   * @return Upon successful completion, a dispositionReport is returned with a
   *         single success indicator
   * @note The returned YPFuture will contain a DispositionReport when completed.
   */
  YPFuture delete_publisherAssertions(String authInfo,
                                       PublisherAssertion publisherAssertion)
    throws UDDIException;

  /**
   * The delete_publisherAssertions message is used to delete specific publisher assertions
   * from the assertion collection controlled by a particular publisher account. Deleting assertions from
   * the assertion collection will affect the visibility of business relationships.  Deleting an assertion
   * will cause any relationships based on that assertion to be invalidated.
   *
   * @param authInfo    Contains an authentication token. Authentication tokens are obtained
   *                    using the get_authToken method.
   * @param publisherAssertion   Is a vector of publisherAssertion values. Each publisherAssertion
   *                             contains a relationship assertion.
   * @return Upon successful completion, a dispositionReport is returned with a
   *         single success indicator
   * @note The returned YPFuture will contain a DispositionReport when completed.
   */
  YPFuture delete_publisherAssertions(String authInfo,
                                       Vector publisherAssertion)
    throws UDDIException;


  /**
   * The discard_authToken message is used to inform an Operator Site that the
   * authentication token can be discarded.  Subsequent calls that use the
   * same authToken may be rejected.  This message is optional for Operator
   * Sites that do not manage session state or that do not support the
   * get_authToken message.
   *
   * @param authInfo Contains an String authentication token. Authentication tokens are obtained
   *                 using the get_authToken method.
   * @return Upon successful completion, a dispositionReport is returned with a
   *         single success indicator.  Discarding an expired authToken will be
   *         processed and reported as a success condition.
   * @note The returned YPFuture will contain a DispositionReport when completed.
   */
  YPFuture discard_authToken(String authInfo);
  /**
   * The discard_authToken message is used to inform an Operator Site that the
   * authentication token can be discarded.  Subsequent calls that use the
   * same authToken may be rejected.  This message is optional for Operator
   * Sites that do not manage session state or that do not support the
   * get_authToken message.
   *
   * @param authInfo Contains an authentication token. Authentication tokens are obtained
   *                 using the get_authToken method.
   * @return Upon successful completion, a dispositionReport is returned with a
   *         single success indicator.  Discarding an expired authToken will be
   *         processed and reported as a success condition.
   * @note The returned YPFuture will contain a DispositionReport when completed.
   */
  YPFuture discard_authToken(AuthInfo authInfo);

  /**
   * The get_authToken message is used to obtain an authentication token.
   * Authentication tokens are opaque values that are required for all
   * other publisher API calls.  This message is not required for Operator
   * Sites that have an external mechanism defined for users to get an
   * authentication token.  This API is provided for implementations that
   * do not have some other method of obtaining an authentication token or
   * certificate, or that choose to use userID and Password based authentication.
   *
   * @param userid user that an individual authorized user was assigned by an Operator Site.
   *               Operator Sites will each provide a way for individuals to obtain a UserID
   *               and password that will be valid only at the given Operator Site.
   * @param cred   password or credential that is associated with the user.
   * @return This function returns a YPFuture wrapping an authToken object that contains a valid
   *         authInfo object that can be used in subsequent calls to publisher
   *         API calls that require an authInfo value.
   * @note The returned YPFuture will contain a AuthToken when completed.
   */
  YPFuture get_authToken(String userid,
                          String cred);


  /**
   * The get_registeredInfo message is used to get an abbreviated list
   * of all businessEntity keys and tModel keys that are controlled by
   * the individual associated the credentials passed.
   *
   * @param authInfo Contains an authentication token.  Authentication tokens are obtained
   *                 using the get_authToken API call.
   * @return The function returns a YPFuture wrapping upon successful completion, a registeredInfo object will be returned,
   *         listing abbreviated business information in one or more businessInfo
   *         objects, and tModel information in one or more tModelInfo objects.
   *         This API is useful for determining the full extent of registered
   *         information controlled by a single user in a single call.
   * @note The returned YPFuture will contain a RegisteredInfo when completed.
   */
  YPFuture get_registeredInfo(String authInfo);

  /**
   * The save_binding message is used to save or update a complete
   * bindingTemplate structure.  This message can be used to add or
   * update one or more bindingTemplate structures to one or more existing
   * businessService structures.
   *
   * @param authInfo Contains an authentication token. Authentication tokens are obtained
   *                 using the get_authToken method.
   * @param bindingTemplates
   *                 Vector of bindingTemplate objects.  The order in which these are
   *                 processed is not defined.  To save a new bindingTemplate, pass a
   *                 bindingTemplate object with an empty bindingKey attribute value.
   * @return This API returns a bindingDetail object containing the final results
   *         of the call that reflects the newly registered information for the
   *         effected bindingTemplate objects.
   * @note The returned YPFuture will contain a BindingDetail when completed.
   */
  YPFuture save_binding(String authInfo,
                         Vector bindingTemplates);

  /**
   * The save_business message is used to save or update information about a
   * complete businessEntity structure.  This API has the broadest scope of
   * all of the save_x API calls in the publisher API, and can be used to make
   * sweeping changes to the published information for one or more
   * businessEntity structures controlled by an individual.
   *
   * @param authInfo Contains an authentication token. Authentication tokens are obtained
   *                 using the get_authToken method.
   * @param businessEntities
   *                 Vector of businessEntity objects.  These objects can be obtained in advance
   *                 by using the get_businessDetail API call or by any other means.
   * @return This API returns a businessDetail message containing the final results
   *         of the call that reflects the new registered information for the
   *         businessEntity information provided.
   * @note The returned YPFuture will contain a BusinessDetail when completed.
   */
  YPFuture save_business(String authInfo,
                          Vector businessEntities);

  /**
   * The save_service message adds or updates one or more businessService
   * structures.
   *
   * @param authInfo Contains an authentication token. Authentication tokens are obtained
   *                 using the get_authToken method.
   * @param businessServices
   *                 Vector of businessService objects.  These objects can be obtained in
   *                 advance by using the get_serviceDetail API call or by any other means.
   * @return This API returns a serviceDetail object containing the final results
   *         of the call that reflects the newly registered information for the
   *         effected businessService structures.
   * @note The returned YPFuture will contain a ServiceDetail when completed.
   */
  YPFuture save_service(String authInfo,
                         Vector businessServices);

  /**
   * The save_tModel message adds or updates one or more tModel structures.
   *
   * @param authInfo Contains an authentication token. Authentication tokens are obtained
   *                 using the get_authToken method.
   * @param tModels  Vector of complete tModel structures.  If adding a new tModel,
   *                 the tModelKey value should be passed as an empty element.
   * @return This API returns a tModelDetail message containing the final results
   *         of the call that reflects the new registered information for the
   *         effected tModel structures
   * @note The returned YPFuture will contain a TModelDetail when completed.
   */
  YPFuture save_tModel(String authInfo,
                        Vector tModels);

  /**
   * The set_publisherAssertions message is used to save the complete set of publisher
   * assertions for an individual publisher account. When this message is processed, the publisher
   * assertions that are active prior to this API call for a given publisher account are examined by the
   * UDDI registry. Any new assertions not present prior to the call are added to the assertions attributed
   * to the publisher. As a result, new relationships may be activated (e.g. determined to have a completed
   * status), and existing relationships may be deactivated.
   *
   * @param authInfo    Contains an authentication token. Authentication tokens are obtained
   *                    using the get_authToken method.
   * @param pub     Contains a relationship assertion.
   * @return   Upon successful completion, a publisherAssertions message is returned containing
   *           all of the relationship assertions currently attributed to the publisher account
   *           that is associated with the authInfo data passed.
   * @note The returned YPFuture will contain a PublisherAssertions when completed.
   */
  YPFuture set_publisherAssertions (String  authInfo,
                                     PublisherAssertion  pub);

  /**
   * The set_publisherAssertions message is used to save the complete set of publisher
   * assertions for an individual publisher account. When this message is processed, the publisher
   * assertions that are active prior to this API call for a given publisher account are examined by the
   * UDDI registry. Any new assertions not present prior to the call are added to the assertions attributed
   * to the publisher. As a result, new relationships may be activated (e.g. determined to have a completed
   * status), and existing relationships may be deactivated.
   *
   * @param authInfo    Contains an authentication token. Authentication tokens are obtained
   *                    using the get_authToken method.
   * @param publisherAssertion    Is a vector of publisherAssertion object. Each publisherAssertion
   *                              contains a relationship assertion.
   * @return   Upon successful completion, a publisherAssertions message is returned containing
   *           all of the relationship assertions currently attributed to the publisher account
   *           that is associated with the authInfo data passed.
   * @note The returned YPFuture will contain a  PublisherAssertions when completed.
   */
  YPFuture set_publisherAssertions (String  authInfo,
                                     Vector publisherAssertion);

  /**
   * A UDDI operator sends the validate_values message to the appropriate external service, whenever a
   * publisher saves data that uses a categorization value or identifier whose use is regulated by the
   * external party who controls that service. The normal use is to verify that specific categories or
   * identifiers (checking the keyValue attribute values supplied) exist within the given taxonomy or
   * identifier system
   *
   * @param businessEntity
   *                  The vector of businessEntity structure being validated.
   * @return Upon successful completion, a dispositionReport is returned with a
   *         single success indicator.
   * @note The returned YPFuture will contain a DispositionReport when completed.
   */
  YPFuture validate_values_businessEntity(Vector businessEntity);

  /**
   * A UDDI operator sends the validate_values message to the appropriate external service, whenever a
   * publisher saves data that uses a categorization value or identifier whose use is regulated by the
   * external party who controls that service. The normal use is to verify that specific categories or
   * identifiers (checking the keyValue attribute values supplied) exist within the given taxonomy or
   * identifier system
   *
   * @param businessService
   *                  The vector of business service structure being validated.
   * @return Upon successful completion, a dispositionReport is returned with a
   *         single success indicator.
   * @note The returned YPFuture will contain a DispositionReport when completed.
   */
  YPFuture validate_values_businessService(Vector businessService);

  /**
   * A UDDI operator sends the validate_values message to the appropriate external service, whenever a
   * publisher saves data that uses a categorization value or identifier whose use is regulated by the
   * external party who controls that service. The normal use is to verify that specific categories or
   * identifiers (checking the keyValue attribute values supplied) exist within the given taxonomy or
   * identifier system
   *
   * @param tModel    The vector of tModel structure being validated.
   * @return Upon successful completion, a dispositionReport is returned with a
   *         single success indicator.
   * @note The returned YPFuture will contain a DispositionReport when completed.
   */
  YPFuture validate_values_tModel(Vector tModel);

  /**
   * Sends a UDDIElement to either the inquiry or publish URL.
   *
   * @param el
   * @param inquiry
   * @return An element representing a XML DOM tree containing the UDDI response.
   * @note The returned YPFuture will contain a Element when completed.
   */
  YPFuture send(UDDIElement el, boolean inquiry);

  /**
   * Sends an XML DOM tree indentified by the given element to either the
   * inquiry or publish URL. Can be used to send an manually constructed
   * message to the UDDI registry.
   *
   * @param el
   * @param inquiry
   * @return An element representing a XML DOM tree containing the UDDI response.
   * @note The returned YPFuture will contain a Element when completed.
   */
  YPFuture send(Element el, boolean inquiry);

  // Extra method for sending queries without blackboard involvement.
  
  YPFuture execute(YPFuture pendingQuery);
}

/*
class SamplePlugin {
  void execute() {
    final BlackboardService bb = getBlackboardService();
    Runnable waker = new Runnable() {
        public void run() { 
          bb.signalClientActivity();
        };
      };
    String context = "geographic";  // geographic context, looks it up in community for most-local YP server
    
    YPProxy yp = ypservice.getYPProxy(context);
    // option1 - make your own uddi4j object
    // YPFuture q1 = yp.send(new GetAuthToken(userid, cred), true);
    // option2 - proxy-like api
    YPFuture q2 = yp.getAuthToken(userid, cred);

    publishAdd(q2);             // implicitly sends query
    
    if (q.isReady()) {
      AuthToken token = (AuthToken) q.get();
     }


     // replacement for old mechanism
     // old
     AuthToken token = uddiproxy.getAuthToken(userid,cred);
     // new
     AuthToken token = (AuthToken) (yp.execute(yp.getAuthToken(userid,cred))).get();
  }
}
*/




