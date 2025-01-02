package uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.draftorders;

import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;
import org.junit.jupiter.params.provider.NullSource;
import org.junit.jupiter.params.provider.ValueSource;

import java.util.stream.Stream;

import static org.assertj.core.api.AssertionsForClassTypes.assertThat;
import static org.assertj.core.api.AssertionsForClassTypes.assertThatThrownBy;
import static uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.draftorders.DraftOrdersConstants.UPLOAD_PARTY_APPLICANT;
import static uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.draftorders.DraftOrdersConstants.UPLOAD_PARTY_RESPONDENT;

class OrderPartyTest {

    @ParameterizedTest
    @MethodSource("testValidInputsForUploadParty")
    void givenValidInputs_whenForUploadParty_thenReturnsOrderParty(String uploadParty, OrderParty expectedOrderParty) {
        assertThat(OrderParty.forUploadParty(uploadParty)).isEqualTo(expectedOrderParty);
    }

    private static Stream<Arguments> testValidInputsForUploadParty() {
        return Stream.of(
            Arguments.of(UPLOAD_PARTY_APPLICANT, OrderParty.APPLICANT),
            Arguments.of(UPLOAD_PARTY_RESPONDENT, OrderParty.RESPONDENT)
        );
    }

    @ParameterizedTest
    @ValueSource(strings = {"unknownparty", "", " "})
    @NullSource
    void givenInvalidInputs_whenForUploadParty_thenThrowsException(String uploadParty) {
        assertThatThrownBy(() -> OrderParty.forUploadParty(uploadParty))
            .isInstanceOf(IllegalArgumentException.class);
    }
}
