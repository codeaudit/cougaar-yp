/*
 * jUDDI - An open source Java implementation of UDDI v2.0
 * http://juddi.org/
 *
 * Copyright (c) 2002, InflexionPoint and contributors
 * All rights reserved.
 */

package org.juddi.datastore.jdbc;

import org.juddi.util.SearchQualifiers;

import org.apache.log4j.Logger;
import org.uddi4j.util.TModelBag;
import org.uddi4j.util.TModelKey;

import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.Statement;
import java.util.Vector;

/**
 * @author Steve Viens (steve@inflexionpoint.com)
 * @version 0.6.2
 */
class FindBusinessByTModelKeyQuery
{
  // private reference to the jUDDI logger
  private static Logger log = Logger.getLogger(FindBusinessByTModelKeyQuery.class);

  static String selectSQL;
  static
  {
    // build selectSQL
    StringBuffer sql = new StringBuffer(200);
    sql.append("SELECT B.BUSINESS_KEY,B.LAST_UPDATE ");
    sql.append("FROM BUSINESS_ENTITY B,BUSINESS_SERVICE S,BINDING_TEMPLATE T,TMODEL_INSTANCE_INFO I ");
    selectSQL = sql.toString();
  }

  /**
   * Select ...
   *
   * @param tModelBag
   * @param keysIn
   * @param qualifiers
   * @param connection JDBC connection
   * @throws java.sql.SQLException
   */
  public static Vector select(TModelBag tModelBag,Vector keysIn,SearchQualifiers qualifiers,Connection connection)
    throws java.sql.SQLException
  {
    // if there is a keysIn vector but it doesn't contain
    // any keys then the previous query has exhausted
    // all possibilities of a match so skip this call.
    if ((keysIn != null) && (keysIn.size() == 0))
      return keysIn;

    Vector keysOut = new Vector();
    Statement statement = null;
    ResultSet resultSet = null;

    // construct the SQL statement
    StringBuffer sql = new StringBuffer(selectSQL);
    appendWhere(sql,tModelBag,qualifiers);
    appendIn(sql,keysIn);
    appendOrderBy(sql,qualifiers);

    try
    {
      statement = connection.createStatement();

      log.info("select from BUSINESS_ENTITY, BUSINESS_SERVICE, BINDING_TEMPLATE & TMODEL_INSTANCE_INFO tables:\n\n\t" + sql.toString() + "\n");

      resultSet = statement.executeQuery(sql.toString());
      while (resultSet.next())
      {
        keysOut.addElement(resultSet.getString("BUSINESS_KEY"));
        //System.out.println(resultSet.getString("BUSINESS_KEY"));
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
   *
   */
  private static void appendWhere(StringBuffer sql,TModelBag tModelBag,SearchQualifiers qualifiers)
  {
    sql.append("WHERE I.BINDING_KEY = T.BINDING_KEY ");
    sql.append("AND T.SERVICE_KEY = S.SERVICE_KEY ");
    sql.append("AND S.BUSINESS_KEY = B.BUSINESS_KEY ");

    Vector keyVector = tModelBag.getTModelKeyVector();

    int vectorSize = keyVector.size();
    if (vectorSize > 0)
    {
      sql.append("AND (");

      for (int i=0; i<vectorSize; i++)
      {
        TModelKey tModelKey = (TModelKey)keyVector.elementAt(i);
        String key = tModelKey.getText();

        sql.append("I.TMODEL_KEY = '").append(key).append("' ");

        if (i+1 < vectorSize)
          sql.append(" OR ");
      }

      sql.append(") ");
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

    sql.append("AND B.BUSINESS_KEY IN (");

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

  /**
   *
   */
  private static void appendOrderBy(StringBuffer sql,SearchQualifiers qualifiers)
  {
    sql.append("ORDER BY ");

    if (qualifiers == null)
      sql.append("B.LAST_UPDATE DESC");
    else if (qualifiers.sortByDateAsc)
      sql.append("B.LAST_UPDATE ASC");
    else
      sql.append("B.LAST_UPDATE DESC");
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
    TModelBag tModelBag = new TModelBag();
    Vector tModelKeyVector = new Vector();
    tModelKeyVector.addElement(new TModelKey(""));
    tModelKeyVector.addElement(new TModelKey(""));
    tModelKeyVector.addElement(new TModelKey("2a33d7d7-2b73-4de9-99cd-d4c51c186bce"));
    tModelKeyVector.addElement(new TModelKey("2a33d7d7-2b73-4de9-99cd-d4c51c186bce"));
    tModelBag.setTModelKeyVector(tModelKeyVector);

    Vector keysIn = new Vector();
    keysIn.add("13411e97-24cf-43d1-bee0-455e7ec5e9fc");
    keysIn.add("3f244f19-7ba7-4c3e-a93e-ae33e530794b");
    keysIn.add("3009f336-98c1-4193-a22f-fea73e79c909");
    keysIn.add("3ef4772f-e04b-46ed-8065-c5a4e167b5ba");

    Transaction txn = new Transaction();

    if (connection != null)
    {
      try
      {
        // begin a new transaction
        txn.begin(connection);

        select(tModelBag,keysIn,new SearchQualifiers(null),connection);
        select(tModelBag,null,new SearchQualifiers(null),connection);

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
