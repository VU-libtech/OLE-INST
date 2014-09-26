package org.kuali.ole.describe.service.impl;

import org.apache.commons.lang.StringUtils;
import org.apache.log4j.Logger;
import org.kuali.ole.describe.bo.OleLocation;
import org.kuali.ole.describe.bo.SearchResultDisplayRow;
import org.kuali.ole.describe.form.CallNumberBrowseForm;
import org.kuali.ole.describe.form.OLESearchForm;
import org.kuali.ole.describe.service.BrowseService;
import org.kuali.ole.docstore.common.client.DocstoreClient;
import org.kuali.ole.docstore.common.client.DocstoreClientLocator;
import org.kuali.ole.docstore.common.document.*;
import org.kuali.ole.docstore.common.search.*;
import org.kuali.ole.docstore.discovery.model.CallNumberBrowseParams;
import org.kuali.ole.docstore.model.enums.DocType;
import org.kuali.ole.sys.context.SpringContext;
import org.kuali.ole.utility.callnumber.CallNumber;
import org.kuali.ole.utility.callnumber.CallNumberFactory;
import org.kuali.rice.krad.service.BusinessObjectService;
import org.kuali.rice.krad.service.KRADServiceLocator;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.List;

/**
 * Created with IntelliJ IDEA.
 * User: jaganm
 * Date: 3/16/14
 * Time: 9:17 PM
 * To change this template use File | Settings | File Templates.
 */
public class BrowseServiceImpl implements BrowseService {

    private static final Logger LOG = Logger.getLogger(BrowseServiceImpl.class);
    private DocstoreClientLocator docstoreClientLocator;

    protected int totRecCount = 0;
    protected int pageSize = 0;
    protected int matchIndex = 0;
    protected int startIndex = 0;
    protected String nextBrowseValue = "";
    protected String previousBrowseValue = "";

    private DocstoreClient docstoreClient;

    public DocstoreClient getDocstoreClient() throws Exception {
        if(docstoreClient == null) {
            docstoreClient = getDocstoreClientLocator().getDocstoreClient();
        }
        return docstoreClient;
    }

    public DocstoreClientLocator getDocstoreClientLocator() {
        if (null == docstoreClientLocator) {
            return SpringContext.getBean(DocstoreClientLocator.class);
        }
        return docstoreClientLocator;
    }

    public int getTotRecCount() {
        return totRecCount;
    }

    public void setTotRecCount(int totRecCount) {
        this.totRecCount = totRecCount;
    }

    public int getPageSize() {
        return pageSize;
    }

    public void setPageSize(int pageSize) {
        this.pageSize = pageSize;
    }

    public int getMatchIndex() {
        return matchIndex;
    }

    public void setMatchIndex(int matchIndex) {
        this.matchIndex = matchIndex;
    }

    public int getStartIndex() {
        return startIndex;
    }

    public void setStartIndex(int startIndex) {
        this.startIndex = startIndex;
    }

    public String validateLocation(String locationString) {
        if (locationString != null) {
            String[] arr = locationString.split("/");
            for (String location : arr) {
                if (isValidLibraryLevel(location)) {
                    return location;
                }
            }
        }
        return null;
    }

    protected List browseBibs (BrowseParams browseParams ) throws Exception{
        SearchResponse searchResponse = getDocstoreClientLocator().getDocstoreClient().search(browseParams);
        List<SearchResultDisplayRow> searchResultDisplayRows = new ArrayList<>();
        for (SearchResult searchResult : searchResponse.getSearchResults()) {
            SearchResultDisplayRow searchResultDisplayRow = new SearchResultDisplayRow();
            for (SearchResultField searchResultField : searchResult.getSearchResultFields()) {
                if (searchResultField.getDocType().equalsIgnoreCase(DocType.BIB.getCode())) {
                    if (searchResultField.getFieldName().equalsIgnoreCase("LocalId_display")) {
                        searchResultDisplayRow.setLocalId(searchResultField.getFieldValue());
                        searchResultDisplayRow.setBibIdentifier(searchResultField.getFieldValue());
                    }
                    if (searchResultField.getFieldName().equalsIgnoreCase("Title_display")) {
                        searchResultDisplayRow.setTitle(searchResultField.getFieldValue());
                    }
                    if (searchResultField.getFieldName().equalsIgnoreCase("Author_display")) {
                        searchResultDisplayRow.setAuthor(searchResultField.getFieldValue());
                    }
                    if (searchResultField.getFieldName().equalsIgnoreCase("PublicationDate_display")) {
                        searchResultDisplayRow.setPublicationDate(searchResultField.getFieldValue());
                    }
                    if (searchResultField.getFieldName().equalsIgnoreCase("DocFormat")) {
                        searchResultDisplayRow.setDocFormat(searchResultField.getFieldValue());
                    }

                }
            }
            searchResultDisplayRows.add(searchResultDisplayRow);
        }
        return searchResultDisplayRows;
    }


