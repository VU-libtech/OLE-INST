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

<%@ attribute name="documentAttributes" required="true" type="java.util.Map"
              description="The DataDictionary entry containing attributes for this doc's fields." %>
<%@ attribute name="vendorQuoteAttributes" required="true" type="java.util.Map"
              description="The DataDictionary entry containing attributes for this row's fields." %>
<%@ attribute name="ctr" required="true" description="vendor count"%>
<%@ attribute name="isPurchaseOrderAwarded" required="true" description="has the PO been awarded?" %>
<%@ attribute name="isSysVendor" required="false" description="vendor is from system?" %>
<%@ attribute name="isAwarded" required="false" description="vendor has been awarded?" %>
<%@ attribute name="isTransmitPrintDisplayed" required="false" description="vendor quote is ready to print?" %>
<%@ attribute name="isTrasnmitted" required="false" description="PO transmitted to vendor?" %>
<%@ attribute name="isPdfDisplayedToUserOnce" required="false" description="PDF shown to user at least one time?" %>

<c:set var="tabindexOverrideBase" value="60" />
<c:set var="fullEntryMode" value="${KualiForm.documentActions[Constants.KUALI_ACTION_CAN_EDIT]}" />
<c:set var="poOutForQuote" value="${KualiForm.document.applicationDocumentStatus eq 'Out for Quote'}" />
<c:set var="quoteEditable" value="${poOutForQuote && !isPurchaseOrderAwarded && fullEntryMode}" />

<tr>
	<td colspan="4" class="subhead">
		<span class="subhead-left">Vendor ${ctr + 1}</span>
        <c:if test="${quoteEditable && !isTrasnmitted}">
		<span class="subhead-right">
			<html:image property="methodToCall.deleteVendor.line${ctr}"
					src="${ConfigProperties.kr.externalizable.images.url}tinybutton-delete1.gif"
					alt="delete vendor" title="delete vendor"
					styleClass="tinybutton" />&nbsp;
		</c:if>				
	</td>
</tr>

<tr>
	<th align=right valign=middle class="bord-l-b">
		<div align="right"><kul:htmlAttributeLabel attributeEntry="${vendorQuoteAttributes.vendorName}" /></div>
	</th>
	<td align=left valign=middle class="datacell">
		<kul:htmlControlAttribute 
			attributeEntry="${vendorQuoteAttributes.vendorName}" property="document.purchaseOrderVendorQuote[${ctr}].vendorName" 
			readOnly="${!quoteEditable || isSysVendor}" tabindexOverride="${tabindexOverrideBase + 8}"/>
	</td>	
	<th align=right valign=middle class="bord-l-b">
		<div align="right"><kul:htmlAttributeLabel attributeEntry="${vendorQuoteAttributes.vendorNumber}" /></div>
	</th>
	<td align=left valign=middle class="datacell">
		<c:if test="${not isSysVendor}">N/A</c:if>
		<kul:htmlControlAttribute 
			attributeEntry="${vendorQuoteAttributes.vendorNumber}" property="document.purchaseOrderVendorQuote[${ctr}].vendorNumber" 
			readOnly="true" />
	</td>
</tr>

