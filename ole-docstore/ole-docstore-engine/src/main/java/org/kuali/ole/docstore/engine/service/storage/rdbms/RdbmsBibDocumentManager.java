package org.kuali.ole.docstore.engine.service.storage.rdbms;

import org.apache.commons.lang.StringUtils;
import org.kuali.ole.DocumentUniqueIDPrefix;
import org.kuali.ole.docstore.DocStoreConstants;
import org.kuali.ole.docstore.common.document.*;
import org.kuali.ole.docstore.common.exception.DocstoreException;
import org.kuali.ole.docstore.common.exception.DocstoreResources;
import org.kuali.ole.docstore.common.exception.DocstoreValidationException;
import org.kuali.ole.docstore.engine.service.storage.rdbms.pojo.*;
import org.kuali.ole.docstore.engine.service.storage.rdbms.pojo.BibHoldingsRecord;
import org.kuali.ole.docstore.engine.service.storage.rdbms.pojo.BibRecord;
import org.kuali.ole.docstore.engine.service.storage.rdbms.pojo.HoldingsRecord;
import org.kuali.ole.docstore.model.enums.DocCategory;
import org.kuali.ole.docstore.model.enums.DocFormat;
import org.kuali.ole.docstore.model.enums.DocType;

import java.sql.Timestamp;
import java.text.SimpleDateFormat;
import java.util.*;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * Created with IntelliJ IDEA.
 * User: sambasivam
 * Date: 12/17/13
 * Time: 1:02 PM
 * To change this template use File | Settings | File Templates.
 */
public class RdbmsBibDocumentManager extends RdbmsAbstarctDocumentManager {

    private static RdbmsBibDocumentManager rdbmsBibDocumentManager = null;

    public static RdbmsBibDocumentManager getInstance() {
        if (rdbmsBibDocumentManager == null) {
            rdbmsBibDocumentManager = new RdbmsBibDocumentManager();
        }
        return rdbmsBibDocumentManager;
    }

    @Override
    public void create(Object object) {
        BibRecord bibRecord = new BibRecord();
        Bib bib = (Bib) object;
        modifyAdditionalAttributes(bib);
        bibRecord.setContent(bib.getContent());
        boolean isBibIdFlag = getBibIdFromBibXMLContent(bibRecord);
        bibRecord.setCreatedBy(bib.getCreatedBy());
        bibRecord.setDateCreated(getTimeStampFromString(bib.getCreatedOn()));
        bibRecord.setStatusUpdatedBy(bib.getStatusUpdatedBy());
        if (StringUtils.isNotBlank(bib.getStatusUpdatedOn())) {
            bibRecord.setStatusUpdatedDate(getTimeStampFromString(bib.getStatusUpdatedOn()));
        }
        bibRecord.setFassAddFlag(bib.isFastAdd());
        bibRecord.setFormerId("");
        bibRecord.setSuppressFromPublic(String.valueOf(bib.isPublic()));
        bibRecord.setStatus(bib.getStatus());
        bibRecord.setDateEntered(getTimeStampFromString(bib.getCreatedOn()));
        bibRecord.setStaffOnlyFlag(bib.isStaffOnly());
        bibRecord.setUniqueIdPrefix(DocumentUniqueIDPrefix.getPrefix(bib.getCategory(), bib.getType(), bib.getFormat()));
        getBusinessObjectService().save(bibRecord);
        bib.setId(DocumentUniqueIDPrefix.getPrefixedId(bibRecord.getUniqueIdPrefix(), bibRecord.getBibId()));
        bib.setContent(bibRecord.getContent());
        if (isBibIdFlag) {
            modifyDocumentContent(bib, bibRecord);
        }

        createBibInfoRecord(bibRecord);
    }

    protected void createBibInfoRecord(BibRecord bibRecord) {

    }

    @Override
    public void update(Object object) {
        Bib bib = (Bib) object;
        Map parentCriteria = new HashMap();
        parentCriteria.put("bibId", DocumentUniqueIDPrefix.getDocumentId(bib.getId()));
        BibRecord bibRecord = getBusinessObjectService().findByPrimaryKey(BibRecord.class, parentCriteria);
        if (bibRecord == null) {
            DocstoreException docstoreException = new DocstoreValidationException(DocstoreResources.BIB_ID_NOT_FOUND, DocstoreResources.BIB_ID_NOT_FOUND);
            docstoreException.addErrorParams("bibId", bib.getId());
            throw docstoreException;
        }
        updateAdditionalAttributes(bib, bibRecord);
        if (bibRecord != null) {
            bibRecord.setContent(bib.getContent());
            bibRecord.setFassAddFlag(bib.isFastAdd());
            bibRecord.setStaffOnlyFlag(bib.isStaffOnly());
            bibRecord.setSuppressFromPublic(String.valueOf(bib.isPublic()));
            getBusinessObjectService().save(bibRecord);
        }

        createBibInfoRecord(bibRecord);
    }

