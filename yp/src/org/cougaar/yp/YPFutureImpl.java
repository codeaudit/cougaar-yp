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
import org.uddi4j.*;
import org.uddi4j.datatype.*;
import org.uddi4j.datatype.business.*;
import org.uddi4j.datatype.service.*;
import org.uddi4j.datatype.tmodel.*;
import org.uddi4j.datatype.assertion.*;
import org.uddi4j.request.*;
import org.uddi4j.response.*;
import org.uddi4j.util.*;

import java.io.*;
import java.net.*;
import java.util.*;

import org.cougaar.core.component.Service;
import org.cougaar.core.thread.SchedulableStatus;

import java.net.MalformedURLException;
import java.net.URL;
import java.util.Arrays;
import java.util.Properties;
import java.util.Vector;
import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;
import org.w3c.dom.Element;
import org.w3c.dom.DOMException;

import org.cougaar.util.log.*;
import org.cougaar.core.mts.MessageAddress;
import org.cougaar.core.persist.Persistable;
import org.cougaar.core.service.community.Community;


final class YPFutureImpl implements YPFuture, Persistable {
  private static final Logger logger = Logging.getLogger(YPProxy.class); // reuse proxy's logger

  private Object initialContext;
  private Element element;
  private boolean queryP;
  private boolean ready = false;
  private Object result = null;
  private Callback callback = null;
  private Object finalContext = null;
  private Class resultClass;
  private boolean isSubmitted = false;
  private boolean blackboardp = false;
  private int  searchMode;

  YPFutureImpl(Object context, Element e, boolean qp, Class resultClass, 
	       int searchMode) {
    this.initialContext = context;
    this.element = e;
    this.queryP = qp;
    this.resultClass = resultClass;
    this.searchMode = searchMode;
  }

  public Element getElement() {
    return element;
  }
  public boolean isInquiry() {
    return queryP;
  }
  public Object getInitialContext() {
    return initialContext;
  }


  public int getSearchMode() {
    return searchMode;
  }

  public synchronized boolean isReady() { return ready; }
    
  public Object get() throws UDDIException {
    return get(0L);
  }
  public synchronized Object get(final long msecs) throws UDDIException {
    if (!ready) {
      SchedulableStatus.withWait("YP Lookup",
                                 new Runnable() { public void run() {
                                   try {
                                     this.wait(msecs);
                                   } catch (InterruptedException ie) {
                                     logger.warn("Saw InterruptedException while waiting for YPReponse", ie);
                                   }
                                 }
                                 });
    }
    if (ready) {
      if (result instanceof Throwable) {
	if (result instanceof RuntimeException) {
	  // Leave original exception so that clients can catch explicitly.
	  throw (RuntimeException) result;
	} else {
	  throw new RuntimeException("YPFuture exception", (Throwable) result);
	}
      } else {
        return convert(result); // convert to UDDI response object
      }
    } else {
      return null;
    }
  }

  private static class ResponseCallbackAdapter implements Callback {
    private final ResponseCallback rc;
    ResponseCallbackAdapter(ResponseCallback c) { this.rc = c; }
    public final void ready(YPFuture response) {
      try {
	Object r = response.get();
	try {
	  if (logger.isDebugEnabled()) {
	    logger.debug("ResponseCallbackAdapter.read() calling " + rc);
	  }
	  rc.invoke(r);
	} catch (RuntimeException ix) {
	  logger.error("Invocation of YPFuture ResponseCallback resulted in Exception", ix);
	}
      } catch (Exception e) {
	rc.handle(e);
      }
    }
  }
    
  public synchronized void setCallback(YPComplete notifier) {
    Callback c;
    if (notifier instanceof ResponseCallback) {
      c = new ResponseCallbackAdapter((ResponseCallback) notifier);
    } else if (notifier instanceof Callback) {
      c = (Callback) notifier;
    } else {
      throw new IllegalArgumentException("Only Callback and ResponseCallback instances are allowed");
    }

    if (callback != null) throw new IllegalArgumentException("Already had a callback");
    callback = c;
    if (ready) {
      callback.ready(this);
    }
  }

  public Object getFinalContext() {
    return finalContext;
  }

  // package-private setters
  void set(Object value) {
    synchronized (this) {
      if (ready) throw new RuntimeException("Cannot reset a YPFuture");
      result = value;
      ready = true;
      this.notifyAll();
    }

    if (callback != null) {
      callback.ready(this);
    }
  }

  void setException(Throwable t) {
    set(t);
  }


  void setFinalContext(Object fc) {
    if (logger.isDebugEnabled()) {
      logger.debug("setFinalContext(): fc " + fc + " callback " + callback);
    }
    finalContext = fc;
  }
  
  synchronized void submitted() {
    if (isSubmitted) {
      throw new IllegalArgumentException("YPFuture was previously submitted");
    }
    isSubmitted = true;
  }

  synchronized void setIsFromBlackboard(boolean v) {
    blackboardp = v;
  }

  synchronized boolean isFromBlackboard() {
    return blackboardp;
  }

  private static final Class[] cargs = new Class[] { Element.class };

  /** Convert from an XML Element to a UDDI response object **/
  private Object convert(Object el) throws UDDIException {
    if (el == null) {
      return null;
    } else {
      if (el instanceof Element) {
        Element ell = (Element) el;
	if (logger.isDebugEnabled()) {
	  logger.debug("convert: " + ell);
	}
        if (UDDIException.isValidElement(ell)) {
	  if (logger.isDebugEnabled()) {
	    RuntimeException re = new RuntimeException();
	    logger.debug("Throwing UDDI exception " + ell, re);
	  }
          throw new UDDIException(ell, true);
        }
      }

      if (resultClass == null) { // no class conversion
        return el;
      } else {                //  otherwise, construct from the element
        try {
          java.lang.reflect.Constructor c = resultClass.getConstructor(cargs);
          return c.newInstance(new Object[] { el });
        } catch (Exception e) {
          throw new RuntimeException("Could not convert response Element to "+resultClass, e);
        }
      }
    }
  }

  // implement Persistable
  public boolean isPersistable() { return false; }
}







