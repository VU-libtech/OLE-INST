/*
 * Copyright 2005-2007 The Kuali Foundation
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
package org.kuali.ole.module.purap.document;

import static junit.framework.TestCase.assertTrue;
import static org.junit.Assert.assertFalse;
import static org.kuali.ole.module.purap.fixture.PurchaseOrderItemAccountsFixture.WITH_DESC_WITH_UOM_WITH_PRICE_WITH_ACCOUNT;
import static org.kuali.ole.sys.document.AccountingDocumentTestUtils.testGetNewDocument_byDocumentClass;

import java.util.ArrayList;
import java.util.List;

import junit.framework.Assert;

import org.apache.commons.lang.StringUtils;
import org.junit.Before;
import org.junit.Test;
import org.kuali.ole.*;
import org.kuali.ole.fixture.UserNameFixture;
import org.kuali.ole.module.purap.businessobject.PurchaseOrderItem;
import org.kuali.ole.module.purap.businessobject.PurchasingItem;
import org.kuali.ole.module.purap.fixture.PurchaseOrderDocumentFixture;
import org.kuali.ole.module.purap.fixture.PurchaseOrderItemAccountsFixture;
import org.kuali.ole.sys.context.SpringContext;
import org.kuali.ole.sys.document.AccountingDocumentTestUtils;
import org.kuali.ole.sys.document.workflow.WorkflowTestUtils;
import org.kuali.rice.kew.api.document.DocumentStatus;
import org.kuali.rice.kim.api.identity.Person;
import org.kuali.rice.kns.service.TransactionalDocumentDictionaryService;
import org.kuali.rice.krad.UserSession;
import org.kuali.rice.krad.dao.DocumentDao;
import org.kuali.rice.krad.service.DocumentService;
import org.kuali.rice.krad.service.impl.DocumentServiceImpl;
import org.kuali.rice.krad.util.GlobalVariables;

/**
 * Used to create and test populated Purchase Order Documents of various kinds.
 */

public class PurchaseOrderDocumentTest extends KFSTestCaseBase {
    public static final Class<PurchaseOrderDocument> DOCUMENT_CLASS = PurchaseOrderDocument.class;
    protected static DocumentServiceImpl documentService = null;

    @Before
    public void setUp() throws Exception {
        super.setUp();
        documentService = (DocumentServiceImpl) SpringContext.getBean("documentService");
        documentService.setDocumentDao((DocumentDao) SpringContext.getBean("documentDao"));
        changeCurrentUser(UserNameFixture.parke);
    }

    private List<PurchaseOrderItemAccountsFixture> getItemParametersFromFixtures() {
        List<PurchaseOrderItemAccountsFixture> list = new ArrayList<PurchaseOrderItemAccountsFixture>();
        list.add(WITH_DESC_WITH_UOM_WITH_PRICE_WITH_ACCOUNT);
        return list;
    }

    private int getExpectedPrePeCount() {
        return 0;
    }

    @Test
    public final void testAddItem() throws Exception {
        List<PurchasingItem> items = new ArrayList<PurchasingItem>();
        for (PurchaseOrderItem item : generateItems()) {
            items.add(item);
        }
        int expectedItemTotal = items.size();
        PurchasingDocumentTestUtils.testAddItem(DocumentTestUtils.createDocument(SpringContext.getBean(DocumentService.class), DOCUMENT_CLASS), items, expectedItemTotal);
    }

    @Test
    public final void testGetNewDocument() throws Exception {
        testGetNewDocument_byDocumentClass(DOCUMENT_CLASS, SpringContext.getBean(DocumentService.class));
    }


    public final void testConvertIntoErrorCorrection_documentAlreadyCorrected() throws Exception {
        AccountingDocumentTestUtils.testConvertIntoErrorCorrection_documentAlreadyCorrected(buildSimpleDocument(), SpringContext.getBean(TransactionalDocumentDictionaryService.class));
    }

   @Test
    public final void testConvertIntoErrorCorrection() throws Exception {
       AccountingDocumentTestUtils.testConvertIntoErrorCorrection(buildSimpleDocument(), getExpectedPrePeCount(), SpringContext.getBean(DocumentService.class), SpringContext.getBean(TransactionalDocumentDictionaryService.class));
    }

    @Test
    public final void testRouteDocument() throws Exception {
        PurchaseOrderDocument poDocument = buildSimpleDocument();
        /*DocumentService documentService = SpringContext.getBean(DocumentService.class);*/
        poDocument.prepareForSave();
        assertFalse("Document should not have been in ENROUTE status.",DocumentStatus.ENROUTE.equals(poDocument.getDocumentHeader().getWorkflowDocument().getStatus()));
        AccountingDocumentTestUtils.routeDocument(poDocument, "test annotation", null, documentService);
        WorkflowTestUtils.waitForDocumentApproval(poDocument.getDocumentNumber());
        assertTrue("Document should now be final.", poDocument.getDocumentHeader().getWorkflowDocument().isEnroute());
    }

