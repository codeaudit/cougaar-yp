/*
 * <copyright>
 *  Copyright 2002-2003 BBNT Solutions, LLC
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

import org.uddi4j.client.*;
import org.uddi4j.transport.*;

import org.w3c.dom.Element;

import java.io.*;
import java.net.*;
import java.util.*;

import org.cougaar.core.component.*;

import org.cougaar.core.mts.*;
import org.cougaar.core.agent.*;
import org.cougaar.core.agent.service.MessageSwitchService;

import org.cougaar.core.blackboard.*;
import org.cougaar.core.service.BlackboardService;
import org.cougaar.core.plugin.*;
import org.cougaar.util.*;

import org.cougaar.util.log.*;

import org.uddi4j.*;
import org.uddi4j.client.*;
import org.uddi4j.datatype.*;
import org.uddi4j.datatype.assertion.*;
import org.uddi4j.datatype.binding.*;
import org.uddi4j.datatype.business.*;
import org.uddi4j.datatype.service.*;
import org.uddi4j.datatype.tmodel.*;
import org.uddi4j.request.*;
import org.uddi4j.response.*;
import org.uddi4j.util.*;


public class YPSamplePlugin extends ComponentPlugin {
  
  String user = "cougaar";
  String pass = "cougaarPass";
  static String sampleName = "Sample Co";
  static String sampleName2 = "Second Co";


  IncrementalSubscription futures;
  YPService yps;
  YPProxy yp;
  YPFuture fut = null;
  int count = 0;

  long startTime = 0L;

  public void setYPService(YPService yps) {
    yps = (YPService) getServiceBroker().getService(this, YPService.class, null);
    this.yp = yps.getYP("A");
  }

  protected void setupSubscriptions() {
    futures = (IncrementalSubscription) blackboard.subscribe(new UnaryPredicate() {
        public boolean execute(Object o) { return (o instanceof YPFuture); }
      });
    issue();
  }

  YPFuture issue() {
    Vector names = new Vector();
    names.add(new Name("S"));
    
    // Setting FindQualifiers to 'caseSensitiveMatch'
    FindQualifiers findQualifiers = new FindQualifiers();
    Vector qualifier = new Vector();
    qualifier.add(new FindQualifier("caseSensitiveMatch"));
    findQualifiers.setFindQualifierVector(qualifier);
    fut = yp.find_business(names, null, null, null,null,findQualifiers,5);
    count++;
    if (count == 2) { startTime = System.currentTimeMillis(); }
    blackboard.publishAdd(fut);
    System.out.println("Issued query "+count+": "+fut);
    return fut;
  }

  void report() {
    System.out.println("Closing query "+count+": "+fut);
    try {
      BusinessList businessList = (BusinessList) fut.get();

      Vector businessInfoVector  = businessList.getBusinessInfos().getBusinessInfoVector();
      for (int i = 0; i < businessInfoVector.size(); i++) {
        BusinessInfo businessInfo = (BusinessInfo)businessInfoVector.elementAt(i);

        // Print name for each business
        //System.out.println("report "+count+": business " + i + " = " + businessInfo.getNameString());
      }
    } catch (Exception e) {
      e.printStackTrace();
    }
    blackboard.publishRemove(fut);
    long et = System.currentTimeMillis();
    double rate = (((double) (count-1))/(et - startTime))*1000.0;
    System.out.println("----------> Round trip rate = "+rate+" transactions/sec");
    fut = null;
  }

  protected void execute() {
    if (fut.isReady()) {
      report();
      issue();
    } else {
      System.out.println("&");
    }
  }

}
