/*
 * jUDDI - An open source Java implementation of UDDI v2.0
 * http://juddi.org/
 *
 * Copyright (c) 2002, InflexionPoint and contributors
 * All rights reserved.
 */

package org.juddi.datastore.jdbc;

import org.apache.log4j.Logger;
import org.uddi4j.util.KeyedReference;

import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.PreparedStatement;
import java.util.Vector;

/**
 * @author Steve Viens (steve@inflexionpoint.com)
 * @version 0.6.2
 */
class FindRelatedBusinessQuery
{
  // private reference to the jUDDI logger
  private static Logger log = Logger.getLogger(FindRelatedBusinessQuery.class);

  static String selectSQL;
  static String selectWithKeyedRefSQL;

  static
  {
    StringBuffer sql = null;

    // build selectSQL
    sql = new StringBuffer(300);
    sql.append("SELECT FROM_KEY,TO_KEY,TMODEL_KEY,KEY_NAME,KEY_VALUE ");
    sql.append("FROM PUBLISHER_ASSERTION ");
    sql.append("WHERE (FROM_KEY=? OR TO_KEY=?) ");
    sql.append("AND FROM_CHECK='true' ");
    sql.append("AND TO_CHECK='true'");
    selectSQL = sql.toString();

    // build selectWithKeyedRefSQL
    sql = new StringBuffer(300);
    sql.append("SELECT FROM_KEY,TO_KEY,TMODEL_KEY,KEY_NAME,KEY_VALUE ");
    sql.append("FROM PUBLISHER_ASSERTION ");
    sql.append("WHERE (FROM_KEY=? OR TO_KEY=?) ");
    sql.append("AND TMODEL_KEY=? ");
    sql.append("AND KEY_NAME=? ");
    sql.append("AND KEY_VALUE=? ");
    sql.append("AND FROM_CHECK='true' ");
    sql.append("AND TO_CHECK='true'");
    selectWithKeyedRefSQL = sql.toString();
  }

  /**
   * Return a Vector of business keys that - together with the business key
   * parameter passed in - represent a valid (ie: status:complete) PublisherAssertion.
   * This is done by inspecting both the FROM_KEY and TO_KEY values returned from the
   * query and adding the businessKey that IS NOT equal to the businessKey passed in.
   *
   * @param businessKey The BusinessKey to find relations for
   * @param connection JDBC connection
   * @throws java.sql.SQLException
   */
  public static Vector select(String businessKey,Connection connection)
    throws java.sql.SQLException
  {
    Vector keysOut = new Vector();
    PreparedStatement statement = null;
    ResultSet resultSet = null;

    try
    {
      statement = connection.prepareStatement(selectSQL);
      statement.setString(1,businessKey.toString());
      statement.setString(2,businessKey.toString());

      log.info("select from PUBLISHER_ASSERTION table:\n\n\t" + selectSQL +
        "\n\t FROM_KEY=" + businessKey+
        "\n\t TO_KEY=" + businessKey + "\n");

      resultSet=statement.executeQuery();
      while (resultSet.next())
      {
        String fromKey = resultSet.getString("FROM_KEY");
        String toKey = resultSet.getString("TO_KEY");

        if (!fromKey.equalsIgnoreCase(businessKey))
          keysOut.addElement(fromKey);
        else if (!toKey.equalsIgnoreCase(businessKey))
          keysOut.addElement(toKey);
      }

      if (keysOut.size() > 0)
        log.info("select successful, at least one matching row was found");
      else
        log.info("select executed successfully but no matching rows were found");

      return keysOut;
    }
    finally
    {
      try {
        resultSet.close();
      }
      catch (Exception e)
      {
        log.warn("An Exception was encountered while attempting to close " +
          "the Find BusinessEntity ResultSet: "+e.getMessage(),e);
      }

      try {
        statement.close();
      }
      catch (Exception e)
      {
        log.warn("An Exception was encountered while attempting to close " +
          "the Find BusinessEntity Statement: "+e.getMessage(),e);
      }
    }
  }

  /**
   * Return a Vector of business keys that - together with the business key
   * parameter passed in - represent a valid (ie: status:complete) PublisherAssertion.
   * This is done by inspecting both the FROM_KEY and TO_KEY values returned from the
   * query and adding the businessKey that IS NOT equal to the businessKey passed in.
   *
   * @param businessKey The BusinessKey to find relations for
   * @param keyedRef A KeyedReference instance to using when searching
   * @param connection JDBC connection
   * @throws java.sql.SQLException
   */
  public static Vector selectWithKeyedRef(String businessKey,KeyedReference keyedRef,Connection connection)
    throws java.sql.SQLException
  {
    Vector keysOut = new Vector();
    PreparedStatement statement = null;
    ResultSet resultSet = null;

    try
    {
      statement = connection.prepareStatement(selectWithKeyedRefSQL);
      statement.setString(1,businessKey);
      statement.setString(2,businessKey);
      statement.setString(3,keyedRef.getTModelKey());
      statement.setString(4,keyedRef.getKeyName());
      statement.setString(5,keyedRef.getKeyValue());

      log.info("select from PUBLISHER_ASSERTION table:\n\n\t" + selectWithKeyedRefSQL +
        "\n\t FROM_KEY=" + businessKey +
        "\n\t TO_KEY=" + businessKey +
        "\n\t TMODEL_KEY=" + keyedRef.getTModelKey() +
        "\n\t KEY_NAME=" + keyedRef.getKeyName() +
        "\n\t KEY_VALUE=" + keyedRef.getKeyValue() + "\n");

      resultSet=statement.executeQuery();
      while (resultSet.next())
      {
        String fromKey = resultSet.getString("FROM_KEY");
        String toKey = resultSet.getString("TO_KEY");

        if (!fromKey.equalsIgnoreCase(businessKey))
          keysOut.addElement(fromKey);
        else if (!toKey.equalsIgnoreCase(businessKey))
          keysOut.addElement(toKey);
      }

      if (keysOut.size() > 0)
        log.info("select successful, at least one matching row was found");
      else
        log.info("select executed successfully but no matching rows were found");

      return keysOut;
    }
    finally
    {
      try {
        resultSet.close();
      }
      catch (Exception e)
      {
        log.warn("An Exception was encountered while attempting to close " +
          "the Find BusinessEntity ResultSet: "+e.getMessage(),e);
      }

      try {
        statement.close();
      }
      catch (Exception e)
      {
        log.warn("An Exception was encountered while attempting to close " +
          "the Find BusinessEntity Statement: "+e.getMessage(),e);
      }
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

    KeyedReference keyedRef = new KeyedReference();
    keyedRef.setTModelKey("");
    keyedRef.setKeyName("");
    keyedRef.setKeyValue("");

    if (connection != null)
    {
      try
      {
        // begin a new transaction
        txn.begin(connection);

        select("2a33d7d7-2b73-4de9-99cd-d4c51c186bce",connection);
        selectWithKeyedRef("2a33d7d7-2b73-4de9-99cd-d4c51c186bce",keyedRef,connection);

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
