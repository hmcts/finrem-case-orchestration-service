package uk.gov.hmcts.reform.finrem.caseorchestration.service;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import uk.gov.hmcts.reform.ccd.client.model.CaseDetails;
import uk.gov.hmcts.reform.finrem.caseorchestration.config.DocumentConfiguration;
import uk.gov.hmcts.reform.finrem.caseorchestration.helper.ConsentedApplicationHelper;
import uk.gov.hmcts.reform.finrem.caseorchestration.helper.DocumentHelper;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.CaseDocument;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.FinremCaseData;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.FinremCaseDetails;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.document.BulkPrintDocument;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.when;
import static uk.gov.hmcts.reform.finrem.caseorchestration.TestConstants.AUTH_TOKEN;
import static uk.gov.hmcts.reform.finrem.caseorchestration.TestSetUpUtils.defaultConsentedFinremCaseDetails;
import static uk.gov.hmcts.reform.finrem.caseorchestration.helper.DocumentHelper.PaperNotificationRecipient.APPLICANT;
import static uk.gov.hmcts.reform.finrem.caseorchestration.helper.DocumentHelper.PaperNotificationRecipient.RESPONDENT;

@ExtendWith(MockitoExtension.class)
class ConsentOrderApprovedDocumentServiceV2Test {

    private static final String CONSENT_ORDER_APPROVED_COVER_LETTER_FILENAME = "consentOrderApprovedCoverLetter.pdf";
    private static final String APPROVED_CONSENT_ORDER_TEMPLATE = "FL-FRM-LET-ENG-00095.docx";

    @Mock
    private CaseDataService caseDataService;
    @Mock
    private DocumentHelper documentHelper;
    @Mock
    private CaseDetails caseDetails;
    @Mock
    private ConsentedApplicationHelper consentedApplicationHelper;
    @Mock
    private DocumentConfiguration documentConfiguration;
    @Mock
    private GenericDocumentService genericDocumentService;
    @Mock
    private CaseDocument caseDocument;
    @Mock
    private BulkPrintDocument bulkPrintDocument;

    @InjectMocks
    private ConsentOrderApprovedDocumentService consentOrderApprovedDocumentService;

    private FinremCaseDetails finremCaseDetails;

    @BeforeEach
    void setUp() {
        finremCaseDetails = defaultConsentedFinremCaseDetails();
    }

    @Test
    void shouldReturnEmptyListWhenNotPaperApplication() {
        isPaperApplication(false);
        List<BulkPrintDocument> result = consentOrderApprovedDocumentService
            .addApprovedConsentOrderCoverLetter(finremCaseDetails, AUTH_TOKEN, APPLICANT);

        assertThat(result).isEmpty();
    }

    @Test
    void shouldAddApprovedConsentOrderCoverLetterForApplicant() {
        isPaperApplication(true);
        mockPrepareLetterTemplateData(APPLICANT);
        isVariationOrder(false);
        mockApprovedConsentOrder();

        List<BulkPrintDocument> result = consentOrderApprovedDocumentService
            .addApprovedConsentOrderCoverLetter(finremCaseDetails, AUTH_TOKEN, APPLICANT);

        assertThat(result.getFirst()).isEqualTo(bulkPrintDocument);
    }

    @Test
    void shouldAddApprovedConsentOrderCoverLetterForRespondent() {
        isPaperApplication(true);
        mockPrepareLetterTemplateData(RESPONDENT);
        isVariationOrder(false);
        mockApprovedConsentOrder();

        List<BulkPrintDocument> result = consentOrderApprovedDocumentService
            .addApprovedConsentOrderCoverLetter(finremCaseDetails, AUTH_TOKEN,DocumentHelper.PaperNotificationRecipient.RESPONDENT);

        assertThat(result.getFirst()).isEqualTo(bulkPrintDocument);
    }

    @Test
    void shouldAddApprovedConsentOrderCoverLetterForVariationOrder() {
        isPaperApplication(true);
        mockPrepareLetterTemplateData(APPLICANT);
        isVariationOrder(true);
        when(documentConfiguration.getApprovedVariationOrderNotificationFileName()).thenReturn("VariationOrderApprovedCoverLetter.pdf");
        when(documentConfiguration.getApprovedConsentOrderNotificationTemplate()).thenReturn(APPROVED_CONSENT_ORDER_TEMPLATE);
        when(genericDocumentService.generateDocument(any(String.class), any(CaseDetails.class), any(String.class), any(String.class)))
            .thenReturn(caseDocument);
        when(documentHelper.mapToBulkPrintDocument(any(CaseDocument.class))).thenReturn(bulkPrintDocument);

        List<BulkPrintDocument> result = consentOrderApprovedDocumentService
            .addApprovedConsentOrderCoverLetter(finremCaseDetails, AUTH_TOKEN, APPLICANT);

        assertThat(result.getFirst()).isEqualTo(bulkPrintDocument);
    }

    private void mockApprovedConsentOrder() {
        when(documentConfiguration.getApprovedConsentOrderNotificationFileName()).thenReturn(CONSENT_ORDER_APPROVED_COVER_LETTER_FILENAME);
        when(documentConfiguration.getApprovedConsentOrderNotificationTemplate()).thenReturn(APPROVED_CONSENT_ORDER_TEMPLATE);
        when(genericDocumentService.generateDocument(any(String.class), any(CaseDetails.class), any(String.class), any(String.class)))
            .thenReturn(caseDocument);
        when(documentHelper.mapToBulkPrintDocument(any(CaseDocument.class))).thenReturn(bulkPrintDocument);
    }

    private void isPaperApplication(Boolean value) {
        when(caseDataService.isPaperApplication(any(FinremCaseData.class))).thenReturn(value);
    }

    private void isVariationOrder(Boolean value) {
        when(consentedApplicationHelper.isVariationOrder(any(FinremCaseData.class))).thenReturn(value);
    }

    private void mockPrepareLetterTemplateData(DocumentHelper.PaperNotificationRecipient recipient) {
        when(documentHelper.prepareLetterTemplateData(any(FinremCaseDetails.class), eq(recipient)))
            .thenReturn(caseDetails);
    }
}
