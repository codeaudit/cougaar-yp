/*
 * jUDDI - An open source Java implementation of UDDI v2.0
 * http://juddi.org/
 *
 * Copyright (c) 2002, InflexionPoint and contributors
 * All rights reserved.
 */

package org.juddi.datastore.jdbc;

import org.uddi4j.response.AssertionStatusItem;
import org.uddi4j.response.CompletionStatus;
import org.uddi4j.datatype.assertion.PublisherAssertion;
import org.uddi4j.util.*;
import org.apache.log4j.Logger;

import java.sql.*;
import java.util.Vector;

/**
 * @author Steve Viens (steve@inflexionpoint.com)
 * @version 0.6.2
 */
class PublisherAssertionTable
{
  // private reference to the jUDDI logger
  private static Logger log = Logger.getLogger(PublisherAssertionTable.class);

  static String dropSQL = null;
  static String createSQL = null;
  static String insertSQL = null;
  static String selectSQL = null;
  static String deleteDeadAssertionsSQL = null;
  static String updateFromCheckSQL = null;
  static String updateToCheckSQL = null;
  static String updateFromCheckByFromKeySQL = null;
  static String updateToCheckByToKeySQL = null;
  static String selectAssertionsSQL = null;
  static String selectRelationships = null;

  static
  {
    // buffer used to build SQL statements
    StringBuffer sql = null;

    // build dropSQL
    dropSQL = "DROP TABLE PUBLISHER_ASSERTION";

    sql = new StringBuffer(150);
    sql.append("CREATE TABLE PUBLISHER_ASSERTION (");
    sql.append("FROM_KEY VARCHAR(41) NOT NULL,");
    sql.append("TO_KEY VARCHAR(41) NOT NULL,");
    sql.append("TMODEL_KEY VARCHAR(41) NOT NULL,");
    sql.append("KEY_NAME VARCHAR(255) NOT NULL,");
    sql.append("KEY_VALUE VARCHAR(255) NOT NULL,");
    sql.append("FROM_CHECK VARCHAR(5) NOT NULL,");
    sql.append("TO_CHECK VARCHAR(5) NOT NULL,");
    sql.append("FOREIGN KEY (FROM_KEY) REFERENCES BUSINESS_ENTITY (BUSINESS_KEY),");
    sql.append("FOREIGN KEY (TO_KEY) REFERENCES BUSINESS_ENTITY (BUSINESS_KEY))");
    createSQL = sql.toString();

    // build insertSQL
    sql = new StringBuffer(150);
    sql.append("INSERT INTO PUBLISHER_ASSERTION (");
    sql.append("FROM_KEY,");
    sql.append("TO_KEY,");
    sql.append("TMODEL_KEY,");
    sql.append("KEY_NAME,");
    sql.append("KEY_VALUE,");
    sql.append("FROM_CHECK,");
    sql.append("TO_CHECK) ");
    sql.append("VALUES (?,?,?,?,?,?,?)");
    insertSQL = sql.toString();

    // build selectSQL
    sql = new StringBuffer(200);
    sql.append("SELECT ");
    sql.append("FROM_KEY,");
    sql.append("TO_KEY,");
    sql.append("TMODEL_KEY,");
    sql.append("KEY_NAME,");
    sql.append("KEY_VALUE ");
    sql.append("FROM PUBLISHER_ASSERTION ");
    sql.append("WHERE FROM_KEY=? ");
    sql.append("AND TO_KEY=? ");
    sql.append("AND TMODEL_KEY=? ");
    sql.append("AND KEY_NAME=? ");
    sql.append("AND KEY_VALUE=?");
    selectSQL = sql.toString();

    // build deleteDeadAssertionsSQL
    sql = new StringBuffer(200);
    sql.append("DELETE FROM PUBLISHER_ASSERTION ");
    sql.append("WHERE FROM_CHECK='false' ");
    sql.append("AND TO_CHECK='false'");
    deleteDeadAssertionsSQL = sql.toString();

    // build updateFromCheckSQL
    sql = new StringBuffer(200);
    sql.append("UPDATE PUBLISHER_ASSERTION ");
    sql.append("SET FROM_CHECK=? ");
    sql.append("WHERE FROM_KEY=? ");
    sql.append("AND TO_KEY=? ");
    sql.append("AND TMODEL_KEY=? ");
    sql.append("AND KEY_NAME=? ");
    sql.append("AND KEY_VALUE=?");
    updateFromCheckSQL = sql.toString();

    // build updateToCheckSQL
    sql = new StringBuffer(200);
    sql.append("UPDATE PUBLISHER_ASSERTION ");
    sql.append("SET TO_CHECK=? ");
    sql.append("WHERE FROM_KEY=? ");
    sql.append("AND TO_KEY=? ");
    sql.append("AND TMODEL_KEY=? ");
    sql.append("AND KEY_NAME=? ");
    sql.append("AND KEY_VALUE=?");
    updateToCheckSQL = sql.toString();

    // build updateFromCheckByFromKeySQL
    sql = new StringBuffer(200);
    sql.append("UPDATE PUBLISHER_ASSERTION ");
    sql.append("SET FROM_CHECK=? ");
    sql.append("WHERE FROM_KEY IN ");
    updateFromCheckByFromKeySQL = sql.toString();

    // build updateFromCheckByFromKeySQL
    sql = new StringBuffer(200);
    sql.append("UPDATE PUBLISHER_ASSERTION ");
    sql.append("SET TO_CHECK=? ");
    sql.append("WHERE TO_KEY IN ");
    updateFromCheckByFromKeySQL = sql.toString();

    // build selectAssertionsSQL
    sql = new StringBuffer(200);
    sql.append("SELECT ");
    sql.append("FROM_KEY,");
    sql.append("TO_KEY,");
    sql.append("TMODEL_KEY,");
    sql.append("KEY_NAME,");
    sql.append("KEY_VALUE,");
    sql.append("FROM_CHECK,");
    sql.append("TO_CHECK ");
    sql.append("FROM PUBLISHER_ASSERTION ");
    selectAssertionsSQL = sql.toString();

    // build selectRelationships
    sql = new StringBuffer(200);
    sql.append("SELECT TMODEL_KEY,KEY_NAME,KEY_VALUE ");
	sql.append("FROM PUBLISHER_ASSERTION ");
	sql.append("WHERE ((FROM_KEY = ? AND TO_KEY = ?) OR (FROM_KEY = ? AND TO_KEY = ?)) ");
	sql.append("AND FROM_CHECK = 'true' ");
    sql.append("AND TO_CHECK = 'true') ");
    selectRelationships = sql.toString();
  }

