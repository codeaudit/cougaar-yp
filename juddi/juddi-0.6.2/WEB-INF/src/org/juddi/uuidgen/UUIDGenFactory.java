/*
 * jUDDI - An open source Java implementation of UDDI v2.0
 * http://juddi.org/
 *
 * Copyright (c) 2002, InflexionPoint and contributors
 * All rights reserved.
 */

package org.juddi.uuidgen;

import org.apache.log4j.Logger;

import java.io.*;

/**
 * A Universally Unique Identifier (UUID) is a 128 bit number generated
 * according to an algorithm that is garanteed to be unique in time and space
 * from all other UUIDs. It consists of an IEEE 802 Internet Address and
 * various time stamps to ensure uniqueness. For a complete specification,
 * see ftp://ietf.org/internet-drafts/draft-leach-uuids-guids-01.txt [leach].
 *
 * @author Steve Viens (steve@inflexionpoint.com)
 * @version 0.6.2
 */
public abstract class UUIDGenFactory
{
  // private reference to the jUDDI logger
  private static Logger log = Logger.getLogger(UUIDGenFactory.class);

  // default UUIDGen implementation
  private static final String defaultUUIDGenClassName = "org.juddi.uuidgen.JavaUUIDGen";

  /**
   * getInstance
   *
   * Returns the singleton instance of UUIDGen
   */
  public static UUIDGen getUUIDGen(String uuidgenClassName)
  {
    UUIDGen uuidgen = null;

    if ((uuidgenClassName == null) || (uuidgenClassName.length() == 0))
    {
      log.warn("An implementation of the org.juddi.uuidgen.UUIDGen interface " +
        "was not specified.  Defaulting to: " + defaultUUIDGenClassName);

      // use the default UUIDGen implementation
      uuidgenClassName = defaultUUIDGenClassName;
    }

    Class uuidgenClass = null;
    try
    {
      // instruct the class loader to load the UUIDGen implementation
      uuidgenClass = java.lang.Class.forName(uuidgenClassName);
    }
    catch(ClassNotFoundException e)
    {
      throw new RuntimeException("The implementation of UUIDGen interface " +
        "specified cannot be found in the classpath: "+uuidgenClassName +
        " not found.");
    }

    try
    {
      // try to instantiate the UUIDGen subclass
      uuidgen = (UUIDGen)uuidgenClass.newInstance();
    }
    catch(java.lang.Exception e)
    {
      throw new RuntimeException("Exception encountered while attempting to " +
        "instantiate the specified implementation of UUIDFactory: " +
        uuidgenClass.getName() + "; message = " + e.getMessage());
    }

    return uuidgen;
  }


  /***************************************************************************/
  /***************************** TEST DRIVER *********************************/
  /***************************************************************************/


  public static void main(String argc[])
  {
    UUIDGen generator = UUIDGenFactory.getUUIDGen(null);
    //UUIDGen generator = UUIDGenFactory.getUUIDGen("org.juddi.uuidgen.JavaUUIDGen");
    //UUIDGen generator = UUIDGenFactory.getUUIDGen("org.juddi.uuidgen.LinuxUUIDGen");
    //UUIDGen generator = UUIDGenFactory.getUUIDGen("org.juddi.uuidgen.Win32UUIDGen");
    //UUIDGen generator = UUIDGenFactory.getUUIDGen("org.juddi.uuidgen.AixUUIDGen");

    for (int i = 1; i <= 250; ++i)
      System.out.println( i + ":  " + generator.uuidgen() );
  }
}