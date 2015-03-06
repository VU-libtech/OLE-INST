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

<channel:portalChannelTop channelTitle="Import" />
<%@ attribute name="riceUrl" required="false" type="java.lang.String"%>
<div class="body">
    <%--<portal:olePortalLink rice2="true" riceUrl="${riceUrl}" green="true" displayTitle="true" title="Staff Upload" url="${riceUrl}/ole-kr-krad/staffuploadcontroller?viewId=StaffUploadView&methodToCall=start&__login_user=admin&user=${user}"/><br/></br>--%>
    <portal:olePortalLink green="true" displayTitle="true" title="Load Reports" url="batchlookup.do?methodToCall=start&businessObjectClassName=org.kuali.ole.select.businessobject.OleLoadSumRecords&docFormKey=88888888&returnLocation=${ConfigProperties.application.url}/portal.do&hideReturnLink=true" /><br/><br/>
</div>
<channel:portalChannelBottom />