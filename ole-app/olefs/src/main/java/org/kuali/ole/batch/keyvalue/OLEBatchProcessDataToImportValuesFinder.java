package org.kuali.ole.batch.keyvalue;

import org.kuali.rice.core.api.util.ConcreteKeyValue;
import org.kuali.rice.core.api.util.KeyValue;
import org.kuali.rice.krad.keyvalues.KeyValuesBase;

import java.util.ArrayList;
import java.util.List;

/**
 * Created with IntelliJ IDEA.
 * User: adityas
 * Date: 7/25/13
 * Time: 4:25 PM
 * To change this template use File | Settings | File Templates.
 */
public class OLEBatchProcessDataToImportValuesFinder extends KeyValuesBase {

    @Override
    public List<KeyValue> getKeyValues() {
        List<KeyValue> keyValues = new ArrayList<KeyValue>();
        keyValues.add(new ConcreteKeyValue("Bibliographic Data Only","Bibliographic Data Only"));
        keyValues.add(new ConcreteKeyValue("Bibliographic and Instance Data","Bibliographic and Instance Data"));
//        keyValues.add(new ConcreteKeyValue("Instance Data Only","Instance Data Only"));
        keyValues.add(new ConcreteKeyValue("Bibliographic and EInstance Data","Bibliographic and EInstance Data"));
//        keyValues.add(new ConcreteKeyValue("EInstance Data Only","EInstance Data Only"));
        keyValues.add(new ConcreteKeyValue("Bibliographic,Instance and EInstance Data","Bibliographic,Instance and EInstance Data"));
        return keyValues;
    }
}
