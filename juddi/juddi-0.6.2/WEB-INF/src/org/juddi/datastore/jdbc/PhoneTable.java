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
class PhoneTable
{
  // private reference to the jUDDI logger
  private static Logger log = Logger.getLogger(PhoneTable.class);

  static String dropSQL = null;
  static String createSQL = null;
  static String insertSQL = null;
  static String selectSQL = null;
  static String deleteSQL = null;
  static String deleteByBusinessKeySQL = null;

  static
  {
    // buffer used to build SQL statements
    StringBuffer sql = null;

    // build dropSQL
    dropSQL = "DROP TABLE PHONE";

    // build createSQL
    sql = new StringBuffer(150);
    sql.append("CREATE TABLE PHONE (");
    sql.append("BUSINESS_KEY VARCHAR(41) NOT NULL,");
    sql.append("CONTACT_ID INT NOT NULL,");
    sql.append("PHONE_ID INT NOT NULL,");
    sql.append("USE_TYPE VARCHAR(255) NULL,");
    sql.append("PHONE_NUMBER VARCHAR(50) NOT NULL,");
    sql.append("PRIMARY KEY (BUSINESS_KEY,CONTACT_ID,PHONE_ID),");
    sql.append("FOREIGN KEY (BUSINESS_KEY,CONTACT_ID) REFERENCES CONTACT (BUSINESS_KEY,CONTACT_ID))");
    createSQL = sql.toString();

    // build insertSQL
    sql = new StringBuffer(150);
    sql.append("INSERT INTO PHONE (");
    sql.append("BUSINESS_KEY,");
    sql.append("CONTACT_ID,");
    sql.append("PHONE_ID,");
    sql.append("USE_TYPE,");
    sql.append("PHONE_NUMBER) ");
    sql.append("VALUES (?,?,?,?,?)");
    insertSQL = sql.toString();

    // build selectSQL
    sql = new StringBuffer(200);
    sql.append("SELECT ");
    sql.append("USE_TYPE,");
    sql.append("PHONE_NUMBER ");
    sql.append("FROM PHONE ");
    sql.append("WHERE BUSINESS_KEY=? ");
    sql.append("AND CONTACT_ID=? ");
    sql.append("ORDER BY PHONE_ID");
    selectSQL = sql.toString();

    // build deleteSQL
    sql = new StringBuffer(100);
    sql.append("DELETE FROM PHONE ");
    sql.append("WHERE BUSINESS_KEY=?");
    deleteSQL = sql.toString();
  }

  /**
   * Drop the PHONE table.
   */
  public static void drop(Connection connection)
  {
    System.out.print("DROP TABLE PHONE: ");
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
   * Create the PHONE table.
   */
  public static void create(Connection connection)
  {
    System.out.print("CREATE TABLE PHONE: ");
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
   * Insert new row into the PHONE table.<p>
   *
   * @param businessKey String to the BusinessEntity object that owns the Contact to be inserted
   * @param contactID The unique ID generated when saving the parent Contact instance.
   * @param phoneList Vector of Phone objects holding values to be inserted
   * @param connection JDBC connection
   * @throws java.sql.SQLException
   */
  public static void insert(String businessKey,int contactID,Vector phoneList,Connection connection)
    throws java.sql.SQLException
  {
    if ((phoneList == null) || (phoneList.size() == 0))
      return; // everything is valid but no elements to insert

    PreparedStatement statement = null;

    try
    {
      statement = connection.prepareStatement(insertSQL);
      statement.setString(1,businessKey.toString());
      statement.setInt(2,contactID);

      int listSize = phoneList.size();
      for (int phoneID=0; phoneID<listSize; phoneID++)
      {
        Phone phone = (Phone)phoneList.elementAt(phoneID);

        statement.setInt(3,phoneID);
        statement.setString(4,phone.getUseType());
        statement.setString(5,phone.getText());

        log.info("insert into PHONE table:\n\n\t" + insertSQL +
          "\n\t BUSINESS_KEY=" + businessKey.toString() +
          "\n\t CONTACT_ID=" + contactID +
          "\n\t PHONE_ID=" + phoneID +
          "\n\t USE_TYPE=" + phone.getUseType() +
          "\n\t PHONE_NUMBER=" + phone.getText() + "\n");

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
   * Select all rows from the CONTACT table for a given BusinessKey.<p>
   *
   * @param  businessKey String
   * @param  contactID Unique ID representing the parent Contact instance
   * @param  connection JDBC connection
   * @throws java.sql.SQLException
   */
  public static Vector select(String businessKey,int contactID,Connection connection)
    throws java.sql.SQLException
  {
    Vector phoneList = new Vector();
    PreparedStatement statement = null;
    ResultSet resultSet = null;

    try
    {
      // create a statement to query with
      statement = connection.prepareStatement(selectSQL);
      statement.setString(1,businessKey.toString());
      statement.setInt(2,contactID);

      log.info("select from PHONE table:\n\n\t" + selectSQL +
  "\n\t BUSINESS_KEY=" + businessKey.toString() +
  "\n\t CONTACT_ID=" + contactID + "\n");

      // execute the statement
      resultSet = statement.executeQuery();

      Phone phone = null;
      while (resultSet.next())
      {
        phone = new Phone();
        phone.setUseType(resultSet.getString("USE_TYPE"));
        phone.setText(resultSet.getString("PHONE_NUMBER"));
        phoneList.add(phone);
        phone = null;
      }

      log.info("select was successful, rows selected=" + phoneList.size());
      return phoneList;
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
   * Delete multiple rows from the PHONE table that are assigned to the
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

      log.info("delete from the PHONE table:\n\n\t" + deleteSQL +
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

        Vector contactList = new Vector();
        Contact contact = new Contact("John Smith");
        contact.setUseType("tech support");
        contactList.add(contact);
        int contactID = 0;

        Vector phoneList = new Vector();
        Phone phone = null;

        phone = new Phone("603.457.8110");
        phone.setUseType("Voice Mailbox");
        phoneList.add(phone);

        phone = new Phone("603.457.8111");
        phone.setUseType("Fax");
        phoneList.add(phone);

        phone = new Phone("603.457.8112");
        phone.setUseType("Mobil");
        phoneList.add(phone);

        phone = new Phone("603.457.8113");
        phone.setUseType("Pager");
        phoneList.add(phone);

        String authorizedUserID = "sviens";

        // begin a new transaction
        txn.begin(connection);

        // insert a new BusinessEntity
        BusinessEntityTable.insert(business,authorizedUserID,connection);

        // insert a new Contact
        ContactTable.insert(businessKey,contactList,connection);

        // insert a Collection of Phone objects
        PhoneTable.insert(businessKey,contactID,phoneList,connection);

        // select a Collection of Phone objects by BusinessKey
        phoneList = PhoneTable.select(businessKey,contactID,connection);

        // delete a Collection of Phone objects by BusinessKey
        PhoneTable.delete(businessKey,connection);

        // re-select a Collection of Phone objects by BusinessKey
        phoneList = PhoneTable.select(businessKey,contactID,connection);

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
