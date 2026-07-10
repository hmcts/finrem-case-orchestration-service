package uk.gov.hmcts.reform.finrem.caseorchestration.service;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.EnumSource;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import uk.gov.hmcts.reform.finrem.caseorchestration.config.DocumentConfiguration;
import uk.gov.hmcts.reform.finrem.caseorchestration.helper.DocumentHelper;
import uk.gov.hmcts.reform.finrem.caseorchestration.mapper.letterdetails.bulkprint.BulkPrintCoverLetterDetailsMapper;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.CaseDocument;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.CaseType;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.FinremCaseData;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.FinremCaseDetails;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.YesOrNo;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.wrapper.BulkPrintCoversheetWrapper;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.wrapper.ContactDetailsWrapper;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.wrapper.RegionWrapper;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.intervener.IntervenerChangeDetails;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.intervener.IntervenerType;
import uk.gov.hmcts.reform.finrem.caseorchestration.util.TestLogger;
import uk.gov.hmcts.reform.finrem.caseorchestration.util.TestLogs;

import java.util.Map;
import java.util.Objects;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static uk.gov.hmcts.reform.finrem.caseorchestration.TestConstants.AUTH_TOKEN;
import static uk.gov.hmcts.reform.finrem.caseorchestration.TestConstants.CASE_ID_IN_LONG;
import static uk.gov.hmcts.reform.finrem.caseorchestration.TestConstants.TEST_DOCUMENT_BINARY_URL;
import static uk.gov.hmcts.reform.finrem.caseorchestration.TestConstants.TEST_DOCUMENT_FILENAME;
import static uk.gov.hmcts.reform.finrem.caseorchestration.TestConstants.TEST_DOCUMENT_URL;
import static uk.gov.hmcts.reform.finrem.caseorchestration.TestSetUpUtils.caseDocument;
import static uk.gov.hmcts.reform.finrem.caseorchestration.helper.DocumentHelper.PaperNotificationRecipient.APPLICANT;

@ExtendWith(MockitoExtension.class)
class GenerateCoverSheetServiceTest {

    @TestLogs
    private final TestLogger logs = new TestLogger(GenerateCoverSheetService.class);

    private static final String BULK_PRINT_TEMPLATE = "bulk-print-template";
    private static final String BULK_PRINT_FILE_NAME = "bulk-print.pdf";
    public static final String OLD_COVERSHEET_URL = "http://dm-store:8080/documents/old-app-cover-sheet";

    @Mock
    private GenericDocumentService genericDocumentService;
    @Mock
    private DocumentConfiguration documentConfiguration;
    @Mock
    private BulkPrintCoverLetterDetailsMapper bulkPrintCoverLetterDetailsMapper;
    @Mock
    private FeatureToggleService featureToggleService;
    @InjectMocks
    private GenerateCoverSheetService generateCoverSheetService;
    @Mock
    private CaseType caseType;

    @Test
    void shouldGenerateApplicantCoverSheet() {
        setUpDocGenerationMocks();
        FinremCaseDetails caseDetails = getCaseDetails(YesOrNo.NO, YesOrNo.NO);

        CaseDocument result = generateCoverSheetService.generateApplicantCoverSheet(caseDetails, AUTH_TOKEN);

        assertGeneratedDocument(result);
        verify(genericDocumentService).generateDocumentFromPlaceholdersMap(
            eq(AUTH_TOKEN),
            any(),
            eq(BULK_PRINT_TEMPLATE),
            eq(BULK_PRINT_FILE_NAME),
            eq(caseType)
        );
        verify(bulkPrintCoverLetterDetailsMapper).getLetterDetailsAsMap(caseDetails, APPLICANT,
            caseDetails.getData().getRegionWrapper().getDefaultCourtList());
    }

    @Test
    void shouldGenerateAndSetApplicantCoverSheet_whenAddressNotHidden() {
        setUpDocGenerationMocks();
        when(featureToggleService.isDeleteOldBpCoversheetEnabled()).thenReturn(true);
        FinremCaseDetails caseDetails = getCaseDetails(YesOrNo.NO, YesOrNo.NO);
        caseDetails.getData().getBulkPrintCoversheetWrapper().setBulkPrintCoverSheetApp(
            caseDocument(OLD_COVERSHEET_URL, BULK_PRINT_FILE_NAME)
        );

        generateCoverSheetService.generateAndSetApplicantCoverSheet(caseDetails, AUTH_TOKEN);

        assertEquals(TEST_DOCUMENT_FILENAME,
            caseDetails.getData().getBulkPrintCoversheetWrapper().getBulkPrintCoverSheetApp().getDocumentFilename());
        assertNull(caseDetails.getData().getBulkPrintCoversheetWrapper().getBulkPrintCoverSheetAppConfidential());
        assertThat(caseDetails.getData().getBin().getFileUrlsToBeDeleted())
            .extracting(item -> item.getValue().getBinFileUrl())
            .containsExactly(OLD_COVERSHEET_URL);
    }

