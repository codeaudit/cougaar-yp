/*
 * <copyright>
 *  Copyright 2002-2003 BBNT Solutions, LLC
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

import java.io.*;
import java.net.*;
import java.util.*;

/** Implement an asynchronous wait/notify map with value pass-through, selected by an opaque key.
 **/
class WaitQueue {
  /** An object returned by waitFor if timed out **/
  public static final Object TIMEOUT = new Object();

  private long counter = 0L;    // access must be synced on selects
  private final HashMap selects = new HashMap(11); // assume not too many at a time

  /** Construct a key to be used in this wait queue
   **/
  Object getKey() {
    synchronized (selects) {
      Object key = new Long(counter++);
      selects.put(key, new Waiter());
      return key;
    }
  }

  /** Activate any thread(s) which are waiting for a response from the key **/
  void trigger(Object key, Object value) {
    Waiter waiter = null;
    synchronized (selects) {
      waiter = (Waiter) selects.get(key);
      if (waiter == null) return; // bail if no waiter
    }
    waiter.trigger(value);
  }

  /** Block the current thread until there is a triggerKey has been called
   * on the referenced key.
   * Returns immediately if the key is unknown or has previously been triggered.
   * @return The value or TIMEOUT if the key is still outstanding.
   * @throw InterruptedException if the wait is interrupted.  In this case,
   * the trigger is <em>not</em> cleared.
   * @param key The key as returned by #getKey()
   * @param timeout How long to wait (in millis) or 0 (forever)
   **/
  Object waitFor(Object key, long timeout) throws InterruptedException {
    Waiter waiter = null;
    synchronized (selects) {
      waiter = (Waiter) selects.get(key);
      if (waiter == null) return null; // bail if no waiter
    }

    // non-null waiter: pass control to it
    boolean done = waiter.waitFor(timeout);
    if (!done) {
      return TIMEOUT;
    } else {
      // when done, clear it
      synchronized (selects) {
        selects.remove(key);
      }

      return waiter.value();
    }
  }

  /** Equivalent to waitFor(key,0L)
   **/
  Object waitFor(Object key) throws InterruptedException {
    return waitFor(key,0L);
  }

  /** Return true IFF waitFor(key) call would have blocked for some amount of time **/
  boolean wouldBlock(Object key) {
    Waiter waiter = null;
    synchronized (selects) {
      waiter = (Waiter) selects.get(key);
      if (waiter == null) return false;
    }

    // non-null waiter: pass control to it
    return waiter.wouldBlock();
  }

  private static class Waiter {
    private boolean triggered = false;
    private Object value = null;

    /** wait a specified length of time for a trigger event.
     * @return true if an event happened.
     **/
    synchronized boolean waitFor(long timeout) throws InterruptedException {
      if (triggered) return true;
      this.wait(timeout);
      return triggered;
    }

    /** The value of the Waiter, only defined if triggered already **/
    synchronized Object value() {
      assert triggered;
      return value;
    }

    synchronized void trigger(Object value) {
      triggered = true;
      this.value = value;
      this.notify();
    }

    synchronized boolean wouldBlock() {
      return (!triggered);
    }
  }

  /*
  public static void main(String [] arg) {
    final ArrayList keys = new ArrayList();
    final WaitQueue wq = new WaitQueue();
    final int m = Integer.parseInt(arg[0]);
    final Random rand = new Random();

    System.out.println("Starting threads...");
    for (int j = 0; j<10; j++) {
      final int fj = j*1000;
      new Thread(new Runnable() {
          public void run() {
            for (int i = 0; i<m; i++) {
              final Object k = wq.getKey();
              final Object r = new Integer(fj+i);
              new Thread(new Runnable() {
                  public void run() {
                    try {
                      Thread.sleep(rand.nextInt(10000));
                    } catch(Exception e) {
                      e.printStackTrace();
                    }
                    wq.trigger(k, r);
                  }
                }).start();
              synchronized (keys) {
                keys.add(k);
              }
            }
          }
        }).start();
    }
      
    System.out.println("Waiting for key set completion...");
    while (true) {
      synchronized (keys) {
        if (keys.size() == m*10) break;
        try {
          Thread.sleep(10L);
        } catch(Exception e) {
          e.printStackTrace();
        }
      }
    }
    
    System.out.println("Waiting for threads...");
    for (int i=0;i<(m*10); i++) {
      try {
        Object v;
        while ( (v=wq.waitFor(keys.get(i), 100L)) == TIMEOUT) {
          System.out.print(".");
        }
        System.out.println("result "+i+" = "+v);
      } catch (Exception e) {
        e.printStackTrace();
      }
    }
    System.out.println("All threads in...");    
  }
  */
}
