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

<%@ attribute name="itemAttributes" required="true" type="java.util.Map" description="The DataDictionary entry containing attributes for this row's fields."%>
<%@ attribute name="accountingLineAttributes" required="true" type="java.util.Map" description="The DataDictionary entry containing attributes for this row's fields."%>
<%@ attribute name="mainColumnCount" required="true" %>

<c:set var="fullEntryMode" value="${KualiForm.documentActions[Constants.KUALI_ACTION_CAN_EDIT]}" />
<c:set var="lockTaxAmountEntry" value="${(not empty KualiForm.editingMode['lockTaxAmountEntry'])}" />
<c:set var="clearAllTaxes" value="${(not empty KualiForm.editingMode['clearAllTaxes'])}" />
<c:set var="purapTaxEnabled" value="${(not empty KualiForm.editingMode['purapTaxEnabled'])}" />

<c:set var="colSpanDescription" value="4"/>
<c:if test="${purapTaxEnabled}">
	<c:set var="colSpanDescription" value="2"/>
</c:if>
		
<tr>
	<td colspan="${mainColumnCount}" class="subhead">
		<span class="subhead-left">Items</span>
	</td>
</tr>

<c:set var="usePO" value="true" />
<!--  replace literal with PurapConstants once exported -->
<c:if test="${KualiForm.document.creditMemoType eq 'PREQ'}" >
  <c:set var="usePO" value="false" />
</c:if>
<c:set var="tabindexOverrideBase" value="50" />

<c:if test="${KualiForm.countOfAboveTheLine>=1}">
	<tr>
		<kul:htmlAttributeHeaderCell attributeEntry="${itemAttributes.itemLineNumber}" width="2%"/>
		
		<c:if test="${usePO}" >
	    	<kul:htmlAttributeHeaderCell attributeEntry="${itemAttributes.poInvoicedTotalQuantity}" width="7%"/>
	    	<kul:htmlAttributeHeaderCell attributeEntry="${itemAttributes.poUnitPrice}" width="7%"/>		
	    	<kul:htmlAttributeHeaderCell attributeEntry="${itemAttributes.poTotalAmount}" width="7%"/>
	    </c:if>
	    
		<c:if test="${!usePO}" >
	    	<kul:htmlAttributeHeaderCell attributeEntry="${itemAttributes.preqInvoicedTotalQuantity}" width="7%"/>
	    	<kul:htmlAttributeHeaderCell attributeEntry="${itemAttributes.preqUnitPrice}" width="7%"/>		
	    	<kul:htmlAttributeHeaderCell attributeEntry="${itemAttributes.preqTotalAmount}" width="7%"/>
	    </c:if>
	    	
		<kul:htmlAttributeHeaderCell attributeEntry="${itemAttributes.itemQuantity}" width="7%"/>		
		<kul:htmlAttributeHeaderCell attributeEntry="${itemAttributes.itemUnitPrice}" width="7%"/>
		<kul:htmlAttributeHeaderCell attributeEntry="${itemAttributes.extendedPrice}" width="7%"/>

		<c:if test="${purapTaxEnabled}">
			<kul:htmlAttributeHeaderCell attributeEntry="${itemAttributes.itemTaxAmount}" width="7%"/>		
			<kul:htmlAttributeHeaderCell attributeEntry="${itemAttributes.totalAmount}" width="7%"/>
		</c:if>

		<kul:htmlAttributeHeaderCell attributeEntry="${itemAttributes.itemCatalogNumber}" width="7%"/>
		<kul:htmlAttributeHeaderCell attributeEntry="${itemAttributes.itemDescription}" width="45%" colspan="${colSpanDescription}"/>
	</tr>
</c:if>

<c:if test="${KualiForm.countOfAboveTheLine<1}">
	<tr>
		<th height=30 colspan="${mainColumnCount}">No items added to document</th>
	</tr>
</c:if>

