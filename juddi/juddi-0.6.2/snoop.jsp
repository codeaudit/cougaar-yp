<%@ page import="org.juddi.util.Config,
                 java.io.Reader,
                 java.util.Set,
                 java.util.Iterator" %>
<%@ page import="java.io.IOException" %>
<%@ page import="java.io.PrintWriter" %>
<%@ page import="java.util.Enumeration" %>
<%@ page import="java.util.Properties" %>
<%@ page import="javax.servlet.ServletException" %>
<%@ page import="javax.servlet.http.HttpServlet" %>
<%@ page import="javax.servlet.http.HttpServletRequest" %>
<%@ page import="javax.servlet.http.HttpServletResponse" %>


<html>
<head>
<title>jUDDI</title>
<link rel="stylesheet" href="juddi.css">
</head>

<div class="nav" align="right"><font size="-2"><a href="http://www.juddi.org/">jUDDI.org</a></font></div>
<h1>jUDDI</h1>
<table cellspacing="10">
<tr>

<td valign="top" class="side" nowrap width="100">
<A href="index.html">Overview</A><br>
<A href="guide.html">User's Guide</A><br>
<A href="state.jsp">Current State</A><br>
<A href="license.html">License</A><br>
</td>

<td valign="top" width="100%" height="100%">
<div class="announcement">
<p>
<h3>jUDDI Request</h3>


<h4>HTTP Request Attributes</h4>
<pre>
<%
  out.println("Request method:     " + request.getMethod());
	out.println("Request URI:        " + request.getRequestURI());
	out.println("Request protocol:   " + request.getProtocol().trim());
	out.println("Servlet path:       " + request.getServletPath());
	out.println("Path info:          " + request.getPathInfo());
	out.println("Path translated:    " + request.getPathTranslated());
	out.println("Query string:       " + request.getQueryString());
	out.println("Content length:     " + request.getContentLength());
	out.println("Content type:       " + request.getContentType());
	out.println("Server name:        " + request.getServerName());
	out.println("Server port:        " + request.getServerPort());
	out.println("Remote user:        " + request.getRemoteUser());
	out.println("Remote address:     " + request.getRemoteAddr());
	out.println("Remote host:        " + request.getRemoteHost());
	out.println("Authorization type: " + request.getAuthType());
%>
</pre>


<h4>HTTP Request Headers</h4>
<pre>
<%
	Enumeration headerEnum = request.getHeaderNames();
	if ((headerEnum != null) && (headerEnum.hasMoreElements()))
	{
		while (headerEnum.hasMoreElements())
		{
			String name = (String)headerEnum.nextElement();
			out.println(name + ": " + request.getHeader(name));
		}
	}
%>
</pre>


<h4>HTTP Request Payload</h4>
<pre>
<%
	// only try to write this if request method was post!
	if (request.getMethod().equalsIgnoreCase("post"))
	{
		out.println();
		out.println("<h4>Request Payload (http \"post\" method only)</h4>");
		out.println("<pre>");
		Reader requestReader = request.getReader();
		int contentLength = request.getContentLength();
		char[] payload = new char[contentLength];
		int offset = 0;

		while (offset < contentLength)
		offset += requestReader.read(payload,offset,contentLength - offset);

		out.println(payload);
	}
	else
	{
		out.println("(valid for HTTP \"POST\" request method only)");
	}
%>
</pre>


</div>
</td>
</tr>
</table>
</html>