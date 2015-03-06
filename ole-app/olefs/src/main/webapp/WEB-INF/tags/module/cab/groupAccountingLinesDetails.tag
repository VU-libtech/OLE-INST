<%--
 Copyright 2007-2009 The Kuali Foundation
 
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
<%@ attribute name="capitalAssetInformation" required="true" type="java.lang.Object" description="The group accounting lines for each capital asset that will be shown"%>
<c:set var="groupAccountingLinesAttributes" value="${DataDictionary.CapitalAssetAccountsGroupDetails.attributes}" />
<%@ attribute name="capitalAssetPosition" required="true" description="The index of the CAB Capital Asset"%>
<%@ attribute name="showViewButton" required="true" description="To show the view/hide button for payments"%>

<c:set var="docPos" value="0" />
<c:set var="linePos" value="0" />

<c:set var="docPos" value="${docPos+1}" />

<div class="tab-container" align="center">
<table width="80%" cellpadding="0" cellspacing="0" class="datatable"  align="center"
	       style="width: 85%; text-align: left;">	
	<tr>
		<td class="tab-subhead"  width="100%" colspan="11">Accounting Lines Amount Distributions</td>
	</tr>
	<tr>
		<c:set var="tabKey" value="payment-${capitalAssetPosition}"/>
		<html:hidden property="tabStates(${tabKey})" value="CLOSE" />
		<td colspan="7" style="padding:0px; border-style:none;">
		<table class="datatable" cellpadding="0" cellspacing="0" align="center"
	       style="width: 100%; text-align: left;">
	       <c:if test="${showViewButton == true}" >
				<tr>
					<td colspan="7" class="tab-subhead" style="border-right: medium none;">
					<html:image src="${ConfigProperties.kr.externalizable.images.url}tinybutton-show.gif"
			                                    property="methodToCall.toggleTab.tab${tabKey}"
			                                    title="toggle"
			                                    alt="show"
			                                    styleClass="tinybutton"
			                                    styleId="tab-${tabKey}-imageToggle"
			                                    onclick="javascript: return toggleTab(document, '${tabKey}'); "/>
			            Payments
					</td>
				</tr>
			<tbody  style="display: none;" id="tab-${tabKey}-div">
 		</c:if>			
			<tr align="center">
				<kul:htmlAttributeHeaderCell
					attributeEntry="${groupAccountingLinesAttributes.capitalAssetAccountLineNumber}"
					useShortLabel="true" />
				<kul:htmlAttributeHeaderCell
					attributeEntry="${groupAccountingLinesAttributes.sequenceNumber}"
					useShortLabel="true" />
				<kul:htmlAttributeHeaderCell
					attributeEntry="${groupAccountingLinesAttributes.financialDocumentLineTypeCode}"
					useShortLabel="true" />
				<kul:htmlAttributeHeaderCell
					attributeEntry="${groupAccountingLinesAttributes.chartOfAccountsCode}"
					useShortLabel="true"
					hideRequiredAsterisk="true" />
				<kul:htmlAttributeHeaderCell
					attributeEntry="${groupAccountingLinesAttributes.accountNumber}"
					useShortLabel="false" />
				<kul:htmlAttributeHeaderCell
					attributeEntry="${groupAccountingLinesAttributes.financialObjectCode}"
					useShortLabel="true" />
				<kul:htmlAttributeHeaderCell
					attributeEntry="${groupAccountingLinesAttributes.amount}"
					useShortLabel="true" />
			</tr>
			<c:forEach items="${capitalAssetInformation.capitalAssetAccountsGroupDetails}" var="accountLine" >
				<tr>
					<td class="infoline" align="center">
						<div align="center" valign="middle">
							${accountLine.capitalAssetAccountLineNumber}&nbsp;
						</div></td>
					<td class="infoline">
						<div align="center" valign="middle">
							${accountLine.sequenceNumber}&nbsp;
						</div></td>
					<td class="infoline">
						<div>
				        	<c:set var="lineType" value="${accountLine.financialDocumentLineTypeCode}" />
				            <c:if test="${lineType eq OLEConstants.SOURCE_ACCT_LINE_TYPE_CODE}">
					        	<c:out value="${OLEConstants.SOURCE}" />
					        </c:if>
				            <c:if test="${lineType eq OLEConstants.TARGET_ACCT_LINE_TYPE_CODE}">
					            	<c:out value="${OLEConstants.TARGET}" />
					        </c:if>
				       </div></td>					
					<td class="infoline">${accountLine.chartOfAccountsCode}&nbsp;</td>
					<td class="infoline">${accountLine.accountNumber}&nbsp;</td>
					<td class="infoline">${accountLine.financialObjectCode}&nbsp;</td>
						
					<td class="infoline">
						<div align="right" valign="middle">
							${accountLine.amount}&nbsp;</div></td>
				</tr>
			</c:forEach>
			</tbody>
		</table>
		</td>
	</tr>
</table>
</div>