    protected List browseItems (BrowseParams browseParams ) throws Exception{
        SearchResponse searchResponse = getDocstoreClient().browseItems(browseParams);
        List<Item> itemList = new ArrayList<Item>();
        for (SearchResult searchResult : searchResponse.getSearchResults()) {
            Item item = new Item();
            for (SearchResultField searchResultField : searchResult.getSearchResultFields()) {
                if (searchResultField.getDocType().equalsIgnoreCase(DocType.ITEM.getCode())) {
                    if (searchResultField.getFieldName().equalsIgnoreCase("id")) {
                        item.setId(searchResultField.getFieldValue());
//                        item = getDocstoreClient().retrieveItem(searchResultField.getFieldValue());
//                        Bib bib = getDocstoreClient().retrieveBib(item.getHolding().getBib().getId());
                        Holdings holdings = new PHoldings();
                        item.setHolding(holdings);
                        Bib bib = new BibMarc();
                        item.getHolding().setBib(bib);
                    }
                    if (searchResultField.getFieldName().equalsIgnoreCase("LocalId_display")) {
                        item.setLocalId(searchResultField.getFieldValue());
                    }
                    if (searchResultField.getFieldName().equalsIgnoreCase("CallNumber_display")) {
                        item.setCallNumber(searchResultField.getFieldValue());
                    }
                    if (searchResultField.getFieldName().equalsIgnoreCase("Location_search")) {
                        item.setLocationName(searchResultField.getFieldValue());
                    }
                    if (searchResultField.getFieldName().equalsIgnoreCase("bibIdentifier")) {
                        item.getHolding().getBib().setId(searchResultField.getFieldValue());
                    }
                    if (searchResultField.getFieldName().equalsIgnoreCase("Author_display")) {
                        item.getHolding().getBib().setAuthor(searchResultField.getFieldValue());
                    }
                    if (searchResultField.getFieldName().equalsIgnoreCase("Title_display")) {
                        item.getHolding().getBib().setTitle(searchResultField.getFieldValue());
                    }
                    if (searchResultField.getFieldName().equalsIgnoreCase("ShelvingOrder_sort")) {
                        item.setShelvingOrder(searchResultField.getFieldValue());
                    }
                }
            }
            itemList.add(item);
        }
        return itemList;
    }



    protected List browseHoldings(BrowseParams browseParams) throws Exception {
        SearchResponse searchResponse = getDocstoreClient().browseHoldings(browseParams);
        List<Holdings> holdingsList = new ArrayList<Holdings>();
        for (SearchResult searchResult : searchResponse.getSearchResults()) {
            Holdings holdings = new Holdings();
            for (SearchResultField searchResultField : searchResult.getSearchResultFields()) {
                if (searchResultField.getDocType().equalsIgnoreCase(DocType.HOLDINGS.getCode())) {
                    if (searchResultField.getFieldName().equalsIgnoreCase("id")) {
                        holdings.setId(searchResultField.getFieldValue());
//                        holdings = getDocstoreClient().retrieveHoldings(searchResultField.getFieldValue());
//                        Bib bib = getDocstoreClient().retrieveBib(holdings.getBib().getId());
                        Bib bib = new BibMarc();
                        holdings.setBib(bib);
                    }
                    if (searchResultField.getFieldName().equalsIgnoreCase("LocalId_display")) {
                        holdings.setLocalId(searchResultField.getFieldValue());
                    }
                    if (searchResultField.getFieldName().equalsIgnoreCase("CallNumber_display")) {
                        holdings.setCallNumber(searchResultField.getFieldValue());
                    }
                    if (searchResultField.getFieldName().equalsIgnoreCase("Location_search")) {
                        holdings.setLocationName(searchResultField.getFieldValue());
                    }
                    if (searchResultField.getFieldName().equalsIgnoreCase("bibIdentifier")) {
                        holdings.getBib().setId(searchResultField.getFieldValue());
                    }
                    if (searchResultField.getFieldName().equalsIgnoreCase("Author_display")) {
                        holdings.getBib().setAuthor(searchResultField.getFieldValue());
                    }
                    if (searchResultField.getFieldName().equalsIgnoreCase("Title_display")) {
                        holdings.getBib().setTitle(searchResultField.getFieldValue());
                    }
                    if (searchResultField.getFieldName().equalsIgnoreCase("ShelvingOrder_sort")) {
                        holdings.setShelvingOrder(searchResultField.getFieldValue());
                    }

                }
            }
            holdingsList.add(holdings);
        }

        return holdingsList;
    }

    protected List buildBrowseBibsResults(SearchResponse searchResponse) throws Exception{

        List<SearchResultDisplayRow> searchResultDisplayRows = new ArrayList<>();
        for (SearchResult searchResult : searchResponse.getSearchResults()) {
            SearchResultDisplayRow searchResultDisplayRow = new SearchResultDisplayRow();
            for (SearchResultField searchResultField : searchResult.getSearchResultFields()) {
                if (searchResultField.getDocType().equalsIgnoreCase(DocType.BIB.getCode())) {
                    if (searchResultField.getFieldName().equalsIgnoreCase("LocalId_display")) {
                        searchResultDisplayRow.setLocalId(searchResultField.getFieldValue());
                        searchResultDisplayRow.setBibIdentifier(searchResultField.getFieldValue());
                    }
                    if (searchResultField.getFieldName().equalsIgnoreCase("Title_display")) {
                        searchResultDisplayRow.setTitle(searchResultField.getFieldValue());
                    }
                    if (searchResultField.getFieldName().equalsIgnoreCase("Author_display")) {
                        searchResultDisplayRow.setAuthor(searchResultField.getFieldValue());
                    }
                    if (searchResultField.getFieldName().equalsIgnoreCase("PublicationDate_display")) {
                        searchResultDisplayRow.setPublicationDate(searchResultField.getFieldValue());
                    }
                    if (searchResultField.getFieldName().equalsIgnoreCase("DocFormat")) {
                        searchResultDisplayRow.setDocFormat(searchResultField.getFieldValue());
                    }

                }
            }
            searchResultDisplayRows.add(searchResultDisplayRow);
        }
        return searchResultDisplayRows;
    }