    @Test
    void shouldNotDeleteDocument_whenOldCoverSheetIsNull() {
        setUpDocGenerationMocks();
        when(featureToggleService.isDeleteOldBpCoversheetEnabled()).thenReturn(true);
        FinremCaseDetails caseDetails = getCaseDetails(YesOrNo.NO, YesOrNo.NO);
        caseDetails.getData().getBulkPrintCoversheetWrapper().setBulkPrintCoverSheetApp(null);

        generateCoverSheetService.generateAndSetApplicantCoverSheet(caseDetails, AUTH_TOKEN);

        assertEquals(TEST_DOCUMENT_FILENAME,
            caseDetails.getData().getBulkPrintCoversheetWrapper().getBulkPrintCoverSheetApp().getDocumentFilename());
        verify(genericDocumentService, never()).deleteDocument(any(), eq(AUTH_TOKEN));
    }

    @Test
    void shouldNotDeleteDocument_whenOldCoverSheetDeleteDisabled() {
        setUpDocGenerationMocks();
        when(featureToggleService.isDeleteOldBpCoversheetEnabled()).thenReturn(false);
        FinremCaseDetails caseDetails = getCaseDetails(YesOrNo.NO, YesOrNo.NO);
        caseDetails.getData().getBulkPrintCoversheetWrapper().setBulkPrintCoverSheetApp(null);

        generateCoverSheetService.generateAndSetApplicantCoverSheet(caseDetails, AUTH_TOKEN);

        assertEquals(TEST_DOCUMENT_FILENAME,
            caseDetails.getData().getBulkPrintCoversheetWrapper().getBulkPrintCoverSheetApp().getDocumentFilename());
        verify(genericDocumentService, never()).deleteDocument(any(), eq(AUTH_TOKEN));
    }

    @Test
    void shouldStoreApplicantCoverSheetInConfidentialField_whenApplicantAddressHiddenFromRespondent() {
        setUpDocGenerationMocks();
        when(featureToggleService.isDeleteOldBpCoversheetEnabled()).thenReturn(true);
        FinremCaseDetails caseDetails = getCaseDetails(YesOrNo.YES, YesOrNo.NO);
        caseDetails.getData().getBulkPrintCoversheetWrapper().setBulkPrintCoverSheetAppConfidential(
            caseDocument(OLD_COVERSHEET_URL, BULK_PRINT_FILE_NAME)
        );

        generateCoverSheetService.generateAndSetApplicantCoverSheet(caseDetails, AUTH_TOKEN);

        assertNull(caseDetails.getData().getBulkPrintCoversheetWrapper().getBulkPrintCoverSheetApp());
        assertEquals(TEST_DOCUMENT_FILENAME,
            caseDetails.getData().getBulkPrintCoversheetWrapper().getBulkPrintCoverSheetAppConfidential().getDocumentFilename());
        assertThat(caseDetails.getData().getBin().getFileUrlsToBeDeleted())
            .extracting(item -> item.getValue().getBinFileUrl())
            .containsExactly(OLD_COVERSHEET_URL);
    }

    @Test
    void shouldGenerateAndSetRespondentCoverSheet_whenAddressNotHidden() {
        setUpDocGenerationMocks();
        when(featureToggleService.isDeleteOldBpCoversheetEnabled()).thenReturn(true);
        FinremCaseDetails caseDetails = getCaseDetails(YesOrNo.NO, YesOrNo.NO);
        caseDetails.getData().getBulkPrintCoversheetWrapper().setBulkPrintCoverSheetRes(
            caseDocument(OLD_COVERSHEET_URL, BULK_PRINT_FILE_NAME)
        );

        generateCoverSheetService.generateAndSetRespondentCoverSheet(caseDetails, AUTH_TOKEN);

        assertEquals(TEST_DOCUMENT_FILENAME,
            caseDetails.getData().getBulkPrintCoversheetWrapper().getBulkPrintCoverSheetRes().getDocumentFilename());
        assertNull(caseDetails.getData().getBulkPrintCoversheetWrapper().getBulkPrintCoverSheetResConfidential());
        assertThat(caseDetails.getData().getBin().getFileUrlsToBeDeleted())
            .extracting(item -> item.getValue().getBinFileUrl())
            .containsExactly(OLD_COVERSHEET_URL);
    }

