package org.kuali.ole.select.controller;

import org.junit.Test;
import org.kuali.ole.KualiTestBase;
import org.kuali.ole.docstore.common.client.DocstoreRestClient;
import org.kuali.ole.fixture.UserNameFixture;
import org.kuali.ole.module.purap.document.PurchaseOrderDocument;
import org.kuali.ole.module.purap.fixture.PurchaseOrderDocumentFixture;
import org.kuali.ole.select.document.OleInvoiceDocument;
import org.kuali.ole.select.document.OlePurchaseOrderDocument;
import org.kuali.ole.select.fixture.OLEInvoiceDocumentFixture;
import org.kuali.ole.select.form.OLEInvoiceForm;
import org.kuali.ole.sys.context.SpringContext;
import org.kuali.ole.sys.document.AccountingDocumentTestUtils;
import org.kuali.rice.krad.dao.DocumentDao;
import org.kuali.rice.krad.service.impl.DocumentServiceImpl;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.validation.BindingResult;
import org.springframework.web.servlet.ModelAndView;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import static junit.framework.Assert.assertNotNull;
import static junit.framework.TestCase.assertEquals;
import static junit.framework.TestCase.assertTrue;

/**
 * Created with IntelliJ IDEA.
 * User: meenau
 * Date: 5/29/14
 * Time: 6:39 PM
 * To change this template use File | Settings | File Templates.
 */

/**
 * Used to create and test populated Invoice Documents of various kinds.
 */

public class OLEInvoiceController_IT extends KualiTestBase {
    @Mock
    private OLEInvoiceForm mockUifFormBase;
    @Mock
    private BindingResult mockBindingResult;
    @Mock
    private HttpServletRequest mockRequest;
    @Mock
    private HttpServletResponse mockResponse;
    @Mock
    private ModelAndView mockModelView;

    public  MockOleInvoiceController mockOleInvoiceController;
    protected static DocumentServiceImpl documentService = null;
    protected OlePurchaseOrderDocument olePurchaseOrderDocument = null;

    public void setUp() throws Exception {
        super.setUp();
        MockitoAnnotations.initMocks(this);
        mockOleInvoiceController = new MockOleInvoiceController();
        documentService = (DocumentServiceImpl) SpringContext.getBean("documentService");
        documentService.setDocumentDao((DocumentDao) SpringContext.getBean("documentDao"));
        changeCurrentUser(UserNameFixture.khuntley);
        //create a simple po document for Invoice test.
        olePurchaseOrderDocument = (OlePurchaseOrderDocument) buildSimplePODocument();
        AccountingDocumentTestUtils.routeDocument(olePurchaseOrderDocument, "test po annotation", null, documentService);
    }

    /**
     * test case for testing save document
     */
    @Test
    @Transactional
    public final void testSaveDocument() throws Exception {
        mockUifFormBase = new OLEInvoiceForm();
        OleInvoiceDocument oleInvoiceDocument = buildSimpleInvoiceDocument();
        String paymentMethodIdentifier = oleInvoiceDocument.getPaymentMethodIdentifier();
        mockUifFormBase.setDocument(oleInvoiceDocument);
        ModelAndView modelAndView =
                mockOleInvoiceController.searchVendor(mockUifFormBase, mockBindingResult, mockRequest, mockResponse);
        assertNotNull(modelAndView);
        modelAndView =
                mockOleInvoiceController.addItem(mockUifFormBase, mockBindingResult, mockRequest, mockResponse);
        assertNotNull(modelAndView);
        oleInvoiceDocument = (OleInvoiceDocument) mockUifFormBase.getDocument();
        oleInvoiceDocument.setPaymentMethodIdentifier(paymentMethodIdentifier);
        mockUifFormBase.setDocument(oleInvoiceDocument);
        modelAndView = mockOleInvoiceController.addPoItems(mockUifFormBase, mockBindingResult, mockRequest, mockResponse);
        assertNotNull(modelAndView);

        modelAndView = mockOleInvoiceController.save(mockUifFormBase, mockBindingResult, mockRequest, mockResponse);
        assertNotNull(modelAndView);
        OLEInvoiceForm oleInvoiceForm = mockUifFormBase;
        oleInvoiceDocument = (OleInvoiceDocument) oleInvoiceForm.getDocument();
        OleInvoiceDocument result = (OleInvoiceDocument) documentService.getByDocumentHeaderId(oleInvoiceDocument.getDocumentNumber());
        assertTrue("Document should now be saved.",result.getDocumentHeader().getWorkflowDocument().getStatus().getLabel().equalsIgnoreCase("SAVED"));
        assertEquals(oleInvoiceDocument.getDocumentNumber(), result.getDocumentNumber());
    }

