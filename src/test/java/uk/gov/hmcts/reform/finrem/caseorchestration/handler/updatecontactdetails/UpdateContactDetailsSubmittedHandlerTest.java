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
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.context.ApplicationEventPublisher;
import uk.gov.hmcts.reform.finrem.caseorchestration.FinremCallbackRequestFactory;
import uk.gov.hmcts.reform.finrem.caseorchestration.ccd.callback.CallbackType;
import uk.gov.hmcts.reform.finrem.caseorchestration.controllers.GenericAboutToStartOrSubmitCallbackResponse;
import uk.gov.hmcts.reform.finrem.caseorchestration.handler.FinremCallbackRequest;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.EventType;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.CaseType;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.FinremCaseData;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.FinremCaseDetails;
import uk.gov.hmcts.reform.finrem.caseorchestration.notifications.notifiers.SendCorrespondenceEvent;
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
import uk.gov.hmcts.reform.finrem.caseorchestration.utils.retry.ThrowingRunnable;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertAll;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.argThat;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.spy;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoMoreInteractions;
import static org.mockito.Mockito.when;
import static uk.gov.hmcts.reform.finrem.caseorchestration.TestConstants.AUTH_TOKEN;
import static uk.gov.hmcts.reform.finrem.caseorchestration.TestConstants.CASE_ID;
import static uk.gov.hmcts.reform.finrem.caseorchestration.TestConstants.CASE_ID_IN_LONG;
import static uk.gov.hmcts.reform.finrem.caseorchestration.TestSetUpUtils.getThrowingRunnableCaptor;
import static uk.gov.hmcts.reform.finrem.caseorchestration.test.Assertions.assertCanHandle;

@ExtendWith(MockitoExtension.class)
class UpdateContactDetailsSubmittedHandlerTest {

    @Mock
    private SolicitorAccessService solicitorAccessService;
    @Mock
    private UpdateContactDetailsNotificationService updateContactDetailsNotificationService;
    @Mock
    private RetryExecutor retryExecutor;
    @Mock
    private ApplicationEventPublisher applicationEventPublisher;
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
    @Test
    void givenNotificationNotRequired_whenHandled_thenNoNotificationIsSent() {
        FinremCaseData finremCaseData = spy(FinremCaseData.builder().build());

        when(updateContactDetailsNotificationService.requiresNotifications(finremCaseData)).thenReturn(false);

        // Act
        GenericAboutToStartOrSubmitCallbackResponse<FinremCaseData> response
            = handler.handle(FinremCallbackRequestFactory.from(CASE_ID_IN_LONG, finremCaseData), AUTH_TOKEN);

        // Verify
        assertAll(
            () -> assertThat(response.getConfirmationBody()).isNull(),
            () -> assertThat(response.getConfirmationHeader()).isNull(),
            () -> verify(updateContactDetailsNotificationService).requiresNotifications(finremCaseData),
            () -> verify(updateContactDetailsNotificationService, never()).prepareNocEmailToLitigantSolicitor(any(FinremCaseDetails.class)),
            () -> verify(retryExecutor, never()).runWithRetryWithHandler(
                any(ThrowingRunnable.class),
                argThat(a -> List.of(
                    "Sending NOC email to litigant solicitor",
                    "Sending NOC letter"
                ).contains(a)),
                anyString(),
                any())
        );
    }

    @Test
    void givenNotificationRequired_whenHandled_thenNotificationIsSent() {
        FinremCaseData finremCaseData = spy(FinremCaseData.builder().build());
        FinremCaseData finremCaseDataBefore = spy(FinremCaseData.builder().build());
        FinremCallbackRequest callbackRequest = FinremCallbackRequestFactory.from(CASE_ID_IN_LONG, finremCaseDataBefore,
            finremCaseData);
        SendCorrespondenceEvent event = mock(SendCorrespondenceEvent.class);
        when(event.getCaseId()).thenReturn(CASE_ID);

        when(updateContactDetailsNotificationService.requiresNotifications(finremCaseData)).thenReturn(true);
        when(updateContactDetailsNotificationService.prepareNocEmailToLitigantSolicitor(callbackRequest.getCaseDetails()))
            .thenReturn(event);

        // Act
        GenericAboutToStartOrSubmitCallbackResponse<FinremCaseData> response = handler.handle(callbackRequest, AUTH_TOKEN);

        // Verify
        ArgumentCaptor<ThrowingRunnable> nocEmailToSolicitorsCaptor = getThrowingRunnableCaptor();

        assertAll(
            () -> assertThat(response.getConfirmationBody()).isNull(),
            () -> assertThat(response.getConfirmationHeader()).isNull(),
            () -> verify(updateContactDetailsNotificationService).requiresNotifications(finremCaseData),
            () -> verify(updateContactDetailsNotificationService).prepareNocEmailToLitigantSolicitor(callbackRequest.getCaseDetails()),
            () -> {
                verify(retryExecutor)
                    .runWithRetryWithHandler(
                        nocEmailToSolicitorsCaptor.capture(),
                        eq("Sending NOC email to litigant solicitor"),
                        eq(CASE_ID),
                        any());
                nocEmailToSolicitorsCaptor.getValue().run();
                verify(applicationEventPublisher).publishEvent(event);
            },
            () -> {
                verify(retryExecutor)
                    .runWithRetryWithHandler(
                        nocEmailToSolicitorsCaptor.capture(),
                        eq("Sending NOC letter"),
                        eq(CASE_ID),
                        any());
                nocEmailToSolicitorsCaptor.getValue().run();
                verify(updateContactDetailsNotificationService).sendNocLetterToLitigants(callbackRequest.getCaseDetails(),
                    callbackRequest.getCaseDetailsBefore(), AUTH_TOKEN);
            },
            () -> verifyNoMoreInteractions(retryExecutor)
        );
    }
}
