package uk.gov.hmcts.reform.finrem.caseorchestration.handler.stoprepresentingclient;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.EnumSource;
import org.junit.jupiter.params.provider.MethodSource;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.util.function.ThrowingSupplier;
import uk.gov.hmcts.reform.finrem.caseorchestration.FinremCallbackRequestFactory;
import uk.gov.hmcts.reform.finrem.caseorchestration.TestSetUpUtils;
import uk.gov.hmcts.reform.finrem.caseorchestration.controllers.GenericAboutToStartOrSubmitCallbackResponse;
import uk.gov.hmcts.reform.finrem.caseorchestration.handler.FinremCallbackRequest;
import uk.gov.hmcts.reform.finrem.caseorchestration.mapper.FinremCaseDetailsMapper;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.BarristerChange;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.Barrister;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.BarristerParty;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.FinremCaseData;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.NoticeOfChangeParty;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.wrapper.ContactDetailsWrapper;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.wrapper.IntervenerOne;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.wrapper.IntervenerThree;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.intervener.IntervenerType;
import uk.gov.hmcts.reform.finrem.caseorchestration.notifications.notifiers.SendCorrespondenceEvent;
import uk.gov.hmcts.reform.finrem.caseorchestration.notifications.notifiers.SendCorrespondenceEventEnvelop;
import uk.gov.hmcts.reform.finrem.caseorchestration.service.FeatureToggleService;
import uk.gov.hmcts.reform.finrem.caseorchestration.service.stoprepresentingclient.StopRepresentingClientInfo;
import uk.gov.hmcts.reform.finrem.caseorchestration.service.stoprepresentingclient.StopRepresentingClientService;
import uk.gov.hmcts.reform.finrem.caseorchestration.utils.retry.RetryExecutor;
import uk.gov.hmcts.reform.finrem.caseorchestration.utils.retry.ThrowingRunnable;

import java.util.Arrays;
import java.util.List;
import java.util.Optional;
import java.util.Set;
import java.util.stream.Stream;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.argThat;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.doAnswer;
import static org.mockito.Mockito.doNothing;
import static org.mockito.Mockito.lenient;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoMoreInteractions;
import static org.mockito.Mockito.when;
import static uk.gov.hmcts.reform.finrem.caseorchestration.TestConstants.AUTH_TOKEN;
import static uk.gov.hmcts.reform.finrem.caseorchestration.TestConstants.CASE_ID;
import static uk.gov.hmcts.reform.finrem.caseorchestration.TestConstants.CASE_ID_IN_LONG;
import static uk.gov.hmcts.reform.finrem.caseorchestration.TestSetUpUtils.getSafely;
import static uk.gov.hmcts.reform.finrem.caseorchestration.TestSetUpUtils.getThrowingRunnableCaptor;
import static uk.gov.hmcts.reform.finrem.caseorchestration.TestSetUpUtils.getThrowingSupplierCaptor;
import static uk.gov.hmcts.reform.finrem.caseorchestration.TestSetUpUtils.runSafely;
import static uk.gov.hmcts.reform.finrem.caseorchestration.ccd.callback.CallbackType.SUBMITTED;
import static uk.gov.hmcts.reform.finrem.caseorchestration.model.EventType.STOP_REPRESENTING_CLIENT;
import static uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.CaseType.CONSENTED;
import static uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.CaseType.CONTESTED;
import static uk.gov.hmcts.reform.finrem.caseorchestration.test.Assertions.assertCanHandle;

@ExtendWith(MockitoExtension.class)
class StopRepresentingClientSubmittedHandlerTest {

    public static ArgumentCaptor<StopRepresentingClientInfo> getStopRepresentingClientInfoCaptor() {
        return ArgumentCaptor.forClass(StopRepresentingClientInfo.class);
    }

    private static FinremCaseData buildFinremCaseData(NoticeOfChangeParty party) {
        return FinremCaseData.builder()
            .contactDetailsWrapper(
                ContactDetailsWrapper.builder()
                    .nocParty(party)
                    .build()
            )
            .build();
    }

