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
  private static final String jdbcUserID    = "sa";
  private static final String jdbcPassword  = "";

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
   *
   */
  public DataStore aquireDataStore()
  {
    Connection conn = null;

    // create a new JDBC connection
    try {
      conn = DriverManager.getConnection(jdbcURL,jdbcUserID,jdbcPassword);
    }
    catch(SQLException sqlex) {
      log.error("Exception occured while attempting to create a " +
        "new JDBC connection: "+sqlex.getMessage());
    }

    // create a JDBCDataStore with the connection.
    return new JDBCDataStore(conn);
  }

  /**
   *
   */
  public void releaseDataStore(DataStore datastore)
  {
    // get the connection from of the datastore.
    Connection conn = ((JDBCDataStore)datastore).getConnection();

    // close a JDBC connection
    try {
      conn.close();
    }
    catch(SQLException sqlex) {
      log.error("Exception occured while attempting to close a " +
        "JDBC connection: "+sqlex.getMessage());
    }
  }

  /**
   *
   */
  protected Connection getConnection()
  {
    Connection conn = null;

    // create a new JDBC connection
    try {
      conn = DriverManager.getConnection(jdbcURL,jdbcUserID,jdbcPassword);
    }
    catch(SQLException sqlex) {
      log.error("Exception occured while attempting to create a " +
        "new JDBC connection: "+sqlex.getMessage());
    }

    // create a JDBCDataStore with the connection.
    return conn;
  }
}