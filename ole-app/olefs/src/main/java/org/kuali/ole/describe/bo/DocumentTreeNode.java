package org.kuali.ole.describe.bo;

import org.apache.commons.lang.StringEscapeUtils;
import org.kuali.ole.OLEConstants;
import org.kuali.ole.docstore.common.document.*;
import org.kuali.ole.docstore.model.bo.*;
import org.kuali.ole.docstore.model.enums.DocType;

import java.util.Map;

/**
 * Created with IntelliJ IDEA.
 * User: Sreekanth
 * Date: 12/20/12
 * Time: 10:57 AM
 * To change this template use File | Settings | File Templates.
 */
public class DocumentTreeNode {

    private String title;
    private String uuid;
    private boolean select;
    private boolean returnCheck;

    private OleDocument oleDocument;
    private Bib bib;
    private Holdings holdings;
    private Item item;
    private BibTree bibTree;

    private String selectedInstance;
    private Map<String, String> selectionMap;
    private String holdingLocation;

    public DocumentTreeNode() {

    }

    public BibTree getBibTree() {
        return bibTree;
    }

    public void setBibTree(BibTree bibTree) {
        this.bibTree = bibTree;
    }

    public Holdings getHoldings() {
        return holdings;
    }

    public Item getItem() {
        return item;
    }

    public void setItem(Item item) {
        this.item = item;
    }

    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public String getUuid() {
        return uuid;
    }

    public void setUuid(String uuid) {
        this.uuid = uuid;
    }

    public void setBib(Bib bib) {
        this.bib = bib;
        StringBuilder titleBuilder = new StringBuilder();
        if (bib != null) {
            if (bib.getTitle() != null) {
                titleBuilder.append(bib.getTitle());
            }
            if (titleBuilder.length() > 0) {
                titleBuilder.append(" / ");
            }
            if (bib.getAuthor() != null) {
                titleBuilder.append(bib.getAuthor());
            }
            if (bib.isStaffOnly()) {
                setTitle("<i><font color='red'>" + StringEscapeUtils.escapeHtml(titleBuilder.toString()) + "</font></i>");
            } else {
                setTitle(StringEscapeUtils.escapeHtml(titleBuilder.toString()));
            }
        } else {
            if (bib.isStaffOnly()) {
                setTitle("<i><font color='red'>" + "Bibliographic Title" + "</font></i>");
            } else {
                setTitle("Bibliographic Title");
            }
        }

        setUuid(bib.getId());

    }

    public void setHoldings(HoldingsTree holdingsTree) {
        this.holdings = holdingsTree.getHoldings();

        if (holdingsTree != null && holdingsTree.getHoldings() != null) {
            if (holdingsTree.getHoldings().getHoldingsType().equalsIgnoreCase("electronic")) {
                setEholdings(holdingsTree);
            } else {
                setTitle(buildTreeDataForHoldings(holdingsTree));
                setUuid(holdingsTree.getHoldings().getId());
            }

        } else {
            setTitle("");
        }

    }

    public void setWorkBibDocument(WorkBibDocument workBibDocument) {
        this.oleDocument = workBibDocument;
        StringBuilder titleBuilder = new StringBuilder();
        if (workBibDocument != null) {
            if (workBibDocument.getTitle() != null) {
                titleBuilder.append(workBibDocument.getTitle());
            }
            if (titleBuilder.length() > 0) {
                titleBuilder.append(" / ");
            }
            if (workBibDocument.getAuthor() != null) {
                titleBuilder.append(workBibDocument.getAuthor());
            }

            setTitle(StringEscapeUtils.escapeHtml(titleBuilder.toString()));
        } else {
            setTitle("Bibliographic Title");
        }

        setUuid(workBibDocument.getId());

    }

    public void setWorkItemDocument(Item item) {
        this.item = item;
        if (item != null) {
            setTitle(buildTreeDataForItem(item));
        } else {
            setTitle("Item");
        }

        setUuid(item.getId());
    }

    public void setWorkHoldingsDocument(Item item, Holdings holdings) {
        this.item = item;
        if (item != null) {
            setTitle(buildTreeDataForItem(item, holdings));
        } else {
            setTitle("Item");
        }

        setUuid(item.getId());
    }

    public void setEholdings(HoldingsTree holdingsTree) {
        this.holdings = holdingsTree.getHoldings();
        setTitle(buildTreeDataForHoldings(holdingsTree));
        String eHoldingsTitle = OLEConstants.E_HOLDINGS_DOC_TYPE;
        StringBuffer stringBuffer = new StringBuffer();
        if (holdingsTree != null && holdingsTree.getHoldings() != null) {

               /* if (holdings.getLocationName() != null && holdings.getLocationName().length() > 0) {
                    stringBuffer.append(holdings.getLocationName());
                }
               *//* if (stringBuffer.length() > 0 && workEInstanceDocument.getWorkEHoldingsDocument().geteResourceName() != null && workEInstanceDocument.getWorkEHoldingsDocument().geteResourceName().length() > 0) {
                    stringBuffer.append("-");
                }
                if (workEInstanceDocument.getWorkEHoldingsDocument().geteResourceName() != null) {
                    stringBuffer.append(workEInstanceDocument.getWorkEHoldingsDocument().geteResourceName());
                }*//*

            if (stringBuffer.length() > 0) {
                setTitle(stringBuffer.toString());
            } else {
                setTitle(eHoldingsTitle);
            }*/

            if ( holdingsTree.getHoldings().isStaffOnly()) {
                String label = getTitle();
                label = "<i><font color='red'>" + label + "</font></i>";
                setTitle(label);
            }
        }


        setUuid( holdingsTree.getHoldings().getId() + " " + DocType.EHOLDINGS.getCode());
    }

