/*
 * jUDDI - An open source Java implementation of UDDI v2.0
 * http://juddi.org/
 *
 * Copyright (c) 2002, InflexionPoint and contributors
 * All rights reserved.
 */

package org.juddi.util;

import org.apache.log4j.PropertyConfigurator;

import java.io.File;

/**
 * @author Steve Viens (steve@inflexionpoint.com)
 * @version 0.6.2
 */
public class SysManager
{
  // system state.
  private static boolean started = false;

  /**
   * ...
   */
  public static synchronized void startup()
  {
    if (SysManager.started)
      return;

    // check to make sure that the 'juddi.homeDir' jvm parameter has been
    // set so that we know what directory we're working out of.
    if (System.getProperty("juddi.homeDir",null) == null)
      System.out.println("jUDDI ERROR: juddi.homeDir jvm parameter has not " +
        "been properly set. jUDDI cannot function properly without this value. " +
        "Specify a jvm parameter this way \"java -Djuddi.homeDir=<somedir>\"");

    Config.startup();

    // setup/configure Log4j by accessing the log4j properties in the
    // 'log4j.properties' file that is found in jUDDI's conf directory
    String configDir = Config.getConfigDir();
    String propsFile = "log4j.properties";
    String log4jProps = configDir + File.separator + propsFile;
    PropertyConfigurator.configure(log4jProps);

    UUID.startup();
    Auth.startup();

    SysManager.started = true;
  }

  /**
   * called only from org.juddi.util.Shutdown.shutdown() to release any
   * aquired resources and stop any background threads.
   */
  public static synchronized void shutdown()
  {
    if (!SysManager.started)
      return;

    Auth.shutdown();
    UUID.shutdown();
    Config.shutdown();

    SysManager.started = false;
  }


  /***************************************************************************/
  /***************************** TEST DRIVER *********************************/
  /***************************************************************************/


  public static void main(String[] args)
    throws Exception
  {
    SysManager.startup();
    SysManager.shutdown();
  }
}