    protected List buildBrowseHoldingsResults(SearchResponse searchResponse) throws Exception{
        List<Holdings> holdingsList = new ArrayList<Holdings>();
        for (SearchResult searchResult : searchResponse.getSearchResults()) {
            Holdings holdings = new Holdings();
            for (SearchResultField searchResultField : searchResult.getSearchResultFields()) {
                if (searchResultField.getDocType().equalsIgnoreCase(DocType.HOLDINGS.getCode())) {
                    if (searchResultField.getFieldName().equalsIgnoreCase("id")) {
                        holdings.setId(searchResultField.getFieldValue());
                        Bib bib = new BibMarc();
                        holdings.setBib(bib);
                    }
                    if (searchResultField.getFieldName().equalsIgnoreCase("LocalId_display")) {
                        holdings.setLocalId(searchResultField.getFieldValue());
                    }
                    if (searchResultField.getFieldName().equalsIgnoreCase("CallNumber_display")) {
                        holdings.setCallNumber(searchResultField.getFieldValue());
                    }
                    if (searchResultField.getFieldName().equalsIgnoreCase("Location_search")) {
                        holdings.setLocationName(searchResultField.getFieldValue());
                    }
                    if (searchResultField.getFieldName().equalsIgnoreCase("bibIdentifier")) {
                        holdings.getBib().setId(searchResultField.getFieldValue());
                    }
                    if (searchResultField.getFieldName().equalsIgnoreCase("Author_display")) {
                        holdings.getBib().setAuthor(searchResultField.getFieldValue());
                    }
                    if (searchResultField.getFieldName().equalsIgnoreCase("Title_display")) {
                        holdings.getBib().setTitle(searchResultField.getFieldValue());
                    }
                    if (searchResultField.getFieldName().equalsIgnoreCase("Title_display")) {
                        holdings.getBib().setTitle(searchResultField.getFieldValue());
                    }
                    if (searchResultField.getFieldName().equalsIgnoreCase("ShelvingOrder_sort")) {
                        holdings.setShelvingOrder(searchResultField.getFieldValue());
                    }

                }
            }
            holdingsList.add(holdings);
        }
        previousBrowseValue = "[ * TO " + holdingsList.get(0).getShelvingOrder() +"]";
        nextBrowseValue = "[" + holdingsList.get(holdingsList.size() - 1).getShelvingOrder() + " TO * ]";

        return holdingsList;
    }



    protected List buildBrowseItemsResults(SearchResponse searchResponse) throws Exception {
        List<Item> itemList = new ArrayList<Item>();
        for (SearchResult searchResult : searchResponse.getSearchResults()) {
            Item item = new Item();
            for (SearchResultField searchResultField : searchResult.getSearchResultFields()) {
                if (searchResultField.getDocType().equalsIgnoreCase(DocType.ITEM.getCode())) {
                    if (searchResultField.getFieldName().equalsIgnoreCase("id")) {
                        item.setId(searchResultField.getFieldValue());
//                        item = getDocstoreClient().retrieveItem(searchResultField.getFieldValue());
//                        Bib bib = getDocstoreClient().retrieveBib(item.getHolding().getBib().getId());
                        Holdings holdings = new PHoldings();
                        item.setHolding(holdings);
                        Bib bib = new BibMarc();
                        item.getHolding().setBib(bib);
                    }
                    if (searchResultField.getFieldName().equalsIgnoreCase("LocalId_display")) {
                        item.setLocalId(searchResultField.getFieldValue());
                    }
                    if (searchResultField.getFieldName().equalsIgnoreCase("CallNumber_display")) {
                        item.setCallNumber(searchResultField.getFieldValue());
                    }
                    if (searchResultField.getFieldName().equalsIgnoreCase("Location_search")) {
                        item.setLocationName(searchResultField.getFieldValue());
                    }
                    if (searchResultField.getFieldName().equalsIgnoreCase("holdingsIdentifier")) {
                        item.getHolding().setId(searchResultField.getFieldValue());
                    }
                    if (searchResultField.getFieldName().equalsIgnoreCase("bibIdentifier")) {
                        item.getHolding().getBib().setId(searchResultField.getFieldValue());
                    }
                    if (searchResultField.getFieldName().equalsIgnoreCase("Author_display")) {
                        item.getHolding().getBib().setAuthor(searchResultField.getFieldValue());
                    }
                    if (searchResultField.getFieldName().equalsIgnoreCase("Title_display")) {
                        item.getHolding().getBib().setTitle(searchResultField.getFieldValue());
                    }
                    if (searchResultField.getFieldName().equalsIgnoreCase("ShelvingOrder_sort")) {
                        item.setShelvingOrder(searchResultField.getFieldValue());
                    }

                }
            }
            itemList.add(item);
        }

        previousBrowseValue = "[ * TO " + itemList.get(0).getShelvingOrder() +"]";
        nextBrowseValue = "[" + itemList.get(itemList.size() - 1).getShelvingOrder() + " TO * ]";

        return itemList;
    }

    protected List browseList(CallNumberBrowseParams callNumberBrowseParams) {
        List list = null;
        try {
            BrowseParams browseParams = totalBrowseParams(callNumberBrowseParams);
            browseParams.setPageSize(callNumberBrowseParams.getNumRows());
            String docType = callNumberBrowseParams.getDocTye();
            if (DocType.BIB.getCode().equals(docType)) {
                  list = browseBibs(browseParams);
            } else {
                if (docType.equalsIgnoreCase(DocType.ITEM.getCode())) {
                    list = browseItems(browseParams);
                } else {
                    list = browseHoldings(browseParams);
                }
            }
        } catch (Exception e) {
            LOG.info("Exception in callNumberBrowse " + e);
        }
        return list;
    }

    protected List browseListNext(CallNumberBrowseParams callNumberBrowseParams) {
        List list = null;
        try {
            BrowseParams browseParams = totalBrowseParamsNext(callNumberBrowseParams, nextBrowseValue);
            browseParams.setPageSize(callNumberBrowseParams.getNumRows());
            String docType = callNumberBrowseParams.getDocTye();
            if (DocType.BIB.getCode().equals(docType)) {
                  list = browseBibs(browseParams);
            } else {
                if (docType.equalsIgnoreCase(DocType.ITEM.getCode())) {
                    List<Item> itemList = browseItems(browseParams);
                    previousBrowseValue = "[ * TO " + itemList.get(0).getShelvingOrder() +"]";
                    nextBrowseValue = "[" + itemList.get(itemList.size() - 1).getShelvingOrder() + " TO * ]";
                    return itemList;
                } else {
                    List<Holdings> holdingsList = browseHoldings(browseParams);
                    previousBrowseValue = "[ * TO " + holdingsList.get(0).getShelvingOrder() +"]";
                    nextBrowseValue = "[" + holdingsList.get(holdingsList.size() - 1).getShelvingOrder() + " TO * ]";
                    return holdingsList;
                }

            }
        } catch (Exception e) {
            LOG.info("Exception in callNumberBrowse " + e);
        }
        return list;
    }