    private static void verifyStopRepresentingClientInfoCaptured(ArgumentCaptor<StopRepresentingClientInfo> captor,
                                                                 FinremCaseData caseData) {
        captor.getAllValues().forEach(a ->
            assertThat(a)
                .extracting(
                    StopRepresentingClientInfo::getFinremCaseData,
                    StopRepresentingClientInfo::getCaseId
                )
                .containsExactly(caseData, CASE_ID_IN_LONG)
        );
    }

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
    void setup() {
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
    class RevokeBarristersTests {

        @ParameterizedTest
        @EnumSource(value = BarristerParty.class)
        void shouldRevokeBarristerAndNotifyBarrister_whenHandled(BarristerParty barristerParty) {
            // ---------- Given ----------
            // Mock revocation step
            mockRevokeApplicantSolicitorOrRespondentSolicitor();
            mockPreparingLitigantRevocationLetterNotification();

            BarristerChange barristerChange = mock(BarristerChange.class);
            when(barristerChange.getRemoved()).thenReturn(Set.of(mock(Barrister.class)));
            when(stopRepresentingClientService.getToBeRevokedBarristers(any(StopRepresentingClientInfo.class),
                eq(barristerParty))).thenReturn(barristerChange);

            SendCorrespondenceEventEnvelop eventEnvelop = mock(SendCorrespondenceEventEnvelop.class);
            SendCorrespondenceEvent event = mock(SendCorrespondenceEvent.class);
            when(eventEnvelop.getEvent()).thenReturn(event);
            when(eventEnvelop.getDescription()).thenReturn("whatever barrister notification");

            when(retryExecutor.supplyWithRetrySuppressException(any(ThrowingSupplier.class),
                eq("revoking %s barrister access".formatted(barristerParty.getValue())), eq(CASE_ID)))
                .thenReturn(Optional.of(List.of(eventEnvelop)));

            FinremCaseData caseData = buildFinremCaseData(NoticeOfChangeParty.APPLICANT);

            // ---------- When ----------
            underTest.handle(FinremCallbackRequestFactory.from(CASE_ID_IN_LONG, caseData), AUTH_TOKEN);

            // ---------- Then ----------
            ArgumentCaptor<StopRepresentingClientInfo> infoCaptor = getStopRepresentingClientInfoCaptor();
            verify(stopRepresentingClientService).getToBeRevokedBarristers(infoCaptor.capture(), eq(barristerParty));
            verifyStopRepresentingClientInfoCaptured(infoCaptor, caseData);

            // Verify revokeBarristers called
            ArgumentCaptor<ThrowingSupplier<SendCorrespondenceEventEnvelop>> throwingSupplierCaptor = getThrowingSupplierCaptor();
            verify(retryExecutor).supplyWithRetrySuppressException(throwingSupplierCaptor.capture(),
                eq("revoking %s barrister access".formatted(barristerParty.getValue())), eq(CASE_ID));
            getSafely(throwingSupplierCaptor.getValue());
            ArgumentCaptor<StopRepresentingClientInfo> infoCaptor2 = getStopRepresentingClientInfoCaptor();
            verify(stopRepresentingClientService).revokeBarristers(infoCaptor2.capture(), eq(barristerChange));
            verifyStopRepresentingClientInfoCaptured(infoCaptor2, caseData);

            // Verify notification event was published correctly
            ArgumentCaptor<ThrowingRunnable> publishEventCaptor = getThrowingRunnableCaptor();
            verify(retryExecutor)
                .runWithRetrySuppressException(
                    publishEventCaptor.capture(),
                    eq("whatever barrister notification"),
                    eq(CASE_ID)
                );
            publishEventCaptor.getAllValues().forEach(TestSetUpUtils::runSafely);
            verifySendCorrespondenceEventPublished(event);
        }

        @Test
        void shouldMultipleRevokeBarristerAndNotifyBarristers_whenHandled() {
            // ---------- Given ----------
            // Mock revocation step
            mockRevokeApplicantSolicitorOrRespondentSolicitor();
            mockPreparingLitigantRevocationLetterNotification();

            BarristerChange applicantBarristerChange = mock(BarristerChange.class);
            when(applicantBarristerChange.getRemoved()).thenReturn(Set.of(mock(Barrister.class)));
            when(stopRepresentingClientService.getToBeRevokedBarristers(any(StopRepresentingClientInfo.class),
                eq(BarristerParty.APPLICANT))).thenReturn(applicantBarristerChange);
            BarristerChange intervenerTwoBarristerChange = mock(BarristerChange.class);
            when(intervenerTwoBarristerChange.getRemoved()).thenReturn(Set.of(mock(Barrister.class), mock(Barrister.class)));
            when(stopRepresentingClientService.getToBeRevokedBarristers(any(StopRepresentingClientInfo.class),
                eq(BarristerParty.INTERVENER2))).thenReturn(intervenerTwoBarristerChange);

            SendCorrespondenceEventEnvelop applicantBarristerEventEnvelop = mock(SendCorrespondenceEventEnvelop.class);
            SendCorrespondenceEvent applicantBarristerEvent = mock(SendCorrespondenceEvent.class);
            when(applicantBarristerEventEnvelop.getEvent()).thenReturn(applicantBarristerEvent);
            when(applicantBarristerEventEnvelop.getDescription()).thenReturn("applicant barrister notification");
            when(retryExecutor.supplyWithRetrySuppressException(any(ThrowingSupplier.class),
                eq("revoking applicant barrister access"), eq(CASE_ID)))
                .thenReturn(Optional.of(List.of(applicantBarristerEventEnvelop)));

            SendCorrespondenceEventEnvelop intvTwoBarristerEventEnvelop = mock(SendCorrespondenceEventEnvelop.class);
            SendCorrespondenceEvent intvTwoBarristerEvent = mock(SendCorrespondenceEvent.class);
            when(intvTwoBarristerEventEnvelop.getEvent()).thenReturn(intvTwoBarristerEvent);
            when(intvTwoBarristerEventEnvelop.getDescription()).thenReturn("intervener2 barrister notification");
            when(retryExecutor.supplyWithRetrySuppressException(any(ThrowingSupplier.class),
                eq("revoking intervener2 barrister access"), eq(CASE_ID)))
                .thenReturn(Optional.of(List.of(intvTwoBarristerEventEnvelop)));

            FinremCaseData caseData = buildFinremCaseData(NoticeOfChangeParty.APPLICANT);

            // ---------- When ----------
            underTest.handle(FinremCallbackRequestFactory.from(CASE_ID_IN_LONG, caseData), AUTH_TOKEN);

            // ---------- Then ----------
            ArgumentCaptor<StopRepresentingClientInfo> infoCaptor = getStopRepresentingClientInfoCaptor();
            verify(stopRepresentingClientService).getToBeRevokedBarristers(infoCaptor.capture(), eq(BarristerParty.APPLICANT));
            verify(stopRepresentingClientService).getToBeRevokedBarristers(infoCaptor.capture(), eq(BarristerParty.INTERVENER2));
            verifyStopRepresentingClientInfoCaptured(infoCaptor, caseData);

            // Verify revokeBarristers called
            ArgumentCaptor<ThrowingSupplier<SendCorrespondenceEventEnvelop>> throwingSupplierCaptor = getThrowingSupplierCaptor();
            verify(retryExecutor, times(2)).supplyWithRetrySuppressException(throwingSupplierCaptor.capture(),
                argThat(a -> List.of(
                    "revoking applicant barrister access",
                    "revoking intervener2 barrister access"
                ).contains(a)), eq(CASE_ID));
            throwingSupplierCaptor.getAllValues().forEach(TestSetUpUtils::getSafely);
            ArgumentCaptor<StopRepresentingClientInfo> infoCaptor2 = getStopRepresentingClientInfoCaptor();
            verify(stopRepresentingClientService).revokeBarristers(infoCaptor2.capture(), eq(applicantBarristerChange));
            verify(stopRepresentingClientService).revokeBarristers(infoCaptor2.capture(), eq(intervenerTwoBarristerChange));
            verifyStopRepresentingClientInfoCaptured(infoCaptor2, caseData);

            // Verify notification event was published correctly
            ArgumentCaptor<ThrowingRunnable> publishEventCaptor = getThrowingRunnableCaptor();
            verify(retryExecutor, times(2))
                .runWithRetrySuppressException(
                    publishEventCaptor.capture(),
                    argThat(a -> List.of(
                        "applicant barrister notification",
                        "intervener2 barrister notification"
                    ).contains(a)),
                    eq(CASE_ID)
                );
            publishEventCaptor.getAllValues().forEach(TestSetUpUtils::runSafely);
            verifySendCorrespondenceEventPublished(applicantBarristerEvent, intvTwoBarristerEvent);
        }
    }

    @Nested
    class RevokeIntervenerSolicitorTests {

        @Test
        void shouldRevokeIntervenerSolicitorAndNotifyIntervenerSolicitor_whenHandled() {
            // ---------- Given ----------
            IntervenerOne intervenerOne = IntervenerOne.builder().build();
            when(stopRepresentingClientService.getToBeRevokedIntervenerSolicitors(any(StopRepresentingClientInfo.class)))
                .thenReturn(List.of(intervenerOne));

            // Mock revocation step
            mockRevokeApplicantSolicitorOrRespondentSolicitor();
            mockPreparingLitigantRevocationLetterNotification();

            SendCorrespondenceEvent event1 = mock(SendCorrespondenceEvent.class);
            SendCorrespondenceEventEnvelop intvOneEnvelop = mock(SendCorrespondenceEventEnvelop.class);
            when(intvOneEnvelop.getEvent()).thenReturn(event1);
            when(intvOneEnvelop.getDescription()).thenReturn("intvOne notification");

            when(retryExecutor.supplyWithRetrySuppressException(
                any(ThrowingSupplier.class),
                eq("revoking intervener1 access"),
                eq(CASE_ID)
            )).thenReturn(Optional.of(intvOneEnvelop));

            FinremCaseData caseData = buildFinremCaseData(NoticeOfChangeParty.APPLICANT);

            // ---------- When ----------
            underTest.handle(FinremCallbackRequestFactory.from(CASE_ID_IN_LONG, caseData), AUTH_TOKEN);

            // ---------- Then ----------
            ArgumentCaptor<StopRepresentingClientInfo> infoCaptor = getStopRepresentingClientInfoCaptor();
            verify(stopRepresentingClientService).getToBeRevokedIntervenerSolicitors(infoCaptor.capture());
            verifyStopRepresentingClientInfoCaptured(infoCaptor, caseData);

            // Verify revokeIntervenerSolicitor called
            ArgumentCaptor<ThrowingSupplier<SendCorrespondenceEventEnvelop>> throwingSupplierCaptor = getThrowingSupplierCaptor();
            verify(retryExecutor).supplyWithRetrySuppressException(throwingSupplierCaptor.capture(), eq("revoking intervener1 access"), eq(CASE_ID));
            getSafely(throwingSupplierCaptor.getValue());
            ArgumentCaptor<StopRepresentingClientInfo> infoCaptor2 = getStopRepresentingClientInfoCaptor();
            verify(stopRepresentingClientService).revokeIntervenerSolicitor(infoCaptor2.capture(), eq(intervenerOne));
            verifyStopRepresentingClientInfoCaptured(infoCaptor2, caseData);

            // Verify notification event was published correctly
            ArgumentCaptor<ThrowingRunnable> publishEventCaptor = getThrowingRunnableCaptor();
            verify(retryExecutor)
                .runWithRetrySuppressException(
                    publishEventCaptor.capture(),
                    eq("intvOne notification"),
                    eq(CASE_ID)
                );
            publishEventCaptor.getAllValues().forEach(TestSetUpUtils::runSafely);
            verifySendCorrespondenceEventPublished(event1);
        }

        @Test
        void shouldMultipleRevokeIntervenerSolicitorAndNotifyIntervenerSolicitors_whenHandled() {
            // ---------- Given ----------
            IntervenerOne intervenerOne = IntervenerOne.builder().build();
            IntervenerThree intervenerThree = IntervenerThree.builder().build();
            when(stopRepresentingClientService.getToBeRevokedIntervenerSolicitors(any(StopRepresentingClientInfo.class)))
                .thenReturn(List.of(intervenerOne, intervenerThree));

            // Mock revocation step
            mockRevokeApplicantSolicitorOrRespondentSolicitor();
            mockPreparingLitigantRevocationLetterNotification();

            SendCorrespondenceEvent event1 = mock(SendCorrespondenceEvent.class);
            SendCorrespondenceEvent event3 = mock(SendCorrespondenceEvent.class);
            SendCorrespondenceEventEnvelop intvOneEnvelop = mock(SendCorrespondenceEventEnvelop.class);
            when(intvOneEnvelop.getEvent()).thenReturn(event1);
            when(intvOneEnvelop.getDescription()).thenReturn("intvOne notification");
            SendCorrespondenceEventEnvelop intvThreeEnvelop = mock(SendCorrespondenceEventEnvelop.class);
            when(intvThreeEnvelop.getEvent()).thenReturn(event3);
            when(intvThreeEnvelop.getDescription()).thenReturn("intvThree notification");
            when(retryExecutor.supplyWithRetrySuppressException(
                any(ThrowingSupplier.class),
                eq("revoking intervener1 access"),
                eq(CASE_ID)
            )).thenReturn(Optional.of(intvOneEnvelop));
            when(retryExecutor.supplyWithRetrySuppressException(
                any(ThrowingSupplier.class),
                eq("revoking intervener3 access"),
                eq(CASE_ID)
            )).thenReturn(Optional.of(intvThreeEnvelop));

            FinremCaseData caseData = buildFinremCaseData(NoticeOfChangeParty.APPLICANT);

            // ---------- When ----------
            underTest.handle(FinremCallbackRequestFactory.from(CASE_ID_IN_LONG, caseData), AUTH_TOKEN);

            // ---------- Then ----------
            ArgumentCaptor<StopRepresentingClientInfo> infoCaptor = getStopRepresentingClientInfoCaptor();
            verify(stopRepresentingClientService).getToBeRevokedIntervenerSolicitors(infoCaptor.capture());
            verifyStopRepresentingClientInfoCaptured(infoCaptor, caseData);

            // Verify revokeIntervenerSolicitor called
            ArgumentCaptor<ThrowingSupplier<SendCorrespondenceEventEnvelop>> throwingSupplierCaptor = getThrowingSupplierCaptor();
            verify(retryExecutor).supplyWithRetrySuppressException(throwingSupplierCaptor.capture(), eq("revoking intervener1 access"), eq(CASE_ID));
            verify(retryExecutor).supplyWithRetrySuppressException(throwingSupplierCaptor.capture(), eq("revoking intervener3 access"), eq(CASE_ID));
            throwingSupplierCaptor.getAllValues().forEach(TestSetUpUtils::getSafely);
            ArgumentCaptor<StopRepresentingClientInfo> infoCaptor2 = getStopRepresentingClientInfoCaptor();
            verify(stopRepresentingClientService).revokeIntervenerSolicitor(infoCaptor2.capture(), argThat(i
                -> IntervenerType.INTERVENER_ONE.equals(i.getIntervenerType())));
            verify(stopRepresentingClientService).revokeIntervenerSolicitor(infoCaptor2.capture(), argThat(i
                -> IntervenerType.INTERVENER_THREE.equals(i.getIntervenerType())));
            verifyStopRepresentingClientInfoCaptured(infoCaptor2, caseData);

            // Verify notification events were published correctly
            ArgumentCaptor<ThrowingRunnable> publishEventCaptor = getThrowingRunnableCaptor();
            verify(retryExecutor)
                .runWithRetrySuppressException(
                    publishEventCaptor.capture(),
                    eq("intvOne notification"),
                    eq(CASE_ID)
                );
            verify(retryExecutor)
                .runWithRetrySuppressException(
                    publishEventCaptor.capture(),
                    eq("intvThree notification"),
                    eq(CASE_ID)
                );
            publishEventCaptor.getAllValues().forEach(TestSetUpUtils::runSafely);
            verifySendCorrespondenceEventPublished(event1, event3);
        }
    }

    @Nested
    class RevokeApplicantSolicitorOrRespondentSolicitorTests {

        @ParameterizedTest
        @EnumSource(NoticeOfChangeParty.class)
        void shouldPerformCleanUpAfterNocWorkflow_whenHandled(NoticeOfChangeParty party) {
            // ---------- Given ----------
            StopRepresentingClientService.LitigantRevocation revocation = mock(StopRepresentingClientService.LitigantRevocation.class);
            when(revocation.wasRevoked()).thenReturn(true);

            // Mock revocation step
            when(retryExecutor.supplyWithRetrySuppressException(
                any(ThrowingSupplier.class),
                eq("revoking %s access".formatted(party.getValue())),
                eq(CASE_ID)
            )).thenReturn(Optional.of(revocation));

            // Capture clean-up runnable
            ArgumentCaptor<ThrowingRunnable> cleanupCaptor = getThrowingRunnableCaptor();

            doNothing().when(retryExecutor).runWithRetrySuppressException(
                cleanupCaptor.capture(),
                eq("cleaning up after noc workflow"),
                eq(CASE_ID)
            );

            FinremCaseData caseData = buildFinremCaseData(party);

            // ---------- When ----------
            underTest.handle(FinremCallbackRequestFactory.from(CASE_ID_IN_LONG, caseData), AUTH_TOKEN);

            // Execute captured clean-up logic
            runSafely(cleanupCaptor.getValue());

            // ---------- Then ----------
            verifyRevokeApplicantSolicitorOrRespondentSolicitorInvoked(party);
            // Verify correct data was passed to clean-up method
            ArgumentCaptor<StopRepresentingClientInfo> infoCaptor =
                getStopRepresentingClientInfoCaptor();

            verify(stopRepresentingClientService)
                .performCleanUpAfterNocWorkflow(infoCaptor.capture());
            verifyStopRepresentingClientInfoCaptured(infoCaptor, caseData);
        }

        @ParameterizedTest
        @EnumSource(NoticeOfChangeParty.class)
        void givenNoLitigantGetRevoked_whenHandled_thenShouldNotPerformCleanUpAfterNocWorkflow(NoticeOfChangeParty party) {
            // ---------- Given ----------
            FinremCaseData caseData = buildFinremCaseData(party);

            StopRepresentingClientService.LitigantRevocation revocation =
                new StopRepresentingClientService.LitigantRevocation(false, false);

            // Mock revocation result
            when(retryExecutor.supplyWithRetrySuppressException(
                any(ThrowingSupplier.class),
                eq("revoking %s access".formatted(party.getValue())),
                eq(CASE_ID)
            )).thenReturn(Optional.of(revocation));

            // ---------- When ----------
            underTest.handle(FinremCallbackRequestFactory.from(CASE_ID_IN_LONG, caseData), AUTH_TOKEN);

            // ---------- Then ----------
            verifyRevokeApplicantSolicitorOrRespondentSolicitorInvoked(party);
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
        void shouldNotifyLitigantSolicitor_whenHandled(NoticeOfChangeParty party) {
            // ---------- Given ----------
            StopRepresentingClientService.LitigantRevocation revocation = mock(StopRepresentingClientService.LitigantRevocation.class);
            when(revocation.wasRevoked()).thenReturn(true);

            // Mock retry to return a successful revocation result
            when(retryExecutor.supplyWithRetrySuppressException(
                any(ThrowingSupplier.class),
                eq("revoking %s access".formatted(party.getValue())),
                eq(CASE_ID)
            )).thenReturn(Optional.of(revocation));

            SendCorrespondenceEventEnvelop envelope = mock(SendCorrespondenceEventEnvelop.class);
            SendCorrespondenceEvent event = mock(SendCorrespondenceEvent.class);

            when(envelope.getDescription()).thenReturn("litigant solicitor notification");
            when(envelope.getEvent()).thenReturn(event);

            // Mock service to return notification event
            when(stopRepresentingClientService.prepareLitigantRevocationNotificationEvents(
                eq(revocation),
                any(StopRepresentingClientInfo.class)
            )).thenReturn(List.of(envelope));

            FinremCaseData caseData = buildFinremCaseData(party);

            // ---------- When ----------
            underTest.handle(FinremCallbackRequestFactory.from(CASE_ID_IN_LONG, caseData), AUTH_TOKEN);

            // ---------- Then ----------
            verifyRevokeApplicantSolicitorOrRespondentSolicitorInvoked(party);
            // Capture the info passed into the notification preparation method
            ArgumentCaptor<StopRepresentingClientInfo> infoCaptor =
                getStopRepresentingClientInfoCaptor();
            verify(stopRepresentingClientService)
                .prepareLitigantRevocationNotificationEvents(eq(revocation), infoCaptor.capture());
            // Assert correct case data and case ID were passed
            verifyStopRepresentingClientInfoCaptured(infoCaptor, caseData);

            // Capture the runnable used for publishing events
            ArgumentCaptor<ThrowingRunnable> runnableCaptor = getThrowingRunnableCaptor();

            verify(retryExecutor)
                .runWithRetrySuppressException(
                    runnableCaptor.capture(),
                    eq("litigant solicitor notification"),
                    eq(CASE_ID)
                );

            // Execute the captured runnable to verify event publishing
            runSafely(runnableCaptor.getValue());

            // Verify event was published correctly
            verifySendCorrespondenceEventPublished(event);
        }

        @ParameterizedTest
        @EnumSource(NoticeOfChangeParty.class)
        void givenNoLitigantNotificationPrepared_whenHandled_thenShouldNotNotifyLitigant(NoticeOfChangeParty party) {
            // ---------- Given ----------
            StopRepresentingClientService.LitigantRevocation revocation = mock(StopRepresentingClientService.LitigantRevocation.class);
            when(revocation.wasRevoked()).thenReturn(true);

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
            FinremCaseData caseData = buildFinremCaseData(party);

            // ---------- When ----------
            underTest.handle(FinremCallbackRequestFactory.from(CASE_ID_IN_LONG, caseData), AUTH_TOKEN);

            // ---------- Then ----------
            verifyRevokeApplicantSolicitorOrRespondentSolicitorInvoked(party);
            // Capture and verify info passed to service
            ArgumentCaptor<StopRepresentingClientInfo> infoCaptor =
                getStopRepresentingClientInfoCaptor();

            verify(stopRepresentingClientService)
                .prepareLitigantRevocationNotificationEvents(eq(revocation), infoCaptor.capture());
            verifyStopRepresentingClientInfoCaptured(infoCaptor, caseData);

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

        static Stream<Arguments> shouldNotifyLitigant_whenHandled_params() {
            return Stream.of(
                Arguments.of(NoticeOfChangeParty.APPLICANT, List.of(mock(SendCorrespondenceEvent.class))),
                Arguments.of(NoticeOfChangeParty.RESPONDENT, List.of(
                    mock(SendCorrespondenceEvent.class),
                    mock(SendCorrespondenceEvent.class)
                ))
            );
        }

        @ParameterizedTest
        @MethodSource("shouldNotifyLitigant_whenHandled_params")
        void shouldNotifyLitigant_whenHandled(NoticeOfChangeParty party, List<SendCorrespondenceEvent> events) {
            // ---------- Given ----------
            StopRepresentingClientService.LitigantRevocation revocation =
                mock(StopRepresentingClientService.LitigantRevocation.class);

            List<SendCorrespondenceEventEnvelop> envelopes = events.stream()
                .map(event -> {
                    SendCorrespondenceEventEnvelop envelope = mock(SendCorrespondenceEventEnvelop.class);
                    when(envelope.getDescription()).thenReturn("litigant letter notification");
                    when(envelope.getEvent()).thenReturn(event);
                    return envelope;
                })
                .toList();

            // ---------- Retry executor ----------
            when(retryExecutor.supplyWithRetrySuppressException(
                any(ThrowingSupplier.class),
                eq("revoking %s access".formatted(party.getValue())),
                eq(CASE_ID)
            )).thenReturn(Optional.of(revocation));

            doAnswer(invocation -> {
                ThrowingSupplier<?> supplier = invocation.getArgument(0);
                supplier.get();
                return Optional.of(envelopes);
            }).when(retryExecutor).supplyWithRetrySuppressException(
                any(ThrowingSupplier.class),
                eq("preparing litigant letter notifications"),
                eq(CASE_ID)
            );

            FinremCaseData caseData = buildFinremCaseData(party);

            // ---------- Service ----------
            when(stopRepresentingClientService.prepareLitigantRevocationNotificationEvents(
                eq(revocation),
                any(StopRepresentingClientInfo.class)
            )).thenReturn(List.of());

            // ---------- When ----------
            underTest.handle(FinremCallbackRequestFactory.from(CASE_ID_IN_LONG, caseData), AUTH_TOKEN);

            // ---------- Then ----------
            verifyRevokeApplicantSolicitorOrRespondentSolicitorInvoked(party);
            ArgumentCaptor<StopRepresentingClientInfo> infoCaptor =
                getStopRepresentingClientInfoCaptor();

            verify(stopRepresentingClientService)
                .prepareLitigantRevocationNotificationEvents(eq(revocation), infoCaptor.capture());
            verifyStopRepresentingClientInfoCaptured(infoCaptor, caseData);

            verify(stopRepresentingClientService)
                .prepareLitigantRevocationLetterNotificationEvents(eq(revocation), any());

            // ---------- Execute captured runnables ----------
            ArgumentCaptor<ThrowingRunnable> runnableCaptor = getThrowingRunnableCaptor();

            verify(retryExecutor, times(events.size()))
                .runWithRetrySuppressException(
                    runnableCaptor.capture(),
                    eq("litigant letter notification"),
                    eq(CASE_ID)
                );

            runnableCaptor.getAllValues().forEach(TestSetUpUtils::runSafely);

            // ---------- Verify published events ----------
            verifySendCorrespondenceEventPublished(events.toArray(new SendCorrespondenceEvent[0]));
        }

        private void verifyRevokeApplicantSolicitorOrRespondentSolicitorInvoked(NoticeOfChangeParty party) {
            ArgumentCaptor<ThrowingSupplier<StopRepresentingClientService.LitigantRevocation>> captor =
                getThrowingSupplierCaptor();
            verify(retryExecutor).supplyWithRetrySuppressException(
                captor.capture(),
                eq("revoking %s access".formatted(party.getValue())),
                eq(CASE_ID)
            );
            getSafely(captor.getValue());
            verify(stopRepresentingClientService).revokeApplicantSolicitorOrRespondentSolicitor(any(StopRepresentingClientInfo.class));
        }
    }

    private void verifySendCorrespondenceEventPublished(SendCorrespondenceEvent... events) {
        ArgumentCaptor<SendCorrespondenceEvent> captor = ArgumentCaptor.forClass(SendCorrespondenceEvent.class);

        verify(applicationEventPublisher, times(events.length)).publishEvent(captor.capture());

        assertThat(captor.getAllValues())
            .containsExactlyElementsOf(Arrays.asList(events));

        verifyNoMoreInteractions(applicationEventPublisher);
    }

    private void mockRevokeApplicantSolicitorOrRespondentSolicitor() {
        // Mock revocation step
        when(retryExecutor.supplyWithRetrySuppressException(
            any(ThrowingSupplier.class),
            eq("revoking applicant access"),
            eq(CASE_ID)
        )).thenReturn(Optional.of(mock(StopRepresentingClientService.LitigantRevocation.class)));
    }

    private void mockPreparingLitigantRevocationLetterNotification() {
        // Mock revocation step
        when(retryExecutor.supplyWithRetrySuppressException(
            any(ThrowingSupplier.class),
            eq("preparing litigant letter notifications"),
            eq(CASE_ID)
        )).thenReturn(Optional.of(List.of()));
    }
}