<tr>
	<th align=right valign=middle class="bord-l-b">
		<div align="right"><kul:htmlAttributeLabel attributeEntry="${vendorQuoteAttributes.vendorLine1Address}" /></div>
	</th>
	<td align=left valign=middle class="datacell">
		<kul:htmlControlAttribute 
			attributeEntry="${vendorQuoteAttributes.vendorLine1Address}" property="document.purchaseOrderVendorQuote[${ctr}].vendorLine1Address" 
			readOnly="${!quoteEditable}" tabindexOverride="${tabindexOverrideBase + 8}"/>		
		<c:if test="${quoteEditable}">
		   <kul:lookup  boClassName="org.kuali.ole.vnd.businessobject.VendorAddress" 
              readOnlyFields="active, vendorHeaderGeneratedIdentifier,vendorDetailAssignedIdentifier" autoSearch="yes"
              lookupParameters="'Y':active,document.purchaseOrderVendorQuote[${ctr}].vendorHeaderGeneratedIdentifier:vendorHeaderGeneratedIdentifier,document.purchaseOrderVendorQuote[${ctr}].vendorDetailAssignedIdentifier:vendorDetailAssignedIdentifier"
              fieldConversions="vendorAddressGeneratedIdentifier:document.purchaseOrderVendorQuote[${ctr}].vendorAddressGeneratedIdentifier,vendorLine1Address:document.purchaseOrderVendorQuote[${ctr}].vendorLine1Address,vendorLine2Address:document.purchaseOrderVendorQuote[${ctr}].vendorLine2Address,vendorCityName:document.purchaseOrderVendorQuote[${ctr}].vendorCityName,vendorStateCode:document.purchaseOrderVendorQuote[${ctr}].vendorStateCode,vendorCountryCode:document.purchaseOrderVendorQuote[${ctr}].vendorCountry.name,vendorFaxNumber:document.purchaseOrderVendorQuote[${ctr}].vendorFaxNumber,vendorAttentionName:document.purchaseOrderVendorQuote[${ctr}].vendorAttentionName,vendorZipCode:document.purchaseOrderVendorQuote[${ctr}].vendorPostalCode"/>
        </c:if>
	</td>
	<th align=right valign=middle class="bord-l-b">
		<div align="right"><kul:htmlAttributeLabel attributeEntry="${vendorQuoteAttributes.vendorPhoneNumber}" /></div>
	</th>
	<td align=left valign=middle class="datacell">
		<kul:htmlControlAttribute 
			attributeEntry="${vendorQuoteAttributes.vendorPhoneNumber}" property="document.purchaseOrderVendorQuote[${ctr}].vendorPhoneNumber" 
			readOnly="${!quoteEditable}" tabindexOverride="${tabindexOverrideBase + 9}"/>
	</td>
</tr>

<tr>
	<th align=right valign=middle class="bord-l-b">
		<div align="right"><kul:htmlAttributeLabel attributeEntry="${vendorQuoteAttributes.vendorLine2Address}" /></div>
	</th>
	<td align=left valign=middle class="datacell">
       	<kul:htmlControlAttribute 
			attributeEntry="${vendorQuoteAttributes.vendorLine2Address}" property="document.purchaseOrderVendorQuote[${ctr}].vendorLine2Address" 
			readOnly="${!quoteEditable}" tabindexOverride="${tabindexOverrideBase + 8}"/>
	</td>
	<th align=right valign=middle class="bord-l-b">
		<div align="right"><kul:htmlAttributeLabel attributeEntry="${vendorQuoteAttributes.vendorFaxNumber}" /></div>
	</th>
	<td align=left valign=middle class="datacell">
		<kul:htmlControlAttribute 
			attributeEntry="${vendorQuoteAttributes.vendorFaxNumber}" property="document.purchaseOrderVendorQuote[${ctr}].vendorFaxNumber" 
			readOnly="${!quoteEditable}" tabindexOverride="${tabindexOverrideBase + 9}"/>
	</td>
</tr>

<tr>
	<th align=right valign=middle class="bord-l-b">
		<div align="right"><kul:htmlAttributeLabel attributeEntry="${vendorQuoteAttributes.vendorCityName}" />/
		<div align="right"><kul:htmlAttributeLabel attributeEntry="${vendorQuoteAttributes.vendorStateCode}" />
	</th>
	<td align=left valign=middle class="datacell">
		<kul:htmlControlAttribute 
			attributeEntry="${vendorQuoteAttributes.vendorCityName}" property="document.purchaseOrderVendorQuote[${ctr}].vendorCityName" 
			readOnly="${!quoteEditable}" tabindexOverride="${tabindexOverrideBase + 8}"/> / 
		<kul:htmlControlAttribute 
			attributeEntry="${vendorQuoteAttributes.vendorStateCode}" property="document.purchaseOrderVendorQuote[${ctr}].vendorStateCode" 
			readOnly="${!quoteEditable}" tabindexOverride="${tabindexOverrideBase + 8}"/>
	</td>
	<th align=right valign=middle class="bord-l-b">
		<div align="right"><kul:htmlAttributeLabel attributeEntry="${vendorQuoteAttributes.vendorAttentionName}" />
	</th>
	<td align=left valign=middle class="datacell">
		<kul:htmlControlAttribute 
			attributeEntry="${vendorQuoteAttributes.vendorAttentionName}" property="document.purchaseOrderVendorQuote[${ctr}].vendorAttentionName" 
			readOnly="${!quoteEditable}" tabindexOverride="${tabindexOverrideBase + 8}"/>
	</td>
