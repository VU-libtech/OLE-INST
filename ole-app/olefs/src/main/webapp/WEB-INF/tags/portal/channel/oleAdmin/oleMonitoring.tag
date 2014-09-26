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

<channel:portalChannelTop channelTitle="Monitoring" />
<div class="body">
    <strong>Service Bus</strong></br><br/>

        <portal:portalLink displayTitle="true" title="Cache Admin" url="${ConfigProperties.application.url}/kr-krad/core/admin/cache?viewId=CacheAdmin-view1&methodToCall=start"/><br/>
        <portal:portalLink displayTitle="true"
                               title="Configuration Viewer"
                               url="${ConfigProperties.ksb.client.url}/ConfigViewer.do" /><br/>
        <portal:portalLink displayTitle="true" title="Java Melody" url="${ConfigProperties.application.url}/monitoring" /><br/>
        <portal:portalLink displayTitle="true" title="Message Queue" url="${ConfigProperties.ksb.client.url}/${ConfigProperties.message.queue.url}" /><br/>
        <portal:portalLink displayTitle="true" title="Quartz" url="${ConfigProperties.ksb.client.url}/Quartz.do" /><br/>
        <portal:portalLink displayTitle="true" title="Security Management" url="${ConfigProperties.ksb.client.url}/JavaSecurityManagement.do" /><br/>
        <portal:portalLink displayTitle="true" title="Service Registry" url="${ConfigProperties.ksb.server.url}/${ConfigProperties.service.registry.url}" /><br/>
        <portal:portalLink displayTitle="true" title="Thread Pool" url="${ConfigProperties.ksb.client.url}/${ConfigProperties.thread.pool.url}" /><br/>



     <br/>
    <strong>Workflow</strong></br><br/>

        <portal:portalLink displayTitle="true" title="Document Operation" url="${ConfigProperties.kew.url}/DocumentOperation.do" /><br/>
        <portal:portalLink displayTitle="true" title="Statistics Report" url="${ConfigProperties.kew.url}/Stats.do" /><br/>

</div>
<channel:portalChannelBottom />

