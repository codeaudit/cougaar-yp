/*
 * jUDDI - An open source Java implementation of UDDI v2.0
 * http://juddi.org/
 *
 * Copyright (c) 2002, InflexionPoint and contributors
 * All rights reserved.
 */

package org.juddi.datastore.jdbc;

import org.juddi.util.UUID;

import org.uddi4j.datatype.business.BusinessEntity;
import org.apache.log4j.Logger;

import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.PreparedStatement;
import java.sql.Statement;
import java.sql.Timestamp;
import java.util.Vector;

/**
 * @author Steve Viens (steve@inflexionpoint.com)
 * @version 0.6.2
 */
class BusinessEntityTable
{
  // private reference to the jUDDI logger
  private static Logger log = Logger.getLogger(BusinessEntityTable.class);

  static String dropSQL = null;
  static String createSQL = null;
  static String insertSQL = null;
  static String deleteSQL = null;
  static String selectSQL = null;
  static String selectByPublisherSQL = null;
  static String verifyOwnershipSQL = null;
  static String selectPublisherSQL = null;

  static
  {
    // buffer used to build SQL statements
    StringBuffer sql = null;

    // build dropSQL
    dropSQL = "DROP TABLE BUSINESS_ENTITY";

    // build createSQL
    sql = new StringBuffer(150);
    sql.append("CREATE TABLE BUSINESS_ENTITY (");
    sql.append("BUSINESS_KEY VARCHAR(41) NOT NULL,");
    sql.append("AUTHORIZED_NAME VARCHAR(255) NOT NULL,");
    sql.append("PUBLISHER_ID VARCHAR(20) NULL,");
    sql.append("OPERATOR VARCHAR(255) NOT NULL,");
    sql.append("LAST_UPDATE TIMESTAMP NOT NULL,");
    sql.append("PRIMARY KEY (BUSINESS_KEY))");
    createSQL = sql.toString();

    // build insertSQL
    sql = new StringBuffer(150);
    sql.append("INSERT INTO BUSINESS_ENTITY (");
    sql.append("BUSINESS_KEY,");
    sql.append("AUTHORIZED_NAME,");
    sql.append("PUBLISHER_ID,");
    sql.append("OPERATOR,");
    sql.append("LAST_UPDATE) ");
    sql.append("VALUES (?,?,?,?,?)");
    insertSQL = sql.toString();

    // build deleteSQL
    sql = new StringBuffer(100);
    sql.append("DELETE FROM BUSINESS_ENTITY ");
    sql.append("WHERE BUSINESS_KEY=?");
    deleteSQL = sql.toString();

    // build selectSQL
    sql = new StringBuffer(200);
    sql.append("SELECT ");
    sql.append("AUTHORIZED_NAME,");
    sql.append("OPERATOR ");
    sql.append("FROM BUSINESS_ENTITY ");
    sql.append("WHERE BUSINESS_KEY=?");
    selectSQL = sql.toString();

    // build selectByPublisherIDSQL
    sql = new StringBuffer(200);
    sql.append("SELECT ");
    sql.append("BUSINESS_KEY ");
    sql.append("FROM BUSINESS_ENTITY ");
    sql.append("WHERE PUBLISHER_ID=?");
    selectByPublisherSQL = sql.toString();

    // build verifyOwnershipSQL
    sql = new StringBuffer(200);
    sql.append("SELECT ");
    sql.append("* ");
    sql.append("FROM BUSINESS_ENTITY ");
    sql.append("WHERE BUSINESS_KEY=? ");
    sql.append("AND PUBLISHER_ID=?");
    verifyOwnershipSQL = sql.toString();

    // build selectPublisherSQL
    sql = new StringBuffer(200);
    sql.append("SELECT ");
    sql.append("PUBLISHER_ID ");
    sql.append("FROM BUSINESS_ENTITY ");
    sql.append("WHERE BUSINESS_KEY=?");
    selectPublisherSQL = sql.toString();
  }

