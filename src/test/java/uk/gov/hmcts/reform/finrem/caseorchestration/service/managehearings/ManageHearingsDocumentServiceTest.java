package uk.gov.hmcts.reform.finrem.caseorchestration.service.managehearings;

import org.apache.commons.lang3.tuple.Pair;
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
import uk.gov.hmcts.reform.finrem.caseorchestration.mapper.letterdetails.managehearings.VacateOrAdjournNoticeLetterDetailsMapper;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.CaseDocument;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.CaseDocumentType;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.DocumentCollectionItem;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.ExpressCaseParticipation;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.FinremCaseData;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.FinremCaseDetails;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.Region;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.YesOrNo;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.managehearings.ManageHearingDocument;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.managehearings.ManageHearingDocumentsCollectionItem;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.managehearings.VacateOrAdjournAction;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.managehearings.hearings.Hearing;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.managehearings.hearings.ManageHearingsCollectionItem;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.wrapper.ExpressCaseWrapper;
import uk.gov.hmcts.reform.finrem.caseorchestration.service.GenericDocumentService;
import uk.gov.hmcts.reform.finrem.caseorchestration.service.express.ExpressCaseService;

import java.util.List;
import java.util.Map;
import java.util.stream.Stream;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static uk.gov.hmcts.reform.finrem.caseorchestration.TestConstants.AUTH_TOKEN;
import static uk.gov.hmcts.reform.finrem.caseorchestration.TestConstants.CASE_ID;
import static uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.CaseType.CONTESTED;
import static uk.gov.hmcts.reform.finrem.caseorchestration.model.document.DocumentCategory.SYSTEM_DUPLICATES;

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
    private VacateOrAdjournNoticeLetterDetailsMapper vacateOrAdjournNoticeLetterDetailsMapper;
    @Mock
    private ManageHearingFormCLetterDetailsMapper manageHearingFormCLetterDetailsMapper;
    @Mock
    private ManageHearingFormGLetterDetailsMapper formGLetterDetailsMapper;
    @Mock
    private ExpressCaseService expressCaseService;
    @Mock
    private StaticHearingDocumentService staticHearingDocumentService;

    private static final String HEARING_NOTICE_TEMPLATE = "hearingNoticeTemplate";
    private static final String VACATE_OR_ADJOURN_NOTICE_TEMPLATE = "vacateOrAdjournNoticeTemplate";
    private static final String HEARING_NOTICE_FILE_NAME = "hearingNoticeFileName";
    private static final String HEARING_NOTICE_FILE_URL = "hearingNoticeURL";
    private static final String VACATE_NOTICE_FILE_NAME = "vacateHearingNoticeFileName";
    private static final String ADJOURNED_NOTICE_FILE_NAME = "adjournedHearingNoticeFileName";

    private static final String VACATE_NOTICE_FILE_URL = "vacateHearingNoticeURL";
    private static final String ADJOURNED_NOTICE_FILE_URL = "adjournedHearingNoticeURL";

    private static final String FORM_A_URL = "formAURL";

    private static final String STANDARD_FORM_C = "standardFormCTemplate";
    private static final String EXPRESS_FORM_C = "expressFormCTemplate";
    private static final String FAST_TRACK_FORM_C = "fastTrackFormCTemplate";
    private static final String FORM_C_FILE_NAME = "formCFileName";
    private static final String FORM_C_URL = "formCURL";
    private static final String FORM_C_EXPRESS_URL = "formCExpressURL";
    private static final String FORM_C_FAST_TRACK_URL = "formCFastTrackURL";

    private static final String FORM_G_TEMPLATE = "formGTemplate";
    private static final String FORM_G_FILE_NAME = "formGFileName";
    private static final String FORM_G_URL = "formGURL";

    private static final String PFD_NCDR_COMPLIANCE_LETTER = "pfdNcdrComplianceLetter";
    private static final String PFD_NCDR_COMPLIANCE_LETTER_FILE_NAME = "pfdNcdrComplianceLetterFileName";
    private static final String PFD_NCDR_COMPLIANCE_LETTER_URL = "pfdNcdrComplianceLetterURL";

    private static final String PFD_NCDR_COVER_LETTER = "pfdNcdrCoverLetter";
    private static final String PFD_NCDR_COVER_LETTER_FILE_NAME = "pfdNcdrCoverLetterFileName";
    private static final String PFD_NCDR_COVER_LETTER_URL = "pfdNcdrCoverLetterURL";

    private static final String OUT_OF_COURT_RESOLUTION = "OutOfCourtResolution.pdf";
    private static final String OUT_OF_COURT_RESOLUTION_URL = "outOfCourtResolutionURL";

    private FinremCaseDetails finremCaseDetails;

    @BeforeEach
    void setUp() {
        finremCaseDetails = FinremCaseDetails
            .builder()
            .id(Long.valueOf(CASE_ID))
            .caseType(CONTESTED)
            .build();
    }

    @ParameterizedTest
    @MethodSource("hearingNoticeTestSetup")
    void shouldGenerateHearingNotice(Map<String, Object> documentDataMap, CaseDocument expectedDocument) {
        // Arrange
        when(hearingNoticeLetterDetailsMapper.getDocumentTemplateDetailsAsMap(finremCaseDetails))
            .thenReturn(documentDataMap);
        when(documentConfiguration.getManageHearingNoticeTemplate(Region.SOUTHWEST))
            .thenReturn(HEARING_NOTICE_TEMPLATE);
        when(documentConfiguration.getManageHearingNoticeFileName())
            .thenReturn(HEARING_NOTICE_FILE_NAME);

        when(genericDocumentService.generateDocumentFromPlaceholdersMap(
            AUTH_TOKEN, documentDataMap, HEARING_NOTICE_TEMPLATE, HEARING_NOTICE_FILE_NAME, CONTESTED))
            .thenReturn(expectedDocument);

        // Act
        CaseDocument actualDocument =
            manageHearingsDocumentService.generateHearingNotice(finremCaseDetails, Region.SOUTHWEST,  AUTH_TOKEN);

        // Assert
        assertEquals(expectedDocument, actualDocument);
        verify(hearingNoticeLetterDetailsMapper).getDocumentTemplateDetailsAsMap(finremCaseDetails);
        verify(documentConfiguration).getManageHearingNoticeTemplate(Region.SOUTHWEST);
        verify(documentConfiguration).getManageHearingNoticeFileName();
        verify(genericDocumentService).generateDocumentFromPlaceholdersMap(
            AUTH_TOKEN, documentDataMap, HEARING_NOTICE_TEMPLATE, "hearingNoticeFileName", CONTESTED);
    }

    @ParameterizedTest
    @MethodSource("vacateAndAdjournNoticeProvider")
    void shouldGenerateVacateOrAdjournNotice(Map<String, Object> documentDataMap,
                                             CaseDocument expectedDocument,
                                             VacateOrAdjournAction action,
                                             String expectedFileName) {
        // Arrange
        when(vacateOrAdjournNoticeLetterDetailsMapper.getDocumentTemplateDetailsAsMap(finremCaseDetails))
            .thenReturn(documentDataMap);
        when(documentConfiguration.getVacateOrAdjournNoticeTemplate(Region.SOUTHWEST))
            .thenReturn(VACATE_OR_ADJOURN_NOTICE_TEMPLATE);

        if (VacateOrAdjournAction.ADJOURN_HEARING.equals(action)) {
            when(documentConfiguration.getAdjournHearingNoticeFileName())
                .thenReturn(ADJOURNED_NOTICE_FILE_NAME);
        } else if (VacateOrAdjournAction.VACATE_HEARING.equals(action)) {
            when(documentConfiguration.getVacateHearingNoticeFileName())
                .thenReturn(VACATE_NOTICE_FILE_NAME);
        }

        when(genericDocumentService.generateDocumentFromPlaceholdersMap(
            AUTH_TOKEN, documentDataMap, VACATE_OR_ADJOURN_NOTICE_TEMPLATE, expectedFileName, CONTESTED))
            .thenReturn(expectedDocument);

        // Act
        CaseDocument actualDocument =
            manageHearingsDocumentService.generateVacateOrAdjournNotice(finremCaseDetails, Region.SOUTHWEST,  AUTH_TOKEN, action);

        // Assert
        assertEquals(expectedDocument, actualDocument);
        verify(vacateOrAdjournNoticeLetterDetailsMapper).getDocumentTemplateDetailsAsMap(finremCaseDetails);
        verify(documentConfiguration).getVacateOrAdjournNoticeTemplate(Region.SOUTHWEST);

        if (VacateOrAdjournAction.ADJOURN_HEARING.equals(action)) {
            verify(documentConfiguration).getAdjournHearingNoticeFileName();
        } else if (VacateOrAdjournAction.VACATE_HEARING.equals(action)) {
            verify(documentConfiguration).getVacateHearingNoticeFileName();
        }

        verify(genericDocumentService).generateDocumentFromPlaceholdersMap(
            AUTH_TOKEN, documentDataMap, VACATE_OR_ADJOURN_NOTICE_TEMPLATE, expectedFileName, CONTESTED);
    }

    private static Stream<Arguments> vacateAndAdjournNoticeProvider() {
        return Stream.of(
            Arguments.of(
                Map.of("key", "value"),
                CaseDocument.builder().documentFilename(VACATE_NOTICE_FILE_NAME).build(),
                VacateOrAdjournAction.VACATE_HEARING,
                VACATE_NOTICE_FILE_NAME),
            Arguments.of(
                Map.of("key", "value"),
                CaseDocument.builder().documentFilename(ADJOURNED_NOTICE_FILE_NAME).build(),
                VacateOrAdjournAction.ADJOURN_HEARING,
                ADJOURNED_NOTICE_FILE_NAME)
        );
    }

    /*
     * Used by shouldGenerateHearingNotice and shouldGenerateVacateHearingNotice
     */
    private static Stream<Arguments> hearingNoticeTestSetup() {
        return Stream.of(
            Arguments.of(
                Map.of("key", "value"),
                CaseDocument.builder().documentFilename(HEARING_NOTICE_FILE_NAME).build())
        );
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
            when(documentConfiguration.getManageHearingExpressFormCTemplate())
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
            AUTH_TOKEN, documentDataMap, expectedTemplate, FORM_C_FILE_NAME, CONTESTED))
            .thenReturn(expectedDocument);

        // Act
        CaseDocument actualDocument = manageHearingsDocumentService.generateFormC(finremCaseDetails, AUTH_TOKEN);

        // Assert
        assertEquals(expectedDocument, actualDocument);
        verify(manageHearingFormCLetterDetailsMapper).getDocumentTemplateDetailsAsMap(finremCaseDetails);
        verify(genericDocumentService).generateDocumentFromPlaceholdersMap(
            AUTH_TOKEN, documentDataMap, expectedTemplate, FORM_C_FILE_NAME, CONTESTED);
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
            AUTH_TOKEN, documentDataMap, FORM_G_TEMPLATE, FORM_G_FILE_NAME, CONTESTED))
            .thenReturn(expectedDocument);

        // Act
        CaseDocument actualDocument = manageHearingsDocumentService.generateFormG(finremCaseDetails, AUTH_TOKEN);

        // Assert
        assertEquals(expectedDocument, actualDocument);
        verify(formGLetterDetailsMapper).getDocumentTemplateDetailsAsMap(finremCaseDetails);
        verify(documentConfiguration).getFormGTemplate(finremCaseDetails);
        verify(documentConfiguration).getFormGFileName();
        verify(genericDocumentService).generateDocumentFromPlaceholdersMap(
            AUTH_TOKEN, documentDataMap, FORM_G_TEMPLATE, FORM_G_FILE_NAME, CONTESTED);
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

        when(staticHearingDocumentService.uploadPfdNcdrComplianceLetter(CONTESTED, AUTH_TOKEN))
            .thenReturn(complianceLetter);

        when(staticHearingDocumentService.isPdfNcdrCoverSheetRequired(finremCaseDetails))
            .thenReturn(true);

        when(staticHearingDocumentService.uploadPfdNcdrCoverLetter(CONTESTED, AUTH_TOKEN))
            .thenReturn(coverLetter);

        // Act
        Map<String, CaseDocument> documentMap =
            manageHearingsDocumentService.generatePfdNcdrDocuments(finremCaseDetails, AUTH_TOKEN);

        // Assert
        assertEquals(complianceLetter, documentMap.get(PFD_NCDR_COMPLIANCE_LETTER));
        assertEquals(coverLetter, documentMap.get(PFD_NCDR_COVER_LETTER));

        verify(staticHearingDocumentService).uploadPfdNcdrComplianceLetter(CONTESTED, AUTH_TOKEN);
        verify(staticHearingDocumentService).isPdfNcdrCoverSheetRequired(finremCaseDetails);
        verify(staticHearingDocumentService).uploadPfdNcdrCoverLetter(CONTESTED, AUTH_TOKEN);
    }

    @Test
    void shouldGeneratePfdNcdrDocumentsWithoutCoverSheet() {
        // Arrange
        CaseDocument complianceLetter = CaseDocument.builder()
            .documentFilename(PFD_NCDR_COMPLIANCE_LETTER_FILE_NAME)
            .build();

        when(staticHearingDocumentService.uploadPfdNcdrComplianceLetter(CONTESTED, AUTH_TOKEN))
            .thenReturn(complianceLetter);

        when(staticHearingDocumentService.isPdfNcdrCoverSheetRequired(finremCaseDetails)).thenReturn(false);

        // Act
        Map<String, CaseDocument> documentMap =
            manageHearingsDocumentService.generatePfdNcdrDocuments(finremCaseDetails, AUTH_TOKEN);

        // Assert
        assertEquals(complianceLetter, documentMap.get(PFD_NCDR_COMPLIANCE_LETTER));
        assertNull(documentMap.get(PFD_NCDR_COVER_LETTER));

        verify(staticHearingDocumentService).uploadPfdNcdrComplianceLetter(CONTESTED, AUTH_TOKEN);
        verify(staticHearingDocumentService).isPdfNcdrCoverSheetRequired(finremCaseDetails);
        verify(staticHearingDocumentService, never()).uploadPfdNcdrCoverLetter(CONTESTED, AUTH_TOKEN);
    }

    @Test
    void categoriseSystemDuplicateDocs_shouldGenerateOutOfCourtResolutionDoc() {
        // Arrange
        CaseDocument expectedDocument = CaseDocument.builder()
            .documentFilename(OUT_OF_COURT_RESOLUTION)
            .build();

        when(staticHearingDocumentService.uploadOutOfCourtResolutionDocument(CONTESTED, AUTH_TOKEN))
            .thenReturn(expectedDocument);

        // Act
        CaseDocument actualDocument =
            manageHearingsDocumentService.generateOutOfCourtResolutionDoc(finremCaseDetails, AUTH_TOKEN);

        // Assert
        assertEquals(expectedDocument, actualDocument);
        verify(staticHearingDocumentService).uploadOutOfCourtResolutionDocument(CONTESTED, AUTH_TOKEN);
    }

    @Test
    void shouldCategoriseSystemDuplicateDocs() {
        // Arrange
        String expectedCategoryId = SYSTEM_DUPLICATES.getDocumentCategoryId();

        ManageHearingsCollectionItem hearingItem = ManageHearingsCollectionItem.builder()
            .value(Hearing.builder()
                .additionalHearingDocs(List.of(
                    DocumentCollectionItem
                        .builder()
                        .value(CaseDocument
                            .builder()
                            .categoryId(null)
                            .build())
                        .build()))
                .build())
            .build();

        ManageHearingDocumentsCollectionItem hearingDocumentItem = ManageHearingDocumentsCollectionItem.builder()
            .value(ManageHearingDocument.builder()
                .hearingDocument(CaseDocument
                    .builder()
                    .categoryId(null)
                    .build())
                .build())
            .build();

        List<ManageHearingsCollectionItem> hearings = List.of(hearingItem);
        List<ManageHearingDocumentsCollectionItem> hearingDocuments = List.of(hearingDocumentItem);

        // Act
        manageHearingsDocumentService.categoriseSystemDuplicateDocs(hearings, hearingDocuments);

        // Assert
        assertEquals(expectedCategoryId, hearingItem.getValue().getAdditionalHearingDocs().getFirst().getValue().getCategoryId());
        assertEquals(expectedCategoryId, hearingDocumentItem.getValue().getHearingDocument().getCategoryId());
    }

    @Test
    void determineFormCTemplateShouldReturnExpressFormC() {
        when(expressCaseService.isExpressCase(finremCaseDetails.getData())).thenReturn(true);
        when(documentConfiguration.getManageHearingExpressFormCTemplate()).thenReturn("an express template");

        Pair<CaseDocumentType, String> result = manageHearingsDocumentService.determineFormCTemplate(finremCaseDetails);

        assertEquals(CaseDocumentType.FORM_C_EXPRESS, result.getLeft());
        assertEquals("an express template", result.getRight());
    }

    @Test
    void determineFormCTemplateShouldReturnFastTrackFormC() {
        FinremCaseData someCaseData = FinremCaseData.builder()
            .fastTrackDecision(YesOrNo.YES)
            .build();
        finremCaseDetails.setData(someCaseData);
        when(expressCaseService.isExpressCase(finremCaseDetails.getData())).thenReturn(false);
        when(documentConfiguration.getFormCFastTrackTemplate(finremCaseDetails)).thenReturn("a fast track template");

        Pair<CaseDocumentType, String> result = manageHearingsDocumentService.determineFormCTemplate(finremCaseDetails);

        assertEquals(CaseDocumentType.FORM_C_FAST_TRACK, result.getLeft());
        assertEquals("a fast track template", result.getRight());
    }

    @Test
    void determineFormCTemplateShouldReturnStandardFormC() {
        FinremCaseData someCaseData = FinremCaseData.builder()
            .fastTrackDecision(YesOrNo.NO)
            .build();
        finremCaseDetails.setData(someCaseData);
        when(expressCaseService.isExpressCase(finremCaseDetails.getData())).thenReturn(false);
        when(documentConfiguration.getFormCStandardTemplate(finremCaseDetails)).thenReturn("a standard form C template");

        Pair<CaseDocumentType, String> result = manageHearingsDocumentService.determineFormCTemplate(finremCaseDetails);

        assertEquals(CaseDocumentType.FORM_C, result.getLeft());
        assertEquals("a standard form C template", result.getRight());
    }
}
