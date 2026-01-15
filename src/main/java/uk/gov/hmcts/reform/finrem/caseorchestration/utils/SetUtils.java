package uk.gov.hmcts.reform.finrem.caseorchestration.utils;

import java.util.Set;

public class SetUtils {

    private SetUtils() {
    }

    /**
     * Returns the given set if it is not {@code null} and not empty;
     * otherwise returns {@code null}.
     *
     * @param <T> the type of elements in the set
     * @param set the input set
     * @return the input set if it is non-null and non-empty, otherwise {@code null}
     */
    public static <T> Set<T> nullIfEmpty(Set<T> set) {
        return (set == null || set.isEmpty()) ? null : set;
    }
}
