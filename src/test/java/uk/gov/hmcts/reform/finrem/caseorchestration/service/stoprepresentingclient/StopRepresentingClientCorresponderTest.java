package uk.gov.hmcts.reform.finrem.caseorchestration.service.stoprepresentingclient;

import org.apache.commons.lang3.function.TriFunction;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.EnumSource;
import org.junit.jupiter.params.provider.MethodSource;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockedStatic;
import org.mockito.Mockito;
import org.mockito.Spy;
import org.mockito.junit.jupiter.MockitoExtension;
import uk.gov.hmcts.reform.finrem.caseorchestration.config.DocumentConfiguration;
import uk.gov.hmcts.reform.finrem.caseorchestration.helper.DocumentHelper;
import uk.gov.hmcts.reform.finrem.caseorchestration.mapper.letterdetails.LetterDetailsMapper;
import uk.gov.hmcts.reform.finrem.caseorchestration.mapper.notificationrequest.FinremNotificationRequestMapper;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.Barrister;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.CaseDocument;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.CaseRole;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.CaseType;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.FinremCaseData;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.FinremCaseDetails;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.intervener.IntervenerType;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.notification.NotificationRequest;
import uk.gov.hmcts.reform.finrem.caseorchestration.notifications.domain.EmailTemplateNames;
import uk.gov.hmcts.reform.finrem.caseorchestration.notifications.notifiers.NotificationParty;
import uk.gov.hmcts.reform.finrem.caseorchestration.notifications.notifiers.SendCorrespondenceEvent;
import uk.gov.hmcts.reform.finrem.caseorchestration.notifications.notifiers.SendCorrespondenceEventWithDescription;
import uk.gov.hmcts.reform.finrem.caseorchestration.service.GenericDocumentService;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;
import java.util.stream.Stream;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertAll;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyMap;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.mockStatic;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static uk.gov.hmcts.reform.finrem.caseorchestration.TestConstants.AUTH_TOKEN;
import static uk.gov.hmcts.reform.finrem.caseorchestration.TestSetUpUtils.caseDocument;
import static uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.CaseRole.APP_SOLICITOR;
import static uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.CaseRole.RESP_SOLICITOR;
import static uk.gov.hmcts.reform.finrem.caseorchestration.notifications.notifiers.NotificationParty.APPLICANT;
import static uk.gov.hmcts.reform.finrem.caseorchestration.notifications.notifiers.NotificationParty.FORMER_APPLICANT_BARRISTER_ONLY;
import static uk.gov.hmcts.reform.finrem.caseorchestration.notifications.notifiers.NotificationParty.FORMER_APPLICANT_SOLICITOR_ONLY;
import static uk.gov.hmcts.reform.finrem.caseorchestration.notifications.notifiers.NotificationParty.FORMER_RESPONDENT_BARRISTER_ONLY;
import static uk.gov.hmcts.reform.finrem.caseorchestration.notifications.notifiers.NotificationParty.FORMER_RESPONDENT_SOLICITOR_ONLY;
import static uk.gov.hmcts.reform.finrem.caseorchestration.notifications.notifiers.NotificationParty.RESPONDENT;

@ExtendWith(MockitoExtension.class)
class StopRepresentingClientCorresponderTest {

    static LocalDateTime fixedLocalDateTime = LocalDateTime.of(2026, 2, 2, 12, 12);

    @Mock
    private GenericDocumentService genericDocumentService;

    @Mock
    private DocumentConfiguration documentConfiguration;

    @Mock
    private LetterDetailsMapper letterDetailsMapper;

    @InjectMocks
    @Spy
    private StopRepresentingClientCorresponder underTest;

    @Mock
    private FinremNotificationRequestMapper finremNotificationRequestMapper;

    @Mock
    private FinremCaseDetails finremCaseDetails;

    @Mock
    private FinremCaseDetails finremCaseDetailsBefore;

    @Mock
    private FinremCaseData finremCaseData;

