package uk.gov.hmcts.reform.finrem.caseorchestration.handler.updateorganisation;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.MockedStatic;
import org.mockito.junit.jupiter.MockitoExtension;
import uk.gov.hmcts.reform.finrem.caseorchestration.ccd.callback.CallbackType;
import uk.gov.hmcts.reform.finrem.caseorchestration.controllers.GenericAboutToStartOrSubmitCallbackResponse;
import uk.gov.hmcts.reform.finrem.caseorchestration.handler.FinremCallbackRequest;
import uk.gov.hmcts.reform.finrem.caseorchestration.helper.ContactDetailsValidator;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.EventType;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.CaseType;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.FinremCaseData;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.FinremCaseDetails;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.mockStatic;
import static org.mockito.Mockito.spy;
import static org.mockito.Mockito.when;
import static uk.gov.hmcts.reform.finrem.caseorchestration.TestConstants.AUTH_TOKEN;
import static uk.gov.hmcts.reform.finrem.caseorchestration.test.Assertions.assertCanHandle;

@ExtendWith(MockitoExtension.class)
class UpdateOrganisationAboutToSubmitHandlerTest {

    @InjectMocks
    private UpdateOrganisationAboutToSubmitHandler handler;

    @Test
    void testCanHandle() {
        assertCanHandle(handler, CallbackType.ABOUT_TO_SUBMIT, CaseType.CONSENTED, EventType.UPDATE_ORGANISATION);
    }

    @Test
    void givenInvalidOrganisationPolicy_whenHandle_thenReturnsValidationError() {
        FinremCallbackRequest callbackRequest = mock(FinremCallbackRequest.class);
        FinremCaseDetails caseDetails = mock(FinremCaseDetails.class);
        when(callbackRequest.getCaseDetails()).thenReturn(caseDetails);
        FinremCaseData finremCaseData = spy(FinremCaseData.class);
        when(caseDetails.getData()).thenReturn(finremCaseData);

        try (MockedStatic<ContactDetailsValidator> mockedStatic = mockStatic(ContactDetailsValidator.class)) {
            mockedStatic.when(() -> ContactDetailsValidator.validateOrganisationPolicy(finremCaseData))
                .thenReturn(List.of("VALIDATION FAILED"));

            GenericAboutToStartOrSubmitCallbackResponse<FinremCaseData> response = handler.handle(callbackRequest, AUTH_TOKEN);
            mockedStatic.verify(() -> ContactDetailsValidator.validateOrganisationPolicy(finremCaseData));
            assertThat(response.getErrors()).containsExactly("VALIDATION FAILED");
        }
    }
}
