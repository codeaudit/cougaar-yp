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

// import all of uddi4j
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

import java.io.*;
import java.net.*;
import java.util.*;

import org.cougaar.core.component.*;
import org.cougaar.core.mts.*;
import org.cougaar.util.log.*;

// the next three are solely for the standalone test
import org.uddi4j.transport.*;
import org.w3c.dom.Element;
import org.w3c.dom.Node;


/** This is a trivial YP service tester.
 * It may be run in two ways:  Either as a standalone class (e.g. via main()) or
 * as a plugin/component by adding the appropriate line in 
 * a society config file, e.g. "plugin = org.cougaar.yp.YPTest(A)" where "A" is the
 * name of an agent which has a YPServer loaded and the current agent has
 * YPClientComponent loaded).
 */

public class YPTest extends ComponentSupport {
  private static final Logger logger = Logging.getLogger(YPServer.class);

  private String arg = "Unknown";

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

  public void initialize() {
    super.initialize();
    System.err.println("YPTest running.");

    YPService yps = (YPService) getServiceBroker().getService(this, YPService.class, null);

    String context = arg;

    UDDIProxy proxy = yps.getYP(context);

    test(proxy);
    System.err.println("YPTest done.");
  }

  String user = "cougaar";
  String pass = "cougaarPass";
  String businessName = "Sample Co";


  public void test(UDDIProxy proxy) {
    testPutBusiness(proxy);
    testGetBusiness(proxy);
  }

  public void testPutBusiness(UDDIProxy proxy) {
    try {
      // Get an authorization token
      System.out.println("\nGet authtoken");

      // Pass in userid and password registered at the UDDI site
      AuthToken token = proxy.get_authToken(user, pass);

      System.out.println("Returned authToken:" + token.getAuthInfoString());

      System.out.println("\nSave '" + businessName + "'");

      // Create minimum required data objects
      Vector entities = new Vector();

      // Create a new business entity using required elements constructor
      // Name is the business name. BusinessKey must be "" to save a new
      // business
      BusinessEntity be = new BusinessEntity("", businessName);
      entities.addElement(be);

      // Save business
      BusinessDetail bd = proxy.save_business(token.getAuthInfoString(),entities);

      // Process returned BusinessDetail object
      Vector businessEntities = bd.getBusinessEntityVector();
      BusinessEntity returnedBusinessEntity = (BusinessEntity)(businessEntities.elementAt(0));
      System.out.println("Returned businessKey:" + returnedBusinessEntity.getBusinessKey());

      // Find all businesses that start with that particular letter e.g. 'S' for 'Sample Business'.
      String businessNameLeadingSubstring = businessName.substring (0,1);
      System.out.println("\nListing businesses starting with " + businessNameLeadingSubstring
                         + " after we publish");

      //creating vector of Name Object
      Vector names = new Vector();
      names.add(new Name(businessNameLeadingSubstring));

      // Setting FindQualifiers to 'caseSensitiveMatch'
      FindQualifiers findQualifiers = new FindQualifiers();
      Vector qualifier = new Vector();
      qualifier.add(new FindQualifier("caseSensitiveMatch"));
      findQualifiers.setFindQualifierVector(qualifier);

      // Find businesses by name
      // And setting the maximum rows to be returned as 5.
      BusinessList businessList = proxy.find_business(names, null, null, null,null,findQualifiers,5);

      Vector businessInfoVector  = businessList.getBusinessInfos().getBusinessInfoVector();
      for (int i = 0; i < businessInfoVector.size(); i++) {
        BusinessInfo businessInfo = (BusinessInfo)businessInfoVector.elementAt(i);
        System.out.println(businessInfo.getNameString());
      }

      // Handle possible errors
    } catch (UDDIException e) {
      DispositionReport dr = e.getDispositionReport();
      if (dr!=null) {
        System.out.println("UDDIException faultCode:" + e.getFaultCode() +
                           "\n operator:" + dr.getOperator() +
                           "\n generic:"  + dr.getGeneric() +
                           "\n errno:"    + dr.getErrno() +
                           "\n errCode:"  + dr.getErrCode() +
                           "\n errInfoText:" + dr.getErrInfoText());
      }
      e.printStackTrace();

      // Catch any other exception that may occur
    } catch (Exception e) {
      e.printStackTrace();
    }
  } 

