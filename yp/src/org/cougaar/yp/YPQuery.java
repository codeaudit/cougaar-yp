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

import org.cougaar.core.component.Service;

/** A YPQuery is an implementation of the JAXR BusinessQueryManager.
 * Since YP Queries are generally just packaged up for sending across the wire
 * rather than executed immediately (and to simplify Binder design),
 * all of the methods JAXR methods actually just return null;
 * @note It is undefined to call more than one query method.
 **/
public interface YPQuery extends BusinessQueryManager, Serializable {
  /** addProperties allows the querier to add arbitrary properties to the 
   * query to be interpreted by the responding server.  For example,
   * a JAXR server with a UDDI backend will probably need various types of connection
   * information, perhaps as provided by the WP service. 
   * @note addProperties may be called more than once.  The props argument is copied into 
   * an internal object so the argument may be reused.
   **/
  void addProperties(Properties props);

  /** Any (server) properties assigned to this query.
   * @note It is undefined to modify the returned value.
   */
  Properties getProperties();

  /** Execute a query against a real BusinessQueryManager.
   * This method always returns a valid YPResponse instance, never
   * null or JAXRException.  Rather, if there is a problem, it will
   * return a YPResponse with a getStatus() of FAILURE and
   * a non-null getExceptions().
   * @return a YPResponse with the answer.
   * @note This method is used by the YP application.  It is unlikely to be useful
   * or interesting for clients, unlike the rest of the API.
   **/
  YPResponse execute(BusinessQueryManager bqm);

}
