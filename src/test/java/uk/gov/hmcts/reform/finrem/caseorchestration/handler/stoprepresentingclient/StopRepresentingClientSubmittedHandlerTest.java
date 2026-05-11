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
import org.mockito.MockedStatic;
import org.mockito.Mockito;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.util.function.ThrowingSupplier;
import uk.gov.hmcts.reform.ccd.client.model.CaseDetails;
import uk.gov.hmcts.reform.finrem.caseorchestration.FinremCallbackRequestFactory;
import uk.gov.hmcts.reform.finrem.caseorchestration.TestSetUpUtils;
import uk.gov.hmcts.reform.finrem.caseorchestration.controllers.GenericAboutToStartOrSubmitCallbackResponse;
import uk.gov.hmcts.reform.finrem.caseorchestration.handler.FinremCallbackRequest;
import uk.gov.hmcts.reform.finrem.caseorchestration.mapper.FinremCaseDetailsMapper;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.BarristerChange;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.Barrister;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.BarristerParty;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.CaseType;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.FinremCaseData;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.FinremCaseDetails;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.NoticeOfChangeParty;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.wrapper.ContactDetailsWrapper;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.wrapper.IntervenerOne;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.wrapper.IntervenerThree;
import uk.gov.hmcts.reform.finrem.caseorchestration.notifications.notifiers.SendCorrespondenceEvent;
import uk.gov.hmcts.reform.finrem.caseorchestration.notifications.notifiers.SendCorrespondenceEventWithDescription;
import uk.gov.hmcts.reform.finrem.caseorchestration.service.FeatureToggleService;
import uk.gov.hmcts.reform.finrem.caseorchestration.service.barristers.ManageBarristerService;
import uk.gov.hmcts.reform.finrem.caseorchestration.service.ccd.CoreCaseDataService;
import uk.gov.hmcts.reform.finrem.caseorchestration.service.intervener.IntervenerService;
import uk.gov.hmcts.reform.finrem.caseorchestration.service.noc.NocUtils;
import uk.gov.hmcts.reform.finrem.caseorchestration.service.stoprepresentingclient.LitigantRevocation;
import uk.gov.hmcts.reform.finrem.caseorchestration.service.stoprepresentingclient.StopRepresentingClientCorresponder;
import uk.gov.hmcts.reform.finrem.caseorchestration.service.stoprepresentingclient.StopRepresentingClientInfo;
import uk.gov.hmcts.reform.finrem.caseorchestration.service.stoprepresentingclient.StopRepresentingClientService;
import uk.gov.hmcts.reform.finrem.caseorchestration.utils.retry.RetryExecutor;
import uk.gov.hmcts.reform.finrem.caseorchestration.utils.retry.ThrowingRunnable;

import java.time.Duration;
import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import java.util.function.Function;
import java.util.stream.Stream;

import static org.assertj.core.api.Assertions.assertThat;
import static org.awaitility.Awaitility.await;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.argThat;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.doAnswer;
import static org.mockito.Mockito.doNothing;
import static org.mockito.Mockito.lenient;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.spy;
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
import static uk.gov.hmcts.reform.finrem.caseorchestration.model.EventType.INTERNAL_CHANGE_UPDATE_CASE;
import static uk.gov.hmcts.reform.finrem.caseorchestration.model.EventType.STOP_REPRESENTING_CLIENT;
import static uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.CaseType.CONSENTED;
import static uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.CaseType.CONTESTED;
import static uk.gov.hmcts.reform.finrem.caseorchestration.model.intervener.IntervenerType.INTERVENER_FOUR;
import static uk.gov.hmcts.reform.finrem.caseorchestration.model.intervener.IntervenerType.INTERVENER_ONE;
import static uk.gov.hmcts.reform.finrem.caseorchestration.model.intervener.IntervenerType.INTERVENER_THREE;
import static uk.gov.hmcts.reform.finrem.caseorchestration.model.intervener.IntervenerType.INTERVENER_TWO;
import static uk.gov.hmcts.reform.finrem.caseorchestration.test.Assertions.assertCanHandle;

@ExtendWith(MockitoExtension.class)
class StopRepresentingClientSubmittedHandlerTest {

    public static ArgumentCaptor<StopRepresentingClientInfo> getStopRepresentingClientInfoCaptor() {
        return ArgumentCaptor.forClass(StopRepresentingClientInfo.class);
    }

