package uk.gov.hmcts.reform.finrem.caseorchestration.service.stoprepresentingclient;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;
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
import uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.CaseDocument;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.CaseType;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.FinremCaseData;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.FinremCaseDetails;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.notification.NotificationRequest;
import uk.gov.hmcts.reform.finrem.caseorchestration.notifications.notifiers.SendCorrespondenceEvent;
import uk.gov.hmcts.reform.finrem.caseorchestration.notifications.notifiers.SendCorrespondenceEventWithDescription;
import uk.gov.hmcts.reform.finrem.caseorchestration.service.GenericDocumentService;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertAll;
import static org.mockito.ArgumentMatchers.anyMap;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static uk.gov.hmcts.reform.finrem.caseorchestration.TestConstants.AUTH_TOKEN;
import static uk.gov.hmcts.reform.finrem.caseorchestration.TestSetUpUtils.caseDocument;
import static uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.CaseRole.APP_SOLICITOR;
import static uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.CaseRole.RESP_SOLICITOR;
import static uk.gov.hmcts.reform.finrem.caseorchestration.notifications.domain.EmailTemplateNames.FR_CONSENTED_REPRESENTATIVE_STOP_REPRESENTING_APPLICANT;
import static uk.gov.hmcts.reform.finrem.caseorchestration.notifications.domain.EmailTemplateNames.FR_CONSENTED_REPRESENTATIVE_STOP_REPRESENTING_RESPONDENT;
import static uk.gov.hmcts.reform.finrem.caseorchestration.notifications.domain.EmailTemplateNames.FR_CONTESTED_REPRESENTATIVE_STOP_REPRESENTING_APPLICANT;
import static uk.gov.hmcts.reform.finrem.caseorchestration.notifications.domain.EmailTemplateNames.FR_CONTESTED_REPRESENTATIVE_STOP_REPRESENTING_RESPONDENT;
import static uk.gov.hmcts.reform.finrem.caseorchestration.notifications.notifiers.NotificationParty.APPLICANT;
import static uk.gov.hmcts.reform.finrem.caseorchestration.notifications.notifiers.NotificationParty.FORMER_APPLICANT_SOLICITOR_ONLY;
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

    private FinremCaseDetails caseDetails;

    @BeforeEach
    void setUp() {
        caseDetails = mock(FinremCaseDetails.class);
    }

    @Nested
    class PrepareLitigantRevocationNotificationTests {

        @Test
        void shouldReturnEmptyEmailNotification_whenLitigantSolicitorWasNotRevoked() {
            LitigantRevocation litigantRevocation =
                new LitigantRevocation(false, false);
            List<SendCorrespondenceEventWithDescription> actual = underTest.prepareLitigantRevocationNotificationEvents(litigantRevocation,
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

        @ParameterizedTest
        @ValueSource(booleans = {true, false})
        void shouldPrepareApplicantSolicitorEmailNotification_whenApplicantSolicitorRevoked(boolean isContested) {
            LitigantRevocation litigantRevocation =
                new LitigantRevocation(true, false);

            FinremCaseData finremCaseData = mock(FinremCaseData.class);
            when(finremCaseData.isContestedApplication()).thenReturn(isContested);
            FinremCaseDetails infoCaseDetails = mock(FinremCaseDetails.class);
            when(infoCaseDetails.getData()).thenReturn(finremCaseData);
            FinremCaseDetails infoCaseDetailsBefore = mock(FinremCaseDetails.class);
            StopRepresentingClientInfo info = StopRepresentingClientInfo.builder()
                .userAuthorisation(AUTH_TOKEN)
                .caseDetails(infoCaseDetails)
                .caseDetailsBefore(infoCaseDetailsBefore)
                .build();

            NotificationRequest notificationRequest = mock(NotificationRequest.class);
            when(finremNotificationRequestMapper.getNotificationRequestForStopRepresentingClientEmail(info.getCaseDetailsBefore(), APP_SOLICITOR))
                .thenReturn(notificationRequest);

            List<SendCorrespondenceEventWithDescription> actual = underTest.prepareLitigantRevocationNotificationEvents(litigantRevocation, info);
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
                    isContested ? FR_CONTESTED_REPRESENTATIVE_STOP_REPRESENTING_APPLICANT
                        : FR_CONSENTED_REPRESENTATIVE_STOP_REPRESENTING_APPLICANT,
                    infoCaseDetails,
                    infoCaseDetailsBefore,
                    AUTH_TOKEN
                );

            verify(finremNotificationRequestMapper)
                .getNotificationRequestForStopRepresentingClientEmail(info.getCaseDetailsBefore(), APP_SOLICITOR);
        }

        @Test
        void shouldPrepareApplicantLetterNotification_whenApplicantSolicitorRevoked() {
            LitigantRevocation litigantRevocation =
                new LitigantRevocation(true, false);
            FinremCaseData finremCaseData = mock(FinremCaseData.class);

            FinremCaseDetails infoCaseDetails = mock(FinremCaseDetails.class);
            when(infoCaseDetails.getData()).thenReturn(finremCaseData);
            FinremCaseDetails infoCaseDetailsBefore = mock(FinremCaseDetails.class);
            StopRepresentingClientInfo info = StopRepresentingClientInfo.builder()
                .userAuthorisation(AUTH_TOKEN)
                .caseDetails(infoCaseDetails)
                .caseDetailsBefore(infoCaseDetailsBefore)
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
                    .isEqualTo(infoCaseDetails),

                () -> assertThat(event.getCaseDetailsBefore())
                    .isEqualTo(infoCaseDetailsBefore),

                () -> assertThat(event.getAuthToken())
                    .isEqualTo(AUTH_TOKEN),

                () -> assertThat(event.getDocumentsToPost())
                    .containsExactly(generatedDocument),

                () -> verify(underTest)
                    .generateStopRepresentingApplicantLetter(info.getCaseDetails(), AUTH_TOKEN)
            );
        }

        @ParameterizedTest
        @ValueSource(booleans = {true, false})
        void shouldPrepareRespondentSolicitorEmailNotification_whenRespondentSolicitorRevoked(boolean isContested) {
            LitigantRevocation litigantRevocation =
                new LitigantRevocation(false, true);
            FinremCaseData finremCaseData = mock(FinremCaseData.class);
            when(finremCaseData.isContestedApplication()).thenReturn(isContested);

            FinremCaseDetails infoCaseDetails = mock(FinremCaseDetails.class);
            when(infoCaseDetails.getData()).thenReturn(finremCaseData);
            FinremCaseDetails infoCaseDetailsBefore = mock(FinremCaseDetails.class);
            StopRepresentingClientInfo info = StopRepresentingClientInfo.builder()
                .userAuthorisation(AUTH_TOKEN)
                .caseDetails(infoCaseDetails)
                .caseDetailsBefore(infoCaseDetailsBefore)
                .build();

            NotificationRequest notificationRequest = mock(NotificationRequest.class);
            when(finremNotificationRequestMapper.getNotificationRequestForStopRepresentingClientEmail(info.getCaseDetailsBefore(), RESP_SOLICITOR))
                .thenReturn(notificationRequest);

            List<SendCorrespondenceEventWithDescription> actual = underTest.prepareLitigantRevocationNotificationEvents(litigantRevocation, info);
            var eventWithDesc = actual.getFirst();
            var event = eventWithDesc.getEvent();

            var expectedTemplate = isContested
                ? FR_CONTESTED_REPRESENTATIVE_STOP_REPRESENTING_RESPONDENT
                : FR_CONSENTED_REPRESENTATIVE_STOP_REPRESENTING_RESPONDENT;

            assertAll(
                () -> assertThat(eventWithDesc.getDescription())
                    .isEqualTo("notifying respondent solicitor"),

                () -> assertThat(event.getNotificationParties())
                    .containsExactly(FORMER_RESPONDENT_SOLICITOR_ONLY),

                () -> assertThat(event.getCaseData())
                    .isEqualTo(finremCaseData),

                () -> assertThat(event.getEmailTemplate())
                    .isEqualTo(expectedTemplate),

                () -> assertThat(event.getCaseDetails())
                    .isEqualTo(infoCaseDetails),

                () -> assertThat(event.getCaseDetailsBefore())
                    .isEqualTo(infoCaseDetailsBefore),

                () -> assertThat(event.getAuthToken())
                    .isEqualTo(AUTH_TOKEN),

                () -> verify(finremNotificationRequestMapper)
                    .getNotificationRequestForStopRepresentingClientEmail(
                        info.getCaseDetailsBefore(), RESP_SOLICITOR
                    )
            );
        }

        @Test
        void shouldPrepareRespondentLetterNotification_whenRespondentSolicitorRevoked() {
            LitigantRevocation litigantRevocation =
                new LitigantRevocation(false, true);
            FinremCaseData finremCaseData = mock(FinremCaseData.class);

            FinremCaseDetails infoCaseDetails = mock(FinremCaseDetails.class);
            when(infoCaseDetails.getData()).thenReturn(finremCaseData);
            FinremCaseDetails infoCaseDetailsBefore = mock(FinremCaseDetails.class);
            StopRepresentingClientInfo info = StopRepresentingClientInfo.builder()
                .userAuthorisation(AUTH_TOKEN)
                .caseDetails(infoCaseDetails)
                .caseDetailsBefore(infoCaseDetailsBefore)
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
                    .isEqualTo(infoCaseDetails),

                () -> assertThat(event.getCaseDetailsBefore())
                    .isEqualTo(infoCaseDetailsBefore),

                () -> assertThat(event.getAuthToken())
                    .isEqualTo(AUTH_TOKEN),

                () -> assertThat(event.getDocumentsToPost())
                    .containsExactly(generatedDocument),

                () -> verify(underTest)
                    .generateStopRepresentingRespondentLetter(info.getCaseDetails(), AUTH_TOKEN)
            );
        }
    }

    @Test
    void shouldGenerateStopRepresentingRespondentLetter() {
        // Arrange
        CaseType caseType = mock(CaseType.class);
        when(caseDetails.getCaseType()).thenReturn(caseType);
        when(documentConfiguration.getStopRepresentingLetterToRespondentTemplate())
            .thenReturn("RESPONDENT_TEMPLATE");
        Map<String, Object> respondentMap = mock(Map.class);
        when(letterDetailsMapper.getLetterDetailsAsMap(caseDetails, DocumentHelper.PaperNotificationRecipient.RESPONDENT))
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
                caseDetails, AUTH_TOKEN
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
        when(caseDetails.getCaseType()).thenReturn(caseType);
        when(documentConfiguration.getStopRepresentingLetterToApplicantTemplate())
            .thenReturn("APPLICANT_TEMPLATE");
        Map<String, Object> applicantMap = mock(Map.class);
        when(letterDetailsMapper.getLetterDetailsAsMap(caseDetails, DocumentHelper.PaperNotificationRecipient.APPLICANT))
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
                caseDetails, AUTH_TOKEN
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
