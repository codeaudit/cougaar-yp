/*
 * The source code contained herein is licensed under the IBM Public License
 * Version 1.0, which has been approved by the Open Source Initiative.
 * Copyright (C) 2001, International Business Machines Corporation
 * All Rights Reserved.
 *
 */

package org.uddi4j;

import java.util.Vector;
import org.w3c.dom.Node;

public class VectorNodeList implements org.w3c.dom.NodeList {
    private Vector v = null;

    public VectorNodeList(Vector v) {
        this.v = v;
    }

    public Vector getVector() {
        return v;
    }
    /**
     * Returns the <code>index</code>th item in the collection. If
     * <code>index</code> is greater than or equal to the number of nodes in
     * the list, this returns <code>null</code>.
     * @param index Index item into the collection.
     * @return The node at the <code>index</code>th position in the
     *   <code>NodeList</code>, or <code>null</code>, if that index is not a
     *   valid.
     */
    public Node item(int index) {
        return (Node)v.elementAt(index);
    }
    /**
     * The number of nodes in the list. The range of valid child node indices is
     * 0 to <code>length-1</code>, inclusive.
     */
    public int getLength() {
        return v.size();
    }
}
