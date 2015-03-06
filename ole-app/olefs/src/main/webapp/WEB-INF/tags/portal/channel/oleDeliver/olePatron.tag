<%@ include file="/rice-portal/jsp/sys/riceTldHeader.jsp" %>

<channel:portalChannelTop channelTitle="Patron"/>

<div class="body">
    <portal:portalLink displayTitle="true" title="Deliver Notices"
                       url="${ConfigProperties.application.url}/ole-kr-krad/oleDeliverNoticeController?viewId=OLEDeliverNoticeView&methodToCall=start"/> <br/>
    <portal:portalLink displayTitle="true"   title="Patron"
                       url="${ConfigProperties.application.url}/ole-kr-krad/lookup?methodToCall=start&dataObjectClassName=org.kuali.ole.deliver.bo.OlePatronDocument&returnLocation=${ConfigProperties.application.url}/portal.do&hideReturnLink=true&showMaintenanceLinks=true"/><br/>
    <portal:portalLink displayTitle="true"   title="Patron Bill"
                       url="${ConfigProperties.application.url}/ole-kr-krad/lookup?methodToCall=start&dataObjectClassName=org.kuali.ole.deliver.bo.PatronBillPayment&returnLocation=${ConfigProperties.application.url}/portal.do&hideReturnLink=true&showMaintenanceLinks=true"/><br/>
    <%--<portal:portalLink displayTitle="true" title="MyAccount"
                       url="${ConfigProperties.application.url}/ole-kr-krad/myaccountcontroller?viewId=RenewalItemView&methodToCall=start"/> <br/><br/>--%>

    </br>

</div>
<channel:portalChannelBottom/>
