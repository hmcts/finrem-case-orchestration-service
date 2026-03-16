package uk.gov.hmcts.reform.finrem.caseorchestration.handler.stoprepresentingclient;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.EnumSource;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.util.function.ThrowingSupplier;
import uk.gov.hmcts.reform.finrem.caseorchestration.FinremCallbackRequestFactory;
import uk.gov.hmcts.reform.finrem.caseorchestration.controllers.GenericAboutToStartOrSubmitCallbackResponse;
import uk.gov.hmcts.reform.finrem.caseorchestration.handler.FinremCallbackRequest;
import uk.gov.hmcts.reform.finrem.caseorchestration.mapper.FinremCaseDetailsMapper;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.BarristerChange;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.BarristerParty;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.FinremCaseData;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.NoticeOfChangeParty;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.wrapper.ContactDetailsWrapper;
import uk.gov.hmcts.reform.finrem.caseorchestration.notifications.notifiers.SendCorrespondenceEvent;
import uk.gov.hmcts.reform.finrem.caseorchestration.notifications.notifiers.SendCorrespondenceEventEnvelop;
import uk.gov.hmcts.reform.finrem.caseorchestration.service.FeatureToggleService;
import uk.gov.hmcts.reform.finrem.caseorchestration.service.stoprepresentingclient.StopRepresentingClientInfo;
import uk.gov.hmcts.reform.finrem.caseorchestration.service.stoprepresentingclient.StopRepresentingClientService;
import uk.gov.hmcts.reform.finrem.caseorchestration.utils.retry.RetryExecutor;
import uk.gov.hmcts.reform.finrem.caseorchestration.utils.retry.ThrowingRunnable;

import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.doNothing;
import static org.mockito.Mockito.lenient;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoMoreInteractions;
import static org.mockito.Mockito.when;
import static uk.gov.hmcts.reform.finrem.caseorchestration.TestConstants.AUTH_TOKEN;
import static uk.gov.hmcts.reform.finrem.caseorchestration.TestConstants.CASE_ID;
import static uk.gov.hmcts.reform.finrem.caseorchestration.TestConstants.CASE_ID_IN_LONG;
import static uk.gov.hmcts.reform.finrem.caseorchestration.ccd.callback.CallbackType.SUBMITTED;
import static uk.gov.hmcts.reform.finrem.caseorchestration.model.EventType.STOP_REPRESENTING_CLIENT;
import static uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.CaseType.CONSENTED;
import static uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.CaseType.CONTESTED;
import static uk.gov.hmcts.reform.finrem.caseorchestration.test.Assertions.assertCanHandle;

@ExtendWith(MockitoExtension.class)
class StopRepresentingClientSubmittedHandlerTest {

    private StopRepresentingClientSubmittedHandler underTest;
    @Mock
    private FinremCaseDetailsMapper finremCaseDetailsMapper;
    @Mock
    private StopRepresentingClientService stopRepresentingClientService;
    @Mock
    private FeatureToggleService featureToggleService;
    @Mock
    private ApplicationEventPublisher applicationEventPublisher;
    @Mock
    private RetryExecutor retryExecutor;

    @BeforeEach
    public void setup() {
        underTest = new StopRepresentingClientSubmittedHandler(finremCaseDetailsMapper, stopRepresentingClientService,
            featureToggleService, applicationEventPublisher, retryExecutor);
        lenient().when(featureToggleService.isExui3990WorkaroundEnabled()).thenReturn(true);
        lenient().when(stopRepresentingClientService.getToBeRevokedBarristers(any(StopRepresentingClientInfo.class),
            any(BarristerParty.class))).thenReturn(mock(BarristerChange.class));
    }

    @Test
    void testCanHandle() {
        assertCanHandle(underTest,
            Arguments.of(SUBMITTED, CONTESTED, STOP_REPRESENTING_CLIENT),
            Arguments.of(SUBMITTED, CONSENTED, STOP_REPRESENTING_CLIENT));
    }

    @Test
    void givenAnyCase_whenHandled_thenReturnConfirmationMessages() {
        FinremCallbackRequest request = FinremCallbackRequestFactory.from();
        GenericAboutToStartOrSubmitCallbackResponse<FinremCaseData> response = underTest.handle(request, AUTH_TOKEN);
        assertThat(response.getConfirmationBody()).isEqualTo("<ul><li><h2>Your changes will be applied shortly.</h2></li></ul>");
        assertThat(response.getConfirmationHeader()).isEqualTo("# Notice of change request submitted");
    }

