<%@ include file="/jsp/sys/kfsTldHeader.jsp"%>

<channel:portalChannelTop channelTitle="Electronic Resources" />

<div class="body">
    <strong>E-Resources</strong><br/>
    <portal:portalLink displayTitle="true" title="Create" url="${ConfigProperties.application.url}/ole-kr-krad/oleERSController?viewId=OLEEResourceRecordView&methodToCall=docHandler&command=initiate&documentClass=org.kuali.ole.select.document.OLEEResourceRecordDocument"/><br/>
    <portal:portalLink displayTitle="true" title="Search" url="${ConfigProperties.application.url}/ole-kr-krad/searchEResourceController?viewId=OLEEResourceSearchView&methodToCall=start"/><br/>
    <br/>


</div>
<channel:portalChannelBottom />
