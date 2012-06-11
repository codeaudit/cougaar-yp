/*
 * jUDDI - An open source Java implementation of UDDI v2.0
 * http://juddi.org/
 *
 * Copyright (c) 2002, InflexionPoint and contributors
 * All rights reserved.
 */

package org.juddi.datastore.jdbc;

import org.juddi.util.UUID;

import org.uddi4j.datatype.*;
import org.uddi4j.datatype.tmodel.*;
import org.apache.log4j.Logger;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.Statement;
import java.sql.Timestamp;
import java.util.Vector;

/**
 * @author Steve Viens (steve@inflexionpoint.com)
 * @version 0.6.2
 */
class TModelTable
{
  // private reference to the jUDDI logger
  private static Logger log = Logger.getLogger(TModelTable.class);

  static String dropSQL = null;
  static String createSQL = null;
  static String insertSQL = null;
  static String deleteSQL = null;
  static String selectSQL = null;
  static String selectByPublisherSQL = null;
  static String verifyOwnershipSQL = null;

  static
  {
    // buffer used to build SQL statements
    StringBuffer sql = null;

    // build dropSQL
    dropSQL = "DROP TABLE TMODEL";

    // build createSQL
    sql = new StringBuffer(150);
    sql.append("CREATE TABLE TMODEL (");
    sql.append("TMODEL_KEY VARCHAR(41) NOT NULL,");
    sql.append("AUTHORIZED_NAME VARCHAR(255) NOT NULL,");
    sql.append("PUBLISHER_ID VARCHAR(20) NULL,");
    sql.append("OPERATOR VARCHAR(255) NOT NULL,");
    sql.append("NAME VARCHAR(255) NOT NULL,");
    sql.append("OVERVIEW_URL VARCHAR(255) NULL,");
    sql.append("LAST_UPDATE TIMESTAMP NOT NULL,");
    sql.append("PRIMARY KEY (TMODEL_KEY))");
    createSQL = sql.toString();

    // build insertSQL
    sql = new StringBuffer(150);
    sql.append("INSERT INTO TMODEL (");
    sql.append("TMODEL_KEY,");
    sql.append("AUTHORIZED_NAME,");
    sql.append("PUBLISHER_ID,");
    sql.append("OPERATOR,");
    sql.append("NAME,");
    sql.append("OVERVIEW_URL,");
    sql.append("LAST_UPDATE) ");
    sql.append("VALUES (?,?,?,?,?,?,?)");
    insertSQL = sql.toString();

    // build deleteSQL
    sql = new StringBuffer(100);
    sql.append("DELETE FROM TMODEL ");
    sql.append("WHERE TMODEL_KEY=?");
    deleteSQL = sql.toString();

    // build selectSQL
    sql = new StringBuffer(200);
    sql.append("SELECT ");
    sql.append("AUTHORIZED_NAME,");
    sql.append("OPERATOR,");
    sql.append("NAME,");
    sql.append("OVERVIEW_URL ");
    sql.append("FROM TMODEL ");
    sql.append("WHERE TMODEL_KEY=?");
    selectSQL = sql.toString();

    // build selectByPublisherSQL
    sql = new StringBuffer(200);
    sql.append("SELECT ");
    sql.append("TMODEL_KEY ");
    sql.append("FROM TMODEL ");
    sql.append("WHERE PUBLISHER_ID=?");
    selectByPublisherSQL = sql.toString();

    // build verifyOwnershipSQL
    sql = new StringBuffer(200);
    sql.append("SELECT ");
    sql.append("* ");
    sql.append("FROM TMODEL ");
    sql.append("WHERE TMODEL_KEY=? ");
    sql.append("AND PUBLISHER_ID=?");
    verifyOwnershipSQL = sql.toString();
  }

