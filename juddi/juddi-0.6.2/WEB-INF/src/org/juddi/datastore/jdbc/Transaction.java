/*
 * jUDDI - An open source Java implementation of UDDI v2.0
 * http://juddi.org/
 *
 * Copyright (c) 2002, InflexionPoint and contributors
 * All rights reserved.
 */

package org.juddi.datastore.jdbc;

import java.util.*;
import java.sql.*;

/**
 * Transaction txn = new Transaction();
 * txn.begin(conn1);
 * txn.begin(conn2);
 * txn.begin(conn3);
 * txn.commit();
 * txn.rollback();
 *
 * @author  Graeme Riddell
 * @version 0.6.2
 */
class Transaction
{
  /**
   * Vector of all connections involved in this transaction
   */
  private Vector vect = null;

  /**
   * default constructor
   */
  public Transaction()
  {
    this.vect = new Vector();
  }

  /**
   * If the connection is known then do nothing. If the connection is
   * new then issue a SQL begin work and hold onto it for later. Actually the
   * begin work is implicit and autocommit drives whether a transaction is
   * progressed.
   */
  public void begin(Connection conn)
    throws SQLException
  {
    // make sure auto commit is disabled
    if (conn.getAutoCommit() == true)
      conn.setAutoCommit(false);

    // If this connection has already been begun then
    // just return to the caller. Nothing more to do.
    for (int i=0; i<vect.size(); i++)
    {
      if ((Connection)(vect.elementAt(i)) == conn)
        return;
    }

    // add new connection to the collection
    vect.add(conn);
  }

 /**
  * commit on all connections. This is not XA, but it could be one day.
  */
  public void commit()
    throws SQLException
  {
    // loop through all connections and commit them
    for (int i=0; i<vect.size(); i++)
    {
      Connection conn = (Connection)vect.elementAt(i);
      conn.commit();
    }

    // they're all committed, now let's discard them
    vect.removeAllElements();
  }

 /**
  * rollback on all connections. This is not XA, but it could be one day.
  */
  public void rollback()
    throws SQLException
  {
    // loop through all collections and roll them back
    for (int i=0; i<vect.size(); i++)
    {
      Connection conn = (Connection)vect.elementAt(i);
      conn.rollback();
    }

    // they're all rolled back, now let's discard them
    vect.removeAllElements();
  }
}
