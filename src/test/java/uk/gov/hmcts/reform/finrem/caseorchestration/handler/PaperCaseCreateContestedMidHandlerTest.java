package uk.gov.hmcts.reform.finrem.caseorchestration.handler;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockedStatic;
import org.mockito.junit.jupiter.MockitoExtension;
import uk.gov.hmcts.reform.finrem.caseorchestration.FinremCallbackRequestFactory;
import uk.gov.hmcts.reform.finrem.caseorchestration.controllers.GenericAboutToStartOrSubmitCallbackResponse;
import uk.gov.hmcts.reform.finrem.caseorchestration.helper.ContactDetailsValidator;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.FinremCaseData;
import uk.gov.hmcts.reform.finrem.caseorchestration.service.InternationalPostalService;
import uk.gov.hmcts.reform.finrem.caseorchestration.service.express.ExpressCaseService;

import java.util.ArrayList;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.mockStatic;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static uk.gov.hmcts.reform.finrem.caseorchestration.TestConstants.AUTH_TOKEN;
import static uk.gov.hmcts.reform.finrem.caseorchestration.TestConstants.CASE_ID;
import static uk.gov.hmcts.reform.finrem.caseorchestration.ccd.callback.CallbackType.MID_EVENT;
import static uk.gov.hmcts.reform.finrem.caseorchestration.model.EventType.NEW_PAPER_CASE;
import static uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.CaseType.CONTESTED;
import static uk.gov.hmcts.reform.finrem.caseorchestration.test.Assertions.assertCanHandle;

@ExtendWith(MockitoExtension.class)
class PaperCaseCreateContestedMidHandlerTest {

    @InjectMocks
    private PaperCaseCreateContestedMidHandler handler;

    @Mock
    private ExpressCaseService expressCaseService;

    @Mock
    private InternationalPostalService internationalPostalService;

    @Test
    void testCanHandle() {
        assertCanHandle(handler, MID_EVENT, CONTESTED, NEW_PAPER_CASE);
    }

    @Test
    void testHandle() {

        FinremCaseData caseData = mock(FinremCaseData.class);
        FinremCallbackRequest callbackRequest =
            FinremCallbackRequestFactory.from(Long.valueOf(CASE_ID), CONTESTED, NEW_PAPER_CASE, caseData);

        // Mock static methods
        try (MockedStatic<ContactDetailsValidator> contactValidatorMock = mockStatic(ContactDetailsValidator.class)) {
            contactValidatorMock.when(() -> ContactDetailsValidator.validateCaseDataAddresses(caseData))
                .thenReturn(new ArrayList<>(List.of("address error")));
            contactValidatorMock.when(() -> ContactDetailsValidator.validateCaseDataEmailAddresses(caseData))
                .thenReturn(new ArrayList<>(List.of("email error")));

            when(internationalPostalService.validate(caseData)).thenReturn(new ArrayList<>(List.of("postal address error")));

            GenericAboutToStartOrSubmitCallbackResponse<FinremCaseData> response =
                handler.handle(callbackRequest, AUTH_TOKEN);

            assertThat(response.getErrors()).containsExactly("address error", "email error", "postal address error");

            verify(internationalPostalService).validate(caseData);
            verify(expressCaseService).setExpressCaseEnrollmentStatus(caseData);
        }
    }
}
