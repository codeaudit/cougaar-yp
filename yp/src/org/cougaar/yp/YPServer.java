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

import org.uddi4j.UDDIElement;
import org.uddi4j.client.*;
import org.uddi4j.transport.*;

import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.Element;
import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;


import java.io.*;
import java.net.*;
import java.util.*;

import org.cougaar.util.log.*;

import org.cougaar.core.component.*;

import org.cougaar.core.mts.*;
import org.cougaar.core.agent.*;
import org.cougaar.core.agent.service.MessageSwitchService;
import org.cougaar.util.ConfigFinder;

// database connection support
import java.sql.*;

// soaduddi imports
//import com.induslogic.uddi.server.service.*;
//import com.induslogic.uddi.server.util.*;
//import com.induslogic.uddi.*;

// juddi imports
import org.juddi.datastore.jdbc.CreateDatabase;
import org.juddi.datastore.jdbc.HSQLDataStoreFactory;
import org.juddi.service.ServiceFactory;
import org.juddi.service.UDDIService;
import org.juddi.transport.axis.RequestFactory;


/**
 * This is the basic in-memory YP Server component.
 * We use soapuddi to deal with the queries and 
 * hsqldb in in-memory mode as a database.
 **/

public class YPServer extends ComponentSupport {
  private static final Logger logger = Logging.getLogger(YPServer.class);

  // create an XML document builder factory
  private static DocumentBuilderFactory documentBuilderFactory = 
    DocumentBuilderFactory.newInstance();

  private DocumentBuilder builder = 
    documentBuilderFactory.newDocumentBuilder();
  private MessageSwitchService mss = null;
  private MessageAddress originMA;

  public void initialize() {
    super.initialize();

    initUDDI();
    initDB();
    
    // this should probably go into load

    // need to hook into the Agent MessageHandler protocol
    MessageHandler mh = new MessageHandler() {
        public boolean handleMessage(Message message) {
	  logger.debug("\n\n\n handleMessage: " + message.getClass());
          if (message instanceof YPQueryMessage) {
            dispatchQuery((YPQueryMessage) message);
            return true;
          }
          return false;
        }
      };
    ServiceBroker sb = getServiceBroker();
    mss = (MessageSwitchService) sb.getService(this,MessageSwitchService.class, null);
    mss.addMessageHandler(mh);
    originMA = mss.getMessageAddress();
  }

  private synchronized void dispatchQuery(YPQueryMessage r) {
    if (logger.isDebugEnabled()) {
      logger.debug("\n\n\n\n dispatchQuery: query - " + r.getKey() + " " + 
		  r.getElement());
    }

    Object key = r.getKey();
    Element qel = r.getElement();
    Element rel = null;
    boolean isInquiry = r.isInquiry();
    rel = executeQuery(qel);
    YPResponseMessage m = new YPResponseMessage(originMA, r.getOriginator(), rel, key);
    
    if (logger.isDebugEnabled()) {
      logger.debug("dispatchQuery: response - " + m);
    }
    sendMessage(m);
  }

  protected void sendMessage(Message m) {
    mss.sendMessage(m);
  }
  //public static final String DB_URL = "jdbc:hsqldb:.";
  public static final String DB_FILE = "foodb";
  public static final String DB_URL = "jdbc:hsqldb:"+DB_FILE;
  public static final String DB_DRIVER = "org.hsqldb.jdbcDriver";
  public static final String DB_USER = "sa";
  public static final String DB_PASS = "";

  Element executeQuery(Element qel) {
	
    try {
      if (logger.isDebugEnabled()) {
	logger.debug("executeQuery: query -");
	describeElement(qel);
      }

      Document document = builder.newDocument();
      Element holder = document.createElement("holder");
      document.appendChild(holder);  // holder element is thrown away
      Element response = document.getDocumentElement();

      UDDIElement request = (UDDIElement) RequestFactory.getRequest(qel);
      UDDIService uService = ServiceFactory.getService(request.getClass().getName());
      
      uService.invoke(request).saveToXML(response);

      if (logger.isDebugEnabled()) {
	logger.debug("executeQuery: returned -");
	describeElement(response);
      }

      return (Element) response.getChildNodes().item(0);

      } catch (Exception e) {
        e.printStackTrace();
        return null;
    } 
  }
  
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

  void initDB() {
    Connection connection = null;

    try {
      connection = (new org.juddi.datastore.jdbc.HSQLDataStoreFactory()).getConnection();

      CreateDatabase.dropDatabase(connection);
      CreateDatabase.createDatabase(connection);
      
      
    } catch (Exception e) {
      logger.error("Exception creating UDDI tables in the HSQL database.");
      e.printStackTrace();
    } finally {
      try {
	if (connection != null) {
	  connection.close();
	}
      } catch(SQLException sqlex) {
        sqlex.printStackTrace();
      }
    }
  }

  void initUDDI() {
    Properties props = new Properties();
    props.setProperty("operator", "Cougaar");
    props.setProperty("user",DB_USER);
    props.setProperty("passwd",DB_PASS);
    props.setProperty("authorisedName","auser");

    props.setProperty("Class", DB_DRIVER);
    props.setProperty("URL", DB_URL);

    //com.induslogic.uddi.server.util.GlobalProperties.loadProperties(props);
  }
}




