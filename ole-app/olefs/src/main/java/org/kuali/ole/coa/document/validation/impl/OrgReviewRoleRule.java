/*
 * Copyright 2007-2009 The Kuali Foundation
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

import java.util.Collections;
import java.util.Date;
import java.util.List;
import java.util.Map;

import org.apache.commons.lang.StringUtils;
import org.apache.log4j.Logger;
import org.kuali.ole.coa.identity.OleKimDocumentAttributeData;
import org.kuali.ole.coa.identity.OrgReviewRole;
import org.kuali.ole.coa.service.OrgReviewRoleService;
import org.kuali.ole.sys.OLEConstants;
import org.kuali.ole.sys.OLEKeyConstants;
import org.kuali.ole.sys.context.SpringContext;
import org.kuali.ole.sys.identity.OleKimAttributes;
import org.kuali.ole.sys.util.KfsDateUtils;
import org.kuali.rice.core.api.criteria.PredicateUtils;
import org.kuali.rice.core.api.criteria.QueryByCriteria;
import org.kuali.rice.core.api.util.type.KualiDecimal;
import org.kuali.rice.kim.api.KimConstants;
import org.kuali.rice.kim.api.common.delegate.DelegateMember;
import org.kuali.rice.kim.api.role.DelegateMemberQueryResults;
import org.kuali.rice.kim.api.role.RoleMember;
import org.kuali.rice.kim.api.role.RoleMembership;
import org.kuali.rice.kim.api.services.KimApiServiceLocator;
import org.kuali.rice.kns.document.MaintenanceDocument;
import org.kuali.rice.kns.maintenance.rules.MaintenanceDocumentRuleBase;

/**
 * This class represents the business rules for the maintenance of {@link AccountGlobal} business objects
 */
public class OrgReviewRoleRule extends MaintenanceDocumentRuleBase {
    private static final Logger LOG = Logger.getLogger(OrgReviewRoleRule.class);

    private transient static OrgReviewRoleService orgReviewRoleService;

    /**
     * Need to override to avoid the primary key check which (wrongly) assumes that the object's PKs can be found in the persistence service.
     *
     * @see org.kuali.rice.kns.maintenance.rules.MaintenanceDocumentRuleBase#processGlobalSaveDocumentBusinessRules(org.kuali.rice.kns.document.MaintenanceDocument)
     */
    @Override
    protected boolean processGlobalSaveDocumentBusinessRules(MaintenanceDocument document) {
        return dataDictionaryValidate(document);
    }

    @Override
    protected boolean processCustomRouteDocumentBusinessRules(MaintenanceDocument document) {
        boolean valid = super.processCustomRouteDocumentBusinessRules(document);
        OrgReviewRole orr = (OrgReviewRole)document.getNewMaintainableObject().getBusinessObject();
        if(!orr.hasAnyMember()){
            valid = false;
            putFieldError( OrgReviewRole.PRINCIPAL_NAME_FIELD_NAME, OLEKeyConstants.NO_MEMBER_SELECTED);
        } else{
            getOrgReviewRoleService().validateDocumentType(orr.getFinancialSystemDocumentTypeCode());
            valid &= validateRoleMember(orr);
            valid &= validateAmounts(orr);
            valid &= validateDates(orr, document.isEdit());
            // skip these validations if there are other fundamental problems
            if ( valid ) {
                if( orr.isDelegate() ) {
                    valid &= validateDelegation(orr, document.isEdit());
                } else {
                    if ( !document.isEdit() ) {
                        valid &= verifyUniqueRoleMembership(orr);
                    }
                }
            }
        }
        return valid;
    }

    protected boolean validateDates( OrgReviewRole orr, boolean editingExistingRecord ) {
        boolean valid = true;
        Date today = KfsDateUtils.clearTimeFields(getDateTimeService().getCurrentDate());
        Date startDate = orr.getActiveFromDate();
        Date endDate = orr.getActiveToDate();
        // we only need to validate the start date when creating a new record
        if ( !editingExistingRecord ) {
            if ( startDate == null ) {
                orr.setActiveFromDate(today);
            } else {
                if ( startDate.before(today) ) {
                    String label = getDataDictionaryService().getAttributeLabel(OrgReviewRole.class, OrgReviewRole.ACTIVE_FROM_DATE);
                    putFieldError( OrgReviewRole.ACTIVE_FROM_DATE, "error.document.orgReview.invalidStartDate", label);
                    valid = false;
                }
            }
        }
        // end date must be after start date at all times
        if ( endDate != null && startDate != null ) {
            if ( startDate.after(endDate) ) {
                String label = getDataDictionaryService().getAttributeLabel(OrgReviewRole.class, OrgReviewRole.ACTIVE_TO_DATE);
                putFieldError(OrgReviewRole.ACTIVE_TO_DATE, "error.document.orgReview.invalidDates", label);
                valid = false;
            }
        }
        // end date must always be in the future
        if ( endDate != null && endDate.before(today) ) {
            String label = getDataDictionaryService().getAttributeLabel(OrgReviewRole.class, OrgReviewRole.ACTIVE_TO_DATE);
            putFieldError(OrgReviewRole.ACTIVE_TO_DATE, "error.document.orgReview.invalidEndDate", label);
            valid = false;
        }

        return valid;
    }

