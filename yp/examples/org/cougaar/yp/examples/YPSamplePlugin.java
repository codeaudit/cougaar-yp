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

package org.cougaar.yp.examples;

import org.cougaar.yp.*;

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
  int count = 0;

  long startTime = 0L;

  private Object flock = new Object();
  YPFuture fut = null;
  void setFut(YPFuture f) { 
    synchronized (flock) {
      fut = f;
    }
  }
  YPFuture getFut() {
    synchronized (flock) {
      return fut;
    }
  }


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
    setFut(yp.find_business(names, null, null, null,null,findQualifiers,5));
    count++;
    if (count == 5) { startTime = System.currentTimeMillis(); }
    //System.err.println("Issued query "+count); //+": "+fut
    //System.err.println("ISSUE "+getFut());
    blackboard.publishAdd(getFut());
    return getFut();
  }

  void report(YPFuture f) {
    //System.err.println("Closing query "+count); //+": "+fut
    try {
      Object o = f.get();
      if (o instanceof BusinessList) {
        BusinessList businessList = (BusinessList)o;
        

        Vector businessInfoVector  = businessList.getBusinessInfos().getBusinessInfoVector();
        for (int i = 0; i < businessInfoVector.size(); i++) {
          BusinessInfo businessInfo = (BusinessInfo)businessInfoVector.elementAt(i);

          // Print name for each business
          //System.err.println("report "+count+": business " + i + " = " + businessInfo.getNameString());
        }
      } else {
        System.err.println("Didn't get a BusinessList response: "+o);
      }
    } catch (Exception e) {
      System.err.println("Error reporting on "+f);
      e.printStackTrace();
    }
    //System.err.println("CLOSE "+f);
    blackboard.publishRemove(f);
    long et = System.currentTimeMillis();
    double rate = (((double) ((count-5)+1))/(et - startTime))*1000.0;
    System.err.println("Sample rate="+rate+"t/s");
    setFut(null);
  }

  protected void execute() {
    Collection changed = futures.getChangedCollection();
    for (Iterator it = changed.iterator(); it.hasNext(); ) {
      YPFuture f = (YPFuture) it.next();
      if (f.isReady()) {
        report(f);  // sets fut=null
        issue();   // sets fut=new fut
      } else {
        System.err.println("Query changed but not ready: "+f);
      }
    }
  }
}
