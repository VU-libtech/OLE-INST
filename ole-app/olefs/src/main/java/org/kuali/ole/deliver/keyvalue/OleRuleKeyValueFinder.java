package org.kuali.ole.deliver.keyvalue;

import org.kuali.rice.core.api.util.ConcreteKeyValue;
import org.kuali.rice.core.api.util.KeyValue;
import org.kuali.rice.krad.keyvalues.KeyValuesBase;
import org.kuali.rice.krad.service.KRADServiceLocator;
import org.kuali.rice.krms.impl.repository.RuleBo;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

/**
 * OleBorrowerKeyValue returns BorrowerTypeId and BorrowerTypeName for OleBorrowerType.
 */
public class OleRuleKeyValueFinder extends KeyValuesBase {
    /**
     * This method will populate the code as a key and name as a value and return it as list
     *
     * @return keyValues(list)
     */
    @Override
    public List getKeyValues() {
        List<KeyValue> keyValues = new ArrayList<KeyValue>();
        Collection<RuleBo> rules = KRADServiceLocator.getBusinessObjectService().findAll(RuleBo.class);
        keyValues.add(new ConcreteKeyValue("", ""));
        for (RuleBo ruleBo : rules) {
            keyValues.add(new ConcreteKeyValue(ruleBo.getName(), ruleBo.getName()));
        }
        return keyValues;
    }
}
