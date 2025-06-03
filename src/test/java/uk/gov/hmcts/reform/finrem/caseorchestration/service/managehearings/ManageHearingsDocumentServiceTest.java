package uk.gov.hmcts.reform.finrem.caseorchestration.service.managehearings;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import uk.gov.hmcts.reform.finrem.caseorchestration.config.DocumentConfiguration;
import uk.gov.hmcts.reform.finrem.caseorchestration.mapper.letterdetails.managehearings.HearingNoticeLetterDetailsMapper;
import uk.gov.hmcts.reform.finrem.caseorchestration.mapper.letterdetails.managehearings.ManageHearingFormCLetterDetailsMapper;
import uk.gov.hmcts.reform.finrem.caseorchestration.mapper.letterdetails.managehearings.ManageHearingFormGLetterDetailsMapper;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.CaseDocument;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.ExpressCaseParticipation;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.FinremCaseData;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.FinremCaseDetails;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.YesOrNo;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.wrapper.ExpressCaseWrapper;
import uk.gov.hmcts.reform.finrem.caseorchestration.service.GenericDocumentService;
import uk.gov.hmcts.reform.finrem.caseorchestration.service.express.ExpressCaseService;

import java.util.Map;
import java.util.stream.Stream;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.mockito.Mockito.eq;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static uk.gov.hmcts.reform.finrem.caseorchestration.TestConstants.AUTH_TOKEN;
import static uk.gov.hmcts.reform.finrem.caseorchestration.TestConstants.CASE_ID;

@ExtendWith(MockitoExtension.class)
class ManageHearingsDocumentServiceTest {

    @InjectMocks
    private ManageHearingsDocumentService manageHearingsDocumentService;

    @Mock
    private GenericDocumentService genericDocumentService;
    @Mock
    private DocumentConfiguration documentConfiguration;
    @Mock
    private HearingNoticeLetterDetailsMapper hearingNoticeLetterDetailsMapper;
    @Mock
    private ManageHearingFormCLetterDetailsMapper manageHearingFormCLetterDetailsMapper;
    @Mock
    private ManageHearingFormGLetterDetailsMapper formGLetterDetailsMapper;
    @Mock
    private ExpressCaseService expressCaseService;
    @Mock
    private StaticHearingDocumentService staticHearingDocumentService;

    private static final String HEARING_NOTICE_TEMPLATE = "hearingNoticeTemplate";
    private static final String HEARING_NOTICE_FILE_NAME = "hearingNoticeFileName";

    private static final String STANDARD_FORM_C = "standardFormCTemplate";
    private static final String EXPRESS_FORM_C = "expressFormCTemplate";
    private static final String FAST_TRACK_FORM_C = "fastTrackFormCTemplate";
    private static final String FORM_C_FILE_NAME = "formCFileName";

    private static final String FORM_G_TEMPLATE = "formGTemplate";
    private static final String FORM_G_FILE_NAME = "formGFileName";

    private static final String PFD_NCDR_COMPLIANCE_LETTER = "pfdNcdrComplianceLetter";
    private static final String PFD_NCDR_COMPLIANCE_LETTER_FILE_NAME = "pfdNcdrComplianceLetterFileName";

    private static final String PFD_NCDR_COVER_LETTER = "pfdNcdrCoverLetter";
    private static final String PFD_NCDR_COVER_LETTER_FILE_NAME = "pfdNcdrCoverLetterFileName";

    private static final String OUT_OF_COURT_RESOLUTION = "OutOfCourtResolution.pdf";

    private FinremCaseDetails finremCaseDetails;

    @BeforeEach
    void setUp() {
        finremCaseDetails = FinremCaseDetails
            .builder()
            .id(Long.valueOf(CASE_ID))
            .build();
    }

    @Test
    void shouldGenerateHearingNotice() {
        // Arrange
        Map<String, Object> documentDataMap = Map.of("key", "value");
        CaseDocument expectedDocument = CaseDocument
            .builder()
            .documentFilename(HEARING_NOTICE_FILE_NAME)
            .build();

        when(hearingNoticeLetterDetailsMapper.getDocumentTemplateDetailsAsMap(finremCaseDetails))
            .thenReturn(documentDataMap);
        when(documentConfiguration.getManageHearingNoticeTemplate(finremCaseDetails))
            .thenReturn(HEARING_NOTICE_TEMPLATE);
        when(documentConfiguration.getManageHearingNoticeFileName())
            .thenReturn(HEARING_NOTICE_FILE_NAME);

        when(genericDocumentService.generateDocumentFromPlaceholdersMap(
            AUTH_TOKEN, documentDataMap, HEARING_NOTICE_TEMPLATE, HEARING_NOTICE_FILE_NAME, CASE_ID))
            .thenReturn(expectedDocument);

        // Act
        CaseDocument actualDocument =
            manageHearingsDocumentService.generateHearingNotice(finremCaseDetails, AUTH_TOKEN);

        // Assert
        assertEquals(expectedDocument, actualDocument);
        verify(hearingNoticeLetterDetailsMapper).getDocumentTemplateDetailsAsMap(finremCaseDetails);
        verify(documentConfiguration).getManageHearingNoticeTemplate(finremCaseDetails);
        verify(documentConfiguration).getManageHearingNoticeFileName();
        verify(genericDocumentService).generateDocumentFromPlaceholdersMap(
            AUTH_TOKEN, documentDataMap, HEARING_NOTICE_TEMPLATE, "hearingNoticeFileName", CASE_ID);
    }

