package uk.gov.hmcts.reform.finrem.caseorchestration.utils;

import org.junit.jupiter.api.Test;

import java.util.Set;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;
import static uk.gov.hmcts.reform.finrem.caseorchestration.utils.SetUtils.nullIfEmpty;

class SetUtilsTest {

    @Test
    void testNullIfEmpty() {
        // null input
        assertThat(nullIfEmpty(null)).isNull();

        // empty list
        assertNull(nullIfEmpty(Set.of()));

        // non-empty list
        Set<String> input = Set.of("a", "b");
        Set<String> result = nullIfEmpty(input);
        assertNotNull(result);
        assertEquals(Set.of("a", "b"), result);
    }
}
