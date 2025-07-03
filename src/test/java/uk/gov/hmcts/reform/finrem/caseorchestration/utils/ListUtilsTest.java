package uk.gov.hmcts.reform.finrem.caseorchestration.utils;

import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static uk.gov.hmcts.reform.finrem.caseorchestration.utils.ListUtils.toSingletonListOrNull;

class ListUtilsTest {

    @Test
    void testToSingletonListOrNull() {
        assertThat(toSingletonListOrNull(null)).isNull();
        assertThat(toSingletonListOrNull("B")).isEqualTo(List.of("B"));
    }
}
