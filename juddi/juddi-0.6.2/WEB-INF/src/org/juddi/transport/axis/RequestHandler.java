/*
 * jUDDI - An open source Java implementation of UDDI v2.0
 * http://juddi.org/
 *
 * Copyright (c) 2002, InflexionPoint and contributors
 * All rights reserved.
 */

package org.juddi.transport.axis;

import org.juddi.error.JUDDIException;
import org.juddi.service.ServiceFactory;
import org.juddi.service.UDDIService;

import org.apache.axis.AxisFault;
import org.apache.axis.Message;
import org.apache.axis.MessageContext;
import org.apache.axis.message.SOAPEnvelope;
import org.apache.axis.message.SOAPBodyElement;
import org.apache.axis.message.SOAPFaultElement;
import org.apache.log4j.Logger;
import org.uddi4j.response.DispositionReport;
import org.uddi4j.UDDIElement;
import org.w3c.dom.Document;
import org.w3c.dom.Element;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;

/**
 * @author  Alex Ceponkus (alex@inflexionpoint.com)
 * @author Steve Viens (steve@inflexionpoint.com)
 * @version 0.6.2
 */
public class RequestHandler extends org.apache.axis.handlers.BasicHandler
{
  // private reference to the jUDDI logger
  private static Logger log = Logger.getLogger(RequestHandler.class);

  // create an XML document builder factory
  private static DocumentBuilderFactory factory = DocumentBuilderFactory.newInstance();

  /**
   *
   */
  public void invoke(MessageContext msgContext)
    throws org.apache.axis.AxisFault
  {
    try
    {
      // 1. pull the request element out of the SOAP envelope
      Message msg = msgContext.getRequestMessage();
      SOAPEnvelope reqSoapEnv = msg.getSOAPEnvelope();
      Element reqSoapBody = reqSoapEnv.getFirstBody().getAsDOM();

      // 2. build a UDDI4j request object from the contents of the SOAP body
      UDDIElement request = (UDDIElement)RequestFactory.getRequest(reqSoapBody);

      // 3. obtain correct jUDDI service object (using UDDI4j request class name)
      UDDIService service = ServiceFactory.getService(request.getClass().getName());

      // 4. build up a response element for UDDI4j to 'saveToXML()' into
      DocumentBuilder builder = factory.newDocumentBuilder();
      Document document = builder.newDocument();
      Element holder = document.createElement("holder");
      document.appendChild(holder);  // holder element is thrown away
      Element response = document.getDocumentElement();

      // 5. invoke jUDDI service and save response to the DOM element
      service.invoke(request).saveToXML(response);

      // 6. build SOAP response
      SOAPBodyElement respSoapBody = new SOAPBodyElement((Element)response.getChildNodes().item(0));
      SOAPEnvelope respSoapEnv = new SOAPEnvelope();
      respSoapEnv.addBodyElement(respSoapBody);

      // 7. let Axis know what the response is
      msgContext.setResponseMessage(new Message(respSoapEnv));
    }
    catch(JUDDIException juddiex)
    {
      AxisFault fault = AxisFault.makeFault(juddiex);
      fault.setFaultActor(juddiex.getFaultActor());
      fault.setFaultCode(juddiex.getFaultCode());
      fault.setFaultString(juddiex.getFaultString());

      try
      {
        // Check to see if there's a DispositionReport available
        // with this exception. If so then we need to grab it and
        // stuff it into the SOAPFault's detail element.
        DispositionReport dispRpt = juddiex.getDispositionReport();
        if (dispRpt != null)
        {
          // Build up a response element for UDDI4j to 'saveToXML()' into
          DocumentBuilder builder = factory.newDocumentBuilder();
          Document document = builder.newDocument();
          Element holder = document.createElement("holder");
          document.appendChild(holder);  // holder element is thrown away
          Element faultDetail = document.getDocumentElement();

          // Stuff the disposition report into SOAPFault's detail element
          dispRpt.saveToXML(faultDetail);
          Element[] elarray = new Element[1];
          elarray[0] = (Element)faultDetail.getChildNodes().item(0);
          fault.setFaultDetail(elarray);
        }
      }
      catch(ParserConfigurationException pcex)
      {
        log.error("Difficulty creating a new javax.xml DocumentBuilder",pcex);
      }

      throw fault;
    }
    catch(Exception ex)
    {
      AxisFault fault = AxisFault.makeFault(ex);
      fault.setFaultActor("");
      fault.setFaultCode("");
      fault.setFaultString(ex.getMessage());

      throw fault;
    }
  }
}