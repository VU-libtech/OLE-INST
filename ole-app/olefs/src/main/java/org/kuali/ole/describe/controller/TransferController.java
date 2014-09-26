package org.kuali.ole.describe.controller;

import com.google.common.base.Joiner;
import org.apache.log4j.Logger;
import org.kuali.ole.OLEConstants;
import org.kuali.ole.describe.bo.BoundwithSelection;
import org.kuali.ole.describe.bo.DocumentSelectionTree;
import org.kuali.ole.describe.bo.DocumentTreeNode;
import org.kuali.ole.describe.bo.TransferRightToLeft;
import org.kuali.ole.describe.form.BoundwithForm;
import org.kuali.ole.docstore.common.document.BibTree;
import org.kuali.ole.docstore.common.document.Holdings;
import org.kuali.ole.docstore.common.exception.DocstoreException;
import org.kuali.ole.docstore.model.xmlpojo.ingest.Response;
import org.kuali.ole.docstore.model.xmlpojo.ingest.ResponseDocument;
import org.kuali.ole.docstore.model.xstream.ingest.ResponseHandler;
import org.kuali.ole.select.util.TransferUtil;
import org.kuali.rice.core.api.util.tree.Node;
import org.kuali.rice.core.api.util.tree.Tree;
import org.kuali.rice.krad.util.GlobalVariables;
import org.kuali.rice.krad.util.KRADConstants;
import org.kuali.rice.krad.web.form.UifFormBase;
import org.springframework.stereotype.Controller;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.servlet.ModelAndView;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

/**
 * Created with IntelliJ IDEA.
 * User: ?
 * Date: 12/29/12
 * Time: 12:03 PM
 * To change this template use File | Settings | File Templates.
 */
