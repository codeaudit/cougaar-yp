/*
 * jUDDI - An open source Java implementation of UDDI v2.0
 * http://juddi.org/
 *
 * Copyright (c) 2002, InflexionPoint and contributors
 * All rights reserved.
 */

package org.juddi.client;

import org.uddi4j.client.UDDIProxy;

import java.util.Properties;
import java.net.URL;
import java.net.MalformedURLException;

/**
 *
 * @author Steve Viens (steve@inflexionpoint.com)
 */
public class UDDI4jProxy extends UDDIProxy
{
	/**
   * @see org.uddi4j.client.UDDIProxy#UDDIProxy()
	 */
	public UDDI4jProxy()
  {
		super();
	}

	/**
   * @see org.uddi4j.client.UDDIProxy#UDDIProxy(URL,URL)
	 */
	public UDDI4jProxy(URL inquiryURL, URL publishURL)
  {
		super(inquiryURL,publishURL);
	}

  /**
   * @see org.uddi4j.client.UDDIProxy#UDDIProxy(Properties)
	 */
	public UDDI4jProxy(Properties p)
    throws MalformedURLException
  {
		super(p);
	}

  // test driver
  public static void main(String[] args)
  {
    UDDI4jProxy prox = new UDDI4jProxy();
  }
}