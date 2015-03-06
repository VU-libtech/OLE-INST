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
<%@ include file="/rice-portal/jsp/sys/riceTldHeader.jsp" %>

<channel:portalChannelTop channelTitle="Deliver"/>
<div class="body">


   <%-- <portal:portalLink displayTitle="true" title="Loan/Return" green="true"
                       url="${ConfigProperties.application.url}/kr-krad/loancontroller?viewId=PatronItemView&methodToCall=start"/> <br/>--%>
    <portal:portalLink displayTitle="true" title="Loan"
                          url="${ConfigProperties.application.url}/ole-kr-krad/loancontroller?viewId=PatronItemView&methodToCall=start"/> <br/>
    <portal:portalLink displayTitle="true" title="Return"
                          url="${ConfigProperties.application.url}/ole-kr-krad/loancontroller?viewId=ReturnItemView&methodToCall=start"/> <br/>
    <portal:portalLink displayTitle="true"   title="Circulation Desk"
                       url="${ConfigProperties.application.url}/ole-kr-krad/lookup?methodToCall=start&dataObjectClassName=org.kuali.ole.deliver.bo.OleCirculationDesk&returnLocation=${ConfigProperties.application.url}/portal.do&hideReturnLink=true&showMaintenanceLinks=true"/><br/>
    <portal:portalLink displayTitle="true" title="Circulation Desk Mapping"
                       url="${ConfigProperties.application.url}/ole-kr-krad/circulationDeskDetailController?viewId=OleCirculationDeskDetailView&methodToCall=start"/> <br/>
    <portal:portalLink displayTitle="true"   title="Fixed Due Date"
                       url="${ConfigProperties.application.url}/ole-kr-krad/lookup?methodToCall=start&dataObjectClassName=org.kuali.ole.deliver.bo.OleFixedDueDate&returnLocation=${ConfigProperties.application.url}/portal.do&hideReturnLink=true&showMaintenanceLinks=true"/><br/>
    <portal:portalLink displayTitle="true"   title="Request"
                       url="${ConfigProperties.application.url}/ole-kr-krad/lookup?methodToCall=start&dataObjectClassName=org.kuali.ole.deliver.bo.OleDeliverRequestBo&returnLocation=${ConfigProperties.application.url}/portal.do&hideReturnLink=true&showMaintenanceLinks=true"/><br/>
    <portal:portalLink displayTitle="true"   title="Request Type"
                       url="${ConfigProperties.application.url}/ole-kr-krad/lookup?methodToCall=start&dataObjectClassName=org.kuali.ole.deliver.bo.OleDeliverRequestType&returnLocation=${ConfigProperties.application.url}/portal.do&hideReturnLink=true&showMaintenanceLinks=true"/><br/>
    <portal:portalLink displayTitle="true" title="Request Re-order"
                       url="${ConfigProperties.application.url}/ole-kr-krad/deliverRequestController?viewId=DeliverRequestSearch&methodToCall=start"/> <br/>
    <portal:portalLink displayTitle="true" title="View Item Bill"
                       url="${ConfigProperties.application.url}/ole-kr-krad/itemPatronBill?viewId=ItemBillView&methodToCall=start"/><br/>

    <portal:portalLink displayTitle="true"   title="Calendar Group"
                          url="${ConfigProperties.application.url}/ole-kr-krad/lookup?methodToCall=start&dataObjectClassName=org.kuali.ole.deliver.calendar.bo.OleCalendarGroup&returnLocation=${ConfigProperties.application.url}/portal.do&hideReturnLink=true&showMaintenanceLinks=true"/><br/>
    <portal:portalLink displayTitle="true"  title="Calendar"
                          url="${ConfigProperties.application.url}/ole-kr-krad/lookup?methodToCall=start&dataObjectClassName=org.kuali.ole.deliver.calendar.bo.OleCalendar&returnLocation=${ConfigProperties.application.url}/portal.do&hideReturnLink=true&showMaintenanceLinks=true"/><br/>
    <br/>
</div>
<channel:portalChannelBottom/>
