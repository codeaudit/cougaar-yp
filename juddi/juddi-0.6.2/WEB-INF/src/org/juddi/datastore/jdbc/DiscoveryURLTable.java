/*
 * jUDDI - An open source Java implementation of UDDI v2.0
 * http://juddi.org/
 *
 * Copyright (c) 2002, InflexionPoint and contributors
 * All rights reserved.
 */

package org.juddi.datastore.jdbc;

import org.juddi.util.UUID;

import org.uddi4j.datatype.business.*;
import org.uddi4j.util.*;
import org.apache.log4j.Logger;

import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.PreparedStatement;
import java.sql.Statement;
import java.util.Vector;

/**
 * @author Steve Viens (steve@inflexionpoint.com)
 * @version 0.6.2
 */
class DiscoveryURLTable
{
  // private reference to the jUDDI logger
  private static Logger log = Logger.getLogger(DiscoveryURLTable.class);

  static String dropSQL = null;
  static String createSQL = null;
  static String insertSQL = null;
  static String selectSQL = null;
  static String deleteSQL = null;

  static
  {
    // buffer used to build SQL statements
    StringBuffer sql = null;

    // build dropSQL
    dropSQL = "DROP TABLE DISCOVERY_URL";

    // build createSQL
    sql = new StringBuffer(150);
    sql.append("CREATE TABLE DISCOVERY_URL (");
    sql.append("BUSINESS_KEY VARCHAR(41) NOT NULL,");
    sql.append("DISCOVERY_URL_ID INT NOT NULL,");
    sql.append("USE_TYPE VARCHAR(255) NOT NULL,");
    sql.append("URL VARCHAR(255) NOT NULL,");
    sql.append("PRIMARY KEY (BUSINESS_KEY,DISCOVERY_URL_ID),");
    sql.append("FOREIGN KEY (BUSINESS_KEY) REFERENCES BUSINESS_ENTITY (BUSINESS_KEY))");
    createSQL = sql.toString();

    // build insertSQL
    sql = new StringBuffer(150);
    sql.append("INSERT INTO DISCOVERY_URL (");
    sql.append("BUSINESS_KEY,");
    sql.append("DISCOVERY_URL_ID,");
    sql.append("USE_TYPE,");
    sql.append("URL) ");
    sql.append("VALUES (?,?,?,?)");
    insertSQL = sql.toString();

    // build selectSQL
    sql = new StringBuffer(200);
    sql.append("SELECT ");
    sql.append("USE_TYPE,");
    sql.append("URL ");
    sql.append("FROM DISCOVERY_URL ");
    sql.append("WHERE BUSINESS_KEY=? ");
    sql.append("ORDER BY DISCOVERY_URL_ID");
    selectSQL = sql.toString();

    // build deleteSQL
    sql = new StringBuffer(100);
    sql.append("DELETE FROM DISCOVERY_URL ");
    sql.append("WHERE BUSINESS_KEY=?");
    deleteSQL = sql.toString();
  }

  /**
   * Drop the DISCOVERY_URL table.
   */
  public static void drop(Connection connection)
  {
    System.out.print("DROP TABLE DISCOVERY_URL: ");
    Statement statement = null;

    try
    {
      statement = connection.createStatement();
      int returnCode = statement.executeUpdate(dropSQL);
      System.out.println("Successful (return code=" + returnCode + ")");
    }
    catch (java.sql.SQLException sqlex)
    {
      System.out.println("Failed (error message="+sqlex.getMessage() + ")\n");
      System.out.println("SQL="+dropSQL);
    }
    finally
    {
      try {
        statement.close();
      }
      catch (Exception e) { } // nothing we can do about this!
    }
  }

  /**
   * Create the DISCOVERY_URL table.
   */
  public static void create(Connection connection)
  {
    System.out.print("CREATE TABLE DISCOVERY_URL: ");
    Statement statement = null;

    try
    {
      statement = connection.createStatement();
      int returnCode = statement.executeUpdate(createSQL);
      System.out.println("Successful (return code=" + returnCode + ")");
    }
    catch (java.sql.SQLException sqlex)
    {
      System.out.println("Failed (error message="+sqlex.getMessage() + ")\n");
      System.out.println("SQL="+createSQL);
    }
    finally
    {
      try {
        statement.close();
      }
      catch (Exception e) { } // nothing we can do about this!
    }
  }

  /**
   * Insert new row into the DISCOVERY_URL table.<p>
   *
   * @param  businessKey String to the BusinessEntity object that owns
   *  the Description to be inserted
   * @param urlList Vector of Description objects holding values to be inserted
   * @param connection JDBC connection
   * @throws java.sql.SQLException
   */
  public static void insert(String businessKey,Vector urlList,Connection connection)
    throws java.sql.SQLException
  {
    if ((urlList == null) || (urlList.size() == 0))
      return; // everything is valid but no elements to insert

    PreparedStatement statement = null;

    try
    {
      statement = connection.prepareStatement(insertSQL);
      statement.setString(1,businessKey.toString());

      int listSize = urlList.size();
      for (int urlID=0; urlID<listSize; urlID++)
      {
        DiscoveryURL url = (DiscoveryURL)urlList.elementAt(urlID);

        String urlValue = null;
        if (url.getText() != null)
          urlValue = url.getText().toString();

        log.info("insert into DISCOVERY_URL table:\n\n\t" + insertSQL +
          "\n\t BUSINESS_KEY=" + businessKey.toString() +
          "\n\t DISCOVERY_URL_ID=" + urlID +
          "\n\t USE_TYPE=" + url.getUseType() +
          "\n\t URL=" + urlValue + "\n");

        statement.setInt(2,urlID);
        statement.setString(3,url.getUseType());
        statement.setString(4,urlValue);

        int returnCode = statement.executeUpdate();

        log.info("insert was successful, return code=" + returnCode);
      }
    }
    finally
    {
      try {
        statement.close();
      }
      catch (Exception e) { } // nothing we can do about this!
    }
  }