</tr>

<tr>
	<th align=right valign=middle class="bord-l-b">
		<div align="right"><kul:htmlAttributeLabel attributeEntry="${vendorQuoteAttributes.vendorPostalCode}" />/
		<div align="right"><kul:htmlAttributeLabel attributeEntry="${vendorQuoteAttributes.vendorCountryCode}" />
	</th>
	<td align=left valign=middle class="datacell">
		<kul:htmlControlAttribute 
			attributeEntry="${vendorQuoteAttributes.vendorPostalCode}" property="document.purchaseOrderVendorQuote[${ctr}].vendorPostalCode" 
			readOnly="${!quoteEditable}" tabindexOverride="${tabindexOverrideBase + 8}"/>/ 
		<kul:htmlControlAttribute 
			attributeEntry="${vendorQuoteAttributes.vendorCountryCode}" property="document.purchaseOrderVendorQuote[${ctr}].vendorCountryCode" 
			readOnly="${!quoteEditable}" extraReadOnlyProperty="document.purchaseOrderVendorQuote[${ctr}].vendorCountry.name" 
			tabindexOverride="${tabindexOverrideBase + 8}"/>
	</td>
	<th align=right valign=middle class="bord-l-b">
		<div align="right"><kul:htmlAttributeLabel attributeEntry="${vendorQuoteAttributes.purchaseOrderQuoteTransmitTypeCode}" />
	</th>
	<td align=left valign=middle class="datacell">
		<kul:htmlControlAttribute 
			attributeEntry="${vendorQuoteAttributes.purchaseOrderQuoteTransmitTypeCode}" 
			property="document.purchaseOrderVendorQuote[${ctr}].purchaseOrderQuoteTransmitTypeCode" 
			readOnly="${!quoteEditable}" tabindexOverride="${tabindexOverrideBase + 9}"/>
		<c:if test="${quoteEditable}">
		<html:image property="methodToCall.transmitPurchaseOrderQuote.line${ctr}"
					src="${ConfigProperties.externalizable.images.url}tinybutton-transmit.gif"
					alt="transmit quote" title="transmit quote" 
					styleClass="tinybutton" />
		<c:if test="${isTransmitPrintDisplayed}">
			<c:if test="${isPdfDisplayedToUserOnce eq false}">		
			<script language ="javascript">
				window.onload = dothis();
	    		function dothis() {
					_win = window.open('purapPrint.do?poDocNumber=${KualiForm.document.documentHeader.documentNumber}&vendorQuoteId=${KualiForm.document.purchaseOrderVendorQuotes[ctr].purchaseOrderVendorQuoteIdentifier}', 'printpopdf');
				}
			</script>
			</c:if>
			Transmit information saved.
			<a href="purapPrint.do?poDocNumber=${KualiForm.document.documentHeader.documentNumber}&vendorQuoteId=${KualiForm.document.purchaseOrderVendorQuotes[ctr].purchaseOrderVendorQuoteIdentifier}" target="_BLANK">
				Click here to print Quote.
			</a>
		</c:if>
		</c:if>
	</td>
</tr>