    @Nested
    class RevokeApplicantSolicitorOrRespondentSolicitorTests {
        @ParameterizedTest
        @EnumSource(NoticeOfChangeParty.class)
        void shouldPerformCleanUpAfterNocWorkflow_whenHandled(NoticeOfChangeParty party) throws Exception {
            // ---------- Given ----------
            FinremCaseData caseData = FinremCaseData.builder()
                .contactDetailsWrapper(
                    ContactDetailsWrapper.builder()
                        .nocParty(party)
                        .build()
                )
                .build();

            FinremCallbackRequest request =
                FinremCallbackRequestFactory.from(CASE_ID_IN_LONG, caseData);

            StopRepresentingClientService.LitigantRevocation revocation =
                new StopRepresentingClientService.LitigantRevocation(true, false);

            // Mock revocation step
            when(retryExecutor.supplyWithRetrySuppressException(
                any(ThrowingSupplier.class),
                eq("revoking %s access".formatted(party.getValue())),
                eq(CASE_ID)
            )).thenReturn(Optional.of(revocation));

            // Capture clean-up runnable
            ArgumentCaptor<ThrowingRunnable> cleanupCaptor = ArgumentCaptor.forClass(ThrowingRunnable.class);

            doNothing().when(retryExecutor).runWithRetrySuppressException(
                cleanupCaptor.capture(),
                eq("cleaning up after noc workflow"),
                eq(CASE_ID)
            );

            // ---------- When ----------
            underTest.handle(request, AUTH_TOKEN);

            // Execute captured clean-up logic
            cleanupCaptor.getValue().run();

            // ---------- Then ----------
            // Verify correct data was passed to clean-up method
            ArgumentCaptor<StopRepresentingClientInfo> infoCaptor =
                ArgumentCaptor.forClass(StopRepresentingClientInfo.class);

            verify(stopRepresentingClientService)
                .performCleanUpAfterNocWorkflow(infoCaptor.capture());

            assertThat(infoCaptor.getValue())
                .extracting(
                    StopRepresentingClientInfo::getFinremCaseData,
                    StopRepresentingClientInfo::getCaseId
                )
                .containsExactly(caseData, CASE_ID_IN_LONG);
        }

        @ParameterizedTest
        @EnumSource(NoticeOfChangeParty.class)
        void givenNoLitigantGetRevoked_whenHandled_thenShouldNotPerformCleanUpAfterNocWorkflow(NoticeOfChangeParty party) {
            // ---------- Given ----------
            FinremCaseData caseData = FinremCaseData.builder()
                .contactDetailsWrapper(
                    ContactDetailsWrapper.builder()
                        .nocParty(party)
                        .build()
                )
                .build();

            FinremCallbackRequest request =
                FinremCallbackRequestFactory.from(CASE_ID_IN_LONG, caseData);

            StopRepresentingClientService.LitigantRevocation revocation =
                new StopRepresentingClientService.LitigantRevocation(false, false);

            // Mock revocation result
            when(retryExecutor.supplyWithRetrySuppressException(
                any(ThrowingSupplier.class),
                eq("revoking %s access".formatted(party.getValue())),
                eq(CASE_ID)
            )).thenReturn(Optional.of(revocation));

            // ---------- When ----------
            underTest.handle(request, AUTH_TOKEN);

            // ---------- Then ----------
            // Clean-up must NOT be triggered via retryExecutor
            verify(retryExecutor, never())
                .runWithRetrySuppressException(
                    any(ThrowingRunnable.class),
                    eq("cleaning up after noc workflow"),
                    eq(CASE_ID)
                );

            // Clean-up service must NOT be called directly
            verify(stopRepresentingClientService, never())
                .performCleanUpAfterNocWorkflow(any(StopRepresentingClientInfo.class));
        }

