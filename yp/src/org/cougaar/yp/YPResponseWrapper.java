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

/** Implement YPResponse as a wrapper around some other response type.
 * This class doesn't implement the YPReponse wait/callback methods - they
 * all return immediately having done nothing.
 **/
class YPResponseWrapper implements YPResponse {
  /** Delegatee BR **/
  protected BulkResponse response;
  private YPResponseWrapper(BulkResponse o) { response = o; }

  // JAXRResponse
  public String getRequestId() throws JAXRException { return response.getRequestId(); }
  public int getStatus() throws JAXRException { return response.getStatus(); }
  public boolean isAvailable() throws JAXRException { return response.isAvailable(); }
  // BulkResponse
  public Collection getCollection() throws JAXRException { return response.getCollection(); }
  public Collection getExceptions() throws JAXRException { return response.getExceptions(); }
  public boolean isPartialResponse() throws JAXRException { return response.isPartialResponse(); }

  // YPResponse
  /** Does nothing in this implementation **/
  public void waitForIsAvailable() { }
  /** Does nothing in this implementation **/
  public void waitForIsAvailable(long timeout) { }
  /** Does nothing in this implementation **/
  public void addCallback(Runnable callback) { callback.run(); }

  //
  // implement the factories (used by YPQueryImpl.execute())
  //
  static YPResponse wrapBulkResponse(BulkResponse r) {
    return new YPResponseWrapper(r);
  }
  static YPResponse wrapClassificationScheme(ClassificationScheme r) {
    return new CompleteResponse(r);
  }
  static YPResponse wrapConcept(Concept r) {
    return new CompleteResponse(r);
  }
  static YPResponse wrapRegistryObject(RegistryObject r) {
    return new CompleteResponse(r);
  }
  static YPResponse wrapRegistryService(RegistryService r) {
    return new CompleteResponse(r);
  }
  static YPResponse wrapJAXRException(JAXRException r) {
    return new ExceptionResponse(r);
  }


  // these guys are actually alternative implementations of YPReponse rather than subclasses
  // of YPResponseWrapper
  private static class CompleteResponse implements YPResponse {
    protected Object response;
    CompleteResponse(Object o) { response = o; }
    // JAXRResponse
    public String getRequestId() { return "Immediate"; }
    public int getStatus() { return STATUS_SUCCESS; }
    public boolean isAvailable() { return true; }
    // BulkResponse
    public Collection getCollection(){ return Collections.singletonList(response); }
    public Collection getExceptions() { return Collections.EMPTY_LIST; }
    public boolean isPartialResponse() { return false; }
    // YPResponse
    public void waitForIsAvailable() { }
    public void waitForIsAvailable(long timeout) { }
    public void addCallback(Runnable callback) { callback.run(); }
  }
  
  private static class ExceptionResponse extends CompleteResponse {
    ExceptionResponse(JAXRException e) { super(e); }
    public int getStatus() { return STATUS_FAILURE; }
    public Collection getCollection() { return Collections.EMPTY_LIST; }
    public Collection getExceptions() { return Collections.singletonList(response); }
  }

}
    


