/*
 * Copyright 2006 The Kuali Foundation
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
package org.kuali.ole.coa.document.validation.impl;


import java.util.HashMap;
import java.util.Map;

import org.junit.Before;
import org.junit.Test;
import org.kuali.ole.KualiTestAssertionUtils;
import org.kuali.ole.coa.businessobject.Organization;
import org.kuali.ole.sys.OLEConstants;
import org.kuali.ole.sys.context.SpringContext;
import org.kuali.rice.kns.document.MaintenanceDocument;
import org.kuali.rice.krad.service.BusinessObjectService;

import static junit.framework.TestCase.assertFalse;
import static junit.framework.TestCase.assertTrue;


public class OrgRuleTest extends ChartRuleTestBase {



    private static final String GOOD_CHART = "UA";
    private static final String BAD_CHART = "ZZ";

    private static class OrgKeys {
        private String chartOfAccountsCode;
        private String organizationCode;
        private String organizationManager;

        private OrgKeys(String chartOfAccountsCode, String organizationCode, String organizationManager) {
            this.chartOfAccountsCode = chartOfAccountsCode;
            this.organizationCode = organizationCode;
            this.organizationManager = organizationManager;
        }

        static OrgKeys getGoodTopLevelOrgKeys() {
            return new OrgKeys(TopLevelOrg.CHART_OF_ACCOUNTS_CODE, TopLevelOrg.ORGANIZATION_CODE, TopLevelOrg.ORGANIZATION_MANAGER);
        }

        static OrgKeys getBadTopLevelOrgKeys() {
            return new OrgKeys(CampusOrg.CHART_OF_ACCOUNTS_CODE, CampusOrg.ORGANIZATION_CODE, CampusOrg.ORGANIZATION_MANAGER);
        }

        static OrgKeys getCampusOrgKeys() {
            return new OrgKeys(CampusOrg.CHART_OF_ACCOUNTS_CODE, CampusOrg.ORGANIZATION_CODE, CampusOrg.ORGANIZATION_MANAGER);
        }

        static OrgKeys getDepartmentOrgKeys() {
            return new OrgKeys(DepartmentOrg.CHART_OF_ACCOUNTS_CODE, DepartmentOrg.ORGANIZATION_CODE, DepartmentOrg.ORGANIZATION_MANAGER);
        }

        private class TopLevelOrg {
            private static final String CHART_OF_ACCOUNTS_CODE = "IU";
            private static final String ORGANIZATION_CODE = "UNIV";
            private static final String ORGANIZATION_MANAGER = "CAMCSWEE";
        }

        private class CampusOrg {
            private static final String CHART_OF_ACCOUNTS_CODE = "FW";
            private static final String ORGANIZATION_CODE = "FW";
            private static final String ORGANIZATION_MANAGER = "MFLANDER";
        }

        private class DepartmentOrg {
            private static final String CHART_OF_ACCOUNTS_CODE = "FW";
            private static final String ORGANIZATION_CODE = "ANTH";
            private static final String ORGANIZATION_MANAGER = "HOATES";
        }
    }

    private OrgRule rule;
    private Organization org;
    private MaintenanceDocument maintDoc;

    public void testDefaultExistenceChecks_chartOfAccounts_good() {
        org = SpringContext.getBean(BusinessObjectService.class).findByPrimaryKey(Organization.class, getPrimaryKeysForTopLevelOrg(OrgKeys.getGoodTopLevelOrgKeys()));
        maintDoc = this.newMaintDoc(org);
        testDefaultExistenceCheck(org, "organizationCode", false);
        KualiTestAssertionUtils.assertGlobalMessageMapEmpty();
    }

    /**
     * This tests makes certain that only one top level heirarchy is allowed.
     */
    @Test
    public void testCheckSimpleRules_topLevelHeirarchy_noMoreThanOne() {
        Organization oldBO = SpringContext.getBean(BusinessObjectService.class).findByPrimaryKey(Organization.class, this.getPrimaryKeysForTopLevelOrg(OrgKeys.getGoodTopLevelOrgKeys()));
        Organization newBO = SpringContext.getBean(BusinessObjectService.class).findByPrimaryKey(Organization.class, this.getPrimaryKeysForTopLevelOrg(OrgKeys.getBadTopLevelOrgKeys()));
        maintDoc = this.newMaintDoc(oldBO, newBO);
        maintDoc.getNewMaintainableObject().setMaintenanceAction(OLEConstants.MAINTENANCE_EDIT_ACTION); // simulate editing
        newBO.setReportsToChartOfAccountsCode(OrgKeys.CampusOrg.CHART_OF_ACCOUNTS_CODE); // simulate trying to create a new top
                                                                                            // level org
        newBO.setReportsToOrganizationCode(OrgKeys.CampusOrg.ORGANIZATION_CODE);
        rule = (OrgRule) this.setupMaintDocRule(maintDoc, OrgRule.class);
        assertFalse(rule.checkSimpleRules(maintDoc)); // we may not add more than one top level org
    }

    /**
     * This test makes certain that a top level heirarchy can be edited in a maintenance doc.
     */
    @Test
    public void testCheckSimpleRules_topLevelHeirarchy_mayEdit() {
        rule = new OrgRule();
        Organization oldBO = SpringContext.getBean(BusinessObjectService.class).findByPrimaryKey(Organization.class, this.getPrimaryKeysForTopLevelOrg(OrgKeys.getGoodTopLevelOrgKeys()));
        Organization newBO = SpringContext.getBean(BusinessObjectService.class).findByPrimaryKey(Organization.class, this.getPrimaryKeysForTopLevelOrg(OrgKeys.getGoodTopLevelOrgKeys()));
        newBO.setReportsToChartOfAccountsCode(OrgKeys.TopLevelOrg.CHART_OF_ACCOUNTS_CODE); // simulate editing top level org
        newBO.setReportsToOrganizationCode(OrgKeys.TopLevelOrg.ORGANIZATION_CODE);
        maintDoc = this.newMaintDoc(oldBO, newBO);
        maintDoc.getNewMaintainableObject().setMaintenanceAction(OLEConstants.MAINTENANCE_EDIT_ACTION); // simulate editing
        rule = (OrgRule) this.setupMaintDocRule(maintDoc, OrgRule.class);
        assertTrue(rule.checkSimpleRules(maintDoc)); // it is okay to edit the top level org
    }

    /**
     * This method assures that on edit, a university level org does not need a default account number.
     */
    @Test
    public void testCheckDefaultAccountNumber_canEditUniversity() {
        Organization oldBO = SpringContext.getBean(BusinessObjectService.class).findByPrimaryKey(Organization.class, this.getPrimaryKeysForTopLevelOrg(OrgKeys.getGoodTopLevelOrgKeys()));
        Organization newBO = SpringContext.getBean(BusinessObjectService.class).findByPrimaryKey(Organization.class, this.getPrimaryKeysForTopLevelOrg(OrgKeys.getGoodTopLevelOrgKeys()));
        newBO.setOrganizationDefaultAccountNumber(null); // simulate no organization default account number
        maintDoc = this.newMaintDoc(oldBO, newBO);
        maintDoc.getNewMaintainableObject().setMaintenanceAction(OLEConstants.MAINTENANCE_EDIT_ACTION); // simulate editing
        rule = (OrgRule) this.setupMaintDocRule(maintDoc, OrgRule.class);
        assertTrue(rule.checkDefaultAccountNumber(maintDoc)); // it is okay for a university to have no default account number
    }

    /**
     * This method assures that on edit, a campus level org does not need a default account number.
     */
    @Test
    public void testCheckDefaultAccountNumber_canEditCampus() {
        Organization oldBO = SpringContext.getBean(BusinessObjectService.class).findByPrimaryKey(Organization.class, this.getPrimaryKeysForTopLevelOrg(OrgKeys.getCampusOrgKeys()));
        Organization newBO = SpringContext.getBean(BusinessObjectService.class).findByPrimaryKey(Organization.class, this.getPrimaryKeysForTopLevelOrg(OrgKeys.getCampusOrgKeys()));
        newBO.setOrganizationDefaultAccountNumber(null); // simulate no organization default account number
        maintDoc = this.newMaintDoc(oldBO, newBO);
        maintDoc.getNewMaintainableObject().setMaintenanceAction(OLEConstants.MAINTENANCE_EDIT_ACTION); // simulate editing
        rule = (OrgRule) this.setupMaintDocRule(maintDoc, OrgRule.class);
        assertTrue(rule.checkDefaultAccountNumber(maintDoc)); // it is okay for a campus to have no default account number
    }

    /**
     * This method assures that on edit, a non-university/non-campus *does* need a default account number.
     */
    @Test
    public void testCheckDefaultAccountNumber_cannotEditDepartment() {
        Organization oldBO = SpringContext.getBean(BusinessObjectService.class).findByPrimaryKey(Organization.class, this.getPrimaryKeysForTopLevelOrg(OrgKeys.getDepartmentOrgKeys()));
        Organization newBO = SpringContext.getBean(BusinessObjectService.class).findByPrimaryKey(Organization.class, this.getPrimaryKeysForTopLevelOrg(OrgKeys.getDepartmentOrgKeys()));
        newBO.setOrganizationDefaultAccountNumber(null); // simulate no organization default account number
        maintDoc = this.newMaintDoc(oldBO, newBO);
        maintDoc.getNewMaintainableObject().setMaintenanceAction(OLEConstants.MAINTENANCE_EDIT_ACTION); // simulate editing
        rule = (OrgRule) this.setupMaintDocRule(maintDoc, OrgRule.class);
        assertFalse(rule.checkDefaultAccountNumber(maintDoc)); // it is NOT okay for a non-university/non-campus to have no default
                                                                // account number
    }

    private Map getPrimaryKeysForTopLevelOrg(OrgKeys org) {
        Map primaryKeys = new HashMap();
        primaryKeys.put("chartOfAccountsCode", org.chartOfAccountsCode);
        primaryKeys.put("organizationCode", org.organizationCode);
        return primaryKeys;
    }
}

