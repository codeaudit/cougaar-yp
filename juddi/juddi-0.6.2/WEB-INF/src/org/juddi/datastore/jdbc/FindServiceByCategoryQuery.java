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
import org.uddi4j.util.CategoryBag;
import org.uddi4j.util.KeyedReference;

import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.Statement;
import java.util.Vector;

/**
 * @author Steve Viens (steve@inflexionpoint.com)
 * @version 0.6.2
 */
class FindServiceByCategoryQuery
{
  // private reference to the jUDDI logger
  private static Logger log = Logger.getLogger(FindServiceByCategoryQuery.class);

  static String selectSQL;
  static
  {
    // build selectSQL
    StringBuffer sql = new StringBuffer(200);
    sql.append("SELECT S.SERVICE_KEY,S.LAST_UPDATE ");
    sql.append("FROM BUSINESS_SERVICE S,SERVICE_CATEGORY C ");
    selectSQL = sql.toString();
  }

  /**
   * Select ...
   *
   * @param businessKey
   * @param categoryBag
   * @param keysIn
   * @param qualifiers
   * @param connection JDBC connection
   * @throws java.sql.SQLException
   */
  public static Vector select(String businessKey,CategoryBag categoryBag,Vector keysIn,SearchQualifiers qualifiers,Connection connection)
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
    appendWhere(sql,businessKey,categoryBag,qualifiers);
    appendIn(sql,keysIn);
    appendOrderBy(sql,qualifiers);

    try
    {
      statement = connection.createStatement();

      log.info("select from BUSINESS_SERVICE & SERVICE_CATEGORY tables:\n\n\t" + sql.toString() + "\n");

      resultSet = statement.executeQuery(sql.toString());
      while (resultSet.next())
      {
        keysOut.addElement(resultSet.getString("SERVICE_KEY"));
	log.info(resultSet.getString("SERVICE_KEY"));
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
          "the Find BusinessService ResultSet: "+e.getMessage(),e);
      }

      try {
        statement.close();
      }
      catch (Exception e)
      {
        log.warn("An Exception was encountered while attempting to close " +
          "the Find BusinessService Statement: "+e.getMessage(),e);
      }
    }
  }

  /**
   *
   */
  private static void appendWhere(StringBuffer sql,String businessKey,CategoryBag categoryBag,SearchQualifiers qualifiers)
  {
    sql.append("WHERE C.SERVICE_KEY = S.SERVICE_KEY ");

    // per UDDI v2.0 Programmers API Errata (pg. 14), businessKey is
    // is no longer a required attribute of the find_service service.
    if ((businessKey != null) && (businessKey.length() > 0)) {
      sql.append("AND S.BUSINESS_KEY = '").append(businessKey).append("' ");
    }

    Vector keyedRefVector = categoryBag.getKeyedReferenceVector();

    int vectorSize = keyedRefVector.size();
    if (vectorSize > 0)
    {
      sql.append("AND (");

      for (int i=0; i<vectorSize; i++)
      {
        KeyedReference keyedRef = (KeyedReference)keyedRefVector.elementAt(i);
        String name = keyedRef.getKeyName();
        String value = keyedRef.getKeyValue();

        if ((name != null) && (value != null))
        {
          sql.append("(C.KEY_NAME = '").append(name).append("' AND C.KEY_VALUE = '").append(value).append("')");

          if (i+1 < vectorSize)
            sql.append(" OR ");
        }
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

    sql.append("AND S.SERVICE_KEY IN (");

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
      sql.append("S.LAST_UPDATE DESC");
    else if (qualifiers.sortByDateAsc)
      sql.append("S.LAST_UPDATE ASC");
    else
      sql.append("S.LAST_UPDATE DESC");
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
    String businessKey = "740d75b1-3cde-4547-85dd-9578cd3ea1cd";

    CategoryBag categoryBag = new CategoryBag();
    Vector keyedRefVector = new Vector();
    keyedRefVector.addElement(new KeyedReference("ntis-gov:NAICS:1997","51121"));
    keyedRefVector.addElement(new KeyedReference("Mining","21"));
    keyedRefVector.addElement(new KeyedReference("cff049d0-c460-40c2-91c7-aa2261123dc7","Yadda, Yadda, Yadda"));
    keyedRefVector.addElement(new KeyedReference("1775f0f8-cd47-451d-88da-73ce508836f3","blah, blah, blah"));
    categoryBag.setKeyedReferenceVector(keyedRefVector);

    Vector keysIn = new Vector();
    keysIn.add("740d75b1-3cde-4547-85dd-9578cd3ea1cd");
    keysIn.add("c311085b-3277-470d-8ce9-07b81c484e4b");
    keysIn.add("6b368a5a-6a62-4f23-a002-f11e22780a91");
    keysIn.add("45994713-d3c3-40d6-87b5-6ce51f36001c");
    keysIn.add("901b15c5-799c-4387-8337-a1a35fceb791");
    keysIn.add("80fdae14-0e5d-4ea6-8eb8-50fde422056d");
    keysIn.add("e1996c33-c436-4004-9e3e-14de191bcc6b");
    keysIn.add("3ef4772f-e04b-46ed-8065-c5a4e167b5ba");

    Transaction txn = new Transaction();

    if (connection != null)
    {
      try
      {
        // begin a new transaction
        txn.begin(connection);

        select(businessKey,categoryBag,keysIn,new SearchQualifiers(null),connection);
        select(businessKey,categoryBag,null,new SearchQualifiers(null),connection);

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