    @ParameterizedTest
    @MethodSource("provideCaseDataForFormCGeneration")
    void shouldGenerateFormC(boolean isExpressCase, boolean isFastTrackApplication, String expectedTemplate) {
        // Arrange
        Map<String, Object> documentDataMap = Map.of("key", "value");

        FinremCaseData caseData = FinremCaseData.builder()
            .expressCaseWrapper(
                ExpressCaseWrapper.builder()
                .expressCaseParticipation(isExpressCase
                    ? ExpressCaseParticipation.ENROLLED : ExpressCaseParticipation.DOES_NOT_QUALIFY)
                .build())
            .fastTrackDecision(isFastTrackApplication ? YesOrNo.YES : YesOrNo.NO)
            .build();
        finremCaseDetails.setData(caseData);

        when(manageHearingFormCLetterDetailsMapper.getDocumentTemplateDetailsAsMap(finremCaseDetails))
            .thenReturn(documentDataMap);
        when(expressCaseService.isExpressCase(caseData))
            .thenReturn(isExpressCase);

        if (isFastTrackApplication) {
            when(documentConfiguration.getFormCFastTrackTemplate(finremCaseDetails))
                .thenReturn(FAST_TRACK_FORM_C);
        }
        if (isExpressCase) {
            when(documentConfiguration.getManageHearingExpressFromCTemplate())
                .thenReturn(EXPRESS_FORM_C);
        }
        if (!isExpressCase && !isFastTrackApplication) {
            when(documentConfiguration.getFormCStandardTemplate(finremCaseDetails))
                .thenReturn(STANDARD_FORM_C);
        }

        when(documentConfiguration.getFormCFileName())
            .thenReturn(FORM_C_FILE_NAME);

        CaseDocument expectedDocument = CaseDocument
            .builder()
            .documentFilename(FORM_C_FILE_NAME)
            .build();

        when(genericDocumentService.generateDocumentFromPlaceholdersMap(
            AUTH_TOKEN, documentDataMap, expectedTemplate, FORM_C_FILE_NAME, CASE_ID))
            .thenReturn(expectedDocument);

        // Act
        CaseDocument actualDocument = manageHearingsDocumentService.generateFormC(finremCaseDetails, AUTH_TOKEN);

        // Assert
        assertEquals(expectedDocument, actualDocument);
        verify(manageHearingFormCLetterDetailsMapper).getDocumentTemplateDetailsAsMap(finremCaseDetails);
        verify(genericDocumentService).generateDocumentFromPlaceholdersMap(
            AUTH_TOKEN, documentDataMap, expectedTemplate, FORM_C_FILE_NAME, CASE_ID);
    }

    private static Stream<Arguments> provideCaseDataForFormCGeneration() {
        return Stream.of(
            Arguments.of(true, false, EXPRESS_FORM_C),
            Arguments.of(false, true, FAST_TRACK_FORM_C),
            Arguments.of(false, false, STANDARD_FORM_C)
        );
    }

    @Test
    void shouldGenerateFormG() {
        // Arrange
        Map<String, Object> documentDataMap = Map.of("key", "value");
        CaseDocument expectedDocument = CaseDocument
            .builder()
            .documentFilename(FORM_G_FILE_NAME)
            .build();

        when(formGLetterDetailsMapper.getDocumentTemplateDetailsAsMap(finremCaseDetails))
            .thenReturn(documentDataMap);
        when(documentConfiguration.getFormGTemplate(finremCaseDetails))
            .thenReturn(FORM_G_TEMPLATE);
        when(documentConfiguration.getFormGFileName())
            .thenReturn(FORM_G_FILE_NAME);

        when(genericDocumentService.generateDocumentFromPlaceholdersMap(
            AUTH_TOKEN, documentDataMap, FORM_G_TEMPLATE, FORM_G_FILE_NAME, CASE_ID))
            .thenReturn(expectedDocument);

        // Act
        CaseDocument actualDocument = manageHearingsDocumentService.generateFormG(finremCaseDetails, AUTH_TOKEN);

        // Assert
        assertEquals(expectedDocument, actualDocument);
        verify(formGLetterDetailsMapper).getDocumentTemplateDetailsAsMap(finremCaseDetails);
        verify(documentConfiguration).getFormGTemplate(finremCaseDetails);
        verify(documentConfiguration).getFormGFileName();
        verify(genericDocumentService).generateDocumentFromPlaceholdersMap(
            AUTH_TOKEN, documentDataMap, FORM_G_TEMPLATE,  FORM_G_FILE_NAME, CASE_ID);
    }

