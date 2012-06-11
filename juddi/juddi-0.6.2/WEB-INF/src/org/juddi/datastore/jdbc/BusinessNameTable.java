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
import org.uddi4j.datatype.business.*;
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
class BusinessNameTable
{
  // private reference to the jUDDI logger
  private static Logger log = Logger.getLogger(BusinessNameTable.class);

  static String dropSQL = null;
  static String createSQL = null;
  static String insertSQL = null;
  static String selectSQL = null;
  static String deleteSQL = null;
  static String selectNameSQL = null;

  static
  {
    // buffer used to build SQL statements
    StringBuffer sql = null;

    // build dropSQL
    dropSQL = "DROP TABLE BUSINESS_NAME";

    // build createSQL
    sql = new StringBuffer(150);
    sql.append("CREATE TABLE BUSINESS_NAME (");
    sql.append("BUSINESS_KEY VARCHAR(41) NOT NULL,");
    sql.append("BUSINESS_NAME_ID INT NOT NULL,");
    sql.append("LANG_CODE VARCHAR(2) NULL,");
    sql.append("NAME VARCHAR(255) NOT NULL,");
    sql.append("PRIMARY KEY (BUSINESS_KEY,BUSINESS_NAME_ID),");
    sql.append("FOREIGN KEY (BUSINESS_KEY) REFERENCES BUSINESS_ENTITY (BUSINESS_KEY))");
    createSQL = sql.toString();

    // build insertSQL
    sql = new StringBuffer(150);
    sql.append("INSERT INTO BUSINESS_NAME (");
    sql.append("BUSINESS_KEY,");
    sql.append("BUSINESS_NAME_ID,");
    sql.append("LANG_CODE,");
    sql.append("NAME) ");
    sql.append("VALUES (?,?,?,?)");
    insertSQL = sql.toString();

    // build selectSQL
    sql = new StringBuffer(200);
    sql.append("SELECT ");
    sql.append("LANG_CODE,");
    sql.append("NAME ");
    sql.append("FROM BUSINESS_NAME ");
    sql.append("WHERE BUSINESS_KEY=? ");
    sql.append("ORDER BY BUSINESS_NAME_ID");
    selectSQL = sql.toString();

    // build deleteSQL
    sql = new StringBuffer(100);
    sql.append("DELETE FROM BUSINESS_NAME ");
    sql.append("WHERE BUSINESS_KEY=?");
    deleteSQL = sql.toString();

    // build selectNameSQL
    sql = new StringBuffer(200);
    sql.append("SELECT ");
    sql.append("NAME ");
    sql.append("FROM BUSINESS_NAME ");
    sql.append("WHERE BUSINESS_KEY=? AND LANG_CODE=? ");
    selectNameSQL = sql.toString();
  }