    @Nested
    class PrepareBarristerEmailNotificationEvent {
        static Stream<Arguments> shouldPrepareLitigantBarristerEmailNotification() {
            return Stream.of(
                Arguments.of(
                    (MockedStatic.Verification) () ->
                        EmailTemplateResolver.getNotifyApplicantRepresentativeTemplateName(any(FinremCaseData.class)),
                    "notifying applicant barrister",
                    List.of(FORMER_APPLICANT_BARRISTER_ONLY),
                    (TriFunction<
                        StopRepresentingClientCorresponder,
                        StopRepresentingClientInfo,
                        Barrister,
                        SendCorrespondenceEventWithDescription
                        >) StopRepresentingClientCorresponder::prepareApplicantBarristerEmailNotificationEvent
                ),

                Arguments.of(
                    (MockedStatic.Verification) () ->
                        EmailTemplateResolver.getNotifyRespondentRepresentativeTemplateName(any(FinremCaseData.class)),
                    "notifying respondent barrister",
                    List.of(FORMER_RESPONDENT_BARRISTER_ONLY),
                    (TriFunction<
                        StopRepresentingClientCorresponder,
                        StopRepresentingClientInfo,
                        Barrister,
                        SendCorrespondenceEventWithDescription
                        >) StopRepresentingClientCorresponder::prepareRespondentBarristerEmailNotificationEvent
                )
            );
        }

        @ParameterizedTest
        @MethodSource
        void shouldPrepareLitigantBarristerEmailNotification(
            MockedStatic.Verification verification, String description, List<NotificationParty> notifying,
            TriFunction<StopRepresentingClientCorresponder, StopRepresentingClientInfo, Barrister,
                SendCorrespondenceEventWithDescription> function) {

            try (MockedStatic<EmailTemplateResolver> emailTemplateResolver = mockStatic(EmailTemplateResolver.class)) {
                EmailTemplateNames emailTemplateNames = mock(EmailTemplateNames.class);
                emailTemplateResolver.when(verification)
                    .thenReturn(emailTemplateNames);

                StopRepresentingClientInfo info = mock(StopRepresentingClientInfo.class);
                when(info.getFinremCaseData()).thenReturn(finremCaseData);
                when(info.getCaseDetails()).thenReturn(finremCaseDetails);
                when(info.getCaseDetailsBefore()).thenReturn(finremCaseDetailsBefore);
                when(info.getUserAuthorisation()).thenReturn(AUTH_TOKEN);
                Barrister barrister = mock(Barrister.class);

                NotificationRequest notificationRequest = mock(NotificationRequest.class);
                when(finremNotificationRequestMapper
                    .getNotificationRequestForStopRepresentingClientEmail(info.getCaseDetailsBefore(), barrister))
                    .thenReturn(notificationRequest);

                SendCorrespondenceEventWithDescription actual = function.apply(underTest, info, barrister);

                assertAll(
                    () -> assertThat(actual)
                        .extracting(SendCorrespondenceEventWithDescription::getDescription)
                        .isEqualTo(description),

                    () -> assertThat(actual)
                        .extracting(SendCorrespondenceEventWithDescription::getEvent)
                        .extracting(
                            SendCorrespondenceEvent::getNotificationParties,
                            SendCorrespondenceEvent::getEmailNotificationRequest,
                            SendCorrespondenceEvent::getEmailTemplate,
                            SendCorrespondenceEvent::getCaseDetails,
                            SendCorrespondenceEvent::getCaseDetailsBefore,
                            SendCorrespondenceEvent::getAuthToken,
                            SendCorrespondenceEvent::getBarrister
                        )
                        .containsExactly(
                            notifying,
                            notificationRequest,
                            emailTemplateNames,
                            finremCaseDetails,
                            finremCaseDetailsBefore,
                            AUTH_TOKEN,
                            barrister
                        ),
                    () -> verify(finremNotificationRequestMapper).getNotificationRequestForStopRepresentingClientEmail(finremCaseDetailsBefore,
                        barrister)
                );
            }
        }