    protected List browseListPrevious(CallNumberBrowseParams callNumberBrowseParams) {
        List list = null;
        try {
            BrowseParams browseParams = totalBrowseParamsNextPrevious(callNumberBrowseParams, previousBrowseValue);
            browseParams.setPageSize(callNumberBrowseParams.getNumRows());
            String docType = callNumberBrowseParams.getDocTye();
            if (DocType.BIB.getCode().equals(docType)) {
                  list = browseBibs(browseParams);
            } else {
                if (docType.equalsIgnoreCase(DocType.ITEM.getCode())) {
                    List<Item> itemList = browseItems(browseParams);
                    if(startIndex >= pageSize) {
                        Collections.reverse(itemList);
                    }
                    previousBrowseValue = "[ * TO " + itemList.get(0).getShelvingOrder() +"]";
                    nextBrowseValue = "[" + itemList.get(itemList.size() - 1).getShelvingOrder() + " TO * ]";
                    return itemList;

                } else {
                    List<Holdings> holdingsList = browseHoldings(browseParams);
                    if(startIndex >= pageSize) {
                        Collections.reverse(holdingsList);
                    }
                    previousBrowseValue = "[ * TO " + holdingsList.get(0).getShelvingOrder() +"]";
                    nextBrowseValue = "[" + holdingsList.get(holdingsList.size() - 1).getShelvingOrder() + " TO * ]";
                    return holdingsList;
                }
            }
        } catch (Exception e) {
            LOG.info("Exception in callNumberBrowse " + e);
        }
        return list;
    }

    protected boolean isValidLibraryLevel(String location) {
        boolean valid = false;
        BusinessObjectService businessObjectService = KRADServiceLocator.getBusinessObjectService();
        Collection<OleLocation> oleLocationCollection = businessObjectService.findAll(OleLocation.class);
        for (OleLocation oleLocation : oleLocationCollection) {
            String locationCode = oleLocation.getLocationCode();
            String levelCode = oleLocation.getOleLocationLevel().getLevelCode();
            if ("LIBRARY".equals(levelCode) && (locationCode.equals(location))) {
                valid = true;
                return valid;
            }
        }
        return valid;
    }

    public boolean getPreviosFlag() {
        if (this.startIndex == 0)
            return false;
        return true;
    }

    public boolean getNextFlag() {
        if (this.startIndex + this.pageSize < this.totRecCount)
            return true;
        return false;
    }

    public String getPageShowEntries() {
        return "Showing " + ((this.startIndex == 0) ? 1 : this.startIndex + 1) + " to "
                + (((this.startIndex + this.pageSize) > this.totRecCount) ? this.totRecCount : (this.startIndex + this.pageSize))
                + " of " + this.totRecCount + " entries";
    }

    protected List initBrowse(CallNumberBrowseParams callNumberBrowseParams) throws Exception {
        List list = null;
        int startIndex = 0;
        int totalCount = 0;
        int totalForwardCount = 0;
        String classificationScheme = callNumberBrowseParams.getClassificationScheme();
        String callNumberBrowseText = callNumberBrowseParams.getCallNumberBrowseText();
        String title = callNumberBrowseParams.getTitle();
        String docType = callNumberBrowseParams.getDocTye();
        SearchResponse searchResponse = null;

        if (DocType.BIB.getCode().equals(docType)) {
            BrowseParams browseParams = totalBrowseParams(callNumberBrowseParams);
            searchResponse = getDocstoreClient().search(browseParams);
            totalCount = (int) searchResponse.getTotalRecordCount();
            LOG.info("Total Call Number count:" + totalCount);

            if(StringUtils.isNotEmpty(title)) {
                browseParams = buildBrowseParams(callNumberBrowseParams);
                searchResponse = getDocstoreClient().search(browseParams);
                totalForwardCount = (int) searchResponse.getTotalRecordCount();
                LOG.info("Total Call Number count:" + totalCount);
            }
            else {
                totalForwardCount = totalCount;
            }
            list = buildBrowseBibsResults(searchResponse);

        } else {
            if (StringUtils.isNotEmpty(classificationScheme)) {
                BrowseParams browseParamsForTotalCallNumber = totalBrowseParams(callNumberBrowseParams);
                if (callNumberBrowseParams.getDocTye().equalsIgnoreCase(DocType.ITEM.getCode())) {
                    searchResponse = getDocstoreClient().browseItems(browseParamsForTotalCallNumber);
                    list = buildBrowseItemsResults(searchResponse);
                } else {
                    searchResponse = getDocstoreClient().browseHoldings(browseParamsForTotalCallNumber);
                    list = buildBrowseHoldingsResults(searchResponse);
                }
                totalCount = (int) searchResponse.getTotalRecordCount();
                LOG.info("Total Call Number count:" + totalCount);

                if (StringUtils.isNotEmpty(callNumberBrowseText)) {
                    CallNumber callNumber = CallNumberFactory.getInstance().getCallNumber(classificationScheme);
                    String normalizedCallNumberBrowseText = callNumber.getSortableKey(callNumberBrowseText);
                    normalizedCallNumberBrowseText = normalizedCallNumberBrowseText.replaceAll(" ", "-");
                    callNumberBrowseParams.setCallNumberBrowseText(normalizedCallNumberBrowseText);
                    BrowseParams browseParamsForwardCallNumberCount = buildBrowseParams(callNumberBrowseParams);
                    if (callNumberBrowseParams.getDocTye().equalsIgnoreCase(DocType.ITEM.getCode())) {
                        searchResponse = getDocstoreClient().browseItems(browseParamsForwardCallNumberCount);
                        list = buildBrowseItemsResults(searchResponse);
                    } else {
                        searchResponse = getDocstoreClient().browseHoldings(browseParamsForwardCallNumberCount);
                        list = buildBrowseHoldingsResults(searchResponse);
                    }
                    totalForwardCount = (int) searchResponse.getTotalRecordCount();
                } else   {
                    totalForwardCount = totalCount;
                }

            }
        }
        LOG.info("Total Forward Call Number Count:" + totalForwardCount);
        startIndex = (totalCount - totalForwardCount);
        callNumberBrowseParams.setStartIndex(startIndex);
        callNumberBrowseParams.setTotalCallNumberCount(totalCount);
        callNumberBrowseParams.setTotalForwardCallNumberCount(totalForwardCount);
        return list;
    }

