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
 * (NOT USED IN UDDI versions 1.0 or 2.0)
 *
 * @author Steve Viens (steve@inflexionpoint.com)
 * @version 0.6.2
 */
public class InvalidURLPassedException extends JUDDIException
{
  public InvalidURLPassedException()
  {
    this(ResultCodes.E_INVALID_URL_PASSED_MSG);
  }

  public InvalidURLPassedException(String msg)
  {
    super(msg);

    ErrInfo errInfo = new ErrInfo();
    errInfo.setErrCode(ResultCodes.E_INVALID_URL_PASSED_CODE);
    errInfo.setText(ResultCodes.E_INVALID_URL_PASSED_MSG);

    Result result = new Result();
    result.setErrno(ResultCodes.E_INVALID_URL_PASSED);
    result.setErrInfo(errInfo);

    this.setFaultActor("");
    this.setFaultCode("Client");
    this.setFaultString(msg);
    this.addResult(result);
  }
}