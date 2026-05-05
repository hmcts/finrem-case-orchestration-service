package uk.gov.hmcts.reform.finrem.caseorchestration.handler.updatecontactdetails;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;
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
import uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.Address;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.FinremCaseData;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.FinremCaseDetails;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.State;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.YesOrNo;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.wrapper.ContactDetailsWrapper;
import uk.gov.hmcts.reform.finrem.caseorchestration.notifications.notifiers.SendCorrespondenceEvent;
import uk.gov.hmcts.reform.finrem.caseorchestration.service.UpdateContactDetailsNotificationService;
import uk.gov.hmcts.reform.finrem.caseorchestration.utils.retry.RetryErrorHandler;
import uk.gov.hmcts.reform.finrem.caseorchestration.utils.retry.RetryExecutor;
import uk.gov.hmcts.reform.finrem.caseorchestration.utils.retry.ThrowingRunnable;

import java.lang.reflect.Method;
import java.time.LocalDate;
import java.util.List;
import java.util.Optional;
import java.util.stream.Stream;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertAll;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.argThat;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.doAnswer;
import static org.mockito.Mockito.lenient;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
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
        ContactDetailsWrapper beforeWrapper = ContactDetailsWrapper.builder()
            .applicantSolicitorEmail("old@email.com")
            .applicantRepresented(YesOrNo.YES)
            .build();

        ContactDetailsWrapper afterWrapper = ContactDetailsWrapper.builder()
            .applicantSolicitorEmail("new@email.com")
            .applicantRepresented(YesOrNo.YES)
            .build();

        FinremCaseDetails beforeDetails = buildCaseDetails(beforeWrapper);
        FinremCaseDetails afterDetails = buildCaseDetails(afterWrapper);

        FinremCallbackRequest callbackRequest =
            FinremCallbackRequestFactory.from(Long.valueOf(CASE_ID), beforeDetails.getData(), afterDetails.getData());

        lenient().when(retryExecutor.supplyWithRetryWithHandler(any(), anyString(), anyString(), any()))
            .thenReturn(java.util.Optional.of(true));
        lenient().when(updateContactDetailsNotificationService.prepareNocEmailToLitigantSolicitor(any()))
            .thenReturn(mock(SendCorrespondenceEvent.class));

        // Act
        GenericAboutToStartOrSubmitCallbackResponse<FinremCaseData> response = handler.handle(callbackRequest, AUTH_TOKEN);

        // Verify
        assertAll(
            () -> assertThat(response.getConfirmationBody()).isNull(),
            () -> assertThat(response.getConfirmationHeader()).isNull(),
            () -> verify(retryExecutor).supplyWithRetryWithHandler(
                any(),
                eq("Update Contact Details - granting applicant solicitor"),
                eq(CASE_ID),
                any(RetryErrorHandler.class)),
            () -> verify(retryExecutor).supplyWithRetryWithHandler(
                any(),
                eq("Update Contact Details - revoking applicant solicitor"),
                eq(CASE_ID),
                any(RetryErrorHandler.class))
        );
    }

    @Test
    void givenUpdateContactDetails_WhenRespondentSolicitorEmailChangeThenCheckAndAssignSolicitorToContestedCase() {
        ContactDetailsWrapper beforeWrapper = ContactDetailsWrapper.builder()
            .respondentSolicitorEmail("old@email.com")
            .contestedRespondentRepresented(YesOrNo.YES)
            .build();

        ContactDetailsWrapper afterWrapper = ContactDetailsWrapper.builder()
            .respondentSolicitorEmail("new@email.com")
            .contestedRespondentRepresented(YesOrNo.YES)
            .build();

        FinremCaseDetails beforeDetails = buildCaseDetails(beforeWrapper);
        FinremCaseDetails afterDetails = buildCaseDetails(afterWrapper);

        FinremCallbackRequest callbackRequest =
            FinremCallbackRequestFactory.from(Long.valueOf(CASE_ID), beforeDetails.getData(), afterDetails.getData());

        lenient().when(retryExecutor.supplyWithRetryWithHandler(any(), anyString(), anyString(), any()))
            .thenReturn(java.util.Optional.of(true));
        lenient().when(updateContactDetailsNotificationService.prepareNocEmailToLitigantSolicitor(any()))
            .thenReturn(mock(SendCorrespondenceEvent.class));

        // Act
        GenericAboutToStartOrSubmitCallbackResponse<FinremCaseData> response = handler.handle(callbackRequest, AUTH_TOKEN);

        // Verify
        assertAll(
            () -> assertThat(response.getConfirmationBody()).isNull(),
            () -> assertThat(response.getConfirmationHeader()).isNull(),
            () -> verify(retryExecutor).supplyWithRetryWithHandler(
                any(),
                eq("Update Contact Details - granting respondent solicitor"),
                eq(CASE_ID),
                any(RetryErrorHandler.class)),
            () -> verify(retryExecutor).supplyWithRetryWithHandler(
                any(),
                eq("Update Contact Details - revoking respondent solicitor"),
                eq(CASE_ID),
                any(RetryErrorHandler.class))
        );
    }

    @Test
    void givenUpdateContactDetails_WhenRespondentSolicitorEmailChangeThenCheckAndAssignSolicitorToConsentedCase() {
        ContactDetailsWrapper beforeWrapper = ContactDetailsWrapper.builder()
            .respondentSolicitorEmail("new@email.com")
            .consentedRespondentRepresented(YesOrNo.YES)
            .build();

        ContactDetailsWrapper afterWrapper = ContactDetailsWrapper.builder()
            .respondentSolicitorEmail("old@email.com")
            .consentedRespondentRepresented(YesOrNo.YES)
            .build();

        FinremCaseDetails beforeDetails = buildCaseDetails(beforeWrapper);
        FinremCaseDetails afterDetails = buildCaseDetails(afterWrapper);

        FinremCallbackRequest callbackRequest =
            FinremCallbackRequestFactory.from(Long.valueOf(CASE_ID), beforeDetails.getData(), afterDetails.getData());

        lenient().when(retryExecutor.supplyWithRetryWithHandler(any(), anyString(), anyString(), any()))
            .thenReturn(java.util.Optional.of(true));
        lenient().when(updateContactDetailsNotificationService.prepareNocEmailToLitigantSolicitor(any()))
            .thenReturn(mock(SendCorrespondenceEvent.class));

        // Act
        GenericAboutToStartOrSubmitCallbackResponse<FinremCaseData> response = handler.handle(callbackRequest, AUTH_TOKEN);

        // Verify
        assertAll(
            () -> assertThat(response.getConfirmationBody()).isNull(),
            () -> assertThat(response.getConfirmationHeader()).isNull(),
            () -> verify(retryExecutor).supplyWithRetryWithHandler(
                any(),
                eq("Update Contact Details - granting respondent solicitor"),
                eq(CASE_ID),
                any(RetryErrorHandler.class)),
            () -> verify(retryExecutor).supplyWithRetryWithHandler(
                any(),
                eq("Update Contact Details - revoking respondent solicitor"),
                eq(CASE_ID),
                any(RetryErrorHandler.class))
        );
    }

    @Test
    void givenNotificationNotRequired_whenHandled_thenNoNotificationIsSent() {
        FinremCaseData finremCaseData = FinremCaseData.builder()
            .contactDetailsWrapper(ContactDetailsWrapper.builder()
                .updateIncludesRepresentativeChange(YesOrNo.NO)
                .build())
            .build();

        // Act
        GenericAboutToStartOrSubmitCallbackResponse<FinremCaseData> response
            = handler.handle(FinremCallbackRequestFactory.from(CASE_ID_IN_LONG, finremCaseData), AUTH_TOKEN);

        // Verify
        assertAll(
            () -> assertThat(response.getConfirmationBody()).isNull(),
            () -> assertThat(response.getConfirmationHeader()).isNull(),
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
        ContactDetailsWrapper wrapper = ContactDetailsWrapper.builder()
            .applicantSolicitorEmail("new@email.com")
            .applicantRepresented(YesOrNo.YES)
            .updateIncludesRepresentativeChange(YesOrNo.YES)
            .build();
        ContactDetailsWrapper wrapperBefore = ContactDetailsWrapper.builder()
            .applicantSolicitorEmail("old@email.com")
            .applicantRepresented(YesOrNo.YES)
            .build();

        FinremCaseData finremCaseData = FinremCaseData.builder()
            .contactDetailsWrapper(wrapper)
            .issueDate(LocalDate.now())
            .build();
        FinremCaseData finremCaseDataBefore = FinremCaseData.builder()
            .contactDetailsWrapper(wrapperBefore)
            .build();

        FinremCallbackRequest callbackRequest = FinremCallbackRequestFactory.from(CASE_ID_IN_LONG, finremCaseDataBefore,
            finremCaseData);
        SendCorrespondenceEvent event = mock(SendCorrespondenceEvent.class);
        when(event.getCaseId()).thenReturn(CASE_ID);

        lenient().when(retryExecutor.supplyWithRetryWithHandler(any(), anyString(), anyString(), any()))
            .thenReturn(java.util.Optional.of(true));
        lenient().when(updateContactDetailsNotificationService.prepareNocEmailToLitigantSolicitor(any()))
            .thenReturn(mock(SendCorrespondenceEvent.class));
        when(updateContactDetailsNotificationService.prepareNocEmailToLitigantSolicitor(any(FinremCaseDetails.class)))
            .thenReturn(event);

        // Act
        GenericAboutToStartOrSubmitCallbackResponse<FinremCaseData> response = handler.handle(callbackRequest, AUTH_TOKEN);

        // Verify
        ArgumentCaptor<ThrowingRunnable> nocEmailToSolicitorsCaptor = getThrowingRunnableCaptor();

        assertAll(
            () -> assertThat(response.getConfirmationBody()).isNull(),
            () -> assertThat(response.getConfirmationHeader()).isNull(),
            () -> verify(updateContactDetailsNotificationService).prepareNocEmailToLitigantSolicitor(any(FinremCaseDetails.class)),
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
                verify(updateContactDetailsNotificationService).sendNocLetterToLitigants(any(FinremCaseDetails.class),
                    any(FinremCaseDetails.class), eq(AUTH_TOKEN));
            },
            () -> verifyNoMoreInteractions(applicationEventPublisher)
        );
    }

    @Test
    void givenMultipleExceptionsThrow_whenHandled_thenPopulateErrorToConfirmationBody() {

        SendCorrespondenceEvent event = mock(SendCorrespondenceEvent.class);

        lenient().when(updateContactDetailsNotificationService.prepareNocEmailToLitigantSolicitor(any(FinremCaseDetails.class)))
            .thenReturn(event);
        lenient().when(retryExecutor.supplyWithRetryWithHandler(any(), anyString(), anyString(), any()))
            .thenReturn(java.util.Optional.of(true));
        mockRunWithRetryWithHandlerInvokesFirstErrorHandler(
            retryExecutor,
            "Sending NOC email to litigant solicitor"
        );
        mockRunWithRetryWithHandlerInvokesFirstErrorHandler(
            retryExecutor,
            "Sending NOC letter"
        );
        ContactDetailsWrapper wrapper = ContactDetailsWrapper.builder()
            .applicantSolicitorEmail("new@email.com")
            .applicantRepresented(YesOrNo.YES)
            .updateIncludesRepresentativeChange(YesOrNo.YES)
            .build();
        ContactDetailsWrapper wrapperBefore = ContactDetailsWrapper.builder()
            .applicantSolicitorEmail("old@email.com")
            .applicantRepresented(YesOrNo.YES)
            .build();
        FinremCaseData finremCaseData = FinremCaseData.builder()
            .contactDetailsWrapper(wrapper)
            .issueDate(LocalDate.now())
            .build();
        FinremCaseData finremCaseDataBefore = FinremCaseData.builder()
            .contactDetailsWrapper(wrapperBefore)
            .build();
        FinremCallbackRequest callbackRequest = FinremCallbackRequestFactory.from(CASE_ID_IN_LONG, finremCaseDataBefore, finremCaseData);

        // Act
        GenericAboutToStartOrSubmitCallbackResponse<FinremCaseData> response = handler.handle(callbackRequest, AUTH_TOKEN);

        assertAll(
            () -> assertThat(response.getConfirmationHeader()).contains("Contact details updated with errors"),
            () -> assertThat(response.getConfirmationBody())
                .contains(
                    "Fail to send notice of change email to litigant solicitor.",
                    "Fail to send NOC letter to litigants."),
            () -> verify(updateContactDetailsNotificationService).prepareNocEmailToLitigantSolicitor(any(FinremCaseDetails.class))
        );
    }

    @Test
    void givenExceptionThrown_whenSendingNocEmail_thenPopulateErrorToConfirmationBody() {

        SendCorrespondenceEvent event = mock(SendCorrespondenceEvent.class);

        lenient().when(updateContactDetailsNotificationService.prepareNocEmailToLitigantSolicitor(any(FinremCaseDetails.class)))
            .thenReturn(event);
        lenient().when(retryExecutor.supplyWithRetryWithHandler(any(), anyString(), anyString(), any()))
            .thenReturn(java.util.Optional.of(true));
        mockRunWithRetryWithHandlerInvokesFirstErrorHandler(
            retryExecutor,
            "Sending NOC email to litigant solicitor"
        );
        doAnswer(invocation -> {
            // Nothing happened
            return null;
        }).when(retryExecutor).runWithRetryWithHandler(any(), eq("Sending NOC letter"), any(), any());
        ContactDetailsWrapper wrapper = ContactDetailsWrapper.builder()
            .applicantSolicitorEmail("new@email.com")
            .applicantRepresented(YesOrNo.YES)
            .updateIncludesRepresentativeChange(YesOrNo.YES)
            .build();
        ContactDetailsWrapper wrapperBefore = ContactDetailsWrapper.builder()
            .applicantSolicitorEmail("old@email.com")
            .applicantRepresented(YesOrNo.YES)
            .build();
        FinremCaseData finremCaseData = FinremCaseData.builder()
            .contactDetailsWrapper(wrapper)
            .issueDate(LocalDate.now())
            .build();
        FinremCaseData finremCaseDataBefore = FinremCaseData.builder()
            .contactDetailsWrapper(wrapperBefore)
            .build();
        FinremCallbackRequest callbackRequest = FinremCallbackRequestFactory.from(CASE_ID_IN_LONG, finremCaseDataBefore, finremCaseData);

        // Act
        GenericAboutToStartOrSubmitCallbackResponse<FinremCaseData> response = handler.handle(callbackRequest, AUTH_TOKEN);

        // then
        assertAll(
            () -> assertThat(response.getConfirmationHeader()).contains("Contact details updated with errors"),
            () -> assertThat(response.getConfirmationBody())
                .contains("Fail to send notice of change email to litigant solicitor.")
                .doesNotContain("Fail to send NOC letter to litigants."),
            () -> verify(updateContactDetailsNotificationService).prepareNocEmailToLitigantSolicitor(any(FinremCaseDetails.class))
        );
    }

    @Test
    void givenExceptionThrown_whenSendingNocLetter_thenPopulateErrorToConfirmationBody() {

        SendCorrespondenceEvent event = mock(SendCorrespondenceEvent.class);

        lenient().when(updateContactDetailsNotificationService.prepareNocEmailToLitigantSolicitor(any(FinremCaseDetails.class)))
            .thenReturn(event);
        lenient().when(retryExecutor.supplyWithRetryWithHandler(any(), anyString(), anyString(), any()))
            .thenReturn(java.util.Optional.of(true));

        // Simulate retryExecutor calling the handler with an exception
        doAnswer(invocation -> {
            // Nothing happened
            return null;
        }).when(retryExecutor).runWithRetryWithHandler(any(), eq("Sending NOC email to litigant solicitor"), any(), any());
        mockRunWithRetryWithHandlerInvokesFirstErrorHandler(
            retryExecutor,
            "Sending NOC letter"
        );
        ContactDetailsWrapper wrapper = ContactDetailsWrapper.builder()
            .applicantSolicitorEmail("new@email.com")
            .applicantRepresented(YesOrNo.YES)
            .updateIncludesRepresentativeChange(YesOrNo.YES)
            .build();
        ContactDetailsWrapper wrapperBefore = ContactDetailsWrapper.builder()
            .applicantSolicitorEmail("old@email.com")
            .applicantRepresented(YesOrNo.YES)
            .build();
        FinremCaseData finremCaseData = FinremCaseData.builder()
            .contactDetailsWrapper(wrapper)
            .issueDate(LocalDate.now())
            .build();
        FinremCaseData finremCaseDataBefore = FinremCaseData.builder()
            .contactDetailsWrapper(wrapperBefore)
            .build();
        FinremCallbackRequest callbackRequest = FinremCallbackRequestFactory.from(CASE_ID_IN_LONG, finremCaseDataBefore, finremCaseData);

        // Act
        GenericAboutToStartOrSubmitCallbackResponse<FinremCaseData> response = handler.handle(callbackRequest, AUTH_TOKEN);

        // then
        assertAll(
            () -> assertThat(response.getConfirmationHeader()).contains("Contact details updated with errors"),
            () -> assertThat(response.getConfirmationBody())
                .contains("Fail to send NOC letter to litigants.")
                .doesNotContain("Fail to send notice of change email to litigant solicitor."),
            () -> verify(updateContactDetailsNotificationService).prepareNocEmailToLitigantSolicitor(any(FinremCaseDetails.class))
        );
    }

    @Test
    void shouldReturnErrorConfirmationWhenCheckAndAssignSolicitorAccessFails() {
        // Arrange
        FinremCaseData finremCaseData = FinremCaseData
            .builder()
            .issueDate(LocalDate.now())
            .contactDetailsWrapper(ContactDetailsWrapper.builder()
                .applicantSolicitorEmail("new@email.com")
                .applicantRepresented(YesOrNo.YES)
                .build())
            .build();
        FinremCaseData finremCaseDataBefore = FinremCaseData.builder()
            .contactDetailsWrapper(ContactDetailsWrapper.builder()
                .applicantSolicitorEmail("old@email.com")
                .applicantRepresented(YesOrNo.YES)
                .build())
            .build();
        FinremCallbackRequest callbackRequest = FinremCallbackRequestFactory.from(CASE_ID_IN_LONG, finremCaseDataBefore, finremCaseData);

        // Simulate error in checkAndAssignSolicitorAccess by making retryExecutor set the error
        when(retryExecutor.supplyWithRetryWithHandler(any(), anyString(), anyString(), any())).thenAnswer(invocation -> {
            String actionName = invocation.getArgument(1);
            String caseId = invocation.getArgument(2);
            RetryErrorHandler errorHandler = invocation.getArgument(3);
            errorHandler.handle(new RuntimeException("fail"), actionName, caseId);
            return java.util.Optional.empty();
        });

        // Act
        GenericAboutToStartOrSubmitCallbackResponse<FinremCaseData> response = handler.handle(callbackRequest, AUTH_TOKEN);

        // Assert
        assertAll(
            () -> assertThat(response.getConfirmationHeader()).contains("Contact details updated with errors"),
            () -> assertThat(response.getConfirmationBody())
                .contains("There was a problem granting access to applicant solicitor (new@email.com). Please grant access manually.")
        );
    }

    @Test
    void shouldReturnTrueWhenIssueDateIsPresent() throws Exception {
        FinremCaseData caseData = mock(FinremCaseData.class);
        when(caseData.getIssueDate()).thenReturn(LocalDate.now());

        Method method = UpdateContactDetailsSubmittedHandler.class.getDeclaredMethod("hasApplicationBeenIssued", FinremCaseData.class);
        method.setAccessible(true);

        boolean result = (boolean) method.invoke(handler, caseData);

        assertTrue(result, "Expected true when issue date is present");
    }

    @Test
    void shouldReturnFalseWhenIssueDateIsNull() throws Exception {
        FinremCaseData caseData = mock(FinremCaseData.class);
        when(caseData.getIssueDate()).thenReturn(null);

        Method method = UpdateContactDetailsSubmittedHandler.class.getDeclaredMethod("hasApplicationBeenIssued", FinremCaseData.class);
        method.setAccessible(true);

        boolean result = (boolean) method.invoke(handler, caseData);

        assertFalse(result, "Expected false when issue date is null");
    }

    @Test
    void givenApplicantSolicitorEmailChangedAndApplicationNotIssued_thenCheckAndAssignSolicitorAccess() {
        // Arrange
        ContactDetailsWrapper beforeWrapper = ContactDetailsWrapper.builder()
            .applicantSolicitorEmail("old@email.com")
            .applicantRepresented(YesOrNo.YES)
            .build();
        ContactDetailsWrapper afterWrapper = ContactDetailsWrapper.builder()
            .applicantSolicitorEmail("new@email.com")
            .applicantRepresented(YesOrNo.YES)
            .build();

        FinremCaseData caseDataBefore = FinremCaseData.builder()
            .contactDetailsWrapper(beforeWrapper)
            .build();
        FinremCaseData caseData = FinremCaseData.builder()
            .contactDetailsWrapper(afterWrapper)
            .issueDate(null) // Simulate application not issued
            .build();

        FinremCallbackRequest callbackRequest = FinremCallbackRequestFactory.from(Long.valueOf(CASE_ID), caseDataBefore, caseData);

        lenient().when(retryExecutor.supplyWithRetryWithHandler(any(), anyString(), anyString(), any()))
            .thenReturn(java.util.Optional.of(true));
        lenient().when(updateContactDetailsNotificationService.prepareNocEmailToLitigantSolicitor(any()))
            .thenReturn(mock(SendCorrespondenceEvent.class));

        // Act
        handler.handle(callbackRequest, AUTH_TOKEN);

        // Assert
        verify(retryExecutor).supplyWithRetryWithHandler(any(), eq("Update Contact Details - granting applicant solicitor"), eq(CASE_ID), any());
    }

    /**
     * Provides a stream of test scenarios for verifying solicitor change scenarios
     * and their impact on auto assignment of solicitor's access.
     * Stream of test scenario arguments lines corresponds to the following :-
     * 1. Old Applicant Solicitor details
     * 2. Old Respondent Solicitor details
     * 3. New Applicant Solicitor details
     * 4. New Respondent Solicitor details
     * 5. Expected auto assignment for applicant and respondent (boolean flags)
     */
    static Stream<Arguments> testSolicitorChangeScenarios() {
        return Stream.of(
            Arguments.of("1. Both applicant Solicitor and respondent Solicitor changed",
                buildContactDetailsWrapper(
                    "Old AppSol Name", "Old AppSol Firm",
                    "OldAppSol@email.com", true, false,
                    "Old RespSol Name", "Old RespSol Firm",
                    "OldRespSol@email.com", false),
                buildContactDetailsWrapper(
                    "New AppSol Name", "New AppSol Firm",
                    "NewAppSol@email.com", true, false,
                    "New RespSol Name", "New RespSol Firm",
                    "NewRespSol@email.com", false),
                true, true
            ),
            Arguments.of("2. No change both Applicant Solicitor and Respondent solicitor",
                buildContactDetailsWrapper(
                    "Old AppSol Name", "Old AppSol Firm",
                    "OldAppSol@email.com", true, false,
                    "Old RespSol Name", "Old RespSol Firm",
                    "OldRespSol@email.com", false),
                buildContactDetailsWrapper(
                    "Old AppSol Name", "Old AppSol Firm",
                    "OldAppSol@email.com", true, false,
                    "Old RespSol Name", "Old RespSol Firm",
                    "OldRespSol@email.com", false),
                false, false
            ),
            Arguments.of("3. Applicant no change, Respondent unrepresented",
                buildContactDetailsWrapper(
                    "Old AppSol Name", "Old AppSol Firm",
                    "OldAppSol@email.com", true, false,
                    null, null,
                    null, false),
                buildContactDetailsWrapper(
                    "Old AppSol Name", "Old AppSol Firm",
                    "OldAppSol@email.com", true, false,
                    null, null,
                    null, false),
                false, false
            ),
            Arguments.of("4. Respondent no change, Applicant unrepresented",
                buildContactDetailsWrapper(
                    null, null,
                    null, false, false,
                    "Old RespSol Name", "Old RespSol Firm",
                    "OldRespSol@email.com", false),
                buildContactDetailsWrapper(
                    null, null,
                    null, false, false,
                    "Old RespSol Name", "Old RespSol Firm",
                    "OldRespSol@email.com", false),
                false, false
            ),
            Arguments.of("5. Both Unpresented no change",
                buildContactDetailsWrapper(
                    null, null,
                    null, false, false,
                    null, null,
                    null, false),
                buildContactDetailsWrapper(
                    null, null,
                    null, false, false,
                    null, null,
                    null, false),
                false, false
            ),
            Arguments.of("6. Applicant Solicitor change, Respondent not represented.",
                buildContactDetailsWrapper(
                    "Old AppSol Name", "Old AppSol Firm",
                    "OldAppSol@email.com", true, false,
                    null, null,
                    null, false),
                buildContactDetailsWrapper(
                    "New AppSol Name", "New AppSol Firm",
                    "NewAppSol@email.com", true, false,
                    null, null,
                    null, false),
                true, false
            ),
            Arguments.of("7. Respondent Solicitor change, Applicant not represented.",
                buildContactDetailsWrapper(
                    null, null,
                    null, false, false,
                    "Old RespSol Name", "Old RespSol Firm",
                    "OldRespSol@email.com", true),
                buildContactDetailsWrapper(
                    null, null,
                    null, false, false,
                    "New RespSol Name", "New RespSol Firm",
                    "NewRespSol@email.com", true),
                false, true
            ),
            Arguments.of("9. Only applicant solicitor to unpresented, Respondent not represented.",
                buildContactDetailsWrapper(
                    null, null,
                    null, false, true,
                    "Old RespSol Name", "Old RespSol Firm",
                    "OldRespSol@email.com", false),
                buildContactDetailsWrapper(
                    null, null,
                    null, false, true,
                    "New RespSol Name", "New RespSol Firm",
                    "NewRespSol@email.com", false),
                false, true
            ),
            Arguments.of("10. Only Applicant solicitor addded",
                buildContactDetailsWrapper(
                    null, null,
                    null, false, true,
                    null, null,
                    null, false),
                buildContactDetailsWrapper(
                    "New AppSol Name", "New AppSol Firm",
                    "NewAppSol@email.com", true, false,
                    null, null,
                    null, false),
                true, false
            ),
            Arguments.of("11. No change Applicant Solicitor, Respondent added.",
                buildContactDetailsWrapper(
                    null, null,
                    null, false, true,
                    null, null,
                    null, false),
                buildContactDetailsWrapper(
                    null, null,
                    null, false, true,
                    "New RespSol Name", "New RespSol Firm",
                    "NewRespSol@email.com", true),
                false, true
            ),
            Arguments.of("12. Only RespSol change to rep.",
                buildContactDetailsWrapper(
                    "Old AppSol Name", "Old AppSol Firm",
                    "OldAppSol@email.com", true, false,
                    null, null,
                    null, false),
                buildContactDetailsWrapper(
                    "Old AppSol Name", "Old AppSol Firm",
                    "OldAppSol@email.com", true, false,
                    "Old RespSol Name", "Old RespSol Firm",
                    "OldRespSol@email.com", false),
                false, true
            ),
            Arguments.of("13. Only RespSol to Unrep.",
                buildContactDetailsWrapper(
                    null, null,
                    null, false, true,
                    "Old RespSol Name", "Old RespSol Firm",
                    "OldRespSol@email.com", false),
                buildContactDetailsWrapper(
                    "Old AppSol Name", "Old AppSol Firm",
                    "OldAppSol@email.com", true, false,
                    "Old RespSol Name", "Old RespSol Firm",
                    "OldRespSol@email.com", false),
                true, false
            ),
            Arguments.of("14. Only AppSol change, Respondent not represented.",
                buildContactDetailsWrapper(
                    "Old AppSol Name", "Old AppSol Firm",
                    "OldAppSol@email.com", true, false,
                    null, null,
                    null, false),
                buildContactDetailsWrapper(
                    "New AppSol Name", "New AppSol Firm",
                    "NewppSol@email.com", true, false,
                    null, null,
                    null, false),
                true, false
            ),
            Arguments.of("15. Only RespSol change, Applicant not represented.",
                buildContactDetailsWrapper(
                    null, null,
                    null, false, true,
                    "Old RespSol Name", "Old RespSol Firm",
                    "OldRespSol@email.com", false),
                buildContactDetailsWrapper(
                    null, null,
                    null, false, true,
                    "New RespSol Name", "New RespSol Firm",
                    "NewRespSol@email.com", false),
                false, true
            ),
            Arguments.of("16. No change Applicant Solicitor, Respondent not represented.",
                buildContactDetailsWrapper(
                    "Old AppSol Name", "Old AppSol Firm",
                    "OldAppSol@email.com", true, false,
                    null, null,
                    null, false),
                buildContactDetailsWrapper(
                    "Old AppSol Name", "Old AppSol Firm",
                    "OldAppSol@email.com", true, false,
                    null, null,
                    null, false),
                false, false
            ),
            Arguments.of("17. Only Applicant Change by email case-sensitivity - FinremCallbackRequest normalizes emails",
                buildContactDetailsWrapper(
                    "old appsol name", "old appsol firm",
                    "OldAppSol@email.com", true, false,
                    null, null,
                    null, false),
                buildContactDetailsWrapper(
                    "OLD APPSOL NAME", "OLD APPSOL FIRM",
                    "OLDAPPSOL@email.com", true,
                    false, null, null,
                    null, false),
                false, false
            ),
            Arguments.of("18. Only Respondent Change by email",
                buildContactDetailsWrapper(
                    "Old AppSol Name", "Old AppSol Firm",
                    "OldAppSol@email.com", true,
                    true, "Old RespSol Name", "Old RespSol Firm",
                    "OldRespSol@email.com", false),
                buildContactDetailsWrapper(
                    "Old AppSol Name", "Old AppSol Firm",
                    "OldAppSol@email.com", true, true,
                    "OLD RESPSOL NAME", "OLD RESPSOL FIRM",
                    "newrespsol@email.com", false),
                false, true
            )
        );
    }

    /**
     * Tests various scenarios of solicitor change and their impact on auto assignment of solicitor's access.
     *
     * @param beforeWrapper - Contact details wrapper representing the application and respondent solicitor details before the update
     * @param afterWrapper - Contact details wrapper representing the application and respondent solicitor details after the update
     * @param expectApplicantAutoAssignAccess - boolean flag indicating whether an applicant auto assignment of solicitor's access is expected
     * @param expectRespondentAutoAssignAccess - boolean flag indicating whether a respondent auto assignment of solicitor's access is expected
     */
    @ParameterizedTest
    @MethodSource
    void testSolicitorChangeScenarios(String scenarioName, ContactDetailsWrapper beforeWrapper, ContactDetailsWrapper afterWrapper,
                                      boolean expectApplicantAutoAssignAccess, boolean expectRespondentAutoAssignAccess) {

        FinremCaseDetails beforeDetails = buildCaseDetails(beforeWrapper);
        FinremCaseDetails afterDetails = buildCaseDetails(afterWrapper);

        lenient().when(retryExecutor.supplyWithRetryWithHandler(any(), anyString(), anyString(), any()))
            .thenReturn(Optional.of(true));
        lenient().when(updateContactDetailsNotificationService.prepareNocEmailToLitigantSolicitor(any()))
            .thenReturn(mock(SendCorrespondenceEvent.class));
        lenient().when(updateContactDetailsNotificationService.prepareNocEmailToLitigantSolicitor(any()))
            .thenReturn(mock(SendCorrespondenceEvent.class));
        FinremCallbackRequest callbackRequest = FinremCallbackRequestFactory.from(Long.valueOf(CASE_ID),
            beforeDetails.getData(), afterDetails.getData());

        // Act
        handler.handle(callbackRequest, AUTH_TOKEN);

        // Verify
        if (expectApplicantAutoAssignAccess) {
            verify(retryExecutor).supplyWithRetryWithHandler(
                any(),
                eq("Update Contact Details - granting applicant solicitor"),
                eq(CASE_ID),
                any(RetryErrorHandler.class));
        }

        if (expectRespondentAutoAssignAccess) {
            verify(retryExecutor).supplyWithRetryWithHandler(
                any(),
                eq("Update Contact Details - granting respondent solicitor"),
                eq(CASE_ID),
                any(RetryErrorHandler.class));
        }

        if (!expectApplicantAutoAssignAccess && !expectRespondentAutoAssignAccess) {
            verify(retryExecutor, never()).supplyWithRetryWithHandler(any(), anyString(), anyString(), any());
        }
    }

    static FinremCaseDetails buildCaseDetails(ContactDetailsWrapper wrapper) {
        FinremCaseData data = FinremCaseData.builder()
            .contactDetailsWrapper(wrapper)
            .issueDate(LocalDate.now())
            .build();
        return FinremCaseDetails.builder()
            .data(data)
            .id(CASE_ID_IN_LONG)
            .state(State.APPLICATION_ISSUED)
            .build();
    }

    static Address buildAddress() {
        return Address.builder()
            .addressLine1("AddressLine1")
            .addressLine2("AddressLine2")
            .addressLine3("AddressLine3")
            .county("County")
            .country("Country")
            .postTown("Town")
            .postCode("EC1 3AS")
            .build();
    }

    static ContactDetailsWrapper buildContactDetailsWrapper(
        String applicantSolicitorName,
        String applicantSolicitorFirm,
        String applicantSolicitorEmail,
        boolean isCurrentUserApplicantSolicitor,
        boolean isUpdateIncludesRepresentativeChange,
        String respondentSolicitorName,
        String respondentSolicitorFirm,
        String respondentSolicitorEmail,
        boolean isCurrentUserRespondentSolicitor) {

        ContactDetailsWrapper.ContactDetailsWrapperBuilder builder = ContactDetailsWrapper.builder()
            .applicantSolicitorName(applicantSolicitorName)
            .applicantSolicitorFirm(applicantSolicitorFirm)
            .applicantSolicitorEmail(applicantSolicitorEmail)
            .applicantSolicitorAddress(buildAddress())
            .applicantRepresented(applicantSolicitorEmail != null ? YesOrNo.YES : YesOrNo.NO)
            .currentUserIsApplicantSolicitor(isCurrentUserApplicantSolicitor ? YesOrNo.YES : YesOrNo.NO)
            .updateIncludesRepresentativeChange(isUpdateIncludesRepresentativeChange ? YesOrNo.YES : YesOrNo.NO)
            .respondentSolicitorName(respondentSolicitorName)
            .respondentSolicitorFirm(respondentSolicitorFirm)
            .respondentSolicitorEmail(respondentSolicitorEmail)
            .respondentSolicitorAddress(buildAddress())
            .contestedRespondentRepresented(respondentSolicitorEmail != null ? YesOrNo.YES : YesOrNo.NO)
            .consentedRespondentRepresented(respondentSolicitorEmail != null ? YesOrNo.YES : YesOrNo.NO)
            .currentUserIsRespondentSolicitor(isCurrentUserRespondentSolicitor ? YesOrNo.YES : YesOrNo.NO);
        return builder.build();
    }
}
