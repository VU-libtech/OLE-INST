package org.kuali.ole.describe.keyvalue;

import org.kuali.rice.core.api.util.ConcreteKeyValue;
import org.kuali.rice.core.api.util.KeyValue;
import org.kuali.rice.krad.keyvalues.KeyValuesBase;

import java.util.ArrayList;
import java.util.List;

/**
 * Created with IntelliJ IDEA.
 * User: ?
 * Date: 2/7/13
 * Time: 3:27 PM
 * To change this template use File | Settings | File Templates.
 */
public class OleDiscoveryExportFormatValueFinder extends KeyValuesBase {
    /**
     * This method will populate the code as a key and name as a value and return it as list
     *
     * @return keyValues(list)
     */
    @Override
    public List getKeyValues() {
        List<KeyValue> keyValues = new ArrayList<KeyValue>();

        keyValues.add(new ConcreteKeyValue("MARC XML", "MARC XML"));
        keyValues.add(new ConcreteKeyValue("MARC21", "MARC21"));
        return keyValues;
    }

}
