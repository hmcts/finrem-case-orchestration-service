package uk.gov.hmcts.reform.finrem.caseorchestration.service.stoprepresentingclient;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockedStatic;
import org.mockito.Mockito;
import org.mockito.junit.jupiter.MockitoExtension;
import uk.gov.hmcts.reform.finrem.caseorchestration.config.DocumentConfiguration;
import uk.gov.hmcts.reform.finrem.caseorchestration.helper.DocumentHelper;
import uk.gov.hmcts.reform.finrem.caseorchestration.mapper.letterdetails.LetterDetailsMapper;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.CaseDocument;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.CaseType;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.FinremCaseDetails;
import uk.gov.hmcts.reform.finrem.caseorchestration.service.GenericDocumentService;

import java.time.LocalDateTime;
import java.util.Map;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.anyMap;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static uk.gov.hmcts.reform.finrem.caseorchestration.TestConstants.AUTH_TOKEN;

@ExtendWith(MockitoExtension.class)
class StopRepresentingClientLetterServiceTest {

    static LocalDateTime fixedLocalDateTime = LocalDateTime.of(2026, 2, 2, 12, 12);

    @Mock
    private GenericDocumentService genericDocumentService;

    @Mock
    private DocumentConfiguration documentConfiguration;

    @Mock
    private LetterDetailsMapper letterDetailsMapper;

    @InjectMocks
    private StopRepresentingClientLetterService underTest;

    private FinremCaseDetails caseDetails;

    @BeforeEach
    void setUp() {
        caseDetails = mock(FinremCaseDetails.class);
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
