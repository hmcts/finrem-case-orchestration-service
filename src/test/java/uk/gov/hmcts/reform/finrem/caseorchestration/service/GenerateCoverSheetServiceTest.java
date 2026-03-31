package uk.gov.hmcts.reform.finrem.caseorchestration.service;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Captor;
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

import java.util.Map;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static uk.gov.hmcts.reform.finrem.caseorchestration.TestConstants.AUTH_TOKEN;
import static uk.gov.hmcts.reform.finrem.caseorchestration.TestConstants.CASE_ID_IN_LONG;
import static uk.gov.hmcts.reform.finrem.caseorchestration.TestConstants.TEST_DOCUMENT_BINARY_URL;
import static uk.gov.hmcts.reform.finrem.caseorchestration.TestConstants.TEST_DOCUMENT_FILENAME;
import static uk.gov.hmcts.reform.finrem.caseorchestration.TestConstants.TEST_DOCUMENT_URL;
import static uk.gov.hmcts.reform.finrem.caseorchestration.helper.DocumentHelper.PaperNotificationRecipient.APPLICANT;

@ExtendWith(MockitoExtension.class)
class GenerateCoverSheetServiceTest {

    private static final String BULK_PRINT_TEMPLATE = "bulk-print-template";
    private static final String BULK_PRINT_FILE_NAME = "bulk-print.pdf";
    public static final String OLD_COVERESHEET_URL = "http://dm-store:8080/documents/old-app-cover-sheet";

    @Mock
    private GenericDocumentService genericDocumentService;
    @Mock
    private DocumentConfiguration documentConfiguration;
    @Mock
    private BulkPrintCoverLetterDetailsMapper bulkPrintCoverLetterDetailsMapper;
    @Captor
    private ArgumentCaptor<Map<String, Object>> placeholdersCaptor;
    private GenerateCoverSheetService generateCoverSheetService;

    @BeforeEach
    void setUp() {
        generateCoverSheetService = new GenerateCoverSheetService(
            genericDocumentService,
            documentConfiguration,
            bulkPrintCoverLetterDetailsMapper
        );

        when(documentConfiguration.getBulkPrintTemplate()).thenReturn(BULK_PRINT_TEMPLATE);
        when(documentConfiguration.getBulkPrintFileName()).thenReturn(BULK_PRINT_FILE_NAME);

        when(bulkPrintCoverLetterDetailsMapper.getLetterDetailsAsMap(any(), any(), any()))
            .thenReturn(Map.of("caseDetails", Map.of("id", CASE_ID_IN_LONG)));

        when(genericDocumentService.generateDocumentFromPlaceholdersMap(
            eq(AUTH_TOKEN),
            any(),
            eq(BULK_PRINT_TEMPLATE),
            eq(BULK_PRINT_FILE_NAME),
            eq(CaseType.CONSENTED)
        )).thenReturn(CaseDocument.builder()
            .documentBinaryUrl(TEST_DOCUMENT_BINARY_URL)
            .documentUrl(TEST_DOCUMENT_URL)
            .documentFilename(TEST_DOCUMENT_FILENAME)
            .build());
    }

    @Test
    void shouldGenerateApplicantCoverSheet() {
        FinremCaseDetails caseDetails = caseDetails(YesOrNo.NO, YesOrNo.NO);

        CaseDocument result = generateCoverSheetService.generateApplicantCoverSheet(caseDetails, AUTH_TOKEN);

        assertGeneratedDocument(result);
        verify(genericDocumentService).generateDocumentFromPlaceholdersMap(
            eq(AUTH_TOKEN),
            any(),
            eq(BULK_PRINT_TEMPLATE),
            eq(BULK_PRINT_FILE_NAME),
            eq(CaseType.CONSENTED)
        );
        verify(bulkPrintCoverLetterDetailsMapper).getLetterDetailsAsMap(caseDetails, APPLICANT,
            caseDetails.getData().getRegionWrapper().getDefaultCourtList());
    }

    @Test
    void shouldGenerateAndSetApplicantCoverSheet_whenAddressNotHidden() {
        FinremCaseDetails caseDetails = caseDetails(YesOrNo.NO, YesOrNo.NO);
        caseDetails.getData().getBulkPrintCoversheetWrapper().setBulkPrintCoverSheetApp(
            CaseDocument.builder().documentUrl(OLD_COVERESHEET_URL).build()
        );

        generateCoverSheetService.generateAndSetApplicantCoverSheet(caseDetails, AUTH_TOKEN);

        assertEquals(TEST_DOCUMENT_FILENAME,
            caseDetails.getData().getBulkPrintCoversheetWrapper().getBulkPrintCoverSheetApp().getDocumentFilename());
        assertNull(caseDetails.getData().getBulkPrintCoversheetWrapper().getBulkPrintCoverSheetAppConfidential());
        verify(genericDocumentService).deleteDocument(OLD_COVERESHEET_URL, AUTH_TOKEN);
    }