@Controller
@RequestMapping(value = "/transferController")
public class TransferController
        extends BoundwithController {
    private static final Logger LOG = Logger.getLogger(TransferController.class);

    @Override
    @RequestMapping(params = "methodToCall=start")
    public ModelAndView start(@ModelAttribute("KualiForm") UifFormBase form, BindingResult result,
                              HttpServletRequest request, HttpServletResponse response) {
        LOG.debug("Inside the workbenchForm--TransferController start method");
        if (request.getSession().getAttribute("LeftList") != null && request.getSession().getAttribute("RightList") != null) {
            request.getSession().removeAttribute("LeftList");
            request.getSession().removeAttribute("RightList");
        }
        return super.start(form, result, request, response);
    }


    @RequestMapping(params = "methodToCall=selectRootNodeItems")
    public ModelAndView selectRootNodeItems(@ModelAttribute("KualiForm") UifFormBase form, BindingResult result,
                                            HttpServletRequest request, HttpServletResponse response) throws Exception {
        List<String> bibIdentifierListForTree1 = new ArrayList<String>();
        List<String> bibInstanceListForTree1 = new ArrayList<String>();
        List<String> bibItemListForTree1 = new ArrayList<String>();
        List<String> bibIdentifierListForTree2 = new ArrayList<String>();
        List<String> destBibIdentifierListForTree2 = new ArrayList<String>();
        List<String> destInstanceIdentifierListForTree2 = new ArrayList<String>();
        List<String> itemsSelectedListForRightTree = new ArrayList<String>();
        List<String> bibIdentifiersSelectedListForLeftTree = new ArrayList<String>();
        List<String> bibIdentifiersListWhenInstanceSelectedForLeftTree = new ArrayList<String>();
        List<String> instanceIdentifiersToDelete = new ArrayList<String>();
        boolean transferInstance = true;
        //  StringBuffer stringBufferLeftTree=new StringBuffer();
        //StringBuffer stringBufferRightTree=new StringBuffer();

        //String isLeftBibsSelected="false";
        //String isRightItemsSelected="false";
        String status1 = "";
        String status2 = "";

        BoundwithForm transferForm = (BoundwithForm) form;
        /*boolean hasPermission = performTransferHoldingOrItem(GlobalVariables.getUserSession().getPrincipalId());
        if (!hasPermission) {
            //transferForm.setMessage("<font size='3' color='red'> Current user is not authorized to perform this action.</font>");
            GlobalVariables.getMessageMap().putErrorForSectionId("TransferTreeSection1", "info.authorization");
            return navigate(transferForm, result, request, response);
        }*/
        transferForm.setTransferLeftTree(true);
        transferForm.setTransferRighttree(false);
        //transferForm.setInDelete("false");
        transferForm.setInDeleteLeftTree("false");
        transferForm.setInDeleteRightTree("false");
        // List<OleWorkBibDocument>  oleWorkBibDocumentList=transferForm.getWorkBibDocumentList();
        Tree<DocumentTreeNode, String> leftTree = transferForm.getLeftTree();
        Node<DocumentTreeNode, String> leftTreeRootElement = leftTree.getRootElement();
        Tree<DocumentTreeNode, String> rightTree = transferForm.getRightTree();
        Node<DocumentTreeNode, String> rightTreeRootElement = rightTree.getRootElement();
        List<String> bibIdentifiersToDelete = new ArrayList<String>();
        String deleteResponseFromDocStore = "";

        status1 = selectCheckedNodesTree1(leftTreeRootElement, bibIdentifierListForTree1, bibInstanceListForTree1,
                bibItemListForTree1, bibIdentifiersSelectedListForLeftTree,
                bibIdentifiersListWhenInstanceSelectedForLeftTree, bibIdentifiersToDelete,
                instanceIdentifiersToDelete);
        selectCheckedNodesTree2(rightTreeRootElement, bibIdentifierListForTree2, itemsSelectedListForRightTree,
                destBibIdentifierListForTree2, destInstanceIdentifierListForTree2);
        LOG.debug("bibIdentifiersToDelete " + bibIdentifiersToDelete);

        //  isLeftBibsSelected= stringBufferLeftTree.toString();
        //
        if (leftTreeRootElement == null) {
            //transferForm.setMessage("No records copied for left tree");
            //GlobalVariables.getMessageMap().putInfo(KRADConstants.GLOBAL_INFO,"error.transfer.empty.records", "right");
            GlobalVariables.getMessageMap().putErrorForSectionId(OLEConstants.TRANSFER_LEFT_TREE_SECTION, "error.transfer.empty.records", "left");
        } else if (rightTreeRootElement == null) {
            //transferForm.setMessage("No records copied for right tree");
            GlobalVariables.getMessageMap().putErrorForSectionId(OLEConstants.TRANSFER_RIGHT_TREE_SECTION, "error.transfer.empty.records", "right");
        } else if (bibIdentifiersSelectedListForLeftTree.size() > 0) {
            //transferForm.setMessage("Bib(s) selected in Left Tree which is invalid");
            GlobalVariables.getMessageMap().putErrorForSectionId(OLEConstants.TRANSFER_LEFT_TREE_SECTION, "error.transfer.invalid", "Bib", "left");
        } else if (bibInstanceListForTree1.size() == 0 && bibItemListForTree1.size() == 0) {
            //transferForm.setMessage("Nothing selected in left tree");
            GlobalVariables.getMessageMap().putErrorForSectionId(OLEConstants.TRANSFER_LEFT_TREE_SECTION, "error.transfer.selected.none", "left");
        } else if (itemsSelectedListForRightTree.size() > 0) {
            //transferForm.setMessage("Item(s) selected in Right Tree which is invalid");
            GlobalVariables.getMessageMap().putErrorForSectionId(OLEConstants.TRANSFER_RIGHT_TREE_SECTION, "error.transfer.invalid", "Item", "right");
        } else if (destBibIdentifierListForTree2.size() == 0 && destInstanceIdentifierListForTree2.size() == 0) {
            //transferForm.setMessage("Nothing selected in right tree");
            GlobalVariables.getMessageMap().putErrorForSectionId(OLEConstants.TRANSFER_RIGHT_TREE_SECTION, "error.transfer.selected.none", "right");
        } else if (bibInstanceListForTree1.size() > 0 && bibItemListForTree1.size() > 0) {
            //transferForm.setMessage("Instances and items both selected in left tree: Transfer failed");
            GlobalVariables.getMessageMap().putErrorForSectionId(OLEConstants.TRANSFER_LEFT_TREE_SECTION, "error.transfer.selected.multiple", "Instances", "items", "left");
        } else if (destBibIdentifierListForTree2.size() > 0 && destInstanceIdentifierListForTree2.size() > 0) {
            //transferForm.setMessage("Bibs and instances both selected in right tree: Transfer failed");
            GlobalVariables.getMessageMap().putErrorForSectionId(OLEConstants.TRANSFER_RIGHT_TREE_SECTION, "error.transfer.selected.multiple", "Bibs", "instances", "right");
        } else if (destBibIdentifierListForTree2.size() > 1) {
            //transferForm.setMessage("Only one bib can be selected in right tree");
            GlobalVariables.getMessageMap().putErrorForSectionId(OLEConstants.TRANSFER_RIGHT_TREE_SECTION, "error.transfer.selected.invalid", "bib", "right");
        } else if (destInstanceIdentifierListForTree2.size() > 1) {
            //transferForm.setMessage("Only one instance can be selected in right tree");
            GlobalVariables.getMessageMap().putErrorForSectionId(OLEConstants.TRANSFER_RIGHT_TREE_SECTION, "error.transfer.selected.invalid", "instance", "right");
        } else if (bibInstanceListForTree1.size() > 0 && destInstanceIdentifierListForTree2.size() > 0) {
            //transferForm.setMessage("Instances of left tree cant be transferred to instances of right tree: Transfer failed");
            GlobalVariables.getMessageMap().putErrorForSectionId(OLEConstants.TRANSFER_LEFT_TREE_SECTION, "error.transfer.left", "Instances", "Instances");
        } else if (bibItemListForTree1.size() > 0 && destBibIdentifierListForTree2.size() > 0) {
            //transferForm.setMessage("Items of left tree cant be transferred to Bibs of right tree: Transfer failed");
            GlobalVariables.getMessageMap().putErrorForSectionId(OLEConstants.TRANSFER_LEFT_TREE_SECTION, "error.transfer.left", "Items", "Bibs");
        }
        //        else if(!status1.equalsIgnoreCase("success")){
        //            System.out.println("in if checking status1");
        //             transferForm.setMessage(status1);
        //        }
        else if (bibInstanceListForTree1.size() > 0) {
            for (String bibUuid : bibIdentifiersListWhenInstanceSelectedForLeftTree) {
                if (destBibIdentifierListForTree2.contains(bibUuid)) {
                    //transferForm.setMessage("Transfer of instances should not happen between same left bib and right bib. Transfer of instances failed");
                    GlobalVariables.getMessageMap().putErrorForSectionId(OLEConstants.TRANSFER_RIGHT_TREE_SECTION, "error.transfer.bib");
                    transferInstance = false;
                    break;
                }
            }
            if (transferInstance) {
                String destBibIdentifier = destBibIdentifierListForTree2.get(0);
                if (bibIdentifiersToDelete.size() > 0) {
                    transferForm.setDocFormat(OLEConstants.MARC_FORMAT);
                    transferForm.setDocCategory(OLEConstants.WORK_CATEGORY);
                    transferForm.setDocType(OLEConstants.BIB_DOC_TYPE);
//                    String bibIdentifiers = Joiner.on(",").join(bibIdentifiersToDelete);
//                    deleteResponseFromDocStore = TransferUtil.getInstance().getDeleteResponseFromDocStore("deleteVerify", bibIdentifiers,
//                            transferForm);
                    transferForm.setBibInstanceListForTree1(bibInstanceListForTree1);
                    transferForm.setDestBibIdentifier(destBibIdentifier);
//                    transferForm.setDeleteVerifyResponse(deleteResponseFromDocStore);
//                    TransferUtil.getInstance().deleteVerify(transferForm, deleteResponseFromDocStore);

                    transferForm.setDeleteVerifyResponse(TransferUtil.getInstance().checkItemExistsInOleForBibs(bibIdentifiersToDelete));
                    transferForm.setDeleteIds(bibIdentifiersToDelete);
                    TransferUtil.getInstance().deleteVerify(transferForm, bibIdentifiersToDelete);
                    // Holding wouldn't transfer, if item is attached with Loan, PO, etc.
                    if (transferForm.getDeleteVerifyResponse().equalsIgnoreCase(OLEConstants.OLEBatchProcess.RESPONSE_STATUS_FAILED)) {
                        GlobalVariables.getMessageMap().putErrorForSectionId(OLEConstants.TRANSFER_LEFT_TREE_SECTION, OLEConstants.TRANSFER_FAIL_MESSAGE_ITEM_ATTACHED_OLE);
                    }
                    return getUIFModelAndView(transferForm);
                }
                TransferUtil.getInstance().transferInstances(bibInstanceListForTree1, destBibIdentifier);
                TransferUtil.getInstance().copyToTree(transferForm, bibIdentifierListForTree1, "leftTree");
                TransferUtil.getInstance().copyToTree(transferForm, bibIdentifierListForTree2, "rightTree");
                //transferForm.setMessage("Instances transferred successfully");
                GlobalVariables.getMessageMap().putInfoForSectionId(OLEConstants.TRANSFER_RIGHT_TREE_SECTION, "info.transfer", "Instances");
            }
        } else if (bibItemListForTree1.size() > 0) {
            String destInstanceIdentifier = destInstanceIdentifierListForTree2.get(0);
            if (instanceIdentifiersToDelete.size() > 0) {
                transferForm.setDocFormat(OLEConstants.OLEML_FORMAT);
                transferForm.setDocCategory(OLEConstants.WORK_CATEGORY);
                transferForm.setDocType(OLEConstants.HOLDING_DOC_TYPE);
//                String instanceIdentifiers = Joiner.on(",").join(instanceIdentifiersToDelete);
//                deleteResponseFromDocStore = TransferUtil.getInstance().getDeleteResponseFromDocStore("deleteVerify", instanceIdentifiers,
//                        transferForm);
                transferForm.setBibItemListForTree1(bibItemListForTree1);
                transferForm.setDestInstanceIdentifier(destInstanceIdentifier);
//                transferForm.setDeleteVerifyResponse(deleteResponseFromDocStore);
//                TransferUtil.getInstance().deleteVerify(transferForm, deleteResponseFromDocStore);
                transferForm.setDeleteVerifyResponse(TransferUtil.getInstance().checkItemExistsInOleForHoldings(instanceIdentifiersToDelete));
                transferForm.setDeleteIds(instanceIdentifiersToDelete);
                TransferUtil.getInstance().deleteVerify(transferForm, instanceIdentifiersToDelete);
                // Item wouldn't transfer, if item is attached with Loan, PO, etc.
                if (transferForm.getDeleteVerifyResponse().equalsIgnoreCase(OLEConstants.OLEBatchProcess.RESPONSE_STATUS_FAILED)) {
                    GlobalVariables.getMessageMap().putErrorForSectionId(OLEConstants.TRANSFER_LEFT_TREE_SECTION, OLEConstants.TRANSFER_FAIL_MESSAGE_ITEM_ATTACHED_OLE);
                }
                return getUIFModelAndView(transferForm);
                //return deleteVerify(transferForm, deleteResponseFromDocStore);
            }
            TransferUtil.getInstance().transferItems(bibItemListForTree1, destInstanceIdentifier);
            TransferUtil.getInstance().copyToTree(transferForm, bibIdentifierListForTree1, "leftTree");
            TransferUtil.getInstance().copyToTree(transferForm, bibIdentifierListForTree2, "rightTree");
            //transferForm.setMessage("Items transferred successfully");
            GlobalVariables.getMessageMap().putInfoForSectionId(OLEConstants.TRANSFER_RIGHT_TREE_SECTION, "info.transfer", "Items");
        } else {
            //transferForm.setMessage("Nothing selected or Selection is wrong");
            GlobalVariables.getMessageMap().putErrorForSectionId(OLEConstants.TRANSFER_RIGHT_TREE_SECTION, "error.transfer.bib");
        }
        return navigate(transferForm, result, request, response);
        //        return getUIFModelAndView(transferForm);
    }

//    public void delete(ResponseDocument responseDocument) {
//    }

    @RequestMapping(params = "methodToCall=transferRightToLeft")
    public ModelAndView transferRightToLeft(@ModelAttribute("KualiForm") UifFormBase form, BindingResult result,
                                            HttpServletRequest request, HttpServletResponse response) throws Exception {
        BoundwithForm transferForm = (BoundwithForm) form;
        /*boolean hasPermission = performTransferHoldingOrItem(GlobalVariables.getUserSession().getPrincipalId());
        if (!hasPermission) {
            //transferForm.setMessage("<font size='3' color='red'> Current user is not authorized to perform this action.</font>");
            GlobalVariables.getMessageMap().putErrorForSectionId("TransferTreeSection2", "info.authorization");
            return navigate(transferForm, result, request, response);
        }*/
        transferForm.setInDeleteLeftTree("false");
        transferForm.setInDeleteRightTree("false");
        transferForm.setTransferLeftTree(false);
        transferForm.setTransferRighttree(true);
        TransferRightToLeft transferRightToLeft = new TransferRightToLeft();
        transferRightToLeft.transferRightToLeft(transferForm, result, request, response);
        return navigate(transferForm, result, request, response);
        //        return getUIFModelAndView(transferForm);
    }

    private String selectCheckedNodesTree1(Node<DocumentTreeNode, String> rootElement,
                                           List<String> bibIdentifierListForTree1, List<String> bibInstanceListForTree1,
                                           List<String> bibItemListForTree1,
                                           List<String> bibIdentifiersSelectedListForLeftTree,
                                           List<String> bibIdentifiersListWhenInstanceSelectedForLeftTree,
                                           List<String> bibIdentifiersToDelete,
                                           List<String> instanceIdentifiersToDelete) {

        BoundwithSelection boundwithSelection;
        DocumentTreeNode documentTreeNode;
        String status = "success";
        String bibTitle = "";
        int instanceCount = 0;
        int itemCount = 0;
        List<String> instanceidenifiersListTemp = new ArrayList<String>();
        if (rootElement != null) {
            List<Node<DocumentTreeNode, String>> list = rootElement.getChildren();
            for (Node<DocumentTreeNode, String> marcNode : list) {
                instanceCount = 0;
                itemCount = 0;
                String instanceUUID = "";
                documentTreeNode = marcNode.getData();

                bibTitle = documentTreeNode.getTitle();
                String bibUUID = marcNode.getNodeType();
                bibIdentifierListForTree1.add(bibUUID);
                LOG.info("documentTreeNode.isSelectTree1()-->" + documentTreeNode.isSelect());
                if (documentTreeNode.isSelect()) {
                    bibIdentifiersSelectedListForLeftTree.add(bibUUID);
                    break;
                }
                List<Node<DocumentTreeNode, String>> instanceList = marcNode.getChildren();
                List<Node<DocumentTreeNode, String>> itemList = null;
                instanceidenifiersListTemp.clear();
                for (Node<DocumentTreeNode, String> instanceNode : instanceList) {
                    itemCount = 0;
                    documentTreeNode = instanceNode.getData();
                    instanceUUID = instanceNode.getNodeType();
                    LOG.debug("instanceUUID out of select " + instanceUUID);
                    if (documentTreeNode.isSelect()) {
                        instanceCount++;

                        LOG.debug("instanceUUID " + instanceUUID);
                        bibInstanceListForTree1.add(instanceUUID);
                        //instanceidenifiersListTemp.add(instanceUUID);
                        bibIdentifiersListWhenInstanceSelectedForLeftTree.add(bibUUID);
                    }
                    itemList = instanceNode.getChildren();
                    for (Node<DocumentTreeNode, String> itemNode : itemList) {
                        documentTreeNode = itemNode.getData();
                        if (documentTreeNode.isSelect()) {
                            itemCount++;
                            String itemUUID = itemNode.getNodeType();
                            LOG.debug("itemUUID " + itemUUID);
                            bibItemListForTree1.add(itemUUID);
                        }
                    }  //item loop end
                    if (instanceNode.getNumberOfChildren() > 0 && itemCount == itemList.size()) {
                        instanceidenifiersListTemp.add(instanceUUID);
                        //status="Instance will remain with no items if this transfer takes place. Instance must have atleast one item. Transfer failed";
                    }
                } //instance loop end
                if (marcNode.getNumberOfChildren() > 0 && instanceCount == instanceList.size()) {
                    bibIdentifiersToDelete.add(bibUUID);
                    LOG.debug("in if of no instances bibIdentifierToDelete " + bibIdentifiersToDelete);
                }
                instanceIdentifiersToDelete.addAll(instanceidenifiersListTemp);
            }  //bib loop end
        }
        return status;
    }


    private void selectCheckedNodesTree2(Node<DocumentTreeNode, String> rootElement,
                                         List<String> bibIdentifierListForTree2,
                                         List<String> itemsSelectedListForRightTree,
                                         List<String> destBibIdentifierListForTree2,
                                         List<String> destInstanceIdentifierListForTree2) {

        BoundwithSelection boundwithSelection;
        DocumentTreeNode documentTreeNode;
        if (rootElement != null) {
            List<Node<DocumentTreeNode, String>> list = rootElement.getChildren();
            for (Node<DocumentTreeNode, String> marcNode : list) {
                documentTreeNode = marcNode.getData();
                String bibUUID = marcNode.getNodeType();
                bibIdentifierListForTree2.add(bibUUID);
                LOG.info("documentTreeNode.isSelectTree1()-->" + documentTreeNode.isSelect());
                if (documentTreeNode.isSelect()) {
                    destBibIdentifierListForTree2.add(bibUUID);
                }
                List<Node<DocumentTreeNode, String>> instanceList = marcNode.getChildren();
                for (Node<DocumentTreeNode, String> instanceNode : instanceList) {
                    documentTreeNode = instanceNode.getData();
                    if (documentTreeNode.isSelect()) {
                        String instanceUUID = instanceNode.getNodeType();
                        destInstanceIdentifierListForTree2.add(instanceUUID);
                    }
                    List<Node<DocumentTreeNode, String>> itemList = instanceNode.getChildren();
                    for (Node<DocumentTreeNode, String> itemNode : itemList) {
                        documentTreeNode = itemNode.getData();
                        if (documentTreeNode.isSelect()) {
                            String itemUUID = itemNode.getNodeType();
                            //stringBufferRightTree.append("true");
                            itemsSelectedListForRightTree.add(itemUUID);
                            break;
                        }
                    }

                }
            }
        }
    }

    String listToCsv(List<String> listOfStrings, char separator) {
        StringBuilder sb = new StringBuilder();

        // all but last
        for (int i = 0; i < listOfStrings.size() - 1; i++) {
            sb.append(listOfStrings.get(i));
            sb.append(separator);
        }
        // last string, no separator
        sb.append(listOfStrings.get(listOfStrings.size() - 1));
        return sb.toString();
    }

    @RequestMapping(params = "methodToCall=delete")
    public ModelAndView delete(@ModelAttribute("KualiForm") UifFormBase form, BindingResult result,
                               HttpServletRequest request, HttpServletResponse httpResponse) throws Exception {
        StringBuilder uuidList = new StringBuilder();
        BoundwithForm transferForm = (BoundwithForm) form;
        DocumentSelectionTree documentSelectionTree = new DocumentSelectionTree();
        //        uuidList.append(boundwithForm.getDocId());
        //boundwithForm.setInDelete("false");
        transferForm.setInDeleteLeftTree("false");
        transferForm.setInDeleteRightTree("false");
//        WorkInstanceDocument workInstanceDocumentForTree1 = transferForm.getWorkInstanceDocumentForTree1();

        String deleteVerifyResponse = transferForm.getDeleteVerifyResponse();
        if (deleteVerifyResponse.contains("Failed")) {
            /*transferForm.setSelectedInstance(
                    "Unable to delete the instance " + workInstanceDocumentForTree1.getHoldingsDocument()
                                                                                   .getLocationName()
                    + "\t \n because it exists in OLE database");*/
//            String message = workInstanceDocumentForTree1.getHoldingsDocument().getLocationName();
            String message = "location";
            GlobalVariables.getMessageMap().putInfoForSectionId(OLEConstants.TRANSFER_LEFT_TREE_SECTION, "info.transfer.delete.failure", message);
        } else if (deleteVerifyResponse.contains("success")) {
            String treeId = transferForm.getActionParamaterValue("treeId");
            if (transferForm.getDocType().equalsIgnoreCase("bibliographic")) {
                if (transferForm.getDestBibIdentifier() != null && transferForm.getDestBibIdentifier().length() > 0) {
                    TransferUtil.getInstance().transferInstances(transferForm.getBibInstanceListForTree1(),
                            transferForm.getDestBibIdentifier());
                    //transferForm.setMessage("Instance transferred successfully. Bib is deleted.");
                    if (treeId.equalsIgnoreCase("leftTree")) {
                        HashMap leftList = (HashMap) request.getSession().getAttribute("LeftList");
                        if (leftList.size() == 1) {
                            clearTree(transferForm, result, request, httpResponse);
                        }
                        GlobalVariables.getMessageMap().putInfoForSectionId(OLEConstants.TRANSFER_RIGHT_TREE_SECTION, "info.transfer.instance.success");
                        GlobalVariables.getMessageMap().putInfoForSectionId(OLEConstants.TRANSFER_LEFT_TREE_SECTION, "info.transfer.instance.success.bib.delete");
                    } else {
                        HashMap rightList = (HashMap) request.getSession().getAttribute("RightList");
                        if (rightList.size() == 1) {
                            clearTree(transferForm, result, request, httpResponse);
                        }
                        GlobalVariables.getMessageMap().putInfoForSectionId(OLEConstants.TRANSFER_LEFT_TREE_SECTION, "info.transfer.instance.success");
                        GlobalVariables.getMessageMap().putInfoForSectionId(OLEConstants.TRANSFER_RIGHT_TREE_SECTION, "info.transfer.instance.success.bib.delete");
                    }
                    getDocstoreClientLocator().getDocstoreClient().deleteBibs(transferForm.getDeleteIds());
                    transferForm.setDestBibIdentifier(null);
                } else {
                    TransferUtil.getInstance().transferItems(transferForm.getBibItemListForTree1(),
                            transferForm.getDestInstanceIdentifier());
                    //transferForm.setMessage("Item transferred successfully. Bib and Instance are deleted.");
                    Holdings holdings = null;
                    try {
                        holdings = getDocstoreClientLocator().getDocstoreClient().retrieveHoldings(transferForm.getDeleteIds().get(0));
                    }
                    catch (Exception e) {
                        DocstoreException docstoreException = (DocstoreException) e;
                        if (org.apache.commons.lang3.StringUtils.isNotEmpty(docstoreException.getErrorCode())) {
                            GlobalVariables.getMessageMap().putError(KRADConstants.GLOBAL_ERRORS, docstoreException.getErrorCode());
                        } else {
                            GlobalVariables.getMessageMap().putError(KRADConstants.GLOBAL_ERRORS, e.getMessage());
                        }
                    }
                    BibTree bibTree = null;
                    try {
                        bibTree = getDocstoreClientLocator().getDocstoreClient().retrieveBibTree(holdings.getBib().getId());
                    }
                    catch (Exception e) {
                        DocstoreException docstoreException = (DocstoreException) e;
                        if (org.apache.commons.lang3.StringUtils.isNotEmpty(docstoreException.getErrorCode())) {
                            GlobalVariables.getMessageMap().putError(KRADConstants.GLOBAL_ERRORS, docstoreException.getErrorCode());
                        } else {
                            GlobalVariables.getMessageMap().putError(KRADConstants.GLOBAL_ERRORS, e.getMessage());
                        }
                    }
                    if(bibTree.getHoldingsTrees() != null && bibTree.getHoldingsTrees().size() > 1) {
                        if (treeId.equalsIgnoreCase("leftTree")) {
                            HashMap leftList = (HashMap) request.getSession().getAttribute("LeftList");
                            GlobalVariables.getMessageMap().putInfoForSectionId(OLEConstants.TRANSFER_RIGHT_TREE_SECTION, "info.transfer.item.success");
                            GlobalVariables.getMessageMap().putInfoForSectionId(OLEConstants.TRANSFER_LEFT_TREE_SECTION, "info.transfer.item.success.holdings.delete");
                        } else {
                            HashMap rightList = (HashMap) request.getSession().getAttribute("RightList");
                            GlobalVariables.getMessageMap().putInfoForSectionId(OLEConstants.TRANSFER_LEFT_TREE_SECTION, "info.transfer.item.success");
                            GlobalVariables.getMessageMap().putInfoForSectionId(OLEConstants.TRANSFER_RIGHT_TREE_SECTION, "info.transfer.item.success.holdings.delete");
                        }
                        try {
                            getDocstoreClientLocator().getDocstoreClient().deleteHoldings(transferForm.getDeleteIds().get(0));
                        }
                        catch (Exception e) {
                            DocstoreException docstoreException = (DocstoreException) e;
                            if (org.apache.commons.lang3.StringUtils.isNotEmpty(docstoreException.getErrorCode())) {
                                GlobalVariables.getMessageMap().putError(KRADConstants.GLOBAL_ERRORS, docstoreException.getErrorCode());
                            } else {
                                GlobalVariables.getMessageMap().putError(KRADConstants.GLOBAL_ERRORS, e.getMessage());
                            }
                        }
                    }
                    else {
                        if (treeId.equalsIgnoreCase("leftTree")) {
                            HashMap leftList = (HashMap) request.getSession().getAttribute("LeftList");
                            if (leftList.size() == 1) {
                                clearTree(transferForm, result, request, httpResponse);
                            }
                            GlobalVariables.getMessageMap().putInfoForSectionId(OLEConstants.TRANSFER_RIGHT_TREE_SECTION, "info.transfer.item.success");
                            GlobalVariables.getMessageMap().putInfoForSectionId(OLEConstants.TRANSFER_LEFT_TREE_SECTION, "info.transfer.item.success.bib.instance.delete");
                        } else {
                            HashMap rightList = (HashMap) request.getSession().getAttribute("RightList");
                            if (rightList.size() == 1) {
                                clearTree(transferForm, result, request, httpResponse);
                            }
                            GlobalVariables.getMessageMap().putInfoForSectionId(OLEConstants.TRANSFER_LEFT_TREE_SECTION, "info.transfer.item.success");
                            GlobalVariables.getMessageMap().putInfoForSectionId(OLEConstants.TRANSFER_RIGHT_TREE_SECTION, "info.transfer.item.success.bib.instance.delete");
                        }
                        try {
                            getDocstoreClientLocator().getDocstoreClient().deleteBib(holdings.getBib().getId());
                        }
                        catch (Exception e) {
                            DocstoreException docstoreException = (DocstoreException) e;
                            if (org.apache.commons.lang3.StringUtils.isNotEmpty(docstoreException.getErrorCode())) {
                                GlobalVariables.getMessageMap().putError(KRADConstants.GLOBAL_ERRORS, docstoreException.getErrorCode());
                            } else {
                                GlobalVariables.getMessageMap().putError(KRADConstants.GLOBAL_ERRORS, e.getMessage());
                            }
                        }
                    }
                }
            } else if (transferForm.getDocType().equalsIgnoreCase("holdings")) {
                TransferUtil.getInstance().transferItems(transferForm.getBibItemListForTree1(),
                        transferForm.getDestInstanceIdentifier());

                if (treeId.equalsIgnoreCase("leftTree")) {
                    GlobalVariables.getMessageMap().putInfoForSectionId(OLEConstants.TRANSFER_RIGHT_TREE_SECTION, "info.transfer.item.success");
                    GlobalVariables.getMessageMap().putInfoForSectionId(OLEConstants.TRANSFER_LEFT_TREE_SECTION, "info.transfer.item.success.holdings.delete");
                } else {
                    GlobalVariables.getMessageMap().putInfoForSectionId(OLEConstants.TRANSFER_LEFT_TREE_SECTION, "info.transfer.item.success");
                    GlobalVariables.getMessageMap().putInfoForSectionId(OLEConstants.TRANSFER_RIGHT_TREE_SECTION, "info.transfer.item.success.holdings.delete");
                }
                for(String id : transferForm.getDeleteIds()) {
                    getDocstoreClientLocator().getDocstoreClient().deleteHoldings(id);
                }
            }
//            String deleteResponseFromDocStore = TransferUtil.getInstance().getDeleteResponseFromDocStore("delete", uuidList.toString(),
//                    transferForm);
            transferForm.setShowBoundwithTree(false);
        }
        return getUIFModelAndView(transferForm);
    }

    @RequestMapping(params = "methodToCall=OnlyTransfer")
    public ModelAndView OnlyTransfer(@ModelAttribute("KualiForm") UifFormBase form, BindingResult result,
                                     HttpServletRequest request, HttpServletResponse httpResponse) throws Exception {
        BoundwithForm transferForm = (BoundwithForm) form;
        String treeId = transferForm.getActionParamaterValue("treeId");
        if (transferForm.getDocType().equalsIgnoreCase("bibliographic")) {
            if (transferForm.getDestBibIdentifier() != null && transferForm.getDestBibIdentifier().length() > 0) {
                TransferUtil.getInstance().transferInstances(transferForm.getBibInstanceListForTree1(),
                        transferForm.getDestBibIdentifier());
                //transferForm.setMessage("Instance transferred successfully.");
                if (treeId.equalsIgnoreCase("leftTree")) {
                    GlobalVariables.getMessageMap().putInfoForSectionId(OLEConstants.TRANSFER_RIGHT_TREE_SECTION, "info.transfer", "Instance");
                } else {
                    GlobalVariables.getMessageMap().putInfoForSectionId(OLEConstants.TRANSFER_LEFT_TREE_SECTION, "info.transfer", "Instance");
                }
                transferForm.setDestBibIdentifier(null);
            } else {
                TransferUtil.getInstance().transferItems(transferForm.getBibItemListForTree1(),
                        transferForm.getDestInstanceIdentifier());
                //transferForm.setMessage("Item transferred successfully.");
                if (treeId.equalsIgnoreCase("leftTree")) {
                    GlobalVariables.getMessageMap().putInfoForSectionId(OLEConstants.TRANSFER_RIGHT_TREE_SECTION, "info.transfer", "Item");
                } else {
                    GlobalVariables.getMessageMap().putInfoForSectionId(OLEConstants.TRANSFER_LEFT_TREE_SECTION, "info.transfer", "Item");
                }
            }
        } else if (transferForm.getDocType().equalsIgnoreCase("instance")) {
            TransferUtil.getInstance().transferItems(transferForm.getBibItemListForTree1(),
                    transferForm.getDestInstanceIdentifier());
            //transferForm.setMessage("Item transferred successfully.");
            GlobalVariables.getMessageMap().putInfoForSectionId(OLEConstants.TRANSFER_LEFT_TREE_SECTION, "info.transfer", "Item");
        }
        //boundwithForm.setInDelete("false");
        transferForm.setInDeleteLeftTree("false");
        transferForm.setInDeleteRightTree("false");
        return getUIFModelAndView(transferForm);
    }

    /**
     * Enable, disable the next and previous and also show the message for number of entries
     * @param boundwithForm
     * @return
     */
    public void setPageNextPreviousAndEntriesInfo(BoundwithForm boundwithForm) {
        this.totalRecCount = boundwithForm.getSearchResponse().getTotalRecordCount();
        this.start = boundwithForm.getSearchResponse().getStartIndex();
        this.pageSize = boundwithForm.getSearchResponse().getPageSize();
        boundwithForm.setPreviousFlag(getWorkbenchPreviousFlag());
        boundwithForm.setNextFlag(getWorkbenchNextFlag());
        boundwithForm.setPageShowEntries(getWorkbenchPageShowEntries());
    }

}