    private static void verifyStopRepresentingClientInfoCaptured(ArgumentCaptor<StopRepresentingClientInfo> captor,
                                                                 FinremCaseData caseData) {
        assertThat(captor.getValue()).isNotNull();
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
    @Mock
    private StopRepresentingClientCorresponder stopRepresentingClientCorresponder;
    @Mock
    private ManageBarristerService manageBarristerService;
    @Mock
    private IntervenerService intervenerService;
    @Mock
    private CoreCaseDataService coreCaseDataService;
    @Mock
    private CaseType caseType;

    @BeforeEach
    void setup() {
        underTest = new StopRepresentingClientSubmittedHandler(finremCaseDetailsMapper, stopRepresentingClientService,
            stopRepresentingClientCorresponder, featureToggleService, applicationEventPublisher, retryExecutor,
            manageBarristerService, intervenerService, coreCaseDataService);
        lenient().when(featureToggleService.isExui3990WorkaroundEnabled()).thenReturn(true);
        lenient().when(manageBarristerService.getBarristerChange(any(FinremCaseDetails.class),
            any(FinremCaseData.class), any(BarristerParty.class))).thenReturn(mock(BarristerChange.class));
    }

    @Test
    void testCanHandle() {
        assertCanHandle(underTest,
            Arguments.of(SUBMITTED, CONTESTED, STOP_REPRESENTING_CLIENT),
            Arguments.of(SUBMITTED, CONSENTED, STOP_REPRESENTING_CLIENT));
    }

    @Test
    void givenFeatureFlagDisabled_whenHandled_thenReturnConfirmationMessages() {
        when(featureToggleService.isExui3990WorkaroundEnabled()).thenReturn(false);
        FinremCallbackRequest request = FinremCallbackRequestFactory.from();
        GenericAboutToStartOrSubmitCallbackResponse<FinremCaseData> response = underTest.handle(request, AUTH_TOKEN);
        assertThat(response.getConfirmationBody()).contains("Your changes will be applied shortly.");
        assertThat(response.getConfirmationHeader()).contains("Notice of change request submitted");
    }

    @Test
    void givenFeatureFlagDisabled_whenHandled_shouldDelegateToBusinessLogicAsync() {
        // ---------- Given ----------
        when(featureToggleService.isExui3990WorkaroundEnabled()).thenReturn(false);

        StopRepresentingClientSubmittedHandler spyHandler = spy(underTest);

        FinremCaseData caseData = buildFinremCaseData(NoticeOfChangeParty.APPLICANT);

        // ---------- When ----------
        spyHandler.handle(FinremCallbackRequestFactory.from(CASE_ID_IN_LONG, caseData), AUTH_TOKEN);

        // ---------- Then ----------
        await().atMost(Duration.ofSeconds(1))
            .untilAsserted(() ->
                verify(spyHandler)
                    .revokePartiesAccessAndNotifyParties(any()));
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
            when(barristerChange.getBarristerParty()).thenReturn(barristerParty);
            Barrister barrister = mock(Barrister.class);
            when(barristerChange.getRemoved()).thenReturn(Set.of(barrister));
            when(manageBarristerService.getBarristerChange(any(FinremCaseDetails.class),
                any(FinremCaseData.class), eq(barristerParty))).thenReturn(barristerChange);

            SendCorrespondenceEventWithDescription eventWithDesc = mock(SendCorrespondenceEventWithDescription.class);
            SendCorrespondenceEvent event = mock(SendCorrespondenceEvent.class);
            when(eventWithDesc.getEvent()).thenReturn(event);
            when(eventWithDesc.getDescription()).thenReturn("whatever barrister notification");

            when(retryExecutor.supplyWithRetrySuppressException(any(ThrowingSupplier.class),
                eq("revoking %s barrister access".formatted(barristerParty.getValue())), eq(CASE_ID)))
                .thenReturn(Optional.of(List.of(eventWithDesc)));

            FinremCaseData caseData = buildFinremCaseData(NoticeOfChangeParty.APPLICANT);

            // ---------- When ----------
            underTest.handle(FinremCallbackRequestFactory.from(CASE_ID_IN_LONG, caseData), AUTH_TOKEN);

            // ---------- Then ----------
            verify(manageBarristerService).getBarristerChange(any(FinremCaseDetails.class),
                any(FinremCaseData.class), eq(barristerParty));

            // Verify revokeBarristers called
            ArgumentCaptor<ThrowingSupplier<SendCorrespondenceEventWithDescription>> throwingSupplierCaptor = getThrowingSupplierCaptor();
            verify(retryExecutor).supplyWithRetrySuppressException(throwingSupplierCaptor.capture(),
                eq("revoking %s barrister access".formatted(barristerParty.getValue())), eq(CASE_ID));
            getSafely(throwingSupplierCaptor.getValue());
            ArgumentCaptor<StopRepresentingClientInfo> infoCaptor2 = getStopRepresentingClientInfoCaptor();
            verify(manageBarristerService).executeBarristerChange(CASE_ID_IN_LONG, barristerChange);
            switch (barristerParty) {
                case APPLICANT ->
                    verify(stopRepresentingClientCorresponder).prepareApplicantBarristerEmailNotificationEvent(infoCaptor2.capture(),
                        eq(barrister));
                case RESPONDENT ->
                    verify(stopRepresentingClientCorresponder).prepareRespondentBarristerEmailNotificationEvent(infoCaptor2.capture(),
                        eq(barrister));
                case INTERVENER1 ->
                    verify(stopRepresentingClientCorresponder).prepareIntervenerBarristerEmailNotificationEvent(infoCaptor2.capture(),
                        eq(INTERVENER_ONE), eq(barrister));
                case INTERVENER2 ->
                    verify(stopRepresentingClientCorresponder).prepareIntervenerBarristerEmailNotificationEvent(infoCaptor2.capture(),
                        eq(INTERVENER_TWO), eq(barrister));
                case INTERVENER3 ->
                    verify(stopRepresentingClientCorresponder).prepareIntervenerBarristerEmailNotificationEvent(infoCaptor2.capture(),
                        eq(INTERVENER_THREE), eq(barrister));
                case INTERVENER4 ->
                    verify(stopRepresentingClientCorresponder).prepareIntervenerBarristerEmailNotificationEvent(infoCaptor2.capture(),
                        eq(INTERVENER_FOUR), eq(barrister));
                default ->
                    throw new IllegalStateException("Unexpected value: " + barristerParty);
            }
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
            Barrister applicantBarrister = mock(Barrister.class);
            when(applicantBarristerChange.getBarristerParty()).thenReturn(BarristerParty.APPLICANT);
            when(applicantBarristerChange.getRemoved()).thenReturn(Set.of(applicantBarrister));
            when(manageBarristerService.getBarristerChange(any(FinremCaseDetails.class),
                any(FinremCaseData.class), eq(BarristerParty.APPLICANT))).thenReturn(applicantBarristerChange);
            BarristerChange intervenerTwoBarristerChange = mock(BarristerChange.class);
            Barrister intv2Barrister = mock(Barrister.class);
            when(intervenerTwoBarristerChange.getBarristerParty()).thenReturn(BarristerParty.INTERVENER2);
            when(intervenerTwoBarristerChange.getRemoved()).thenReturn(Set.of(mock(Barrister.class), intv2Barrister));
            when(manageBarristerService.getBarristerChange(any(FinremCaseDetails.class),
                any(FinremCaseData.class), eq(BarristerParty.INTERVENER2))).thenReturn(intervenerTwoBarristerChange);

            SendCorrespondenceEventWithDescription applicantBarristerEventWithDesc = mock(SendCorrespondenceEventWithDescription.class);
            SendCorrespondenceEvent applicantBarristerEvent = mock(SendCorrespondenceEvent.class);
            when(applicantBarristerEventWithDesc.getEvent()).thenReturn(applicantBarristerEvent);
            when(applicantBarristerEventWithDesc.getDescription()).thenReturn("applicant barrister notification");
            when(retryExecutor.supplyWithRetrySuppressException(any(ThrowingSupplier.class),
                eq("revoking applicant barrister access"), eq(CASE_ID)))
                .thenReturn(Optional.of(List.of(applicantBarristerEventWithDesc)));

            SendCorrespondenceEventWithDescription intvTwoBarristerEventWithDesc = mock(SendCorrespondenceEventWithDescription.class);
            SendCorrespondenceEvent intvTwoBarristerEvent = mock(SendCorrespondenceEvent.class);
            when(intvTwoBarristerEventWithDesc.getEvent()).thenReturn(intvTwoBarristerEvent);
            when(intvTwoBarristerEventWithDesc.getDescription()).thenReturn("intervener2 barrister notification");
            when(retryExecutor.supplyWithRetrySuppressException(any(ThrowingSupplier.class),
                eq("revoking intervener2 barrister access"), eq(CASE_ID)))
                .thenReturn(Optional.of(List.of(intvTwoBarristerEventWithDesc)));

            FinremCaseData caseData = buildFinremCaseData(NoticeOfChangeParty.APPLICANT);

            // ---------- When ----------
            underTest.handle(FinremCallbackRequestFactory.from(CASE_ID_IN_LONG, caseData), AUTH_TOKEN);

            // ---------- Then ----------
            verify(manageBarristerService).getBarristerChange(any(FinremCaseDetails.class),
                any(FinremCaseData.class), eq(BarristerParty.APPLICANT));
            verify(manageBarristerService).getBarristerChange(any(FinremCaseDetails.class),
                any(FinremCaseData.class), eq(BarristerParty.INTERVENER2));

            // Verify revokeBarristers called
            ArgumentCaptor<ThrowingSupplier<SendCorrespondenceEventWithDescription>> throwingSupplierCaptor = getThrowingSupplierCaptor();
            verify(retryExecutor, times(2)).supplyWithRetrySuppressException(throwingSupplierCaptor.capture(),
                argThat(a -> List.of(
                    "revoking applicant barrister access",
                    "revoking intervener2 barrister access"
                ).contains(a)), eq(CASE_ID));
            throwingSupplierCaptor.getAllValues().forEach(TestSetUpUtils::getSafely);
            ArgumentCaptor<StopRepresentingClientInfo> infoCaptor2 = getStopRepresentingClientInfoCaptor();
            verify(manageBarristerService).executeBarristerChange(CASE_ID_IN_LONG, applicantBarristerChange);
            verify(stopRepresentingClientCorresponder).prepareApplicantBarristerEmailNotificationEvent(infoCaptor2.capture(),
                eq(applicantBarrister));
            verify(manageBarristerService).executeBarristerChange(CASE_ID_IN_LONG, intervenerTwoBarristerChange);
            verify(stopRepresentingClientCorresponder).prepareIntervenerBarristerEmailNotificationEvent(infoCaptor2.capture(),
                eq(INTERVENER_TWO), eq(intv2Barrister));
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
            SendCorrespondenceEventWithDescription intvOneEvents = mock(SendCorrespondenceEventWithDescription.class);
            when(intvOneEvents.getEvent()).thenReturn(event1);
            when(intvOneEvents.getDescription()).thenReturn("intvOne notification");

            when(retryExecutor.supplyWithRetrySuppressException(
                any(ThrowingSupplier.class),
                eq("revoking intervener1 access"),
                eq(CASE_ID)
            )).thenReturn(Optional.of(intvOneEvents));

            FinremCaseData caseData = buildFinremCaseData(NoticeOfChangeParty.APPLICANT);

            // ---------- When ----------
            underTest.handle(FinremCallbackRequestFactory.from(CASE_ID_IN_LONG, caseData), AUTH_TOKEN);

            // ---------- Then ----------
            ArgumentCaptor<StopRepresentingClientInfo> infoCaptor = getStopRepresentingClientInfoCaptor();
            verify(stopRepresentingClientService).getToBeRevokedIntervenerSolicitors(infoCaptor.capture());
            verifyStopRepresentingClientInfoCaptured(infoCaptor, caseData);

            // Verify revokeIntervenerSolicitor called
            ArgumentCaptor<ThrowingSupplier<SendCorrespondenceEventWithDescription>> throwingSupplierCaptor = getThrowingSupplierCaptor();
            verify(retryExecutor).supplyWithRetrySuppressException(throwingSupplierCaptor.capture(), eq("revoking intervener1 access"), eq(CASE_ID));
            getSafely(throwingSupplierCaptor.getValue());
            ArgumentCaptor<StopRepresentingClientInfo> infoCaptor2 = getStopRepresentingClientInfoCaptor();
            verify(intervenerService).revokeIntervenerSolicitor(CASE_ID_IN_LONG, intervenerOne);
            verify(stopRepresentingClientCorresponder).prepareIntervenerSolicitorEmailNotificationEvent(infoCaptor2.capture(),
                eq(INTERVENER_ONE));
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
            SendCorrespondenceEventWithDescription intvOneEvents = mock(SendCorrespondenceEventWithDescription.class);
            when(intvOneEvents.getEvent()).thenReturn(event1);
            when(intvOneEvents.getDescription()).thenReturn("intvOne notification");
            SendCorrespondenceEventWithDescription intvThreeEvents = mock(SendCorrespondenceEventWithDescription.class);
            when(intvThreeEvents.getEvent()).thenReturn(event3);
            when(intvThreeEvents.getDescription()).thenReturn("intvThree notification");
            when(retryExecutor.supplyWithRetrySuppressException(
                any(ThrowingSupplier.class),
                eq("revoking intervener1 access"),
                eq(CASE_ID)
            )).thenReturn(Optional.of(intvOneEvents));
            when(retryExecutor.supplyWithRetrySuppressException(
                any(ThrowingSupplier.class),
                eq("revoking intervener3 access"),
                eq(CASE_ID)
            )).thenReturn(Optional.of(intvThreeEvents));

            FinremCaseData caseData = buildFinremCaseData(NoticeOfChangeParty.APPLICANT);

            // ---------- When ----------
            underTest.handle(FinremCallbackRequestFactory.from(CASE_ID_IN_LONG, caseData), AUTH_TOKEN);

            // ---------- Then ----------
            ArgumentCaptor<StopRepresentingClientInfo> infoCaptor = getStopRepresentingClientInfoCaptor();
            verify(stopRepresentingClientService).getToBeRevokedIntervenerSolicitors(infoCaptor.capture());
            verifyStopRepresentingClientInfoCaptured(infoCaptor, caseData);

            // Verify revokeIntervenerSolicitor called
            ArgumentCaptor<ThrowingSupplier<SendCorrespondenceEventWithDescription>> throwingSupplierCaptor = getThrowingSupplierCaptor();
            verify(retryExecutor).supplyWithRetrySuppressException(throwingSupplierCaptor.capture(), eq("revoking intervener1 access"), eq(CASE_ID));
            verify(retryExecutor).supplyWithRetrySuppressException(throwingSupplierCaptor.capture(), eq("revoking intervener3 access"), eq(CASE_ID));
            throwingSupplierCaptor.getAllValues().forEach(TestSetUpUtils::getSafely);
            ArgumentCaptor<StopRepresentingClientInfo> infoCaptor2 = getStopRepresentingClientInfoCaptor();
            verify(intervenerService).revokeIntervenerSolicitor(eq(CASE_ID_IN_LONG), argThat(i
                -> INTERVENER_ONE.equals(i.getIntervenerType())));
            verify(stopRepresentingClientCorresponder).prepareIntervenerSolicitorEmailNotificationEvent(infoCaptor2.capture(),
                eq(INTERVENER_ONE));
            verify(intervenerService).revokeIntervenerSolicitor(eq(CASE_ID_IN_LONG), argThat(i
                -> INTERVENER_THREE.equals(i.getIntervenerType())));
            verify(stopRepresentingClientCorresponder).prepareIntervenerSolicitorEmailNotificationEvent(infoCaptor2.capture(),
                eq(INTERVENER_THREE));
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
            LitigantRevocation revocation = mock(LitigantRevocation.class);
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
            ArgumentCaptor<Function<CaseDetails, Map<String, Object>>> captor = ArgumentCaptor.forClass(Function.class);
            verify(coreCaseDataService)
                .performPostSubmitCallback(eq(caseType), eq(CASE_ID_IN_LONG), eq(INTERNAL_CHANGE_UPDATE_CASE.getCcdType()), captor.capture());

            try (MockedStatic<NocUtils> mockedStatic = Mockito.mockStatic(NocUtils.class)) {
                captor.getValue().apply(mock(CaseDetails.class));
                mockedStatic.verify(NocUtils::clearChangeOrganisationRequestField);
            }
        }

        @ParameterizedTest
        @EnumSource(NoticeOfChangeParty.class)
        void givenNoLitigantGetRevoked_whenHandled_thenShouldNotPerformCleanUpAfterNocWorkflow(NoticeOfChangeParty party) {
            // ---------- Given ----------
            FinremCaseData caseData = buildFinremCaseData(party);

            LitigantRevocation revocation =
                new LitigantRevocation(false, false);

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
            verifyNoMoreInteractions(coreCaseDataService);
        }

        @ParameterizedTest
        @EnumSource(NoticeOfChangeParty.class)
        void shouldNotifyLitigantSolicitor_whenHandled(NoticeOfChangeParty party) {
            // ---------- Given ----------
            LitigantRevocation revocation = mock(LitigantRevocation.class);
            when(revocation.wasRevoked()).thenReturn(true);

            // Mock retry to return a successful revocation result
            when(retryExecutor.supplyWithRetrySuppressException(
                any(ThrowingSupplier.class),
                eq("revoking %s access".formatted(party.getValue())),
                eq(CASE_ID)
            )).thenReturn(Optional.of(revocation));

            SendCorrespondenceEventWithDescription eventWithDesc = mock(SendCorrespondenceEventWithDescription.class);
            SendCorrespondenceEvent event = mock(SendCorrespondenceEvent.class);

            when(eventWithDesc.getDescription()).thenReturn("litigant solicitor notification");
            when(eventWithDesc.getEvent()).thenReturn(event);

            // Mock service to return notification event
            when(stopRepresentingClientCorresponder.prepareRepresentativeRevocationNotificationEvent(
                eq(revocation),
                any(StopRepresentingClientInfo.class)
            )).thenReturn(List.of(eventWithDesc));

            FinremCaseData caseData = buildFinremCaseData(party);

            // ---------- When ----------
            underTest.handle(FinremCallbackRequestFactory.from(CASE_ID_IN_LONG, caseData), AUTH_TOKEN);

            // ---------- Then ----------
            verifyRevokeApplicantSolicitorOrRespondentSolicitorInvoked(party);
            // Capture the info passed into the notification preparation method
            ArgumentCaptor<StopRepresentingClientInfo> infoCaptor =
                getStopRepresentingClientInfoCaptor();
            verify(stopRepresentingClientCorresponder)
                .prepareRepresentativeRevocationNotificationEvent(eq(revocation), infoCaptor.capture());
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
            LitigantRevocation revocation = mock(LitigantRevocation.class);
            when(revocation.wasRevoked()).thenReturn(true);

            when(retryExecutor.supplyWithRetrySuppressException(
                any(ThrowingSupplier.class),
                eq("revoking %s access".formatted(party.getValue())),
                eq(CASE_ID)
            )).thenReturn(Optional.of(revocation));

            when(stopRepresentingClientCorresponder.prepareRepresentativeRevocationNotificationEvent(
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

            verify(stopRepresentingClientCorresponder)
                .prepareRepresentativeRevocationNotificationEvent(eq(revocation), infoCaptor.capture());
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
            LitigantRevocation revocation =
                mock(LitigantRevocation.class);

            List<SendCorrespondenceEventWithDescription> eventsWithDesc = events.stream()
                .map(event -> {
                    SendCorrespondenceEventWithDescription envelope = mock(SendCorrespondenceEventWithDescription.class);
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
                return Optional.of(eventsWithDesc);
            }).when(retryExecutor).supplyWithRetrySuppressException(
                any(ThrowingSupplier.class),
                eq("preparing litigant letter notifications"),
                eq(CASE_ID)
            );

            FinremCaseData caseData = buildFinremCaseData(party);

            // ---------- Service ----------
            when(stopRepresentingClientCorresponder.prepareRepresentativeRevocationNotificationEvent(
                eq(revocation),
                any(StopRepresentingClientInfo.class)
            )).thenReturn(List.of());

            // ---------- When ----------
            underTest.handle(FinremCallbackRequestFactory.from(CASE_ID_IN_LONG, caseData), AUTH_TOKEN);

            // ---------- Then ----------
            verifyRevokeApplicantSolicitorOrRespondentSolicitorInvoked(party);
            ArgumentCaptor<StopRepresentingClientInfo> infoCaptor =
                getStopRepresentingClientInfoCaptor();

            verify(stopRepresentingClientCorresponder)
                .prepareRepresentativeRevocationNotificationEvent(eq(revocation), infoCaptor.capture());
            verifyStopRepresentingClientInfoCaptured(infoCaptor, caseData);

            verify(stopRepresentingClientCorresponder)
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
            ArgumentCaptor<ThrowingSupplier<LitigantRevocation>> captor =
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
        )).thenReturn(Optional.of(mock(LitigantRevocation.class)));
    }

    private void mockPreparingLitigantRevocationLetterNotification() {
        // Mock revocation step
        when(retryExecutor.supplyWithRetrySuppressException(
            any(ThrowingSupplier.class),
            eq("preparing litigant letter notifications"),
            eq(CASE_ID)
        )).thenReturn(Optional.of(List.of()));
    }

    private FinremCaseData buildFinremCaseData(NoticeOfChangeParty party) {
        return FinremCaseData.builder()
            .ccdCaseType(caseType)
            .contactDetailsWrapper(
                ContactDetailsWrapper.builder()
                    .nocParty(party)
                    .build()
            )
            .build();
    }
}