    protected BrowseParams buildBrowseParams(CallNumberBrowseParams callNumberBrowseParams) {

        BrowseParams browseParams = new BrowseParams();
        List<SearchCondition>  searchConditions = new ArrayList<>();
        String docType = callNumberBrowseParams.getDocTye();
        browseParams.getSearchResultFields().add(browseParams.buildSearchResultField(docType,"id"));
        browseParams.getSearchResultFields().add(browseParams.buildSearchResultField(docType,"LocalId_display"));
        browseParams.getSearchResultFields().add(browseParams.buildSearchResultField(docType,"DocFormat"));
        if(DocType.BIB.getCode().equals(callNumberBrowseParams.getDocTye())) {
            browseParams.getSortConditions().add(browseParams.buildSortCondition("Title_sort","asc"));
            if(StringUtils.isNotEmpty(callNumberBrowseParams.getTitle())) {
                searchConditions.add(browseParams.buildSearchCondition("NONE", browseParams.buildSearchField(docType, "Title_sort", "[\""+callNumberBrowseParams.getTitle().toLowerCase()+"\" TO *]"), "AND"));
            }
            else {
                searchConditions.add(browseParams.buildSearchCondition("NONE", browseParams.buildSearchField(docType, "Title_sort", "{ * TO *}"), "AND"));
            }

            browseParams.getSearchResultFields().add(browseParams.buildSearchResultField(docType,"Title_display"));
            browseParams.getSearchResultFields().add(browseParams.buildSearchResultField(docType,"Author_display"));
            browseParams.getSearchResultFields().add(browseParams.buildSearchResultField(docType,"PublicationDate_display"));
            browseParams.setStartIndex(0);
        }
        else {
            browseParams.getSortConditions().add(browseParams.buildSortCondition("ShelvingOrder_sort","asc"));

            if(StringUtils.isNotEmpty(callNumberBrowseParams.getLocation())) {
                searchConditions.add(browseParams.buildSearchCondition("AND", browseParams.buildSearchField(docType, "Location_search", callNumberBrowseParams.getLocation()), "AND"));
            }
            if(StringUtils.isNotEmpty(callNumberBrowseParams.getClassificationScheme())) {
                searchConditions.add(browseParams.buildSearchCondition("AND", browseParams.buildSearchField(docType, "ShelvingSchemeCode_search", callNumberBrowseParams.getClassificationScheme()), "AND"));
            }
            if(StringUtils.isNotEmpty(callNumberBrowseParams.getCallNumberBrowseText())){
                searchConditions.add(browseParams.buildSearchCondition("NONE", browseParams.buildSearchField(docType, "ShelvingOrder_sort", "[" + callNumberBrowseParams.getCallNumberBrowseText() + " TO *]"), "AND"));
            }
            else {
                searchConditions.add(browseParams.buildSearchCondition("NONE", browseParams.buildSearchField(docType, "ShelvingOrder_sort", "{ * TO *}"), "AND"));
            }
            if(StringUtils.isNotEmpty(docType)) {
                searchConditions.add(browseParams.buildSearchCondition("AND", browseParams.buildSearchField(docType, "DocType", docType), "AND"));
            }
            browseParams.getSearchResultFields().add(browseParams.buildSearchResultField(docType,"Location_search"));
            browseParams.getSearchResultFields().add(browseParams.buildSearchResultField(docType,"ShelvingOrder_search"));
            browseParams.getSearchResultFields().add(browseParams.buildSearchResultField(docType,"ShelvingOrder_sort"));
            browseParams.getSearchResultFields().add(browseParams.buildSearchResultField(docType,"CallNumber_display"));
            browseParams.getSearchResultFields().add(browseParams.buildSearchResultField(docType,"Title_display"));
            browseParams.getSearchResultFields().add(browseParams.buildSearchResultField(docType,"Author_display"));
            browseParams.getSearchResultFields().add(browseParams.buildSearchResultField(docType,"bibIdentifier"));
            browseParams.getSearchResultFields().add(browseParams.buildSearchResultField(docType,"holdingsIdentifier"));
            browseParams.setStartIndex(0);
        }
        browseParams.setPageSize(callNumberBrowseParams.getNumRows());
        browseParams.getSearchConditions().addAll(searchConditions);
        return browseParams;
    }

