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

<channel:portalChannelTop channelTitle="Fund" />
<div class="body">
    <portal:portalLink displayTitle="true" title="Fund Balances" url="oleFundLookup.do?methodToCall=docHandler&command=initiate&docTypeName=OLE_FLU"/><br/><br/>



    <strong>BUDGET ADJUSTMENT</strong><br/>
    <portal:portalLink displayTitle="true" title='Create' url='financialBudgetAdjustment.do?methodToCall=docHandler&command=initiate&docTypeName=OLE_BA'/><br/>
    <portal:olePortalLink green="false" displayTitle="false" title='Search' url='' hiddenTitle="true"/>
<br/>
    <br/>

    <strong>TRANSFER OF FUNDS</strong><br/>
    <portal:portalLink displayTitle="true" title="Create" url='financialTransferOfFunds.do?methodToCall=docHandler&command=initiate&docTypeName=OLE_TF' /><br/>
    <portal:olePortalLink green="false" displayTitle="false" title='Search' url='' hiddenTitle="true"/>
<br/>
    <br/>




   <%-- <portal:olePortalLink displayTitle="true"  title="Donor" green="true"
                       url="${ConfigProperties.application.url}/ole-kr-krad/lookup?methodToCall=start&dataObjectClassName=org.kuali.ole.select.bo.OLEDonor&returnLocation=${ConfigProperties.application.url}/portal.do&hideReturnLink=true&showMaintenanceLinks=true"/><br/>
    <portal:olePortalLink displayTitle="true" green="true" title="Encumbered Report" url="${ConfigProperties.application.url}/ole-kr-krad/searchDonorEncumberedReportController?viewId=OLEEncumberedReportView&methodToCall=start"/><br/>
    <portal:olePortalLink displayTitle="true" green="true" title="Annual Stewardship Report" url="${ConfigProperties.application.url}/ole-kr-krad/searchAnnualStewardshipReportController?viewId=OLEAnnualStewardshipReportView&methodToCall=start"/><br/>--%>
</div>
<channel:portalChannelBottom />
