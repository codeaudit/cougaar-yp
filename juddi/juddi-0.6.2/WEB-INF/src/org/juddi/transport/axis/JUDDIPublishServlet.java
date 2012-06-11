/*
 * jUDDI - An open source Java implementation of UDDI v2.0
 * http://juddi.org/
 *
 * Copyright (c) 2002, InflexionPoint and contributors
 * All rights reserved.
 */

package org.juddi.transport.axis;

import org.juddi.util.SysManager;

import org.apache.axis.transport.http.AxisServlet;
import org.apache.log4j.Logger;

import java.io.*;
import java.util.*;
import javax.servlet.*;
import javax.servlet.http.*;

/**
 * @author Steve Viens (steve@inflexionpoint.com)
 * @version 0.6.2
 */
public class JUDDIPublishServlet extends AxisServlet
{
  // private reference to the jUDDI logger
  private static Logger log = Logger.getLogger(JUDDIPublishServlet.class);

  /**
   *
   */
  public void init()
  {
    // determin the location of jUDDI's WEB-INF directory
    // and stuff it into the VM as a System property so that
    // the rest of the webapp can use it (specifically Config).
    String realPath = getServletContext().getRealPath("/WEB-INF/");
    if (realPath != null)
      System.setProperty("juddi.homeDir",realPath);

    // Send startup message to the console
    //System.out.println("Starting service jUDDI 0.6.2 (UDDI Publish)");

    // call jUDDI's transport independent startup method
    // located in the org.juddi.util.SysManager class. This
    // method is responsible for starting up and/or
    // initializing jUDDI's lower level components such as
    // the UUID Generator, and the Datasource.
    SysManager.startup();
  }

  /**
   *
   */
  public void destroy()
  {
    // call jUDDI's transport independent shutdown method
    // located in the org.juddi.util.SysManager class. This
    // method is responsible for a'safely' releasing any
    // resources being used by jUDDI's lower level components
    // such as org.juddi.uuidgen and org.juddi.datasource.
    SysManager.shutdown();
  }
}
