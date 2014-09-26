/*
 * Copyright 2012 The Kuali Foundation.
 *
 * Licensed under the Educational Community License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://www.opensource.org/licenses/ecl2.php
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package org.kuali.ole.coa.service.impl;

import java.util.Collections;
import java.util.List;
import java.util.Map;

import org.apache.log4j.Level;
import org.apache.log4j.Logger;
import org.kuali.ole.KualiTestBase;
import org.kuali.ole.TestUtils;
import org.kuali.ole.coa.identity.OrgReviewRole;
import org.kuali.ole.fixture.UserNameFixture;
import org.kuali.ole.sys.OLEConstants;
import org.kuali.ole.sys.context.SpringContext;
import org.kuali.ole.sys.dataaccess.UnitTestSqlDao;
import org.kuali.ole.sys.identity.OleKimAttributes;
import org.kuali.rice.core.api.CoreApiServiceLocator;
import org.kuali.rice.core.api.criteria.PredicateUtils;
import org.kuali.rice.core.api.criteria.QueryByCriteria;
import org.kuali.rice.core.api.datetime.DateTimeService;
import org.kuali.rice.core.api.delegation.DelegationType;
import org.kuali.rice.core.api.membership.MemberType;
import org.kuali.rice.core.api.util.type.KualiDecimal;
import org.kuali.rice.kew.api.action.ActionRequestPolicy;
import org.kuali.rice.kew.api.action.ActionRequestType;
import org.kuali.rice.kim.api.KimConstants;
import org.kuali.rice.kim.api.common.delegate.DelegateMember;
import org.kuali.rice.kim.api.identity.Person;
import org.kuali.rice.kim.api.identity.PersonService;
import org.kuali.rice.kim.api.responsibility.Responsibility;
import org.kuali.rice.kim.api.responsibility.ResponsibilityService;
import org.kuali.rice.kim.api.role.Role;
import org.kuali.rice.kim.api.role.RoleMember;
import org.kuali.rice.kim.api.role.RoleMemberQueryResults;
import org.kuali.rice.kim.api.role.RoleResponsibility;
import org.kuali.rice.kim.api.role.RoleResponsibilityAction;
import org.kuali.rice.kim.api.role.RoleService;
import org.kuali.rice.kim.api.services.KimApiServiceLocator;
import org.kuali.rice.kim.api.type.KimTypeInfoService;

import static junit.framework.Assert.assertEquals;
import static junit.framework.TestCase.assertFalse;
import static org.junit.Assert.assertNotNull;

public class OrgReviewRoleTestBase extends KualiTestBase {

    protected OrgReviewRoleServiceImpl orgReviewRoleService;
    protected RoleService roleService;
    protected ResponsibilityService responsibilityService;
    protected KimTypeInfoService kimTypeInfoService;
    protected PersonService personService;
    protected DateTimeService dateTimeService;
    protected Role orgHierRole;
    protected Role acctHierRole;

    @Override
    public void setUp() throws Exception {
        super.setUp();
        orgReviewRoleService =  (OrgReviewRoleServiceImpl) TestUtils.getUnproxiedService("orgReviewRoleService");
        roleService = KimApiServiceLocator.getRoleService();
        responsibilityService = KimApiServiceLocator.getResponsibilityService();
        kimTypeInfoService = KimApiServiceLocator.getKimTypeInfoService();
        personService = KimApiServiceLocator.getPersonService();
        dateTimeService = CoreApiServiceLocator.getDateTimeService();
        orgHierRole = roleService.getRoleByNamespaceCodeAndName(OLEConstants.SysKimApiConstants.ORGANIZATION_REVIEWER_ROLE_NAMESPACECODE, OLEConstants.SysKimApiConstants.ORGANIZATION_REVIEWER_ROLE_NAME);
        acctHierRole = roleService.getRoleByNamespaceCodeAndName(OLEConstants.SysKimApiConstants.ORGANIZATION_REVIEWER_ROLE_NAMESPACECODE, OLEConstants.SysKimApiConstants.ACCOUNTING_REVIEWER_ROLE_NAME);
        Logger.getLogger(OrgReviewRoleServiceImpl.class).setLevel(Level.DEBUG);
    }

    protected OrgReviewRole buildOrgHierData() {
        OrgReviewRole orr = new OrgReviewRole();

        orr.setRoleId( orgHierRole.getId() );
        orr.setKimTypeId( orgHierRole.getKimTypeId() );
        orr.setRoleName(orgHierRole.getName());
        orr.setNamespaceCode(orgHierRole.getNamespaceCode());

        orr.setChartOfAccountsCode("BL");
        orr.setOrganizationCode("PSY");
        //orr.setOverrideCode("");
        //orr.setFromAmount(KualiDecimal.ZERO);
        //orr.setToAmount(new KualiDecimal("5000.00"));
        orr.setFinancialSystemDocumentTypeCode("OLE_ACCT");

        orr.setActionTypeCode(ActionRequestType.APPROVE.getCode());
        orr.setPriorityNumber("");
        orr.setActionPolicyCode(ActionRequestPolicy.FIRST.getCode());
        orr.setForceAction(false);

        orr.setMemberTypeCode(MemberType.PRINCIPAL.getCode());
        Person p = UserNameFixture.khuntley.getPerson();
        orr.setPrincipalMemberPrincipalName(p.getPrincipalName());
        orr.setPrincipalMemberPrincipalId(p.getPrincipalId());

        //orr.setActiveFromDate(null);
        //orr.setActiveToDate(null);
        return orr;
    }

    protected OrgReviewRole buildAcctOrgHierData() {
        OrgReviewRole orr = new OrgReviewRole();

        orr.setRoleId( acctHierRole.getId() );
        orr.setKimTypeId( acctHierRole.getKimTypeId() );
        orr.setRoleName(acctHierRole.getName());
        orr.setNamespaceCode(acctHierRole.getNamespaceCode());

        orr.setChartOfAccountsCode("BL");
        orr.setOrganizationCode("PSY");
        orr.setOverrideCode("");
        orr.setFromAmount(KualiDecimal.ZERO);
        orr.setToAmount(new KualiDecimal("5000.00"));
        orr.setFinancialSystemDocumentTypeCode("DI");

        orr.setActionTypeCode(ActionRequestType.APPROVE.getCode());
        orr.setPriorityNumber("1");
        orr.setActionPolicyCode(ActionRequestPolicy.FIRST.getCode());
        orr.setForceAction(false);

        orr.setMemberTypeCode(MemberType.PRINCIPAL.getCode());
        Person p = UserNameFixture.khuntley.getPerson();
        orr.setPrincipalMemberPrincipalName(p.getPrincipalName());
        orr.setPrincipalMemberPrincipalId(p.getPrincipalId());

        //orr.setActiveFromDate(null);
        //orr.setActiveToDate(null);
        return orr;
    }

    protected OrgReviewRole buildOrgHierDelegateTypeData( DelegationType delegationType ) {
        OrgReviewRole orr = new OrgReviewRole();
        orr.setDelegate(true);
        orr.setRoleId( orgHierRole.getId() );
        orr.setKimTypeId( orgHierRole.getKimTypeId() );
        orr.setRoleName(orgHierRole.getName());
        orr.setNamespaceCode(orgHierRole.getNamespaceCode());
        orr.setDelegationTypeCode(delegationType.getCode());

        orr.setChartOfAccountsCode("BL");
        orr.setOrganizationCode("PSY");
        //orr.setOverrideCode("");
        //orr.setFromAmount(KualiDecimal.ZERO);
        //orr.setToAmount(new KualiDecimal("5000.00"));
        orr.setFinancialSystemDocumentTypeCode("OLE_ACCT");

        orr.setActionTypeCode(ActionRequestType.APPROVE.getCode());
        orr.setPriorityNumber("");
        orr.setActionPolicyCode(ActionRequestPolicy.FIRST.getCode());
        orr.setForceAction(false);

        orr.setMemberTypeCode(MemberType.PRINCIPAL.getCode());
        Person p = UserNameFixture.rorenfro.getPerson();
        orr.setPrincipalMemberPrincipalName(p.getPrincipalName());
        orr.setPrincipalMemberPrincipalId(p.getPrincipalId());

        //orr.setActiveFromDate(null);
        //orr.setActiveToDate(null);
        return orr;
    }

    protected OrgReviewRole buildAcctHierDelegateTypeData( DelegationType delegationType ) {
        OrgReviewRole orr = new OrgReviewRole();
        orr.setDelegate(true);
        orr.setRoleId( orgHierRole.getId() );
        orr.setKimTypeId( orgHierRole.getKimTypeId() );
        orr.setRoleName(orgHierRole.getName());
        orr.setNamespaceCode(orgHierRole.getNamespaceCode());
        orr.setDelegationTypeCode(delegationType.getCode());

        orr.setChartOfAccountsCode("BL");
        orr.setOrganizationCode("PSY");
        orr.setOverrideCode("");
        // delegates don't have amounts
//        orr.setFromAmount(KualiDecimal.ZERO);
//        orr.setToAmount(new KualiDecimal("5000.00"));
        orr.setFinancialSystemDocumentTypeCode("DI");

        orr.setActionTypeCode(ActionRequestType.APPROVE.getCode());
        orr.setPriorityNumber("");
        orr.setActionPolicyCode(ActionRequestPolicy.FIRST.getCode());
        orr.setForceAction(false);

        orr.setMemberTypeCode(MemberType.PRINCIPAL.getCode());
        Person p = UserNameFixture.rorenfro.getPerson();
        orr.setPrincipalMemberPrincipalName(p.getPrincipalName());
        orr.setPrincipalMemberPrincipalId(p.getPrincipalId());

        //orr.setActiveFromDate(null);
        //orr.setActiveToDate(null);
        return orr;
    }

    protected int getNumberOfResponsibilitiesWithRoleMemberLevelActions( Role role ) {
        List<RoleResponsibility> roleResponsibilities = roleService.getRoleResponsibilities(role.getId());
        assertNotNull( "There should have been responsibilities assigned to the role (was null)", roleResponsibilities );
        assertFalse( "There should have been responsibilities assigned to the role", roleResponsibilities.isEmpty() );
        int num = 0;
        for ( RoleResponsibility rr : roleResponsibilities ) {
            Responsibility r = responsibilityService.getResponsibility(rr.getResponsibilityId());
            if ( Boolean.parseBoolean( r.getAttributes().get(KimConstants.AttributeConstants.ACTION_DETAILS_AT_ROLE_MEMBER_LEVEL) ) ) {
                num++;
            }
        }
        return num;
    }

    protected void checkForMatchingRoleMember( OrgReviewRole orr, Role role ) {
        RoleMemberQueryResults roleMembers = roleService.findRoleMembers(QueryByCriteria.Builder.fromPredicates( PredicateUtils.convertMapToPredicate(Collections.singletonMap(KimConstants.PrimaryKeyConstants.ID, orr.getRoleMemberId()))));
        assertNotNull( "Returned role members object should not have been null", roleMembers );
        assertEquals( "One result should have been returned", 1, roleMembers.getResults().size() );
        RoleMember rm = roleMembers.getResults().get(0);
        matchOrgReviewRoleToRoleMember(orr, rm, role);
    }

    protected void checkForMatchingDelegationMember( OrgReviewRole orr, Role role ) {
        DelegateMember dm = roleService.getDelegationMemberById(orr.getDelegationMemberId());
        assertNotNull( "Returned delegation member object should not have been null", dm );
        matchOrgReviewRoleToDelegationMember(orr, dm, role);
    }

    protected void matchOrgReviewRoleToRoleMember( OrgReviewRole orr, RoleMember rm, Role role ) {
        System.err.println( "RoleMember: " + rm );
        System.err.println( "OrgReviewRole: " + orr );

        assertEquals( "RoleMember id should be that requested", orr.getRoleMemberId(), rm.getId() );
        assertEquals( "RoleMember's ID should match the inputs", orr.getPrincipalMemberPrincipalId(), rm.getMemberId() );
        assertEquals( "Role should be " + role.getName(), role.getId(), rm.getRoleId() );

        Map<String,String> attr = rm.getAttributes();
        assertNotNull( "The role member attributes should not have been null", rm.getAttributes() );
        assertEquals("Chart attrib is incorrect", orr.getChartOfAccountsCode(), attr.get(OleKimAttributes.CHART_OF_ACCOUNTS_CODE));
        assertEquals("Org attrib is incorrect", orr.getOrganizationCode(), attr.get(OleKimAttributes.ORGANIZATION_CODE));
        assertEquals("Doc Type attrib is incorrect", orr.getFinancialSystemDocumentTypeCode(), attr.get(KimConstants.AttributeConstants.DOCUMENT_TYPE_NAME));
        assertEquals("From amount is incorrect", orr.getFromAmountStr(), attr.get(OleKimAttributes.FROM_AMOUNT));
        assertEquals("To amount is incorrect", orr.getToAmountStr(), attr.get(OleKimAttributes.TO_AMOUNT));

        List<RoleResponsibilityAction> respActions = KimApiServiceLocator.getRoleService().getRoleMemberResponsibilityActions(rm.getId());
        assertNotNull( "Role resp actions should not be null", respActions );
        assertFalse( "Role resp actions should not be empty", respActions.isEmpty() );
        assertEquals( "There should only be one RoleRespAction", 1, respActions.size() );

        RoleResponsibilityAction rra = respActions.get(0);
        assertEquals( "The responsibility type does not match", orr.getActionTypeCode(), rra.getActionTypeCode() );
        assertEquals( "The responsibility policy does not match", orr.getActionPolicyCode(), rra.getActionPolicyCode() );
        assertEquals( "The role resp ID is not correct", "*", rra.getRoleResponsibilityId() );
    }

    protected void matchOrgReviewRoleToDelegationMember( OrgReviewRole orr, DelegateMember dm, Role role ) {
        System.err.println( "DelegateMember: " + dm );
        System.err.println( "OrgReviewRole: " + orr );

        assertEquals( "DelegateMember id should be that requested", orr.getDelegationMemberId(), dm.getDelegationMemberId() );
        assertEquals( "DelegateMember's ID should match the inputs", orr.getPrincipalMemberPrincipalId(), dm.getMemberId() );
        assertEquals( "DelegateMember's role member ID should match the inputs", orr.getRoleMemberId(), dm.getRoleMemberId() );

        Map<String,String> attr = dm.getAttributes();
        assertNotNull( "The delegate member's attributes should not have been null", dm.getAttributes() );
        assertEquals("Chart attrib is incorrect", orr.getChartOfAccountsCode(), attr.get(OleKimAttributes.CHART_OF_ACCOUNTS_CODE));
        assertEquals("Org attrib is incorrect", orr.getOrganizationCode(), attr.get(OleKimAttributes.ORGANIZATION_CODE));
        assertEquals("Doc Type attrib is incorrect", orr.getFinancialSystemDocumentTypeCode(), attr.get(KimConstants.AttributeConstants.DOCUMENT_TYPE_NAME));
        assertEquals("From amount is incorrect", orr.getFromAmountStr(), attr.get(OleKimAttributes.FROM_AMOUNT));
        assertEquals("To amount is incorrect", orr.getToAmountStr(), attr.get(OleKimAttributes.TO_AMOUNT));
    }

    protected void dumpQueryResultsToErr( String sql ) {
        List<Map<String,?>> queryResults = SpringContext.getBean(UnitTestSqlDao.class,"riceUnitTestSqlDao").sqlSelect(sql);
        System.err.println( sql );
        for ( Map<String,?> row : queryResults ) {
            System.err.println( row );
        }
    }

}
