<%@ page language="java" contentType="text/html; charset=ISO-8859-1"
    pageEncoding="ISO-8859-1"%>
<!DOCTYPE html PUBLIC "-//W3C//DTD HTML 4.01 Transitional//EN" "http://www.w3.org/TR/html4/loose.dtd">
<html>
<head>
<meta http-equiv="Content-Type" content="text/html; charset=ISO-8859-1">
<title>Adit Monitoring Application</title>
</head>

<%  
	String status2 = (String) request.getAttribute("test");
%>

<body>
	<h1>Adit Monitoring</h1>
	
	<table>
		<tr>
			<td>Component</td>
			<td>Status</td>
			<td>Time / Error</td>
		</tr>
		<tr>
			<td>Komponent X</td>
			<td></td>
			<td><%=status2%></td>
		</tr>
	</table>
	
</body>
</html>