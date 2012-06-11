/*
 * jUDDI - An open source Java implementation of UDDI v2.0
 * http://juddi.org/
 *
 * Copyright (c) 2002, InflexionPoint and contributors
 * All rights reserved.
 */

package org.juddi.datastore;

import org.juddi.error.JUDDIException;
import org.juddi.util.Config;

import org.apache.log4j.Logger;

/**
 * Implementation of Factory pattern responsible for instantiating
 * the DataStore interface implementation.
 *
 * The name of the class to instantiate should exist as a property
 * in the juddi.properties configuration file with a property name
 * of juddi.datasource.datastoreClassName. If the property is not
 * found an Exception is thrown.
 *
 * @author Steve Viens (steve@inflexionpoint.com)
 * @version 0.6.2
 */
public abstract class DataStoreFactory
{
  // private reference to the jUDDI logger
  private static Logger log = Logger.getLogger(DataStoreFactory.class);

  // default DataStore implementation
  private static final String defaultDataStore =
      "org.juddi.datastore.jdbc.HSQLDataStoreFactory";

  // the DataStore class
  private static Class factoryClass = null;

  /**
   * Returns a new instance of the DataStore interface as specified by the
   * juddi.datastore property in the juddi.properties configuration file.
   * @return DataStoreFactory
   */
  public static DataStoreFactory getInstance()
  {
    DataStoreFactory factory = null;

    if (factoryClass==null)
      factoryClass = loadDataStoreFactoryClass();

    try {
      // try to instantiate the DataStoreFactory subclass
      factory = (DataStoreFactory)factoryClass.newInstance();
    }
    catch(java.lang.Exception e) {
      log.error("Exception while attempting to instantiate a subclass of the " +
        "DataStoreFactory class: " + factoryClass.getName() + "\n" + e.getMessage());
      log.error(e);
    }

    return factory;
  }

  /**
   *
   */
  public abstract DataStore aquireDataStore()
    throws JUDDIException;
  
  /**
   *
   */
  public abstract void releaseDataStore(DataStore datastore)
    throws JUDDIException;

  /**
   * Loads and returns a Class object that is an implementation of the DataStore
   * interface as specified by the juddi.datastore.datastoreClassName property
   * that's found in the juddi.properties configuration file.
   *
   * NOTE: This method DOES NOT return an instance of an DataStore,
   * it returns an instance of the java.lang.Class object which happens to be
   * an implementation of the DataStore interface (got that?).
   */
  private static synchronized Class loadDataStoreFactoryClass()
  {
    // check to make sure another thread didn't beat us to this code and
    // already load and instantiate an IDataStore.
    if (factoryClass==null)
    {
      // try to obtain the name of the DataStore implementaion to create
      String className = Config.getProperty("org.juddi.datastore.className");
      if ((className == null) || (className.length() == 0))
      {
        log.warn("An implementation of the org.juddi.datastore.DataStore " +
                  "interface was not specified. We're going to default " +
                  "to: " + defaultDataStore);

        // use the default DataStore implementation
        className = defaultDataStore;
      }

      try {
        // instruct the class loader to load the DataStore implementation
        factoryClass = java.lang.Class.forName(className);
      }
      catch(ClassNotFoundException e) {
        log.error("The specified sub class of the DataStoreFactory class was not " +
          "found in classpath: " + className + " not found.");
        log.error(e);
      }
    }

    return factoryClass;
  }

  // test driver
  public static void main(String[] args)
  {
    DataStoreFactory factory = DataStoreFactory.getInstance();
    if (factory != null)
      System.out.println("Got a DataStoreFactory: "+factory.getClass().getName());
    else
      System.out.println("Sorry - no DataStoreFactory for you.");
  }
}