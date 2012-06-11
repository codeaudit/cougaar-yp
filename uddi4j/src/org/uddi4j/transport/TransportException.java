/*
 * The source code contained herein is licensed under the IBM Public License
 * Version 1.0, which has been approved by the Open Source Initiative.
 * Copyright (C) 2001, International Business Machines Corporation
 * All Rights Reserved.
 *
 */

package org.uddi4j.transport;

/**
 * Represents a communications error originating
 * within the SOAP transport. The native exception
 * thrown by the SOAP transport is encapsulated
 * within this exception.
 *
 * @author David Melgar (dmelgar@us.ibm.com)
 */
public class TransportException extends Exception {
   Exception nativeException = null;

   /**
    * Constructs an <code>TransportException</code> with no specified detail message.
    */
   public TransportException() {
       super();
   }

   /**
    * Constructs a <code>TransportException</code> with the specified detail message.
    *
    * @param   s   the detail message.
    */
   public TransportException(String s) {
       super(s);
   }

   /**
    * Constructs a <code>TransportException</code> wrappering the native transport
    * exception.
    *
    * @param   e   Native exception to be wrappered
    */
   public TransportException(Exception e) {
      super(e.getMessage());
      nativeException = e;
   }

   /**
    * Returns the native transport exception wrappered
    * by this class.
    *
    * @return Exception
    */
   public Exception getException() {
      return nativeException;
   }


   /**
    * Prints this <code>Throwable</code> and its backtrace to the
    * specified print stream as well as the wrappered exception.
    */
   public void printStackTrace() {
      printStackTrace(System.err);
   }

   /**
    * Prints this <code>Throwable</code> and its backtrace to the
    * specified print stream as well as the wrappered exception.
    *
    * @param s <code>PrintStream</code> to use for output
    */
   public void printStackTrace(java.io.PrintStream s) {
      super.printStackTrace(s);
      if (nativeException != null) {
         s.println("\nNested exception:");
         nativeException.printStackTrace(s);
      }
   }
}
