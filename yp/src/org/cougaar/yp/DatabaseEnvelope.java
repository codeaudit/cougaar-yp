/*
 * <copyright>
 *  Copyright 2003 BBNT Solutions, LLC
 *  under sponsorship of the Defense Advanced Research Projects Agency (DARPA).
 *
 *  This program is free software; you can redistribute it and/or modify
 *  it under the terms of the Cougaar Open Source License as published by
 *  DARPA on the Cougaar Open Source Website (www.cougaar.org).
 *
 *  THE COUGAAR SOFTWARE AND ANY DERIVATIVE SUPPLIED BY LICENSOR IS
 *  PROVIDED 'AS IS' WITHOUT WARRANTIES OF ANY KIND, WHETHER EXPRESS OR
 *  IMPLIED, INCLUDING (BUT NOT LIMITED TO) ALL IMPLIED WARRANTIES OF
 *  MERCHANTABILITY AND FITNESS FOR A PARTICULAR PURPOSE, AND WITHOUT
 *  ANY WARRANTIES AS TO NON-INFRINGEMENT.  IN NO EVENT SHALL COPYRIGHT
 *  HOLDER BE LIABLE FOR ANY DIRECT, SPECIAL, INDIRECT OR CONSEQUENTIAL
 *  DAMAGES WHATSOEVER RESULTING FROM LOSS OF USE OF DATA OR PROFITS,
 *  TORTIOUS CONDUCT, ARISING OUT OF OR IN CONNECTION WITH THE USE OR
 *  PERFORMANCE OF THE COUGAAR SOFTWARE.
 * </copyright>
 */

package org.cougaar.yp;

import java.io.BufferedInputStream;
import java.io.BufferedOutputStream;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.io.Serializable;
import java.util.zip.ZipEntry;
import java.util.zip.ZipInputStream;
import java.util.zip.ZipOutputStream;

import org.cougaar.util.log.Logger;
import org.cougaar.util.log.Logging;

/**
 * A serialization wrapper around the disk files which make up the database.
 * The key point here is that these objects are trivial to create (four data members,
 * the non-trivial ones pointing to externally maintained objects), but expensive
 * to serialize and read.
 * <p>
 * Essentially, serializing a DatabaseEnvelope means zipping up the contents of the 
 * database file directory while holding the database activity lock so that it cannot
 * be changed.  The zip archive is then copied to the serialization stream as a byte array.
 * <p>
 * Reading one of these objects keeps the byte array around for later dumping back to 
 * a directory.
 *
 */

public class DatabaseEnvelope implements Serializable {
  public static final Logger logger = Logging.getLogger(DatabaseEnvelope.class);

  private File directory;
  private long timestamp;

  private transient byte[] stuff = null; // if non-null, contains a persist snapshot
  private transient Locker locker = null; 

  DatabaseEnvelope(File directory, Locker locker) {
    assert directory.isDirectory();
    assert locker != null;
    this.directory = directory;
    this.locker = locker;
    timestamp = System.currentTimeMillis();
    if (logger.isInfoEnabled()) logger.info("Created "+this);
  }
  
  public String toString() {
    return "DatabaseEnvelope of "+directory+
      " @"+timestamp+
      ((stuff==null)?"":" (pending)");
  }

  private void writeObject(ObjectOutputStream out) throws IOException {
    if (logger.isInfoEnabled()) logger.info("About to serialize "+this);
    out.defaultWriteObject();
    if (locker == null) {       // if locker is null, then we've already rehydrated
      assert stuff != null;
      out.writeObject(stuff);         // might as well support re-writing
      if (logger.isInfoEnabled()) logger.info("Reserialized "+this);
    } else {
      ByteArrayOutputStream bos = new ByteArrayOutputStream();

      synchronized (locker) {   // sync
        try {
          locker.stop();

          // when we are really called, we serialize the database into the stream (yech!)
          ZipOutputStream zos = new ZipOutputStream(bos);
          // we'll leave it the default for now since this stuff is easily compressed.
          File[] files = directory.listFiles();
          byte[] buf = new byte[1024];
          for (int i=0; i<files.length; i++) {
            File f = files[i];
            if (! f.isFile()) continue; // skip any directories

            ZipEntry ze = new ZipEntry(f.getName());
            zos.putNextEntry(ze);

            // copy the file to the zip stream
            BufferedInputStream fin = new BufferedInputStream(new FileInputStream(f));
            int len;
            while ( (len = fin.read(buf,0,1024)) > 0) {
              zos.write(buf,0,len);
            }
            fin.close();
      
            zos.closeEntry();
          }
          zos.finish();

        } finally {
          locker.start();
        }
      }

      out.writeObject(bos.toByteArray());
      if (logger.isInfoEnabled()) logger.info("Serialized "+this);
    }
  }
  
  private void readObject(ObjectInputStream in) throws ClassNotFoundException, IOException {
    in.defaultReadObject();
    // when we read the object, we just suck the bits into our byte array - if we need to
    // use the bits, we'll have to fool with the database at that point.
    stuff = (byte[]) in.readObject();
    if (logger.isInfoEnabled()) logger.info("Got rehydration snapshot ("+stuff.length+" bytes) from "+this);
  }

  public boolean hasPayload() { return stuff != null; }

  /** dump the stored bytes into the specified directory **/
  public void dumpPayload(File parent) throws IOException {
    assert hasPayload();

    if (logger.isInfoEnabled()) logger.info("Rehydrating from "+this);

    ByteArrayInputStream is = new ByteArrayInputStream(stuff);
    ZipInputStream zis = new ZipInputStream(is);
    byte[] buf = new byte[1024];

    ZipEntry ze;
    while ( (ze = zis.getNextEntry()) != null) {
      String fname = ze.getName();
      File f = new File(parent, fname);
      if (f.exists()) f.delete();
      BufferedOutputStream out = new BufferedOutputStream(new FileOutputStream(f));
      int len;
      while ( (len = zis.read(buf)) > 0) {
        out.write(buf, 0, len);
      }
      out.close();
      zis.closeEntry();
    }
    zis.close();
    
    // when done, should we null the stuff to prime the gc?  No - we need to be able to
    // re-write it until the database gets another snapshot taken.
    //stuff = null;
  }

  /** Database Lock abstraction - provides for both locking (to prevent database modifications
   * during backups) and stop/start (to provide for cache flushing and database compression)
   **/
  public interface Locker {
    /** call while synchronized to start the database instance **/
    void stop();
    /** call while synchronized and stopped to restart the database instance **/
    void start();
  }

}