  /**
   * Drop the BUSINESS_ENTITY table.
   */
  public static void drop(Connection connection)
  {
    System.out.print("DROP TABLE BUSINESS_ENTITY: ");
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
   * Create the BUSINESS_ENTITY table.
   */
  public static void create(Connection connection)
  {
    System.out.print("CREATE TABLE BUSINESS_ENTITY: ");
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
      catch (Exception e)
      {
        log.warn("An Exception was encountered while attempting to close " +
          "the 'Create BusinessEntity Table' Statement: "+e.getMessage(),e);
      }
    }
  }

  /**
   * Insert new row into the BUSINESS_ENTITY table.
   *
   * @param business object holding values to be inserted
   * @param publisherID
   * @param connection connection
   * @throws java.sql.SQLException
   */
  public static void insert(BusinessEntity business,String publisherID,Connection connection)
    throws java.sql.SQLException
  {
    PreparedStatement statement = null;
    Timestamp timeStamp = new Timestamp(System.currentTimeMillis());

    try
    {
      statement = connection.prepareStatement(insertSQL);
      statement.setString(1,business.getBusinessKey());
      statement.setString(2,business.getAuthorizedName());
      statement.setString(3,publisherID);
      statement.setString(4,business.getOperator());
      statement.setTimestamp(5,timeStamp);

      log.info("insert into BUSINESS_ENTITY table:\n\n\t" + insertSQL +
        "\n\t BUSINESS_KEY=" + business.getBusinessKey() +
        "\n\t AUTHORIZED_NAME=" + business.getAuthorizedName() +
        "\n\t PUBLISHER_ID=" + publisherID +
        "\n\t OPERATOR=" + business.getOperator() +
        "\n\t LAST_UPDATE=" + timeStamp.getTime() + "\n");

      int returnCode = statement.executeUpdate();

      log.info("insert into BUSINESS_ENTITY was successful, return code=" + returnCode);
    }
    finally
    {
      try {
        statement.close();
      }
      catch (Exception e)
      {
        log.warn("An Exception was encountered while attempting to close " +
          "the Insert BusinessEntity PreparedStatement: "+e.getMessage(),e);
      }
    }
  }

  /**
   * Delete row from the BUSINESS_ENTITY table.
   *
   * @param businessKey key value
   * @param  connection JDBC Connection
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

      log.info("delete from BUSINESS_ENTITY table:\n\n\t" + deleteSQL +
        "\n\t BUSINESS_KEY=" + businessKey.toString() + "\n");

      // execute the delete
      int returnCode = statement.executeUpdate();

      log.info("delete from BUSINESS_ENTITY was successful, rows deleted=" + returnCode);
    }
    finally
    {
      try {
        statement.close();
      }
      catch (Exception e)
      {
        log.warn("An Exception was encountered while attempting to close " +
          "the Delete BusinessEntity PreparedStatement: "+e.getMessage(),e);
      }
    }
  }

  /**
   * Select one row from the BUSINESS_ENTITY table.
   *
   * @param businessKey key value
   * @param  connection JDBC Connection
   * @throws java.sql.SQLException
   */
  public static BusinessEntity select(String businessKey,Connection connection)
    throws java.sql.SQLException
  {
    BusinessEntity business = null;
    PreparedStatement statement = null;
    ResultSet resultSet = null;

    try
    {
      statement = connection.prepareStatement(selectSQL);
      statement.setString(1,businessKey.toString());

      log.info("select from BUSINESS_ENTITY table:\n\n\t" + selectSQL +
        "\n\t BUSINESS_KEY=" + businessKey.toString() + "\n");

      resultSet = statement.executeQuery();
      if (resultSet.next())
      {
        business = new BusinessEntity();
        business.setBusinessKey(businessKey);
        business.setAuthorizedName(resultSet.getString("AUTHORIZED_NAME"));
        business.setOperator(resultSet.getString("OPERATOR"));
      }

      if (business != null)
        log.info("select successful, at least one row was found");
      else
        log.info("select executed successfully but no rows were found with BUSINESS_KEY=" + businessKey.toString());

      return business;
    }
    finally
    {
      try {
        resultSet.close();
        statement.close();
      }
      catch (Exception e)
      {
        log.warn("An Exception was encountered while attempting to close " +
          "the Select BusinessEntity ResultSet and PreparedStatement: "+e.getMessage(),e);
      }
    }
  }