    @Override
    public Object retrieve(String id) {
        Bib bibMarc = retrieveBib(id);
        return bibMarc;
    }

    @Override
    public List<Object> retrieve(List<String> ids) {
        List<Object> bibs = new ArrayList<>();
        for (String id : ids) {
            bibs.add(retrieve(id));
        }
        return bibs;
    }

    @Override
    public void delete(String id) {
        Map parentCriteria1 = new HashMap();
        parentCriteria1.put("bibId", DocumentUniqueIDPrefix.getDocumentId(id));
//        List<BibRecord> bibRecords = (List<BibRecord>) getBusinessObjectService().findMatching(BibRecord.class, parentCriteria1);
        List<HoldingsRecord> holdingsRecordList = (List<HoldingsRecord>) getBusinessObjectService().findMatching(HoldingsRecord.class, parentCriteria1);
        for (HoldingsRecord holdingsRecord : holdingsRecordList) {

            holdingsRecord.setCallNumberTypeId(null);
            getBusinessObjectService().save(holdingsRecord);
            //HoldingsRecord holdingsRecord = (HoldingsRecord) bibRecords.get(0).getHoldingsRecords();
            List<ExtentOfOwnerShipRecord> extentOfOwnerShipRecords = (List<ExtentOfOwnerShipRecord>) getBusinessObjectService().findMatching(ExtentOfOwnerShipRecord.class, getHoldingsMap(holdingsRecord.getHoldingsId()));
            if (extentOfOwnerShipRecords != null && extentOfOwnerShipRecords.size() > 0) {
                for (int j = 0; j < extentOfOwnerShipRecords.size(); j++) {
                    List<ExtentNoteRecord> extentNoteRecords = extentOfOwnerShipRecords.get(j).getExtentNoteRecords();
                    if (extentNoteRecords != null && extentNoteRecords.size() > 0) {
                        getBusinessObjectService().delete(extentNoteRecords);
                    }
                }
                getBusinessObjectService().delete(extentOfOwnerShipRecords);
            }
            if (holdingsRecord.getHoldingsNoteRecords() != null && holdingsRecord.getHoldingsNoteRecords().size() > 0) {
                List<HoldingsNoteRecord> holdingsNoteRecords = holdingsRecord.getHoldingsNoteRecords();
                getBusinessObjectService().delete(holdingsNoteRecords);
            }

            if (holdingsRecord.getAccessUriRecords() != null && holdingsRecord.getAccessUriRecords().size() > 0) {
                List<HoldingsUriRecord> accessUriRecords = holdingsRecord.getAccessUriRecords();
                getBusinessObjectService().delete(accessUriRecords);
            }
            if (holdingsRecord.getHoldingsUriRecords() != null && holdingsRecord.getHoldingsUriRecords().size() > 0) {
                List<HoldingsUriRecord> holdingsUriRecords = holdingsRecord.getHoldingsUriRecords();
                getBusinessObjectService().delete(holdingsUriRecords);
            }
            holdingsRecord.setCallNumberTypeId(null);
            holdingsRecord.setReceiptStatusId(null);

            List<ItemRecord> itemRecords = (List<ItemRecord>) getBusinessObjectService().findMatching(ItemRecord.class, getHoldingsMap(holdingsRecord.getHoldingsId()));
            for (ItemRecord itemRecord : itemRecords) {
                itemRecord.setCallNumberTypeId(null);
                getBusinessObjectService().save(itemRecord);
                if (itemRecord.getFormerIdentifierRecords() != null && itemRecord.getFormerIdentifierRecords().size() > 0) {
                    List<FormerIdentifierRecord> formerIdentifierRecords = itemRecord.getFormerIdentifierRecords();
                    getBusinessObjectService().delete(formerIdentifierRecords);
                }
                if (itemRecord.getItemNoteRecords() != null && itemRecord.getItemNoteRecords().size() > 0) {
                    List<ItemNoteRecord> itemNoteRecords = itemRecord.getItemNoteRecords();
                    getBusinessObjectService().delete(itemNoteRecords);
                }
                if (itemRecord.getLocationsCheckinCountRecords() != null && itemRecord.getLocationsCheckinCountRecords().size() > 0) {
                    List<LocationsCheckinCountRecord> locationsCheckinCountRecords = itemRecord.getLocationsCheckinCountRecords();
                    getBusinessObjectService().delete(locationsCheckinCountRecords);
                }
                itemRecord.setItemStatusId(null);
                itemRecord.setItemTypeId(null);
                itemRecord.setTempItemTypeId(null);
                itemRecord.setStatisticalSearchId(null);
                itemRecord.setHighDensityStorageId(null);
                Map map = new HashMap();
                map.put("itemId", itemRecord.getItemId());
                getBusinessObjectService().deleteMatching(ItemStatisticalSearchRecord.class, map);
                getBusinessObjectService().delete(itemRecord);

            }
            Map holdingsMap = new HashMap();
            holdingsMap.put("holdingsId", holdingsRecord.getHoldingsId());
            getBusinessObjectService().deleteMatching(EInstanceCoverageRecord.class, holdingsMap);
            getBusinessObjectService().deleteMatching(EInstancePerpetualAccessRecord.class, holdingsMap);
            getBusinessObjectService().deleteMatching(HoldingsAccessLocation.class, holdingsMap);
            getBusinessObjectService().deleteMatching(HoldingsStatisticalSearchRecord.class, holdingsMap);
            getBusinessObjectService().deleteMatching(OLEHoldingsDonorRecord.class, holdingsMap);


            getBusinessObjectService().delete(holdingsRecord);


        }
        getBusinessObjectService().deleteMatching(BibRecord.class, parentCriteria1);
        // Modified for performance tuning jira OLE-7173
        parentCriteria1 = new HashMap();
        parentCriteria1.put("bibIdStr", id);
        getBusinessObjectService().deleteMatching(BibInfoRecord.class, parentCriteria1);
        //TODO : check in oracle DB also.
    }

