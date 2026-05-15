package uk.gov.hmcts.reform.finrem.caseorchestration.service.noc;

import org.junit.jupiter.api.Test;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.FinremCaseData;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.YesOrNo;

import java.util.Map;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertAll;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.CCDConfigConstant.CHANGE_ORGANISATION_REQUEST;
import static uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.CCDConfigConstant.IS_NOC_REJECTED;

class NocUtilsTest {

    @Test
    void shouldReturnMapWithNullChangeOrganisationRequest() {
        Map<String, Object> result = NocUtils.clearChangeOrganisationRequestField();

        assertThat(result)
            .isNotNull()
            .containsKey(CHANGE_ORGANISATION_REQUEST);

        assertThat(result.get(CHANGE_ORGANISATION_REQUEST)).isNull();
    }

    @Test
    void testIsNocRequestAccepted() {
        assertAll(
            () -> assertTrue(NocUtils.isNocRequestAccepted(Map.of())),
            () -> assertTrue(NocUtils.isNocRequestAccepted(Map.of(IS_NOC_REJECTED, YesOrNo.NO.getYesOrNo()))),
            () -> assertFalse(NocUtils.isNocRequestAccepted(Map.of(IS_NOC_REJECTED, YesOrNo.YES.getYesOrNo()))),
            () -> assertTrue(NocUtils.isNocRequestAccepted(buildFinremCaseData(null))),
            () -> assertTrue(NocUtils.isNocRequestAccepted(buildFinremCaseData(YesOrNo.NO))),
            () -> assertFalse(NocUtils.isNocRequestAccepted(buildFinremCaseData(YesOrNo.YES)))
        );
    }

    private FinremCaseData buildFinremCaseData(YesOrNo isNocRejected) {
        return FinremCaseData.builder().isNocRejected(isNocRejected).build();
    }
}