    protected BrowseParams totalBrowseParams(CallNumberBrowseParams callNumberBrowseParams) {

        BrowseParams browseParams = new BrowseParams();

        List<SearchCondition>  searchConditions = new ArrayList<>();
        String docType = callNumberBrowseParams.getDocTye();

        browseParams.getSearchResultFields().add(browseParams.buildSearchResultField(docType,"id"));
        browseParams.getSearchResultFields().add(browseParams.buildSearchResultField(docType,"LocalId_display"));
        browseParams.getSearchResultFields().add(browseParams.buildSearchResultField(docType,"DocFormat"));

        if(DocType.BIB.getCode().equals(callNumberBrowseParams.getDocTye())) {
            browseParams.getSortConditions().add(browseParams.buildSortCondition("Title_sort","asc"));
            searchConditions.add(browseParams.buildSearchCondition("NONE", browseParams.buildSearchField(docType, "Title_sort", "{ * TO *}"), "AND"));
            browseParams.getSearchResultFields().add(browseParams.buildSearchResultField(docType,"Title_display"));
            browseParams.getSearchResultFields().add(browseParams.buildSearchResultField(docType,"Author_display"));
            browseParams.getSearchResultFields().add(browseParams.buildSearchResultField(docType,"PublicationDate_display"));
        }
        else {
            browseParams.getSortConditions().add(browseParams.buildSortCondition("ShelvingOrder_sort","asc"));
            if(StringUtils.isNotEmpty(callNumberBrowseParams.getLocation())) {
                searchConditions.add(browseParams.buildSearchCondition("AND", browseParams.buildSearchField(docType, "Location_search", callNumberBrowseParams.getLocation()), "AND"));
            }
            if(StringUtils.isNotEmpty(callNumberBrowseParams.getClassificationScheme())) {
                searchConditions.add(browseParams.buildSearchCondition("AND", browseParams.buildSearchField(docType, "ShelvingSchemeCode_search", callNumberBrowseParams.getClassificationScheme()), "AND"));
            }
            searchConditions.add(browseParams.buildSearchCondition("NONE", browseParams.buildSearchField(docType, "ShelvingOrder_sort", "{ * TO *}"), "AND"));
            browseParams.getSearchResultFields().add(browseParams.buildSearchResultField(docType,"Location_search"));
            browseParams.getSearchResultFields().add(browseParams.buildSearchResultField(docType,"ShelvingOrder_search"));
            browseParams.getSearchResultFields().add(browseParams.buildSearchResultField(docType,"ShelvingOrder_sort"));
            browseParams.getSearchResultFields().add(browseParams.buildSearchResultField(docType,"CallNumber_display"));
            browseParams.getSearchResultFields().add(browseParams.buildSearchResultField(docType,"Title_display"));
            browseParams.getSearchResultFields().add(browseParams.buildSearchResultField(docType,"Author_display"));
            browseParams.getSearchResultFields().add(browseParams.buildSearchResultField(docType,"bibIdentifier"));
            browseParams.getSearchResultFields().add(browseParams.buildSearchResultField(docType,"holdingsIdentifier"));
        }

        if(StringUtils.isNotEmpty(docType)) {
            searchConditions.add(browseParams.buildSearchCondition("AND", browseParams.buildSearchField(docType, "DocType", docType), "AND"));
        }

        browseParams.setStartIndex(startIndex);
        browseParams.setPageSize(callNumberBrowseParams.getNumRows());
        browseParams.getSearchConditions().addAll(searchConditions);
        return browseParams;
    }
    protected BrowseParams totalBrowseParamsNext(CallNumberBrowseParams callNumberBrowseParams, String nextBrowseValue) {

        BrowseParams browseParams = new BrowseParams();

        List<SearchCondition>  searchConditions = new ArrayList<>();
        String docType = callNumberBrowseParams.getDocTye();

        browseParams.getSearchResultFields().add(browseParams.buildSearchResultField(docType,"id"));
        browseParams.getSearchResultFields().add(browseParams.buildSearchResultField(docType,"LocalId_display"));
        browseParams.getSearchResultFields().add(browseParams.buildSearchResultField(docType,"DocFormat"));

        if(DocType.BIB.getCode().equals(callNumberBrowseParams.getDocTye())) {
            browseParams.getSortConditions().add(browseParams.buildSortCondition("Title_sort","asc"));
            searchConditions.add(browseParams.buildSearchCondition("NONE", browseParams.buildSearchField(docType, "Title_sort", "{ * TO *}"), "AND"));
            browseParams.getSearchResultFields().add(browseParams.buildSearchResultField(docType,"Title_display"));
            browseParams.getSearchResultFields().add(browseParams.buildSearchResultField(docType,"Author_display"));
            browseParams.getSearchResultFields().add(browseParams.buildSearchResultField(docType,"PublicationDate_display"));
            browseParams.setStartIndex(startIndex);

        }
        else {
            browseParams.getSortConditions().add(browseParams.buildSortCondition("ShelvingOrder_sort","asc"));
            if(StringUtils.isNotEmpty(callNumberBrowseParams.getLocation())) {
                searchConditions.add(browseParams.buildSearchCondition("AND", browseParams.buildSearchField(docType, "Location_search", callNumberBrowseParams.getLocation()), "AND"));
            }
            if(StringUtils.isNotEmpty(callNumberBrowseParams.getClassificationScheme())) {
                searchConditions.add(browseParams.buildSearchCondition("AND", browseParams.buildSearchField(docType, "ShelvingSchemeCode_search", callNumberBrowseParams.getClassificationScheme()), "AND"));
            }
            searchConditions.add(browseParams.buildSearchCondition("NONE", browseParams.buildSearchField(docType, "ShelvingOrder_sort", nextBrowseValue), "AND"));
            browseParams.getSearchResultFields().add(browseParams.buildSearchResultField(docType,"Location_search"));
            browseParams.getSearchResultFields().add(browseParams.buildSearchResultField(docType,"ShelvingOrder_search"));
            browseParams.getSearchResultFields().add(browseParams.buildSearchResultField(docType,"ShelvingOrder_sort"));
            browseParams.getSearchResultFields().add(browseParams.buildSearchResultField(docType,"CallNumber_display"));
            browseParams.getSearchResultFields().add(browseParams.buildSearchResultField(docType,"Title_display"));
            browseParams.getSearchResultFields().add(browseParams.buildSearchResultField(docType,"Author_display"));
            browseParams.getSearchResultFields().add(browseParams.buildSearchResultField(docType,"bibIdentifier"));
            browseParams.getSearchResultFields().add(browseParams.buildSearchResultField(docType,"holdingsIdentifier"));
            browseParams.setStartIndex(1);

        }

        if(StringUtils.isNotEmpty(docType)) {
            searchConditions.add(browseParams.buildSearchCondition("AND", browseParams.buildSearchField(docType, "DocType", docType), "AND"));
        }
        browseParams.setPageSize(callNumberBrowseParams.getNumRows());
        browseParams.getSearchConditions().addAll(searchConditions);
        return browseParams;
    }
    protected BrowseParams totalBrowseParamsNextPrevious(CallNumberBrowseParams callNumberBrowseParams, String previousBrowseValue) {

        BrowseParams browseParams = new BrowseParams();

        List<SearchCondition>  searchConditions = new ArrayList<>();
        String docType = callNumberBrowseParams.getDocTye();

        browseParams.getSearchResultFields().add(browseParams.buildSearchResultField(docType,"id"));
        browseParams.getSearchResultFields().add(browseParams.buildSearchResultField(docType,"LocalId_display"));
        browseParams.getSearchResultFields().add(browseParams.buildSearchResultField(docType,"DocFormat"));

        if(DocType.BIB.getCode().equals(callNumberBrowseParams.getDocTye())) {
            browseParams.getSortConditions().add(browseParams.buildSortCondition("Title_sort","asc"));
            searchConditions.add(browseParams.buildSearchCondition("NONE", browseParams.buildSearchField(docType, "Title_sort", "{ * TO *}"), "AND"));
            browseParams.getSearchResultFields().add(browseParams.buildSearchResultField(docType,"Title_display"));
            browseParams.getSearchResultFields().add(browseParams.buildSearchResultField(docType,"Author_display"));
            browseParams.getSearchResultFields().add(browseParams.buildSearchResultField(docType,"PublicationDate_display"));
            browseParams.setStartIndex(startIndex);
        }
        else {

            if(StringUtils.isNotEmpty(callNumberBrowseParams.getLocation())) {
                searchConditions.add(browseParams.buildSearchCondition("AND", browseParams.buildSearchField(docType, "Location_search", callNumberBrowseParams.getLocation()), "AND"));
            }
            if(StringUtils.isNotEmpty(callNumberBrowseParams.getClassificationScheme())) {
                searchConditions.add(browseParams.buildSearchCondition("AND", browseParams.buildSearchField(docType, "ShelvingSchemeCode_search", callNumberBrowseParams.getClassificationScheme()), "AND"));
            }
            if(startIndex == 0) {
                browseParams.getSortConditions().add(browseParams.buildSortCondition("ShelvingOrder_sort","asc"));
                searchConditions.add(browseParams.buildSearchCondition("NONE", browseParams.buildSearchField(docType, "ShelvingOrder_sort", "[* TO *]"), "AND"));
                browseParams.setStartIndex(0);
            }
            else {
                browseParams.getSortConditions().add(browseParams.buildSortCondition("ShelvingOrder_sort","desc"));
                searchConditions.add(browseParams.buildSearchCondition("NONE", browseParams.buildSearchField(docType, "ShelvingOrder_sort", previousBrowseValue), "AND"));
                browseParams.setStartIndex(1);
            }
            browseParams.getSearchResultFields().add(browseParams.buildSearchResultField(docType,"Location_search"));
            browseParams.getSearchResultFields().add(browseParams.buildSearchResultField(docType,"ShelvingOrder_search"));
            browseParams.getSearchResultFields().add(browseParams.buildSearchResultField(docType,"ShelvingOrder_sort"));
            browseParams.getSearchResultFields().add(browseParams.buildSearchResultField(docType,"CallNumber_display"));
            browseParams.getSearchResultFields().add(browseParams.buildSearchResultField(docType,"Title_display"));
            browseParams.getSearchResultFields().add(browseParams.buildSearchResultField(docType,"Author_display"));
            browseParams.getSearchResultFields().add(browseParams.buildSearchResultField(docType,"bibIdentifier"));
            browseParams.getSearchResultFields().add(browseParams.buildSearchResultField(docType,"holdingsIdentifier"));
        }

        if(StringUtils.isNotEmpty(docType)) {
            searchConditions.add(browseParams.buildSearchCondition("AND", browseParams.buildSearchField(docType, "DocType", docType), "AND"));
        }

        browseParams.setPageSize(callNumberBrowseParams.getNumRows());
        browseParams.getSearchConditions().addAll(searchConditions);
        return browseParams;
    }
   /* protected BrowseParams buildBrowseParams(String location, String classificationScheme, String callNumberBrowseText, String docType, int startIndex) {
        BrowseParams browseParams = new BrowseParams();
        browseParams.getSortConditions().add(browseParams.buildSortCondition("ShelvingOrder_sort", "asc"));
        List<SearchCondition> searchConditions = new ArrayList<>();
        if (DocType.BIB.getCode().equals(docType)) {

        } else {

            if (StringUtils.isNotEmpty(location)) {
                searchConditions.add(browseParams.buildSearchCondition("AND", browseParams.buildSearchField(docType, "LocationLevel_search", location), "AND"));
            }
            if (StringUtils.isNotEmpty(classificationScheme)) {
                searchConditions.add(browseParams.buildSearchCondition("AND", browseParams.buildSearchField(docType, "ShelvingSchemeCode_search", classificationScheme), "AND"));
            }
            if (StringUtils.isNotEmpty(callNumberBrowseText)) {
                searchConditions.add(browseParams.buildSearchCondition("NONE", browseParams.buildSearchField(docType, "ShelvingOrder_sort", "{" + callNumberBrowseText + "* TO *}"), "AND"));
            } else {
                searchConditions.add(browseParams.buildSearchCondition("NONE", browseParams.buildSearchField(docType, "ShelvingOrder_sort", "{ * TO *}"), "AND"));
            }
            if (StringUtils.isNotEmpty(docType)) {
                searchConditions.add(browseParams.buildSearchCondition("AND", browseParams.buildSearchField(docType, "DocType", docType), "AND"));
            }
            browseParams.getSearchResultFields().add(browseParams.buildSearchResultField(docType, "LocationLevel_search"));
            browseParams.getSearchResultFields().add(browseParams.buildSearchResultField(docType, "ShelvingOrder_search"));
        }
        browseParams.setStartIndex(startIndex);
        browseParams.getSearchResultFields().add(browseParams.buildSearchResultField(docType, "id"));
        browseParams.getSearchResultFields().add(browseParams.buildSearchResultField(docType, "LocalId_display"));

        browseParams.getSearchConditions().addAll(searchConditions);
        return browseParams;
    }*/

