<%@ page language="java" contentType="text/html; charset=ISO-8859-1" pageEncoding="ISO-8859-1"%>
<%@ page isELIgnored="false" %>
<%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core" %>
<%@ taglib prefix="sec" uri="http://www.springframework.org/security/tags"%>

<html>

<head>
	<meta http-equiv="Content-Type" content="text/html; charset=ISO-8859-1">
	<title>Auto List</title>
	<link href="<c:url value='/static/css/bootstrap.css' />" rel="stylesheet"></link>
	<link href="<c:url value='/static/css/app.css' />" rel="stylesheet"></link>
</head>

<body>
	<div class="generic-container">
		<%@include file="authheader.jsp" %>	
		<div class="panel panel-default">
			  <!-- Default panel contents -->
		  	<div class="panel-heading"><span class="lead">List of Prenotazioni </span></div>
			<table class="table table-hover">
	    		<thead>
		      		<tr>
				        <th>Targa</th>
				        <th>Username</th>
				        <th>Data di inizio</th>
				        <th>Data di fine</th>
						<th>Approvazione</th>
						<th width="100"></th>
						<th width="100"></th>
					</tr>
		    	</thead>
	    		<tbody>
				<c:forEach items="${prenotaziones}" var="prenotazione">
					<tr>
						<td>${prenotazione.targa}</td>
						<td>${prenotazione.username}</td>
						<td>${prenotazione.dataDiInizio}</td>
						<td>${prenotazione.dataDiFine}</td>
						<td>${prenotazione.approvata}</td>
						<sec:authorize access="hasRole('CUSTOMER')">
							<td><a href="<c:url value='/edit-prenotazione-${prenotazione.id}' />" class="btn btn-success custom-width">edit</a></td>
				        </sec:authorize>
				        <sec:authorize access="hasRole('ROLE_CUSTOMER')">
							<td><a href="<c:url value='/delete-prenotazione-${prenotazione.id}' />" class="btn btn-danger custom-width">delete</a></td>
        				</sec:authorize>
						<sec:authorize access="hasRole('ADMIN')">
							<td><a href="<c:url value='/approve-prenotazione-${prenotazione.id}' />" class="btn btn-success custom-width">approva</a></td>
						</sec:authorize>
						<sec:authorize access="hasRole('ADMIN')">
							<td><a href="<c:url value='/disapprove-prenotazione-${prenotazione.id}' />" class="btn btn-danger btn-block">disapprova</a></td>
						</sec:authorize>
					</tr>
				</c:forEach>
	    		</tbody>
	    	</table>
		</div>
		 	<div class="well">
		 		<a href="<c:url value='/newprenotazione' />">Add New Prenotazione</a>
		 	</div>
   	</div>
</body>
</html>