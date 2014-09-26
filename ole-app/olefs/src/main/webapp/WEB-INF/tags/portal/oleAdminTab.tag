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
<%@ attribute name="environment" required="false" type="java.lang.String"%>
<%@ attribute name="riceUrl" required="false" type="java.lang.String"%>
<td class="content" valign="top">
   <oleAdminChannel:oleBatchProcessing/>
</td>
<td class="content" valign="top">
    <oleAdminChannel:oleMonitoring/>
    <oleAdminChannel:oleTesting/>
    <oleAdminChannel:oleDocumentation/>
</td>
<td class="content" valign="top">
    <oleAdminChannel:oleServiceAndFunction/>
    <oleAdminChannel:config/>
</td>
<td class="content" valign="top">
    <oleAdminChannel:oleWorkflowAdmin/>
    <oleAdminChannel:oleKRMSAdmin/>
    <oleAdminChannel:oleIdentityAdmin/>
</td>
<%--
<td class="content" valign="top">
    <oleAdminChannel:oleDemoHelp/>
    <oleAdminChannel:monitoring/>
    <oleAdminChannel:oleTesting/>
</td>--%>