    protected CallNumberBrowseParams getBrowseParams(OLESearchForm oleSearchForm) {
        CallNumberBrowseParams callNumberBrowseParams = new CallNumberBrowseParams();

        if(StringUtils.isNotEmpty(oleSearchForm.getBrowseText())) {
            callNumberBrowseParams.setTitle(oleSearchForm.getBrowseText());
        }
        else {
            callNumberBrowseParams.setClassificationScheme(oleSearchForm.getClassificationScheme());
            callNumberBrowseParams.setLocation(oleSearchForm.getLocation());
            callNumberBrowseParams.setCallNumberBrowseText(oleSearchForm.getCallNumberBrowseText());
        }
        callNumberBrowseParams.setNumRows(oleSearchForm.getPageSize());
        callNumberBrowseParams.setDocTye(oleSearchForm.getDocType());
        this.pageSize = oleSearchForm.getPageSize();
        return callNumberBrowseParams;
    }


    public List callNumberBrowse(CallNumberBrowseForm callNumberBrowseForm) {

        List list = null;
        try {
            CallNumberBrowseParams callNumberBrowseParams = getCallNumberBrowseParams(callNumberBrowseForm);
            initBrowse(callNumberBrowseParams);
            this.totRecCount = callNumberBrowseParams.getTotalCallNumberCount();
            this.startIndex = Math.max(0 ,(int)callNumberBrowseParams.getStartIndex());
            callNumberBrowseParams.setStartIndex(this.startIndex);
            list = browseList(callNumberBrowseParams);
        } catch (Exception e) {
            LOG.info("Exception in callNumberBrowse " + e);
        }
        return list;
    }

