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
import uk.gov.hmcts.reform.finrem.caseorchestration.service.StaticDocumentService;
import uk.gov.hmcts.reform.finrem.caseorchestration.service.express.ExpressCaseService;

import java.util.Map;
import java.util.stream.Stream;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.mockito.Mockito.*;
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
    private StaticDocumentService staticDocumentService;

    private static final String HEARING_NOTICE_TEMPLATE = "HearingNoticeTemplate";
    private static final String STANDARD_FORM_C = "StandardFormCTemplate";
    private static final String EXPRESS_FORM_C = "ExpressFormCTemplate";
    private static final String FAST_TRACK_FORM_C = "FastTrackFormCTemplate";
    private static final String PFD_NCDR_COMPLIANCE_LETTER = "PfdNcdrComplianceLetter";
    private static final String PFD_NCDR_COVER_LETTER = "PfdNcdrCoverLetter";



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
            .documentFilename("hearingNoticeFileName")
            .documentBinaryUrl("hearingNoticeBinaryUrl")
            .documentUrl("hearingNoticeUrl")
            .build();

        when(hearingNoticeLetterDetailsMapper.getDocumentTemplateDetailsAsMap(finremCaseDetails))
            .thenReturn(documentDataMap);
        when(documentConfiguration.getManageHearingNoticeTemplate(finremCaseDetails))
            .thenReturn(HEARING_NOTICE_TEMPLATE);
        when(documentConfiguration.getManageHearingNoticeFileName())
            .thenReturn("hearingNoticeFileName");

        when(genericDocumentService.generateDocumentFromPlaceholdersMap(
            AUTH_TOKEN, documentDataMap, HEARING_NOTICE_TEMPLATE, "hearingNoticeFileName", CASE_ID))
            .thenReturn(expectedDocument);

        // Act
        CaseDocument actualDocument = manageHearingsDocumentService.generateHearingNotice(finremCaseDetails, AUTH_TOKEN);

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
        CaseDocument expectedDocument = CaseDocument
            .builder()
            .documentFilename("formCFileName")
            .documentBinaryUrl("formCBinaryUrl")
            .documentUrl("formCUrl")
            .build();

        FinremCaseData caseData = FinremCaseData.builder()
            .expressCaseWrapper(
                ExpressCaseWrapper.builder()
                .expressCaseParticipation(isExpressCase ? ExpressCaseParticipation.ENROLLED : ExpressCaseParticipation.DOES_NOT_QUALIFY)
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
            .thenReturn("formCFileName");

        when(genericDocumentService.generateDocumentFromPlaceholdersMap(
            AUTH_TOKEN, documentDataMap, expectedTemplate, "formCFileName", CASE_ID))
            .thenReturn(expectedDocument);

        // Act
        CaseDocument actualDocument = manageHearingsDocumentService.generateFormC(finremCaseDetails, AUTH_TOKEN);

        // Assert
        assertEquals(expectedDocument, actualDocument);
        verify(manageHearingFormCLetterDetailsMapper).getDocumentTemplateDetailsAsMap(finremCaseDetails);
        verify(genericDocumentService).generateDocumentFromPlaceholdersMap(
            AUTH_TOKEN, documentDataMap, expectedTemplate, "formCFileName", CASE_ID);
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
            .documentFilename("formGFileName")
            .documentBinaryUrl("formGBinaryUrl")
            .documentUrl("formGUrl")
            .build();

        when(formGLetterDetailsMapper.getDocumentTemplateDetailsAsMap(finremCaseDetails))
            .thenReturn(documentDataMap);
        when(documentConfiguration.getFormGTemplate(finremCaseDetails))
            .thenReturn("formGTemplate");
        when(documentConfiguration.getFormGFileName())
            .thenReturn("formGFileName");

        when(genericDocumentService.generateDocumentFromPlaceholdersMap(
            AUTH_TOKEN, documentDataMap, "formGTemplate", "formGFileName", CASE_ID))
            .thenReturn(expectedDocument);

        // Act
        CaseDocument actualDocument = manageHearingsDocumentService.generateFormG(finremCaseDetails, AUTH_TOKEN);

        // Assert
        assertEquals(expectedDocument, actualDocument);
        verify(formGLetterDetailsMapper).getDocumentTemplateDetailsAsMap(finremCaseDetails);
        verify(documentConfiguration).getFormGTemplate(finremCaseDetails);
        verify(documentConfiguration).getFormGFileName();
        verify(genericDocumentService).generateDocumentFromPlaceholdersMap(
            AUTH_TOKEN, documentDataMap, "formGTemplate", "formGFileName", CASE_ID);
    }

    @ParameterizedTest
    @MethodSource("provideCoverSheetRequiredValues")
    void shouldGeneratePfdNcdrDocuments(boolean isCoverSheetRequired) {
        // Arrange
        CaseDocument complianceLetter = CaseDocument.builder()
            .documentFilename("complianceLetterFileName")
            .documentBinaryUrl("complianceLetterBinaryUrl")
            .documentUrl("complianceLetterUrl")
            .build();

        CaseDocument coverLetter = CaseDocument.builder()
            .documentFilename("coverLetterFileName")
            .documentBinaryUrl("coverLetterBinaryUrl")
            .documentUrl("coverLetterUrl")
            .build();

        when(staticDocumentService.uploadPfdNcdrComplianceLetter(eq(CASE_ID), eq(AUTH_TOKEN)))
            .thenReturn(complianceLetter);

        // Ensure proper stubbing for cover sheet requirement
        when(staticDocumentService.isPdfNcdrCoverSheetRequired(eq(finremCaseDetails)))
            .thenReturn(isCoverSheetRequired);

        if (isCoverSheetRequired) {
            // Ensure proper stubbing for cover letter
            when(staticDocumentService.uploadPfdNcdrCoverLetter(eq(CASE_ID), eq(AUTH_TOKEN)))
                .thenReturn(coverLetter);
        }

        // Act
        Map<String, CaseDocument> documentMap = manageHearingsDocumentService.generatePfdNcdrDocuments(finremCaseDetails, AUTH_TOKEN);

        // Assert
        assertEquals(complianceLetter, documentMap.get(PFD_NCDR_COMPLIANCE_LETTER));
        if (isCoverSheetRequired) {
            assertEquals(coverLetter, documentMap.get(PFD_NCDR_COVER_LETTER));
        } else {
            assertNull(documentMap.get(PFD_NCDR_COVER_LETTER));
        }

        verify(staticDocumentService).uploadPfdNcdrComplianceLetter(eq(CASE_ID), eq(AUTH_TOKEN));
        verify(staticDocumentService).isPdfNcdrCoverSheetRequired(eq(finremCaseDetails));
        if (isCoverSheetRequired) {
            verify(staticDocumentService).uploadPfdNcdrCoverLetter(eq(CASE_ID), eq(AUTH_TOKEN));
        } else {
            verify(staticDocumentService, never()).uploadPfdNcdrCoverLetter(eq(CASE_ID), eq(AUTH_TOKEN));
        }
    }

    private static Stream<Arguments> provideCoverSheetRequiredValues() {
        return Stream.of(
            Arguments.of(true),  // Cover sheet required
            Arguments.of(false)  // Cover sheet not required
        );
    }

}
