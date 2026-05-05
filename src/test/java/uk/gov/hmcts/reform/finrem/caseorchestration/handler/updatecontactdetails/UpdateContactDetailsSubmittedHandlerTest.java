package uk.gov.hmcts.reform.finrem.caseorchestration.handler.updatecontactdetails;

import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.CsvSource;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Spy;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.util.function.ThrowingSupplier;
import uk.gov.hmcts.reform.finrem.caseorchestration.FinremCallbackRequestFactory;
import uk.gov.hmcts.reform.finrem.caseorchestration.ccd.callback.CallbackType;
import uk.gov.hmcts.reform.finrem.caseorchestration.controllers.GenericAboutToStartOrSubmitCallbackResponse;
import uk.gov.hmcts.reform.finrem.caseorchestration.handler.FinremCallbackRequest;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.FinremCaseData;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.FinremCaseDetails;
import uk.gov.hmcts.reform.finrem.caseorchestration.notifications.notifiers.SendCorrespondenceEvent;
import uk.gov.hmcts.reform.finrem.caseorchestration.service.AssignPartiesAccessService;
import uk.gov.hmcts.reform.finrem.caseorchestration.service.UpdateContactDetailsNotificationService;
import uk.gov.hmcts.reform.finrem.caseorchestration.utils.retry.RetryErrorHandler;
import uk.gov.hmcts.reform.finrem.caseorchestration.utils.retry.RetryExecutor;
import uk.gov.hmcts.reform.finrem.caseorchestration.utils.retry.ThrowingRunnable;

