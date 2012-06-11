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
class BusinessIdentifierTable
{
  // private reference to the jUDDI logger
  private static Logger log = Logger.getLogger(BusinessIdentifierTable.class);

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
    dropSQL = "DROP TABLE BUSINESS_IDENTIFIER";

    // build createSQL
    sql = new StringBuffer(150);
    sql.append("CREATE TABLE BUSINESS_IDENTIFIER (");
    sql.append("BUSINESS_KEY VARCHAR(41) NOT NULL,");
    sql.append("IDENTIFIER_ID INT NOT NULL,");
    sql.append("TMODEL_KEY_REF VARCHAR(41) NULL,");
    sql.append("KEY_NAME VARCHAR(255) NULL,");
    sql.append("KEY_VALUE VARCHAR(255) NOT NULL,");
    sql.append("PRIMARY KEY (BUSINESS_KEY,IDENTIFIER_ID),");
    sql.append("FOREIGN KEY (BUSINESS_KEY) REFERENCES BUSINESS_ENTITY (BUSINESS_KEY))");
    createSQL = sql.toString();

    // build insertSQL
    sql = new StringBuffer(150);
    sql.append("INSERT INTO BUSINESS_IDENTIFIER (");
    sql.append("BUSINESS_KEY,");
    sql.append("IDENTIFIER_ID,");
    sql.append("TMODEL_KEY_REF,");
    sql.append("KEY_NAME,");
    sql.append("KEY_VALUE) ");
    sql.append("VALUES (?,?,?,?,?)");
    insertSQL = sql.toString();

    // build selectSQL
    sql = new StringBuffer(200);
    sql.append("SELECT ");
    sql.append("TMODEL_KEY_REF,");
    sql.append("KEY_NAME,");
    sql.append("KEY_VALUE ");
    sql.append("FROM BUSINESS_IDENTIFIER ");
    sql.append("WHERE BUSINESS_KEY=? ");
    sql.append("ORDER BY IDENTIFIER_ID");
    selectSQL = sql.toString();

    // build deleteSQL
    sql = new StringBuffer(100);
    sql.append("DELETE FROM BUSINESS_IDENTIFIER ");
    sql.append("WHERE BUSINESS_KEY=?");
    deleteSQL = sql.toString();
  }

  /**
   * Drop the BUSINESS_IDENTIFIER table.
   */
  public static void drop(Connection connection)
  {
    System.out.print("DROP TABLE BUSINESS_IDENTIFIER: ");
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
   * Create the BUSINESS_IDENTIFIER table.
   */
  public static void create(Connection connection)
  {
    System.out.print("CREATE TABLE BUSINESS_IDENTIFIER: ");
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
   * Insert new row into the BUSINESS_IDENTIFIER table.<p>
   *
   * @param businessKey BusinessKey to the parent BusinessEntity object.
   * @param keyRefs A Vector of KeyedReference instances to insert.
   * @param connection JDBC connection
   * @throws java.sql.SQLException
   */
  public static void insert(String businessKey,Vector keyRefs,Connection connection)
    throws java.sql.SQLException
  {
    PreparedStatement statement = null;

    try
    {
      statement = connection.prepareStatement(insertSQL);
      statement.setString(1,businessKey.toString());

      int listSize = keyRefs.size();
      for (int identifierID=0; identifierID<listSize; identifierID++)
      {
        KeyedReference keyRef = (KeyedReference)keyRefs.elementAt(identifierID);

        // extract values to insert
        String tModelKeyValue = null;
        if (keyRef.getTModelKey() != null)
          tModelKeyValue = keyRef.getTModelKey().toString();

        // set the values
        statement.setInt(2,identifierID);
        statement.setString(3,tModelKeyValue);
        statement.setString(4,keyRef.getKeyName());
        statement.setString(5,keyRef.getKeyValue());

        log.info("insert into BUSINESS_IDENTIFIER table:\n\n\t" + insertSQL +
          "\n\t BUSINESS_KEY=" + businessKey.toString() +
          "\n\t IDENTIFIER_ID=" + identifierID +
          "\n\t TMODEL_KEY_REF=" + tModelKeyValue +
          "\n\t KEY_NAME=" + keyRef.getKeyName() +
          "\n\t KEY_VALUE=" + keyRef.getKeyValue() + "\n");

        // insert!
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
   * Select all rows from the BUSINESS_IDENTIFIER table for a given BusinessKey.<p>
   *
   * @param  businessKey BusinessKey
   * @param  connection JDBC connection
   * @throws java.sql.SQLException
   */
  public static Vector select(String businessKey,Connection connection)
    throws java.sql.SQLException
  {
    Vector keyRefList = new Vector();
    PreparedStatement statement = null;
    ResultSet resultSet = null;

    try
    {
      // create a statement to query with
      statement = connection.prepareStatement(selectSQL);
      statement.setString(1,businessKey.toString());

      log.info("select from BUSINESS_IDENTIFIER table:\n\n\t" + selectSQL +
        "\n\t BUSINESS_KEY=" + businessKey.toString() + "\n");

      // execute the statement
      resultSet = statement.executeQuery();

      KeyedReference keyRef = null;
      while (resultSet.next())
      {
        keyRef = new KeyedReference();
        keyRef.setTModelKey(resultSet.getString("TMODEL_KEY_REF"));
        keyRef.setKeyName(resultSet.getString("KEY_NAME"));
        keyRef.setKeyValue(resultSet.getString("KEY_VALUE"));
        keyRefList.add(keyRef);
        keyRef = null;
      }

      log.info("select was successful, rows selected=" + keyRefList.size());
      return keyRefList;
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
   * Delete multiple rows from the BUSINESS_IDENTIFIER table that are assigned to the
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

      log.info("delete from BUSINESS_IDENTIFIER table:\n\n\t" + deleteSQL +
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

        Vector keyRefs = new Vector();
        keyRefs.add(new KeyedReference(UUID.nextID(),"blah, blah, blah"));
        keyRefs.add(new KeyedReference(UUID.nextID(),"Yadda, Yadda, Yadda"));
        keyRefs.add(new KeyedReference(UUID.nextID(),"WhoobWhoobWhoobWhoob"));
        keyRefs.add(new KeyedReference(UUID.nextID(),"Haachachachacha"));

        String authorizedUserID = "sviens";

        // begin a new transaction
        txn.begin(connection);

        // insert a new BusinessEntity
        BusinessEntityTable.insert(business,authorizedUserID,connection);

        // insert a Collection of new Identifier KeyedReference objects
        BusinessIdentifierTable.insert(businessKey,keyRefs,connection);

        // insert another new BusinessEntity
        business.setBusinessKey(UUID.nextID());
        BusinessEntityTable.insert(business,authorizedUserID,connection);

        // insert another Collection of new Identifier KeyedReference objects
        BusinessIdentifierTable.insert(business.getBusinessKey(),keyRefs,connection);

        // select a Collection of Identifier KeyedReference objects
        keyRefs = BusinessIdentifierTable.select(businessKey,connection);

        // delete a Collection of Identifier KeyedReference objects
        BusinessIdentifierTable.delete(businessKey,connection);

        // re-select a Collection of Identifier KeyedReference objects
        keyRefs = BusinessIdentifierTable.select(businessKey,connection);

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
