/*
 * jUDDI - An open source Java implementation of UDDI v2.0
 * http://juddi.org/
 *
 * Copyright (c) 2002, InflexionPoint and contributors
 * All rights reserved.
 */

package org.juddi.datastore.jdbc;

import org.juddi.datastore.*;
import org.juddi.util.Config;

import org.apache.log4j.Logger;

import java.io.File;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;
import java.util.HashMap;
  
/**
 * Implementation of Factory pattern to decide which class that
 * implements the DataStore interface will get instantiated.
 *
 * The name of the class to instantiate should exist as a property
 * in the juddi.properties configuration file with a property name
 * of juddi.datastore.datastoreClassName. If the property is not 
 * found an Exception is thrown.
 *
 * @author Steve Viens (steve@inflexionpoint.com)
 * @version 0.6.2
 */
public class HSQLDataStoreFactory extends DataStoreFactory
{
  // private reference to the jUDDI logger
  private static Logger log = Logger.getLogger(HSQLDataStoreFactory.class);

  private static final String jdbcDriver    = "org.hsqldb.jdbcDriver";
  //private static final String jdbcURL       = "jdbc:hsqldb:" + Config.getHomeDir() + "/hsql/juddidb";
  private static final String inMemoryURL   = "jdbc:hsqldb:.";
  private static final String jdbcUserID    = "sa";
  private static final String jdbcPassword  = "";
  private static final boolean perThread = Config.getOneServerPerThread();

  private static final boolean inMemory = Config.getInMemoryDatabase();




  // Use Config.dbTag as the HashMap key. So ... if process is using more than
  // 1 database, must set dbTag before each interaction. Connections are not
  // re-entrant so only one thread can be using a connection at a time.
  private static HashMap cachedConnections = new HashMap();


  /**
   *
   */
  public HSQLDataStoreFactory()
  {
    try {
      Class.forName(jdbcDriver);
    }
    catch(ClassNotFoundException cnfex)
    {
      throw new RuntimeException("ClassNotFoundException thrown while " +
        "attempting to load JDBC Driver: " + jdbcDriver + " :: " + cnfex.getMessage());
    }
  }

  /**
   * using this function will allow different URLs for different threads. 
   * Thread must set ThreadLocal Config.dbTag() before invoking JUDDI.
   */
  private static String jdbcURL() {
    return "jdbc:hsqldb:" + Config.getHomeDir() + File.separator + 
      "hsql" + File.separator + "juddidb";
  }

  /**
   * using this function will allow JUDDI to use both file and memory based 
   * HSQL instances.
   */
  public static String getURL() {
    String url = inMemory ? inMemoryURL : jdbcURL();
    return url;
  }

  /**
   *
   */
  public DataStore aquireDataStore()
  {
    Connection connection = (Connection) cachedConnections.get(Config.dbTag.get());
    
    if (connection == null) {
      try {
        connection = DriverManager.getConnection(getURL(), 
						 jdbcUserID, 
						 jdbcPassword);
	cachedConnections.put(Config.dbTag.get(), connection);
      } catch(SQLException sqlex) {
        log.error("Exception occured while attempting to create a " +
		  "new JDBC connection: for getURL() " + sqlex.getMessage());
	return null;
      }
    }

    return new JDBCDataStore(connection);
  }

  /**
   *
   */
  public void releaseDataStore(DataStore datastore) {
  }

  /**
   * Get cached connection associated with ThreadLocal Config.dbTag.
   * If process is using more than 1 database,  must set Config.dbTag before each 
   * interaction. Connections are not re-entrant so only one thread can be 
   * using a connection at a time.
   *
   */
  public Connection getConnection()
  {
    return (Connection) cachedConnections.get(Config.dbTag.get());
  }
  /**
   * Get cached connection associated with ThreadLocal dbTag.
   * If process is using more than 1 database,  must set dbTag before each 
   * interaction. Connections are not re-entrant so only one thread can be 
   * using a connection at a time.
   *
   */
  public static boolean closeConnection()
  {
    Connection connection = (Connection) cachedConnections.get(Config.dbTag.get());
    
    if (connection != null) {
      try {
	connection.close();
	cachedConnections.remove(Config.dbTag.get());
      } catch(SQLException sqlex) {
        log.error("Exception occured while attempting to close a " +
		  "JDBC connection: for " + Config.dbTag.get() + sqlex.getMessage());
	return false;
      }
    }
    
    return true;
  }
}
