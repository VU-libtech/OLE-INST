<%--
 Copyright 2005-2009 The Kuali Foundation
 
 Licensed under the Educational Community License, Version 2.0 (the "License");
 you may not use this file except in compliance with the License.
 You may obtain a copy of the License at
 
 http://www.opensource.org/licenses/ecl2.php
 
 Unless required by applicable law or agreed to in writing, software
 distributed under the License is distributed on an "AS IS" BASIS,
 WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 See the License for the specific language governing permissions and
 limitations under the License.
--%>
<%@ include file="/jsp/sys/kfsTldHeader.jsp"%><%--

 --%><%@ attribute name="url" required="true" %>
<%@ attribute name="title" required="true" %>
<%@ attribute name="displayTitle" required="false" %>
<%@ attribute name="prefix" required="false" %>

<c:set var="backdoorPortalUrlAddition" value="" />
<c:set var="backdoorMainUrlAddition" value="" />
<c:if test="${UserSession.backdoorInUse}">
	<%-- Can't add this.  If on the main (portal) request, it assumes this was a
	 backdoor login request and appends an additional parameter which causes some forms to blow 
	<c:set var="backdoorPortalUrlAddition" value="&backdoorId=${UserSession.principalName}" />
	 --%>
	<c:choose>
		<c:when test="${fn:contains(url,'?')}">
			<c:set var="backdoorMainUrlAddition" value="&backdoorId=${UserSession.principalName}" />
		</c:when>
		<c:otherwise>
			<c:set var="backdoorMainUrlAddition" value="?backdoorId=${UserSession.principalName}" />
		</c:otherwise>
	</c:choose>
</c:if>

<c:if test="${displayTitle}" >
  <a class="portal_link" href="${prefix}oleportal.do?channelTitle=${title}&channelUrl=${url}${backdoorMainUrlAddition}"  title="${title}">${title}</a>
</c:if>
<c:if test="${! displayTitle}" >
  <a class="portal_link" href="${prefix}oleportal.do?channelTitle=${title}&channelUrl=${url}${backdoorMainUrlAddition}" title="${title}"><jsp:doBody/></a>
</c:if>
