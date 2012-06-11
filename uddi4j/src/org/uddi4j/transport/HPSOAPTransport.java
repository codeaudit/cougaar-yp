/*
 * The source code contained herein is licensed under the IBM Public License
 * Version 1.0, which has been approved by the Open Source Initiative.
 * Copyright (C) 2001, Hewlett-Packard Company
 * All Rights Reserved.
 *
 */

package org.uddi4j.transport;

import org.uddi4j.UDDIElement;
import java.net.URL;
import java.net.MalformedURLException;
import java.util.Vector;
import java.io.PrintStream;
import java.io.FileOutputStream;
import java.io.File;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.Date;
import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.uddi4j.UDDIException;
import com.hp.soap.client.SoapClient;
import com.hp.soap.client.ClientMessage;
import com.hp.soap.client.SoapBody;
import com.hp.soap.client.SoapException;
import com.hp.soap.xml.XMLUtil;


/**
 * Implementation for a SOAP transport to be used by UDDI4J.
 * This uses HP SOAP as the underlying transport.
 * The following system properties are used:
 * <UL>
 * <LI>org.uddi4j.TransportClassName = full class name of Transport
 * implementation class. In this case, it is
 * org.uddi4j.transport.HPSOAPTransport</LI>
 * <LI>http.proxyHost  = Hostname of http proxy</LI>
 * <LI>https.proxyHost = Hostname of https proxy</LI>
 * <LI>http.proxyPort  = Portname of http proxy</LI>
 * <LI>https.proxyPort = Portname of https proxy</LI>
 * <LI>org.uddi4j.logEnabled = (true/false) Turns logging of messages on or
 * off. This can be changed at the runtime, and will be reloaded.</LI>
 * <LI>hpsoap.logFileName    = name of the log file. In case this property is
 * missing or there isnt write permission in the directory, log messages are sent
 * to System.out. The file is created in case the file does not exist.</LI>
 * <LI>hpsoap.logDirectory   = directory containing the log file.
 * In case this property is missing or a wrong directory is specified, the file
 * is assumed to be  in the current working directory. The directory is *NOT*
 * created if it is not present.</LI>
 * </UL>
 *
 * @author Ravi Trivedi (ravi_trivedi@hp.com)
 */
public class HPSOAPTransport implements Transport {
    boolean debug = true;
    boolean useProxy = false;
    URL httpProxy = null;
    URL httpsProxy = null;
    PrintStream print = System.out;
    public SimpleDateFormat LOG_DATE_FORMAT =
        new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");


    public HPSOAPTransport() {
        File file = null;
        String proxyHostString = System.getProperty("http.proxyHost");
        String proxyPortString = System.getProperty("http.proxyPort");
        String secureProxyHostString = System.getProperty("https.proxyHost");
        String secureProxyPortString = System.getProperty("https.proxyPort");

        try {
            if ((proxyPortString != null) && (proxyHostString != null)) {
                httpProxy = new URL("http", proxyHostString,
                    Integer.parseInt(proxyPortString), "");
                useProxy = true;
            } else if ((secureProxyPortString != null)
                         && (secureProxyHostString != null)) {
                httpsProxy = new URL("https", secureProxyHostString,
                                      Integer.parseInt(secureProxyPortString), "");
                useProxy = true;
            }
        } catch(MalformedURLException mue) {
            System.out.println("Invalid URL for proxy");
            mue.printStackTrace();
            useProxy = false;
        }

        try {
            String isDebug = System.getProperty("org.uddi4j.logEnabled");
            if ((isDebug != null) && (isDebug.equalsIgnoreCase("true"))) {
                debug = true;
            } else {
                debug = false;
            }
	    // Open the file even if logEnabled is false. This enables users
	    // to turn on debug mid-program.
            String fileName = System.getProperty("hpsoap.logFileName");
            String dirName  = System.getProperty("hpsoap.logDirectory");
            if ((fileName != null) && !(fileName.trim().equals (""))) {
               if ((dirName != null) && !(dirName.trim().equals (""))) {
                  File dir = new File(dirName);
                  if (dir.exists()) {
                         fileName = dirName + File.separator + fileName;
                  }
               }
               print = new PrintStream(new FileOutputStream(fileName, true), true);
               System.out.println ("Log file set to " + fileName + "\n");
            } else {  // fileName is null or empty
               System.out.println ("Logging set to command window\n");
            }
        } catch (Exception e) {
            e.printStackTrace();
            print = System.out;
        }
    }


    /**
     * Sends a UDDIElement to either the inquiry or publish URL.
     *
     * @param el Element to send.
     * @param inquiry Destination URL
     * @return An element representing a XML DOM tree containing the UDDI
     * response.
     * @exception SOAPException
     */
    public Element send(UDDIElement el, URL url) throws TransportException {
        Element base = null;

        try {
            DocumentBuilder docBuilder =
                DocumentBuilderFactory.newInstance().newDocumentBuilder();

            base = docBuilder.newDocument().createElement("SOAP:Body");
        } catch (Exception e) {
            e.printStackTrace();
            print.println("FATAL error : bailing out");
            System.exit(-1);
        }
        el.saveToXML(base);
        return send((Element)base, url);
    }


    /**
     * Sends an XML DOM tree, identified by the given element, to either the
     * inquiry or publish URL.
     *
     * @param el Element to send.
     * @param inquiry Destination URL
     * @return An element representing a XML DOM tree containing the UDDI
     * response.
     * @exception SOAPException
     */
    public Element send(Element el, URL url) throws TransportException {
        Element base = null;
        Node node = null;
        SoapClient client = null;
        ClientMessage response = null;
        String trace = System.getProperty("org.uddi4j.logEnabled");

        try {
            if (useProxy) {
                if (url.getProtocol().equalsIgnoreCase("HTTP")) {
                    client = new SoapClient(url, httpProxy);
                } else {
                    client = new SoapClient(url, httpsProxy);
                }
            } else {
                client = new SoapClient(url);
            }
            if ((trace != null) && (trace.equalsIgnoreCase("true"))) {
                print.println("[" + LOG_DATE_FORMAT.format(new Date()) + "]\n");
                client.setLogStream(print);
            } else {
                client.setLogStream(null);
            }
            ClientMessage request = new ClientMessage();

            // Fix to remove the SOAP:encodingStyle namespace from the envelope.
            // UDDI API Spec Version 2 Appendix B, section 6.3, specifies
            // that UDDI does not support SOAP Encoding.
            request.getEnvelope().removeAttribute("SOAP:encodingStyle");
            SoapBody body = new SoapBody(el);

            request.setBody(body);
            response = client.sendRequest(request);
            if (response != null) {
                if (response.getBody() != null) {
                    node =
                        ((Node)(response.getBody().getElement()).getFirstChild());
                } else {
                    print.println("INFO:The SOAP Body of the response obtained from Server is null");
                }
                base = (Element)node;
            } else {
                print.println("INFO:The SOAP Response from Server is null");
            }
        } catch (SoapException e) {
            String faultString = e.getMessage();
            Element ele = null;

            try {
                ele = XMLUtil.stringToElement(faultString);
            } catch (Exception exe) {
                throw new TransportException(exe);
            }
            return ele;
        } catch (IOException ioe) {
            throw new TransportException(ioe);
        }
        return base;
    }
}