<tr>
	<th align=right valign=middle class="bord-l-b">
		<div align="right"><kul:htmlAttributeLabel attributeEntry="${vendorQuoteAttributes.purchaseOrderQuotePriceExpirationDate}" />
	</th>
	<td align=left valign=middle class="datacell">
		<kul:htmlControlAttribute 
			attributeEntry="${vendorQuoteAttributes.purchaseOrderQuotePriceExpirationDate}" 
			property="document.purchaseOrderVendorQuote[${ctr}].purchaseOrderQuotePriceExpirationDate" 
			readOnly="${!quoteEditable}" tabindexOverride="${tabindexOverrideBase + 8}"/>
	</td>
	<th align=right valign=middle class="bord-l-b">
		<div align="right"><kul:htmlAttributeLabel attributeEntry="${vendorQuoteAttributes.purchaseOrderQuoteTransmitTimestamp}" />
	</th>
	<td align=left valign=middle class="datacell">
		<kul:htmlControlAttribute 
			attributeEntry="${vendorQuoteAttributes.purchaseOrderQuoteTransmitTimestamp}" 
			property="document.purchaseOrderVendorQuote[${ctr}].purchaseOrderQuoteTransmitTimestamp" 
			readOnly="true" />
	</td>
</tr>

<tr>
	<th align=right valign=middle class="bord-l-b">
		<div align="right"><kul:htmlAttributeLabel attributeEntry="${vendorQuoteAttributes.purchaseOrderQuoteStatusCode}" />
	</th>
	<td align=left valign=middle class="datacell">
		<kul:htmlControlAttribute 
			attributeEntry="${vendorQuoteAttributes.purchaseOrderQuoteStatusCode}" 
			property="document.purchaseOrderVendorQuote[${ctr}].purchaseOrderQuoteStatusCode" 
			readOnly="${!quoteEditable}" extraReadOnlyProperty="document.purchaseOrderVendorQuotes[${ctr}].purchaseOrderQuoteStatus.statusDescription" 
			tabindexOverride="${tabindexOverrideBase + 8}"/>
	</td> 
	<th align=right valign=middle class="bord-l-b">
		<div align="right"><kul:htmlAttributeLabel attributeEntry="${vendorQuoteAttributes.purchaseOrderQuoteRankNumber}" />
	</th>
	<td align=left valign=middle class="datacell">
		<kul:htmlControlAttribute 
			attributeEntry="${vendorQuoteAttributes.purchaseOrderQuoteRankNumber}" 
			property="document.purchaseOrderVendorQuote[${ctr}].purchaseOrderQuoteRankNumber" 
			readOnly="${!quoteEditable}" tabindexOverride="${tabindexOverrideBase + 9}"/>
	</td>
</tr>

<tr>
	<th align=right valign=middle class="bord-l-b">
		<c:if test="${isPurchaseOrderAwarded}">
			Awarded:
		</c:if>
		<c:if test="${!isPurchaseOrderAwarded}">
			Award:
		</c:if>
	</th>
	<td align=left valign=middle class="datacell">
		<c:if test="${isSysVendor && !isPurchaseOrderAwarded}">
			<html:radio property="awardedVendorNumber" value="${ctr}" disabled="${!quoteEditable}" />
		</c:if>
		<c:if test="${isPurchaseOrderAwarded}">
			<c:if test="${!isAwarded}">
				No
			</c:if>
			<c:if test="${isAwarded}">
				Yes
			</c:if>
		</c:if>
		&nbsp;
	</td>
	<th align=right valign=middle class="bord-l-b">
		<div align="right"><kul:htmlAttributeLabel attributeEntry="${vendorQuoteAttributes.purchaseOrderQuoteAwardTimestamp}" />
	</th>
	<td align=left valign=middle class="datacell">
		<kul:htmlControlAttribute 
			attributeEntry="${vendorQuoteAttributes.purchaseOrderQuoteAwardTimestamp}" 
			property="document.purchaseOrderVendorQuote[${ctr}].purchaseOrderQuoteAwardTimestamp" 
			readOnly="true" />
	</td>
</tr>