<logic:iterate indexId="ctr" name="KualiForm" property="document.items" id="itemLine">

	<c:if test="${itemLine.itemType.lineItemIndicator == true}">
		<c:set var="currentTabIndex" value="${KualiForm.currentTabIndex}" scope="request" />
		<c:set var="topLevelTabIndex" value="${KualiForm.currentTabIndex}" scope="request" />

        <c:choose>
            <c:when test="${itemLine.objectId == null}">
                <c:set var="newObjectId" value="<%= (new org.kuali.rice.kns.util.Guid()).toString()%>" />
                <c:set var="tabKey" value="Item-${newObjectId}" />
            </c:when>
            <c:when test="${itemLine.objectId != null}">
                <c:set var="tabKey" value="Item-${itemLine.objectId}" />
            </c:when>
        </c:choose>
        
        <!--  hit form method to increment tab index -->
        <c:set var="dummyIncrementer" value="${kfunc:incrementTabIndex(KualiForm, tabKey)}" />
        <c:set var="currentTab" value="${kfunc:getTabState(KualiForm, tabKey)}"/>

		<%-- default to closed --%>
		<c:choose>
		<c:when test="${empty currentTab}">
			<c:set var="isOpen" value="false" />
		</c:when>
		<c:when test="${!empty currentTab}">
			<c:set var="isOpen" value="${(isOpen ? 'OPEN' : 'CLOSE')}" />
		</c:when>
		</c:choose>

		<tr>
			<td class="infoline" nowrap="nowrap" rowspan="2">
               &nbsp;<b><bean:write name="KualiForm" property="document.item[${ctr}].itemLineNumber"/></b> 
			</td>
			
	    	<c:if test="${usePO}" >
		    	<td class="infoline">
		    	<c:if test="${itemLine.itemType.quantityBasedGeneralLedgerIndicator}" >
		    	 <div align="right">		    	 
			       <kul:htmlControlAttribute
				    attributeEntry="${itemAttributes.poInvoicedTotalQuantity}"
				    property="document.item[${ctr}].poInvoicedTotalQuantity"
				    readOnly="true" styleClass="infoline" />
				  </div>  
				</c:if>  
		    	<c:if test="${!itemLine.itemType.quantityBasedGeneralLedgerIndicator}" >
		    		&nbsp;
		    	</c:if>  				  
		    	</td>
		     	<td class="infoline">
		    	  <div align="right">
		    	    <kul:htmlControlAttribute
				    attributeEntry="${itemAttributes.poUnitPrice}"
				    property="document.item[${ctr}].poUnitPrice"
				    readOnly="true" styleClass="infoline" />
				  </div>  
	    	    </td>
		    	<td class="infoline">
		    	 <div align="right">
		     	    <kul:htmlControlAttribute
				    attributeEntry="${itemAttributes.poTotalAmount}"
				    property="document.item[${ctr}].poTotalAmount"
				    readOnly="true" styleClass="infoline" />
				  </div>  
	    	    </td>		
	    	</c:if>
	    	
	    	<c:if test="${!usePO}" >
		    	<td class="infoline">
		    	   <c:if test="${itemLine.itemType.quantityBasedGeneralLedgerIndicator}" >
		    	    <div align="right">
			         <kul:htmlControlAttribute
				      attributeEntry="${itemAttributes.preqInvoicedTotalQuantity}"
				      property="document.item[${ctr}].preqInvoicedTotalQuantity"
				    readOnly="true" styleClass="infoline" />
				    </div>  
				   </c:if>  
		    	   <c:if test="${!itemLine.itemType.quantityBasedGeneralLedgerIndicator}" >
		    	     &nbsp;
		    	   </c:if>  
		    	</td>
		     	<td class="infoline">
		    	  <div align="right">
		    	    <kul:htmlControlAttribute
				    attributeEntry="${itemAttributes.preqUnitPrice}"
				    property="document.item[${ctr}].preqUnitPrice"
				    readOnly="true" styleClass="infoline" />
				  </div>  
	    	    </td>
		    	<td class="infoline">
		      	  <div align="right">
		     	    <kul:htmlControlAttribute
				    attributeEntry="${itemAttributes.preqTotalAmount}"
				    property="document.item[${ctr}].preqTotalAmount"
				    readOnly="true" styleClass="infoline" />
				  </div>  
	    	    </td>		
	    	</c:if>
	    	
			<td class="infoline">
			    <div align="right">
		    	   <c:if test="${itemLine.itemType.quantityBasedGeneralLedgerIndicator}" >
		    	    <div align="right">
			          <kul:htmlControlAttribute
				          attributeEntry="${itemAttributes.itemQuantity}"
				          property="document.item[${ctr}].itemQuantity"
				          readOnly="${not (fullEntryMode)}" styleClass="amount" 
				          tabindexOverride="${tabindexOverrideBase + 0}"/>
				     </div>     
				   </c:if>
		    	   <c:if test="${!itemLine.itemType.quantityBasedGeneralLedgerIndicator}" >
		    	     &nbsp;
		    	   </c:if>  
				</div>
			</td>
			<td class="infoline">
                   <c:if test="${itemLine.itemType.quantityBasedGeneralLedgerIndicator}" >
                    <div align="right">
	                    <kul:htmlControlAttribute
	                        attributeEntry="${itemAttributes.itemUnitPrice}"
	                        property="document.item[${ctr}].itemUnitPrice"
	                        readOnly="${not (fullEntryMode)}" styleClass="amount" 
	                        tabindexOverride="${tabindexOverrideBase + 0}"/>
                     </div>     
                   </c:if>
                   <c:if test="${!itemLine.itemType.quantityBasedGeneralLedgerIndicator}" >
                     &nbsp;
                   </c:if>  
			</td>
			<td class="infoline">
			    <div align="right">
			        <kul:htmlControlAttribute
				        attributeEntry="${itemAttributes.extendedPrice}"
				        property="document.item[${ctr}].extendedPrice"
				        readOnly="${not fullEntryMode}" styleClass="amount" 
				        tabindexOverride="${tabindexOverrideBase + 0}"/>
			    </div>
			</td>

			<c:if test="${purapTaxEnabled}">
			<td class="infoline">
			    <div align="right">
			        <kul:htmlControlAttribute
				        attributeEntry="${itemAttributes.itemTaxAmount}"
				        property="document.item[${ctr}].itemTaxAmount"
				        readOnly="${not fullEntryMode or lockTaxAmountEntry}" styleClass="amount" 
				        tabindexOverride="${tabindexOverrideBase + 0}"/>
			    </div>
			</td>			
			<td class="infoline">
			    <div align="right">
			        <kul:htmlControlAttribute
				        attributeEntry="${itemAttributes.totalAmount}"
				        property="document.item[${ctr}].totalAmount"
				        readOnly="true" styleClass="amount"/>
			    </div>
			</td>
			</c:if>

			<td class="infoline">
			    <kul:htmlControlAttribute
				    attributeEntry="${itemAttributes.itemCatalogNumber}"
				    property="document.item[${ctr}].itemCatalogNumber"
				    readOnly="true" />
		    </td>		    
			<td class="infoline" colspan="${colSpanDescription}"/>
			    <kul:htmlControlAttribute
				    attributeEntry="${itemAttributes.itemDescription}"
				    property="document.item[${ctr}].itemDescription"
				    readOnly="true" />
			</td>			
		</tr>

		<purap:purapGeneralAccounting
			accountPrefix="document.item[${ctr}]." 
			itemColSpan="${mainColumnCount-1}"/>	
		<c:if test="${isOpen != 'true' && isOpen != 'TRUE'}">
			</tbody>
		</c:if>
	</c:if>
</logic:iterate>

<c:if test="${(fullEntryMode) and (clearAllTaxes) and (purapTaxEnabled)}">
	<tr>
		<th height=30 colspan="${mainColumnCount}">
			<html:image 
			    property="methodToCall.clearAllTaxes" 
			    src="${ConfigProperties.externalizable.images.url}tinybutton-clearalltax.gif" 
			    alt="Clear all tax" 
			    title="Clear all tax" styleClass="tinybutton" />
			 </div>
	 	</th>
	 </tr>
</c:if>	
<tr>
	<th height=30 colspan="${mainColumnCount}">&nbsp;</th>
</tr>
