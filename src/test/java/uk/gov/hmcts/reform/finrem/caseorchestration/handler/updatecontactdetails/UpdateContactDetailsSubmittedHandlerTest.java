package uk.gov.hmcts.reform.finrem.caseorchestration.handler.updatecontactdetails;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.jupiter.params.provider.Arguments;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Spy;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.context.ApplicationEventPublisher;
import uk.gov.hmcts.reform.finrem.caseorchestration.FinremCallbackRequestFactory;
import uk.gov.hmcts.reform.finrem.caseorchestration.ccd.callback.CallbackType;
import uk.gov.hmcts.reform.finrem.caseorchestration.controllers.GenericAboutToStartOrSubmitCallbackResponse;
import uk.gov.hmcts.reform.finrem.caseorchestration.handler.FinremCallbackRequest;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.FinremCaseData;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.FinremCaseDetails;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.YesOrNo;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.wrapper.ContactDetailsWrapper;
import uk.gov.hmcts.reform.finrem.caseorchestration.notifications.notifiers.SendCorrespondenceEvent;
import uk.gov.hmcts.reform.finrem.caseorchestration.service.SolicitorAccessService;
import uk.gov.hmcts.reform.finrem.caseorchestration.service.UpdateContactDetailsNotificationService;
import uk.gov.hmcts.reform.finrem.caseorchestration.utils.retry.RetryErrorHandler;
import uk.gov.hmcts.reform.finrem.caseorchestration.utils.retry.RetryExecutor;
import uk.gov.hmcts.reform.finrem.caseorchestration.utils.retry.ThrowingRunnable;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertAll;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.argThat;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.doAnswer;
import static org.mockito.Mockito.doNothing;
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
import static uk.gov.hmcts.reform.finrem.caseorchestration.TestSetUpUtils.mockRunWithRetryWithHandlerInvokesFirstErrorHandler;
import static uk.gov.hmcts.reform.finrem.caseorchestration.model.EventType.UPDATE_CONTACT_DETAILS;
import static uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.CaseType.CONSENTED;
import static uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.CaseType.CONTESTED;
import static uk.gov.hmcts.reform.finrem.caseorchestration.test.Assertions.assertCanHandle;

@ExtendWith(MockitoExtension.class)
class UpdateContactDetailsSubmittedHandlerTest {

    @Mock
    private UpdateContactDetailsNotificationService updateContactDetailsNotificationService;
    @Mock
    private SolicitorAccessService solicitorAccessService;
    @Mock
    private RetryExecutor retryExecutor;
    @Mock
    private ApplicationEventPublisher applicationEventPublisher;
    @Spy
    @InjectMocks
    private UpdateContactDetailsSubmittedHandler handler;

    @Test
    void testCanHandle() {
        assertCanHandle(handler,
            Arguments.of(CallbackType.SUBMITTED, CONSENTED, UPDATE_CONTACT_DETAILS),
            Arguments.of(CallbackType.SUBMITTED, CONTESTED, UPDATE_CONTACT_DETAILS)
        );
    }

    @Test
    void givenUpdateContactDetails_WhenApplicantSolicitorEmailChangeThenCheckAndAssignSolicitorToCase() {
        FinremCaseData caseData = FinremCaseData.builder().contactDetailsWrapper(
            ContactDetailsWrapper.builder()
                .applicantSolicitorEmail("new@email.com")
                .applicantRepresented(YesOrNo.YES)
                .build()).build();

        FinremCaseData caseDataBefore = FinremCaseData.builder().contactDetailsWrapper(
            ContactDetailsWrapper.builder()
                .applicantSolicitorEmail("old@email.com")
                .applicantRepresented(YesOrNo.YES)
                .build()).build();

        FinremCallbackRequest callbackRequest =
            FinremCallbackRequestFactory.from(Long.valueOf(CASE_ID), caseData, caseDataBefore);

        // Act
        GenericAboutToStartOrSubmitCallbackResponse<FinremCaseData> response = handler.handle(callbackRequest, AUTH_TOKEN);

        // Verify
        ArgumentCaptor<ThrowingRunnable> checkAndAssignSolicitorAccessCaptor = getThrowingRunnableCaptor();

        // Verify
        assertAll(
            () -> assertThat(response.getConfirmationBody()).isNull(),
            () -> assertThat(response.getConfirmationHeader()).isNull(),
            () -> verify(retryExecutor).runWithRetryWithHandler(
                checkAndAssignSolicitorAccessCaptor.capture(),
                eq("Update Contact Details - Case Solicitor Change"),
                eq(CASE_ID),
                any(RetryErrorHandler.class)),
            () -> {
                checkAndAssignSolicitorAccessCaptor.getValue().run();
                verify(solicitorAccessService).checkAndAssignSolicitorAccess(any(FinremCaseData.class), any(FinremCaseData.class));
            }
        );
    }

