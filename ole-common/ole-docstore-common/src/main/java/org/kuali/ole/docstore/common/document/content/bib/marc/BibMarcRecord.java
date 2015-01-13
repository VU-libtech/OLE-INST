package org.kuali.ole.docstore.common.document.content.bib.marc;

import org.apache.commons.collections.CollectionUtils;
import java.util.ArrayList;
import java.util.List;

/**
 * Created with IntelliJ IDEA.
 * User: sambasivam
 * Date: 12/13/13
 * Time: 3:35 PM
 * To change this template use File | Settings | File Templates.
 */
public class BibMarcRecord {

    public static final String TAG_003 = "003";
    private String leader = null;
    private List<ControlField> controlFields = new ArrayList<ControlField>();
    private List<DataField> dataFields = new ArrayList<DataField>();

    public String getLeader() {
        return leader;
    }

    public void setLeader(String leader) {
        this.leader = leader;
    }

    public List<ControlField> getControlFields() {
        if (controlFields == null)
            controlFields = new ArrayList<ControlField>();
        return controlFields;
    }

    public void setControlFields(List<ControlField> controlFields) {
        this.controlFields = controlFields;
    }

    public List<DataField> getDataFields() {
        if (dataFields == null)
            dataFields = new ArrayList<DataField>();
        return dataFields;
    }

    public void setDataFields(List<DataField> dataFields) {
        this.dataFields = dataFields;
    }

    public void addMarcDataField(DataField marcDataField) {
        if (!this.dataFields.contains(marcDataField)) {
            this.dataFields.add(marcDataField);
        }
    }

    public DataField getDataFieldForTag(String tag) {
        if (CollectionUtils.isNotEmpty(dataFields)) {
            for (DataField marcDataField : dataFields) {
                if (marcDataField.getTag().equalsIgnoreCase(tag)) {
                    return marcDataField;
                }
            }
        }
        return null;
    }


    public void addDataFields(DataField marcDataField) {
        if (!this.dataFields.contains(marcDataField)) {
            this.dataFields.add(marcDataField);
        }
    }

    public void addControlFields(ControlField controlField) {
        if (!this.controlFields.contains(controlField)) {
            this.controlFields.add(controlField);
        }
    }

    public String getRecordId() {
        for (ControlField cf : controlFields) {
            if (cf.getTag().equals("001")) {
                return cf.getValue();
            }
        }
        return "";
    }

}