    @Test
    void shouldStoreRespondentCoverSheetInConfidentialField_whenRespondentAddressHiddenFromApplicant() {
        setUpDocGenerationMocks();
        when(featureToggleService.isDeleteOldBpCoversheetEnabled()).thenReturn(true);
        FinremCaseDetails caseDetails = getCaseDetails(YesOrNo.NO, YesOrNo.YES);
        caseDetails.getData().getBulkPrintCoversheetWrapper().setBulkPrintCoverSheetResConfidential(
            caseDocument(OLD_COVERSHEET_URL, BULK_PRINT_FILE_NAME)
        );

        generateCoverSheetService.generateAndSetRespondentCoverSheet(caseDetails, AUTH_TOKEN);

        assertNull(caseDetails.getData().getBulkPrintCoversheetWrapper().getBulkPrintCoverSheetRes());
        assertEquals(TEST_DOCUMENT_FILENAME,
            caseDetails.getData().getBulkPrintCoversheetWrapper().getBulkPrintCoverSheetResConfidential().getDocumentFilename());
        assertThat(caseDetails.getData().getBin().getFileUrlsToBeDeleted())
            .extracting(item -> item.getValue().getBinFileUrl())
            .containsExactly(OLD_COVERSHEET_URL);
    }

    @Test
    void shouldGenerateIntervenerCoverSheet() {
        setUpDocGenerationMocks();
        FinremCaseDetails caseDetails = getCaseDetails(YesOrNo.NO, YesOrNo.NO);

        CaseDocument result = generateCoverSheetService.generateIntervenerCoverSheet(
            caseDetails,
            AUTH_TOKEN,
            DocumentHelper.PaperNotificationRecipient.INTERVENER_ONE
        );

        assertGeneratedDocument(result);
        verify(bulkPrintCoverLetterDetailsMapper).getLetterDetailsAsMap(
            caseDetails,
            DocumentHelper.PaperNotificationRecipient.INTERVENER_ONE,
            caseDetails.getData().getRegionWrapper().getDefaultCourtList()
        );
    }

    @ParameterizedTest
    @EnumSource(IntervenerType.class)
    void shouldGenerateAndStoreIntervenerCoverSheet(IntervenerType intervenerType) {
        setUpDocGenerationMocks();
        when(featureToggleService.isDeleteOldBpCoversheetEnabled()).thenReturn(true);
        FinremCaseDetails caseDetails = getCaseDetails(YesOrNo.NO, YesOrNo.NO);
        setIntervenerCoverSheet(caseDetails, intervenerType, caseDocument(OLD_COVERSHEET_URL, BULK_PRINT_FILE_NAME));

        generateCoverSheetService.generateAndStoreIntervenerCoversheet(caseDetails, intervenerType, AUTH_TOKEN);

        assertEquals(TEST_DOCUMENT_FILENAME, Objects.requireNonNull(getIntervenerCoverSheet(caseDetails, intervenerType)).getDocumentFilename());
        assertThat(caseDetails.getData().getBin().getFileUrlsToBeDeleted())
            .extracting(item -> item.getValue().getBinFileUrl())
            .containsExactly(OLD_COVERSHEET_URL);
    }

    @ParameterizedTest
    @EnumSource(IntervenerType.class)
    void givenNullCoversheet_shouldRemoveIntervenerCoverSheet(IntervenerType intervenerType) {
        FinremCaseDetails caseDetails = getCaseDetails(YesOrNo.NO, YesOrNo.NO);
        setIntervenerCoverSheet(caseDetails, intervenerType, null);

        IntervenerChangeDetails changeDetails = new IntervenerChangeDetails();
        changeDetails.setIntervenerType(intervenerType);

        generateCoverSheetService.removeIntervenerCoverSheet(caseDetails, changeDetails, AUTH_TOKEN);

        assertThat(logs.getInfos()).contains(
            "1234567890 - old cover sheet does not exist. Skip deleting it."
        );
        verify(genericDocumentService, never()).deleteDocument(anyString(), eq(AUTH_TOKEN));
    }

    @ParameterizedTest
    @EnumSource(IntervenerType.class)
    void shouldRemoveIntervenerCoverSheet(IntervenerType intervenerType) {
        FinremCaseDetails caseDetails = getCaseDetails(YesOrNo.NO, YesOrNo.NO);
        setIntervenerCoverSheet(caseDetails, intervenerType, caseDocument(OLD_COVERSHEET_URL, BULK_PRINT_FILE_NAME));

        IntervenerChangeDetails changeDetails = new IntervenerChangeDetails();
        changeDetails.setIntervenerType(intervenerType);

        generateCoverSheetService.removeIntervenerCoverSheet(caseDetails, changeDetails, AUTH_TOKEN);

        assertNull(getIntervenerCoverSheet(caseDetails, intervenerType));

        assertThat(caseDetails.getData().getBin().getFileUrlsToBeDeleted())
            .extracting(item -> item.getValue().getBinFileUrl())
            .containsExactly(OLD_COVERSHEET_URL);
    }

