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

import org.uddi4j.client.*;
import org.uddi4j.transport.*;

import org.w3c.dom.Element;
import org.w3c.dom.Node;

import java.io.*;
import java.net.*;
import java.util.*;

import org.cougaar.util.log.*;

import org.cougaar.core.component.*;

import org.cougaar.core.mts.*;
import org.cougaar.core.agent.*;
import org.cougaar.core.agent.service.MessageSwitchService;

// database connection support
import java.sql.*;

// soaduddi imports
import com.induslogic.uddi.server.service.*;
import com.induslogic.uddi.server.util.*;
import com.induslogic.uddi.*;

/**
 * This is the basic in-memory YP Server component.
 * We use soapuddi to deal with the queries and 
 * hsqldb in in-memory mode as a database.
 **/

public class YPServer extends ComponentSupport {
  private static final Logger logger = Logging.getLogger(YPServer.class);
  
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

  private void dispatchQuery(YPQueryMessage r) {
    Object key = r.getKey();
    Element qel = r.getElement();
    Element rel = null;
    boolean isInquiry = r.isInquiry();
    rel = executeQuery(qel);
    YPResponseMessage m = new YPResponseMessage(originMA, r.getOriginator(), rel, key);
    sendMessage(m);
  }

  protected void sendMessage(Message m) {
    mss.sendMessage(m);
  }

  public static final String DB_DRIVER = "org.hsqldb.jdbcDriver";
  //public static final String DB_URL = "jdbc:hsqldb:.";
  public static final String DB_FILE = "foodb";
  public static final String DB_URL = "jdbc:hsqldb:"+DB_FILE;
  public static final String DB_USER = "sa";
  public static final String DB_PASS = "";

  private Connection theConnection = null;

  void executeSQL(Connection c, String sql) throws SQLException {
    Statement s = c.createStatement();
    s.execute(sql);
  }