        @ParameterizedTest
        @EnumSource(value = IntervenerType.class)
        void shouldPrepareIntervenerBarristerEmailNotification(IntervenerType intervenerType) {
            try (
                MockedStatic<EmailTemplateResolver> emailTemplateResolver = mockStatic(EmailTemplateResolver.class);
                MockedStatic<NotificationParty> notificationPartyStatic = mockStatic(NotificationParty.class)) {
                EmailTemplateNames emailTemplateNames = mock(EmailTemplateNames.class);
                emailTemplateResolver.when(() -> EmailTemplateResolver.getNotifyIntervenerRepresentativeTemplateName(any(FinremCaseData.class)))
                    .thenReturn(emailTemplateNames);
                NotificationParty notificationParty = mock(NotificationParty.class);
                notificationPartyStatic.when(() -> NotificationParty.getFormerIntervenerBarrister(intervenerType))
                    .thenReturn(notificationParty);

                StopRepresentingClientInfo info = mock(StopRepresentingClientInfo.class);
                when(info.getFinremCaseData()).thenReturn(finremCaseData);
                when(info.getCaseDetails()).thenReturn(finremCaseDetails);
                when(info.getCaseDetailsBefore()).thenReturn(finremCaseDetailsBefore);
                when(info.getUserAuthorisation()).thenReturn(AUTH_TOKEN);
                Barrister barrister = mock(Barrister.class);

                NotificationRequest notificationRequest = mock(NotificationRequest.class);
                when(finremNotificationRequestMapper
                    .getNotificationRequestForStopRepresentingClientEmail(info.getCaseDetailsBefore(), barrister, intervenerType))
                    .thenReturn(notificationRequest);

                SendCorrespondenceEventWithDescription actual = underTest.prepareIntervenerBarristerEmailNotificationEvent(info,
                    intervenerType, barrister);

                assertAll(
                    () -> assertThat(actual)
                        .extracting(SendCorrespondenceEventWithDescription::getDescription)
                        .isEqualTo("notifying %s barrister".formatted(intervenerType.getTypeValue())),

                    () -> assertThat(actual)
                        .extracting(SendCorrespondenceEventWithDescription::getEvent)
                        .extracting(
                            SendCorrespondenceEvent::getNotificationParties,
                            SendCorrespondenceEvent::getEmailNotificationRequest,
                            SendCorrespondenceEvent::getEmailTemplate,
                            SendCorrespondenceEvent::getCaseDetails,
                            SendCorrespondenceEvent::getCaseDetailsBefore,
                            SendCorrespondenceEvent::getAuthToken,
                            SendCorrespondenceEvent::getBarrister
                        )
                        .containsExactly(
                            List.of(notificationParty),
                            notificationRequest,
                            emailTemplateNames,
                            finremCaseDetails,
                            finremCaseDetailsBefore,
                            AUTH_TOKEN,
                            barrister
                        ),
                    () -> verify(finremNotificationRequestMapper).getNotificationRequestForStopRepresentingClientEmail(finremCaseDetailsBefore,
                        barrister, intervenerType)
                );
            }
        }
    }

    @Nested
    class PrepareLitigantRevocationNotificationTests {

        @Test
        void shouldReturnEmptyEmailNotification_whenLitigantSolicitorWasNotRevoked() {
            LitigantRevocation litigantRevocation =
                new LitigantRevocation(false, false);
            List<SendCorrespondenceEventWithDescription> actual = underTest.prepareRepresentativeRevocationNotificationEvent(litigantRevocation,
                mock(StopRepresentingClientInfo.class));
            assertThat(actual).isEmpty();
        }

        @Test
        void shouldReturnEmptyLetterNotification_whenLitigantSolicitorWasNotRevoked() {
            LitigantRevocation litigantRevocation =
                new LitigantRevocation(false, false);
            List<SendCorrespondenceEventWithDescription> actual = underTest.prepareLitigantRevocationLetterNotificationEvents(litigantRevocation,
                mock(StopRepresentingClientInfo.class));
            assertThat(actual).isEmpty();
        }

