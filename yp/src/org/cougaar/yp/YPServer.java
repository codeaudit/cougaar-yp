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

  private DocumentBuilder builder;
  private MessageSwitchService mss = null;
  private MessageAddress originMA;

  private DocumentBuilder getBuilder() {
    if (builder == null) {
      try {
        builder = documentBuilderFactory.newDocumentBuilder();
      } catch (Exception e) {
        logger.error ("Could not create builder factory");
      }
    }
    return builder;
  }

  public void initialize() {
    super.initialize();
    initUDDI();
    initDB();
    
    // this should probably go into load

    // need to hook into the Agent MessageHandler protocol
    MessageHandler mh = new MessageHandler() {
        public boolean handleMessage(Message message) {
	  if (logger.isDebugEnabled()) {
	    logger.debug("handleMessage: source " + message.getOriginator() +
			 " target " + message.getTarget() + 
			 " key " + ((YPQueryMessage) message).getKey() + 
			 " element " + ((YPQueryMessage) message).getElement());
	  }
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
      logger.debug("dispatchQuery: response - source " + originMA +
		   " target " + r.getOriginator() + 
		   " key " + key +
		   " rel " + rel);
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

      Document document = getBuilder().newDocument();
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

  private static void copyConfFiles() {
    String installPath = System.getProperty("org.cougaar.install.path", "/tmp");
    String workspacePath = System.getProperty("org.cougaar.workspace", installPath + "/workspace");

    String juddiHomeDirPath = System.getProperty("juddi.homeDir", "");
    if (juddiHomeDirPath.equals("")) {
      juddiHomeDirPath = workspacePath + "/juddi";
      System.setProperty("juddi.homeDir", juddiHomeDirPath);
    }

    try {
      File juddiHomeDir = new File(juddiHomeDirPath);
      juddiHomeDir.mkdirs();
      File hsqlDir = new File(juddiHomeDir, "hsql");
      hsqlDir.mkdir();
      
      File confDir = new File(juddiHomeDir, "conf");
      confDir.mkdir();
      
      File ypConfDir = new File(installPath + "/yp/data/juddi/conf");
      
      if (!ypConfDir.canRead()) {
	logger.fatal("confConfDir: unable to read juddi configuration directory");
	return;
      }

      File []ypConfFiles = ypConfDir.listFiles();
      for (int index = 0; index < ypConfFiles.length; index++) {
	File ypConfFile = ypConfFiles[index];
	if (ypConfFile.isFile()) {
	  BufferedReader in = 
	    new BufferedReader(new FileReader(ypConfFile));
	
	  BufferedWriter out = 
	    new BufferedWriter(new FileWriter(new File(confDir, ypConfFile.getName())));
	  String inLine;
	  while ((inLine = in.readLine()) != null) {
	    out.write(inLine);
	    out.newLine();
	  }
	  
	  in.close();
	  out.close();
	}
      }
    } catch (FileNotFoundException fnfe) {
      fnfe.printStackTrace();
      logger.fatal("copyConfFiles: error copying configuration files.");
    } catch (IOException ioe) {
      ioe.printStackTrace();
      logger.fatal("copyConfFiles: error copying configuration files.");
    }
  }

  void initDB() {
    copyConfFiles();

    Connection connection = null;

    try {
      connection = (new org.juddi.datastore.jdbc.HSQLDataStoreFactory()).getConnection();

      CreateDatabase.dropDatabase(connection);
      CreateDatabase.createDatabase(connection);

    } catch (Exception e) {
      logger.error("Exception creating UDDI tables in the HSQL database.");
      e.printStackTrace();
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




