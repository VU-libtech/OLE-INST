/*
 * Copyright 2007 The Kuali Foundation
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
package org.kuali.ole.pdp.service.impl;

import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

import org.kuali.ole.pdp.PdpPropertyConstants;
import org.kuali.ole.pdp.businessobject.PaymentDetail;
import org.kuali.ole.pdp.dataaccess.PaymentDetailDao;
import org.kuali.ole.pdp.service.PaymentDetailService;
import org.kuali.ole.sys.DynamicCollectionComparator;
import org.kuali.ole.sys.service.NonTransactional;
import org.kuali.rice.krad.service.BusinessObjectService;

@NonTransactional
public class PaymentDetailServiceImpl implements PaymentDetailService {
    private static org.apache.log4j.Logger LOG = org.apache.log4j.Logger.getLogger(PaymentDetailServiceImpl.class);

    private PaymentDetailDao paymentDetailDao;
    private BusinessObjectService businessObjectService;
    
    public void setPaymentDetailDao(PaymentDetailDao c) {
        paymentDetailDao = c;
    }

    /**
     * @see org.kuali.ole.pdp.service.PaymentDetailService#getByDisbursementNumber(java.lang.Integer)
     */
    public Iterator getByDisbursementNumber(Integer disbursementNumber) {
        LOG.debug("getByDisbursementNumber() started");
        
        Map fieldValues = new HashMap();
        fieldValues.put(PdpPropertyConstants.PaymentDetail.PAYMENT_DISBURSEMENT_NUMBER, disbursementNumber);
        List<PaymentDetail> paymentDetailByDisbursementNumberList= (List<PaymentDetail>)this.businessObjectService.findMatching(PaymentDetail.class, fieldValues);
        DynamicCollectionComparator.sort(paymentDetailByDisbursementNumberList, PdpPropertyConstants.PaymentDetail.PAYMENT_DISBURSEMENT_FINANCIAL_DOCUMENT_TYPE_CODE, PdpPropertyConstants.PaymentDetail.PAYMENT_DISBURSEMENT_CUST_PAYMENT_DOC_NBR);

        return paymentDetailByDisbursementNumberList.iterator();
    }
    
    /**
     * Returns all PaymentDetail records with the given disbursement number and a group with the given process id, disbursement type, and bank code
     * @param disbursementNumber the disbursement number of the payment details to find
     * @param processId the process id of the payment group of payment details to find
     * @param disbursementType the disbursement type of the payment group of payment details to find
     * @param bankCode the bank code of the payment group of payment details to find
     * @return an iterator of PaymentDetail records matching the given criteria
     */
    public Iterator<PaymentDetail> getByDisbursementNumber(Integer disbursementNumber, Integer processId, String disbursementType, String bankCode) {
        if (LOG.isDebugEnabled()) {
            LOG.debug("getByDisbursementNumber() started");
        }
        
        Map fieldValues = new HashMap();
        fieldValues.put(PdpPropertyConstants.PaymentDetail.PAYMENT_DISBURSEMENT_NUMBER, disbursementNumber);
        fieldValues.put(PdpPropertyConstants.PaymentDetail.PAYMENT_GROUP+"."+PdpPropertyConstants.PaymentGroup.PAYMENT_GROUP_PROCESS_ID, processId);
        fieldValues.put(PdpPropertyConstants.PaymentDetail.PAYMENT_GROUP+"."+PdpPropertyConstants.PaymentGroup.PAYMENT_GROUP_DISBURSEMENT_TYPE_CODE, disbursementType);
        fieldValues.put(PdpPropertyConstants.PaymentDetail.PAYMENT_GROUP+"."+PdpPropertyConstants.PaymentGroup.PAYMENT_GROUP_BANK_CODE, bankCode);
        List<PaymentDetail> paymentDetailByDisbursementNumberList= (List<PaymentDetail>)this.businessObjectService.findMatching(PaymentDetail.class, fieldValues);
        DynamicCollectionComparator.sort(paymentDetailByDisbursementNumberList, PdpPropertyConstants.PaymentDetail.PAYMENT_DISBURSEMENT_FINANCIAL_DOCUMENT_TYPE_CODE, PdpPropertyConstants.PaymentDetail.PAYMENT_DISBURSEMENT_CUST_PAYMENT_DOC_NBR);

        return paymentDetailByDisbursementNumberList.iterator();
    }

    /**
     * @see org.kuali.ole.pdp.service.PaymentDetailService#getUnprocessedCancelledDetails(java.lang.String, java.lang.String)
     */
    public Iterator getUnprocessedCancelledDetails(String organization, List<String> subUnits) {
        LOG.debug("getUnprocessedCancelledDetails() started");

        return paymentDetailDao.getUnprocessedCancelledDetails(organization, subUnits);
    }

    /**
     * @see org.kuali.ole.pdp.service.PaymentDetailService#getUnprocessedPaidDetails(java.lang.String, java.lang.String)
     */
    public Iterator getUnprocessedPaidDetails(String organization, List<String> subUnits) {
        LOG.debug("getUnprocessedPaidDetails() started");

        return paymentDetailDao.getUnprocessedPaidDetails(organization, subUnits);
    }

    public void setBusinessObjectService(BusinessObjectService businessObjectService) {
        this.businessObjectService = businessObjectService;
    }
}
