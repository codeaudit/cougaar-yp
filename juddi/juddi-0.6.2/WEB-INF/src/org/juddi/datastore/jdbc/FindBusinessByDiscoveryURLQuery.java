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
import org.uddi4j.util.DiscoveryURLs;
import org.uddi4j.util.DiscoveryURL;

import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.Statement;
import java.util.Vector;

/**
 * @author Steve Viens (steve@inflexionpoint.com)
 * @version 0.6.2
 */
class FindBusinessByDiscoveryURLQuery
{
  // private reference to the jUDDI logger
  private static Logger log = Logger.getLogger(FindBusinessByDiscoveryURLQuery.class);

  static String selectSQL;
  static
  {
    // build selectSQL
    StringBuffer sql = new StringBuffer(200);
    sql.append("SELECT B.BUSINESS_KEY,B.LAST_UPDATE ");
    sql.append("FROM BUSINESS_ENTITY B,DISCOVERY_URL U ");
    selectSQL = sql.toString();
  }

  /**
   * Select ...
   *
   * @param discoveryURLs
   * @param keysIn
   * @param qualifiers
   * @param connection JDBC connection
   * @throws java.sql.SQLException
   */
  public static Vector select(DiscoveryURLs discoveryURLs,Vector keysIn,SearchQualifiers qualifiers,Connection connection)
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
    appendWhere(sql,discoveryURLs,qualifiers);
    appendIn(sql,keysIn);
    appendOrderBy(sql,qualifiers);

    try
    {
      statement = connection.createStatement();

      log.info("select from BUSINESS_ENTITY & DISCOVERY_URL tables:\n\n\t" + sql.toString() + "\n");

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
  private static void appendWhere(StringBuffer sql,DiscoveryURLs discoveryURLs,SearchQualifiers qualifiers)
  {
    sql.append("WHERE B.BUSINESS_KEY = U.BUSINESS_KEY ");

    Vector urlVector = discoveryURLs.getDiscoveryURLVector();

    int vectorSize = urlVector.size();
    if (vectorSize > 0)
    {
      sql.append("AND (");

      for (int i=0; i<vectorSize; i++)
      {
        DiscoveryURL discoveryURL = (DiscoveryURL)urlVector.elementAt(i);
        String url = discoveryURL.getText();
        String useType = discoveryURL.getUseType();

        if ((url != null) && (url.length() > 0))
        {
          sql.append("(U.URL = '").append(url).append("'");

          if ((useType != null) && (useType.length() > 0))
            sql.append(" AND U.USE_TYPE = '").append(useType).append("'");

          sql.append(")");

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

    DiscoveryURLs discoveryURLs = new DiscoveryURLs();
    Vector urls = new Vector();
    urls.add(new DiscoveryURL("http://www.uddi.org/","wsdl"));
    urls.add(new DiscoveryURL("http://www.inflexionpoint.com/","soap"));
    urls.add(new DiscoveryURL("http://www.juddi.org/","wsdl"));
    discoveryURLs.setDiscoveryURLVector(urls);

    Vector keysIn = new Vector();
    keysIn.add("d77170a5-cf1e-45e8-bbf3-5b0756b29199");
    keysIn.add("c311085b-3277-470d-8ce9-07b81c484e4b");
    keysIn.add("e2805dbe-3957-4490-9230-5f06d85fd50d");
    keysIn.add("45994713-d3c3-40d6-87b5-6ce51f36001c");
    keysIn.add("901b15c5-799c-4387-8337-a1a35fceb791");
    keysIn.add("80fdae14-0e5d-4ea6-8eb8-50fde422056d");
    keysIn.add("e1996c33-c436-4004-9e3e-14de191bcc6b");
    keysIn.add("36f5d745-3be5-4e8f-8c1a-5f59a4c69070");

    Transaction txn = new Transaction();

    if (connection != null)
    {
      try
      {
        // begin a new transaction
        txn.begin(connection);

        select(discoveryURLs,keysIn,new SearchQualifiers(null),connection);
        select(discoveryURLs,null,new SearchQualifiers(null),connection);

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
