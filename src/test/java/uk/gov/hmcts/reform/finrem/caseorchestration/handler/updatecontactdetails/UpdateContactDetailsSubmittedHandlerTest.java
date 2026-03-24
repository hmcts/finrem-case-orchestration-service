package uk.gov.hmcts.reform.finrem.caseorchestration.handler.updatecontactdetails;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import uk.gov.hmcts.reform.finrem.caseorchestration.FinremCallbackRequestFactory;
import uk.gov.hmcts.reform.finrem.caseorchestration.controllers.GenericAboutToStartOrSubmitCallbackResponse;
import uk.gov.hmcts.reform.finrem.caseorchestration.handler.FinremCallbackRequest;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.FinremCaseData;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.YesOrNo;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.wrapper.ContactDetailsWrapper;
import uk.gov.hmcts.reform.finrem.caseorchestration.service.SolicitorAccessService;
import uk.gov.hmcts.reform.finrem.caseorchestration.ccd.callback.CallbackType;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.EventType;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.CaseType;
import uk.gov.hmcts.reform.finrem.caseorchestration.service.UpdateContactDetailsNotificationService;
import uk.gov.hmcts.reform.finrem.caseorchestration.utils.retry.RetryExecutor;
import java.util.stream.Stream;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.verify;
import static uk.gov.hmcts.reform.finrem.caseorchestration.TestConstants.AUTH_TOKEN;
import static uk.gov.hmcts.reform.finrem.caseorchestration.TestConstants.CASE_ID;
import static uk.gov.hmcts.reform.finrem.caseorchestration.ccd.callback.CallbackType.SUBMITTED;
import static uk.gov.hmcts.reform.finrem.caseorchestration.model.EventType.UPDATE_CONTACT_DETAILS;
import static uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.CaseType.CONTESTED;
import static uk.gov.hmcts.reform.finrem.caseorchestration.test.Assertions.assertCanHandle;

@ExtendWith(MockitoExtension.class)
class UpdateContactDetailsSubmittedHandlerTest {

    @Mock
    private SolicitorAccessService solicitorAccessService;
    @Mock
    private UpdateContactDetailsNotificationService updateContactDetailsNotificationService;
    @Mock
    private RetryExecutor retryExecutor;
    @InjectMocks
    private UpdateContactDetailsSubmittedHandler handler;

    @Test
    void testCanHandle() {
        assertCanHandle(handler,
            Arguments.of(CallbackType.SUBMITTED, CaseType.CONSENTED, EventType.UPDATE_CONTACT_DETAILS),
            Arguments.of(CallbackType.SUBMITTED, CaseType.CONTESTED, EventType.UPDATE_CONTACT_DETAILS)
        );
    }

    static Stream<Arguments> solicitorEmailChangeScenarios() {
        return Stream.of(
            org.junit.jupiter.params.provider.Arguments.of("new@email.com", YesOrNo.YES, "old@email.com", YesOrNo.YES),
            org.junit.jupiter.params.provider.Arguments.of("same@email.com", YesOrNo.YES, "same@email.com", YesOrNo.YES),
            org.junit.jupiter.params.provider.Arguments.of("new@email.com", YesOrNo.YES, "", YesOrNo.NO),
            org.junit.jupiter.params.provider.Arguments.of("", YesOrNo.NO, "old@email.com", YesOrNo.YES)
        );
    }

    @ParameterizedTest
    @MethodSource("solicitorEmailChangeScenarios")
    void handleSolicitorEmailChangeScenarios(String applicantSolicitorEmail, YesOrNo applicantRepresented,
                                             String beforeApplicantSolicitorEmail, YesOrNo beforeApplicantRepresented) {
        FinremCaseData caseData = FinremCaseData.builder().contactDetailsWrapper(
            ContactDetailsWrapper.builder()
                .applicantSolicitorEmail(applicantSolicitorEmail)
                .applicantRepresented(applicantRepresented)
                .build()).build();

        FinremCaseData caseDataBefore = FinremCaseData.builder().contactDetailsWrapper(
            ContactDetailsWrapper.builder()
                .applicantSolicitorEmail(beforeApplicantSolicitorEmail)
                .applicantRepresented(beforeApplicantRepresented)
                .build()).build();

        FinremCallbackRequest callbackRequest =
            FinremCallbackRequestFactory.from(Long.valueOf(CASE_ID), CONTESTED, UPDATE_CONTACT_DETAILS, caseData, caseDataBefore);

        try {
            final GenericAboutToStartOrSubmitCallbackResponse<FinremCaseData> response = handler.handle(callbackRequest, AUTH_TOKEN);
            verify(solicitorAccessService).sendNoticeOfChangeNotificationsCaseworker(callbackRequest, AUTH_TOKEN);
            assertThat(response.getErrors()).isEmpty();
        } catch (Exception e) {
            throw new RuntimeException("Test failed due to exception: " + e.getMessage(), e);
        }
    }
}
