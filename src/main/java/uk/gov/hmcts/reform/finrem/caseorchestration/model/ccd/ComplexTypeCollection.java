package uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd;

import com.google.common.collect.ImmutableMap;

import java.util.ArrayList;
import java.util.List;

/**
 * This model is added to conform with the CCD representation of a collection of complex types.
 *
 * @param <T> the type to be held in this collection
 */
public class ComplexTypeCollection<T> extends ArrayList<ImmutableMap<String, T>> {

    /**
     * CCD requires collections of complex types to have quite specific structure.
     *
     * <pre>{
     *   "FieldId": [
     *     { "value": { "ComplexField1": "Value 1" } },
     *     { "value": { "ComplexField1": "Value 2" } }
     *   ]
     * }</pre>
     */
    public static final String COMPLEX_TYPE_KEY = "value";

    public ComplexTypeCollection() {
        super();
    }

    public ComplexTypeCollection(List<T> itemList) {
        super();
        itemList.forEach(this::addItem);
    }

    public boolean addItem(T item) {
        return this.add(createElement(item));
    }

    public T getItem(int index) {
        if (index < 0) {
            throw new IllegalArgumentException("Index must not be less than 0");
        }

        if (index > size()) {
            throw new IllegalArgumentException("Index must not be greater than size of array");
        }

        return this.get(index).get(COMPLEX_TYPE_KEY);
    }

    private ImmutableMap<String, T> createElement(T item) {
        return ImmutableMap.of(COMPLEX_TYPE_KEY, item);
    }
}
