package uk.gov.hmcts.reform.finrem.caseorchestration.service.noc;

import org.junit.jupiter.api.Test;

import java.util.Map;

import static org.assertj.core.api.Assertions.assertThat;
import static uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.CCDConfigConstant.CHANGE_ORGANISATION_REQUEST;

class NocUtilsTest {

    @Test
    void shouldReturnMapWithNullChangeOrganisationRequest() {
        Map<String, Object> result = NocUtils.clearChangeOrganisationRequestField();

        assertThat(result)
            .isNotNull()
            .containsKey(CHANGE_ORGANISATION_REQUEST);

        assertThat(result.get(CHANGE_ORGANISATION_REQUEST)).isNull();
    }
}
