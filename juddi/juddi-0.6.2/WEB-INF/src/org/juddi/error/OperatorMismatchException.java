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
 * (RETIRED - DO NOT USE) See the Programmers API, Appendix A
 * for more information.
 *
 * @author Steve Viens (steve@inflexionpoint.com)
 * @version 0.6.2
 */
public class OperatorMismatchException extends JUDDIException
{
  public OperatorMismatchException()
  {
    this(ResultCodes.E_OPERATOR_MISMATCH_MSG);
  }

  public OperatorMismatchException(String msg)
  {
    super(msg);

    ErrInfo errInfo = new ErrInfo();
    errInfo.setErrCode(ResultCodes.E_OPERATOR_MISMATCH_CODE);
    errInfo.setText(ResultCodes.E_OPERATOR_MISMATCH_MSG);

    Result result = new Result();
    result.setErrno(ResultCodes.E_OPERATOR_MISMATCH);
    result.setErrInfo(errInfo);

    this.setFaultActor("");
    this.setFaultCode("Client");
    this.setFaultString(msg);
    this.addResult(result);
  }
}