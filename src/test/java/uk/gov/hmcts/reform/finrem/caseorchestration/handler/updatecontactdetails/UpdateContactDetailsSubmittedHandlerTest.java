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

        @ParameterizedTest
        @CsvSource({
            "true,true",
            "true,false",
            "false,true"
        })
        void givenRespondentSolicitorChanged_whenUnableToGrantOrRevoke_thenErrorsPopulatedToConfirmation(boolean failGrant, boolean failRevoke) {
            FinremCaseData finremCaseData = spy(FinremCaseData.builder().issueDate(LocalDate.now()).build());
            when(finremCaseData.getRespSolicitorEmailIfRepresented()).thenReturn("new@email.com");
            FinremCaseData finremCaseDataBefore = spy(FinremCaseData.builder().build());
            when(finremCaseDataBefore.getRespSolicitorEmailIfRepresented()).thenReturn("old@email.com");

            FinremCallbackRequest callbackRequest = spy(FinremCallbackRequestFactory.from(Long.valueOf(CASE_ID), finremCaseDataBefore,
                finremCaseData));
            when(callbackRequest.hasApplicantSolicitorChanged()).thenReturn(false);
            when(callbackRequest.hasRespondentSolicitorChanged()).thenReturn(true);
            when(updateContactDetailsNotificationService.prepareNocEmailToLitigantSolicitor(callbackRequest.getCaseDetails()))
                .thenReturn(mock(SendCorrespondenceEvent.class));

            when(retryExecutor.supplyWithRetryWithHandler(any(ThrowingSupplier.class), eq("Update Contact Details - granting respondent solicitor"),
                eq(CASE_ID), any())).thenAnswer(invocation -> {
                    if (failGrant) {
                        String actionName = invocation.getArgument(1);
                        String caseId = invocation.getArgument(2);
                        RetryErrorHandler errorHandler = invocation.getArgument(3);
                        errorHandler.handle(new RuntimeException("fail"), actionName, caseId);
                    }
                    return Optional.of(Boolean.TRUE);
                });

            when(retryExecutor.supplyWithRetryWithHandler(any(ThrowingSupplier.class), eq("Update Contact Details - revoking respondent solicitor"),
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

            String grantMsg = "There was a problem granting access to respondent solicitor (new@email.com). Please grant access manually.";
            String revokeMsg = "There was a problem revoking access to respondent solicitor (old@email.com). Please revoke access manually.";

            assertAll(
                () -> verify(retryExecutor).supplyWithRetryWithHandler(
                    any(ThrowingSupplier.class),
                    eq("Update Contact Details - granting respondent solicitor"),
                    eq(CASE_ID),
                    any(RetryErrorHandler.class)
                ),
                () -> verify(retryExecutor).supplyWithRetryWithHandler(
                    any(ThrowingSupplier.class),
                    eq("Update Contact Details - revoking respondent solicitor"),
                    eq(CASE_ID),
                    any(RetryErrorHandler.class)
                ),
                () -> assertThat(header).contains("Contact details updated with errors"),
                () -> assertCondition(body, grantMsg, failGrant),
                () -> assertCondition(body, revokeMsg, failRevoke)
            );
        }

        @Test
        void givenRespondentSolicitorChanged_whenHandled_thenGrantAndRevokeRespondentSolicitorAndNotifyParties() {
            FinremCaseData finremCaseData = spy(FinremCaseData.builder().issueDate(LocalDate.now()).build());
            when(finremCaseData.getRespSolicitorEmailIfRepresented()).thenReturn("new@email.com");
            FinremCaseData finremCaseDataBefore = spy(FinremCaseData.builder().build());
            when(finremCaseDataBefore.getRespSolicitorEmailIfRepresented()).thenReturn("old@email.com");

            FinremCallbackRequest callbackRequest = spy(FinremCallbackRequestFactory.from(Long.valueOf(CASE_ID), finremCaseDataBefore,
                finremCaseData));
            when(callbackRequest.hasApplicantSolicitorChanged()).thenReturn(false);
            when(callbackRequest.hasRespondentSolicitorChanged()).thenReturn(true);

            when(updateContactDetailsNotificationService.prepareNocEmailToLitigantSolicitor(callbackRequest.getCaseDetails()))
                .thenReturn(mock(SendCorrespondenceEvent.class));
            // Simulate grant/revoke respondent solicitor success
            when(retryExecutor.supplyWithRetryWithHandler(any(ThrowingSupplier.class),
                eq("Update Contact Details - granting respondent solicitor"),
                eq(CASE_ID), any())).thenAnswer(invocation -> Optional.of(Boolean.TRUE));
            when(retryExecutor.supplyWithRetryWithHandler(any(ThrowingSupplier.class),
                eq("Update Contact Details - revoking respondent solicitor"),
                eq(CASE_ID), any())).thenAnswer(invocation -> Optional.of(Boolean.TRUE));

            // Act
            var response = handler.handle(callbackRequest, AUTH_TOKEN);

            // Verify
            ArgumentCaptor<ThrowingSupplier<Boolean>> grantRespondentSolicitorCaptor = getThrowingSupplierCaptor();
            ArgumentCaptor<ThrowingSupplier<Boolean>> revokeRespondentSolicitorCaptor = getThrowingSupplierCaptor();

            assertAll(
                // to verify execution of granting respondent solicitor
                () -> verify(retryExecutor).supplyWithRetryWithHandler(
                    grantRespondentSolicitorCaptor.capture(),
                    eq("Update Contact Details - granting respondent solicitor"),
                    eq(CASE_ID),
                    any(RetryErrorHandler.class)),
                // to verify assignPartiesAccessService.grantRespondentSolicitor was invoked
                () -> {
                    assertTrue(grantRespondentSolicitorCaptor.getValue().get());
                    verify(assignPartiesAccessService).grantRespondentSolicitor(finremCaseData);
                },
                // to verify execution of revoking respondent solicitor
                () -> verify(retryExecutor).supplyWithRetryWithHandler(
                    revokeRespondentSolicitorCaptor.capture(),
                    eq("Update Contact Details - revoking respondent solicitor"),
                    eq(CASE_ID),
                    any(RetryErrorHandler.class)),
                // to verify assignPartiesAccessService.revokeRespondentSolicitor was invoked
                () -> {
                    assertTrue(revokeRespondentSolicitorCaptor.getValue().get());
                    verify(assignPartiesAccessService).revokeRespondentSolicitor(finremCaseDataBefore);
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

    }

    @Test
    void givenRespondentSolicitorChangedButApplicationNotIssued_whenHandled_thenNoCaseAssignmentAndNoNotification() {
        FinremCaseData finremCaseData = spy(FinremCaseData.builder().build());
        when(finremCaseData.getRespSolicitorEmailIfRepresented()).thenReturn("new@email.com");
        FinremCaseData finremCaseDataBefore = spy(FinremCaseData.builder().build());
        when(finremCaseDataBefore.getRespSolicitorEmailIfRepresented()).thenReturn("old@email.com");

        FinremCallbackRequest callbackRequest = spy(FinremCallbackRequestFactory.from(Long.valueOf(CASE_ID), finremCaseDataBefore,
            finremCaseData));
        when(callbackRequest.hasApplicantSolicitorChanged()).thenReturn(false);
        when(callbackRequest.hasRespondentSolicitorChanged()).thenReturn(true);

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
}