    @Test
    void givenUpdateContactDetails_WhenRespondentSolicitorEmailChangeThenCheckAndAssignSolicitorToContestedCase() {
        FinremCaseData caseData = FinremCaseData.builder().contactDetailsWrapper(
            ContactDetailsWrapper.builder()
                .respondentSolicitorEmail("new@email.com")
                .contestedRespondentRepresented(YesOrNo.YES)
                .build()).build();

        FinremCaseData caseDataBefore = FinremCaseData.builder().contactDetailsWrapper(
            ContactDetailsWrapper.builder()
                .respondentSolicitorEmail("old@email.com")
                .contestedRespondentRepresented(YesOrNo.YES)
                .build()).build();

        FinremCallbackRequest callbackRequest =
            FinremCallbackRequestFactory.from(Long.valueOf(CASE_ID), caseData, caseDataBefore);

        // Act
        GenericAboutToStartOrSubmitCallbackResponse<FinremCaseData> response = handler.handle(callbackRequest, AUTH_TOKEN);

        // Verify
        ArgumentCaptor<ThrowingRunnable> checkAndAssignSolicitorAccessCaptor = getThrowingRunnableCaptor();

        // Verify
        assertAll(
            () -> assertThat(response.getConfirmationBody()).isNull(),
            () -> assertThat(response.getConfirmationHeader()).isNull(),
            () -> verify(retryExecutor).runWithRetryWithHandler(
                checkAndAssignSolicitorAccessCaptor.capture(),
                eq("Update Contact Details - Case Solicitor Change"),
                eq(CASE_ID),
                any(RetryErrorHandler.class)),
            () -> {
                checkAndAssignSolicitorAccessCaptor.getValue().run();
                verify(solicitorAccessService).checkAndAssignSolicitorAccess(any(FinremCaseData.class), any(FinremCaseData.class));
            }
        );
    }

