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


/** A plugin which uses large numbers of blocking YP Service queries to 
 * try to crush the YPServer.  Also good for testing interactions
 * with blackboard, etc.  Argument is a tag used for diagnostics.  Also,
 * if the parameter is "Y", it'll make 10 YP queries per execute cycle instead
 * of just 1.
 **/
public class YPBasher extends ComponentPlugin {
  
  String user = "cougaar";
  String pass = "cougaarPass";
  static String sampleName = "Sample Co";
  static String sampleName2 = "Second Co";


  IncrementalSubscription sub;
  YPService yps;
  YPProxy yp;
  long count = 0L;
  long total = 0L;

  private Object arg = null;

  public void setParameter(Object p) {
    if (p != null) {
      if (p instanceof Collection) {
        Iterator it = ((Collection)p).iterator();
        Object ma = it.next();
        if (ma instanceof String) {
          arg = (String) ma;
        } else {
          System.err.println("First parameter not a string! "+ma);
        }
      } else {
        System.err.println("Parameter not a Collection! "+p);
      }
    } else {
      System.err.println("YPTest requires a parameter which names a YPServer agent!");
    }
  }

  public void setYPService(YPService yps) {
    yps = (YPService) getServiceBroker().getService(this, YPService.class, null);
    this.yp = yps.getAutoYP("A");
  }

  private Integer zing = new Integer(11);

  protected void setupSubscriptions() {
    sub = (IncrementalSubscription) blackboard.subscribe(new UnaryPredicate() {
        public boolean execute(Object o) { return o instanceof Integer; }
      });
    blackboard.publishAdd(zing);
  }

  public void execute() {
    System.err.println("*");
    if (arg.equals("Y")) {
      for (int i=0; i<10; i++) {
        bash();
      }
    } else {
      bash();
    }
    blackboard.publishChange(zing);
  }

  void bash() {
    // issue the query
    Vector names = new Vector();
    names.add(new Name("S"));
    
    // Setting FindQualifiers to 'caseSensitiveMatch'
    FindQualifiers findQualifiers = new FindQualifiers();
    Vector qualifier = new Vector();
    qualifier.add(new FindQualifier("caseSensitiveMatch"));
    findQualifiers.setFindQualifierVector(qualifier);
    long st = System.currentTimeMillis();
    count++;
    System.err.println("["+arg+" Open "+count+"]");
    YPFuture f = yp.find_business(names, null, null, null,null,findQualifiers,5);
    // get result
    //System.err.println("Closing query "+count); //+": "+fut
    try {
      Object o = f.get();
      System.err.println("["+arg+" Closed "+count+"]");
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

    long ft = System.currentTimeMillis();
    
    total = total+ (ft-st);
    double rate = (((double) count)/total)*1000.0;
    System.err.println("Basher rate="+rate+"t/s");
  }

}
