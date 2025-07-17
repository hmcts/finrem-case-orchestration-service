package uk.gov.hmcts.reform.finrem.caseorchestration.utils;

import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static uk.gov.hmcts.reform.finrem.caseorchestration.utils.ListUtils.toListOrNull;
import static uk.gov.hmcts.reform.finrem.caseorchestration.utils.ListUtils.toSingletonListOrNull;

class ListUtilsTest {

    @Test
    void testToSingletonListOrNullEdgeCases() {
        // null object
        assertThat(toSingletonListOrNull(null)).isNull();

        // String object
        assertThat(toSingletonListOrNull("test")).isEqualTo(List.of("test"));

        // Integer
        assertThat(toSingletonListOrNull(42)).isEqualTo(List.of(42));

        // Boolean
        assertThat(toSingletonListOrNull(true)).isEqualTo(List.of(true));

        // Custom object
        Object custom = new Object();
        assertThat(toSingletonListOrNull(custom)).isEqualTo(List.of(custom));

        // List as an item (meta test)
        List<String> innerList = List.of("x");
        assertThat(toSingletonListOrNull(innerList)).isEqualTo(List.of(innerList));

        // Mutable object inside
        StringBuilder sb = new StringBuilder("abc");
        assertThat(toSingletonListOrNull(sb)).containsExactly(sb);
    }

    @Test
    void testToListOrNull() {
        // null input single element
        assertThat(toListOrNull((String) null)).isNull();
        assertThat(toListOrNull((Object) null)).isNull();

        // empty array input
        assertThat(toListOrNull(new String[] {})).isNull();

        // all null values
        String a = null;
        String b = null;
        assertThat(toListOrNull(a, b)).isNull();

        // single non-null elements
        assertThat(toListOrNull("A")).isEqualTo(List.of("A"));
        assertThat(toListOrNull(1)).isEqualTo(List.of(1));

        // mixed null and non-null elements
        assertThat(toListOrNull("A", null)).isEqualTo(List.of("A"));
        assertThat(toListOrNull(1, null)).isEqualTo(List.of(1));
        String[] arrayOfStrings = new String[] {"A", null};
        assertThat(toListOrNull(arrayOfStrings)).isEqualTo(List.of("A"));

        // multiple non-null elements
        assertThat(toListOrNull("A", "B")).isEqualTo(List.of("A", "B"));
        assertThat(toListOrNull(1, 2)).isEqualTo(List.of(1, 2));

        // multiple types mixed (Object varargs)
        assertThat(toListOrNull("A", 2, null, true)).isEqualTo(List.of("A", 2, true));

        // duplicate values - just to check ordering preserved
        assertThat(toListOrNull("A", "A", null)).isEqualTo(List.of("A", "A"));
    }
}