import java.time.LocalDate;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertAll;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.doAnswer;
import static org.mockito.Mockito.lenient;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.spy;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.mockito.Mockito.verifyNoMoreInteractions;
import static org.mockito.Mockito.when;
import static uk.gov.hmcts.reform.finrem.caseorchestration.TestConstants.AUTH_TOKEN;
import static uk.gov.hmcts.reform.finrem.caseorchestration.TestConstants.CASE_ID;
import static uk.gov.hmcts.reform.finrem.caseorchestration.TestConstants.CASE_ID_IN_LONG;
import static uk.gov.hmcts.reform.finrem.caseorchestration.TestSetUpUtils.assertCondition;
import static uk.gov.hmcts.reform.finrem.caseorchestration.TestSetUpUtils.getThrowingRunnableCaptor;
import static uk.gov.hmcts.reform.finrem.caseorchestration.TestSetUpUtils.getThrowingSupplierCaptor;
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
    @Mock
    private AssignPartiesAccessService assignPartiesAccessService;
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

    @Nested
    class NotificationTests {

        FinremCaseData finremCaseData;
        FinremCaseData finremCaseDataBefore;
        FinremCallbackRequest callbackRequest;

        private void mockSolicitorChangedAndApplicationIssued(
            boolean hasApplicantSolicitorChanged, boolean hasRespondentSolicitorChanged, boolean isApplicationIssued) {
            finremCaseData = spy(new FinremCaseData());
            when(finremCaseData.getAppSolicitorEmailIfRepresented()).thenReturn("new.applicant@solicitor.com");
            when(finremCaseData.getRespSolicitorEmailIfRepresented()).thenReturn("new.respondent@solicitor.com");
            finremCaseDataBefore = spy(new FinremCaseData());
            when(finremCaseDataBefore.getAppSolicitorEmailIfRepresented()).thenReturn("old.applicant@solicitor.com");
            when(finremCaseDataBefore.getRespSolicitorEmailIfRepresented()).thenReturn("old.respondent@solicitor.com");
            lenient().when(finremCaseData.getIssueDate()).thenReturn(isApplicationIssued ? mock(LocalDate.class) : null);

            callbackRequest = spy(FinremCallbackRequestFactory.from(CASE_ID_IN_LONG,
                finremCaseDataBefore, finremCaseData));
            when(callbackRequest.hasApplicantSolicitorChanged()).thenReturn(hasApplicantSolicitorChanged);
            when(callbackRequest.hasRespondentSolicitorChanged()).thenReturn(hasRespondentSolicitorChanged);
        }

        private void simulateGrantAndRevokeOperationsWorkingFine() {
            lenient().when(retryExecutor.supplyWithRetryWithHandler(any(ThrowingSupplier.class),
                eq("Update Contact Details - granting applicant solicitor"),
                eq(CASE_ID), any(RetryErrorHandler.class))).thenAnswer(invocation -> Optional.of(Boolean.TRUE));
            lenient().when(retryExecutor.supplyWithRetryWithHandler(any(ThrowingSupplier.class),
                eq("Update Contact Details - revoking applicant solicitor"),
                eq(CASE_ID), any(RetryErrorHandler.class))).thenAnswer(invocation -> Optional.of(Boolean.TRUE));
            lenient().when(retryExecutor.supplyWithRetryWithHandler(any(ThrowingSupplier.class),
                eq("Update Contact Details - granting respondent solicitor"),
                eq(CASE_ID), any(RetryErrorHandler.class))).thenAnswer(invocation -> Optional.of(Boolean.TRUE));
            lenient().when(retryExecutor.supplyWithRetryWithHandler(any(ThrowingSupplier.class),
                eq("Update Contact Details - revoking respondent solicitor"),
                eq(CASE_ID), any(RetryErrorHandler.class))).thenAnswer(invocation -> Optional.of(Boolean.TRUE));
        }

        @ParameterizedTest
        @CsvSource(value = {
            "false,false,false",
            "false,false,true",
            "true,false,false"
        })
        void givenNoSolicitorChanged_whenHandled_thenNoNocEmailAndLetterSent(
            boolean hasApplicantSolicitorChanged, boolean hasRespondentSolicitorChanged, boolean isApplicationIssued
        ) {
            mockSolicitorChangedAndApplicationIssued(hasApplicantSolicitorChanged, hasRespondentSolicitorChanged, isApplicationIssued);

            // Act
            GenericAboutToStartOrSubmitCallbackResponse<FinremCaseData> response = handler.handle(callbackRequest, AUTH_TOKEN);

            // Verify
            assertAll(
                () -> assertThat(response.getConfirmationBody()).isNull(),
                () -> assertThat(response.getConfirmationHeader()).isNull(),
                () -> verifyNoInteractions(applicationEventPublisher, updateContactDetailsNotificationService)
            );
        }

        @ParameterizedTest
        @CsvSource(value = {
            "true,true,false",
            "true,true,true",
            "true,false,true",
            "true,false,false",
            "false,true,true"
        })
        void givenAnySolicitorChanged_whenHandled_thenNocEmailAndLetterSent(
            boolean hasApplicantSolicitorChanged, boolean hasRespondentSolicitorChanged, boolean isApplicationIssued
        ) {
            mockSolicitorChangedAndApplicationIssued(hasApplicantSolicitorChanged, hasRespondentSolicitorChanged, isApplicationIssued);

            // Act
            simulateGrantAndRevokeOperationsWorkingFine();

            SendCorrespondenceEvent event = mock(SendCorrespondenceEvent.class);
            when(event.getCaseId()).thenReturn(CASE_ID);
            when(updateContactDetailsNotificationService.prepareNocEmailToLitigantSolicitor(callbackRequest.getCaseDetails()))
                .thenReturn(event);

            // Act
            GenericAboutToStartOrSubmitCallbackResponse<FinremCaseData> response = handler.handle(callbackRequest, AUTH_TOKEN);

            // Verify
            ArgumentCaptor<ThrowingRunnable> nocNotificationCaptor = getThrowingRunnableCaptor();
            assertAll(
                () -> assertThat(response.getConfirmationBody()).isNull(),
                () -> assertThat(response.getConfirmationHeader()).isNull(),
                () -> verify(updateContactDetailsNotificationService).prepareNocEmailToLitigantSolicitor(callbackRequest.getCaseDetails()),
                () -> verify(retryExecutor).runWithRetryWithHandler(
                    nocNotificationCaptor.capture(),
                    eq("Sending NOC email to litigant solicitor"),
                    eq(CASE_ID),
                    any(RetryErrorHandler.class)),
                () -> {
                    nocNotificationCaptor.getValue().run();
                    verify(applicationEventPublisher).publishEvent(event);
                },
                () -> verify(retryExecutor).runWithRetryWithHandler(
                    nocNotificationCaptor.capture(),
                    eq("Sending NOC letter"),
                    eq(CASE_ID),
                    any(RetryErrorHandler.class)),
                () -> {
                    nocNotificationCaptor.getValue().run();
                    verify(updateContactDetailsNotificationService).sendNocLetterToLitigants(any(FinremCaseDetails.class),
                        any(FinremCaseDetails.class), eq(AUTH_TOKEN));
                },
                () -> verifyNoMoreInteractions(applicationEventPublisher, updateContactDetailsNotificationService)
            );
        }

        @ParameterizedTest
        @CsvSource(value = {
            "true,true",
            "true,false",
            "false,true",
            "false,false"
        })
        void givenExceptionThrown_whenSendingNotification_thenPopulateErrorToConfirmationBody(
            boolean nocLetterFailure, boolean nocEmailToLitigantSolicitorFailure
        ) {
            mockSolicitorChangedAndApplicationIssued(true, true, false);

            // Act
            simulateGrantAndRevokeOperationsWorkingFine();
            SendCorrespondenceEvent event = mock(SendCorrespondenceEvent.class);
            when(event.getCaseId()).thenReturn(CASE_ID);
            when(updateContactDetailsNotificationService.prepareNocEmailToLitigantSolicitor(callbackRequest.getCaseDetails()))
                .thenReturn(event);

            if (nocEmailToLitigantSolicitorFailure) {
                mockRunWithRetryWithHandlerInvokesFirstErrorHandler(
                    retryExecutor,
                    "Sending NOC email to litigant solicitor"
                );
            } else {
                doAnswer(invocation -> {
                    // Nothing happened
                    return null;
                }).when(retryExecutor).runWithRetryWithHandler(any(), eq("Sending NOC email to litigant solicitor"), any(), any());
            }
            if (nocLetterFailure) {
                mockRunWithRetryWithHandlerInvokesFirstErrorHandler(
                    retryExecutor,
                    "Sending NOC letter"
                );
            } else {
                doAnswer(invocation -> {
                    // Nothing happened
                    return null;
                }).when(retryExecutor).runWithRetryWithHandler(any(), eq("Sending NOC letter"), any(), any());
            }

            // Act
            GenericAboutToStartOrSubmitCallbackResponse<FinremCaseData> response = handler.handle(callbackRequest, AUTH_TOKEN);

            // then
            if (!nocEmailToLitigantSolicitorFailure && !nocLetterFailure) {
                assertAll(
                    () -> assertThat(response.getConfirmationHeader()).isNull(),
                    () -> assertThat(response.getConfirmationBody()).isNull()
                );
            } else {
                assertAll(
                    () -> assertThat(response.getConfirmationHeader()).contains("Contact details updated with errors"),
                    () -> assertCondition(response.getConfirmationBody(), "Fail to send NOC letter to litigants.",
                        nocLetterFailure),
                    () -> assertCondition(response.getConfirmationBody(), "Fail to send notice of change email to litigant solicitor.",
                        nocEmailToLitigantSolicitorFailure),
                    () -> verify(updateContactDetailsNotificationService).prepareNocEmailToLitigantSolicitor(any(FinremCaseDetails.class))
                );
            }
        }
    }

    @Nested
    class GrantOrRevokeTests {

        @ParameterizedTest
        @CsvSource({
            "true,true",
            "true,false",
            "false,true"
        })
        void givenApplicantSolicitorChanged_whenUnableToGrantOrRevoke_thenErrorsPopulatedToConfirmation(boolean failGrant, boolean failRevoke) {
            FinremCaseData finremCaseData = spy(FinremCaseData.builder().build());
            when(finremCaseData.getAppSolicitorEmailIfRepresented()).thenReturn("new@email.com");
            FinremCaseData finremCaseDataBefore = spy(FinremCaseData.builder().build());
            when(finremCaseDataBefore.getAppSolicitorEmailIfRepresented()).thenReturn("old@email.com");

            FinremCallbackRequest callbackRequest = spy(FinremCallbackRequestFactory.from(Long.valueOf(CASE_ID), finremCaseDataBefore,
                finremCaseData));
            when(callbackRequest.hasApplicantSolicitorChanged()).thenReturn(true);
            when(callbackRequest.hasRespondentSolicitorChanged()).thenReturn(false);
            when(updateContactDetailsNotificationService.prepareNocEmailToLitigantSolicitor(callbackRequest.getCaseDetails()))
                .thenReturn(mock(SendCorrespondenceEvent.class));

            // Simulate error in checkAndAssignSolicitorAccess by making retryExecutor set the error

            when(retryExecutor.supplyWithRetryWithHandler(any(ThrowingSupplier.class), eq("Update Contact Details - granting applicant solicitor"),
                eq(CASE_ID), any())).thenAnswer(invocation -> {
                    if (failGrant) {
                        String actionName = invocation.getArgument(1);
                        String caseId = invocation.getArgument(2);
                        RetryErrorHandler errorHandler = invocation.getArgument(3);
                        errorHandler.handle(new RuntimeException("fail"), actionName, caseId);
                    }
                return Optional.of(Boolean.TRUE);
            });

            when(retryExecutor.supplyWithRetryWithHandler(any(ThrowingSupplier.class), eq("Update Contact Details - revoking applicant solicitor"),
                eq(CASE_ID), any())).thenAnswer(invocation -> {
                if (failRevoke) {
                    String actionName = invocation.getArgument(1);
                    String caseId = invocation.getArgument(2);
                    RetryErrorHandler errorHandler = invocation.getArgument(3);
                    errorHandler.handle(new RuntimeException("fail"), actionName, caseId);
                    return Optional.empty();
                }
                return Optional.of(Boolean.TRUE);
            });

            // Act
            GenericAboutToStartOrSubmitCallbackResponse<FinremCaseData> response = handler.handle(callbackRequest, AUTH_TOKEN);

            // Assert
            String body = response.getConfirmationBody();
            String header = response.getConfirmationHeader();

            String grantMsg = "There was a problem granting access to applicant solicitor (new@email.com). Please grant access manually.";
            String revokeMsg = "There was a problem revoking access to applicant solicitor (old@email.com). Please revoke access manually.";

            assertAll(
                () -> verify(retryExecutor).supplyWithRetryWithHandler(
                    any(ThrowingSupplier.class),
                    eq("Update Contact Details - granting applicant solicitor"),
                    eq(CASE_ID),
                    any(RetryErrorHandler.class)
                ),
                () -> verify(retryExecutor).supplyWithRetryWithHandler(
                    any(ThrowingSupplier.class),
                    eq("Update Contact Details - revoking applicant solicitor"),
                    eq(CASE_ID),
                    any(RetryErrorHandler.class)
                ),
                () -> assertThat(header).contains("Contact details updated with errors"),
                () -> assertCondition(body, grantMsg, failGrant),
                () -> assertCondition(body, revokeMsg, failRevoke)
            );
        }

        @Test
        void givenApplicantSolicitorChanged_whenHandled_thenGrantAndRevokeApplicantSolicitorAndNotifyParties() {
            FinremCaseData finremCaseData = spy(FinremCaseData.builder().build());
            when(finremCaseData.getAppSolicitorEmailIfRepresented()).thenReturn("new@email.com");
            FinremCaseData finremCaseDataBefore = spy(FinremCaseData.builder().build());
            when(finremCaseDataBefore.getAppSolicitorEmailIfRepresented()).thenReturn("old@email.com");

            FinremCallbackRequest callbackRequest = spy(FinremCallbackRequestFactory.from(Long.valueOf(CASE_ID), finremCaseDataBefore,
                finremCaseData));
            when(callbackRequest.hasApplicantSolicitorChanged()).thenReturn(true);
            when(callbackRequest.hasRespondentSolicitorChanged()).thenReturn(false);

            when(updateContactDetailsNotificationService.prepareNocEmailToLitigantSolicitor(callbackRequest.getCaseDetails()))
                .thenReturn(mock(SendCorrespondenceEvent.class));
            // Simulate grant/revoke applicant solicitor success
            when(retryExecutor.supplyWithRetryWithHandler(any(ThrowingSupplier.class),
                eq("Update Contact Details - granting applicant solicitor"),
                eq(CASE_ID), any())).thenAnswer(invocation -> Optional.of(Boolean.TRUE));
            when(retryExecutor.supplyWithRetryWithHandler(any(ThrowingSupplier.class),
                eq("Update Contact Details - revoking applicant solicitor"),
                eq(CASE_ID), any())).thenAnswer(invocation -> Optional.of(Boolean.TRUE));

            // Act
            var response = handler.handle(callbackRequest, AUTH_TOKEN);

            // Verify
            ArgumentCaptor<ThrowingSupplier<Boolean>> grantApplicantSolicitorCaptor = getThrowingSupplierCaptor();
            ArgumentCaptor<ThrowingSupplier<Boolean>> revokeApplicantSolicitorCaptor = getThrowingSupplierCaptor();

            assertAll(
                // to verify execution of granting applicant solicitor
                () -> verify(retryExecutor).supplyWithRetryWithHandler(
                    grantApplicantSolicitorCaptor.capture(),
                    eq("Update Contact Details - granting applicant solicitor"),
                    eq(CASE_ID),
                    any(RetryErrorHandler.class)),
                // to verify assignPartiesAccessService.grantApplicantSolicitor was invoked
                () -> {
                    assertTrue(grantApplicantSolicitorCaptor.getValue().get());
                    verify(assignPartiesAccessService).grantApplicantSolicitor(finremCaseData);
                },
                // to verify execution of revoking applicant solicitor
                () -> verify(retryExecutor).supplyWithRetryWithHandler(
                    revokeApplicantSolicitorCaptor.capture(),
                    eq("Update Contact Details - revoking applicant solicitor"),
                    eq(CASE_ID),
                    any(RetryErrorHandler.class)),
                // to verify assignPartiesAccessService.revokeApplicantSolicitor was invoked
                () -> {
                    assertTrue(revokeApplicantSolicitorCaptor.getValue().get());
                    verify(assignPartiesAccessService).revokeApplicantSolicitor(finremCaseDataBefore);
                },
                () -> verifyNoMoreInteractions(assignPartiesAccessService),
                // to verify notifying parties
                () -> verify(updateContactDetailsNotificationService)
                    .prepareNocEmailToLitigantSolicitor(callbackRequest.getCaseDetails()),
                // to verify happy path that return null
                () -> assertThat(response.getConfirmationHeader()).isNull(),
                () -> assertThat(response.getConfirmationBody()).isNull()
            );
        }

        // TODO Dawud
        void givenRespondentSolicitorChanged_whenUnableToGrantOrRevoke_thenErrorsPopulatedToConfirmation() {}

        // TODO Dawud
        void givenRespondentSolicitorChanged_whenHandled_thenGrantAndRevokeApplicantSolicitorAndNotifyParties() {}

    }

    // TODO Dawud
    void givenRespondentSolicitorChangedButApplicationNotIssued_whenHandled_thenNoCaseAssignmentAndNoNotification() {}

    @Test
    void givenNoApplicantAndRespondentSolicitorChanged_whenHandled_thenNoCaseAssignmentAndNoNotification() {
        FinremCallbackRequest callbackRequest = spy(FinremCallbackRequestFactory.from());
        when(callbackRequest.hasApplicantSolicitorChanged()).thenReturn(false);
        when(callbackRequest.hasRespondentSolicitorChanged()).thenReturn(false);

        // Act
        var response = handler.handle(callbackRequest, AUTH_TOKEN);

        assertAll(
            () -> verifyNoInteractions(assignPartiesAccessService, updateContactDetailsNotificationService,
                applicationEventPublisher, retryExecutor),
            // to verify happy path that return null
            () -> assertThat(response.getConfirmationHeader()).isNull(),
            () -> assertThat(response.getConfirmationBody()).isNull()
        );
    }

