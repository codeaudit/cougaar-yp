/*
 * jUDDI - An open source Java implementation of UDDI v2.0
 * http://juddi.org/
 *
 * Copyright (c) 2002, InflexionPoint and contributors
 * All rights reserved.
 */

package org.juddi.client;

import org.juddi.util.Config;

import org.apache.log4j.Logger;
import org.uddi4j.client.UDDIProxy;

import java.net.MalformedURLException;

/**
 *
 * @author waterman
 */
public class UDDIProxyFactory
{
	// private reference to the jUDDI logger
  private static Logger log = Logger.getLogger(UDDIProxyFactory.class);

	private static final String CLIENT_PROXY_CLASS_KEY = "org.juddi.client.className";
	private static final String	CLIENT_PROXY_CLASS_DEFAULT = "org.juddi.client.JUDDIProxy";

	/**
   *
   * @return UDDIProxy
   */
  public static UDDIProxy getInstance()
	{
		UDDIProxy retval = null;

		String proxyName = Config.getProperty(CLIENT_PROXY_CLASS_KEY);
		if ( proxyName == null )
			proxyName = CLIENT_PROXY_CLASS_DEFAULT;

    Class proxyClass = null;
		try {
      // instruct the class loader to load the Proxy implementation
      proxyClass = java.lang.Class.forName(proxyName);
		}
		catch(ClassNotFoundException e) {
      String msg = "The specified sub class of the UDDIProxy class was " +
        "not found in classpath: " + proxyName + " not found.";

      log.error(msg,e);
      throw new RuntimeException(msg);
		}

		try {
			log.info("Attempting to instantiate: " + proxyName);

			// try to instantiate the DataStoreFactory subclass
			retval = (UDDIProxy)proxyClass.newInstance();
		}
		catch(java.lang.Exception e) {
      String msg = "Exception while attempting to instantiate a subclass " +
        "of the UDDIProxy class: " + e.getMessage();

			log.error(msg,e);
      throw new RuntimeException(msg);
		}

		try {
			retval.setConfiguration(Config.getProperties());
		}
		catch (MalformedURLException me ) {
			log.error(me);
			throw new RuntimeException(me);
		}

    return retval;
	}
}