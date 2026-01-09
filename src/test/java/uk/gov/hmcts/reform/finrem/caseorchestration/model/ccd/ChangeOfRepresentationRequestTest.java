package uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd;

import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

class ChangeOfRepresentationRequestTest {

    @Test
    void testGetIntervenerPartyByIndex() {
        assertThat(ChangeOfRepresentationRequest.getIntervenerPartyByIndex(1)).isEqualTo("Intervener 1");
        assertThat(ChangeOfRepresentationRequest.getIntervenerPartyByIndex(2)).isEqualTo("Intervener 2");
        assertThat(ChangeOfRepresentationRequest.getIntervenerPartyByIndex(3)).isEqualTo("Intervener 3");
        assertThat(ChangeOfRepresentationRequest.getIntervenerPartyByIndex(4)).isEqualTo("Intervener 4");
        assertThat(ChangeOfRepresentationRequest.getIntervenerPartyByIndex(0)).isNull();
    }
}
