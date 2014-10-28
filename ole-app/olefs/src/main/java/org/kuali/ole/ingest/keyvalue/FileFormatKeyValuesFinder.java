package org.kuali.ole.ingest.keyvalue;

import org.kuali.ole.IngestUtil;
import org.kuali.ole.OLEConstants;
import org.kuali.rice.core.api.util.ConcreteKeyValue;
import org.kuali.rice.core.api.util.KeyValue;
import org.kuali.rice.krad.keyvalues.KeyValuesBase;
import java.util.ArrayList;
import java.util.List;

/**
 * Created with IntelliJ IDEA.
 * User: palanivel
 * Date: 3/6/13
 * Time: 3:01 PM
 * To change this template use File | Settings | File Templates.
 */
public class FileFormatKeyValuesFinder extends KeyValuesBase {

    @Override
    public List getKeyValues() {
        List<KeyValue> keyValues = new ArrayList<KeyValue>();
        String validFileFormat= IngestUtil.getIngestUtil().getProperty(OLEConstants.VALID_FILE_FORMAT);
        String[] fileFormat = validFileFormat.split(",");
        keyValues.add(new ConcreteKeyValue("", ""));
        for(int fileType=0;fileType<fileFormat.length;fileType++){
           keyValues.add(new ConcreteKeyValue(fileFormat[fileType], fileFormat[fileType]));
        }
        return keyValues;
    }
}
