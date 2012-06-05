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
<h3>jUDDI State & Configuration</h3>

<h4>jUDDI Properties</h4>
<pre>
<%
	Properties almsProps = Config.getProperties();
	Enumeration almsPropEnum = almsProps.keys();
	if ((almsPropEnum != null) && (almsPropEnum.hasMoreElements()))
	{
    while (almsPropEnum.hasMoreElements())
    {
        String name = (String)almsPropEnum.nextElement();
        out.println(name + ": " + almsProps.getProperty(name));
    }
	}
%>
</pre>


<h4>System Properties</h4>
<pre>
<%
	Properties sysProps = System.getProperties();
	Enumeration sysPropEnum = sysProps.keys();
	if ((sysPropEnum != null) && (sysPropEnum.hasMoreElements()))
	{
		while (sysPropEnum.hasMoreElements())
		{
			String name = (String)sysPropEnum.nextElement();
			out.println(name + ": " + sysProps.getProperty(name));
		}
	}
%>
</pre>


</div>
</td>
</tr>
</table>
</html>