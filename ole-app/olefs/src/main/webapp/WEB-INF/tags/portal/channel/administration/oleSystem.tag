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

<channel:portalChannelTop channelTitle="System" />
<div class="body">
	<strong>Identity</strong><br/><br/>
    
		<portal:olePortalLink green="true"  displayTitle="true" title="Person" url="${ConfigProperties.kr.url}/lookup.do?methodToCall=start&businessObjectClassName=org.kuali.rice.kim.api.identity.Person&docFormKey=88888888&returnLocation=${ConfigProperties.application.url}/portal.do&hideReturnLink=true" /><br/>
		<portal:olePortalLink green="true"  displayTitle="true" title="Group" url="${ConfigProperties.kr.url}/lookup.do?methodToCall=start&businessObjectClassName=org.kuali.rice.kim.impl.group.GroupBo&docFormKey=88888888&returnLocation=${ConfigProperties.application.url}/portal.do&hideReturnLink=true" /><br/>
		<portal:olePortalLink green="true"  displayTitle="true" title="Role" url="${ConfigProperties.kr.url}/lookup.do?methodToCall=start&businessObjectClassName=org.kuali.rice.kim.impl.role.RoleBo&docFormKey=88888888&returnLocation=${ConfigProperties.application.url}/portal.do&hideReturnLink=true" /><br/>
		<portal:olePortalLink green="true"  displayTitle="true" title="Permission" url="${ConfigProperties.kr.url}/lookup.do?methodToCall=start&businessObjectClassName=org.kuali.rice.kim.impl.permission.UberPermissionBo&docFormKey=88888888&returnLocation=${ConfigProperties.application.url}/portal.do&hideReturnLink=true" /><br/>
		<portal:olePortalLink green="true"  displayTitle="true" title="Processing Unit" url="" /><br/>
		<portal:olePortalLink green="true"  displayTitle="true" title="Responsibility" url="${ConfigProperties.kr.url}/lookup.do?methodToCall=start&businessObjectClassName=org.kuali.rice.kim.impl.responsibility.UberResponsibilityBo&docFormKey=88888888&returnLocation=${ConfigProperties.application.url}/portal.do&hideReturnLink=true" /><br/><br/>
	
	<c:if test="${ConfigProperties.module.access.security.enabled == 'true'}">	
		<strong>Access Security</strong><br/><br/>
	    
	        <portal:olePortalLink green="true"  displayTitle="true" title="Attribute" url="kr/lookup.do?methodToCall=start&businessObjectClassName=org.kuali.ole.sec.businessobject.SecurityAttribute&docFormKey=88888888&returnLocation=${ConfigProperties.application.url}/portal.do&hideReturnLink=true" /><br/>
			<portal:olePortalLink green="true"  displayTitle="true" title="Definition" url="kr/lookup.do?methodToCall=start&businessObjectClassName=org.kuali.ole.sec.businessobject.SecurityDefinition&docFormKey=88888888&returnLocation=${ConfigProperties.application.url}/portal.do&hideReturnLink=true" /><br/>
	    	<portal:olePortalLink green="true"  displayTitle="true" title="Model" url="kr/lookup.do?methodToCall=start&businessObjectClassName=org.kuali.ole.sec.businessobject.SecurityModel&docFormKey=88888888&returnLocation=${ConfigProperties.application.url}/portal.do&hideReturnLink=true" /><br/>
		    <portal:olePortalLink green="true"  displayTitle="true" title="Principal" url="kr/lookup.do?methodToCall=start&businessObjectClassName=org.kuali.ole.sec.businessobject.SecurityPrincipal&docFormKey=88888888&returnLocation=${ConfigProperties.application.url}/portal.do&hideReturnLink=true" /><br/><br/>
		
	</c:if>
	<strong>Locations</strong><br/><br/>
    
        <portal:olePortalLink green="true"  displayTitle="true" title="Campus" url="${ConfigProperties.kr.url}/lookup.do?methodToCall=start&businessObjectClassName=org.kuali.rice.location.impl.campus.CampusBo&docFormKey=88888888&returnLocation=${ConfigProperties.application.url}/portal.do&hideReturnLink=true" /><br/>
        <portal:olePortalLink green="true"  displayTitle="true" title="Country" url="${ConfigProperties.kr.url}/lookup.do?methodToCall=start&businessObjectClassName=org.kuali.rice.location.impl.country.CountryBo&docFormKey=88888888&returnLocation=${ConfigProperties.application.url}/portal.do&hideReturnLink=true" /><br/>
		<portal:olePortalLink green="true"  displayTitle="true" title="County" url="${ConfigProperties.kr.url}/lookup.do?methodToCall=start&businessObjectClassName=org.kuali.rice.location.impl.county.CountyBo&docFormKey=88888888&returnLocation=${ConfigProperties.application.url}/portal.do&hideReturnLink=true" /><br/>
        <portal:olePortalLink green="true"  displayTitle="true" title="Postal Code" url="${ConfigProperties.kr.url}/lookup.do?methodToCall=start&businessObjectClassName=org.kuali.rice.location.impl.postalcode.PostalCodeBo&docFormKey=88888888&returnLocation=${ConfigProperties.application.url}/portal.do&hideReturnLink=true" /><br/>
        <portal:olePortalLink green="true"  displayTitle="true" title="Province" url="" /><br/>
        <portal:olePortalLink green="true"  displayTitle="true" title="State" url="${ConfigProperties.kr.url}/lookup.do?methodToCall=start&businessObjectClassName=org.kuali.rice.location.impl.state.StateBo&docFormKey=88888888&returnLocation=${ConfigProperties.application.url}/portal.do&hideReturnLink=true" /><br/><br/>
	
	<strong>Identity Management Reference</strong><br/><br/>

        <portal:olePortalLink green="true" displayTitle="true" title="Address Type" url="${ConfigProperties.kr.url}/lookup.do?methodToCall=start&businessObjectClassName=org.kuali.rice.kim.impl.identity.address.EntityAddressTypeBo&docFormKey=88888888&returnLocation=${ConfigProperties.application.url}/portal.do&hideReturnLink=true" /><br />
        <portal:olePortalLink green="true" displayTitle="true" title="Affiliation Type" url="${ConfigProperties.kr.url}/lookup.do?methodToCall=start&businessObjectClassName=org.kuali.rice.kim.impl.identity.affiliation.EntityAffiliationTypeBo&docFormKey=88888888&returnLocation=${ConfigProperties.application.url}/portal.do&hideReturnLink=true" /><br />
        <portal:olePortalLink green="true" displayTitle="true" title="Campus Type" url="${ConfigProperties.kr.url}/lookup.do?methodToCall=start&businessObjectClassName=org.kuali.rice.location.impl.campus.CampusTypeBo&docFormKey=88888888&returnLocation=${ConfigProperties.application.url}/portal.do&hideReturnLink=true" /><br />
        <portal:olePortalLink green="true" displayTitle="true" title="Citizenship Status" url="${ConfigProperties.kr.url}/lookup.do?methodToCall=start&businessObjectClassName=org.kuali.rice.kim.impl.identity.citizenship.EntityCitizenshipStatusBo&docFormKey=88888888&returnLocation=${ConfigProperties.application.url}/portal.do&hideReturnLink=true" /><br />
        <portal:olePortalLink green="true" displayTitle="true" title="Email Type" url="${ConfigProperties.kr.url}/lookup.do?methodToCall=start&businessObjectClassName=org.kuali.rice.kim.impl.identity.email.EntityEmailTypeBo&docFormKey=88888888&returnLocation=${ConfigProperties.application.url}/portal.do&hideReturnLink=true" /><br />
        <portal:olePortalLink grey="true" displayTitle="false" title="Employment Status" url="${ConfigProperties.kr.url}/lookup.do?methodToCall=start&businessObjectClassName=org.kuali.rice.kim.impl.identity.employment.EntityEmploymentStatusBo&docFormKey=88888888&returnLocation=${ConfigProperties.application.url}/portal.do&hideReturnLink=true" /><br />
        <portal:olePortalLink grey="true" displayTitle="false" title="Employment Type" url="${ConfigProperties.kr.url}/lookup.do?methodToCall=start&businessObjectClassName=org.kuali.rice.kim.impl.identity.employment.EntityEmploymentTypeBo&docFormKey=88888888&returnLocation=${ConfigProperties.application.url}/portal.do&hideReturnLink=true" /><br />
        <portal:olePortalLink green="true" displayTitle="true" title="Entity Type" url="${ConfigProperties.kr.url}/lookup.do?methodToCall=start&businessObjectClassName=org.kuali.rice.kim.impl.identity.EntityTypeBo&docFormKey=88888888&returnLocation=${ConfigProperties.application.url}/portal.do&hideReturnLink=true" /><br />
        <portal:olePortalLink green="true" displayTitle="true" title="External Identifier Type" url="${ConfigProperties.kr.url}/lookup.do?methodToCall=start&businessObjectClassName=org.kuali.rice.kim.impl.identity.external.EntityExternalIdentifierTypeBo&docFormKey=88888888&returnLocation=${ConfigProperties.application.url}/portal.do&hideReturnLink=true" /><br />
        <portal:olePortalLink green="true" displayTitle="true" title="Name Type" url="${ConfigProperties.kr.url}/lookup.do?methodToCall=start&businessObjectClassName=org.kuali.rice.kim.impl.identity.name.EntityNameTypeBo&docFormKey=88888888&returnLocation=${ConfigProperties.application.url}/portal.do&hideReturnLink=true" /><br />
        <portal:olePortalLink green="true" displayTitle="true" title="Phone Type" url="${ConfigProperties.kr.url}/lookup.do?methodToCall=start&businessObjectClassName=org.kuali.rice.kim.impl.identity.phone.EntityPhoneTypeBo&docFormKey=88888888&returnLocation=${ConfigProperties.application.url}/portal.do&hideReturnLink=true" /><br />
        <portal:olePortalLink green="true" displayTitle="true" title="Role/Group/Permission/Responsibility Type" url="${ConfigProperties.kr.url}/lookup.do?methodToCall=start&businessObjectClassName=org.kuali.rice.kim.impl.type.KimTypeBo&docFormKey=88888888&returnLocation=${ConfigProperties.application.url}/portal.do&hideReturnLink=true" /><br />
    
</div>
<channel:portalChannelBottom />       

