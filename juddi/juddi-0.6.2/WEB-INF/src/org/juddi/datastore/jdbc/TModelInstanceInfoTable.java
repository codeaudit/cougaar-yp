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
import org.uddi4j.datatype.binding.*;
import org.uddi4j.datatype.business.*;
import org.uddi4j.datatype.service.*;
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
class TModelInstanceInfoTable
{
  // private reference to the jUDDI logger
  private static Logger log = Logger.getLogger(TModelInstanceInfoTable.class);

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
    dropSQL = "DROP TABLE TMODEL_INSTANCE_INFO";

    // build createSQL
    sql = new StringBuffer(250);
    sql.append("CREATE TABLE TMODEL_INSTANCE_INFO (");
    sql.append("BINDING_KEY VARCHAR(41) NOT NULL,");
    sql.append("TMODEL_INSTANCE_INFO_ID INT NOT NULL,");
    sql.append("TMODEL_KEY VARCHAR(41) NOT NULL,");
    sql.append("OVERVIEW_URL VARCHAR(255) NULL,");
    sql.append("INSTANCE_PARMS VARCHAR(255) NULL,");
    sql.append("PRIMARY KEY (BINDING_KEY,TMODEL_INSTANCE_INFO_ID),");
    sql.append("FOREIGN KEY (BINDING_KEY) REFERENCES BINDING_TEMPLATE (BINDING_KEY))");
    createSQL = sql.toString();

    // build insertSQL
    sql = new StringBuffer(150);
    sql.append("INSERT INTO TMODEL_INSTANCE_INFO (");
    sql.append("BINDING_KEY,");
    sql.append("TMODEL_INSTANCE_INFO_ID,");
    sql.append("TMODEL_KEY, ");
    sql.append("OVERVIEW_URL,");
    sql.append("INSTANCE_PARMS) ");
    sql.append("VALUES (?,?,?,?,?)");
    insertSQL = sql.toString();

    // build selectSQL
    sql = new StringBuffer(200);
    sql.append("SELECT ");
    sql.append("TMODEL_KEY,");
    sql.append("OVERVIEW_URL,");
    sql.append("INSTANCE_PARMS ");
    sql.append("FROM TMODEL_INSTANCE_INFO ");
    sql.append("WHERE BINDING_KEY=? ");
    sql.append("ORDER BY TMODEL_INSTANCE_INFO_ID");
    selectSQL = sql.toString();

