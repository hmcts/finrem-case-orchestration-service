package uk.gov.hmcts.reform.finrem.caseorchestration.utils;

import org.apache.tika.utils.StringUtils;

import java.lang.reflect.Field;
import java.util.Arrays;
import java.util.Objects;

public class NullChecker {

    private NullChecker() {
        // All access through static methods
    }

    @SuppressWarnings({"java:S3011", "java:S3864"})
    public static boolean anyNonNull(Object target) {
        if (target == null) {
            return false;
        }
        if (target instanceof CharSequence charSequence) {
           return !StringUtils.isEmpty(charSequence);
        }
        return Arrays.stream(target.getClass().getDeclaredFields())
            .peek(f -> f.setAccessible(true))
            .map(f -> getFieldValue(f, target))
            .anyMatch(Objects::nonNull);
    }

    private static Object getFieldValue(Field field, Object target) {
        try {
            return field.get(target);
        } catch (IllegalAccessException e) {
            throw new RuntimeException(e);
        }
    }
}
