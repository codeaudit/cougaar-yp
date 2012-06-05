/*
 * jUDDI - An open source Java implementation of UDDI v2.0
 * http://juddi.org/
 *
 * Copyright (c) 2002, InflexionPoint and contributors
 * All rights reserved.
 */

package org.juddi.uuidgen;

import org.juddi.util.Config;

import java.io.IOException;
import java.io.BufferedReader;
import java.io.InputStreamReader;

/**
 * Used to create new universally unique identifiers or UUID's (sometimes called
 * GUID's).  UDDI UUID's are allways formmated according to DCE UUID conventions.
 *
 * @author Steve Viens (steve@inflexionpoint.com)
 * @version 0.6.2
 */
public final class Win32UUIDGen implements UUIDGen
{
  /**
   *
   */
  public String uuidgen()
  {
    String[] uuids = this.uuidgen(1);
    return uuids[0];
  }

  /**
   *
   */
  public String[] uuidgen(int nmbr)
  {
    String[] uuids = new String[nmbr];

    try
    {
      Runtime r = Runtime.getRuntime();
      Process p = r.exec("uuidgen -n" + nmbr);
      BufferedReader x = new BufferedReader(new InputStreamReader(p.getInputStream()));

      for (int i = 0; i < nmbr; ++i)
        uuids[i] = x.readLine();
    }
    catch (IOException ex)
    {
      ex.printStackTrace();
      throw new RuntimeException(ex.getMessage());
    }

    return uuids;
  }


  /***************************************************************************/
  /***************************** TEST DRIVER *********************************/
  /***************************************************************************/


  public static void main(String argc[])
  {
    UUIDGen generator = new Win32UUIDGen();

    long start = System.currentTimeMillis();

    for (int i = 1; i <= 250; ++i)
      generator.uuidgen();

    long end = System.currentTimeMillis();

    System.out.println("Generation of 250 UUID's took "+(end-start)+" milliseconds.");
  }
}