/*
 * jUDDI - An open source Java implementation of UDDI v2.0
 * http://juddi.org/
 *
 * Copyright (c) 2002, InflexionPoint and contributors
 * All rights reserved.
 */

package org.juddi.error;

import org.juddi.util.Config;

import org.uddi4j.response.ErrInfo;
import org.uddi4j.response.DispositionReport;
import org.uddi4j.response.Result;

import java.util.Vector;

/**
 * Thrown to indicate that a UDDI Exception was encountered.
 *
 * @author Steve Viens (steve@inflexionpoint.com)
 * @version 0.6.2
 */
public class JUDDIException extends Exception
{
  // private reference to the name of this UDDI Operator Site
  private static String operator = Config.getProperty("org.juddi.operatorName");

  // SOAP SOAPFault Actor
  private String faultActor;

  // SOAP SOAPFault Code
  private String faultCode;

  // SOAP SOAPFault SOAPMessage
  private String faultString;

  // Vector of UDDI Result instances
  private Vector results = new Vector();

  /**
   * Constructs a <code>JUDDIException</code> instance.
   */
  public JUDDIException(Exception ex)
  {
    super(ex.getMessage());

//    this.setFaultActor();
//    this.setFaultCode();
    this.setFaultString(ex.getMessage());
  }

  /**
   * Constructs a <code>JUDDIException</code> instance and initializes the
   * exceptions DisposotionReport with a more detailed message than those
   * from the UDDI Programmers API, Appendix A.
   * @param msg an more specific message describing to the exception
   */
  public JUDDIException(String msg)
  {
    super(msg);

//    this.setFaultActor();
//    this.setFaultCode();
    this.setFaultString(msg);
  }

  /**
   * Sets the fault actor of this SOAP SOAPFault to the given value.
   * @param actor The new actor value for this SOAP SOAPFault.
   */
  public void setFaultActor(String actor)
  {
    this.faultActor = actor;
  }

  /**
   * Returns the fault actor of this SOAP SOAPFault.
   * @return The fault actor of this SOAP SOAPFault.
   */
  public String getFaultActor()
  {
    return this.faultActor;
  }

  /**
   * Sets the fault code of this SOAP SOAPFault to the given value.
   * @param code The new code number for this SOAP SOAPFault.
   */
  public void setFaultCode(String code)
  {
    this.faultCode = code;
  }

  /**
   * Returns the fault code of this SOAP SOAPFault.
   * @return The fault code of this SOAP SOAPFault.
   */
  public String getFaultCode()
  {
    return this.faultCode;
  }

  /**
   * Sets the fault string of this SOAP SOAPFault to the given value.
   * @param value The new fault string for this SOAP SOAPFault.
   */
  public void setFaultString(String value)
  {
    this.faultString = value;
  }

  /**
   * Returns the fault string of this SOAP SOAPFault.
   * @return The fault string of this SOAP SOAPFault.
   */
  public String getFaultString()
  {
    return this.faultString;
  }

  /**
   * Adds a result instance to this Exception. Multiple result objects
   * may exist within a DispositionReport
   */
  public void addResult(Result result)
  {
    if (this.results==null)
      this.results = new Vector();
    this.results.add(result);
  }

  /**
   * Returns the disposition report associated with this jUDDI exception. It
   * uses the results Vector to determine if a disposition report is present
   * and should be returned.
   * @return The disposition report associated with this jUDDI exception.
   */
  public DispositionReport getDispositionReport()
  {
    DispositionReport dispRpt = null;
    
    // if the results vector is empty then there's not DispositionReport
    if ((results != null) && (results.size() > 0))
    {
      // create a UDDI DispositionReport
      dispRpt = new DispositionReport();
      dispRpt.setGeneric(DispositionReport.GENERIC);
      dispRpt.setOperator(JUDDIException.operator);
      dispRpt.setResultVector(this.results);
    }

    return dispRpt;
  }

//  /**
//   *
//   */
//  public String toString()
//  {
//    StringBuffer buff = new StringBuffer(100);
//
//    buff.append("JUDDIException: "+getMessage()+"\n");
//    buff.append(" SOAPFault Actor: "+getFaultActor()+"\n");
//    buff.append(" SOAPFault Code: "+getFaultCode()+"\n");
//    buff.append(" SOAPFault String: "+getFaultString()+"\n");
//
//    // pull the DispositionReport out if it's present
//    DispositionReport dispRpt = getDispositionReport();
//    if (dispRpt != null)
//    {
//      buff.append(" Generic: "+dispRpt.getGeneric()+"\n");
//      buff.append(" Operator: "+dispRpt.getOperator()+"\n");
//
//      Vector results = dispRpt.getResults();
//      if ((results != null) && (results.size() > 0))
//      {
//        for (int i=0; i<results.size(); i++)
//        {
//          Result result = (Result)results.elementAt(i);
//          buff.append(" >Errno: "+result.getErrno()+"\n");
//
//          ErrInfo errInfo = result.getErrInfo();
//          buff.append(" >Error Code: "+errInfo.getErrCode()+"\n");
//          buff.append(" >Error Info Text: "+errInfo.getText()+"\n");
//        }
//      }
//      else
//        buff.append("\n >[No Results were present]");
//    }
//    else
//      buff.append("\n [A DispositionReport was not present]");
//
//    return buff.toString();
//  }


  /***************************************************************************/
  /***************************** TEST DRIVER *********************************/
  /***************************************************************************/


  public static void main(String[] args)
    throws JUDDIException
  {        
    ErrInfo errinfo = null;
    Result result = null;

    JUDDIException ex = new JUDDIException("myFaultString");
    ex.setFaultActor("myFaultActor");
    ex.setFaultCode("myFaultCode");
    ex.setFaultString("myFaultString");

    errinfo = new ErrInfo();
    errinfo.setErrCode("E_fatalError");
    errinfo.setText("A serious technical error has occured");
    result = new Result();
    result.setErrno("10500");
    result.setErrInfo(errinfo);
    ex.addResult(result);

    errinfo = new ErrInfo();
    errinfo.setErrCode("E_unsupported");
    errinfo.setText("Feature or API is unsupported");
    result = new Result();
    result.setErrno("10050");
    result.setErrInfo(errinfo);
    ex.addResult(result);

    throw ex;
  }
}