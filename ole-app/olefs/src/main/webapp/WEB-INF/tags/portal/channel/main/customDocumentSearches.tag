<%--
 Copyright 2007 The Kuali Foundation
 
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

<channel:portalChannelTop channelTitle="Custom Document Searches" />
<div class="body">
	<portal:portalLink displayTitle="true" title="Financial Transactions" url="${ConfigProperties.workflow.documentsearch.base.url}&documentTypeName=OpenLibraryEnvironmentTransactionalDocument" /><br /><br />
 	<strong>Accounts Receivable</strong><br/>
    <ul class="chan">
        <li><portal:portalLink displayTitle="true" title='Customer Invoices' url='${ConfigProperties.workflow.documentsearch.base.url}&documentTypeName=OLE_INV'/></li>
        <li><portal:portalLink displayTitle="true" title='Customer Credit Memos' url='${ConfigProperties.workflow.documentsearch.base.url}&documentTypeName=OLE_CRM'/></li>
        <li><portal:portalLink displayTitle="true" title='Customer Invoice Writeoffs' url='${ConfigProperties.workflow.documentsearch.base.url}&documentTypeName=OLE_INVW'/></li>
        <li><portal:portalLink displayTitle="true" title='Cash Controls' url='${ConfigProperties.workflow.documentsearch.base.url}&documentTypeName=OLE_CTRL'/></li>
        <li><portal:portalLink displayTitle="true" title='Payment Applications' url='${ConfigProperties.workflow.documentsearch.base.url}&documentTypeName=OLE_APP'/></li>
    </ul>
    <strong>Capital Asset Management</strong><br/>
    <ul class="chan">
		<li><portal:portalLink displayTitle="true" title='Asset Maintenance' url='${ConfigProperties.workflow.documentsearch.base.url}&documentTypeName=OLECapitalAssetManagementComplexMaintenanceDocument'/></li>
	</ul>
	<strong>Contracts & Grants</strong><br/>
    <ul class="chan">
		<li><portal:portalLink displayTitle="true" title='Proposals' url='${ConfigProperties.workflow.documentsearch.base.url}&documentTypeName=OLE_PRPL'/></li>
	</ul>
	<strong>Financial Processing</strong><br/>
    <ul class="chan">
		<li><portal:portalLink displayTitle="true" title="Disbursement Vouchers" url="${ConfigProperties.workflow.documentsearch.base.url}&documentTypeName=OLE_DV" /></li>
	</ul>
	<strong>Purchasing/Accounts Payable</strong><br/>
    <ul class="chan">
        <li><portal:portalLink displayTitle="true" title='Electronic Invoice Rejects' url='${ConfigProperties.workflow.documentsearch.base.url}&documentTypeName=OLE_EIRT'/></li>
        <li><portal:portalLink displayTitle="true" title='Payment Requests' url='${ConfigProperties.workflow.documentsearch.base.url}&documentTypeName=OLE_PREQ'/></li>
        <li><portal:portalLink displayTitle="true" title='Purchase Orders' url='${ConfigProperties.workflow.documentsearch.base.url}&documentTypeName=OLE_PO'/></li>
        <li><portal:portalLink displayTitle="true" title='Receiving' url='${ConfigProperties.workflow.documentsearch.base.url}&documentTypeName=OLE_RCV'/></li>
        <li><portal:portalLink displayTitle="true" title='Requisitions' url='${ConfigProperties.workflow.documentsearch.base.url}&documentTypeName=OLE_REQS'/></li>
        <li><portal:portalLink displayTitle="true" title='Vendor Credit Memos' url='${ConfigProperties.workflow.documentsearch.base.url}&documentTypeName=OLE_CM'/></li>         
        <li><portal:portalLink displayTitle="true" title='Acquisitions Search' url="oleAcquisitionsSearch.do?methodToCall=docHandler&command=initiate&docTypeName=OLE_ACQS"/></li>
     </ul>
    </div>
<channel:portalChannelBottom />
