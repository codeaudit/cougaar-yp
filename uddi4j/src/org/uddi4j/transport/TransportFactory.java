/*
 * The source code contained herein is licensed under the IBM Public License
 * Version 1.0, which has been approved by the Open Source Initiative.
 * Copyright (C) 2001, International Business Machines Corporation
 * All Rights Reserved.
 *
 */

package org.uddi4j.transport;

import java.util.Properties;

/**
 * Factory to dynamically create a Transport implementation.
 *
 * @author David Melgar (dmelgar@us.ibm.com)
 */
public class TransportFactory {

   public static final String PROPERTY_NAME =
     "org.uddi4j.TransportClassName";

   public static final String DEFAULT_TRANSPORT_NAME=
      "org.uddi4j.transport.ApacheSOAPTransport";

   private Transport transport = null;
   static private String transportClassName  = null;

   Properties config = null;

   /**
    * Private constructor used by newInstance method.
    * 
    * @param p
    */
   private TransportFactory(Properties p) {
      config = p;
   }

   /**
    * Returns Transport implementation to be used.
    * Transport is cached.
    * Transport is dynamically loaded based on property
    * org.uddi4j.transport.TransportClassName set as
    * either a system property or in passed properties object.
    * If this property is not set, the default transport
    * is loaded.
    *
    * @return Transport
    * @exception TransportException
    *                   Thrown if transport class cannot be loaded.
    */
  public Transport getTransport() throws TransportException {

     if (transport==null) {
        transportClassName = config.getProperty(PROPERTY_NAME, DEFAULT_TRANSPORT_NAME);
        // Load and return the class
        try {
           transport = (Transport)Class.forName(transportClassName).newInstance();
           if ("true".equals(config.getProperty("org.uddi4j.logEnabled"))) {
              System.err.println("TransportFactory: Using transport name:" + transportClassName);
           }
        } catch (Exception e) {
           throw new TransportException(e);
        }
     }
     return transport;
   }

   /**
    * Create a TransportFactory.
    * 
    * @return TransportFactory
    */
   static public TransportFactory newInstance() {
      return new TransportFactory(System.getProperties());
   }

   /**
    * Create a TransportFactory passing in configuration information
    * in a properties object.
    * 
    * @param p Properties
    * @return TransportFactory
    * @see org.uddi4j.client.UDDIProxy#UDDIProxy(Properties) UDDIProxy(Properties) constructor for information on configuration object.
    */
   static public TransportFactory newInstance(Properties p) {
      return new TransportFactory(p);
   }
}