        @Test
        void shouldPrepareApplicantSolicitorEmailNotification_whenApplicantSolicitorRevoked() {
            try (
                MockedStatic<EmailTemplateResolver> emailTemplateResolver = mockStatic(EmailTemplateResolver.class)) {
                EmailTemplateNames emailTemplateNames = mock(EmailTemplateNames.class);
                emailTemplateResolver.when(() -> EmailTemplateResolver.getNotifyApplicantRepresentativeTemplateName(any(FinremCaseData.class)))
                    .thenReturn(emailTemplateNames);

                LitigantRevocation litigantRevocation =
                    new LitigantRevocation(true, false);

                when(finremCaseDetails.getData()).thenReturn(finremCaseData);
                StopRepresentingClientInfo info = StopRepresentingClientInfo.builder()
                    .userAuthorisation(AUTH_TOKEN)
                    .caseDetails(finremCaseDetails)
                    .caseDetailsBefore(finremCaseDetailsBefore)
                    .build();

                NotificationRequest notificationRequest = mock(NotificationRequest.class);
                when(finremNotificationRequestMapper.getNotificationRequestForStopRepresentingClientEmail(info.getCaseDetailsBefore(), APP_SOLICITOR))
                    .thenReturn(notificationRequest);

                List<SendCorrespondenceEventWithDescription> actual = underTest
                    .prepareRepresentativeRevocationNotificationEvent(litigantRevocation, info);
                var eventWithDesc = actual.getFirst();
                var event = eventWithDesc.getEvent();

                assertThat(eventWithDesc.getDescription())
                    .isEqualTo("notifying applicant solicitor");

                assertThat(event)
                    .extracting(
                        SendCorrespondenceEvent::getNotificationParties,
                        SendCorrespondenceEvent::getCaseData,
                        SendCorrespondenceEvent::getEmailTemplate,
                        SendCorrespondenceEvent::getCaseDetails,
                        SendCorrespondenceEvent::getCaseDetailsBefore,
                        SendCorrespondenceEvent::getAuthToken
                    )
                    .containsExactly(
                        List.of(FORMER_APPLICANT_SOLICITOR_ONLY),
                        finremCaseData,
                        emailTemplateNames,
                        finremCaseDetails,
                        finremCaseDetailsBefore,
                        AUTH_TOKEN
                    );

                verify(finremNotificationRequestMapper)
                    .getNotificationRequestForStopRepresentingClientEmail(info.getCaseDetailsBefore(), APP_SOLICITOR);
            }
        }

        @Test
        void shouldPrepareApplicantLetterNotification_whenApplicantSolicitorRevoked() {
            LitigantRevocation litigantRevocation =
                new LitigantRevocation(true, false);

            when(finremCaseDetails.getData()).thenReturn(finremCaseData);
            StopRepresentingClientInfo info = StopRepresentingClientInfo.builder()
                .userAuthorisation(AUTH_TOKEN)
                .caseDetails(finremCaseDetails)
                .caseDetailsBefore(finremCaseDetailsBefore)
                .build();

            CaseDocument generatedDocument = caseDocument();
            when(underTest.generateStopRepresentingApplicantLetter(info.getCaseDetails(), AUTH_TOKEN))
                .thenReturn(generatedDocument);

            List<SendCorrespondenceEventWithDescription> actual = underTest
                .prepareLitigantRevocationLetterNotificationEvents(litigantRevocation, info);
            var eventWithDesc = actual.getFirst();
            var event = eventWithDesc.getEvent();

            assertAll(
                () -> assertThat(eventWithDesc.getDescription())
                    .isEqualTo("notifying applicant"),

                () -> assertThat(event.getNotificationParties())
                    .containsExactly(APPLICANT),

                () -> assertThat(event.getCaseData())
                    .isEqualTo(finremCaseData),

                () -> assertThat(event.getEmailTemplate())
                    .isNull(),

                () -> assertThat(event.getCaseDetails())
                    .isEqualTo(finremCaseDetails),

                () -> assertThat(event.getCaseDetailsBefore())
                    .isEqualTo(finremCaseDetailsBefore),

                () -> assertThat(event.getAuthToken())
                    .isEqualTo(AUTH_TOKEN),

                () -> assertThat(event.getDocumentsToPost())
                    .containsExactly(generatedDocument),

                () -> verify(underTest)
                    .generateStopRepresentingApplicantLetter(info.getCaseDetails(), AUTH_TOKEN)
            );
        }

