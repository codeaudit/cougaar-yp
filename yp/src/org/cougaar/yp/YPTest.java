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

public class YPTest {
  public static void main(String[] arg) {
    YPTest ypt = new YPTest();
    ypt.execute();
  }

  private void execute() {
    // normally, we'd do something like:
    //YPService yps = (YPService) myservicebroker.getService(this, YPService.class);
    // but so that we can test this in isolation:
    // hack to compile
    YPService yps = null;  // new FakeYPServer();

    String company = "%foo%";

    // create a query object
    YPQuery q = YP.newQuery();

    // can add arbitrary JAXR properties to query
    Properties props = new Properties();
    q.addProperties(props);
   
    // build up the real query
    Collection fQualifiers = new ArrayList();
    fQualifiers.add(FindQualifier.SORT_BY_NAME_DESC);

    Collection names = new ArrayList();
    names.add(company);         // wildcarding name for match
    
    try {
      // YPQuery implements BusinessQueryManager
      q.findOrganizations(fQualifiers, names, null, null, null, null);

      // blocking wait
      {
        YPResponse r = yps.submitQuery(q);
        try {
          r.waitForIsAvailable();
        } catch (InterruptedException ie) {}
        report(r);
      }

      // polling wait
      {
        YPResponse r = yps.submitQuery(q);
        while (! r.isAvailable()) {
          try {
            Thread.sleep(1000);       // wait a sec
          } catch (InterruptedException ie) {}
        }
        report(r);
      }

      // callback
      {
        YPResponse r = yps.submitQuery(q);
        Callback callback = new Callback();
        r.addCallback(callback); // will callback immediately if already has the answer
        try {
          callback.waitForCallback();
          report(r);
        } catch (InterruptedException ie) { }
      }
    } catch (JAXRException je) {
      je.printStackTrace();
    }
    // done
  }

  static class Callback implements Runnable {
    private boolean called = false;
    public synchronized void run() { called = true; this.notify(); }
    public synchronized boolean isCalled() { return called; }
    public synchronized void waitForCallback() throws InterruptedException
      { this.wait(); }
    public synchronized void waitForCallback(long timeout) throws InterruptedException
      { this.wait(timeout); }

  }

  /*
  static class FakeYPServer implements YPService {
  }
  */

  void report(BulkResponse br) throws JAXRException {
    if (br.getStatus() == JAXRResponse.STATUS_SUCCESS) {
      System.out.println("Successfully queried the " +
                         "registry for organization"); 
      Collection orgs = br.getCollection();
      System.out.println("Results found: " + orgs.size() + "\n");
      Iterator iter = orgs.iterator();
      while (iter.hasNext()) {
        Organization org = (Organization) iter.next();
        System.out.println("Organization Name: " +
                           getName(org));
        System.out.println("Organization Key: " +
                           org.getKey().getId());
        System.out.println("Organization Description: " +
                           getDescription(org));
        
        Collection services = org.getServices();
        Iterator siter = services.iterator();
        while (siter.hasNext()) {
          Service service = (Service) siter.next();
          System.out.println("\tService Name: " +
                             getName(service));
          System.out.println("\tService Key: " +
                             service.getKey().getId());
          System.out.println("\tService Description: " +
                             getDescription(service));
        }
      }
    } else {
      System.err.println("One or more JAXRExceptions " +
                         "occurred during the query operation:");
      Collection exceptions = br.getExceptions();
      Iterator iter = exceptions.iterator();
      while (iter.hasNext()) {
        Exception e = (Exception) iter.next();
        System.err.println(e.toString());
      }
    }
  }

  private String getName(RegistryObject ro) throws JAXRException {
    try {
      return ro.getName().getValue();
    } catch (NullPointerException npe) {
      return "";
    }
  }
    
  private String getDescription(RegistryObject ro) throws JAXRException {
    try {
      return ro.getDescription().getValue();
    } catch (NullPointerException npe) {
      return "";
    }
  }
}
