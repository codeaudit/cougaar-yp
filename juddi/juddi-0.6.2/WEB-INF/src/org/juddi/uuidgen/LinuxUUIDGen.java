/*
 * jUDDI - An open source Java implementation of UDDI v2.0
 * http://juddi.org/
 *
 * Copyright (c) 2002, InflexionPoint and contributors
 * All rights reserved.
 */

package org.juddi.uuidgen;

import java.io.*;

/**
 * Used to create new universally unique identifiers or UUID's (sometimes called
 * GUID's).  UDDI UUID's are allways formmated according to DCE UUID conventions.
 *
 * @author Steve Viens (steve@inflexionpoint.com)
 * @version 0.6.2
 */
public final class LinuxUUIDGen implements UUIDGen
{
  /**
   *
   */
  public String uuidgen()
  {
    try
    {
      Runtime r = Runtime.getRuntime();
      Process p = r.exec("uuidgen");
      BufferedReader x = new BufferedReader(new InputStreamReader(p.getInputStream()));

      return x.readLine();
    }
    catch (IOException e)
    {
      throw new RuntimeException(e.getMessage());
    }
  }

  /**
   *
   */
  public String[] uuidgen(int nmbr)
  {
    String[] uuids = new String[nmbr];
    
    for (int i=0; i<uuids.length; i++)
      uuids[i] = uuidgen();
    
    return uuids;
  }


  /***************************************************************************/
  /***************************** TEST DRIVER *********************************/
  /***************************************************************************/


  public static void main(String argc[])
  {
    UUIDGen generator = new LinuxUUIDGen();

    long start = System.currentTimeMillis();

    for (int i = 1; i <= 250; ++i)
      generator.uuidgen();

    long end = System.currentTimeMillis();

    System.out.println("Generation of 250 UUID's took "+(end-start)+" milliseconds.");
  }
}