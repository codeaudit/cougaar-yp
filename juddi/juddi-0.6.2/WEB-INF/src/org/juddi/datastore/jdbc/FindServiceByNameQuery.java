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
import org.uddi4j.datatype.Name;

import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.Statement;
import java.util.Vector;

/**
 * @author Steve Viens (steve@inflexionpoint.com)
 * @version 0.6.2
 */
class FindServiceByNameQuery
{
  // private reference to the jUDDI logger
  private static Logger log = Logger.getLogger(FindServiceByNameQuery.class);

  static String selectSQL;
  static
  {
    // build selectSQL
    StringBuffer sql = new StringBuffer(200);
    sql.append("SELECT S.SERVICE_KEY,S.LAST_UPDATE,N.NAME ");
    sql.append("FROM BUSINESS_SERVICE S,SERVICE_NAME N ");
    selectSQL = sql.toString();
  }

  /**
   * Select ...
   *
   * @param businessKey primary key value
   * @param names
   * @param keysIn
   * @param qualifiers
   * @param connection JDBC connection
   * @throws java.sql.SQLException
   */
  public static Vector select(String businessKey,Vector names,Vector keysIn,SearchQualifiers qualifiers,Connection connection)
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
    appendWhere(sql,businessKey,names,qualifiers);
    appendIn(sql,keysIn);
    appendOrderBy(sql,qualifiers);

    try
    {
      statement = connection.createStatement();

      log.info("select from BUSINESS_SERVICE & SERVICE_NAME tables:\n\n\t" + sql.toString() + "\n");

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
  private static void appendWhere(StringBuffer sql,String businessKey,Vector names,SearchQualifiers qualifiers)
  {
    sql.append("WHERE N.SERVICE_KEY = S.SERVICE_KEY ");

    // per UDDI v2.0 Programmers API Errata (pg. 14), businessKey is
    // is no longer a required attribute of the find_service service.
    if((businessKey != null) && (businessKey.length() > 0) )
	    sql.append("AND S.BUSINESS_KEY = '").append(businessKey).append("' ");

    int nameSize = names.size();
    if (nameSize > 0)
    {
      sql.append("AND (");

      for (int i=0; i<nameSize; i++)
      {
        Name name = (Name)names.elementAt(i);
        String text = name.getText();
        String lang = name.getLang();

        if ((text != null) && (text.length() > 0))
        {
          if (qualifiers.exactNameMatch)
            sql.append("(NAME = '").append(text).append("'");
          else
            sql.append("(NAME LIKE '").append(text).append("%'");

          if ((lang != null) && (lang.length() > 0))
            sql.append(" AND LANG_CODE = '").append(lang).append("'");

          sql.append(")");

          if (i+1 < nameSize)
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

    if ((qualifiers == null) ||
       ((!qualifiers.sortByNameAsc) && (!qualifiers.sortByNameDesc) &&
        (!qualifiers.sortByDateAsc) && (!qualifiers.sortByDateDesc)))
    {
      sql.append("N.NAME ASC,S.LAST_UPDATE DESC");
    }
    else if (qualifiers.sortByNameAsc || qualifiers.sortByNameDesc)
    {
      if (qualifiers.sortByNameAsc && qualifiers.sortByDateDesc)
        sql.append("N.NAME ASC,S.LAST_UPDATE DESC");
      else if (qualifiers.sortByNameAsc && qualifiers.sortByDateAsc)
        sql.append("N.NAME ASC,S.LAST_UPDATE ASC");
      else if (qualifiers.sortByNameDesc && qualifiers.sortByDateDesc)
        sql.append("N.NAME DESC,S.LAST_UPDATE DESC");
      else
        sql.append("N.NAME DESC,S.LAST_UPDATE ASC");
    }
    else if (qualifiers.sortByDateAsc || qualifiers.sortByDateDesc)
    {
      if (qualifiers.sortByDateDesc)
        sql.append("S.LAST_UPDATE ASC,N.NAME ASC");
      else
        sql.append("S.LAST_UPDATE DESC,N.NAME ASC");
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
    String businessKey = "0e70128c-f7c6-4854-b292-d2f13b638acf";

    Vector names = new Vector();
    names.add(new Name("St"));
    //names.add(new Name("X"));
    //names.add(new Name("Select","en"));
    //names.add(new Name("Inflex","en"));

    Vector keysIn = null;
    keysIn = new Vector();
    keysIn.add("0e70128c-f7c6-4854-b292-d2f13b638acf");
    keysIn.add("b405450a-64f5-4f95-8131-450429d0ae8c");
    keysIn.add("3009f336-98c1-4193-a22f-fea73e79c909");
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

        select(businessKey,names,keysIn,new SearchQualifiers(null),connection);
        select(businessKey,names,null,new SearchQualifiers(null),connection);

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