    public OleDocument getWorkBibDocument() {
        getTitle();
        getUuid();
        return oleDocument = new WorkBibDocument();
    }

    public OleDocument getWorkInstanceDocument() {
/*        getTitle();
        getUuid();*/
        return oleDocument = new WorkInstanceDocument();
    }

    public OleDocument getWorkItemDocument() {
        getTitle();
        getUuid();
        return oleDocument = new WorkItemDocument();
    }


    public OleDocument getWorkEInstanceDocument() {
        getTitle();
        getUuid();
        return oleDocument = new WorkEInstanceDocument();
    }


    public String getSelectedInstance() {
        return selectedInstance;
    }

    public void setSelectedInstance(String selectedInstance) {
        this.selectedInstance = selectedInstance;
    }


    public boolean isSelect() {
        return select;
    }

    public void setSelect(boolean select) {
        this.select = select;
    }

    public Map<String, String> getSelectionMap() {
        return selectionMap;
    }

    public void setSelectionMap(Map<String, String> selectionMap) {
        this.selectionMap = selectionMap;
    }

    public boolean isReturnCheck() {
        return returnCheck;
    }

    public void setReturnCheck(boolean returnCheck) {
        this.returnCheck = returnCheck;
    }


    public String buildTreeDataForHoldings(HoldingsTree holdingsTree) {

        return new EditorFormDataHandler().getHoldingsLabel(holdingsTree);
    }

    public String buildTreeDataForItem(Item item, Holdings holdings) {

        return new EditorFormDataHandler().getItemLabel(holdings, item);

    }

    public String buildTreeDataForItem(Item item) {
        String itemLevelContent = null;
        String itemLocation = null;
        StringBuilder treeBuilder = new StringBuilder();
        String locationName = this.getHoldingLocation();
        if (item.getLocation() != null) {
            itemLocation = item.getLocation();
            if (!(itemLocation).equalsIgnoreCase(locationName)) {

                treeBuilder.append(itemLocation);
            }
        }
        StringBuffer removeFromCallNumber = new StringBuffer();
        String callNumberPrefix = null;
        // String itemLocation = null;
        if (item.getLocation() != null) {
            itemLocation = item.getLocation();
            if (!(itemLocation).equalsIgnoreCase(locationName)) {

                treeBuilder.append(itemLocation);
            }
        }
        if (item.getCallNumberPrefix() != null && item.getCallNumberPrefix().length() > 0) {
            callNumberPrefix = item.getCallNumberPrefix();
        }
        if (callNumberPrefix != null) {
            if (treeBuilder.length() > 0) {
                treeBuilder.append("-").append(callNumberPrefix);
            } else {
                treeBuilder.append(callNumberPrefix);
            }
        }
        if (item.getCallNumber() != null && item.getCallNumber().length() > 0) {
            if (treeBuilder.length() > 0) {
                treeBuilder.append("-");
            }
            treeBuilder.append(item.getCallNumber());
        }
        if (item.getEnumeration() != null && item.getEnumeration().length() > 0) {
            if (treeBuilder.length() > 0) {
                treeBuilder.append("-");
            }
            treeBuilder.append(item.getEnumeration());
            removeFromCallNumber.append(" " + item.getEnumeration());
        }
        if (item.getChronology() != null && item.getChronology().length() > 0) {
            if (treeBuilder.length() > 0) {
                treeBuilder.append("-");
            }
            treeBuilder.append(item.getChronology());
            removeFromCallNumber.append(" " + item.getChronology());

        }
        if (item.getCopyNumber() != null && item.getCopyNumber().length() > 0) {
            if (treeBuilder.length() > 0) {
                treeBuilder.append("-");
            }
            treeBuilder.append(item.getCopyNumber());
            removeFromCallNumber.append(" " + item.getCopyNumber());
        }
        if (item.getBarcode() != null && item.getBarcode().length() > 0) {
            if (treeBuilder.length() > 0) {
                treeBuilder.append("-");
            }
            treeBuilder.append(item.getBarcode());
        }
        if (treeBuilder.length() == 0) {
            if (item.getVolumeNumber() != null) {
                treeBuilder.append(item.getVolumeNumber());
            } else {
                treeBuilder.append("Item");
            }
        }
        itemLevelContent = treeBuilder.toString().replaceAll(removeFromCallNumber.toString(), "");

        return itemLevelContent;

    }

    public String getHoldingLocation() {
        return holdingLocation;
    }

    public void setHoldingLocation(String holdingLocation) {
        this.holdingLocation = holdingLocation;
    }
}