  /**
   * Select all rows from the DISCOVERY_URL table for a given BusinessKey.<p>
   *
   * @param  businessKey String
   * @param  connection JDBC connection
   * @throws java.sql.SQLException
   */
  public static Vector select(String businessKey,Connection connection)
    throws java.sql.SQLException
  {
    Vector urlList = new Vector();
    PreparedStatement statement = null;
    ResultSet resultSet = null;

    try
    {
      // create a statement to query with
      statement = connection.prepareStatement(selectSQL);
      statement.setString(1,businessKey.toString());

      log.info("select from DISCOVERY_URL table:\n\n\t" + selectSQL +
        "\n\t BUSINESS_KEY=" + businessKey.toString() + "\n");

      // execute the statement
      resultSet = statement.executeQuery();

      DiscoveryURL url = null;
      while (resultSet.next())
      {
        url = new DiscoveryURL();
        url.setUseType(resultSet.getString("USE_TYPE"));
        url.setText(resultSet.getString("URL"));
        urlList.add(url);
        url = null;
      }

      log.info("select was successful, rows selected=" + urlList.size());
      return urlList;
    }
    finally
    {
      try {
        resultSet.close();
        statement.close();
      }
      catch (Exception e) { } // nothing we can do about this!
    }
  }

  /**
   * Delete multiple rows from the DISCOVERY_URL table that are assigned to the
   * BusinessKey specified.<p>
   *
   * @param  businessKey String
   * @param  connection JDBC connection
   * @throws java.sql.SQLException
   */
  public static void delete(String businessKey,Connection connection)
    throws java.sql.SQLException
  {
    PreparedStatement statement = null;

    try
    {
      // prepare the delete
      statement = connection.prepareStatement(deleteSQL);
      statement.setString(1,businessKey.toString());

      log.info("delete from DISCOVERY_URL table:\n\n\t" + deleteSQL +
        "\n\t BUSINESS_KEY=" + businessKey.toString() + "\n");

      // execute the delete
      int returnCode = statement.executeUpdate();

      log.info("delete was successful, rows deleted=" + returnCode);
    }
    finally
    {
      try {
        statement.close();
      }
      catch (Exception e) { } // nothing we can do about this!
    }
  }


  /***************************************************************************/
  /***************************** TEST DRIVER *********************************/
  /***************************************************************************/


  // unit test-driver
  public static void main(String[] args)
  {
    org.juddi.util.SysManager.startup();

    try {
      Connection connection = (new org.juddi.datastore.jdbc.HSQLDataStoreFactory()).getConnection();
      test(connection);
      connection.close();
    }
    catch (Exception ex) {
      ex.printStackTrace();
    }

    org.juddi.util.SysManager.shutdown();
  }

  // system test-driver
  public static void test(Connection connection)
  {
    Transaction txn = new Transaction();

    if (connection != null)
    {
      try
      {
        String businessKey = UUID.nextID();
        BusinessEntity business = new BusinessEntity();
        business.setBusinessKey(businessKey);
        business.setAuthorizedName("sviens");
        business.setOperator("WebServiceRegistry.com");

        Vector urlList = new Vector();
        urlList.add(new DiscoveryURL("businessEntity","http://www.steveviens.com/abc"));
        urlList.add(new DiscoveryURL("businessEntity","http://www.steveviens.com/def"));
        urlList.add(new DiscoveryURL("businessEntityExt","http://www.steveviens.com/ghi"));
        urlList.add(new DiscoveryURL("businessEntityExt","http://www.steveviens.com/jkl"));

        String authorizedUserID = "sviens";

        // begin a new transaction
        txn.begin(connection);

        // insert a new BusinessEntity
        BusinessEntityTable.insert(business,authorizedUserID,connection);

        // insert a Collection DiscoveryURL objects
        DiscoveryURLTable.insert(businessKey,urlList,connection);

        // select a Collection DiscoveryURL objects by BusinessKey
        urlList = DiscoveryURLTable.select(businessKey,connection);

        // delete a Collection DiscoveryURL objects by BusinessKey
        DiscoveryURLTable.delete(businessKey,connection);

        // re-select a Collection DiscoveryURL objects by BusinessKey
        urlList = DiscoveryURLTable.select(businessKey,connection);

        // commit the transaction
        txn.commit();
      }
      catch(Exception ex)
      {
        ex.printStackTrace();
        try {
          txn.rollback();
        }
        catch(java.sql.SQLException sqlex) {
          sqlex.printStackTrace();
        }
      }
    }
  }
}