    /**
     * test case for testing route document
     */
    @Test
    @Transactional
    public final void testRouteDocument() throws Exception {
        mockUifFormBase = new OLEInvoiceForm();
        OleInvoiceDocument oleInvoiceDocument = buildSimpleInvoiceDocument();
        String paymentMethodIdentifier = oleInvoiceDocument.getPaymentMethodIdentifier();
        mockUifFormBase.setDocument(oleInvoiceDocument);
        ModelAndView modelAndView =
                mockOleInvoiceController.searchVendor(mockUifFormBase, mockBindingResult, mockRequest, mockResponse);
        assertNotNull(modelAndView);
        modelAndView =
                mockOleInvoiceController.addItem(mockUifFormBase, mockBindingResult, mockRequest, mockResponse);
        assertNotNull(modelAndView);
        oleInvoiceDocument = (OleInvoiceDocument) mockUifFormBase.getDocument();
        oleInvoiceDocument.setPaymentMethodIdentifier(paymentMethodIdentifier);
        mockUifFormBase.setDocument(oleInvoiceDocument);
        modelAndView = mockOleInvoiceController.addPoItems(mockUifFormBase, mockBindingResult, mockRequest, mockResponse);
        assertNotNull(modelAndView);

        modelAndView = mockOleInvoiceController.route(mockUifFormBase, mockBindingResult, mockRequest, mockResponse);
        assertNotNull(modelAndView);

        OLEInvoiceForm oleInvoiceForm = mockUifFormBase;
        oleInvoiceDocument = (OleInvoiceDocument) oleInvoiceForm.getDocument();
        OleInvoiceDocument result = (OleInvoiceDocument) documentService.getByDocumentHeaderId(oleInvoiceDocument.getDocumentNumber());
        assertTrue(result.getDocumentHeader().getWorkflowDocument().getStatus().getLabel().equalsIgnoreCase("ENROUTE"));
        assertEquals(oleInvoiceDocument.getDocumentNumber(), result.getDocumentNumber());
    }

    /**
     * test case for testing Invoice document approval
     */
    @Test
    @Transactional
    public final void testApproveDocument() throws Exception {
        mockUifFormBase = new OLEInvoiceForm();
        OleInvoiceDocument oleInvoiceDocument = buildSimpleInvoiceDocument();
        String paymentMethodIdentifier = oleInvoiceDocument.getPaymentMethodIdentifier();
        mockUifFormBase.setDocument(oleInvoiceDocument);
        ModelAndView modelAndView =
                mockOleInvoiceController.searchVendor(mockUifFormBase, mockBindingResult, mockRequest, mockResponse);
        assertNotNull(modelAndView);
        modelAndView =
                mockOleInvoiceController.addItem(mockUifFormBase, mockBindingResult, mockRequest, mockResponse);
        assertNotNull(modelAndView);
        oleInvoiceDocument = (OleInvoiceDocument) mockUifFormBase.getDocument();
        oleInvoiceDocument.setPaymentMethodIdentifier(paymentMethodIdentifier);
        mockUifFormBase.setDocument(oleInvoiceDocument);
        modelAndView = mockOleInvoiceController.addPoItems(mockUifFormBase, mockBindingResult, mockRequest, mockResponse);
        assertNotNull(modelAndView);

        modelAndView = mockOleInvoiceController.blanketApprove(mockUifFormBase, mockBindingResult, mockRequest, mockResponse);
        assertNotNull(modelAndView);

        OLEInvoiceForm oleInvoiceForm = mockUifFormBase;
        oleInvoiceDocument = (OleInvoiceDocument) oleInvoiceForm.getDocument();
        OleInvoiceDocument result = (OleInvoiceDocument) documentService.getByDocumentHeaderId(oleInvoiceDocument.getDocumentNumber());
        assertTrue(result.getDocumentHeader().getWorkflowDocument().getStatus().getLabel().equalsIgnoreCase("PROCESSED"));
        assertEquals(oleInvoiceDocument.getDocumentNumber(), result.getDocumentNumber());
    }

    /**
     * creates a simple PO document with required fields
     */
    public PurchaseOrderDocument buildSimplePODocument() throws Exception {
        return PurchaseOrderDocumentFixture.PO_ONLY_REQUIRED_FIELDS_WITH_COPIES.createPurchaseOrderDocument();
    }

    /**
     * creates a simple Invoice document with required fields
     */
    public OleInvoiceDocument buildSimpleInvoiceDocument() throws Exception {
        return OLEInvoiceDocumentFixture.BASIC_ONLY_REQ_FIELDS_FOR_INVOICE.createInvoiceDocument(olePurchaseOrderDocument.getPurapDocumentIdentifier().toString());
    }

    private class MockOleInvoiceController extends OLEInvoiceController {

    }
}