  /**
   * Drop the TMODEL table.
   */
  public static void drop(Connection connection)
  {
    System.out.print("DROP TABLE TMODEL: ");
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
   * Create the TMODEL table.
   */
  public static void create(Connection connection)
  {
    System.out.print("CREATE TABLE TMODEL: ");
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
   * Insert new row into the TMODEL table.
   *
   * @param tModel TModel object holding values to be inserted
   * @param publisherID
   * @param connection JDBC connection
   * @throws java.sql.SQLException
   */
  public static void insert(TModel tModel,String publisherID,Connection connection)
    throws java.sql.SQLException
  {
    PreparedStatement statement = null;
    Timestamp timeStamp = new Timestamp(System.currentTimeMillis());

    String overviewURL = null;
    if ((tModel.getOverviewDoc() != null) && (tModel.getOverviewDoc().getOverviewURL() != null))
      overviewURL = tModel.getOverviewDoc().getOverviewURL().getText();

    try
    {
      statement = connection.prepareStatement(insertSQL);
      statement.setString(1,tModel.getTModelKey().toString());
      statement.setString(2,tModel.getAuthorizedName());
      statement.setString(3,publisherID);
      statement.setString(4,tModel.getOperator());
      statement.setString(5,tModel.getName().getText());
      statement.setString(6,overviewURL);
      statement.setTimestamp(7,timeStamp);

      log.info("insert into TMODEL table:\n\n\t" + insertSQL +
        "\n\t TMODEL_KEY=" + tModel.getTModelKey().toString() +
        "\n\t AUTHORIZED_NAME=" + tModel.getAuthorizedName() +
        "\n\t PUBLISHER_ID=" + publisherID +
        "\n\t OPERATOR=" + tModel.getOperator() +
        "\n\t NAME=" + tModel.getName().getText() +
        "\n\t OVERVIEW_URL=" + overviewURL +
        "\n\t LAST_UPDATE=" + timeStamp.getTime() + "\n");

      // insert
      int returnCode = statement.executeUpdate();

      log.info("insert was successful, return code=" + returnCode);
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
   * Delete row from the TMODEL table.
   *
   * @param  tModelKey
   * @param  connection JDBC connection
   * @throws java.sql.SQLException
   */
  public static void delete(String tModelKey,Connection connection)
    throws java.sql.SQLException
  {
    PreparedStatement statement = null;

    try
    {
      // prepare the delete
      statement = connection.prepareStatement(deleteSQL);
      statement.setString(1,tModelKey.toString());

      log.info("delete from TMODEL table:\n\n\t" + deleteSQL +
        "\n\t TMODEL_KEY=" + tModelKey.toString() + "\n");

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

  /**
   * Select one row from the TMODEL table.
   *
   * @param  tModelKey
   * @param  connection
   * @throws java.sql.SQLException
   */
  public static TModel select(String tModelKey,Connection connection)
    throws java.sql.SQLException
  {
    TModel tModel = null;
    PreparedStatement statement = null;
    ResultSet resultSet = null;

    try
    {
      statement = connection.prepareStatement(selectSQL);
      statement.setString(1,tModelKey.toString());

      log.info("select from TMODEL table:\n\n\t" + selectSQL +
        "\n\t TMODEL_KEY=" + tModelKey.toString() + "\n");

      resultSet = statement.executeQuery();
      if (resultSet.next())
      {
        tModel = new TModel();
        tModel.setTModelKey(tModelKey);
        tModel.setAuthorizedName(resultSet.getString("AUTHORIZED_NAME"));
        tModel.setOperator(resultSet.getString("OPERATOR"));
        tModel.setName(resultSet.getString("NAME"));

        OverviewDoc overviewDoc = new OverviewDoc();
        overviewDoc.setOverviewURL(resultSet.getString("OVERVIEW_URL"));
        tModel.setOverviewDoc(overviewDoc);
      }

      if (tModel != null)
        log.info("select successful, at least one row was found");
      else
        log.info("select executed successfully but no rows were found with TMODEL_KEY=" + tModelKey.toString());

      return tModel;
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
   * Select all TModelKeys from the business_entities table for a given
   * 'publisherID' value.
   *
   * @param  publisherID The User ID of the TModel owner.
   * @param connection JDBC The JDBC connection
   * @throws java.sql.SQLException
   */
  public static Vector selectByPublisherID(String publisherID,Connection connection)
    throws java.sql.SQLException
  {
    Vector keyList = new Vector();
    PreparedStatement statement = null;
    ResultSet resultSet = null;

    try
    {
      // create a statement to query with
      statement = connection.prepareStatement(selectByPublisherSQL);
      statement.setString(1,publisherID.toString());

      log.info("select from TMODEL table:\n\n\t" + selectByPublisherSQL +
        "\n\t PUBLISHER_ID=" + publisherID + "\n");

      // execute the statement
      resultSet = statement.executeQuery();
      while (resultSet.next())
        keyList.add(resultSet.getString("TMODEL_KEY"));

      log.info("select was successful, rows selected=" + keyList.size());
      return keyList;
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
   * Verify that 'publisherID' has the authority to update or delete
   * TModel identified by the tModelKey parameter
   *
   * @param  tModelKey
   * @param  publisherID
   * @param  connection
   * @throws java.sql.SQLException
   */
  public static boolean verifyOwnership(String tModelKey,String publisherID,Connection connection)
    throws java.sql.SQLException
  {
    if ((tModelKey == null) || (publisherID == null))
      return false;

    boolean authorized = false;
    PreparedStatement statement = null;
    ResultSet resultSet = null;

    try
    {
      statement = connection.prepareStatement(verifyOwnershipSQL);
      statement.setString(1,tModelKey);
      statement.setString(2,publisherID);

      log.info("checking ownership of TMODEL:\n\n\t" + verifyOwnershipSQL +
        "\n\t TMODEL_KEY=" + tModelKey +
        "\n\t PUBLISHER_ID=" + publisherID + "\n");

      resultSet = statement.executeQuery();
      if (resultSet.next())
        authorized = true;
      
      if (authorized)
        log.info("authorization was successful, a matching row was found");
      else
        log.info("select executed successfully but authorization was unsuccessful");

      return authorized;
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
        OverviewDoc overviewDoc = new OverviewDoc();
        overviewDoc.setOverviewURL("http://www.inflexionpoint.com/jtruss.html");

        String tModelKey = UUID.nextID();
        TModel tModel = new TModel();
        tModel.setTModelKey(tModelKey);
        tModel.setAuthorizedName("Steve Viens");
        tModel.setOperator("WebServiceRegistry.com");
        tModel.setName("Tuscany Web Service Company");
        tModel.setOverviewDoc(overviewDoc);

        String publisherID = "sviens";

        // begin a new transaction
        txn.begin(connection);
        
        // insert a new TModel
        TModelTable.insert(tModel,publisherID,connection);

        // select one of the TModel objects
        tModel = TModelTable.select(tModelKey,connection);

        // select a Collection of TModel keys by PublisherID
        Vector tModelKeys = TModelTable.selectByPublisherID(publisherID,connection);

        TModelTable.verifyOwnership(tModelKey,"mviens",connection);
        TModelTable.verifyOwnership(tModelKey,"sviens",connection);

        // delete that TModel object
        //TModelTable.delete(tModelKey,connection);

        // re-select that TModel object
        tModel = TModelTable.select(tModelKey,connection);

        // re-select a Collection of TModel keys by PublisherID
        tModelKeys = TModelTable.selectByPublisherID(publisherID,connection);

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
