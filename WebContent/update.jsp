<%--
/*
 * Copyright 2014  IBM Corp.
 * Licensed under the Apache License, Version 2.0 (the "License"); you may not use this file except in compliance with the License. You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software distributed under the License is
 * distributed on an "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or
 * implied. See the License for the specific language governing permissions and limitations under the
 * License.
 */
--%>
<%@ page session="false" buffer="none" %>
<%@ page trimDirectiveWhitespaces="true" %>
<%@ page language="java" contentType="text/html; charset=UTF-8" pageEncoding="UTF-8"%>
<%@ taglib uri="WEB-INF/healthCheck.tld" prefix="hc" %>
<%@ taglib uri="http://java.sun.com/jsp/jstl/functions" prefix="fn" %>
<%@ taglib uri="http://java.sun.com/jsp/jstl/core" prefix="c" %>

<%-- Set the library name --%>
<c:choose>
<c:when test="${not empty param.library}">
<c:set var="library" scope="request" value="${fn:escapeXml(param.library)}"/>
</c:when>
<c:otherwise>
<c:set var="library" scope="request" value="Web Content"/>
</c:otherwise>
</c:choose>

<%-- Set the text item name in the give library--%>
<c:choose>
<c:when test="${not empty param.textItem}">
<c:set var="textItem" scope="request" value="${fn:escapeXml(param.textItem)}"/>
</c:when>
<c:otherwise>
<c:set var="textItem" scope="request" value="HeathCheckItem"/>
</c:otherwise>
</c:choose>

<%-- Set the virtual portal context --%>
<c:choose>
<c:when test="${not empty param.vpContext}">
<c:set var="vpContext" scope="request" value="${fn:escapeXml(param.vpContext)}"/>
</c:when>
<c:otherwise>
<c:set var="vpContext" scope="request" value=""/>
</c:otherwise>
</c:choose>
<!DOCTYPE html>
<html>
<head>
<meta http-equiv="Content-Type" content="text/html; charset=UTF-8">
<title> WCM Health Check</title>
</head>
<body>

<c:choose>
<c:when test="${not empty requestScope.vpContext}">
Virtual Portal Context: ${requestScope.vpContext} <br />
</c:when>
<c:otherwise>
Virtual Portal Context: <b>none</b> <br />
</c:otherwise>
</c:choose>

Library: ${requestScope.library} <br />

Health Check item name: ${requestScope.textItem} <br />

<p>Updated Time stamp of the health check item to <b><hc:check library="${requestScope.library}" textItem="${requestScope.textItem}" vpContext="${requestScope.vpContext}" update="true"></hc:check></b></p>
</body>
</html>
