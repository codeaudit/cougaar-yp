/*
 * jUDDI - An open source Java implementation of UDDI v2.0
 * http://juddi.org/
 *
 * Copyright (c) 2002, InflexionPoint and contributors
 * All rights reserved.
 */

package org.juddi.datastore.jdbc;

import org.juddi.datastore.*;

import org.apache.log4j.Logger;

import javax.sql.DataSource;
import javax.naming.Context;
import javax.naming.InitialContext;
import java.sql.Connection;
import java.sql.SQLException;
  
/**
 * @author Steve Viens (steve@inflexionpoint.com)
 * @version 0.6.2
 */
public class JDBCDataStoreFactory extends DataStoreFactory
{
  // private reference to the jUDDI logger
  private static Logger log = Logger.getLogger(JDBCDataStoreFactory.class);

  // private reference to the jUDDI DataSource
  private static DataSource dataSource = null;

  /**
   *
   */
  public JDBCDataStoreFactory()
  {
    try {
      InitialContext initialContext = new InitialContext();
      Context context = (Context)initialContext.lookup("java:comp/env");
      this.dataSource = (javax.sql.DataSource)context.lookup("jdbc/juddidb");
    }
    catch (Exception ex) {
      ex.printStackTrace();
    }
  }

  /**
   *
   */
  public DataStore aquireDataStore()
  {
    Connection conn = null;

    // aquire a connection from the DataSource connection pool.
    try {
      conn = dataSource.getConnection();
    }
    catch(SQLException sqlex) {
      log.error("Exception occured while attempting to aquire a JDBC " +
        "connection from the connection pool: "+sqlex.getMessage());
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

    // release the connection back into the DataSource connection pool.
    try {
      conn.close();
    }
    catch(SQLException sqlex) {
      log.error("Exception occured while attempting to return/release a " +
        "JDBC connection back into the pool: "+sqlex.getMessage());
    }
  }
}