    @Override
    public Object retrieveTree(String id) {
//        Map parentCriteria1 = new HashMap();
//        parentCriteria1.put("bibId", DocumentUniqueIDPrefix.getDocumentId(id));
//        Map<String, String> holdingsIdMap = new HashMap<>();
//        BibTree bibTree = new BibTree();
//        bibTree.setBib((Bib) retrieve(id));
//        List<HoldingsRecord> holdingsRecords = (List<HoldingsRecord>) getBusinessObjectService().findMatching(HoldingsRecord.class, parentCriteria1);
//        List<BibHoldingsRecord> bibHoldingsRecords = (List<BibHoldingsRecord>) getBusinessObjectService().findMatching(BibHoldingsRecord.class, parentCriteria1);
//        for(HoldingsRecord holdingsRecord : holdingsRecords) {
//            holdingsIdMap.put(holdingsRecord.getHoldingsId(), holdingsRecord.getHoldingsId());
//        }
//        for(BibHoldingsRecord bibHoldingsRecord : bibHoldingsRecords) {
//            holdingsIdMap.put(bibHoldingsRecord.getHoldingsId(), bibHoldingsRecord.getHoldingsId());
//        }
//        RdbmsHoldingsDocumentManager holdingsDocumentManager = RdbmsHoldingsDocumentManager.getInstance();
//        for(HoldingsRecord holdingsRecord : holdingsRecords) {
//            bibTree.getHoldingsTrees().add((HoldingsTree) holdingsDocumentManager.buildHoldingsTree(holdingsRecord.getHoldingsId(), holdingsRecord));
//            holdingsIdMap.remove(holdingsRecord.getHoldingsId());
//        }
//        if(holdingsIdMap.size() > 0) {
//            for(String holdingsId : holdingsIdMap.keySet()) {
//                bibTree.getHoldingsTrees().add((HoldingsTree) holdingsDocumentManager.retrieveTree(holdingsId));
//            }
//        }
//        Collections.sort(bibTree.getHoldingsTrees());

        BibTree bibTree = retrieveBibTree(id);
        return bibTree;
    }

    @Override
    public void validate(Object object) {
    }

    protected boolean getBibIdFromBibXMLContent(BibRecord bibRecord) {
        return true;
    }

    protected void modifyDocumentContent(Bib bib, BibRecord bibRecord) {

    }

