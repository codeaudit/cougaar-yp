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
import org.uddi4j.response.DispositionReport;
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

import org.cougaar.core.agent.*;
import org.cougaar.core.agent.service.MessageSwitchService;
import org.cougaar.core.mts.*;
import org.cougaar.core.service.AgentIdentificationService;
import org.cougaar.util.ConfigFinder;
import org.cougaar.util.CircularQueue;

import org.cougaar.core.service.ThreadService;
import org.cougaar.core.thread.*;

// persistence support
import org.cougaar.core.persist.*;

// database connection support
import java.sql.*;

// juddi imports
import org.juddi.datastore.jdbc.CreateDatabase;
import org.juddi.datastore.jdbc.HSQLDataStoreFactory;
import org.juddi.error.JUDDIException;
import org.juddi.service.ServiceFactory;
import org.juddi.service.UDDIService;
import org.juddi.transport.axis.RequestFactory;
import org.juddi.util.Config;



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
  private AgentIdentificationService agentIdentificationService;

  private DocumentBuilder getBuilder() {
    if (builder == null) {
      try {
        builder = documentBuilderFactory.newDocumentBuilder();
      } catch (Exception e) {
        logger.error("Could not create builder factory", e);
      }
    }
    return builder;
  }

  /** threading service for launching our service thread, set by introspection **/
  private ThreadService threadService;
  public void setThreadService(ThreadService ts) { this.threadService = ts; }

  // must use explicit getService
  private PersistenceService persistenceService;
  private PersistenceIdentity persistenceIdentity = new PersistenceIdentity(YPServer.class.getName());

  protected final String installPath = System.getProperty("org.cougaar.install.path", "/tmp");
  protected final String workspacePath = System.getProperty("org.cougaar.workspace", installPath + "/workspace");
  protected File dbDirectory;

  public YPServer() {
    String hp = System.getProperty("juddi.homeDir", "");
    if (hp.equals("")) {
      hp = workspacePath + "/juddi";
      System.setProperty("juddi.homeDir", hp);
    }
  }

  public void initialize() {
    super.initialize();
    ServiceBroker sb = getServiceBroker();

    // this should probably go into load

    persistenceService = (PersistenceService) sb.getService(new PersistenceClient() {
        public PersistenceIdentity getPersistenceIdentity() { return persistenceIdentity; }
        public List getPersistenceData() { return encapsulateDatabase(); }
      }, 
                                                            PersistenceService.class,
                                                            null);
   
    RehydrationData rd = persistenceService.getRehydrationData();
    if (rd != null) {
      deencapsulateDatabase(rd);
    }

    // Must get the MessageSwitchService and MessageAddress before
    // registering the MessageHandler - cause messages may start pouring in
    mss = (MessageSwitchService) sb.getService(this,MessageSwitchService.class, null);
    if (mss == null) {
      throw new RuntimeException("YPServer couldnt get MessageSwitchService!");
    }

    originMA = mss.getMessageAddress();
    if (originMA == null) {
      throw new RuntimeException("YPServer got null MessageAddress for local Agent from MessageSwitchService!");
    }

    Config.dbTag.set(originMA.toString());
    dbDirectory = new File(Config.getHomeDir(), "hsql");
    dbDirectory.mkdirs();


    initUDDI();
    initDB();                   // uses rehydrated database information, if available

    startServiceThread();

    // need to hook into the Agent MessageHandler protocol
    MessageHandler mh = new MessageHandler() {
        public boolean handleMessage(Message message) {
          if (message instanceof YPQueryMessage) {
            if (logger.isDebugEnabled()) {
              logger.debug("handleMessage: source " + message.getOriginator() +
                           " target " + message.getTarget() +
                           " key " + ((YPQueryMessage) message).getKey() +
                           " element " + ((YPQueryMessage) message).getElement());
            }

	    
            getServiceThread().addMessage((YPQueryMessage) message);
            return true;
          }
          return false;
        }
      };

    mss.addMessageHandler(mh);
  }

  public void suspend() {
    if (logger.isInfoEnabled()) {
      logger.info(originMA.toString() + " suspending");
    }
      
    // suspend all children
    if (logger.isInfoEnabled()) {
      logger.info(originMA.toString() + "Recursively suspending all child components");
    }
    super.suspend();
    
    if (logger.isInfoEnabled()) {
      logger.info(originMA.toString() + 
		  " dropping database connection for persistence snapshot" );
    }
    
    Config.dbTag.set(originMA.toString());
    org.juddi.datastore.jdbc.HSQLDataStoreFactory.closeConnection();

    getServiceBroker().releaseService(this, AgentIdentificationService.class, 
				      agentIdentificationService);

    getServiceBroker().releaseService(this, MessageSwitchService.class, 
				      mss);
    /*
    getServiceBroker().releaseService(this, PersistenceService.class,
				      persistenceService);
				      */
    getServiceBroker().releaseService(this, ThreadService.class,
				      threadService);
  }

  public void resume() {
    super.resume();
    
    // Restart database????
  }

  //
  // persistence
  // 
  private List encapsulateDatabase() {

    List l = new ArrayList(1);
    DatabaseEnvelope de = getDatabaseEnvelope();
    DatabaseEnvelope.logger.info("Encapsulating database "+de);
    l.add(de);
    return l;
  }

  private void deencapsulateDatabase(RehydrationData rd) {
    List l = rd.getObjects();
    try {
      DatabaseEnvelope de = (DatabaseEnvelope) l.get(0);
      DatabaseEnvelope.logger.info("Dencapsulating database "+de);
      setDatabaseEnvelope(de);
    } catch (Exception e) {     // arrayOOB, classcast, etc
      logger.error("Persistence snapshot objects corrupt", e);
    }
  }

  private DatabaseEnvelope databaseEnvelope = null;
  private final DatabaseEnvelope.Locker databaseLocker = new DatabaseEnvelope.Locker() {
      public void stop() { }
      public void start() { }
    };

  protected DatabaseEnvelope getDatabaseEnvelope() {
    synchronized (databaseLocker) {
      return databaseEnvelope;
    }
  }
  // mutations of the database should create a new DBE instance and set 
  protected void setDatabaseEnvelope(DatabaseEnvelope de) {
    synchronized (databaseLocker) {
      databaseEnvelope = de;
    }
  }

  /** Called each time the database is modified **/
  private void snapshotDatabase() {
    synchronized (databaseLocker) {
      databaseEnvelope = new DatabaseEnvelope(dbDirectory, databaseLocker);
    }
  }


  //
  // Service thread for incoming (response) messages
  //

  private ServiceThread serviceThread = null;
  private void startServiceThread() {
    serviceThread = new ServiceThread( new ServiceThread.Callback() {
        public void dispatch(Message m) {
	  Config.dbTag.set(originMA.toString());
          dispatchQuery((YPQueryMessage)m);
        }},
                                       logger,
                                       "YPServer("+originMA+")");
    serviceThread.start(threadService);
  }

  protected ServiceThread getServiceThread() {
    return serviceThread;
  }

  private int rc = 0;
  private synchronized void dispatchQuery(YPQueryMessage r) {
    if (logger.isDebugEnabled()) {
      logger.debug("dispatchQuery: query: " + r.getKey() + " " +
		  r.getElement());
    }

    Object key = r.getKey();
    Element qel = r.getElement();
    Element rel = null;
    boolean isInquiry = r.isInquiry();
    synchronized (databaseLocker) {
      rel = executeQuery(qel);
      if (!isInquiry) {
        snapshotDatabase();
      }
    }
    YPResponseMessage m = new YPResponseMessage(originMA, r.getOriginator(), rel, key);

    if (logger.isDebugEnabled()) {
      logger.debug("dispatchQuery: response - source " + originMA +
		   " target " + r.getOriginator() +
		   " key " + key +
		   " rel " + rel);
    }
    sendMessage(m);
    rc++;
    //System.err.println("YPServer RES="+rc);
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

      try {
        uService.invoke(request).saveToXML(response);

        if (logger.isDebugEnabled()) {
          logger.debug("executeQuery: returned -");
          describeElement(response);
        }
        return (Element) response.getChildNodes().item(0);

      } catch (JUDDIException je) {
        Element fault = getFaultDoc(je);
        if (logger.isWarnEnabled()) {
          logger.warn("executeQuery: fault", je);
        }
        return fault;
      }

    } catch (Exception e) {
      logger.error("Uncaught Exception ", e);
      return null;
    }
  }

  // from JUDDI/../JUDDIProxy
  private Element getFaultDoc(JUDDIException e)
  {
    DocumentBuilderFactory factory = DocumentBuilderFactory.newInstance();
    try {
      Element faultElement = null;
      Element rootElement = null;

      // 2. build up a response element for UDDI4j to 'saveToXML()' into
      DocumentBuilder builder = factory.newDocumentBuilder();
      Document faultDoc = builder.newDocument();
      rootElement = faultDoc.createElement("Fault");
      faultDoc.appendChild(rootElement);

      faultElement = faultDoc.createElement("faultcode");
      faultElement.appendChild(faultDoc.createTextNode(e.getFaultCode()));
      rootElement.appendChild(faultElement);

      faultElement = faultDoc.createElement("faultstring");
      if ( e.getFaultString() != null ) {
        faultElement.appendChild(faultDoc.createTextNode(e.getFaultString()));
      } else {
        faultElement.appendChild(faultDoc.createTextNode(e.getMessage()));
      }
      rootElement.appendChild(faultElement);

      faultElement = faultDoc.createElement("faultactor");
      faultElement.appendChild(faultDoc.createTextNode(e.getFaultActor()));
      rootElement.appendChild(faultElement);

      DispositionReport dispRpt = e.getDispositionReport();
      if (dispRpt != null) {
        faultElement = faultDoc.createElement("detail");
        dispRpt.saveToXML(faultElement);
        rootElement.appendChild(faultElement);
      }

      return rootElement;
    }
    catch(Exception pcex) {
      System.out.println(pcex.getMessage());
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

  private void copyFiles(String ypDir, String juddiDir) {
    File ypSourceDir = new File(installPath + "/yp/data/juddi/" + ypDir);
    File newDir = new File(juddiDir);

    try {
      
      if (!ypSourceDir.canRead()) {
	logger.fatal("ypSourceDir: unable to read " + 
		     ypSourceDir.getPath() + " directory");
	return;
      }
      
      File []ypSourceFiles = ypSourceDir.listFiles();
      for (int index = 0; index < ypSourceFiles.length; index++) {
	File ypSourceFile = ypSourceFiles[index];
	if (ypSourceFile.isFile()) {
	  BufferedReader in =
	    new BufferedReader(new FileReader(ypSourceFile));
	  
	  BufferedWriter out =
	    new BufferedWriter(new FileWriter(new File(newDir, ypSourceFile.getName())));
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
      logger.fatal("copyFiles: error copying " + ypSourceDir.getPath() + 
		   " files.", fnfe);
    } catch (IOException ioe) {
      logger.fatal("copyFiles: error copying " + ypSourceDir.getPath() + 
		   " files.", ioe);
    }
  }

  void initDB() {
    Config.dbTag.set(originMA.toString());
    String configDir = org.juddi.util.Config.getConfigDir();
    copyFiles("conf", configDir);
    synchronized (databaseLocker) {
      if (databaseEnvelope!=null) { // rehydrate!
        try {
          databaseEnvelope.dumpPayload(dbDirectory);
          return;               // done - all is well
        } catch (IOException ioe) {
          logger.error("Unrecoverable database snapshot "+dbDirectory, ioe);
        }
      }
      
      // no successful recovery, so we'll have to start over
      String hsqlDir = org.juddi.datastore.jdbc.HSQLDataStoreFactory.getURL();
      copyFiles("hsql", dbDirectory.getPath());
      snapshotDatabase();     // take a snapshot immediately
    }
    //hack();
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

  /*
  void hack() {
    try {
      File target = new File("/tmp/hsql/"+originMA.toString());
      target.mkdirs();

      DatabaseEnvelope.logger.info("Hack, writing binfile");
      String binf = "/tmp/"+originMA+".bin";
      ObjectOutputStream os = new ObjectOutputStream(new BufferedOutputStream(new FileOutputStream(binf)));
      os.writeObject(getDatabaseEnvelope());
      os.close();
      DatabaseEnvelope.logger.info("Hack, reading binfile");
      ObjectInputStream is = new ObjectInputStream(new BufferedInputStream(new FileInputStream(binf)));
      DatabaseEnvelope de = (DatabaseEnvelope) is.readObject();
      DatabaseEnvelope.logger.info("Hack, read "+de);
      de.dumpPayload(target);
      DatabaseEnvelope.logger.info("Dumped payload to "+target);      
    } catch (Exception e) {
      e.printStackTrace();
    }
  }
  */
}


