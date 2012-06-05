/*
 * jUDDI - An open source Java implementation of UDDI v2.0
 * http://juddi.org/
 *
 * Copyright (c) 2002, InflexionPoint and contributors
 * All rights reserved.
 */

package org.juddi.uuidgen;

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
public interface UUIDGen
{
 /**
  *
  */
  public String uuidgen();

  /**
   *
   */
  public String[] uuidgen(int nmbr);
}