        @Test
        void shouldPrepareRespondentSolicitorEmailNotification_whenRespondentSolicitorRevoked() {
            try (
                MockedStatic<EmailTemplateResolver> emailTemplateResolver = mockStatic(EmailTemplateResolver.class)) {
                EmailTemplateNames emailTemplateNames = mock(EmailTemplateNames.class);
                emailTemplateResolver.when(() -> EmailTemplateResolver.getNotifyRespondentRepresentativeTemplateName(any(FinremCaseData.class)))
                    .thenReturn(emailTemplateNames);

                LitigantRevocation litigantRevocation =
                    new LitigantRevocation(false, true);

                when(finremCaseDetails.getData()).thenReturn(finremCaseData);
                StopRepresentingClientInfo info = StopRepresentingClientInfo.builder()
                    .userAuthorisation(AUTH_TOKEN)
                    .caseDetails(finremCaseDetails)
                    .caseDetailsBefore(finremCaseDetailsBefore)
                    .build();

                NotificationRequest notificationRequest = mock(NotificationRequest.class);
                when(finremNotificationRequestMapper.getNotificationRequestForStopRepresentingClientEmail(info.getCaseDetailsBefore(),
                    RESP_SOLICITOR)).thenReturn(notificationRequest);

                List<SendCorrespondenceEventWithDescription> actual = underTest
                    .prepareRepresentativeRevocationNotificationEvent(litigantRevocation, info);
                var eventWithDesc = actual.getFirst();
                var event = eventWithDesc.getEvent();

                assertAll(
                    () -> assertThat(eventWithDesc.getDescription())
                        .isEqualTo("notifying respondent solicitor"),

                    () -> assertThat(event.getNotificationParties())
                        .containsExactly(FORMER_RESPONDENT_SOLICITOR_ONLY),

                    () -> assertThat(event.getCaseData())
                        .isEqualTo(finremCaseData),

                    () -> assertThat(event.getEmailTemplate())
                        .isEqualTo(emailTemplateNames),

                    () -> assertThat(event.getCaseDetails())
                        .isEqualTo(finremCaseDetails),

                    () -> assertThat(event.getCaseDetailsBefore())
                        .isEqualTo(finremCaseDetailsBefore),

                    () -> assertThat(event.getAuthToken())
                        .isEqualTo(AUTH_TOKEN),

                    () -> verify(finremNotificationRequestMapper)
                        .getNotificationRequestForStopRepresentingClientEmail(
                            info.getCaseDetailsBefore(), RESP_SOLICITOR
                        )
                );
            }
        }

        @Test
        void shouldPrepareRespondentLetterNotification_whenRespondentSolicitorRevoked() {
            LitigantRevocation litigantRevocation =
                new LitigantRevocation(false, true);

            when(finremCaseDetails.getData()).thenReturn(finremCaseData);
            StopRepresentingClientInfo info = StopRepresentingClientInfo.builder()
                .userAuthorisation(AUTH_TOKEN)
                .caseDetails(finremCaseDetails)
                .caseDetailsBefore(finremCaseDetailsBefore)
                .build();

            CaseDocument generatedDocument = caseDocument();
            when(underTest.generateStopRepresentingRespondentLetter(info.getCaseDetails(), AUTH_TOKEN))
                .thenReturn(generatedDocument);

            List<SendCorrespondenceEventWithDescription> actual = underTest
                .prepareLitigantRevocationLetterNotificationEvents(litigantRevocation, info);
            var eventWithDesc = actual.getFirst();
            var event = eventWithDesc.getEvent();

            assertAll(
                () -> assertThat(eventWithDesc.getDescription())
                    .isEqualTo("notifying respondent"),

                () -> assertThat(event.getNotificationParties())
                    .containsExactly(RESPONDENT),

                () -> assertThat(event.getCaseData())
                    .isEqualTo(finremCaseData),

                () -> assertThat(event.getEmailTemplate())
                    .isNull(),

                () -> assertThat(event.getCaseDetails())
                    .isEqualTo(finremCaseDetails),

                () -> assertThat(event.getCaseDetailsBefore())
                    .isEqualTo(finremCaseDetailsBefore),

                () -> assertThat(event.getAuthToken())
                    .isEqualTo(AUTH_TOKEN),

                () -> assertThat(event.getDocumentsToPost())
                    .containsExactly(generatedDocument),

                () -> verify(underTest)
                    .generateStopRepresentingRespondentLetter(info.getCaseDetails(), AUTH_TOKEN)
            );
        }
    }