  public void testGetBusiness(UDDIProxy proxy) {
    try {
      //creating vector of Name Object
      Vector names = new Vector();
      names.add(new Name("S"));

      // Setting FindQualifiers to 'caseSensitiveMatch'
      FindQualifiers findQualifiers = new FindQualifiers();
      Vector qualifier = new Vector();
      qualifier.add(new FindQualifier("caseSensitiveMatch"));
      findQualifiers.setFindQualifierVector(qualifier);

      // Find businesses by name
      // And setting the maximum rows to be returned as 5.
      BusinessList businessList = proxy.find_business(names, null, null, null,null,findQualifiers,5);

      Vector businessInfoVector  = businessList.getBusinessInfos().getBusinessInfoVector();
      for (int i = 0; i < businessInfoVector.size(); i++) {
        BusinessInfo businessInfo = (BusinessInfo)businessInfoVector.elementAt(i);

        // Print name for each business
        System.out.println(businessInfo.getNameString());
      }

      // Handle possible errors
    } catch (UDDIException e) {
      DispositionReport dr = e.getDispositionReport();
      if (dr!=null) {
        System.out.println("UDDIException faultCode:" + e.getFaultCode() +
                           "\n operator:" + dr.getOperator() +
                           "\n generic:"  + dr.getGeneric() +
                           "\n errno:"    + dr.getErrno() +
                           "\n errCode:"  + dr.getErrCode() +
                           "\n errInfoText:" + dr.getErrInfoText());
      }
      e.printStackTrace();

      // Catch any other exception that may occur
    } catch (Exception ex) {
      ex.printStackTrace();
    }
  }


  /** this is a standalone test which doesn't require a
   * cougaar society.  It does not test the Messaging infrastructure
   * but executes the same actual UDDI operations as the society 
   * test does.
   **/

  public static void main(String[] arg) {
    YPServer yp = new YPServer();
    yp.initDB();
    yp.initUDDI();
    StandaloneYPTransport transport = new StandaloneYPTransport(yp);

    UDDIProxy proxy = new UDDIProxy(transport); // BBN Extension to uddi4j

    try {
      URL iurl = new URL("http","zoop", "frotz");
      URL purl = new URL("https","zart", "glorp");
      proxy.setInquiryURL(iurl);
      proxy.setPublishURL(purl);
    } catch (MalformedURLException e) { 
      // cannot happen
    }


    new YPTest().test(proxy);
  }
  
  private static class StandaloneYPTransport extends TransportBase {
    private YPServer yp;
    StandaloneYPTransport(YPServer yp) {
      this.yp = yp;
    }
    /** Send the DOM element specified to the URL as interpreted by the MTS **/
    public Element send(Element el, java.net.URL url) throws TransportException {
      logger.debug("Transported query "+el);
      describeElement(el);
      Element resp = yp.executeQuery(serialize(el));
      logger.debug("Sending Response "+resp);
      describeElement(resp);
      return serialize(resp);
    }
    private Element serialize(Element el) {
      try {
      ByteArrayOutputStream outbytes = new ByteArrayOutputStream();
      ObjectOutputStream oos = new ObjectOutputStream(outbytes);
      oos.writeObject(el);
      oos.close();
      ByteArrayInputStream inbytes = new ByteArrayInputStream(outbytes.toByteArray());
      ObjectInputStream ois = new ObjectInputStream(inbytes);
      Element rv = (Element) ois.readObject();
      ois.close();
      return rv;
      } catch (Exception e) {
        e.printStackTrace();
        return null;
      }
    }
  }



  // hack XML printer for debugging
  static void describeElement(Node el) { describeElement(el,""); }
  static void describeElement(Node el,String prefix) { 
    System.out.println(prefix+el);
    String pn = prefix+" ";
    if (el.hasChildNodes()) {
      for (Node c = el.getFirstChild(); c!= null; c = c.getNextSibling()) {
        describeElement(c, pn);
      }
    }
  }


  /*
    // this was from the original JAXR code - preserved because I want to 
    // add asynchronous mode to the UDDI interface.
    {
    try {
      // YPQuery implements BusinessQueryManager
      q.findOrganizations(fQualifiers, names, null, null, null, null);

      // blocking wait
      {
        YPResponse r = yps.submitQuery(q, null);
        try {
          r.waitForIsAvailable();
        } catch (InterruptedException ie) {}
        report(r);
      }

      // polling wait
      {
        YPResponse r = yps.submitQuery(q, null);
        while (! r.isAvailable()) {
          try {
            Thread.sleep(1000);       // wait a sec
          } catch (InterruptedException ie) {}
        }
        report(r);
      }

      // callback
      {
        Callback callback = new Callback();
        YPResponse r = yps.submitQuery(q, callback);
        //r.addCallback(callback); // will callback immediately if already has the answer
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
    { if (!called) this.wait(); }
    public synchronized void waitForCallback(long timeout) throws InterruptedException
    { if (!called) this.wait(timeout); }
  }
  */

}