    @Test
    public final void testSaveDocument() throws Exception {
        PurchaseOrderDocument poDocument = buildSimpleDocument();
        /*DocumentService documentService = SpringContext.getBean(DocumentService.class);*/
        poDocument.prepareForSave();
        AccountingDocumentTestUtils.saveDocument(poDocument, documentService);
        PurchaseOrderDocument result = (PurchaseOrderDocument) documentService.getByDocumentHeaderId(poDocument.getDocumentNumber());
        assertMatch(poDocument, result);
    }

    // test util methods

    /**
     * Matches two Purchase Order Documents by comparing their most important persistant fields;
     * Fails the assertion if any of these fields don't match.
     */
    public static void assertMatch(PurchaseOrderDocument doc1, PurchaseOrderDocument doc2) {
        // match header
        Assert.assertEquals(doc1.getDocumentNumber(), doc2.getDocumentNumber());
        Assert.assertEquals(doc1.getDocumentHeader().getWorkflowDocument().getDocumentTypeName(), doc2.getDocumentHeader().getWorkflowDocument().getDocumentTypeName());

        // match posting year
        if (StringUtils.isNotBlank(doc1.getPostingPeriodCode()) && StringUtils.isNotBlank(doc2.getPostingPeriodCode())) {
            Assert.assertEquals(doc1.getPostingPeriodCode(), doc2.getPostingPeriodCode());
        }
        Assert.assertEquals(doc1.getPostingYear(), doc2.getPostingYear());

        // match important fields in PO

        Assert.assertEquals(doc1.getVendorHeaderGeneratedIdentifier(), doc2.getVendorHeaderGeneratedIdentifier());
        Assert.assertEquals(doc1.getVendorDetailAssignedIdentifier(), doc2.getVendorDetailAssignedIdentifier());
        Assert.assertEquals(doc1.getVendorName(), doc2.getVendorName());
        Assert.assertEquals(doc1.getVendorNumber(), doc2.getVendorNumber());
        Assert.assertEquals(doc1.getApplicationDocumentStatus(), doc2.getApplicationDocumentStatus());

        Assert.assertEquals(doc1.getChartOfAccountsCode(), doc2.getChartOfAccountsCode());
        Assert.assertEquals(doc1.getOrganizationCode(), doc2.getOrganizationCode());
        Assert.assertEquals(doc1.getDeliveryCampusCode(), doc2.getDeliveryCampusCode());
        Assert.assertEquals(doc1.getDeliveryRequiredDate(), doc2.getDeliveryRequiredDate());
        Assert.assertEquals(doc1.getRequestorPersonName(), doc2.getRequestorPersonName());
        Assert.assertEquals(doc1.getContractManagerCode(), doc2.getContractManagerCode());
        Assert.assertEquals(doc1.getVendorContractName(), doc2.getVendorContractName());
        Assert.assertEquals(doc1.getPurchaseOrderAutomaticIndicator(), doc2.getPurchaseOrderAutomaticIndicator());
        Assert.assertEquals(doc1.getPurchaseOrderTransmissionMethodCode(), doc2.getPurchaseOrderTransmissionMethodCode());

        Assert.assertEquals(doc1.getRequisitionIdentifier(), doc2.getRequisitionIdentifier());
        Assert.assertEquals(doc1.getPurchaseOrderPreviousIdentifier(), doc2.getPurchaseOrderPreviousIdentifier());
        Assert.assertEquals(doc1.isPurchaseOrderCurrentIndicator(), doc2.isPurchaseOrderCurrentIndicator());
        Assert.assertEquals(doc1.getPurchaseOrderCreateTimestamp(), doc2.getPurchaseOrderCreateTimestamp());
        Assert.assertEquals(doc1.getPurchaseOrderLastTransmitTimestamp(), doc2.getPurchaseOrderLastTransmitTimestamp());
    }

    private List<PurchaseOrderItem> generateItems() throws Exception {
        List<PurchaseOrderItem> items = new ArrayList<PurchaseOrderItem>();
        // set items to document
        for (PurchaseOrderItemAccountsFixture itemFixture : getItemParametersFromFixtures()) {
            items.add(itemFixture.populateItem());
        }

        return items;
    }

    public PurchaseOrderDocument buildSimpleDocument() throws Exception {
        return PurchaseOrderDocumentFixture.PO_ONLY_REQUIRED_FIELDS_WITH_COPIES.createPurchaseOrderDocument();
    }

    private UserNameFixture getInitialUserName() {
        return UserNameFixture.rjweiss;
    }

    protected UserNameFixture getTestUserName() {
        return UserNameFixture.rorenfro;
    }

    protected void changeCurrentUser(UserNameFixture sessionUser) throws Exception {
        Person p = sessionUser.getPerson();
        GlobalVariables.setUserSession(new UserSession(p.getPrincipalName()));
    }

    protected void changeCurrentUser(Person p) throws Exception {
        GlobalVariables.setUserSession(new UserSession(p.getPrincipalName()));
    }

}