    @ParameterizedTest
    @EnumSource(value = IntervenerType.class)
    void shouldPrepareIntervenerSolicitorEmailNotification(IntervenerType intervenerType) {
        try (
            MockedStatic<EmailTemplateResolver> emailTemplateResolver = mockStatic(EmailTemplateResolver.class);
            MockedStatic<NotificationParty> notificationPartyStatic = mockStatic(NotificationParty.class)) {
            EmailTemplateNames emailTemplateNames = mock(EmailTemplateNames.class);
            emailTemplateResolver.when(() -> EmailTemplateResolver.getNotifyIntervenerRepresentativeTemplateName(any(FinremCaseData.class)))
                .thenReturn(emailTemplateNames);
            NotificationParty notificationParty = mock(NotificationParty.class);
            notificationPartyStatic.when(() -> NotificationParty.getFormerIntervenerSolicitor(intervenerType))
                .thenReturn(notificationParty);

            StopRepresentingClientInfo info = mock(StopRepresentingClientInfo.class);
            when(info.getFinremCaseData()).thenReturn(finremCaseData);
            when(info.getCaseDetails()).thenReturn(finremCaseDetails);
            when(info.getCaseDetailsBefore()).thenReturn(finremCaseDetailsBefore);
            when(info.getUserAuthorisation()).thenReturn(AUTH_TOKEN);

            NotificationRequest notificationRequest = mock(NotificationRequest.class);
            when(finremNotificationRequestMapper
                .getNotificationRequestForStopRepresentingClientEmail(info.getCaseDetailsBefore(),
                    CaseRole.getIntervenerSolicitorByIndex(intervenerType.getIntervenerId()), intervenerType))
                .thenReturn(notificationRequest);

            SendCorrespondenceEventWithDescription actual = underTest.prepareIntervenerSolicitorEmailNotificationEvent(info,
                intervenerType);

            assertAll(
                () -> assertThat(actual)
                    .extracting(SendCorrespondenceEventWithDescription::getDescription)
                    .isEqualTo("notifying %s solicitor".formatted(intervenerType.getTypeValue())),

                () -> assertThat(actual)
                    .extracting(SendCorrespondenceEventWithDescription::getEvent)
                    .extracting(
                        SendCorrespondenceEvent::getNotificationParties,
                        SendCorrespondenceEvent::getEmailNotificationRequest,
                        SendCorrespondenceEvent::getEmailTemplate,
                        SendCorrespondenceEvent::getCaseDetails,
                        SendCorrespondenceEvent::getCaseDetailsBefore,
                        SendCorrespondenceEvent::getAuthToken
                    )
                    .containsExactly(
                        List.of(notificationParty),
                        notificationRequest,
                        emailTemplateNames,
                        finremCaseDetails,
                        finremCaseDetailsBefore,
                        AUTH_TOKEN
                    ),
                () -> verify(finremNotificationRequestMapper).getNotificationRequestForStopRepresentingClientEmail(finremCaseDetailsBefore,
                    CaseRole.getIntervenerSolicitorByIndex(intervenerType.getIntervenerId()), intervenerType)
            );
        }
    }

