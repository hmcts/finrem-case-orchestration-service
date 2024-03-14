package uk.gov.hmcts.reform.finrem.caseorchestration.utils;

import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

class FinremDateUtilsTest {

    @Test
    void testGetDateFormatter() {
        assertThat(FinremDateUtils.getDateFormatter()).isNotNull();
    }
}