  /**
   * Select all BusinessKeys from the business_entities table for a given
   * 'publisherID' value.
   *
   * @param publisherID The user id of the BusinessEntity owner.
   * @param connection JDBC connection
   * @return Vector A Vector of BusinessKeys
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
      statement.setString(1,publisherID);

      log.info("select from BUSINESS_ENTITY table:\n\n\t" + selectByPublisherSQL +
        "\n\t PUBLISHER_ID=" + publisherID + "\n");

      // execute the statement
      resultSet = statement.executeQuery();
      while (resultSet.next())
        keyList.add(resultSet.getString("BUSINESS_KEY"));

      log.info("select from the BUSINESS_ENTITY was successful, rows selected=" + keyList.size());
      return keyList;
    }
    finally
    {
      try {
        resultSet.close();
        statement.close();
      }
      catch (Exception e)
      {
        log.warn("An Exception was encountered while attempting to close " +
          "the Select BusinessEntity ResultSet and PreparedStatement: "+e.getMessage(),e);
      }
    }
  }

  /**
   * Verify that 'publisherID' has the authority to update or delete
   * BusinessEntity identified by the businessKey parameter
   *
   * @param  businessKey
   * @param  publisherID
   * @param  connection
   * @throws java.sql.SQLException
   */
  public static boolean verifyOwnership(String businessKey,String publisherID,Connection connection)
    throws java.sql.SQLException
  {
    if ((businessKey == null) || (publisherID == null))
      return false;

    boolean authorized = false;
    PreparedStatement statement = null;
    ResultSet resultSet = null;

    try
    {
      statement = connection.prepareStatement(verifyOwnershipSQL);
      statement.setString(1,businessKey);
      statement.setString(2,publisherID);

      log.info("checking ownership of BUSINESS_ENTITY:\n\n\t" + verifyOwnershipSQL +
        "\n\t BUSINESS_KEY=" + businessKey +
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

  /**
   * Return the 'publisherID' for the BusinessEntity identified by the
   * businessKey parameter. Retusn null if the business entity key does
   * not represent a valid BuisnessEntity or if no publisherID is specified
   * for that particular BusinessEntity.
   *
   * @param  businessKey
   * @param  connection
   * @return publisherID or null if no publisherID is available.
   * @throws java.sql.SQLException
   */
  public static String selectPublisherID(String businessKey,Connection connection)
    throws java.sql.SQLException
  {
    if (businessKey == null)
      return null;

    String publisherID = null;
    PreparedStatement statement = null;
    ResultSet resultSet = null;

    try
    {
      statement = connection.prepareStatement(selectPublisherSQL);
      statement.setString(1,businessKey);

      log.info("fetching publishers ID for BUSINESS_ENTITY:\n\n\t" + selectPublisherSQL +
        "\n\t BUSINESS_KEY=" + businessKey + "\n");

      resultSet = statement.executeQuery();
      if (resultSet.next())
        publisherID = resultSet.getString("PUBLISHER_ID");

      if (publisherID != null)
        log.info("fetch publisher ID was successful, a matching row was found");
      else
        log.info("select executed successfully but a publisher id wasn't found for business key: "+businessKey);

      return publisherID;
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
        String businessKey = UUID.nextID();
        BusinessEntity business = new BusinessEntity();
        business.setBusinessKey(businessKey);
        business.setAuthorizedName("Steve Viens");
        business.setOperator("www.jUDDI.org");

        String publisherID = "sviens";

        // begin a new transaction
        txn.begin(connection);

        // insert a new BusinessEntity
        BusinessEntityTable.insert(business,publisherID,connection);

        // select one of the BusinessEntity objects
        business = BusinessEntityTable.select(businessKey,connection);

        // delete that BusinessEntity object
        //BusinessEntityTable.delete(businessKey,connection);

        // re-select that BusinessEntity object
        business = BusinessEntityTable.select(businessKey,connection);

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