    @Test
    void shouldGeneratePfdNcdrDocumentsWithCoverSheet() {
        // Arrange
        CaseDocument complianceLetter = CaseDocument.builder()
            .documentFilename(PFD_NCDR_COMPLIANCE_LETTER_FILE_NAME)
            .build();

        CaseDocument coverLetter = CaseDocument.builder()
            .documentFilename(PFD_NCDR_COVER_LETTER_FILE_NAME)
            .build();

        when(staticHearingDocumentService.uploadPfdNcdrComplianceLetter(eq(CASE_ID), eq(AUTH_TOKEN)))
            .thenReturn(complianceLetter);

        when(staticHearingDocumentService.isPdfNcdrCoverSheetRequired(eq(finremCaseDetails)))
            .thenReturn(true);

        when(staticHearingDocumentService.uploadPfdNcdrCoverLetter(eq(CASE_ID), eq(AUTH_TOKEN)))
            .thenReturn(coverLetter);

        // Act
        Map<String, CaseDocument> documentMap =
            manageHearingsDocumentService.generatePfdNcdrDocuments(finremCaseDetails, AUTH_TOKEN);

        // Assert
        assertEquals(complianceLetter, documentMap.get(PFD_NCDR_COMPLIANCE_LETTER));
        assertEquals(coverLetter, documentMap.get(PFD_NCDR_COVER_LETTER));

        verify(staticHearingDocumentService).uploadPfdNcdrComplianceLetter(eq(CASE_ID), eq(AUTH_TOKEN));
        verify(staticHearingDocumentService).isPdfNcdrCoverSheetRequired(eq(finremCaseDetails));
        verify(staticHearingDocumentService).uploadPfdNcdrCoverLetter(eq(CASE_ID), eq(AUTH_TOKEN));
    }

    @Test
    void shouldGeneratePfdNcdrDocumentsWithoutCoverSheet() {
        // Arrange
        CaseDocument complianceLetter = CaseDocument.builder()
            .documentFilename(PFD_NCDR_COMPLIANCE_LETTER_FILE_NAME)
            .build();

        when(staticHearingDocumentService.uploadPfdNcdrComplianceLetter(CASE_ID, AUTH_TOKEN))
            .thenReturn(complianceLetter);

        when(staticHearingDocumentService.isPdfNcdrCoverSheetRequired(eq(finremCaseDetails)))
            .thenReturn(false);

        // Act
        Map<String, CaseDocument> documentMap =
            manageHearingsDocumentService.generatePfdNcdrDocuments(finremCaseDetails, AUTH_TOKEN);

        // Assert
        assertEquals(complianceLetter, documentMap.get(PFD_NCDR_COMPLIANCE_LETTER));
        assertNull(documentMap.get(PFD_NCDR_COVER_LETTER));

        verify(staticHearingDocumentService).uploadPfdNcdrComplianceLetter(eq(CASE_ID), eq(AUTH_TOKEN));
        verify(staticHearingDocumentService).isPdfNcdrCoverSheetRequired(eq(finremCaseDetails));
        verify(staticHearingDocumentService, never()).uploadPfdNcdrCoverLetter(eq(CASE_ID), eq(AUTH_TOKEN));
    }

    @Test
    void shouldGenerateOutOfCourtResolutionDoc() {
        // Arrange
        CaseDocument expectedDocument = CaseDocument.builder()
            .documentFilename(OUT_OF_COURT_RESOLUTION)
            .build();

        when(staticHearingDocumentService.uploadOutOfCourtResolutionDocument(CASE_ID, AUTH_TOKEN))
            .thenReturn(expectedDocument);

        // Act
        CaseDocument actualDocument =
            manageHearingsDocumentService.generateOutOfCourtResolutionDoc(finremCaseDetails, AUTH_TOKEN);

        // Assert
        assertEquals(expectedDocument, actualDocument);
        verify(staticHearingDocumentService).uploadOutOfCourtResolutionDocument(CASE_ID, AUTH_TOKEN);
    }
}
