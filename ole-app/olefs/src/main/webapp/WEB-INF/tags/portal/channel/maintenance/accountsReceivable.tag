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

<channel:portalChannelTop channelTitle="Accounts Receivable" />
<div class="body">
    <ul class="chan">
        <li><portal:portalLink displayTitle="true" title="Customer" url="kr/lookup.do?methodToCall=start&businessObjectClassName=org.kuali.ole.module.ar.businessobject.Customer&docFormKey=88888888&returnLocation=${ConfigProperties.application.url}/portal.do&hideReturnLink=true" /></li>
        <li><portal:portalLink displayTitle="true" title="Customer Type" url="kr/lookup.do?methodToCall=start&businessObjectClassName=org.kuali.ole.module.ar.businessobject.CustomerType&docFormKey=88888888&returnLocation=${ConfigProperties.application.url}/portal.do&hideReturnLink=true" /></li>
        <li><portal:portalLink displayTitle="true" title="Customer Address Type" url="kr/lookup.do?methodToCall=start&businessObjectClassName=org.kuali.ole.module.ar.businessobject.CustomerAddressType&docFormKey=88888888&returnLocation=${ConfigProperties.application.url}/portal.do&hideReturnLink=true" /></li>
		<li><portal:portalLink displayTitle="true" title="Customer Invoice Item Code" url="kr/lookup.do?methodToCall=start&businessObjectClassName=org.kuali.ole.module.ar.businessobject.CustomerInvoiceItemCode&docFormKey=88888888&returnLocation=${ConfigProperties.application.url}/portal.do&hideReturnLink=true" /></li>        
		<li><portal:portalLink displayTitle="true" title="Invoice Recurrence" url="kr/lookup.do?methodToCall=start&businessObjectClassName=org.kuali.ole.module.ar.businessobject.InvoiceRecurrence&docFormKey=88888888&returnLocation=${ConfigProperties.application.url}/portal.do&hideReturnLink=true" /></li>        
        <li><portal:portalLink displayTitle="true" title="Organization Options" url="kr/lookup.do?methodToCall=start&businessObjectClassName=org.kuali.ole.module.ar.businessobject.OrganizationOptions&docFormKey=88888888&returnLocation=${ConfigProperties.application.url}/portal.do&hideReturnLink=true" /></li>
        <li><portal:portalLink displayTitle="true" title="Organization Accounting Default" url="kr/lookup.do?methodToCall=start&businessObjectClassName=org.kuali.ole.module.ar.businessobject.OrganizationAccountingDefault&docFormKey=88888888&returnLocation=${ConfigProperties.application.url}/portal.do&hideReturnLink=true" /></li>
        <li><portal:portalLink displayTitle="true" title="Payment Medium" url="kr/lookup.do?methodToCall=start&businessObjectClassName=org.kuali.ole.module.ar.businessobject.PaymentMedium&docFormKey=88888888&returnLocation=${ConfigProperties.application.url}/portal.do&hideReturnLink=true" /></li>
        <li><portal:portalLink displayTitle="true" title="System Information" url="kr/lookup.do?methodToCall=start&businessObjectClassName=org.kuali.ole.module.ar.businessobject.SystemInformation&docFormKey=88888888&returnLocation=${ConfigProperties.application.url}/portal.do&hideReturnLink=true" /></li>   
   	</ul>
</div>
<channel:portalChannelBottom />
