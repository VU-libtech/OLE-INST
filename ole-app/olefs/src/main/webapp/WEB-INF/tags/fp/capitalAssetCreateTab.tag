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
<%@ tag description="render the creare capital tag that contains the given capital asset info"%>

<%@ attribute name="readOnly" required="false" description="Whether the capital asset information should be read only" %>

<c:set var="capitalAssetInfoSize" value="${fn:length(KualiForm.document.capitalAssetInformation)}" />	
<c:set var="defaultOpen" value="false"/>
<c:set var="createdCapitalAssets" value="0"/>

<c:forEach items="${KualiForm.document.capitalAssetInformation}" var="detailLine" varStatus="status">
	<c:if test="${detailLine.capitalAssetActionIndicator == OLEConstants.CapitalAssets.CAPITAL_ASSET_CREATE_ACTION_INDICATOR}">
		<c:set var="createdCapitalAssets" value="${createdCapitalAssets + 1}"/>
	</c:if>
</c:forEach>	

<c:if test="${createdCapitalAssets > 0}" >
	<c:set var="defaultOpen" value="true"/>
</c:if>

<kul:tab tabTitle="${OLEConstants.CapitalAssets.CREATE_CAPITAL_ASSETS_TAB_TITLE}" defaultOpen="${defaultOpen}" tabErrorKey="${OLEConstants.EDIT_CAPITAL_ASSET_INFORMATION_ERRORS}" >
     <div class="tab-container" align="center">
	 	<h3>Create Capital Assets</h3>
		 <table class="datatable" cellpadding="0" cellspacing="0" summary="Capital Asset Information">
		     <c:if test="${createdCapitalAssets <= 0}">
				<tr>
					<td class="datacell" height="50" colspan="5"><div align="center">There are currently no Create Capital Assets entries associated with this Transaction Processing document.</div></td>
				</tr>
			</c:if>
	     	<c:if test="${createdCapitalAssets > 0}">
	     		<tr>
					<td>
		 				<fp:capitalAssetInfo readOnly="${readOnly}"/>
					</td>
				</tr>
	     	</c:if>     
	     </table>
	 </div>	
</kul:tab>	 
