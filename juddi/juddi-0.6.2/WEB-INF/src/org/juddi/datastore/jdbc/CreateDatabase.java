/*
 * jUDDI - An open source Java implementation of UDDI v2.0
 * http://juddi.org/
 *
 * Copyright (c) 2002, InflexionPoint and contributors
 * All rights reserved.
 */

package org.juddi.datastore.jdbc;

import java.sql.Connection;
import java.sql.SQLException;

/**
 * @author Steve Viens (steve@inflexionpoint.com)
 * @version 0.6.2
 */
public class CreateDatabase
{
  /**
   *
   */
  public static void dropDatabase(java.sql.Connection connection)
    throws Exception
  {
    if (connection == null)
      throw new RuntimeException("Connection is null - cannot drop the database");

    BusinessDescTable.drop(connection);
    BusinessCategoryTable.drop(connection);
    BusinessIdentifierTable.drop(connection);
    BusinessNameTable.drop(connection);
    DiscoveryURLTable.drop(connection);
    AddressLineTable.drop(connection);
    AddressTable.drop(connection);
    PhoneTable.drop(connection);
    EmailTable.drop(connection);
    ContactDescTable.drop(connection);
    ContactTable.drop(connection);
    ServiceDescTable.drop(connection);
    ServiceCategoryTable.drop(connection);
    ServiceNameTable.drop(connection);
    BindingDescTable.drop(connection);
    InstanceDetailsDescTable.drop(connection);
    InstanceDetailsDocDescTable.drop(connection);
    TModelInstanceInfoDescTable.drop(connection);
    TModelInstanceInfoTable.drop(connection);
    TModelDescTable.drop(connection);
    TModelCategoryTable.drop(connection);
    TModelIdentifierTable.drop(connection);
    TModelDocDescTable.drop(connection);
    PublisherAssertionTable.drop(connection);
    TModelTable.drop(connection);
    BindingTemplateTable.drop(connection);
    BusinessServiceTable.drop(connection);
    BusinessEntityTable.drop(connection);
  }

  /**
   *
   */
  public static void createDatabase(java.sql.Connection connection)
    throws Exception
  {
    if (connection == null)
      throw new RuntimeException("Connection is null - cannot create the database");

    BusinessEntityTable.create(connection);
    BusinessDescTable.create(connection);
    BusinessCategoryTable.create(connection);
    BusinessIdentifierTable.create(connection);
    BusinessNameTable.create(connection);
    DiscoveryURLTable.create(connection);
    ContactTable.create(connection);
    ContactDescTable.create(connection);
    PhoneTable.create(connection);
    EmailTable.create(connection);
    AddressTable.create(connection);
    AddressLineTable.create(connection);
    BusinessServiceTable.create(connection);
    ServiceDescTable.create(connection);
    ServiceCategoryTable.create(connection);
    ServiceNameTable.create(connection);
    BindingTemplateTable.create(connection);
    BindingDescTable.create(connection);
    TModelInstanceInfoTable.create(connection);
    TModelInstanceInfoDescTable.create(connection);
    InstanceDetailsDescTable.create(connection);
    InstanceDetailsDocDescTable.create(connection);
    TModelTable.create(connection);
    TModelDescTable.create(connection);
    TModelCategoryTable.create(connection);
    TModelIdentifierTable.create(connection);
    TModelDocDescTable.create(connection);
    PublisherAssertionTable.create(connection);
  }

  /**
   *
   */
  private static void testDatabase(java.sql.Connection connection,int testCount)
    throws Exception
  {
    if (connection == null)
      throw new RuntimeException("Connection is null - cannot continue with test");

    for (int i=0; i<testCount; i++)
    {
      BusinessEntityTable.test(connection);
      BusinessDescTable.test(connection);
      BusinessCategoryTable.test(connection);
      BusinessIdentifierTable.test(connection);
      BusinessNameTable.test(connection);
      DiscoveryURLTable.test(connection);
      ContactTable.test(connection);
      ContactDescTable.test(connection);
      PhoneTable.test(connection);
      EmailTable.test(connection);
      AddressTable.test(connection);
      AddressLineTable.test(connection);
      BusinessServiceTable.test(connection);
      ServiceDescTable.test(connection);
      ServiceCategoryTable.test(connection);
      ServiceNameTable.test(connection);
      BindingTemplateTable.test(connection);
      BindingDescTable.test(connection);
      TModelInstanceInfoTable.test(connection);
      TModelInstanceInfoDescTable.test(connection);
      InstanceDetailsDescTable.test(connection);
      InstanceDetailsDocDescTable.test(connection);
      TModelTable.test(connection);
      TModelDescTable.test(connection);
      TModelCategoryTable.test(connection);
      TModelIdentifierTable.test(connection);
      TModelDocDescTable.test(connection);
      PublisherAssertionTable.test(connection);
    }
  }
  

  /**
   *
   */
  public static void main(String[] args)
  {
    org.juddi.util.SysManager.startup();

    Connection connection = null;

    try
    {
      connection = (new org.juddi.datastore.jdbc.HSQLDataStoreFactory()).getConnection();

      if (connection == null)
        throw new RuntimeException("Connection is null - cannot continue with test");

      int testIter = 0;
      if (args.length > 0)
        testIter = Integer.parseInt(args[0]);

      // drop all tables database
      long startDrop = System.currentTimeMillis();
      dropDatabase(connection);
      long endDrop = System.currentTimeMillis();
      System.out.println("*** Drop Database Completed (time="+(endDrop-startDrop)+") ***\n");

      // create the database
      long startCreate = System.currentTimeMillis();
      createDatabase(connection);
      long endCreate = System.currentTimeMillis();
      System.out.println("*** Create Database Completed (time="+(endCreate-startCreate)+") ***\n");

      // test the XxxxTable classes
      long startTest = System.currentTimeMillis();
      testDatabase(connection,testIter);
      long endTest = System.currentTimeMillis();
      System.out.println("*** Test Database Completed (time="+(endTest-startTest)+") ***\n");
    }
    catch(Exception ex)
    {
      ex.printStackTrace();
    }
    finally
    {
      try {
        connection.close();
      }
      catch(SQLException sqlex) {
        sqlex.printStackTrace();
      }
    }

    org.juddi.util.SysManager.shutdown();
  }
}