    protected boolean validateRoleMember(OrgReviewRole orr){
        boolean valid = true;
        if(StringUtils.isNotEmpty(orr.getPrincipalMemberPrincipalName())){
            if (orr.getPerson() == null) {
                putFieldError(OrgReviewRole.PRINCIPAL_NAME_FIELD_NAME, "error.document.orgReview.invalidPrincipal"
                        , getDataDictionaryService().getAttributeLabel(OrgReviewRole.class, OrgReviewRole.PRINCIPAL_NAME_FIELD_NAME) );
                valid = false;
            }
        }
        if(StringUtils.isNotEmpty(orr.getRoleMemberRoleName())){
            if ( StringUtils.equals( OLEConstants.SysKimApiConstants.ACCOUNTING_REVIEWER_ROLE_NAME, orr.getRoleMemberRoleName())
                    || StringUtils.equals( OLEConstants.SysKimApiConstants.ORGANIZATION_REVIEWER_ROLE_NAME, orr.getRoleMemberRoleName() ) ) {
                putFieldError(OrgReviewRole.ROLE_NAME_FIELD_NAME, "error.document.orgReview.recursiveRole" );
            } else {
                if(orr.getRole() == null){
                    putFieldError(OrgReviewRole.ROLE_NAME_FIELD_NAME, "error.document.orgReview.invalidRole"
                            , new String[] {
                                      getDataDictionaryService().getAttributeLabel(OrgReviewRole.class, OrgReviewRole.ROLE_NAME_FIELD_NAME)
                                    , getDataDictionaryService().getAttributeLabel(OrgReviewRole.class, OrgReviewRole.ROLE_NAME_FIELD_NAMESPACE_CODE)
                                    } );
                    valid = false;
                }
            }
        }
        if(StringUtils.isNotEmpty(orr.getGroupMemberGroupName())){
            if( orr.getGroup() == null ){
                putFieldError(OrgReviewRole.GROUP_NAME_FIELD_NAME, "error.document.orgReview.invalidGroup"
                        , new String[] {
                                  getDataDictionaryService().getAttributeLabel(OrgReviewRole.class, OrgReviewRole.GROUP_NAME_FIELD_NAME)
                                , getDataDictionaryService().getAttributeLabel(OrgReviewRole.class, OrgReviewRole.GROUP_NAME_FIELD_NAMESPACE_CODE)
                                } );
                valid = false;
            }
        }
        return valid;
    }

    protected boolean verifyUniqueDelegationMember(OrgReviewRole orr) {
        for(String roleName: orr.getRoleNamesToConsider()){
            String roleId = KimApiServiceLocator.getRoleService().getRoleIdByNamespaceCodeAndName( OLEConstants.SysKimApiConstants.ORGANIZATION_REVIEWER_ROLE_NAMESPACECODE, roleName);
            DelegateMemberQueryResults results = KimApiServiceLocator.getRoleService().findDelegateMembers(
                    QueryByCriteria.Builder.fromPredicates( PredicateUtils.convertMapToPredicate(Collections.singletonMap(KimConstants.PrimaryKeyConstants.ROLE_MEMBER_ID, orr.getRoleMemberId()))));
            List<DelegateMember> roleDelegationMembers = results.getResults();

            //validate if the newly entered delegation members are already assigned to the role
            if(roleDelegationMembers!=null){
                for(DelegateMember delegationMember: roleDelegationMembers){
                    // ignore if retrieved the current delegation member
                    if ( delegationMember.getDelegationMemberId().equals(orr.getDelegationMemberId() )) {
                        continue;
                    }
                    boolean attributesUnique = areAttributesUnique(orr, delegationMember.getAttributes());
                    if(!attributesUnique
                            && StringUtils.isNotBlank(orr.getMemberId())
                            && orr.getMemberId().equals(delegationMember.getMemberId())
                            && (StringUtils.isNotBlank(orr.getRoleMemberId())
                                    && StringUtils.isNotBlank(delegationMember.getRoleMemberId()))
                            ){
                       putFieldError(orr.getMemberFieldName(), OLEKeyConstants.ALREADY_ASSIGNED_MEMBER);
                       return false;
                    }
                }
            }
        }
        return true;
    }

