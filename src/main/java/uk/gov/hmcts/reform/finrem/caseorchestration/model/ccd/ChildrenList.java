package uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd;

import com.google.common.collect.ImmutableMap;

import java.util.ArrayList;

/**
 * This model is added to make working with complex type FR_ChildrenInfo
 * (that is a Collection of complex types FR_ChildInfo) a bit let confusing.
 * */
public class ChildrenList extends ArrayList<ImmutableMap<String, ChildInfo>> {

    /**
     * CCD requires collections of complex types to have quite specific structure.
     *
     * <pre>{
     *   "FieldId": [
     *     { "value": { "ComplexField1": "Value 1" } },
     *     { "value": { "ComplexField1": "Value 2" } }
     *   ]
     * }</pre>
     * */
    public static final String COMPLEX_TYPE_KEY = "value";

    public boolean addValue(ChildInfo childInfo) {
        return this.add(createElement(childInfo));
    }

    private ImmutableMap<String, ChildInfo> createElement(ChildInfo childInfo) {
        return ImmutableMap.of(COMPLEX_TYPE_KEY, childInfo);
    }
}
