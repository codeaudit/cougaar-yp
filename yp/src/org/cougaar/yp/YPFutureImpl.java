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
import org.cougaar.core.persist.Persistable;


final class YPFutureImpl implements YPFuture, Persistable {
  private static final Logger logger = Logging.getLogger(YPProxy.class); // reuse proxy's logger

  private String initialContext;
  private Element element;
  private boolean queryP;
  private boolean ready = false;
  private Object result = null;
  private Callback callback = null;
  private String finalContext = null;
  private Class resultClass;

  YPFutureImpl(String context, Element e, boolean qp, Class resultClass) {
    this.initialContext = context;
    this.element = e;
    this.queryP = qp;
    this.resultClass = resultClass;
  }

  public Element getElement() {
    return element;
  }
  public boolean isInquiry() {
    return queryP;
  }
  public String getInitialContext() {
    return initialContext;
  }


  public synchronized boolean isReady() { return ready; }
    
  public Object get() {
    return get(0L);
  }
  public synchronized Object get(long msecs) {
    if (!ready) {
      try {
        this.wait(msecs);
      } catch (InterruptedException ie) {
        logger.warn("Saw InterruptedException while waiting for YPReponse", ie);
      }
    }

    if (ready) {
      if (result instanceof Throwable) {
        throw new RuntimeException("YPFuture exception", (Throwable) result);
      } else {
        return convert(result); // convert to UDDI response object
      }
    } else {
      return null;
    }
  }

  public synchronized void setCallback(Callback callable) {
    if (callback != null && callable != null) throw new IllegalArgumentException("Already had a callback");
    callback = callable;
    if (ready) {
      callable.ready(this);
    }
  }

  public String getFinalContext() {
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

  void setFinalContext(String fc) {
    finalContext = fc;
  }

  private static final Class[] cargs = new Class[] { Element.class };

  /** Convert from an XML Element to a UDDI response object **/
  private Object convert(Object el) {
    if (el == null) {
      return null;
    } else {
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
