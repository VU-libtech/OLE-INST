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


<td class="content" valign="top">
      <maintenanceChannel:chartOfAccounts />
      <maintenanceChannel:financialProcessing />
      <maintenanceChannel:preDisbursementProcessor />
      <maintenanceChannel:select />
      
</td>
<td class="content" valign="top">
      <maintenanceChannel:accountsReceivable />
      <maintenanceChannel:vendor />
      <maintenanceChannel:purchasingAccountsPayable />
      <maintenanceChannel:capitalAsset />
</td>
<td class="content" valign="top">
      <maintenanceChannel:contractsAndGrants />
      <maintenanceChannel:system /> 
</td>