        @ParameterizedTest
        @EnumSource(NoticeOfChangeParty.class)
        void shouldNotifyLitigant_whenHandled(NoticeOfChangeParty party) throws Exception {
            // ---------- Given ----------
            FinremCaseData caseData = FinremCaseData.builder()
                .contactDetailsWrapper(
                    ContactDetailsWrapper.builder()
                        .nocParty(party)
                        .build()
                )
                .build();

            StopRepresentingClientService.LitigantRevocation revocation =
                new StopRepresentingClientService.LitigantRevocation(true, false);

            // Mock retry to return a successful revocation result
            when(retryExecutor.supplyWithRetrySuppressException(
                any(ThrowingSupplier.class),
                eq("revoking %s access".formatted(party.getValue())),
                eq(CASE_ID)
            )).thenReturn(Optional.of(revocation));

            SendCorrespondenceEventEnvelop envelope = mock(SendCorrespondenceEventEnvelop.class);
            SendCorrespondenceEvent event = mock(SendCorrespondenceEvent.class);

            when(envelope.getDescription()).thenReturn("litigant notification");
            when(envelope.getEvent()).thenReturn(event);

            // Mock service to return notification event
            when(stopRepresentingClientService.prepareLitigantRevocationNotificationEvents(
                eq(revocation),
                any(StopRepresentingClientInfo.class)
            )).thenReturn(List.of(envelope));

            FinremCallbackRequest request =
                FinremCallbackRequestFactory.from(CASE_ID_IN_LONG, caseData);

            // ---------- When ----------
            underTest.handle(request, AUTH_TOKEN);

            // ---------- Then ----------
            // Capture the info passed into the notification preparation method
            ArgumentCaptor<StopRepresentingClientInfo> infoCaptor =
                ArgumentCaptor.forClass(StopRepresentingClientInfo.class);

            verify(stopRepresentingClientService)
                .prepareLitigantRevocationNotificationEvents(eq(revocation), infoCaptor.capture());

            // Assert correct case data and case ID were passed
            assertThat(infoCaptor.getValue())
                .extracting(
                    StopRepresentingClientInfo::getFinremCaseData,
                    StopRepresentingClientInfo::getCaseId
                )
                .containsExactly(caseData, CASE_ID_IN_LONG);

            // Capture the runnable used for publishing events
            ArgumentCaptor<ThrowingRunnable> runnableCaptor =
                ArgumentCaptor.forClass(ThrowingRunnable.class);

            verify(retryExecutor)
                .runWithRetrySuppressException(
                    runnableCaptor.capture(),
                    eq("litigant notification"),
                    eq(CASE_ID)
                );

            // Execute the captured runnable to verify event publishing
            runnableCaptor.getValue().run();

            // Verify event was published correctly
            verify(applicationEventPublisher).publishEvent(event);

            // Ensure no additional interactions occurred
            verifyNoMoreInteractions(applicationEventPublisher);
        }

        @ParameterizedTest
        @EnumSource(NoticeOfChangeParty.class)
        void givenNoLitigantNotificationPrepared_whenHandled_thenShouldNotNotifyLitigant(NoticeOfChangeParty party) {
            // ---------- Given ----------
            FinremCaseData caseData = FinremCaseData.builder()
                .contactDetailsWrapper(
                    ContactDetailsWrapper.builder()
                        .nocParty(party)
                        .build()
                )
                .build();

            StopRepresentingClientService.LitigantRevocation revocation =
                new StopRepresentingClientService.LitigantRevocation(true, false);

            when(retryExecutor.supplyWithRetrySuppressException(
                any(ThrowingSupplier.class),
                eq("revoking %s access".formatted(party.getValue())),
                eq(CASE_ID)
            )).thenReturn(Optional.of(revocation));

            when(stopRepresentingClientService.prepareLitigantRevocationNotificationEvents(
                eq(revocation),
                any(StopRepresentingClientInfo.class)
            )).thenReturn(List.of());

            when(stopRepresentingClientService
                .getToBeRevokedIntervenerSolicitors(any(StopRepresentingClientInfo.class)))
                .thenReturn(List.of());

            FinremCallbackRequest request =
                FinremCallbackRequestFactory.from(CASE_ID_IN_LONG, caseData);

            // ---------- When ----------
            underTest.handle(request, AUTH_TOKEN);

            // ---------- Then ----------
            // Capture and verify info passed to service
            ArgumentCaptor<StopRepresentingClientInfo> infoCaptor =
                ArgumentCaptor.forClass(StopRepresentingClientInfo.class);

            verify(stopRepresentingClientService)
                .prepareLitigantRevocationNotificationEvents(eq(revocation), infoCaptor.capture());

            assertThat(infoCaptor.getValue())
                .extracting(
                    StopRepresentingClientInfo::getFinremCaseData,
                    StopRepresentingClientInfo::getCaseId
                )
                .containsExactly(caseData, CASE_ID_IN_LONG);

            // Verify retry interactions
            verify(retryExecutor).supplyWithRetrySuppressException(
                any(ThrowingSupplier.class),
                eq("revoking %s access".formatted(party.getValue())),
                eq(CASE_ID)
            );

            verify(retryExecutor).runWithRetrySuppressException(
                any(ThrowingRunnable.class),
                eq("cleaning up after noc workflow"),
                eq(CASE_ID)
            );

            verify(retryExecutor).supplyWithRetrySuppressException(
                any(ThrowingSupplier.class),
                eq("preparing litigant letter notifications"),
                eq(CASE_ID)
            );

            verifyNoMoreInteractions(retryExecutor);

            verify(applicationEventPublisher, never())
                .publishEvent(any(SendCorrespondenceEvent.class));
        }
    }
}