    @Test
    void shouldGenerateStopRepresentingRespondentLetter() {
        // Arrange
        CaseType caseType = mock(CaseType.class);
        when(finremCaseDetails.getCaseType()).thenReturn(caseType);
        when(documentConfiguration.getStopRepresentingLetterToRespondentTemplate())
            .thenReturn("RESPONDENT_TEMPLATE");
        Map<String, Object> respondentMap = mock(Map.class);
        when(letterDetailsMapper.getLetterDetailsAsMap(finremCaseDetails, DocumentHelper.PaperNotificationRecipient.RESPONDENT))
            .thenReturn(respondentMap);

        CaseDocument expectedDocument = mock(CaseDocument.class);
        when(genericDocumentService.generateDocumentFromPlaceholdersMap(
            eq(AUTH_TOKEN),
            anyMap(),
            eq("RESPONDENT_TEMPLATE"),
            anyString(),
            eq(caseType)
        )).thenReturn(expectedDocument);

        // Act
        CaseDocument actualDocument = null;
        try (MockedStatic<LocalDateTime> mockedStatic = Mockito.mockStatic(LocalDateTime.class)) {
            mockedStatic.when(LocalDateTime::now).thenReturn(fixedLocalDateTime);
            // Act
            actualDocument = underTest.generateStopRepresentingRespondentLetter(
                finremCaseDetails, AUTH_TOKEN
            );
        }

        // Assert
        assertThat(actualDocument).isEqualTo(expectedDocument);

        // Capture filename to verify format
        ArgumentCaptor<String> filenameCaptor = ArgumentCaptor.forClass(String.class);
        verify(genericDocumentService).generateDocumentFromPlaceholdersMap(
            eq(AUTH_TOKEN),
            eq(respondentMap),
            eq("RESPONDENT_TEMPLATE"),
            filenameCaptor.capture(),
            eq(caseType)
        );

        String filename = filenameCaptor.getValue();
        assertThat(filename).isEqualTo("RespondentRepresentationRemovalNotice_20260202121200.pdf");
    }

    @Test
    void shouldGenerateStopRepresentingApplicantLetter() {
        // Arrange
        CaseType caseType = mock(CaseType.class);
        when(finremCaseDetails.getCaseType()).thenReturn(caseType);
        when(documentConfiguration.getStopRepresentingLetterToApplicantTemplate())
            .thenReturn("APPLICANT_TEMPLATE");
        Map<String, Object> applicantMap = mock(Map.class);
        when(letterDetailsMapper.getLetterDetailsAsMap(finremCaseDetails, DocumentHelper.PaperNotificationRecipient.APPLICANT))
            .thenReturn(applicantMap);

        CaseDocument expectedDocument = mock(CaseDocument.class);
        when(genericDocumentService.generateDocumentFromPlaceholdersMap(
            eq(AUTH_TOKEN),
            anyMap(),
            eq("APPLICANT_TEMPLATE"),
            anyString(),
            eq(caseType)
        )).thenReturn(expectedDocument);

        // Act
        CaseDocument actualDocument = null;
        try (MockedStatic<LocalDateTime> mockedStatic = Mockito.mockStatic(LocalDateTime.class)) {
            mockedStatic.when(LocalDateTime::now).thenReturn(fixedLocalDateTime);
            // Act
            actualDocument = underTest.generateStopRepresentingApplicantLetter(
                finremCaseDetails, AUTH_TOKEN
            );
        }

        // Assert
        assertThat(actualDocument).isEqualTo(expectedDocument);

        // Capture filename to verify format
        ArgumentCaptor<String> filenameCaptor = ArgumentCaptor.forClass(String.class);
        verify(genericDocumentService).generateDocumentFromPlaceholdersMap(
            eq(AUTH_TOKEN),
            eq(applicantMap),
            eq("APPLICANT_TEMPLATE"),
            filenameCaptor.capture(),
            eq(caseType)
        );

        String filename = filenameCaptor.getValue();
        assertThat(filename).isEqualTo("ApplicantRepresentationRemovalNotice_20260202121200.pdf");
    }
}