    public List callNumberBrowsePrev(CallNumberBrowseForm callNumberBrowseForm) {

        List list =null;
        try {
            CallNumberBrowseParams callNumberBrowseParams = getCallNumberBrowseParams(callNumberBrowseForm);
            this.startIndex = Math.max(0, this.startIndex - this.pageSize);
            callNumberBrowseParams.setStartIndex((this.startIndex == 0) ? 0 : this.startIndex - 1);
            list = browseList(callNumberBrowseParams);
        } catch (Exception e) {
            LOG.info("Exception in callNumberBrowsePrev " + e);
        }
        return list;
    }

    public List callNumberBrowseNext(CallNumberBrowseForm callNumberBrowseForm) {

        List list = null;
        try {
            CallNumberBrowseParams callNumberBrowseParams = getCallNumberBrowseParams(callNumberBrowseForm);
            this.startIndex = Math.max(0, this.startIndex + this.pageSize);
            callNumberBrowseParams.setStartIndex(this.startIndex);
            list = browseList(callNumberBrowseParams);
        } catch (Exception e) {
            LOG.info("Exception in callNumberBrowseNext " + e);
        }
        return list;
    }

    private CallNumberBrowseParams getCallNumberBrowseParams(CallNumberBrowseForm callNumberBrowseForm) {
        CallNumberBrowseParams callNumberBrowseParams = new CallNumberBrowseParams();
        callNumberBrowseParams.setClassificationScheme(callNumberBrowseForm.getClassificationScheme());
        callNumberBrowseParams.setLocation(callNumberBrowseForm.getLocation());
        callNumberBrowseParams.setCallNumberBrowseText(callNumberBrowseForm.getCallNumberBrowseText());
        callNumberBrowseParams.setNumRows(callNumberBrowseForm.getPageSize());
        callNumberBrowseParams.setDocTye(callNumberBrowseForm.getDocType());
        this.pageSize = callNumberBrowseForm.getPageSize();
        return callNumberBrowseParams;
    }

    @Override
    public List browse(OLESearchForm oleSearchForm) {
        List list = null;
        try {
            startIndex = 0;
            if(pageSize != 0) {
                oleSearchForm.getSearchParams().setPageSize(pageSize);
            }
            CallNumberBrowseParams callNumberBrowseParams = getBrowseParams(oleSearchForm);
            list = initBrowse(callNumberBrowseParams);
            totRecCount = callNumberBrowseParams.getTotalCallNumberCount();
            startIndex = Math.max(0, (int) callNumberBrowseParams.getStartIndex());
            callNumberBrowseParams.setStartIndex(startIndex);
//            list = browseList(callNumberBrowseParams);
        } catch (Exception e) {
            LOG.info("Exception in title browse " + e);
        }
        return list;
    }

    @Override
    public List browseOnChange(OLESearchForm oleSearchForm) {
        List list = null;
        try {
            oleSearchForm.getSearchParams().setStartIndex(startIndex);
            CallNumberBrowseParams callNumberBrowseParams = getBrowseParams(oleSearchForm);
            list = initBrowse(callNumberBrowseParams);
            totRecCount = callNumberBrowseParams.getTotalCallNumberCount();
//            startIndex = Math.max(0, (int) callNumberBrowseParams.getStartIndex());
            callNumberBrowseParams.setStartIndex(startIndex);
//            list = browseList(callNumberBrowseParams);
        } catch (Exception e) {
            LOG.info("Exception in title browse " + e);
        }
        return list;

    }

    @Override
    public List browsePrev(OLESearchForm oleSearchForm) {
        List list =null;
        try {
            CallNumberBrowseParams callNumberBrowseParams = getBrowseParams(oleSearchForm);
            this.startIndex = Math.max(0, this.startIndex - this.pageSize);
            callNumberBrowseParams.setStartIndex((this.startIndex == 0) ? 0 : this.startIndex - 1);
            list = browseListPrevious(callNumberBrowseParams);
        } catch (Exception e) {
            LOG.info("Exception in callNumberBrowsePrev " + e);
        }
        return list;
    }

    @Override
    public List browseNext(OLESearchForm oleSearchForm) {
        List list = null;
        try {
            CallNumberBrowseParams callNumberBrowseParams = getBrowseParams(oleSearchForm);
            this.startIndex = Math.max(0, this.startIndex + this.pageSize);
            callNumberBrowseParams.setStartIndex(this.startIndex);
            list = browseListNext(callNumberBrowseParams);
        } catch (Exception e) {
            LOG.info("Exception in callNumberBrowseNext " + e);
        }
        return list;
    }

}