    @Test
    void shouldThrowException_whenInvalidIntervenerTypeProvided() {
        FinremCaseDetails caseDetails = getCaseDetails(YesOrNo.NO, YesOrNo.NO);

        IntervenerChangeDetails changeDetails = new IntervenerChangeDetails();
        changeDetails.setIntervenerType(null); // Invalid intervener type

        assertThrows(IllegalArgumentException.class, () -> {
            generateCoverSheetService.removeIntervenerCoverSheet(caseDetails, changeDetails, AUTH_TOKEN);
        });
    }

    private void setIntervenerCoverSheet(FinremCaseDetails caseDetails, IntervenerType intervenerType, CaseDocument document) {
        BulkPrintCoversheetWrapper wrapper = caseDetails.getData().getBulkPrintCoversheetWrapper();
        switch (intervenerType) {
            case INTERVENER_ONE -> wrapper.setBulkPrintCoverSheetIntv1(document);
            case INTERVENER_TWO -> wrapper.setBulkPrintCoverSheetIntv2(document);
            case INTERVENER_THREE -> wrapper.setBulkPrintCoverSheetIntv3(document);
            case INTERVENER_FOUR -> wrapper.setBulkPrintCoverSheetIntv4(document);
            default -> { /* do nothing */ }
        }
    }

    private CaseDocument getIntervenerCoverSheet(FinremCaseDetails caseDetails, IntervenerType intervenerType) {
        BulkPrintCoversheetWrapper wrapper = caseDetails.getData().getBulkPrintCoversheetWrapper();
        switch (intervenerType) {
            case INTERVENER_ONE -> {
                return wrapper.getBulkPrintCoverSheetIntv1();
            }
            case INTERVENER_TWO -> {
                return wrapper.getBulkPrintCoverSheetIntv2();
            }
            case INTERVENER_THREE -> {
                return wrapper.getBulkPrintCoverSheetIntv3();
            }
            case INTERVENER_FOUR -> {
                return wrapper.getBulkPrintCoverSheetIntv4();
            }
            default -> {
                return null;
            }
        }
    }

    private void setUpDocGenerationMocks() {
        when(documentConfiguration.getBulkPrintTemplate()).thenReturn(BULK_PRINT_TEMPLATE);
        when(documentConfiguration.getBulkPrintFileName()).thenReturn(BULK_PRINT_FILE_NAME);

        when(bulkPrintCoverLetterDetailsMapper.getLetterDetailsAsMap(any(), any(), any()))
            .thenReturn(Map.of("caseDetails", Map.of("id", CASE_ID_IN_LONG)));

        when(genericDocumentService.generateDocumentFromPlaceholdersMap(
            eq(AUTH_TOKEN),
            any(),
            eq(BULK_PRINT_TEMPLATE),
            eq(BULK_PRINT_FILE_NAME),
            eq(caseType)
        )).thenReturn(CaseDocument.builder()
            .documentBinaryUrl(TEST_DOCUMENT_BINARY_URL)
            .documentUrl(TEST_DOCUMENT_URL)
            .documentFilename(TEST_DOCUMENT_FILENAME)
            .build());
    }

    private void assertGeneratedDocument(CaseDocument result) {
        assertEquals(TEST_DOCUMENT_BINARY_URL, result.getDocumentBinaryUrl());
        assertEquals(TEST_DOCUMENT_URL, result.getDocumentUrl());
        assertEquals(TEST_DOCUMENT_FILENAME, result.getDocumentFilename());
    }

    private FinremCaseDetails getCaseDetails(YesOrNo applicantAddressHiddenFromRespondent,
                                             YesOrNo respondentAddressHiddenFromApplicant) {
        FinremCaseData caseData = FinremCaseData.builder()
            .ccdCaseType(caseType)
            .contactDetailsWrapper(ContactDetailsWrapper.builder()
                .applicantAddressHiddenFromRespondent(applicantAddressHiddenFromRespondent)
                .respondentAddressHiddenFromApplicant(respondentAddressHiddenFromApplicant)
                .build())
            .bulkPrintCoversheetWrapper(BulkPrintCoversheetWrapper.builder().build())
            .regionWrapper(RegionWrapper.builder().build())
            .build();

        return FinremCaseDetails.builder()
            .id(CASE_ID_IN_LONG)
            .caseType(caseType)
            .data(caseData)
            .build();
    }
}
