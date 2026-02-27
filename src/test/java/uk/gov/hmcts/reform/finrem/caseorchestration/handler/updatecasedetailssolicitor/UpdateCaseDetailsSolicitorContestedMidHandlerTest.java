package uk.gov.hmcts.reform.finrem.caseorchestration.handler.updatecasedetailssolicitor;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.jupiter.params.provider.Arguments;
import org.mockito.InjectMocks;
import org.mockito.junit.jupiter.MockitoExtension;

import static org.assertj.core.api.AssertionsForClassTypes.assertThat;
import static uk.gov.hmcts.reform.finrem.caseorchestration.ccd.callback.CallbackType.MID_EVENT;
import static uk.gov.hmcts.reform.finrem.caseorchestration.model.EventType.UPDATE_CASE_DETAILS_SOLICITOR;
import static uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.CaseType.CONSENTED;
import static uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.CaseType.CONTESTED;
import static uk.gov.hmcts.reform.finrem.caseorchestration.test.Assertions.assertCanHandle;

// Common methods tested in Abstract test class.

@ExtendWith(MockitoExtension.class)
class UpdateCaseDetailsSolicitorContestedMidHandlerTest {

    @InjectMocks
    private UpdateCaseDetailsSolicitorContestedMidHandler underTest;

    @Test
    void testCanHandle() {
        assertCanHandle(underTest,
            Arguments.of(MID_EVENT, CONTESTED, UPDATE_CASE_DETAILS_SOLICITOR),
            Arguments.of(MID_EVENT, CONSENTED, UPDATE_CASE_DETAILS_SOLICITOR)
        );
    }

    @Test
    void when_handle_then_shouldNotClearTemporaryFields() {
        assertThat(underTest.clearsTemporaryFields()).isFalse();
    }
}
