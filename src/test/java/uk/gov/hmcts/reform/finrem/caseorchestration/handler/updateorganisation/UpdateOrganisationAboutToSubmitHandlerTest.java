package uk.gov.hmcts.reform.finrem.caseorchestration.handler.updateorganisation;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import uk.gov.hmcts.reform.finrem.caseorchestration.FinremCallbackRequestFactory;
import uk.gov.hmcts.reform.finrem.caseorchestration.ccd.callback.CallbackType;
import uk.gov.hmcts.reform.finrem.caseorchestration.handler.FinremCallbackRequest;
import uk.gov.hmcts.reform.finrem.caseorchestration.handler.solicitorcreatecase.mandatorydatavalidation.ApplicantSolicitorDetailsValidator;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.EventType;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.CaseType;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.FinremCaseData;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.when;
import static uk.gov.hmcts.reform.finrem.caseorchestration.TestConstants.AUTH_TOKEN;
import static uk.gov.hmcts.reform.finrem.caseorchestration.test.Assertions.assertCanHandle;

@ExtendWith(MockitoExtension.class)
class UpdateOrganisationAboutToSubmitHandlerTest {

    @InjectMocks
    private UpdateOrganisationAboutToSubmitHandler handler;

    @Mock
    private ApplicantSolicitorDetailsValidator applicantSolicitorDetailsValidator;

    @Test
    void canHandle() {
        assertCanHandle(handler, CallbackType.ABOUT_TO_SUBMIT, CaseType.CONSENTED, EventType.UPDATE_ORGANISATION);
    }

    @Test
    void testHandle() {
        FinremCaseData finremCaseData = FinremCaseData.builder().build();
        FinremCallbackRequest request = FinremCallbackRequestFactory.from(finremCaseData);

        when(applicantSolicitorDetailsValidator.validate(finremCaseData)).thenReturn(List.of("ERROR"));

        var response = handler.handle(request, AUTH_TOKEN);
        assertThat(response.getErrors()).containsExactly("ERROR");
    }
}