    @Test
    void shouldNotDeleteDocument_whenOldCoverSheetIsNull() {
        FinremCaseDetails caseDetails = caseDetails(YesOrNo.NO, YesOrNo.NO);
        caseDetails.getData().getBulkPrintCoversheetWrapper().setBulkPrintCoverSheetApp(null);

        generateCoverSheetService.generateAndSetApplicantCoverSheet(caseDetails, AUTH_TOKEN);

        assertEquals(TEST_DOCUMENT_FILENAME,
                caseDetails.getData().getBulkPrintCoversheetWrapper().getBulkPrintCoverSheetApp().getDocumentFilename());
        verify(genericDocumentService, org.mockito.Mockito.never()).deleteDocument(any(), eq(AUTH_TOKEN));
    }

    @Test
    void shouldStoreApplicantCoverSheetInConfidentialField_whenApplicantAddressHiddenFromRespondent() {
        FinremCaseDetails caseDetails = caseDetails(YesOrNo.YES, YesOrNo.NO);
        caseDetails.getData().getBulkPrintCoversheetWrapper().setBulkPrintCoverSheetAppConfidential(
            CaseDocument.builder().documentUrl(OLD_COVERESHEET_URL).build()
        );

        generateCoverSheetService.generateAndSetApplicantCoverSheet(caseDetails, AUTH_TOKEN);

        assertNull(caseDetails.getData().getBulkPrintCoversheetWrapper().getBulkPrintCoverSheetApp());
        assertEquals(TEST_DOCUMENT_FILENAME,
            caseDetails.getData().getBulkPrintCoversheetWrapper().getBulkPrintCoverSheetAppConfidential().getDocumentFilename());
        verify(genericDocumentService).deleteDocument(OLD_COVERESHEET_URL, AUTH_TOKEN);
    }

    @Test
    void shouldGenerateAndSetRespondentCoverSheet_whenAddressNotHidden() {
        FinremCaseDetails caseDetails = caseDetails(YesOrNo.NO, YesOrNo.NO);
        caseDetails.getData().getBulkPrintCoversheetWrapper().setBulkPrintCoverSheetRes(
            CaseDocument.builder().documentUrl(OLD_COVERESHEET_URL).build()
        );

        generateCoverSheetService.generateAndSetRespondentCoverSheet(caseDetails, AUTH_TOKEN);

        assertEquals(TEST_DOCUMENT_FILENAME,
            caseDetails.getData().getBulkPrintCoversheetWrapper().getBulkPrintCoverSheetRes().getDocumentFilename());
        assertNull(caseDetails.getData().getBulkPrintCoversheetWrapper().getBulkPrintCoverSheetResConfidential());
        verify(genericDocumentService).deleteDocument(OLD_COVERESHEET_URL, AUTH_TOKEN);
    }

    @Test
    void shouldStoreRespondentCoverSheetInConfidentialField_whenRespondentAddressHiddenFromApplicant() {
        FinremCaseDetails caseDetails = caseDetails(YesOrNo.NO, YesOrNo.YES);
        caseDetails.getData().getBulkPrintCoversheetWrapper().setBulkPrintCoverSheetResConfidential(
            CaseDocument.builder().documentUrl(OLD_COVERESHEET_URL).build()
        );

        generateCoverSheetService.generateAndSetRespondentCoverSheet(caseDetails, AUTH_TOKEN);

        assertNull(caseDetails.getData().getBulkPrintCoversheetWrapper().getBulkPrintCoverSheetRes());
        assertEquals(TEST_DOCUMENT_FILENAME,
            caseDetails.getData().getBulkPrintCoversheetWrapper().getBulkPrintCoverSheetResConfidential().getDocumentFilename());
        verify(genericDocumentService).deleteDocument(OLD_COVERESHEET_URL, AUTH_TOKEN);
    }

    @Test
    void shouldGenerateIntervenerCoverSheet() {
        FinremCaseDetails caseDetails = caseDetails(YesOrNo.NO, YesOrNo.NO);

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

    private void assertGeneratedDocument(CaseDocument result) {
        assertEquals(TEST_DOCUMENT_BINARY_URL, result.getDocumentBinaryUrl());
        assertEquals(TEST_DOCUMENT_URL, result.getDocumentUrl());
        assertEquals(TEST_DOCUMENT_FILENAME, result.getDocumentFilename());
    }

    private FinremCaseDetails caseDetails(YesOrNo applicantAddressHiddenFromRespondent,
                                          YesOrNo respondentAddressHiddenFromApplicant) {
        FinremCaseData caseData = FinremCaseData.builder()
            .ccdCaseType(CaseType.CONSENTED)
            .contactDetailsWrapper(ContactDetailsWrapper.builder()
                .applicantAddressHiddenFromRespondent(applicantAddressHiddenFromRespondent)
                .respondentAddressHiddenFromApplicant(respondentAddressHiddenFromApplicant)
                .build())
            .bulkPrintCoversheetWrapper(BulkPrintCoversheetWrapper.builder().build())
            .regionWrapper(RegionWrapper.builder().build())
            .build();

        return FinremCaseDetails.builder()
            .id(CASE_ID_IN_LONG)
            .caseType(CaseType.CONSENTED)
            .data(caseData)
            .build();
    }
}
