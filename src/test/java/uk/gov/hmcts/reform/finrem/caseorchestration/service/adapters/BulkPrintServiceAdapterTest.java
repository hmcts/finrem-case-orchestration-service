package uk.gov.hmcts.reform.finrem.caseorchestration.service.adapters;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import uk.gov.hmcts.reform.ccd.client.model.CaseDetails;
import uk.gov.hmcts.reform.finrem.caseorchestration.FinremCaseDetailsBuilderFactory;
import uk.gov.hmcts.reform.finrem.caseorchestration.mapper.FinremCaseDetailsMapper;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.CaseDocument;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.FinremCaseDetails;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.document.BulkPrintDocument;
import uk.gov.hmcts.reform.finrem.caseorchestration.service.BulkPrintService;

import java.util.List;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static uk.gov.hmcts.reform.finrem.caseorchestration.TestConstants.AUTH_TOKEN;
import static uk.gov.hmcts.reform.finrem.caseorchestration.TestConstants.CASE_ID_IN_LONG;

@ExtendWith(MockitoExtension.class)
class BulkPrintServiceAdapterTest {

    private static final String APPLICANT_RECIPIENT = "APPLICANT";
    private static final String INPUT_RECIPIENT = "APPLICANT_CONFIDENTIAL_SOLICITOR";
    private static final String EXPECTED_RECIPIENT = "ApplicantConfidentialSolicitor";

    @Mock
    private BulkPrintService bulkPrintService;

    @Mock
    private FinremCaseDetailsMapper finremCaseDetailsMapper;

    @InjectMocks
    private BulkPrintServiceAdapter bulkPrintServiceAdapter;

    private UUID expectedUuid;
    private BulkPrintDocument document;

    @BeforeEach
    void setUp() {
        expectedUuid = UUID.randomUUID();
        document = BulkPrintDocument.builder()
            .fileName("doc1.pdf")
            .binaryFileUrl("http://doc1")
            .build();
    }

    @Test
    void shouldPrintApplicantDocumentsSuccessfully() {
        CaseDetails caseDetails = caseDetails();
        List<BulkPrintDocument> documents = documents();
        FinremCaseDetails finremCaseDetails = finremCaseDetails(caseDetails);

        when(finremCaseDetailsMapper.mapToFinremCaseDetails(caseDetails)).thenReturn(finremCaseDetails);
        when(bulkPrintService.printApplicantDocuments(finremCaseDetails, AUTH_TOKEN, documents)).thenReturn(expectedUuid);

        UUID actualUuid = bulkPrintServiceAdapter.printApplicantDocuments(caseDetails, AUTH_TOKEN, documents);

        assertThat(actualUuid).isEqualTo(expectedUuid);
        verify(finremCaseDetailsMapper).mapToFinremCaseDetails(caseDetails);
        verify(bulkPrintService).printApplicantDocuments(finremCaseDetails, AUTH_TOKEN, documents);
    }

    @Test
    void shouldHandlePrintRespondentDocumentsSuccessfully() {
        CaseDetails caseDetails = caseDetails();
        List<BulkPrintDocument> documents = documents();
        FinremCaseDetails finremCaseDetails = finremCaseDetails(caseDetails);

        when(finremCaseDetailsMapper.mapToFinremCaseDetails(caseDetails)).thenReturn(finremCaseDetails);
        when(bulkPrintService.printRespondentDocuments(finremCaseDetails, AUTH_TOKEN, documents)).thenReturn(expectedUuid);

        UUID actualUuid = bulkPrintServiceAdapter.printRespondentDocuments(caseDetails, AUTH_TOKEN, documents);

        assertThat(actualUuid).isEqualTo(expectedUuid);
        verify(finremCaseDetailsMapper).mapToFinremCaseDetails(caseDetails);
        verify(bulkPrintService).printRespondentDocuments(finremCaseDetails, AUTH_TOKEN, documents);
    }

    @Test
    void shouldSendDocumentForPrintSuccessfully() {
        CaseDocument caseDocument = CaseDocument.builder()
            .documentBinaryUrl("http://doc2")
            .documentFilename("doc2.pdf")
            .build();
        CaseDetails caseDetails = caseDetails();
        FinremCaseDetails finremCaseDetails = finremCaseDetails(caseDetails);
        String recipient = APPLICANT_RECIPIENT;

        when(finremCaseDetailsMapper.mapToFinremCaseDetails(caseDetails)).thenReturn(finremCaseDetails);
        when(bulkPrintService.sendDocumentForPrint(caseDocument, finremCaseDetails, recipient, AUTH_TOKEN)).thenReturn(expectedUuid);

        UUID actualUuid = bulkPrintServiceAdapter.sendDocumentForPrint(caseDocument, caseDetails, recipient, AUTH_TOKEN);

        assertThat(actualUuid).isEqualTo(expectedUuid);
        verify(finremCaseDetailsMapper).mapToFinremCaseDetails(caseDetails);
        verify(bulkPrintService).sendDocumentForPrint(caseDocument, finremCaseDetails, recipient, AUTH_TOKEN);
    }

    @Test
    void shouldFormatRecipientStringCorrectly() {
        String actual = bulkPrintServiceAdapter.getRecipient(INPUT_RECIPIENT);

        assertThat(actual).isEqualTo(EXPECTED_RECIPIENT);
    }

    private CaseDetails caseDetails() {
        return CaseDetails.builder().id(CASE_ID_IN_LONG).build();
    }

    private FinremCaseDetails finremCaseDetails(CaseDetails caseDetails) {
        return FinremCaseDetailsBuilderFactory.from(caseDetails.getId()).build();
    }

    private List<BulkPrintDocument> documents() {
        return List.of(document);
    }
}