  Element executeQuery(Element qel) {
    Connection con = null;
    try {
      con = getDBConnection();

      String apiName = qel.getNodeName();
      UddiObject param = new UddiObject(qel);

      UddiService uService = new UddiService(con);
      UddiObject obj = uService.invokeAppropriateApi(apiName, param);

      con.close();
      
      return obj.getElement();

      } catch (Exception e) {
        try {
          if (con != null) {
            //con.rollback();
            //con.close();
          }
        }
        catch ( Exception e1){
          e1.printStackTrace();
        }
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


  Connection getDBConnection()
    throws SQLException, ClassNotFoundException, IOException
  {
    // should self-register the driver
    Class.forName(DB_DRIVER);   // ugly
    String url = DB_URL;
    String user = null;
    String passwd = null;
    return DriverManager.getConnection(url, DB_USER, DB_PASS);
  }


  void initDB() {
    try {
      File f = new File(DB_FILE+".data");
      if (f.exists()) {
        f.delete();
        new File(DB_FILE+".properties").delete();
        new File(DB_FILE+".script").delete();
      }
    } catch (Exception e) {}

    Connection con = null;
    try {
      theConnection = getDBConnection();
      con = theConnection;
      executeSQL(con, CT_1);
      executeSQL(con, CT_1a);
      executeSQL(con, CT_2);
      executeSQL(con, CT_3);
      executeSQL(con, CT_4);
      executeSQL(con, CT_5);
      executeSQL(con, CT_6);
      executeSQL(con, CT_7);
      executeSQL(con, CT_8);
      executeSQL(con, CT_9);
      executeSQL(con, CT_10);
      executeSQL(con, CT_11);
      executeSQL(con, CT_12);
      executeSQL(con, CT_13);
      executeSQL(con, CT_14);
      //con.commit();
      con.close();
    } catch (Exception e) {
      try {
        if (con != null) {
          //con.rollback();
          //con.close();
        }
      } catch ( Exception e1){
        e1.printStackTrace();
      }
      
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

    com.induslogic.uddi.server.util.GlobalProperties.loadProperties(props);
  }

  private final static String CT_1 = "CREATE TABLE passwords ( uddi_userid varchar (50)  , uddi_password varchar (50)  , CONSTRAINT PK_passwords PRIMARY KEY ( uddi_userid ) ) ";

  private final static String CT_1a = "INSERT into passwords values ('cougaar','cougaarPass')";

  private final static String CT_2 = "CREATE TABLE authentiTokens ( uddi_userid varchar (50)  , uddi_keys varchar (50)  , uddi_validTill varchar (50)  , CONSTRAINT PK_authentiTokens PRIMARY KEY ( uddi_userid ) , CONSTRAINT FK_aT_passwords FOREIGN KEY ( uddi_userid ) REFERENCES passwords ( uddi_userid ) ) ";


  private final static String CT_3 = "CREATE TABLE TModelDetails ( uddi_tmodelkey varchar (50)  , uddi_authorizedname varchar (50)  , uddi_keyname varchar (64)  , uddi_overview varchar (128)  , uddi_userid varchar (50)  , uddi_isHidden bit NOT NULL , CONSTRAINT PK_TModelDetails PRIMARY KEY ( uddi_tmodelkey ) , CONSTRAINT FK_TMD_passwords FOREIGN KEY ( uddi_userid ) REFERENCES passwords ( uddi_userid ) ) ";


  private final static String CT_4 = "CREATE TABLE BusinessDetails ( uddi_businesskey varchar (50)  , uddi_authorizedname varchar (128)  , uddi_operator varchar (50)  , uddi_name varchar (128)  , uddi_userid varchar (50)  , CONSTRAINT PK_BusinessDetails PRIMARY KEY ( uddi_businesskey )  , CONSTRAINT FK_BD_passwords FOREIGN KEY ( uddi_userid ) REFERENCES passwords ( uddi_userid ) ) ";


  private final static String CT_5 = "CREATE TABLE BusinessService ( uddi_servicekey varchar (50)  , uddi_businesskey varchar (50)  , uddi_servicename varchar (40)  , CONSTRAINT PK_BusinessService PRIMARY KEY ( uddi_servicekey )  , CONSTRAINT FK_BS_BD FOREIGN KEY ( uddi_businesskey ) REFERENCES BusinessDetails ( uddi_businesskey ) ) ";


  private final static String CT_6 = "CREATE TABLE BindingTemplate ( uddi_bindingkey varchar (50)  , uddi_servicekey varchar (50)  , uddi_accesspoint varchar (255)  , uddi_servicetype varchar (20)  , uddi_hostingRedirector varchar (50), CONSTRAINT PK_BindingTemplate PRIMARY KEY ( uddi_bindingkey )  , CONSTRAINT FK_BT_BS FOREIGN KEY ( uddi_servicekey ) REFERENCES BusinessService ( uddi_servicekey ) ) ";


  private final static String CT_7 = "CREATE TABLE CategoryBag ( uddi_keys varchar (50)  , uddi_tmodelkey varchar (50)  , uddi_Keyname varchar (255)  , uddi_Keyvalue varchar (50)  , uddi_isHidden bit NOT NULL , CONSTRAINT PK_CategoryBag PRIMARY KEY ( uddi_keys, uddi_tmodelkey ) , CONSTRAINT FK_CB_BD FOREIGN KEY ( uddi_keys ) REFERENCES BusinessDetails ( uddi_businesskey ) , CONSTRAINT FK_CB_BS FOREIGN KEY ( uddi_keys ) REFERENCES BusinessService ( uddi_servicekey ) , CONSTRAINT FK_CB_TMD FOREIGN KEY ( uddi_tmodelkey ) REFERENCES TModelDetails ( uddi_tmodelkey ) ) ";


  private final static String CT_8 = "CREATE TABLE Contacts ( uddi_businesskey varchar (50)  , uddi_personname varchar (32)  , uddi_phoneusetype varchar (50)  , uddi_phone varchar (50)  , uddi_emailusetype varchar (50)  , uddi_email varchar (128)  , uddi_address varchar (255)  , CONSTRAINT PK_Contacts PRIMARY KEY ( uddi_businesskey ) , CONSTRAINT FK_C_BD FOREIGN KEY ( uddi_businesskey ) REFERENCES BusinessDetails ( uddi_businesskey ) ) ";


  private final static String CT_9 = "CREATE TABLE DiscoveryURLs ( uddi_businesskey varchar (50)  , uddi_discoveryURL varchar (255)  , uddi_usetype varchar (20) , CONSTRAINT FK_DURL_BD FOREIGN KEY ( uddi_businesskey ) REFERENCES BusinessDetails ( uddi_businesskey ) ) ";


  private final static String CT_10 = "CREATE TABLE IdentifierBag ( uddi_Keys varchar (50)  , uddi_keyname varchar (50)  , uddi_keyvalue varchar (50)  , uddi_isHidden bit NOT NULL , CONSTRAINT FK_IB_BD FOREIGN KEY ( uddi_Keys ) REFERENCES BusinessDetails ( uddi_businesskey ) , CONSTRAINT FK_IB_TMD FOREIGN KEY ( uddi_Keys ) REFERENCES TModelDetails ( uddi_tmodelkey ) ) ";


  private final static String CT_11 = "CREATE TABLE InstanceDetails ( uddi_bindingkey varchar (50)  , uddi_tmodelkey varchar (50)  , uddi_overViewUrl varchar (50)  , uddi_instanceParms varchar (50)  , uddi_isHidden bit NOT NULL , CONSTRAINT FK_ID_BT FOREIGN KEY ( uddi_bindingkey ) REFERENCES BindingTemplate ( uddi_bindingkey ), CONSTRAINT FK_ID_TMD FOREIGN KEY ( uddi_tmodelkey ) REFERENCES TModelDetails ( uddi_tmodelkey ) ) ";

  private final static String CT_12 = "CREATE TABLE Descriptions ( uddi_key varchar (50)  , uddi_description varchar (255)  , uddi_lang varchar (50) ) ";

  private final static String CT_13 = "CREATE TABLE overviewDescriptions ( uddi_key varchar (50)  , uddi_description varchar (255)  , uddi_lang varchar (50) ) ";

  private final static String CT_14 = "CREATE TABLE instanceDetailDescriptions ( uddi_bindingkey varchar (50)  , uddi_tmodelkey varchar (50)  , uddi_descType varchar (50)  , uddi_description varchar (255)  , uddi_lang varchar (50) ) ";


  // 
  // the rest is a hack test lash-up
  //

  public static void main(String[] arg) {
    YPServer yp = new YPServer();
    yp.initDB();
    yp.initUDDI();
    YPTransport transport = new YPTransport(yp);

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
  
  private static class YPTransport extends TransportBase {
    private YPServer yp;
    YPTransport(YPServer yp) {
      this.yp = yp;
    }
    /** Send the DOM element specified to the URL as interpreted by the MTS **/
    public Element send(Element el, java.net.URL url) throws TransportException {
      logger.warn("Transported query "+el);
      describeElement(el);
      Element resp = yp.executeQuery(serialize(el));
      logger.warn("Sending Response "+resp);
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
}
