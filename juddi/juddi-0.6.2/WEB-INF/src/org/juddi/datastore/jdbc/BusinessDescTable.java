/*
 * jUDDI - An open source Java implementation of UDDI v2.0
 * http://juddi.org/
 *
 * Copyright (c) 2002, InflexionPoint and contributors
 * All rights reserved.
 */

package org.juddi.datastore.jdbc;

import org.juddi.util.UUID;

import org.uddi4j.datatype.Description;
import org.uddi4j.datatype.business.BusinessEntity;
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
class BusinessDescTable
{
  // private reference to the jUDDI logger
  private static Logger log = Logger.getLogger(BusinessDescTable.class);

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
    dropSQL = "DROP TABLE BUSINESS_DESCR";

    // build createSQL
    sql = new StringBuffer(150);
    sql.append("CREATE TABLE BUSINESS_DESCR (");
    sql.append("BUSINESS_KEY VARCHAR(41) NOT NULL,");
    sql.append("BUSINESS_DESCR_ID INT NOT NULL,");
    sql.append("LANG_CODE VARCHAR(2) NOT NULL,");
    sql.append("DESCR VARCHAR(255) NOT NULL,");
    sql.append("PRIMARY KEY (BUSINESS_KEY,BUSINESS_DESCR_ID),");
    sql.append("FOREIGN KEY (BUSINESS_KEY) REFERENCES BUSINESS_ENTITY (BUSINESS_KEY))");
    createSQL = sql.toString();

    // build insertSQL
    sql = new StringBuffer(150);
    sql.append("INSERT INTO BUSINESS_DESCR (");
    sql.append("BUSINESS_KEY,");
    sql.append("BUSINESS_DESCR_ID,");
    sql.append("LANG_CODE,");
    sql.append("DESCR) ");
    sql.append("VALUES (?,?,?,?)");
    insertSQL = sql.toString();

    // build selectSQL
    sql = new StringBuffer(200);
    sql.append("SELECT ");
    sql.append("LANG_CODE,");
    sql.append("DESCR ");
    sql.append("FROM BUSINESS_DESCR ");
    sql.append("WHERE BUSINESS_KEY=? ");
    sql.append("ORDER BY BUSINESS_DESCR_ID");
    selectSQL = sql.toString();

    // build deleteSQL
    sql = new StringBuffer(100);
    sql.append("DELETE FROM BUSINESS_DESCR ");
    sql.append("WHERE BUSINESS_KEY=?");
    deleteSQL = sql.toString();
  }

  /**
   * Drop the BUSINESS_DESCR table.
   */
  public static void drop(Connection connection)
  {
    System.out.print("DROP TABLE BUSINESS_DESCR: ");
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
   * Create the BUSINESS_DESCR table.
   */
  public static void create(Connection connection)
  {
    System.out.print("CREATE TABLE BUSINESS_DESCR: ");
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
   * Insert new row into the BUSINESS_DESCR table.<p>
   *
   * @param  businessKey BusinessKey to the BusinessEntity object that owns the Description to be inserted
   * @param  descList Vector of Description objects holding values to be inserted
   * @param  connection JDBC connection
   * @throws java.sql.SQLException
   */
  public static void insert(String businessKey,Vector descList,Connection connection)
    throws java.sql.SQLException
  {
    if ((descList == null) || (descList.size() == 0))
      return; // everything is valid but no elements to insert

    PreparedStatement statement = null;

    try
    {
      statement = connection.prepareStatement(insertSQL);
      statement.setString(1,businessKey.toString());

      int listSize = descList.size();
      for (int descID=0; descID<listSize; descID++)
      {
        Description desc = (Description)descList.elementAt(descID);

        statement.setInt(2,descID);
        statement.setString(3,desc.getLang());
        statement.setString(4,desc.getText());

        log.info("insert into BUSINESS_DESCR table:\n\n\t" + insertSQL +
          "\n\t BUSINESS_KEY=" + businessKey.toString() +
          "\n\t BUSINESS_DESCR_ID=" + descID +
          "\n\t LANG_CODE=" + desc.getLang() +
          "\n\t DESCR=" + desc.getText() + "\n");

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
   * Select all rows from the BUSINESS_DESCR table for a given BusinessKey.<p>
   *
   * @param  businessKey BusinessKey
   * @param  connection JDBC connection
   * @throws java.sql.SQLException
   */
  public static Vector select(String businessKey,Connection connection)
    throws java.sql.SQLException
  {
    Vector descList = new Vector();
    PreparedStatement statement = null;
    ResultSet resultSet = null;

    try
    {
      // create a statement to query with
      statement = connection.prepareStatement(selectSQL);
      statement.setString(1,businessKey.toString());

      log.info("select from BUSINESS_DESCR table:\n\n\t" + selectSQL +
        "\n\t BUSINESS_KEY=" + businessKey.toString() + "\n");

      // execute the statement
      resultSet = statement.executeQuery();

      Description desc = null;
      while (resultSet.next())
      {
        desc = new Description();
        desc.setLang(resultSet.getString("LANG_CODE"));
        desc.setText(resultSet.getString("DESCR"));
        descList.add(desc);
        desc = null;
      }

      log.info("select was successful, rows selected=" + descList.size());
      return descList;
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
   * Delete multiple rows from the BUSINESS_DESCR table that are assigned to the
   * BusinessKey specified.<p>
   *
   * @param  businessKey BusinessKey
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

      log.info("delete from BUSINESS_DESCR table:\n\n\t" + deleteSQL +
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

        Vector descList = new Vector();
        descList.add(new Description("blah, blah, blah","en"));
        descList.add(new Description("Yadda, Yadda, Yadda","it"));
        descList.add(new Description("WhoobWhoobWhoobWhoob","cy"));
        descList.add(new Description("Haachachachacha","km"));

        String authorizedUserID = "sviens";

        // begin a new transaction
        txn.begin(connection);

        // insert a new BusinessEntity
        BusinessEntityTable.insert(business,authorizedUserID,connection);

        // insert a Collection of Description objects
        BusinessDescTable.insert(businessKey,descList,connection);

        // select the Collection of Description objects
        descList = BusinessDescTable.select(businessKey,connection);

        //  // delete the Collection of Description objects
        //  BusinessDescTable.delete(businessKey,connection);

        // re-select the Collection of Description objects
        descList = BusinessDescTable.select(businessKey,connection);

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