    // build deleteSQL
    sql = new StringBuffer(100);
    sql.append("DELETE FROM TMODEL_INSTANCE_INFO ");
    sql.append("WHERE BINDING_KEY=?");
    deleteSQL = sql.toString();
  }

  /**
   * Drop the TMODEL_INSTANCE_INFO table.
   */
  public static void drop(Connection connection)
  {
    System.out.print("DROP TABLE TMODEL_INSTANCE_INFO: ");
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
   * Create the TMODEL_INSTANCE_INFO table.
   */
  public static void create(Connection connection)
  {
    System.out.print("CREATE TABLE TMODEL_INSTANCE_INFO: ");
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
   * Insert new row into the TMODEL_INSTANCE_INFO table.<p>
   *
   * @param bindingKey String to the BusinessEntity object that owns the Contact to be inserted
   * @param infoList Vector of Contact objects holding values to be inserted
   * @param connection JDBC connection
   * @throws java.sql.SQLException
   */
  public static void insert(String bindingKey,Vector infoList,Connection connection)
    throws java.sql.SQLException
  {
    if ((infoList == null) || (infoList.size() == 0))
      return; // everything is valid but no elements to insert

    PreparedStatement statement = null;

    try
    {
      statement = connection.prepareStatement(insertSQL);
      statement.setString(1,bindingKey.toString());

      int listSize = infoList.size();
      for (int infoID=0; infoID<listSize; infoID++)
      {
        String tModelKey = null;
        String overURL = null;
        String instParms = null;

        TModelInstanceInfo info = (TModelInstanceInfo)infoList.elementAt(infoID);
        if (info.getTModelKey() != null)
          tModelKey = info.getTModelKey().toString();

        InstanceDetails details = info.getInstanceDetails();
        if (details != null)
        {
          if (details.getOverviewDoc() != null)
            overURL = details.getOverviewDoc().getOverviewURLString();

          if (details.getInstanceParms() != null)
            instParms = details.getInstanceParms().getText();
        }

        // insert sequence number
        statement.setInt(2,infoID);
        statement.setString(3,tModelKey);
        statement.setString(4,overURL);
        statement.setString(5,instParms);

        log.info("insert into TMODEL_INSTANCE_INFO table:\n\n\t" + insertSQL +
          "\n\t BINDING_KEY=" + bindingKey.toString() +
          "\n\t TMODEL_INSTANCE_INFO_ID=" + infoID +
          "\n\t TMODEL_KEY=" + tModelKey +
          "\n\t OVERVIEW_URL=" + overURL +
          "\n\t INSTANCE_PARMS=" + instParms + "\n");

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
   * Select all rows from the TMODEL_INST_INFO table for a given BusinessKey.<p>
   *
   * @param bindingKey String
   * @param connection JDBC connection
   * @throws java.sql.SQLException
   */
  public static Vector select(String bindingKey,Connection connection)
    throws java.sql.SQLException
  {
    Vector infoList = new Vector();
    PreparedStatement statement = null;
    ResultSet resultSet = null;

    try
    {
     // create a statement to query with
      statement = connection.prepareStatement(selectSQL);
      statement.setString(1,bindingKey.toString());

      log.info("select from TMODEL_INSTANCE_INFO table:\n\n\t" + selectSQL +
        "\n\t BINDING_KEY=" + bindingKey.toString() + "\n");

      // execute the statement
      resultSet = statement.executeQuery();

      while (resultSet.next())
      {
        String tModelKey = resultSet.getString("TMODEL_KEY");
        String overURL = resultSet.getString("OVERVIEW_URL");
        String instParms = resultSet.getString("INSTANCE_PARMS");

        if (tModelKey != null)
        {
          TModelInstanceInfo info = new TModelInstanceInfo();
          info.setTModelKey(tModelKey);

          OverviewDoc overviewDoc = null;
          if (overURL != null)
          {
            overviewDoc = new OverviewDoc();
            overviewDoc.setOverviewURL(overURL);
          }

          InstanceParms instanceParms = null;
          if (instParms != null)
          {
            instanceParms = new InstanceParms();
            instanceParms.setText(instParms);
          }

          InstanceDetails details = null;
          if ((overviewDoc != null) || (instanceParms != null))
          {
            details = new InstanceDetails();
            details.setOverviewDoc(overviewDoc);
            details.setInstanceParms(instanceParms);
            info.setInstanceDetails(details);
          }

          infoList.add(info);
        }
      }

      log.info("select was successful, rows selected=" + infoList.size());
      return infoList;
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
   * Delete multiple rows from the TMODEL_INST_INFO table that are assigned to the
   * BusinessKey specified.<p>
   *
   * @param bindingKey String
   * @param connection JDBC connection
   * @throws java.sql.SQLException
   */
  public static void delete(String bindingKey,Connection connection)
    throws java.sql.SQLException
  {
    PreparedStatement statement = null;

    try
    {
      // prepare the delete
      statement = connection.prepareStatement(deleteSQL);
      statement.setString(1,bindingKey.toString());

      log.info("delete from TMODEL_INSTANCE_INFO table:\n\n\t" + deleteSQL +
        "\n\t BINDING_KEY=" + bindingKey.toString() + "\n");

      // execute the delete
      int returnCode = statement.executeUpdate();

      log.info("delete was successful, rows selected=" + returnCode);
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
        business.setAuthorizedName("mleblanc");
        business.setOperator("XMLServiceRegistry.com");

        String serviceKey = UUID.nextID();
        BusinessService service = new BusinessService();
        service.setBusinessKey(businessKey);
        service.setServiceKey(serviceKey);

        String bindingKey = UUID.nextID();
        BindingTemplate binding = new BindingTemplate();
        binding.setServiceKey(serviceKey);
        binding.setBindingKey(bindingKey);
        binding.setAccessPoint(new AccessPoint("http://www.juddi.org/tmodelinstanceinfo.html","http"));

        Vector infoList = new Vector();
        infoList.add(new TModelInstanceInfo(UUID.nextID()));
        infoList.add(new TModelInstanceInfo(UUID.nextID()));
        infoList.add(new TModelInstanceInfo(UUID.nextID()));
        infoList.add(new TModelInstanceInfo(UUID.nextID()));

        String authorizedUserID = "sviens";

        // begin a new transaction
        txn.begin(connection);

        // insert a new BusinessEntity
        BusinessEntityTable.insert(business,authorizedUserID,connection);

        // insert a new BusinessService
        BusinessServiceTable.insert(service,connection);

        // insert a new BindingTemplate
        BindingTemplateTable.insert(binding,connection);

        // insert a Collection of TModelInstanceInfo objects
        TModelInstanceInfoTable.insert(bindingKey,infoList,connection);

        // select a Collection of TModelInstanceInfo objects (by BindingKey)
        infoList = TModelInstanceInfoTable.select(bindingKey,connection);

        // delete a Collection of TModelInstanceInfo objects (by BindingKey)
        TModelInstanceInfoTable.delete(bindingKey,connection);

        // re-select a Collection of TModelInstanceInfo objects (by BindingKey)
        infoList = TModelInstanceInfoTable.select(bindingKey,connection);

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