  /**
   * Drop the BUSINESS_NAME table.
   */
  public static void drop(Connection connection)
  {
    System.out.print("DROP TABLE BUSINESS_NAME: ");
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
   * Create the BUSINESS_NAME table.
   */
  public static void create(Connection connection)
  {
    System.out.print("CREATE TABLE BUSINESS_NAME: ");
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
   * Insert new row into the BUSINESS_NAME table.<p>
   *
   * @param  businessKey String to the BusinessEntity object that owns the Contact to be inserted
   * @param  nameList Vector of Phone objects holding values to be inserted
   * @param  connection JDBC connection
   * @throws java.sql.SQLException
   */
  public static void insert(String businessKey,Vector nameList,Connection connection)
    throws java.sql.SQLException
  {
    if ((nameList == null) || (nameList.size() == 0))
      return; // everything is valid but no elements to insert

    PreparedStatement statement = null;

    try
    {
      statement = connection.prepareStatement(insertSQL);
      statement.setString(1,businessKey.toString());

      int listSize = nameList.size();
      for (int nameID=0; nameID<listSize; nameID++)
      {
        Name name = (Name)nameList.elementAt(nameID);

        statement.setInt(2,nameID);
        statement.setString(3,name.getLang());
        statement.setString(4,name.getText());

        log.info("insert into BUSINESS_NAME table:\n\n\t" + insertSQL +
          "\n\t BUSINESS_KEY=" + businessKey.toString() +
          "\n\t BUSINESS_NAME_ID=" + nameID +
          "\n\t LANG_CODE=" + name.getLang() +
          "\n\t NAME=" + name.getText() + "\n");

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
   * Select all rows from the BUSINESS_NAME table for a given BusinessKey.<p>
   *
   * @param  businessKey String
   * @param  connection JDBC connection
   * @throws java.sql.SQLException
   */
  public static Vector select(String businessKey,Connection connection)
    throws java.sql.SQLException
  {
    Vector nameList = new Vector();
    PreparedStatement statement = null;
    ResultSet resultSet = null;

    try
    {
      // create a statement to query with
      statement = connection.prepareStatement(selectSQL);
      statement.setString(1,businessKey.toString());

      log.info("select from the BUSINESS_NAME table:\n\n\t" + selectSQL +
        "\n\t BUSINESS_KEY=" + businessKey.toString() + "\n");

      // execute the statement
      resultSet = statement.executeQuery();

      Name name = null;
      while (resultSet.next())
      {
        name = new Name();
        name.setText(resultSet.getString("NAME"));
        name.setLang(resultSet.getString("LANG_CODE"));
        nameList.add(name);
        name = null;
      }

      log.info("select was successful, rows selected=" + nameList.size());
      return nameList;
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
   * Delete multiple rows from the BUSINESS_NAME table that are assigned to the
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

      log.info("delete from the BUSINESS_NAME table:\n\n\t" + deleteSQL +
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

  /**
   * Select all rows from the BUSINESS_NAME table for a given BusinessKey.<p>
   *
   * @param  names Vector of Name objects
   * @param  connection JDBC connection
   * @throws java.sql.SQLException
   */
  public static Vector selectBusinessKey(Vector names,Connection connection)
    throws java.sql.SQLException
  {
    Vector keyList = new Vector();

    if (names.size() == 0)
      return keyList;

    // piece the database query together
    StringBuffer sql = new StringBuffer();
    sql.append("SELECT DISTINCT BUSINESS_KEY FROM BUSINESS_NAME ");

    for (int i=0; i<names.size(); i++)
    {
      Name name = (Name)names.elementAt(i);
      String text = name.getText();
      String lang = name.getLang();

      sql.append((i == 0) ? "WHERE" : "OR");
      if (lang != null)
        sql.append(" (NAME LIKE '"+text+"%' AND LANG_CODE = '"+lang+"') ");
      else
        sql.append(" (NAME LIKE '"+text+"%') ");
    }

    // execute the database query.
    Statement statement = null;
    ResultSet resultSet = null;

    try
    {
      // create the statement
      statement = connection.createStatement();

      log.info("select BUSINESS_KEYs from the BUSINESS_NAME table: "+sql.toString()+"\n");

      // execute the statement
      resultSet = statement.executeQuery(sql.toString());
      while (resultSet.next())
        keyList.add(resultSet.getString("BUSINESS_KEY"));

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
   * Select all rows from the BUSINESS_NAME table for a given BusinessKey.<p>
   *
   * @param  names Vector of Name objects
   * @param  names Vector of BusinessKey String objects
   * @param  connection JDBC connection
   * @throws java.sql.SQLException
   */
  public static Vector selectBusinessKey(Vector names,Vector businessKeys,Connection connection)
    throws java.sql.SQLException
  {
    Vector keyList = new Vector();

    if ((names == null) || (names.size() == 0) || (businessKeys == null) || (businessKeys.size() == 0))
      return keyList;

    // piece the database query together
    StringBuffer sql = new StringBuffer();
    sql.append("SELECT DISTINCT BUSINESS_KEY FROM BUSINESS_NAME ");

    for (int i=0; i<names.size(); i++)
    {
      Name name = (Name)names.elementAt(i);
      String text = name.getText();
      String lang = name.getLang();

      sql.append((i == 0) ? "WHERE (" : "OR ");
      if (lang != null)
        sql.append("(NAME LIKE '"+text+"%' AND LANG_CODE = '"+lang+"') ");
      else
        sql.append("(NAME LIKE '"+text+"%') ");
    }

    sql.append(") AND BUSINESS_KEY IN (");
    for (int i=0; i<businessKeys.size(); i++)
    {
      sql.append("'"+(String)businessKeys.elementAt(i)+"'");
      if ((i+1) < businessKeys.size())
        sql.append(",");
    }
    sql.append(")");


    // execute the database query.
    Statement statement = null;
    ResultSet resultSet = null;

    try
    {
      // create the statement
      statement = connection.createStatement();

      log.info("select BUSINESS_KEYs from the BUSINESS_NAME table: "+sql.toString()+"\n");

      // execute the statement
      resultSet = statement.executeQuery(sql.toString());
      while (resultSet.next())
        keyList.add(resultSet.getString("BUSINESS_KEY"));

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
   * Select a particular BUSINESS_ENTITY NAME for a given business key
   * and language code.<p>
   *
   * @param  businessKey String business entity key
   * @param  langCode String language id (i.e. 'en', 'fr', etc.)
   * @param  connection JDBC connection
   * @throws java.sql.SQLException
   */
  public static Name selectBusinessName(String businessKey,String langCode,Connection connection)
    throws java.sql.SQLException
  {
    if ((businessKey == null) || (businessKey.length() == 0))
      return null;

    PreparedStatement statement = null;
    ResultSet resultSet = null;

    try
    {
      // create a statement to query with
      statement = connection.prepareStatement(selectNameSQL);
      statement.setString(1,businessKey);
      statement.setString(2,langCode);

      log.info("select from the BUSINESS_NAME table:\n\n\t" + selectNameSQL +
        "\n\t BUSINESS_KEY=" + businessKey.toString() +
        "\n\t LANG_CODE=" + langCode + "\n");

      // execute the statement
      resultSet = statement.executeQuery();

      Name name = null;
      if (resultSet.next())
      {
        name = new Name();
        name.setText(resultSet.getString("NAME"));
        name.setLang(resultSet.getString("LANG_CODE"));
      }

      log.info("select was successful, business name located=" + name.getText());
      return name;
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
        business.setAuthorizedName("sviens");
        business.setOperator("WebServiceRegistry.com");

        Vector nameList = new Vector();
        nameList.add(new Name("SteveViens.com","en"));
        nameList.add(new Name("EsephanoViens.com","it"));
        nameList.add(new Name("AsdfJkl.com","cy"));
        nameList.add(new Name("AsdfJkl.com"));

        String authorizedUserID = "sviens";

        // begin a new transaction
        txn.begin(connection);

        // insert a new BusinessEntity
        BusinessEntityTable.insert(business,authorizedUserID,connection);

        // insert a Collection of Name objects
        BusinessNameTable.insert(businessKey,nameList,connection);

        // select the Collection of Name objects
        nameList = BusinessNameTable.select(businessKey,connection);

        // delete the Collection of Name objects
        //BusinessNameTable.delete(businessKey,connection);

        // re-select the Collection of Name objects
        nameList = BusinessNameTable.select(businessKey,connection);

        Vector keyList = null;

        nameList = new Vector();
        nameList.add(new Name("F","fr"));
        nameList.add(new Name("M","en"));
        nameList.add(new Name("S"));

        keyList = BusinessNameTable.selectBusinessKey(nameList,connection);

        nameList = new Vector();
        nameList.add(new Name("Steve"));

        keyList = BusinessNameTable.selectBusinessKey(nameList,keyList,connection);

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
