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

public interface YPService extends Service {
  /** Submit a YPQuery object for execution.  Queries may be resubmitted.
   * @returns a YPResponse eventually containing the answer.
   * @param callback If supplied, the callback's run() method will be invoked when
   * YPResponse.isAvailable() will return true.  
   * @note It is possible for the callback to be invoked before submitQuery returns, even
   * in the thread of the caller.
   **/
  YPResponse submitQuery(YPQuery query, Runnable callback);
}