//
//    @Test
//    void givenUpdateContactDetails_WhenApplicantSolicitorEmailChangeThenCheckAndAssignSolicitorToCase() {
//        ContactDetailsWrapper beforeWrapper = ContactDetailsWrapper.builder()
//            .applicantSolicitorEmail("old@email.com")
//            .applicantRepresented(YesOrNo.YES)
//            .build();
//
//        ContactDetailsWrapper afterWrapper = ContactDetailsWrapper.builder()
//            .applicantSolicitorEmail("new@email.com")
//            .applicantRepresented(YesOrNo.YES)
//            .build();
//
//        FinremCaseDetails beforeDetails = buildCaseDetails(beforeWrapper);
//        FinremCaseDetails afterDetails = buildCaseDetails(afterWrapper);
//
//        FinremCallbackRequest callbackRequest =
//            FinremCallbackRequestFactory.from(Long.valueOf(CASE_ID), beforeDetails.getData(), afterDetails.getData());
//
//        lenient().when(retryExecutor.supplyWithRetryWithHandler(any(), anyString(), anyString(), any()))
//            .thenReturn(java.util.Optional.of(true));
//        lenient().when(updateContactDetailsNotificationService.prepareNocEmailToLitigantSolicitor(any()))
//            .thenReturn(mock(SendCorrespondenceEvent.class));
//
//        // Act
//        GenericAboutToStartOrSubmitCallbackResponse<FinremCaseData> response = handler.handle(callbackRequest, AUTH_TOKEN);
//
//        // Verify
//        assertAll(
//            () -> assertThat(response.getConfirmationBody()).isNull(),
//            () -> assertThat(response.getConfirmationHeader()).isNull(),
//            () -> verify(retryExecutor).supplyWithRetryWithHandler(
//                any(),
//                eq("Update Contact Details - granting applicant solicitor"),
//                eq(CASE_ID),
//                any(RetryErrorHandler.class)),
//            () -> verify(retryExecutor).supplyWithRetryWithHandler(
//                any(),
//                eq("Update Contact Details - revoking applicant solicitor"),
//                eq(CASE_ID),
//                any(RetryErrorHandler.class))
//        );
//    }
//
//    @Test
//    void givenUpdateContactDetails_WhenRespondentSolicitorEmailChangeThenCheckAndAssignSolicitorToContestedCase() {
//        ContactDetailsWrapper beforeWrapper = ContactDetailsWrapper.builder()
//            .respondentSolicitorEmail("old@email.com")
//            .contestedRespondentRepresented(YesOrNo.YES)
//            .build();
//
//        ContactDetailsWrapper afterWrapper = ContactDetailsWrapper.builder()
//            .respondentSolicitorEmail("new@email.com")
//            .contestedRespondentRepresented(YesOrNo.YES)
//            .build();
//
//        FinremCaseDetails beforeDetails = buildCaseDetails(beforeWrapper);
//        FinremCaseDetails afterDetails = buildCaseDetails(afterWrapper);
//
//        FinremCallbackRequest callbackRequest =
//            FinremCallbackRequestFactory.from(Long.valueOf(CASE_ID), beforeDetails.getData(), afterDetails.getData());
//
//        lenient().when(retryExecutor.supplyWithRetryWithHandler(any(), anyString(), anyString(), any()))
//            .thenReturn(java.util.Optional.of(true));
//        lenient().when(updateContactDetailsNotificationService.prepareNocEmailToLitigantSolicitor(any()))
//            .thenReturn(mock(SendCorrespondenceEvent.class));
//
//        // Act
//        GenericAboutToStartOrSubmitCallbackResponse<FinremCaseData> response = handler.handle(callbackRequest, AUTH_TOKEN);
//
//        // Verify
//        assertAll(
//            () -> assertThat(response.getConfirmationBody()).isNull(),
//            () -> assertThat(response.getConfirmationHeader()).isNull(),
//            () -> verify(retryExecutor).supplyWithRetryWithHandler(
//                any(),
//                eq("Update Contact Details - granting respondent solicitor"),
//                eq(CASE_ID),
//                any(RetryErrorHandler.class)),
//            () -> verify(retryExecutor).supplyWithRetryWithHandler(
//                any(),
//                eq("Update Contact Details - revoking respondent solicitor"),
//                eq(CASE_ID),
//                any(RetryErrorHandler.class))
//        );
//    }
//
//    @Test
//    void givenUpdateContactDetails_WhenRespondentSolicitorEmailChangeThenCheckAndAssignSolicitorToConsentedCase() {
//        ContactDetailsWrapper beforeWrapper = ContactDetailsWrapper.builder()
//            .respondentSolicitorEmail("new@email.com")
//            .consentedRespondentRepresented(YesOrNo.YES)
//            .build();
//
//        ContactDetailsWrapper afterWrapper = ContactDetailsWrapper.builder()
//            .respondentSolicitorEmail("old@email.com")
//            .consentedRespondentRepresented(YesOrNo.YES)
//            .build();
//
//        FinremCaseDetails beforeDetails = buildCaseDetails(beforeWrapper);
//        FinremCaseDetails afterDetails = buildCaseDetails(afterWrapper);
//
//        FinremCallbackRequest callbackRequest =
//            FinremCallbackRequestFactory.from(Long.valueOf(CASE_ID), beforeDetails.getData(), afterDetails.getData());
//
//        lenient().when(retryExecutor.supplyWithRetryWithHandler(any(), anyString(), anyString(), any()))
//            .thenReturn(java.util.Optional.of(true));
//        lenient().when(updateContactDetailsNotificationService.prepareNocEmailToLitigantSolicitor(any()))
//            .thenReturn(mock(SendCorrespondenceEvent.class));
//
//        // Act
//        GenericAboutToStartOrSubmitCallbackResponse<FinremCaseData> response = handler.handle(callbackRequest, AUTH_TOKEN);
//
//        // Verify
//        assertAll(
//            () -> assertThat(response.getConfirmationBody()).isNull(),
//            () -> assertThat(response.getConfirmationHeader()).isNull(),
//            () -> verify(retryExecutor).supplyWithRetryWithHandler(
//                any(),
//                eq("Update Contact Details - granting respondent solicitor"),
//                eq(CASE_ID),
//                any(RetryErrorHandler.class)),
//            () -> verify(retryExecutor).supplyWithRetryWithHandler(
//                any(),
//                eq("Update Contact Details - revoking respondent solicitor"),
//                eq(CASE_ID),
//                any(RetryErrorHandler.class))
//        );
//    }
//
//    @Test
//    void shouldReturnErrorConfirmationWhenCheckAndAssignSolicitorAccessFails() {
//        // Arrange
//        FinremCaseData finremCaseData = FinremCaseData
//            .builder()
//            .issueDate(LocalDate.now())
//            .contactDetailsWrapper(ContactDetailsWrapper.builder()
//                .applicantSolicitorEmail("new@email.com")
//                .applicantRepresented(YesOrNo.YES)
//                .build())
//            .build();
//        FinremCaseData finremCaseDataBefore = FinremCaseData.builder()
//            .contactDetailsWrapper(ContactDetailsWrapper.builder()
//                .applicantSolicitorEmail("old@email.com")
//                .applicantRepresented(YesOrNo.YES)
//                .build())
//            .build();
//        FinremCallbackRequest callbackRequest = FinremCallbackRequestFactory.from(CASE_ID_IN_LONG, finremCaseDataBefore, finremCaseData);
//
//        // Simulate error in checkAndAssignSolicitorAccess by making retryExecutor set the error
//        when(retryExecutor.supplyWithRetryWithHandler(any(), anyString(), anyString(), any())).thenAnswer(invocation -> {
//            String actionName = invocation.getArgument(1);
//            String caseId = invocation.getArgument(2);
//            RetryErrorHandler errorHandler = invocation.getArgument(3);
//            errorHandler.handle(new RuntimeException("fail"), actionName, caseId);
//            return java.util.Optional.empty();
//        });
//
//        // Act
//        GenericAboutToStartOrSubmitCallbackResponse<FinremCaseData> response = handler.handle(callbackRequest, AUTH_TOKEN);
//
//        // Assert
//        assertAll(
//            () -> assertThat(response.getConfirmationHeader()).contains("Contact details updated with errors"),
//            () -> assertThat(response.getConfirmationBody())
//                .contains("There was a problem granting access to applicant solicitor (new@email.com). Please grant access manually.")
//        );
//    }
//
//    @Test
//    void shouldReturnTrueWhenIssueDateIsPresent() throws Exception {
//        FinremCaseData caseData = mock(FinremCaseData.class);
//        when(caseData.getIssueDate()).thenReturn(LocalDate.now());
//
//        Method method = UpdateContactDetailsSubmittedHandler.class.getDeclaredMethod("hasApplicationBeenIssued", FinremCaseData.class);
//        method.setAccessible(true);
//
//        boolean result = (boolean) method.invoke(handler, caseData);
//
//        assertTrue(result, "Expected true when issue date is present");
//    }
//
//    @Test
//    void shouldReturnFalseWhenIssueDateIsNull() throws Exception {
//        FinremCaseData caseData = mock(FinremCaseData.class);
//        when(caseData.getIssueDate()).thenReturn(null);
//
//        Method method = UpdateContactDetailsSubmittedHandler.class.getDeclaredMethod("hasApplicationBeenIssued", FinremCaseData.class);
//        method.setAccessible(true);
//
//        boolean result = (boolean) method.invoke(handler, caseData);
//
//        assertFalse(result, "Expected false when issue date is null");
//    }
//
//    @Test
//    void givenApplicantSolicitorEmailChangedAndApplicationNotIssued_thenCheckAndAssignSolicitorAccess() {
//        // Arrange
//        ContactDetailsWrapper beforeWrapper = ContactDetailsWrapper.builder()
//            .applicantSolicitorEmail("old@email.com")
//            .applicantRepresented(YesOrNo.YES)
//            .build();
//        ContactDetailsWrapper afterWrapper = ContactDetailsWrapper.builder()
//            .applicantSolicitorEmail("new@email.com")
//            .applicantRepresented(YesOrNo.YES)
//            .build();
//
//        FinremCaseData caseDataBefore = FinremCaseData.builder()
//            .contactDetailsWrapper(beforeWrapper)
//            .build();
//        FinremCaseData caseData = FinremCaseData.builder()
//            .contactDetailsWrapper(afterWrapper)
//            .issueDate(null) // Simulate application not issued
//            .build();
//
//        FinremCallbackRequest callbackRequest = FinremCallbackRequestFactory.from(Long.valueOf(CASE_ID), caseDataBefore, caseData);
//
//        lenient().when(retryExecutor.supplyWithRetryWithHandler(any(), anyString(), anyString(), any()))
//            .thenReturn(java.util.Optional.of(true));
//        lenient().when(updateContactDetailsNotificationService.prepareNocEmailToLitigantSolicitor(any()))
//            .thenReturn(mock(SendCorrespondenceEvent.class));
//
//        // Act
//        handler.handle(callbackRequest, AUTH_TOKEN);
//
//        // Assert
//        verify(retryExecutor).supplyWithRetryWithHandler(any(), eq("Update Contact Details - granting applicant solicitor"), eq(CASE_ID), any());
//    }
//
//
//    /**
//     * Provides a stream of test scenarios for verifying solicitor change scenarios
//     * and their impact on auto assignment of solicitor's access.
//     * Stream of test scenario arguments lines corresponds to the following :-
//     * 1. Old Applicant Solicitor details
//     * 2. Old Respondent Solicitor details
//     * 3. New Applicant Solicitor details
//     * 4. New Respondent Solicitor details
//     * 5. Expected auto assignment for applicant and respondent (boolean flags)
//     */
//    static Stream<Arguments> testSolicitorChangeScenarios() {
//        return Stream.of(
//            Arguments.of("1. Both applicant Solicitor and respondent Solicitor changed",
//                buildContactDetailsWrapper(
//                    "Old AppSol Name", "Old AppSol Firm",
//                    "OldAppSol@email.com", true, false,
//                    "Old RespSol Name", "Old RespSol Firm",
//                    "OldRespSol@email.com", false),
//                buildContactDetailsWrapper(
//                    "New AppSol Name", "New AppSol Firm",
//                    "NewAppSol@email.com", true, false,
//                    "New RespSol Name", "New RespSol Firm",
//                    "NewRespSol@email.com", false),
//                true, true
//            ),
//            Arguments.of("2. No change both Applicant Solicitor and Respondent solicitor",
//                buildContactDetailsWrapper(
//                    "Old AppSol Name", "Old AppSol Firm",
//                    "OldAppSol@email.com", true, false,
//                    "Old RespSol Name", "Old RespSol Firm",
//                    "OldRespSol@email.com", false),
//                buildContactDetailsWrapper(
//                    "Old AppSol Name", "Old AppSol Firm",
//                    "OldAppSol@email.com", true, false,
//                    "Old RespSol Name", "Old RespSol Firm",
//                    "OldRespSol@email.com", false),
//                false, false
//            ),
//            Arguments.of("3. Applicant no change, Respondent unrepresented",
//                buildContactDetailsWrapper(
//                    "Old AppSol Name", "Old AppSol Firm",
//                    "OldAppSol@email.com", true, false,
//                    null, null,
//                    null, false),
//                buildContactDetailsWrapper(
//                    "Old AppSol Name", "Old AppSol Firm",
//                    "OldAppSol@email.com", true, false,
//                    null, null,
//                    null, false),
//                false, false
//            ),
//            Arguments.of("4. Respondent no change, Applicant unrepresented",
//                buildContactDetailsWrapper(
//                    null, null,
//                    null, false, false,
//                    "Old RespSol Name", "Old RespSol Firm",
//                    "OldRespSol@email.com", false),
//                buildContactDetailsWrapper(
//                    null, null,
//                    null, false, false,
//                    "Old RespSol Name", "Old RespSol Firm",
//                    "OldRespSol@email.com", false),
//                false, false
//            ),
//            Arguments.of("5. Both Unpresented no change",
//                buildContactDetailsWrapper(
//                    null, null,
//                    null, false, false,
//                    null, null,
//                    null, false),
//                buildContactDetailsWrapper(
//                    null, null,
//                    null, false, false,
//                    null, null,
//                    null, false),
//                false, false
//            ),
//            Arguments.of("6. Applicant Solicitor change, Respondent not represented.",
//                buildContactDetailsWrapper(
//                    "Old AppSol Name", "Old AppSol Firm",
//                    "OldAppSol@email.com", true, false,
//                    null, null,
//                    null, false),
//                buildContactDetailsWrapper(
//                    "New AppSol Name", "New AppSol Firm",
//                    "NewAppSol@email.com", true, false,
//                    null, null,
//                    null, false),
//                true, false
//            ),
//            Arguments.of("7. Respondent Solicitor change, Applicant not represented.",
//                buildContactDetailsWrapper(
//                    null, null,
//                    null, false, false,
//                    "Old RespSol Name", "Old RespSol Firm",
//                    "OldRespSol@email.com", true),
//                buildContactDetailsWrapper(
//                    null, null,
//                    null, false, false,
//                    "New RespSol Name", "New RespSol Firm",
//                    "NewRespSol@email.com", true),
//                false, true
//            ),
//            Arguments.of("9. Only applicant solicitor to unpresented, Respondent not represented.",
//                buildContactDetailsWrapper(
//                    null, null,
//                    null, false, true,
//                    "Old RespSol Name", "Old RespSol Firm",
//                    "OldRespSol@email.com", false),
//                buildContactDetailsWrapper(
//                    null, null,
//                    null, false, true,
//                    "New RespSol Name", "New RespSol Firm",
//                    "NewRespSol@email.com", false),
//                false, true
//            ),
//            Arguments.of("10. Only Applicant solicitor addded",
//                buildContactDetailsWrapper(
//                    null, null,
//                    null, false, true,
//                    null, null,
//                    null, false),
//                buildContactDetailsWrapper(
//                    "New AppSol Name", "New AppSol Firm",
//                    "NewAppSol@email.com", true, false,
//                    null, null,
//                    null, false),
//                true, false
//            ),
//            Arguments.of("11. No change Applicant Solicitor, Respondent added.",
//                buildContactDetailsWrapper(
//                    null, null,
//                    null, false, true,
//                    null, null,
//                    null, false),
//                buildContactDetailsWrapper(
//                    null, null,
//                    null, false, true,
//                    "New RespSol Name", "New RespSol Firm",
//                    "NewRespSol@email.com", true),
//                false, true
//            ),
//            Arguments.of("12. Only RespSol change to rep.",
//                buildContactDetailsWrapper(
//                    "Old AppSol Name", "Old AppSol Firm",
//                    "OldAppSol@email.com", true, false,
//                    null, null,
//                    null, false),
//                buildContactDetailsWrapper(
//                    "Old AppSol Name", "Old AppSol Firm",
//                    "OldAppSol@email.com", true, false,
//                    "Old RespSol Name", "Old RespSol Firm",
//                    "OldRespSol@email.com", false),
//                false, true
//            ),
//            Arguments.of("13. Only RespSol to Unrep.",
//                buildContactDetailsWrapper(
//                    null, null,
//                    null, false, true,
//                    "Old RespSol Name", "Old RespSol Firm",
//                    "OldRespSol@email.com", false),
//                buildContactDetailsWrapper(
//                    "Old AppSol Name", "Old AppSol Firm",
//                    "OldAppSol@email.com", true, false,
//                    "Old RespSol Name", "Old RespSol Firm",
//                    "OldRespSol@email.com", false),
//                true, false
//            ),
//            Arguments.of("14. Only AppSol change, Respondent not represented.",
//                buildContactDetailsWrapper(
//                    "Old AppSol Name", "Old AppSol Firm",
//                    "OldAppSol@email.com", true, false,
//                    null, null,
//                    null, false),
//                buildContactDetailsWrapper(
//                    "New AppSol Name", "New AppSol Firm",
//                    "NewppSol@email.com", true, false,
//                    null, null,
//                    null, false),
//                true, false
//            ),
//            Arguments.of("15. Only RespSol change, Applicant not represented.",
//                buildContactDetailsWrapper(
//                    null, null,
//                    null, false, true,
//                    "Old RespSol Name", "Old RespSol Firm",
//                    "OldRespSol@email.com", false),
//                buildContactDetailsWrapper(
//                    null, null,
//                    null, false, true,
//                    "New RespSol Name", "New RespSol Firm",
//                    "NewRespSol@email.com", false),
//                false, true
//            ),
//            Arguments.of("16. No change Applicant Solicitor, Respondent not represented.",
//                buildContactDetailsWrapper(
//                    "Old AppSol Name", "Old AppSol Firm",
//                    "OldAppSol@email.com", true, false,
//                    null, null,
//                    null, false),
//                buildContactDetailsWrapper(
//                    "Old AppSol Name", "Old AppSol Firm",
//                    "OldAppSol@email.com", true, false,
//                    null, null,
//                    null, false),
//                false, false
//            ),
//            Arguments.of("17. Only Applicant Change by email case-sensitivity - FinremCallbackRequest normalizes emails",
//                buildContactDetailsWrapper(
//                    "old appsol name", "old appsol firm",
//                    "OldAppSol@email.com", true, false,
//                    null, null,
//                    null, false),
//                buildContactDetailsWrapper(
//                    "OLD APPSOL NAME", "OLD APPSOL FIRM",
//                    "OLDAPPSOL@email.com", true,
//                    false, null, null,
//                    null, false),
//                false, false
//            ),
//            Arguments.of("18. Only Respondent Change by email",
//                buildContactDetailsWrapper(
//                    "Old AppSol Name", "Old AppSol Firm",
//                    "OldAppSol@email.com", true,
//                    true, "Old RespSol Name", "Old RespSol Firm",
//                    "OldRespSol@email.com", false),
//                buildContactDetailsWrapper(
//                    "Old AppSol Name", "Old AppSol Firm",
//                    "OldAppSol@email.com", true, true,
//                    "OLD RESPSOL NAME", "OLD RESPSOL FIRM",
//                    "newrespsol@email.com", false),
//                false, true
//            )
//        );
//    }
//
//    /**
//     * Tests various scenarios of solicitor change and their impact on auto assignment of solicitor's access.
//     *
//     * @param beforeWrapper - Contact details wrapper representing the application and respondent solicitor details before the update
//     * @param afterWrapper - Contact details wrapper representing the application and respondent solicitor details after the update
//     * @param expectApplicantAutoAssignAccess - boolean flag indicating whether an applicant auto assignment of solicitor's access is expected
//     * @param expectRespondentAutoAssignAccess - boolean flag indicating whether a respondent auto assignment of solicitor's access is expected
//     */
//    @ParameterizedTest
//    @MethodSource
//    void testSolicitorChangeScenarios(String scenarioName, ContactDetailsWrapper beforeWrapper, ContactDetailsWrapper afterWrapper,
//                                      boolean expectApplicantAutoAssignAccess, boolean expectRespondentAutoAssignAccess) {
//
//        FinremCaseDetails beforeDetails = buildCaseDetails(beforeWrapper);
//        FinremCaseDetails afterDetails = buildCaseDetails(afterWrapper);
//
//        lenient().when(retryExecutor.supplyWithRetryWithHandler(any(), anyString(), anyString(), any()))
//            .thenReturn(Optional.of(true));
//        lenient().when(updateContactDetailsNotificationService.prepareNocEmailToLitigantSolicitor(any()))
//            .thenReturn(mock(SendCorrespondenceEvent.class));
//        lenient().when(updateContactDetailsNotificationService.prepareNocEmailToLitigantSolicitor(any()))
//            .thenReturn(mock(SendCorrespondenceEvent.class));
//        FinremCallbackRequest callbackRequest = FinremCallbackRequestFactory.from(Long.valueOf(CASE_ID),
//            beforeDetails.getData(), afterDetails.getData());
//
//        // Act
//        handler.handle(callbackRequest, AUTH_TOKEN);
//
//        // Verify
//        if (expectApplicantAutoAssignAccess) {
//            verify(retryExecutor).supplyWithRetryWithHandler(
//                any(),
//                eq("Update Contact Details - granting applicant solicitor"),
//                eq(CASE_ID),
//                any(RetryErrorHandler.class));
//        }
//
//        if (expectRespondentAutoAssignAccess) {
//            verify(retryExecutor).supplyWithRetryWithHandler(
//                any(),
//                eq("Update Contact Details - granting respondent solicitor"),
//                eq(CASE_ID),
//                any(RetryErrorHandler.class));
//        }
//
//        if (!expectApplicantAutoAssignAccess && !expectRespondentAutoAssignAccess) {
//            verify(retryExecutor, never()).supplyWithRetryWithHandler(any(), anyString(), anyString(), any());
//        }
//    }
//
//    static FinremCaseDetails buildCaseDetails(ContactDetailsWrapper wrapper) {
//        FinremCaseData data = FinremCaseData.builder()
//            .contactDetailsWrapper(wrapper)
//            .issueDate(LocalDate.now())
//            .build();
//        return FinremCaseDetails.builder()
//            .data(data)
//            .id(CASE_ID_IN_LONG)
//            .state(State.APPLICATION_ISSUED)
//            .build();
//    }
//
//    static Address buildAddress() {
//        return Address.builder()
//            .addressLine1("AddressLine1")
//            .addressLine2("AddressLine2")
//            .addressLine3("AddressLine3")
//            .county("County")
//            .country("Country")
//            .postTown("Town")
//            .postCode("EC1 3AS")
//            .build();
//    }
//
//    static ContactDetailsWrapper buildContactDetailsWrapper(
//        String applicantSolicitorName,
//        String applicantSolicitorFirm,
//        String applicantSolicitorEmail,
//        boolean isCurrentUserApplicantSolicitor,
//        boolean isUpdateIncludesRepresentativeChange,
//        String respondentSolicitorName,
//        String respondentSolicitorFirm,
//        String respondentSolicitorEmail,
//        boolean isCurrentUserRespondentSolicitor) {
//
//        ContactDetailsWrapper.ContactDetailsWrapperBuilder builder = ContactDetailsWrapper.builder()
//            .applicantSolicitorName(applicantSolicitorName)
//            .applicantSolicitorFirm(applicantSolicitorFirm)
//            .applicantSolicitorEmail(applicantSolicitorEmail)
//            .applicantSolicitorAddress(buildAddress())
//            .applicantRepresented(applicantSolicitorEmail != null ? YesOrNo.YES : YesOrNo.NO)
//            .currentUserIsApplicantSolicitor(isCurrentUserApplicantSolicitor ? YesOrNo.YES : YesOrNo.NO)
//            .updateIncludesRepresentativeChange(isUpdateIncludesRepresentativeChange ? YesOrNo.YES : YesOrNo.NO)
//            .respondentSolicitorName(respondentSolicitorName)
//            .respondentSolicitorFirm(respondentSolicitorFirm)
//            .respondentSolicitorEmail(respondentSolicitorEmail)
//            .respondentSolicitorAddress(buildAddress())
//            .contestedRespondentRepresented(respondentSolicitorEmail != null ? YesOrNo.YES : YesOrNo.NO)
//            .consentedRespondentRepresented(respondentSolicitorEmail != null ? YesOrNo.YES : YesOrNo.NO)
//            .currentUserIsRespondentSolicitor(isCurrentUserRespondentSolicitor ? YesOrNo.YES : YesOrNo.NO);
//        return builder.build();
//    }
}