    protected boolean validateDelgationAmountsWithinRoleMemberBoundaries( OrgReviewRole orr ) {
        boolean valid = true;
        if(StringUtils.isNotEmpty(orr.getRoleMemberId())){
            RoleMember roleMember = getOrgReviewRoleService().getRoleMemberFromKimRoleService(orr.getRoleMemberId());
            List<OleKimDocumentAttributeData> attributes = orr.getAttributeSetAsQualifierList(roleMember.getAttributes());
            if(roleMember!=null && attributes!=null){
                for(OleKimDocumentAttributeData attribute: attributes){
                    if(OleKimAttributes.FROM_AMOUNT.equals(attribute.getKimAttribute().getAttributeName())){
                        KualiDecimal roleMemberFromAmount = new KualiDecimal(attribute.getAttrVal());
                        if(orr.getFromAmount()!=null){
                            KualiDecimal inputFromAmount = orr.getFromAmount();
                            if((roleMemberFromAmount!=null && inputFromAmount==null) || (inputFromAmount!=null && inputFromAmount.isLessThan(roleMemberFromAmount))){
                                putFieldError(OleKimAttributes.FROM_AMOUNT, OLEKeyConstants.FROM_AMOUNT_OUT_OF_RANGE);
                                valid = false;
                            }
                        }
                    }
                    if(OleKimAttributes.TO_AMOUNT.equals(attribute.getKimAttribute().getAttributeName())){
                        KualiDecimal roleMemberToAmount = new KualiDecimal(attribute.getAttrVal());
                        if(orr.getToAmount()!=null){
                            KualiDecimal inputToAmount = orr.getToAmount();
                            if((roleMemberToAmount!=null && inputToAmount==null) || (inputToAmount!=null && inputToAmount.isGreaterThan(roleMemberToAmount))){
                                putFieldError(OleKimAttributes.TO_AMOUNT, OLEKeyConstants.TO_AMOUNT_OUT_OF_RANGE);
                                valid = false;
                            }
                        }
                    }
                }
            }
        }
        return valid;
    }

    protected boolean validateDelegation(OrgReviewRole orr, boolean isEdit){
        boolean valid = true;

        if ( orr.getDelegationType() == null  ) {
            putFieldError( OrgReviewRole.DELEGATION_TYPE_CODE, OLEKeyConstants.ERROR_REQUIRED, "Delegation Type Code");
            valid = false;
        }

        if(!isEdit){
            valid &= verifyUniqueDelegationMember(orr);
        }

        valid &= validateDelgationAmountsWithinRoleMemberBoundaries(orr);
        return valid;
    }

    protected boolean validateAmounts(OrgReviewRole orr){
        boolean valid = true;
        if(orr.getFromAmount()!=null && orr.getToAmount()!=null && orr.getFromAmount().isGreaterThan(orr.getToAmount())){
            putFieldError(OleKimAttributes.FROM_AMOUNT, OLEKeyConstants.FROM_AMOUNT_GREATER_THAN_TO_AMOUNT);
            valid = false;
        }
        return valid;
    }

    /**
     * validate if the newly entered role members are already assigned to the role
     *
     * @param orr
     * @param isEdit
     * @return
     */
    protected boolean verifyUniqueRoleMembership(OrgReviewRole orr){
        for ( String roleName : orr.getRoleNamesToConsider() ) {
            String roleId = KimApiServiceLocator.getRoleService().getRoleIdByNamespaceCodeAndName(
                    OLEConstants.SysKimApiConstants.ORGANIZATION_REVIEWER_ROLE_NAMESPACECODE, roleName);
            List<RoleMembership> roleMembershipInfoList = KimApiServiceLocator.getRoleService().getFirstLevelRoleMembers( Collections.singletonList(roleId));
            if(roleMembershipInfoList!=null){
                for(RoleMembership roleMembershipInfo: roleMembershipInfoList){
                    // ignore if retrieved the current role member
                    if ( roleMembershipInfo.getId().equals(orr.getRoleMemberId() )) {
                        continue;
                    }
                    String memberId = orr.getMemberId();
                    boolean attributesUnique = areAttributesUnique(orr, roleMembershipInfo.getQualifier());
                    if(!attributesUnique && StringUtils.isNotEmpty(memberId) && memberId.equals(roleMembershipInfo.getMemberId()) &&
                            orr.getMemberType().equals(roleMembershipInfo.getType())){
                       putFieldError(orr.getMemberFieldName(), OLEKeyConstants.ALREADY_ASSIGNED_MEMBER);
                       return false;
                    }
                }
            }
        }
        return true;
    }

    protected boolean areAttributesUnique(OrgReviewRole orr, Map<String,String> attributeSet){
        String docTypeName = orr.getFinancialSystemDocumentTypeCode();
        String chartOfAccountCode = orr.getChartOfAccountsCode();
        String organizationCode = orr.getOrganizationCode();
        boolean uniqueAttributes =
            !StringUtils.equals(docTypeName, attributeSet.get(KimConstants.AttributeConstants.DOCUMENT_TYPE_NAME)) ||
            !StringUtils.equals(chartOfAccountCode, attributeSet.get(OleKimAttributes.CHART_OF_ACCOUNTS_CODE)) ||
            !StringUtils.equals(organizationCode, attributeSet.get(OleKimAttributes.ORGANIZATION_CODE));
        return uniqueAttributes;
    }

    protected OrgReviewRoleService getOrgReviewRoleService(){
        if(orgReviewRoleService==null){
            orgReviewRoleService = SpringContext.getBean( OrgReviewRoleService.class );
        }
        return orgReviewRoleService;
    }
}
