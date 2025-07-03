package uk.gov.hmcts.reform.finrem.caseorchestration.utils;

import java.util.List;

public class ListUtils {

    private ListUtils() {
    }

    public static <T> List<T> toSingletonListOrNull(T item) {
        return item == null ? null : List.of(item);
    }
}
