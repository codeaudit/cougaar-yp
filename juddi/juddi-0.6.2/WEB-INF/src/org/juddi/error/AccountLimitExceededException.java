/*
 * jUDDI - An open source Java implementation of UDDI v2.0
 * http://juddi.org/
 *
 * Copyright (c) 2002, InflexionPoint and contributors
 * All rights reserved.
 */

package org.juddi.error;

import org.uddi4j.response.ErrInfo;
import org.uddi4j.response.Result;

/**
 * Thrown to indicate that a UDDI Exception was encountered.
 *
 * @author Steve Viens (steve@inflexionpoint.com)
 * @version 0.6.2
 */
public class AccountLimitExceededException extends JUDDIException
{
  public AccountLimitExceededException()
  {
    this(ResultCodes.E_ACCOUNT_LIMIT_EXCEEDED_MSG);
  }

  public AccountLimitExceededException(String msg)
  {
    super(msg);

    ErrInfo errInfo = new ErrInfo();
    errInfo.setErrCode(ResultCodes.E_ACCOUNT_LIMIT_EXCEEDED_CODE);
    errInfo.setText(ResultCodes.E_ACCOUNT_LIMIT_EXCEEDED_MSG);

    Result result = new Result();
    result.setErrno(ResultCodes.E_ACCOUNT_LIMIT_EXCEEDED);
    result.setErrInfo(errInfo);

    this.setFaultActor("");
    this.setFaultCode("Client");
    this.setFaultString(msg);
    this.addResult(result);
  }
}