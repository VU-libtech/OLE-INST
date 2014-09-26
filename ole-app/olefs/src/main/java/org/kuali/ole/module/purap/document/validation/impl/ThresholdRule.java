/*
 * Copyright 2008 The Kuali Foundation
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
package org.kuali.ole.module.purap.document.validation.impl;

import org.apache.commons.lang.StringUtils;
import org.kuali.ole.coa.businessobject.Chart;
import org.kuali.ole.coa.businessobject.ObjectCode;
import org.kuali.ole.coa.businessobject.Organization;
import org.kuali.ole.coa.businessobject.SubFundGroup;
import org.kuali.ole.coa.service.AccountService;
import org.kuali.ole.coa.service.ChartService;
import org.kuali.ole.module.purap.PurapKeyConstants;
import org.kuali.ole.module.purap.businessobject.ReceivingThreshold;
import org.kuali.ole.module.purap.util.ThresholdField;
import org.kuali.ole.sys.context.SpringContext;
import org.kuali.ole.vnd.businessobject.CommodityCode;
import org.kuali.ole.vnd.businessobject.VendorDetail;
import org.kuali.rice.kns.document.MaintenanceDocument;
import org.kuali.rice.kns.maintenance.rules.MaintenanceDocumentRuleBase;

import java.util.Collection;
import java.util.HashMap;
import java.util.Map;

public class ThresholdRule extends MaintenanceDocumentRuleBase {

    protected static org.apache.log4j.Logger LOG = org.apache.log4j.Logger.getLogger(ThresholdRule.class);
    protected ChartService chartService;
    protected AccountService accountService;
    protected ReceivingThreshold newThreshold;
    protected ReceivingThreshold oldThreshold;

    public ThresholdRule() {
        chartService = SpringContext.getBean(ChartService.class);
        accountService = SpringContext.getBean(AccountService.class);
    }

    @Override
    protected boolean isDocumentValidForSave(MaintenanceDocument document) {
        if (document.isNew() || document.isEdit() || document.isNewWithExisting()) {
            newThreshold = (ReceivingThreshold) document.getNewMaintainableObject().getBusinessObject();
            oldThreshold = document.getOldMaintainableObject() != null ? (ReceivingThreshold) document.getOldMaintainableObject().getBusinessObject() : null;

            //boolean checkDuplicate = newThreshold.isActive(); // we only need to check duplicate if newThreshold is active
            // compare oldThreshold and newThreshold, check if there's any update on the various code fields
            // if yes, then we need to check duplicate of the new threshold among other thresholds; otherwise no need to check            
            boolean checkDuplicate = oldThreshold == null;
            checkDuplicate |= !StringUtils.equals(newThreshold.getChartOfAccountsCode(), oldThreshold.getChartOfAccountsCode());
            checkDuplicate |= !StringUtils.equals(newThreshold.getAccountTypeCode(), oldThreshold.getAccountTypeCode());
            checkDuplicate |= !StringUtils.equals(newThreshold.getSubFundGroupCode(), oldThreshold.getSubFundGroupCode());
            checkDuplicate |= !StringUtils.equals(newThreshold.getPurchasingCommodityCode(), oldThreshold.getPurchasingCommodityCode());
            checkDuplicate |= !StringUtils.equals(newThreshold.getFinancialObjectCode(), oldThreshold.getFinancialObjectCode());
            checkDuplicate |= !StringUtils.equals(newThreshold.getOrganizationCode(), oldThreshold.getOrganizationCode());
            checkDuplicate |= !StringUtils.equals(newThreshold.getVendorNumber(), oldThreshold.getVendorNumber());
            return isValidDocument(newThreshold, checkDuplicate);
        }
        return true;
    }

    protected boolean isValidDocument(ReceivingThreshold newThreshold, boolean checkDuplicate) {

        boolean valid = isValidThresholdCriteria(newThreshold);
        if (!valid) {
            constructFieldError(newThreshold);
            return false;
        }

        valid = isValidChartCode(newThreshold);
        if (valid) {
            valid = isValidSubFund(newThreshold) &&
                    isValidCommodityCode(newThreshold) &&
                    isValidObjectCode(newThreshold) &&
                    isValidOrgCode(newThreshold) &&
                    isValidVendorNumber(newThreshold);
        }

        // check duplication if needed
        if (valid && checkDuplicate) {
            valid = !isDuplicateEntry(newThreshold);
        }
        return valid;
    }

    protected void constructFieldError(ReceivingThreshold threshold) {

        if (StringUtils.isNotBlank(threshold.getAccountTypeCode())) {
            putFieldError(ThresholdField.ACCOUNT_TYPE_CODE.getName(), PurapKeyConstants.INVALID_THRESHOLD_CRITERIA);
        }
        if (StringUtils.isNotBlank(threshold.getSubFundGroupCode())) {
            putFieldError(ThresholdField.SUBFUND_GROUP_CODE.getName(), PurapKeyConstants.INVALID_THRESHOLD_CRITERIA);
        }
        if (StringUtils.isNotBlank(threshold.getPurchasingCommodityCode())) {
            putFieldError(ThresholdField.COMMODITY_CODE.getName(), PurapKeyConstants.INVALID_THRESHOLD_CRITERIA);
        }
        if (StringUtils.isNotBlank(threshold.getFinancialObjectCode())) {
            putFieldError(ThresholdField.FINANCIAL_OBJECT_CODE.getName(), PurapKeyConstants.INVALID_THRESHOLD_CRITERIA);
        }
        if (StringUtils.isNotBlank(threshold.getOrganizationCode())) {
            putFieldError(ThresholdField.ORGANIZATION_CODE.getName(), PurapKeyConstants.INVALID_THRESHOLD_CRITERIA);
        }
        if (StringUtils.isNotBlank(threshold.getVendorNumber())) {
            putFieldError(ThresholdField.VENDOR_NUMBER.getName(), PurapKeyConstants.INVALID_THRESHOLD_CRITERIA);
        }

    }

    protected boolean isValidChartCode(ReceivingThreshold threshold) {
        if (StringUtils.isNotBlank(threshold.getChartOfAccountsCode())) {
            Map pkMap = new HashMap();
            pkMap.put(ThresholdField.CHART_OF_ACCOUNTS_CODE.getName(), newThreshold.getChartOfAccountsCode());

            Chart chart = (Chart) getBoService().findByPrimaryKey(Chart.class, pkMap);
            if (chart == null) {
                putFieldError(ThresholdField.CHART_OF_ACCOUNTS_CODE.getName(), PurapKeyConstants.THRESHOLD_FIELD_INVALID, newThreshold.getChartOfAccountsCode());
                return false;
            } else {
                return true;
            }
        }
        return false;
    }

    protected boolean isValidSubFund(ReceivingThreshold threshold) {

        if (StringUtils.isNotBlank(threshold.getSubFundGroupCode())) {
            Map pkMap = new HashMap();
            pkMap.put(ThresholdField.SUBFUND_GROUP_CODE.getName(), newThreshold.getSubFundGroupCode());
            SubFundGroup subFundGroup = (SubFundGroup) getBoService().findByPrimaryKey(SubFundGroup.class, pkMap);
            if (subFundGroup == null) {
                putFieldError(ThresholdField.SUBFUND_GROUP_CODE.getName(), PurapKeyConstants.THRESHOLD_FIELD_INVALID, newThreshold.getSubFundGroupCode());
                return false;
            }
        }
        return true;
    }

    protected boolean isValidCommodityCode(ReceivingThreshold threshold) {

        if (StringUtils.isNotBlank(threshold.getPurchasingCommodityCode())) {
            Map pkMap = new HashMap();
            pkMap.put(ThresholdField.COMMODITY_CODE.getName(), newThreshold.getPurchasingCommodityCode());

            CommodityCode commodityCode = (CommodityCode) getBoService().findByPrimaryKey(CommodityCode.class, pkMap);
            if (commodityCode == null) {
                putFieldError(ThresholdField.COMMODITY_CODE.getName(), PurapKeyConstants.THRESHOLD_FIELD_INVALID, newThreshold.getPurchasingCommodityCode());
                return false;
            }
        }
        return true;
    }

    protected boolean isValidObjectCode(ReceivingThreshold threshold) {

        if (StringUtils.isNotBlank(threshold.getFinancialObjectCode())) {
            Map pkMap = new HashMap();
            pkMap.put(ThresholdField.FINANCIAL_OBJECT_CODE.getName(), newThreshold.getFinancialObjectCode());

            ObjectCode objectCode = (ObjectCode) getBoService().findByPrimaryKey(ObjectCode.class, pkMap);
            if (objectCode == null) {
                putFieldError(ThresholdField.FINANCIAL_OBJECT_CODE.getName(), PurapKeyConstants.THRESHOLD_FIELD_INVALID, newThreshold.getFinancialObjectCode());
                return false;
            }
        }
        return true;
    }

    protected boolean isValidOrgCode(ReceivingThreshold threshold) {

        if (StringUtils.isNotBlank(threshold.getOrganizationCode())) {
            Map pkMap = new HashMap();
            pkMap.put(ThresholdField.ORGANIZATION_CODE.getName(), newThreshold.getOrganizationCode());

            Organization org = (Organization) getBoService().findByPrimaryKey(Organization.class, pkMap);
            if (org == null) {
                putFieldError(ThresholdField.ORGANIZATION_CODE.getName(), PurapKeyConstants.THRESHOLD_FIELD_INVALID, newThreshold.getOrganizationCode());
                return false;
            }
        }
        return true;
    }

    protected boolean isValidVendorNumber(ReceivingThreshold threshold) {

        if (StringUtils.isNotBlank(threshold.getVendorNumber())) {
            Map keys = new HashMap();
            keys.put(ThresholdField.VENDOR_HEADER_GENERATED_ID.getName(), threshold.getVendorHeaderGeneratedIdentifier());
            keys.put(ThresholdField.VENDOR_DETAIL_ASSIGNED_ID.getName(), threshold.getVendorDetailAssignedIdentifier());

            VendorDetail vendorDetail = (VendorDetail) getBoService().findByPrimaryKey(VendorDetail.class, keys);
            if (vendorDetail == null) {
                putFieldError(ThresholdField.VENDOR_NUMBER.getName(), PurapKeyConstants.THRESHOLD_FIELD_INVALID, newThreshold.getVendorNumber());
                return false;
            }
        }
        return true;
    }

    protected boolean isValidThresholdCriteria(ReceivingThreshold threshold) {

        if (StringUtils.isBlank(threshold.getAccountTypeCode()) &&
                StringUtils.isBlank(threshold.getSubFundGroupCode()) &&
                StringUtils.isBlank(threshold.getPurchasingCommodityCode()) &&
                StringUtils.isBlank(threshold.getFinancialObjectCode()) &&
                StringUtils.isBlank(threshold.getOrganizationCode()) &&
                StringUtils.isBlank(threshold.getVendorNumber())) {
            return true;
        } else if (StringUtils.isNotBlank(threshold.getAccountTypeCode()) &&
                StringUtils.isBlank(threshold.getSubFundGroupCode()) &&
                StringUtils.isBlank(threshold.getPurchasingCommodityCode()) &&
                StringUtils.isBlank(threshold.getFinancialObjectCode()) &&
                StringUtils.isBlank(threshold.getOrganizationCode()) &&
                StringUtils.isBlank(threshold.getVendorNumber())) {
            return true;
        } else if (StringUtils.isBlank(threshold.getAccountTypeCode()) &&
                StringUtils.isNotBlank(threshold.getSubFundGroupCode()) &&
                StringUtils.isBlank(threshold.getPurchasingCommodityCode()) &&
                StringUtils.isBlank(threshold.getFinancialObjectCode()) &&
                StringUtils.isBlank(threshold.getOrganizationCode()) &&
                StringUtils.isBlank(threshold.getVendorNumber())) {
            return true;
        } else if (StringUtils.isBlank(threshold.getAccountTypeCode()) &&
                StringUtils.isBlank(threshold.getSubFundGroupCode()) &&
                StringUtils.isNotBlank(threshold.getPurchasingCommodityCode()) &&
                StringUtils.isBlank(threshold.getFinancialObjectCode()) &&
                StringUtils.isBlank(threshold.getOrganizationCode()) &&
                StringUtils.isBlank(threshold.getVendorNumber())) {
            return true;
        } else if (StringUtils.isBlank(threshold.getAccountTypeCode()) &&
                StringUtils.isBlank(threshold.getSubFundGroupCode()) &&
                StringUtils.isBlank(threshold.getPurchasingCommodityCode()) &&
                StringUtils.isNotBlank(threshold.getFinancialObjectCode()) &&
                StringUtils.isBlank(threshold.getOrganizationCode()) &&
                StringUtils.isBlank(threshold.getVendorNumber())) {
            return true;
        } else if (StringUtils.isBlank(threshold.getAccountTypeCode()) &&
                StringUtils.isBlank(threshold.getSubFundGroupCode()) &&
                StringUtils.isBlank(threshold.getPurchasingCommodityCode()) &&
                StringUtils.isBlank(threshold.getFinancialObjectCode()) &&
                StringUtils.isNotBlank(threshold.getOrganizationCode()) &&
                StringUtils.isBlank(threshold.getVendorNumber())) {
            return true;
        } else if (StringUtils.isBlank(threshold.getAccountTypeCode()) &&
                StringUtils.isBlank(threshold.getSubFundGroupCode()) &&
                StringUtils.isBlank(threshold.getPurchasingCommodityCode()) &&
                StringUtils.isBlank(threshold.getFinancialObjectCode()) &&
                StringUtils.isBlank(threshold.getOrganizationCode()) &&
                StringUtils.isNotBlank(threshold.getVendorNumber())) {
            return true;
        }
        return false;
    }

    protected boolean isDuplicateEntry(ReceivingThreshold newThreshold) {

        Map fieldValues = new HashMap();
        fieldValues.put(ThresholdField.CHART_OF_ACCOUNTS_CODE.getName(), newThreshold.getChartOfAccountsCode());
        //fieldValues.put("active", "Y"); // check duplicates only among active thresholds

        if (StringUtils.isNotBlank(newThreshold.getAccountTypeCode())) {
            fieldValues.put(ThresholdField.ACCOUNT_TYPE_CODE.getName(), newThreshold.getAccountTypeCode());
        } else if (StringUtils.isNotBlank(newThreshold.getSubFundGroupCode())) {
            fieldValues.put(ThresholdField.SUBFUND_GROUP_CODE.getName(), newThreshold.getSubFundGroupCode());
        } else if (StringUtils.isNotBlank(newThreshold.getPurchasingCommodityCode())) {
            fieldValues.put(ThresholdField.COMMODITY_CODE.getName(), newThreshold.getPurchasingCommodityCode());
        } else if (StringUtils.isNotBlank(newThreshold.getFinancialObjectCode())) {
            fieldValues.put(ThresholdField.FINANCIAL_OBJECT_CODE.getName(), newThreshold.getFinancialObjectCode());
        } else if (StringUtils.isNotBlank(newThreshold.getOrganizationCode())) {
            fieldValues.put(ThresholdField.ORGANIZATION_CODE.getName(), newThreshold.getOrganizationCode());
        } else if (StringUtils.isNotBlank(newThreshold.getVendorNumber())) {
            fieldValues.put(ThresholdField.VENDOR_HEADER_GENERATED_ID.getName(), newThreshold.getVendorHeaderGeneratedIdentifier());
            fieldValues.put(ThresholdField.VENDOR_DETAIL_ASSIGNED_ID.getName(), newThreshold.getVendorDetailAssignedIdentifier());
        }

        Collection<ReceivingThreshold> result = (Collection<ReceivingThreshold>) getBoService().findMatching(ReceivingThreshold.class, fieldValues);
        if (result != null && result.size() > 0) {
            putGlobalError(PurapKeyConstants.PURAP_GENERAL_POTENTIAL_DUPLICATE);
            return true;
        }
        return false;
    }
}
