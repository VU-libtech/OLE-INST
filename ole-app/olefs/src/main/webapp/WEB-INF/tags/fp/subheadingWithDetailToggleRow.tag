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
<%@ include file="/jsp/sys/kfsTldHeader.jsp"%>

<%@ attribute name="columnCount" required="true"
    description="Total number of columns in the accounting lines table,
    to be spanned by this row." %>
<%@ attribute name="subheading" required="true"
    description="Tab subheading to display, typically redundant with the tab heading." %>
<tr>
  <td colspan="${columnCount}" class="subhead">
    <span class="subhead-left">${subheading}</span>
    <span class="subhead-right">
      <html:hidden name="KualiForm" property="hideDetails"/>
      <c:if test="${!empty KualiForm.hideDetails}">
        <c:set var="toggle" value="${KualiForm.hideDetails ? 'show' : 'hide'}"/>
        <html:image property="methodToCall.${toggle}Details" src="${ConfigProperties.kr.externalizable.images.url}det-${toggle}.gif"
                    alt="${toggle} transaction details" title="${toggle} transaction details" styleClass="tinybutton"/>
      </c:if>
    </span>
  </td>
</tr>
