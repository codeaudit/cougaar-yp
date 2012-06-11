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

package org.cougaar.yp.examples;
import org.cougaar.yp.*;

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
 * Use this as a plugin/component by adding the appropriate line in 
 * a society config file, e.g. "plugin = org.cougaar.yp.YPTest(A)" where "A" is the
 * name of an agent which has a YPServer loaded and the current agent has
 * YPClientComponent loaded).
 * @note Also see yp/configs/miniyp for a small society with the right parts to run this sample code.
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

  YPService yps = null;
  public void load() {
    super.load();
    System.err.println("YPTest running.");

    yps = (YPService) getServiceBroker().getService(this, YPService.class, null);

    String context = arg;

    YPProxy proxy = yps.getAutoYP(context);

    test(proxy);
    System.err.println("YPTest done.");
  }

  String user = "cougaar";
  String pass = "cougaarPass";
  static String sampleName = "Sample Co";
  static String sampleName2 = "Second Co";


  public void test(YPProxy proxy) {
    test (proxy, sampleName);
  }

  public void test(YPProxy proxy, String bName) {
    testPutBusiness(proxy, bName);
    testGetBusiness(proxy);
    try { Thread.sleep (10000); }
    catch (Exception e) { System.out.println ("Interrupted sleep"); }
    testGetBusiness(proxy);
  }

  public void testPutBusiness(YPProxy proxy, String businessName) {
    try {
      // Get an authorization token
      System.out.println("\nGet authtoken");

      // Pass in userid and password registered at the UDDI site
      AuthToken token = (AuthToken) proxy.get_authToken(user, pass).get();

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
      BusinessDetail bd = (BusinessDetail) proxy.save_business(token.getAuthInfoString(),entities).get();

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
      BusinessList businessList = (BusinessList) proxy.find_business(names, null, null, null,null,findQualifiers,5).get();

      Vector businessInfoVector  = businessList.getBusinessInfos().getBusinessInfoVector();
      for (int i = 0; i < businessInfoVector.size(); i++) {
        BusinessInfo businessInfo = (BusinessInfo)businessInfoVector.elementAt(i);
        System.out.println("in put: business " + i + " = " + businessInfo.getNameString());
      }

      // Handle possible errors
    } catch (Exception e) {
      e.printStackTrace();
    }
  } 

  public void testGetBusiness(YPProxy proxy) {
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
      BusinessList businessList = (BusinessList) proxy.find_business(names, null, null, null,null,findQualifiers,5).get();

      Vector businessInfoVector  = businessList.getBusinessInfos().getBusinessInfoVector();
      for (int i = 0; i < businessInfoVector.size(); i++) {
        BusinessInfo businessInfo = (BusinessInfo)businessInfoVector.elementAt(i);

        // Print name for each business
        System.out.println("in get: business " + i + " = " + businessInfo.getNameString());
      }

      // Handle possible errors
    } catch (Exception ex) {
      ex.printStackTrace();
    }
  }


  // hack XML printer for debugging
  public static void describeElement(Node el) { describeElement(el,""); }
  public static void describeElement(Node el,String prefix) { 
    System.out.println(prefix+el);
    String pn = prefix+" ";
    if (el.hasChildNodes()) {
      for (Node c = el.getFirstChild(); c!= null; c = c.getNextSibling()) {
        describeElement(c, pn);
      }
    }
  }
}