    @Test
    void givenUpdateContactDetails_WhenRespondentSolicitorEmailChangeThenCheckAndAssignSolicitorToConsentedCase() {
        FinremCaseData caseData = FinremCaseData.builder().contactDetailsWrapper(
            ContactDetailsWrapper.builder()
                .respondentSolicitorEmail("new@email.com")
                .consentedRespondentRepresented(YesOrNo.YES)
                .build()).build();

        FinremCaseData caseDataBefore = FinremCaseData.builder().contactDetailsWrapper(
            ContactDetailsWrapper.builder()
                .respondentSolicitorEmail("old@email.com")
                .consentedRespondentRepresented(YesOrNo.YES)
                .build()).build();

        FinremCallbackRequest callbackRequest =
            FinremCallbackRequestFactory.from(Long.valueOf(CASE_ID), caseData, caseDataBefore);

        // Act
        GenericAboutToStartOrSubmitCallbackResponse<FinremCaseData> response = handler.handle(callbackRequest, AUTH_TOKEN);

        // Verify
        ArgumentCaptor<ThrowingRunnable> checkAndAssignSolicitorAccessCaptor = getThrowingRunnableCaptor();

        // Verify
        assertAll(
            () -> assertThat(response.getConfirmationBody()).isNull(),
            () -> assertThat(response.getConfirmationHeader()).isNull(),
            () -> verify(retryExecutor).runWithRetryWithHandler(
                checkAndAssignSolicitorAccessCaptor.capture(),
                eq("Update Contact Details - Case Solicitor Change"),
                eq(CASE_ID),
                any(RetryErrorHandler.class)),
            () -> {
                checkAndAssignSolicitorAccessCaptor.getValue().run();
                verify(solicitorAccessService).checkAndAssignSolicitorAccess(any(FinremCaseData.class), any(FinremCaseData.class));
            }
        );
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
            () -> verify(retryExecutor).runWithRetryWithHandler(
                nocEmailToSolicitorsCaptor.capture(),
                eq("Sending NOC email to litigant solicitor"),
                eq(CASE_ID),
                any(RetryErrorHandler.class)),
            () -> {
                nocEmailToSolicitorsCaptor.getValue().run();
                verify(applicationEventPublisher).publishEvent(event);
            },
            () -> verify(retryExecutor).runWithRetryWithHandler(
                nocEmailToSolicitorsCaptor.capture(),
                eq("Sending NOC letter"),
                eq(CASE_ID),
                any(RetryErrorHandler.class)),
            () -> {
                nocEmailToSolicitorsCaptor.getValue().run();
                verify(updateContactDetailsNotificationService).sendNocLetterToLitigants(callbackRequest.getCaseDetails(),
                    callbackRequest.getCaseDetailsBefore(), AUTH_TOKEN);
            },
            () -> verifyNoMoreInteractions(applicationEventPublisher)
        );
    }

    @Test
    void givenMultipleExceptionsThrow_whenHandled_thenPopulateErrorToConfirmationBody() {
        FinremCaseData finremCaseData = spy(FinremCaseData.builder().build());
        FinremCaseData finremCaseDataBefore = spy(FinremCaseData.builder().build());
        FinremCallbackRequest callbackRequest = FinremCallbackRequestFactory.from(CASE_ID_IN_LONG, finremCaseDataBefore,
            finremCaseData);
        SendCorrespondenceEvent event = mock(SendCorrespondenceEvent.class);

        when(updateContactDetailsNotificationService.requiresNotifications(finremCaseData)).thenReturn(true);
        when(updateContactDetailsNotificationService.prepareNocEmailToLitigantSolicitor(callbackRequest.getCaseDetails()))
            .thenReturn(event);

        mockRunWithRetryWithHandlerInvokesFirstErrorHandler(
            retryExecutor,
            "Sending NOC email to litigant solicitor"
        );
        mockRunWithRetryWithHandlerInvokesFirstErrorHandler(
            retryExecutor,
            "Sending NOC letter"
        );
        doAnswer(invocation -> null).when(retryExecutor).runWithRetryWithHandler(
            any(), eq("Update Contact Details - Case Solicitor Change"), any(), any()
        );

        // Act
        GenericAboutToStartOrSubmitCallbackResponse<FinremCaseData> response = handler.handle(callbackRequest, AUTH_TOKEN);

        assertAll(
            () -> assertThat(response.getConfirmationHeader()).contains("Contact details updated with Errors"),
            () -> assertThat(response.getConfirmationBody())
                .contains(
                    "Fail to send notice of change email to litigant solicitor.",
                    "Fail to send NOC letter to litigants."),
            () -> verify(updateContactDetailsNotificationService).requiresNotifications(finremCaseData),
            () -> verify(updateContactDetailsNotificationService).prepareNocEmailToLitigantSolicitor(callbackRequest.getCaseDetails())
        );
    }

    @Test
    void givenExceptionThrown_whenSendingNocEmail_thenPopulateErrorToConfirmationBody() {
        FinremCaseData finremCaseData = spy(FinremCaseData.builder().build());
        FinremCaseData finremCaseDataBefore = spy(FinremCaseData.builder().build());
        FinremCallbackRequest callbackRequest = FinremCallbackRequestFactory.from(CASE_ID_IN_LONG, finremCaseDataBefore,
            finremCaseData);
        SendCorrespondenceEvent event = mock(SendCorrespondenceEvent.class);

        when(updateContactDetailsNotificationService.requiresNotifications(finremCaseData)).thenReturn(true);
        when(updateContactDetailsNotificationService.prepareNocEmailToLitigantSolicitor(callbackRequest.getCaseDetails()))
            .thenReturn(event);

        mockRunWithRetryWithHandlerInvokesFirstErrorHandler(
            retryExecutor,
            "Sending NOC email to litigant solicitor"
        );
        doAnswer(invocation -> {
            // Nothing happened
            return null;
        }).when(retryExecutor).runWithRetryWithHandler(any(), eq("Sending NOC letter"), any(), any());
        doAnswer(invocation -> null).when(retryExecutor).runWithRetryWithHandler(
            any(), eq("Update Contact Details - Case Solicitor Change"), any(), any()
        );

        // Act
        GenericAboutToStartOrSubmitCallbackResponse<FinremCaseData> response = handler.handle(callbackRequest, AUTH_TOKEN);

        // then
        assertAll(
            () -> assertThat(response.getConfirmationHeader()).contains("Contact details updated with Errors"),
            () -> assertThat(response.getConfirmationBody())
                .contains("Fail to send notice of change email to litigant solicitor.")
                .doesNotContain("Fail to send NOC letter to litigants."),
            () -> verify(updateContactDetailsNotificationService).requiresNotifications(finremCaseData),
            () -> verify(updateContactDetailsNotificationService).prepareNocEmailToLitigantSolicitor(callbackRequest.getCaseDetails())
        );
    }

    @Test
    void givenExceptionThrown_whenSendingNocLetter_thenPopulateErrorToConfirmationBody() {
        FinremCaseData finremCaseData = spy(FinremCaseData.builder().build());
        FinremCaseData finremCaseDataBefore = spy(FinremCaseData.builder().build());
        FinremCallbackRequest callbackRequest = FinremCallbackRequestFactory.from(CASE_ID_IN_LONG, finremCaseDataBefore,
            finremCaseData);
        SendCorrespondenceEvent event = mock(SendCorrespondenceEvent.class);

        when(updateContactDetailsNotificationService.requiresNotifications(finremCaseData)).thenReturn(true);
        when(updateContactDetailsNotificationService.prepareNocEmailToLitigantSolicitor(callbackRequest.getCaseDetails()))
            .thenReturn(event);

        // Lenient stub for solicitor access retry
        doNothing().when(retryExecutor).runWithRetryWithHandler(any(), eq("Update Contact Details - Case Solicitor Change"), any(), any());

        // Simulate retryExecutor calling the handler with an exception
        doAnswer(invocation -> {
            // Nothing happened
            return null;
        }).when(retryExecutor).runWithRetryWithHandler(any(), eq("Sending NOC email to litigant solicitor"), any(), any());
        mockRunWithRetryWithHandlerInvokesFirstErrorHandler(
            retryExecutor,
            "Sending NOC letter"
        );

        // Act
        GenericAboutToStartOrSubmitCallbackResponse<FinremCaseData> response = handler.handle(callbackRequest, AUTH_TOKEN);

        // then
        assertAll(
            () -> assertThat(response.getConfirmationHeader()).contains("Contact details updated with Errors"),
            () -> assertThat(response.getConfirmationBody())
                .contains("Fail to send NOC letter to litigants.")
                .doesNotContain("Fail to send notice of change email to litigant solicitor."),
            () -> verify(updateContactDetailsNotificationService).requiresNotifications(finremCaseData),
            () -> verify(updateContactDetailsNotificationService).prepareNocEmailToLitigantSolicitor(callbackRequest.getCaseDetails())
        );
    }

    @Test
    void shouldReturnErrorConfirmationWhenCheckAndAssignSolicitorAccessFails() {
        // Arrange
        FinremCaseData finremCaseData = FinremCaseData.builder().build();
        FinremCaseData finremCaseDataBefore = FinremCaseData.builder().build();
        FinremCallbackRequest callbackRequest = FinremCallbackRequestFactory.from(CASE_ID_IN_LONG, finremCaseDataBefore, finremCaseData);

        // Simulate error in checkAndAssignSolicitorAccess by making retryExecutor set the error
        doAnswer(invocation -> {
            // No cast needed, just get the error handler and call it
            String actionName = invocation.getArgument(1);
            String caseId = invocation.getArgument(2);
            RetryErrorHandler errorHandler = invocation.getArgument(3);
            errorHandler.handle(new RuntimeException("fail"), actionName, caseId);
            return null;
        }).when(retryExecutor).runWithRetryWithHandler(any(), eq("Update Contact Details - Case Solicitor Change"), any(), any());

        // Act
        GenericAboutToStartOrSubmitCallbackResponse<FinremCaseData> response = handler.handle(callbackRequest, AUTH_TOKEN);

        // Assert
        assertAll(
            () -> assertThat(response.getConfirmationHeader()).contains("Contact details updated with Errors"),
            () -> assertThat(response.getConfirmationBody())
                .contains("There was a problem updating solicitor access to case.")
                .doesNotContain("Fail to send notice of change email to litigant solicitor.")
        );
    }
}