    protected void modifyAdditionalAttributes(Bib bib) {
        Date date = new Date();
        SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
        String dateStr = sdf.format(date);
        String user = bib.getCreatedBy();
        String statusFromReqDoc = "";
        statusFromReqDoc = bib.getStatus();
        bib.setCreatedOn(dateStr);
        bib.setCreatedBy(user);
        //Add statusUpdatedBy and statusUpdatedOn if input request is having non empty status field
        if (StringUtils.isNotEmpty(statusFromReqDoc)) {
            bib.setStatusUpdatedBy(user);
            bib.setStatusUpdatedOn(dateStr);
        }
    }

    protected boolean validateIdField(String bibId) {
        if (StringUtils.isNotEmpty(bibId)) {
            String idPattern = "[0-9]+";
            Matcher match = Pattern.compile(idPattern).matcher(bibId);
            return match.matches();
        }
        return false;
    }

    public void updateAdditionalAttributes(Bib bib, BibRecord bibRecord) {
        Date date = new Date();
        SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
        String dateStr = sdf.format(date);
        String user = bib.getUpdatedBy();
        String statusFromReqDoc = bib.getStatus();
        Timestamp timestampDate = getTimeStampFromString(dateStr);

        bib.setUpdatedOn(dateStr);
        bibRecord.setDateEntered(timestampDate);
        bibRecord.setUpdatedBy(user);

        String status = bibRecord.getStatus();
        if (status == null) {
            status = "";
        }

        if (status != null && !status.equals(statusFromReqDoc)) {
            bib.setStatusUpdatedBy(user);
            bib.setStatusUpdatedOn(dateStr);
            bibRecord.setStatus(statusFromReqDoc);
            bibRecord.setStatusUpdatedBy(user);
            bibRecord.setStatusUpdatedDate(timestampDate);
        }
    }

    protected Bib buildBibDocFromBibRecord(BibRecord bibRecord) {
        Bib bibMarc = null;
        if ("wbu".equals(bibRecord.getUniqueIdPrefix())) {
            bibMarc = new BibDcUnqualified();
            bibMarc.setFormat(DocFormat.DUBLIN_UNQUALIFIED.getCode());
        } else {
            bibMarc = new BibMarc();
            bibMarc.setFormat(DocFormat.MARC.getCode());
        }
        bibMarc.setCategory(DocCategory.WORK.getCode());
        bibMarc.setType(DocType.BIB.getCode());
        String content = bibRecord.getContent();
        bibMarc.setId(DocumentUniqueIDPrefix.getPrefixedId(bibRecord.getUniqueIdPrefix(), bibRecord.getBibId()));
        if (bibRecord.getFassAddFlag() != null) {
            bibMarc.setFastAdd(bibRecord.getFassAddFlag());
        }
        if (bibRecord.getDateEntered() != null && !"".equals(bibRecord.getDateEntered())) {
            bibMarc.setCreatedBy(bibRecord.getDateEntered().toString());
        }
        if (bibRecord.getStatus() != null) {
            bibMarc.setStatus(bibRecord.getStatus());
        }
        if (bibRecord.getStatusUpdatedDate() != null) {
            bibMarc.setStatusUpdatedOn(bibRecord.getStatusUpdatedDate().toString());
        }
        if (bibRecord.getSuppressFromPublic() != null) {
            bibMarc.setPublic(Boolean.valueOf(bibRecord.getSuppressFromPublic()));
        }
        if (bibRecord.getStaffOnlyFlag() != null) {
            bibMarc.setStaffOnly(bibRecord.getStaffOnlyFlag());
        }
        if (bibRecord.getCreatedBy() != null) {
            bibMarc.setCreatedBy(bibRecord.getCreatedBy());
        }
        if (bibRecord.getUpdatedBy() != null) {
            bibMarc.setUpdatedBy(bibRecord.getUpdatedBy());
        }
        if (bibRecord.getStatusUpdatedBy() != null) {
            bibMarc.setStatusUpdatedBy(bibRecord.getStatusUpdatedBy());
        }
        if (bibRecord.getDateCreated() != null && !"".equals(bibRecord.getDateCreated())) {
            bibMarc.setCreatedOn(bibRecord.getDateCreated().toString());
        }
        if (bibRecord.getDateEntered() != null && !"".equals(bibRecord.getDateEntered())) {
            bibMarc.setUpdatedOn(bibRecord.getDateEntered().toString());
        }

        bibMarc.setContent(content);
        if ("wbm".equals(bibRecord.getUniqueIdPrefix())) {
            bibMarc.deserializeContent(bibMarc);
        } else {
            bibMarc.deserializeContent(bibMarc);
        }

        bibMarc.setFormat(DocFormat.MARC.getCode());
        return bibMarc;
    }

