/*
 * jUDDI - An open source Java implementation of UDDI v2.0
 * http://juddi.org/
 *
 * Copyright (c) 2002, InflexionPoint and contributors
 * All rights reserved.
 */

package org.juddi.util;

import org.uddi4j.util.FindQualifier;
import org.uddi4j.util.FindQualifiers;

/**
 * Wraps an instance of FindQualifiers for more granular access to the
 * qualifier values.
 *
 * @author Steve Viens (steve@inflexionpoint.com)
 * @version 0.6.2
 */
public class SearchQualifiers
{
  private FindQualifiers qualifiers;

  public boolean exactNameMatch;
  public boolean caseSensitiveMatch;
  public boolean orAllKeys;
  public boolean orLikeKeys;
  public boolean andAllKeys;
  public boolean sortByNameAsc;
  public boolean sortByNameDesc;
  public boolean sortByDateAsc;
  public boolean sortByDateDesc;
  public boolean serviceSubset;
  public boolean combineCategoryBags;

  /**
   *
   */
  public SearchQualifiers(FindQualifiers qualifiers)
  {
    this.qualifiers = qualifiers;

    if (qualifiers != null)
    {
      for (int i=0; i<qualifiers.size(); i++)
      {
        FindQualifier qualifier = (FindQualifier)qualifiers.get(i);
        String qValue = qualifier.getText();

        if (qValue.equals(FindQualifier.exactNameMatch))
          exactNameMatch = true;
        else if (qValue.equals(FindQualifier.caseSensitiveMatch))
          caseSensitiveMatch = true;
        else if (qValue.equals(FindQualifier.orAllKeys))
          orAllKeys = true;
        else if (qValue.equals(FindQualifier.orLikeKeys))
          orLikeKeys = true;
        else if (qValue.equals(FindQualifier.andAllKeys))
          andAllKeys = true;
        else if (qValue.equals(FindQualifier.sortByNameAsc))
          sortByNameAsc = true;
        else if (qValue.equals(FindQualifier.sortByNameDesc))
          sortByNameDesc = true;
        else if (qValue.equals(FindQualifier.sortByDateAsc))
          sortByDateAsc = true;
        else if (qValue.equals(FindQualifier.sortByDateDesc))
          sortByDateDesc = true;
        else if (qValue.equals(FindQualifier.serviceSubset))
          serviceSubset = true;
        else if (qValue.equals(FindQualifier.combineCategoryBags))
          combineCategoryBags = true;
      }
    }
  }

  /**
   *
   */
  public FindQualifiers getFindQualifiers()
  {
    return this.qualifiers;
  }
}
