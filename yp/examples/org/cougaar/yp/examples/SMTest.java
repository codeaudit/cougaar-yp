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
import org.cougaar.core.service.ThreadService;
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


/** SMTest is a reimplementation of YPTest which uses a 
 * variety of StateMachines to avoid blocking YP queries
 **/

public class SMTest extends ComponentPlugin {
  private static final Logger log = Logging.getLogger(SMTest.class);

  String user = "cougaar";
  String pass = "cougaarPass";
  static String sampleName = "Sample Co";
  static String sampleName2 = "Second Co";


  IncrementalSubscription sub;
  long count = 0L;
  long total = 0L;

  private String arg = null;

  private YPProxy yp;
  private YPService yps;
  private ThreadService threads;

  public void setThreadService(ThreadService threads) { this.threads = threads; }
  public void setYPService(YPService yps) {
    this.yps = yps;
    if (yps != null) {
      yp = yps.getYP(arg);
    }
  }

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


  private Put_SM pmachine = null;
  private Get_SM gmachine = null;

  protected void setupSubscriptions() {
    pmachine = new Put_SM();
    pmachine.init();

    gmachine = new Get_SM();
    gmachine.init();

    sub = (IncrementalSubscription) blackboard.subscribe(new UnaryPredicate() {
        public boolean execute(Object o) { return o instanceof YPFuture; }
      });
    pmachine.start();
  }

  public void execute() {
    // ok - this should be done with a larger state machine rather than two...
    if (pmachine.isDone() && !gmachine.isDone()) {
      gmachine.go();
    }
  }


  /**
   * Demonstrate use of the YPStateMachine base class illustrating non-BB asynchronous
   * YP operations.
   **/
  private class Put_SM extends YPStateMachine {
    Put_SM() {
      super(yps,yp,threads);
    }
    public void transit(State s0, State s1) {
      log.warn(this.toString()+" transiting from "+s0+" to "+s1);
      super.transit(s0, s1);
    }

    protected void init() {
      super.init();
      // YPStateMachine starts at the "START" state, by default
      addLink("START", "A");
      // get the authtoken
      add(new SState("A") {
          public void invoke() {
            call("getAuthToken", null, "B");
          }});
      // publish the business
      addYPQ("B", "C",
             new YPQ() {
               public YPFuture get(Frame f) {
                 System.err.println("Using token "+ getAuthToken());
                 // Create minimum required data objects
                 Vector entities = new Vector();
                 BusinessEntity be = new BusinessEntity("", sampleName);
                 entities.addElement(be);
                 return yp.save_business(getAuthToken().getAuthInfoString(),entities);
               }
               public void set(Frame f, Object result) {
                 BusinessDetail bd = (BusinessDetail) result;
                 // Process returned BusinessDetail object
                 Vector businessEntities = bd.getBusinessEntityVector();
                 BusinessEntity returnedBusinessEntity = (BusinessEntity)(businessEntities.elementAt(0));
                 System.out.println("Returned businessKey:" + returnedBusinessEntity.getBusinessKey());
               }
               public void handle(Frame f, Exception e) {
                 log.error("Caught exception: "+e, e);
                 transit("A");  // start over;
               }});
      // discard the authtoken
      add(new SState("C") {
          public void invoke() {
            call("discardAuthToken", null, "D");
          }});
      // kick the blackboard so that the other demo can proceed
      add(new SState("D") {
          public void invoke() {
            // kicks the subscriber so that the YP-based machine can take over
            blackboard.signalClientActivity();
            transit("DONE");
          }});
    }
  }

  /**
   * Demonstrate use of state machine via blackboard.
   **/
  private class Get_SM extends StateMachine {
    private YPFuture ypq = null;

    public void transit(State s0, State s1) {
      log.warn(this.toString()+" transiting from "+s0+" to "+s1);
      super.transit(s0, s1);
    }

    void init() {
      add(new State("A") {
          public void invoke() { 
            if (ypq != null) transit("ERROR");

            //creating vector of Name Object
            Vector names = new Vector();
            names.add(new Name("S"));

            // Setting FindQualifiers to 'caseSensitiveMatch'
            FindQualifiers findQualifiers = new FindQualifiers();
            Vector qualifier = new Vector();
            qualifier.add(new FindQualifier("caseSensitiveMatch"));
            findQualifiers.setFindQualifierVector(qualifier);

            System.out.println("Issuing find_business");
            ypq = yp.find_business(names, null, null, null,null,findQualifiers,5);

            blackboard.publishAdd(ypq);
            transit("B");
          }
        });

      add(new State("B") {
          public void invoke() { 
            if (ypq == null) transit("ERROR");
            System.out.println("Checking for find_business response");
            if (ypq.isReady()) {

              BusinessList businessList;
              try {
                businessList = (BusinessList) ypq.get();
              } catch (UDDIException ue) {
                log.error("exception", ue);
                transit(ERROR);
                return;
              }
              Vector businessInfoVector  = businessList.getBusinessInfos().getBusinessInfoVector();
              for (int i = 0; i < businessInfoVector.size(); i++) {
                BusinessInfo businessInfo = (BusinessInfo)businessInfoVector.elementAt(i);
                System.out.println("in get: business " + i + " = " + businessInfo.getNameString());
              }
              blackboard.publishRemove(ypq);
              ypq=null;
              transit("DONE");
            } else {
              System.out.println(";");
            }
          }
        });
      set("A");
    }
  }
}