  /**
   * Drop the PUBLISHER_ASSERTION table.
   */
  public static void drop(Connection connection)
  {
    System.out.print("DROP TABLE PUBLISHER_ASSERTION: ");
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
      try { statement.close(); } catch (Exception e) { /* ignored */ }
    }
  }

  /**
   * Create the PUBLISHER_ASSERTION table.
   */
  public static void create(Connection connection)
  {
    System.out.print("CREATE TABLE PUBLISHER_ASSERTION: ");
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
      try { statement.close(); } catch (Exception e) { /* ignored */ }
    }
  }

  /**
   * Insert new row into the PUBLISHER_ASSERTION table.
   *
   * @param assertion Publisher Assertion object holding values to be inserted
   * @param fromCheck boolean true if the FROM_KEY is owned by the individual 'adding' this assertion (otherwise false).
   * @param toCheck boolean true if the TO_KEY is owned by the individual 'adding' this assertion (otherwise false).
   * @param connection JDBC connection
   * @throws java.sql.SQLException
   */
  public static void insert(PublisherAssertion assertion,boolean fromCheck,boolean toCheck,Connection connection)
    throws java.sql.SQLException
  {
    PreparedStatement statement = null;

    try
    {
      // prep insert values
      String tModelKey = null;
      String keyedRefName = null;
      String keyedRefValue = null;

      if (assertion.getKeyedReference() != null)
      {
        tModelKey = assertion.getKeyedReference().getTModelKey();
        keyedRefName = assertion.getKeyedReference().getKeyName();
        keyedRefValue = assertion.getKeyedReference().getKeyValue();
      }

      statement = connection.prepareStatement(insertSQL);
      statement.setString(1,assertion.getFromKeyString());
      statement.setString(2,assertion.getToKeyString());
      statement.setString(3,tModelKey);
      statement.setString(4,keyedRefName);
      statement.setString(5,keyedRefValue);
      statement.setString(6,String.valueOf(fromCheck));
      statement.setString(7,String.valueOf(toCheck));

      log.info("insert into PUBLISHER_ASSERTION table:\n\n\t" + insertSQL +
        "\n\t FROM_KEY=" + assertion.getFromKeyString() +
        "\n\t TO_KEY=" + assertion.getToKeyString() +
        "\n\t TMODEL_KEY=" + tModelKey +
        "\n\t KEY_NAME=" + keyedRefName +
        "\n\t KEY_VALUE=" + keyedRefValue +
        "\n\t FROM_CHECK=" + fromCheck +
        "\n\t TO_CHECK=" + toCheck + "\n");

      // insert
      int returnCode = statement.executeUpdate();

      log.info("insert was successful, return code=" + returnCode);
    }
    catch(java.sql.SQLException sqlex)
    {
      log.error(sqlex.getMessage());
      throw sqlex;
    }
    finally
    {
      try { statement.close(); } catch (Exception e) { /* ignored */ }
    }
  }

  /**
   * Select one row from the PUBLISHER_ASSERTION table.
   *
   * @param assertionIn
   * @param connection JDBC connection
   * @throws java.sql.SQLException
   */
  public static PublisherAssertion select(PublisherAssertion assertionIn,Connection connection)
    throws java.sql.SQLException
  {
    PublisherAssertion assertionOut = null;
    PreparedStatement statement = null;
    ResultSet resultSet = null;

    try
    {
      KeyedReference keyedRefIn = assertionIn.getKeyedReference();

      statement = connection.prepareStatement(selectSQL);
      statement.setString(1,assertionIn.getFromKeyString());
      statement.setString(2,assertionIn.getToKeyString());
      statement.setString(3,keyedRefIn.getTModelKey());
      statement.setString(4,keyedRefIn.getKeyName());
      statement.setString(5,keyedRefIn.getKeyValue());

      log.info("select from PUBLISHER_ASSERTION table:\n\n\t" + selectSQL +
        "\n\t FROM_KEY=" + assertionIn.getFromKeyString() +
        "\n\t TO_KEY=" + assertionIn.getToKeyString() +
        "\n\t TMODEL_KEY=" + keyedRefIn.getTModelKey() +
        "\n\t KEY_NAME=" + keyedRefIn.getKeyName() +
        "\n\t KEY_VALUE=" + keyedRefIn.getKeyValue() + "\n");

      resultSet = statement.executeQuery();
      if (resultSet.next())
      {
        KeyedReference keyedRefOut = new KeyedReference();
        keyedRefOut.setKeyName(resultSet.getString("KEY_NAME"));
        keyedRefOut.setKeyValue(resultSet.getString("KEY_VALUE"));
        keyedRefOut.setTModelKey(resultSet.getString("TMODEL_KEY"));

        assertionOut = new PublisherAssertion();
        assertionOut.setFromKeyString(resultSet.getString("FROM_KEY"));
        assertionOut.setToKeyString(resultSet.getString("TO_KEY"));
        assertionOut.setKeyedReference(keyedRefOut);
      }

      if (assertionOut != null)
        log.info("select successful, at least one row was found");
      else
        log.info("select executed successfully but no rows were found.");

      return assertionOut;
    }
    catch(java.sql.SQLException sqlex)
    {
      log.error(sqlex.getMessage());
      throw sqlex;
    }
    finally
    {
      try { resultSet.close(); } catch (Exception e) { /* ignored */ }
      try { statement.close(); } catch (Exception e) { /* ignored */ }
    }
  }

  /**
   * Delete row from the PUBLISHER_ASSERTION table.
   *
   * @throws java.sql.SQLException
   */
  public static void deleteDeadAssertions(Connection connection)
    throws java.sql.SQLException
  {
    PreparedStatement statement = null;

    try
    {
      // prepare the delete
      statement = connection.prepareStatement(deleteDeadAssertionsSQL);

      log.info("delete from PUBLISHER_ASSERTION table:\n\n\t" + deleteDeadAssertionsSQL + "\n");

      // execute the delete
      int returnCode = statement.executeUpdate();

      log.info("delete was successful, rows deleted=" + returnCode);
    }
    catch(java.sql.SQLException sqlex)
    {
      log.error(sqlex.getMessage());
      throw sqlex;
    }
    finally
    {
      try { statement.close(); } catch (Exception e) { /* ignored */ }
    }
  }

  /**
   * Update the FROM_CHECK column in the PUBLISHER_ASSERTION table for a
   * particular PublisherAssertion.<p>
   *
   * @param  assertion The PublisherAssertion to update BusinessKey
   * @param  fromCheck The value to set the FROM_CHECK column to.
   * @param  connection JDBC connection
   * @throws java.sql.SQLException
   */
  public static void updateFromCheck(PublisherAssertion assertion,boolean fromCheck,Connection connection)
    throws java.sql.SQLException
  {
    KeyedReference keyedRef = assertion.getKeyedReference();
    PreparedStatement statement = null;
    ResultSet resultSet = null;

    try
    {
      log.info("update PUBLISHER_ASSERTION table:\n\n\t" + updateFromCheckSQL +
       "\n\t FROM_CHECK=" + String.valueOf(fromCheck) +
       "\n\t FROM_KEY=" + assertion.getFromKeyString() +
       "\n\t TO_KEY=" + assertion.getToKeyString() +
       "\n\t TMODEL_KEY=" + keyedRef.getTModelKey() +
       "\n\t KEY_NAME=" + keyedRef.getKeyName() +
       "\n\t KEY_VALUE=" + keyedRef.getKeyValue() + "\n");

      // create a statement to query with
      statement = connection.prepareStatement(updateFromCheckSQL);
      statement.setString(1,String.valueOf(fromCheck));
      statement.setString(2,assertion.getFromKeyString());
      statement.setString(3,assertion.getToKeyString());
      statement.setString(4,keyedRef.getTModelKey());
      statement.setString(5,keyedRef.getKeyName());
      statement.setString(6,keyedRef.getKeyValue());

      // execute the update
      int returnCode = statement.executeUpdate();

      log.info("update was successful, rows updated=" + returnCode);
    }
    catch(java.sql.SQLException sqlex)
    {
      log.error(sqlex.getMessage());
      throw sqlex;
    }
    finally
    {
      try { resultSet.close(); } catch (Exception e) { /* ignored */ }
      try { statement.close(); } catch (Exception e) { /* ignored */ }
    }
  }

  /**
   * Update the TO_CHECK column in the PUBLISHER_ASSERTION table for a
   * particular PublisherAssertion.<p>
   *
   * @param assertion The PublisherAssertion to update BusinessKey
   * @param toCheck The value to set the TO_CHECK column to.
   * @param connection JDBC connection
   * @throws java.sql.SQLException
   */
  public static void updateToCheck(PublisherAssertion assertion,boolean toCheck,Connection connection)
    throws java.sql.SQLException
  {
    KeyedReference keyedRef = assertion.getKeyedReference();
    PreparedStatement statement = null;
    ResultSet resultSet = null;

    try
    {
      log.info("update PUBLISHER_ASSERTION table:\n\n\t" + updateToCheckSQL +
       "\n\t TO_CHECK=" + String.valueOf(toCheck) +
       "\n\t FROM_KEY=" + assertion.getFromKeyString() +
       "\n\t TO_KEY=" + assertion.getToKeyString() +
       "\n\t TMODEL_KEY=" + keyedRef.getTModelKey() +
       "\n\t KEY_NAME=" + keyedRef.getKeyName() +
       "\n\t KEY_VALUE=" + keyedRef.getKeyValue() + "\n");

      // create a statement to query with
      statement = connection.prepareStatement(updateToCheckSQL);
      statement.setString(1,String.valueOf(toCheck));
      statement.setString(2,assertion.getFromKeyString());
      statement.setString(3,assertion.getToKeyString());
      statement.setString(4,keyedRef.getTModelKey());
      statement.setString(5,keyedRef.getKeyName());
      statement.setString(6,keyedRef.getKeyValue());


      // execute the update
      int returnCode = statement.executeUpdate();

      log.info("update was successful, rows updated=" + returnCode);
    }
    catch(java.sql.SQLException sqlex)
    {
      log.error(sqlex.getMessage());
      throw sqlex;
    }
    finally
    {
      try { resultSet.close(); } catch (Exception e) { /* ignored */ }
      try { statement.close(); } catch (Exception e) { /* ignored */ }
    }
  }

  /**
   * Update the FROM_CHECK column for all rows from in the PUBLISHER_ASSERTION
   * table whose FROM_KEY is in the Vector of BusinessKeys passed in.<p>
   *
   * @param  fromKeysIn A Vector of BusinessKeys to update
   * @param  fromCheck The value to set the FROM_CHECK column to
   * @param  connection JDBC connection
   * @throws java.sql.SQLException
   */
  public static void updateFromCheckByFromKey(Vector fromKeysIn,boolean fromCheck,Connection connection)
    throws java.sql.SQLException
  {
    StringBuffer sql = new StringBuffer();
    sql.append(updateFromCheckByFromKeySQL);
    sql.append("WHERE FROM_KEY IN ");
    appendIn(sql,fromKeysIn);

    PreparedStatement statement = null;
    ResultSet resultSet = null;

    try
    {
      // create a statement to query with
      statement = connection.prepareStatement(sql.toString());
      statement.setString(1,String.valueOf(fromCheck));

      log.info("update PUBLISHER_ASSERTION table:\n\n\t" + sql.toString() + "\n");

      // execute the update
      int returnCode = statement.executeUpdate();

      log.info("update was successful, rows updated=" + returnCode);
    }
    catch(java.sql.SQLException sqlex)
    {
      log.error(sqlex.getMessage());
      throw sqlex;
    }
    finally
    {
      try { resultSet.close(); } catch (Exception e) { /* ignored */ }
      try { statement.close(); } catch (Exception e) { /* ignored */ }
    }
  }

  /**
   * Update the TO_CHECK column for all rows from in the PUBLISHER_ASSERTION
   * table whose TO_KEY is in the Vector of BusinessKeys passed in.<p>
   *
   * @param toKeysIn A Vector of BusinessKeys to update
   * @param toCheck The value to set the TO_KEY column to
   * @param connection JDBC connection
   * @throws java.sql.SQLException
   */
  public static void updateToCheckByToKey(Vector toKeysIn,boolean toCheck,Connection connection)
    throws java.sql.SQLException
  {
    StringBuffer sql = new StringBuffer();
    sql.append(updateFromCheckByFromKeySQL);
    sql.append("WHERE TO_KEY IN ");
    appendIn(sql,toKeysIn);

    PreparedStatement statement = null;
    ResultSet resultSet = null;

    try
    {
      // create a statement to query with
      statement = connection.prepareStatement(sql.toString());
      statement.setString(1,String.valueOf(toCheck));

      log.info("update PUBLISHER_ASSERTION table:\n\n\t" + sql.toString() + "\n");

      // execute the update
      int returnCode = statement.executeUpdate();

      log.info("update was successful, rows updated=" + returnCode);
    }
    catch(java.sql.SQLException sqlex)
    {
      log.error(sqlex.getMessage());
      throw sqlex;
    }
    finally
    {
      try { resultSet.close(); } catch (Exception e) { /* ignored */ }
      try { statement.close(); } catch (Exception e) { /* ignored */ }
    }
  }

  /**
   * Select any rows from the PUBLISHER_ASSERTION table where the FROM_KEY
   * or TO_KEY column value is found in the Vector of BusinessKeys passed in
   * and return the results as a Vector of assertionStatusItem instances.
   *
   * The assertionStatusItems returned represent PublisherAssertions in
   * which the fromKey and toKey are both are under the control of a
   * particular Publisher.
   *
   * NOTE: Each AssertionStatusItem returned from this method will have a
   *       completion stauts of 'status:complete' because only assertions
   *       in which both business entities are managed (was published) by
   *       same publisher.
   *
   * @param keysIn Vector business keys to look for in the FROM_KEY and TO_KEY column.
   * @param connection JDBC connection
   * @throws java.sql.SQLException
   */
  public static Vector selectBothKeysOwnedAssertion(Vector keysIn,Connection connection)
    throws java.sql.SQLException
  {
    if ((keysIn == null) || (keysIn.size() == 0))
      return null;

    StringBuffer sql = new StringBuffer();
    sql.append(selectAssertionsSQL);
    sql.append("WHERE FROM_KEY IN ");
    appendIn(sql,keysIn);
    sql.append("AND TO_KEY IN ");
    appendIn(sql,keysIn);

    Vector itemList = new Vector();
    PreparedStatement statement = null;
    ResultSet resultSet = null;

    try
    {
      statement = connection.prepareStatement(sql.toString());

      log.info("select from PUBLISHER_ASSERTION table:\n\n\t" + selectSQL + "\n");

      resultSet = statement.executeQuery();
      while (resultSet.next())
      {
        AssertionStatusItem item = new AssertionStatusItem();
        item.setFromKeyString(resultSet.getString("FROM_KEY"));
        item.setToKeyString(resultSet.getString("TO_KEY"));

        // construct and set the KeyedReference instance
        KeyedReference keyedRef = new KeyedReference();
        keyedRef.setKeyName(resultSet.getString("KEY_NAME"));
        keyedRef.setKeyValue(resultSet.getString("KEY_VALUE"));
        keyedRef.setTModelKey(resultSet.getString("TMODEL_KEY"));
        item.setKeyedReference(keyedRef);

        // determine & set the 'completionStatus' (always 'status:complete' here)
        item.setCompletionStatus(new CompletionStatus(CompletionStatus.COMPLETE));

        // add the assertionStatusItem
        itemList.addElement(item);
      }

      if (itemList.size() > 0)
        log.info("select successful, at least one row was found");
      else
        log.info("select executed successfully but no rows were found.");

      return itemList;
    }
    catch(java.sql.SQLException sqlex)
    {
      log.error(sqlex.getMessage());
      throw sqlex;
    }
    finally
    {
      try { resultSet.close(); } catch (Exception e) { /* ignored */ }
      try { statement.close(); } catch (Exception e) { /* ignored */ }
    }
  }

  /**
   * Select any rows from the PUBLISHER_ASSERTION table where the FROM_KEY column
   * DOES CONTAIN one of the business keys found in the Vector of keys passed in
   * and the TO_KEY column DOES NOT CONTAIN one of the business keys from the same
   * Vector of keys. Return the results as a Vector of assertionStatusItem instances.
   *
   * The assertionStatusItems returned represent PublisherAssertions in
   * which ONLY the "fromKey" is under the control of a particular Publisher.

   * @param keysIn Vector business keys to look for in the FROM_KEY and TO_KEY column.
   * @param connection JDBC connection
   * @throws java.sql.SQLException
   */
  public static Vector selectFromKeyOwnedAssertion(Vector keysIn,Connection connection)
    throws java.sql.SQLException
  {
    if ((keysIn == null) || (keysIn.size() == 0))
      return null;

    StringBuffer sql = new StringBuffer();
    sql.append(selectAssertionsSQL);
    sql.append("WHERE FROM_KEY IN ");
    appendIn(sql,keysIn);
    sql.append("AND TO_KEY NOT IN ");
    appendIn(sql,keysIn);

    Vector itemList = new Vector();
    PreparedStatement statement = null;
    ResultSet resultSet = null;

    try
    {
      statement = connection.prepareStatement(sql.toString());

      log.info("select from PUBLISHER_ASSERTION table:\n\n\t" + selectSQL + "\n");

      resultSet = statement.executeQuery();
      while (resultSet.next())
      {
        AssertionStatusItem item = new AssertionStatusItem();
        item.setFromKeyString(resultSet.getString("FROM_KEY"));
        item.setToKeyString(resultSet.getString("TO_KEY"));

        // construct and set the KeyedReference instance
        KeyedReference keyedRef = new KeyedReference();
        keyedRef.setKeyName(resultSet.getString("KEY_NAME"));
        keyedRef.setKeyValue(resultSet.getString("KEY_VALUE"));
        keyedRef.setTModelKey(resultSet.getString("TMODEL_KEY"));
        item.setKeyedReference(keyedRef);

        // determine and set the assertions 'completionStatus'
        CompletionStatus status = null;
        boolean fromCheck = new Boolean(resultSet.getString("FROM_CHECK")).booleanValue();
        boolean toCheck = new Boolean(resultSet.getString("TO_CHECK")).booleanValue();
        if ((fromCheck) && (toCheck))
          status = new CompletionStatus(CompletionStatus.COMPLETE);
        else if ((fromCheck) && (!toCheck))
          status = new CompletionStatus(CompletionStatus.TOKEY_INCOMPLETE);
        else if ((!fromCheck) && (toCheck))
          status = new CompletionStatus(CompletionStatus.FROMKEY_INCOMPLETE);
        item.setCompletionStatus(status);

        // add the assertionStatusItem
        itemList.addElement(item);
      }

      if (itemList.size() > 0)
        log.info("select successful, at least one row was found");
      else
        log.info("select executed successfully but no rows were found.");

      return itemList;
    }
    catch(java.sql.SQLException sqlex)
    {
      log.error(sqlex.getMessage());
      throw sqlex;
    }
    finally
    {
      try { resultSet.close(); } catch (Exception e) { /* ignored */ }
      try { statement.close(); } catch (Exception e) { /* ignored */ }
    }
  }

  /**
   * Select any rows from the PUBLISHER_ASSERTION table where the FROM_KEY column
   * DOES NOT CONTAIN one of the business keys found in the Vector of keys passed
   * in and the TO_KEY column DOES CONTAIN one of the business keys from the same
   * Vector of keys. Return the results as a Vector of assertionStatusItem instances.
   *
   * The assertionStatusItems returned represent PublisherAssertions in
   * which ONLY the "toKey" is under the control of a particular Publisher.
   *
   * @param keysIn Vector business keys to look for in the FROM_KEY and TO_KEY column.
   * @param connection JDBC connection
   * @throws java.sql.SQLException
   */
  public static Vector selectToKeyOwnedAssertion(Vector keysIn,Connection connection)
    throws java.sql.SQLException
  {
    if ((keysIn == null) || (keysIn.size() == 0))
      return null;

    StringBuffer sql = new StringBuffer();
    sql.append(selectAssertionsSQL);
    sql.append("WHERE FROM_KEY NOT IN ");
    appendIn(sql,keysIn);
    sql.append("AND TO_KEY IN ");
    appendIn(sql,keysIn);

    Vector itemList = new Vector();
    PreparedStatement statement = null;
    ResultSet resultSet = null;

    try
    {
      statement = connection.prepareStatement(sql.toString());

      log.info("select from PUBLISHER_ASSERTION table:\n\n\t" + selectSQL + "\n");

      resultSet = statement.executeQuery();
      while (resultSet.next())
      {
        AssertionStatusItem item = new AssertionStatusItem();
        item.setFromKeyString(resultSet.getString("FROM_KEY"));
        item.setToKeyString(resultSet.getString("TO_KEY"));

        // construct and set the KeyedReference instance
        KeyedReference keyedRef = new KeyedReference();
        keyedRef.setKeyName(resultSet.getString("KEY_NAME"));
        keyedRef.setKeyValue(resultSet.getString("KEY_VALUE"));
        keyedRef.setTModelKey(resultSet.getString("TMODEL_KEY"));
        item.setKeyedReference(keyedRef);

        // determine and set the assertions 'completionStatus'
        CompletionStatus status = null;
        boolean fromCheck = new Boolean(resultSet.getString("FROM_CHECK")).booleanValue();
        boolean toCheck = new Boolean(resultSet.getString("TO_CHECK")).booleanValue();
        if ((fromCheck) && (toCheck))
          status = new CompletionStatus(CompletionStatus.COMPLETE);
        else if ((fromCheck) && (!toCheck))
          status = new CompletionStatus(CompletionStatus.TOKEY_INCOMPLETE);
        else if ((!fromCheck) && (toCheck))
          status = new CompletionStatus(CompletionStatus.FROMKEY_INCOMPLETE);
        item.setCompletionStatus(status);

        // add the assertionStatusItem
        itemList.addElement(item);
      }

      if (itemList.size() > 0)
        log.info("select successful, at least one row was found");
      else
        log.info("select executed successfully but no rows were found.");

      return itemList;
    }
    catch(java.sql.SQLException sqlex)
    {
      log.error(sqlex.getMessage());
      throw sqlex;
    }
    finally
    {
      try { resultSet.close(); } catch (Exception e) { /* ignored */ }
      try { statement.close(); } catch (Exception e) { /* ignored */ }
    }
  }

  /**
   * Select any rows from the PUBLISHER_ASSERTION table where the FROM_KEY column
   * CONTAINS one of the business keys found in the Vector of keys passed in OR
   * the TO_KEY column CONTAINS one of the business keys from the same Vector
   * f keys. Return the results as a Vector of PublisherAssertion instances.
   *
   * @param keysIn Vector business keys to look for in the FROM_KEY and TO_KEY column.
   * @param connection JDBC connection
   * @throws java.sql.SQLException
   */
  public static Vector selectAssertions(Vector keysIn,Connection connection)
    throws java.sql.SQLException
  {
    if ((keysIn == null) || (keysIn.size() == 0))
      return null;

    StringBuffer sql = new StringBuffer();
    sql.append(selectAssertionsSQL);
    sql.append("WHERE (FROM_KEY IN ");
    appendIn(sql,keysIn);
    sql.append("AND FROM_CHECK = 'true') ");
    sql.append("OR (TO_KEY IN ");
    appendIn(sql,keysIn);
    sql.append("AND TO_CHECK = 'true')");

    Vector assertionList = new Vector();
    PreparedStatement statement = null;
    ResultSet resultSet = null;

    try
    {
      statement = connection.prepareStatement(sql.toString());

      log.info("select from PUBLISHER_ASSERTION table:\n\n\t" + selectSQL + "\n");

      resultSet = statement.executeQuery();
      while (resultSet.next())
      {
        PublisherAssertion assertion = new PublisherAssertion();
        assertion.setFromKeyString(resultSet.getString("FROM_KEY"));
        assertion.setToKeyString(resultSet.getString("TO_KEY"));

        // construct and set the KeyedReference instance
        KeyedReference keyedRef = new KeyedReference();
        keyedRef.setKeyName(resultSet.getString("KEY_NAME"));
        keyedRef.setKeyValue(resultSet.getString("KEY_VALUE"));
        keyedRef.setTModelKey(resultSet.getString("TMODEL_KEY"));
        assertion.setKeyedReference(keyedRef);

        // add the assertionStatusItem
        assertionList.addElement(assertion);
      }

      if (assertionList.size() > 0)
        log.info("select successful, at least one row was found");
      else
        log.info("select executed successfully but no rows were found.");

      return assertionList;
    }
    catch(java.sql.SQLException sqlex)
    {
      log.error(sqlex.getMessage());
      throw sqlex;
    }
    finally
    {
      try { resultSet.close(); } catch (Exception e) { /* ignored */ }
      try { statement.close(); } catch (Exception e) { /* ignored */ }
    }
  }

  /**
   * Retrieve the TMODEL_KEY, KEY_NAME and KEY_VALUE from all assertions
   * where the FROM_KEY = businessKey and the TO_KEY = relatedBusinessKey
   * parameters or the FROM_KEY = relatedBusinessKey and the TO_KEY =
   * businessKey.
   *
   * @param businessKey The BusinessKey we're searching for relationships to.
   * @param relatedKey The BusinessKey of the related BusinessEntity.
   * @param connection JDBC connection
   * @throws java.sql.SQLException
   */
  public static Vector selectRelatedBusinesses(String businessKey,String relatedKey,Connection connection)
    throws java.sql.SQLException
  {
    Vector refList = new Vector();
    PreparedStatement statement = null;
    ResultSet resultSet = null;

    try
    {
      statement = connection.prepareStatement(selectRelationships);
      statement.setString(1,businessKey);
      statement.setString(2,relatedKey);
      statement.setString(3,relatedKey);
      statement.setString(4,businessKey);

      log.info("select from PUBLISHER_ASSERTION table:\n\n\t" + selectRelationships +
        "\n\t BUSINESS_KEY=" + businessKey.toString() +
        "\n\t RELATED_BUSINESS_KEY=" + relatedKey.toString() + "\n");

      resultSet = statement.executeQuery();
      if (resultSet.next())
      {
        KeyedReference keyedRef = new KeyedReference();
        keyedRef.setKeyName(resultSet.getString("KEY_NAME"));
        keyedRef.setKeyValue(resultSet.getString("KEY_VALUE"));
        keyedRef.setTModelKey(resultSet.getString("TMODEL_KEY"));

        // add the KeyedRef to the Vector
        refList.addElement(keyedRef);
      }

      if (refList.size() > 0)
        log.info("select successful, at least one row was found");
      else
        log.info("select executed successfully but no rows were found.");

      return refList;
    }
    catch(java.sql.SQLException sqlex)
    {
      log.error(sqlex.getMessage());
      throw sqlex;
    }
    finally
    {
      try { resultSet.close(); } catch (Exception e) { /* ignored */ }
      try { statement.close(); } catch (Exception e) { /* ignored */ }
    }
  }

  /**
   * Utility method used to construct SQL "IN" statements such as
   * the following SQL example:
   *
   *   SELECT * FROM TABLE WHERE MONTH IN ('jan','feb','mar')
   *
   * @param sql StringBuffer to append the final results to
   * @param keysIn Vector of Strings used to construct the "IN" clause
   */
  private static void appendIn(StringBuffer sql,Vector keysIn)
  {
    if (keysIn == null)
      return;

    sql.append("(");

    int keyCount = keysIn.size();
    for (int i=0; i<keyCount; i++)
    {
      String key = (String)keysIn.elementAt(i);
      sql.append("'").append(key).append("'");

      if ((i+1) < keyCount)
        sql.append(",");
    }

    sql.append(") ");
  }

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
//        String fromKey = UUID.nextID();
//        String toKey = UUID.nextID();
//
//        KeyedReference keyRef = new KeyedReference();
//        keyRef.setTModelKey(UUID.nextID());
//        keyRef.setKeyName("whoaaaa BABY!");
//        keyRef.setKeyValue("en");
//
//        PublisherAssertion assert1 = new PublisherAssertion(fromKey,toKey,keyRef);
//        PublisherAssertion assert2 = new PublisherAssertion(UUID.nextID(),toKey,keyRef);
//        PublisherAssertion assert3 = new PublisherAssertion(fromKey,UUID.nextID(),keyRef);
//        PublisherAssertion assert4 = new PublisherAssertion(UUID.nextID(),UUID.nextID(),keyRef);
//        PublisherAssertion assert5 = new PublisherAssertion(UUID.nextID(),UUID.nextID(),keyRef);
//
//        Vector assertList = null;
//
//        String publisherID = "sviens";
//
//        // begin a new transaction
//        txn.begin(connection);
//
//        // insert a new PublisherAssertion
//        PublisherAssertionTable.insert(assert1,publisherID,connection);
//
//        // insert another new PublisherAssertion
//        PublisherAssertionTable.insert(assert2,publisherID,connection);
//
//        // insert one more new PublisherAssertion
//        PublisherAssertionTable.insert(assert3,publisherID,connection);
//
//        // insert one more new PublisherAssertion
//        PublisherAssertionTable.insert(assert4,publisherID,connection);
//
//        // insert one more new PublisherAssertion
//        PublisherAssertionTable.insert(assert5,publisherID,connection);
//
//        // select a PublisherAssertion using both from & to BusinessKeys
//        PublisherAssertion assertion = PublisherAssertionTable.select(fromKey,toKey,connection);
//
//        // delete that PublisherAssertion
//        PublisherAssertionTable.delete(publisherID,fromKey,toKey,connection);
//
//        // re-select that PublisherAssertion
//        assertion = PublisherAssertionTable.select(fromKey,toKey,connection);
//
//        // select a Collection of PublisherAssertion objects using fromKey
//        assertList = PublisherAssertionTable.selectByFromKey(fromKey,connection);
//
//        // re-select that Collection of PublisherAssertion objects
//        assertList = PublisherAssertionTable.selectByFromKey(fromKey,connection);
//
//        // select a Collection of PublisherAssertion objects using toKey
//        assertList = PublisherAssertionTable.selectByToKey(toKey,connection);
//
//        // re-select that Collection of PublisherAssertion objects
//        assertList = PublisherAssertionTable.selectByToKey(toKey,connection);
//
//        // commit the transaction
//        txn.commit();
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