    protected Bib retrieveBib(String id) {

        BibRecord bibRecord = getBusinessObjectService().findByPrimaryKey(BibRecord.class, getBibMap(id));
        if (bibRecord == null) {
            DocstoreException docstoreException = new DocstoreValidationException(DocstoreResources.BIB_ID_NOT_FOUND, DocstoreResources.BIB_ID_NOT_FOUND);
            docstoreException.addErrorParams("bibId", id);
            throw docstoreException;
        }
        Bib bib = buildBibDocFromBibRecord(bibRecord);

        return bib;
    }


    protected BibTree retrieveBibTree(String id) {
        BibTree bibTree = new BibTree();

        Bib bib = retrieveBib(id);
        bibTree.setBib(bib);
        Map<String, String> holdingsMap = new HashMap<>();

        RdbmsHoldingsDocumentManager holdingsDocumentManager = RdbmsHoldingsDocumentManager.getInstance();
        List<HoldingsRecord> holdingsRecords = (List<HoldingsRecord>) getBusinessObjectService().findMatching(HoldingsRecord.class, getBibMap(id));
        for (HoldingsRecord holdingsRecord : holdingsRecords) {
            bibTree.getHoldingsTrees().add(holdingsDocumentManager.retrieveHoldingsTree(holdingsRecord.getHoldingsId(), bib, holdingsRecord));
            holdingsMap.put(holdingsRecord.getHoldingsId(), holdingsRecord.getHoldingsId());
        }
        List<BibHoldingsRecord> bibHoldingsRecords = (List<BibHoldingsRecord>) getBusinessObjectService().findMatching(BibHoldingsRecord.class, getBibMap(id));

        if (bibHoldingsRecords != null && bibHoldingsRecords.size() > 0) {
            for (BibHoldingsRecord bibHoldingsRecord : bibHoldingsRecords) {
                if (!holdingsMap.containsKey(bibHoldingsRecord.getHoldingsId())) {
                    bibTree.getHoldingsTrees().add(holdingsDocumentManager.retrieveHoldingsTree(bibHoldingsRecord.getHoldingsId(), bib, null));
                }
            }
        }
        return bibTree;
    }

    /**
     * This method verifies the existence of linked documents to the bib record. If exists throws exception with appropriate error message.
     *
     * @param bibId
     */
    @Override
    public void deleteVerify(String bibId) {
        String uuids = null;
        int uuidCount = 0;
        BibTree deletingBibTree = retrieveBibTree(bibId);
        if (deletingBibTree != null) {
            StringBuilder uuidsSB = new StringBuilder();
            uuidsSB.append(bibId).append(DocStoreConstants.COMMA);
            uuidCount++;
            for (HoldingsTree holdingsTree : deletingBibTree.getHoldingsTrees()) {
                if (null != holdingsTree.getHoldings() && null != holdingsTree.getHoldings().getId()) {
                    if (holdingsTree.getHoldings().isBoundWithBib()) {
                        DocstoreException docstoreException = new DocstoreValidationException(DocstoreResources.BOUND_WITH_DELETE_MESSAGE, DocstoreResources.BOUND_WITH_DELETE_MESSAGE);
                        throw docstoreException;
                    } else if (holdingsTree.getHoldings().isSeries()) {
                        DocstoreException docstoreException = new DocstoreValidationException(DocstoreResources.ANALYTIC_DELETE_MESSAGE_HOLDINGS, DocstoreResources.ANALYTIC_DELETE_MESSAGE_HOLDINGS);
                        throw docstoreException;
                    }
                    uuidsSB.append(holdingsTree.getHoldings().getId()).append(DocStoreConstants.COMMA);
                    uuidCount++;
                }
                for (Item item : holdingsTree.getItems()) {
                    if (null != item && null != item.getId()) {
                        if (item.isAnalytic()) {
                            DocstoreException docstoreException = new DocstoreValidationException(DocstoreResources.ANALYTIC_DELETE_MESSAGE_ITEM, DocstoreResources.ANALYTIC_DELETE_MESSAGE_ITEM);
                            throw docstoreException;
                        }
                        uuidsSB.append(item.getId()).append(DocStoreConstants.COMMA);
                        uuidCount++;
                    }
                }
            }
            uuids = uuidsSB.substring(0, uuidsSB.length() - 1);
        }
        checkUuidsToDelete(uuids, uuidCount);
    }
}
