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

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;
  
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
  private static final String jdbcURL       = "jdbc:hsqldb:" + Config.getHomeDir() + "/hsql/juddidb";
  private static final String inMemoryURL   = "jdbc:hsqldb:.";
  private static final String jdbcUserID    = "sa";
  private static final String jdbcPassword  = "";
  private static final boolean perThread = Config.getOneServerPerThread();

  private static final boolean inMemory = Config.getInMemoryDatabase();
  private static int dbCount = 1;
  private static ThreadLocal dbNum = new ThreadLocal() {
    protected synchronized Object initialValue() {
      return new Integer (dbCount++);
    }
  };

  private static ThreadLocal cachedConnections = new ThreadLocal() {
    protected synchronized Object initialValue() {
      try {
        return DriverManager.getConnection(getURL(),jdbcUserID,jdbcPassword);
      }
      catch(SQLException sqlex) {
        log.error("Exception occured while attempting to create a " +
          "new JDBC connection: "+sqlex.getMessage());
      }
      return null;
    }
  };

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
   * using this function will allow different URLs for different threads
   */
  private static String getURL() {
    String url = inMemory ? inMemoryURL : jdbcURL;
    if (! perThread)
      return url;
    int num = ((Integer) dbNum.get()).intValue();
    return (num == 1) ? url : (url + num);
  }

  /**
   *
   */
  public DataStore aquireDataStore()
  {
    return new JDBCDataStore((Connection) cachedConnections.get());
  }

  /**
   *
   */
  public void releaseDataStore(DataStore datastore) {
  }

  /**
   *
   */
  public Connection getConnection()
  {
    return (Connection) cachedConnections.get();
  }
}