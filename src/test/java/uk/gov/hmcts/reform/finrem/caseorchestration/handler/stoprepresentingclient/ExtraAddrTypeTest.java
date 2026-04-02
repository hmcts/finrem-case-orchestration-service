package uk.gov.hmcts.reform.finrem.caseorchestration.handler.stoprepresentingclient;

import org.junit.jupiter.api.Test;

import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;

class ExtraAddrTypeTest {

    @Test
    void shouldReturnDescription_whenIdMatches() {
        // given
        String id = "applicant";

        // when
        Optional<String> result = ExtraAddrType.describe(id);

        // then
        assertThat(result).isPresent();
        assertThat(result.get()).contains("Applicant");
    }

    @Test
    void shouldMatchIgnoringCase() {
        // given
        String id = "Applicant"; // different case

        // when
        Optional<String> result = ExtraAddrType.describe(id);

        // then
        assertThat(result).isPresent();
    }

    @Test
    void shouldReturnEmpty_whenIdNotFound() {
        // given
        String id = "UNKNOWN";

        // when
        Optional<String> result = ExtraAddrType.describe(id);

        // then
        assertThat(result).isEmpty();
    }

    @Test
    void shouldReturnEmpty_whenIdIsNull() {
        // given
        String id = null;

        // when
        Optional<String> result = ExtraAddrType.describe(id);

        // then
        assertThat(result).isEmpty();
    }
}
