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
        when(genericDocumentService.generateDocument(any(String.class), any(CaseDetails.class), any(String.class), any(String.class)))
            .thenReturn(caseDocument);
        when(documentHelper.mapToBulkPrintDocument(any(CaseDocument.class))).thenReturn(bulkPrintDocument);
        when(documentConfiguration.getApprovedConsentOrderNotificationTemplate()).thenReturn(APPROVED_CONSENT_ORDER_TEMPLATE);
    }

    @Test
    void shouldReturnEmptyListWhenNotPaperApplication() {
        when(caseDataService.isPaperApplication(any(FinremCaseData.class))).thenReturn(false);

        List<BulkPrintDocument> result = consentOrderApprovedDocumentService
            .addApprovedConsentOrderCoverLetter(finremCaseDetails, AUTH_TOKEN, APPLICANT);

        assertThat(result).isEmpty();
    }

    @Test
    void shouldAddApprovedConsentOrderCoverLetterForApplicant() {
        when(caseDataService.isPaperApplication(any(FinremCaseData.class))).thenReturn(true);
        when(documentHelper.prepareLetterTemplateData(any(FinremCaseDetails.class), eq(APPLICANT))).thenReturn(caseDetails);
        when(consentedApplicationHelper.isVariationOrder(any(FinremCaseData.class))).thenReturn(false);
        when(documentConfiguration.getApprovedConsentOrderNotificationFileName()).thenReturn(CONSENT_ORDER_APPROVED_COVER_LETTER_FILENAME);

        List<BulkPrintDocument> result = consentOrderApprovedDocumentService
            .addApprovedConsentOrderCoverLetter(finremCaseDetails, AUTH_TOKEN, APPLICANT);

        assertThat(result).hasSize(1);
    }

    @Test
    void shouldAddApprovedConsentOrderCoverLetterForRespondent() {
        when(caseDataService.isPaperApplication(any(FinremCaseData.class))).thenReturn(true);
        when(documentHelper.prepareLetterTemplateData(any(FinremCaseDetails.class), eq(RESPONDENT))).thenReturn(caseDetails);
        when(consentedApplicationHelper.isVariationOrder(any(FinremCaseData.class))).thenReturn(false);
        when(documentConfiguration.getApprovedConsentOrderNotificationFileName()).thenReturn(CONSENT_ORDER_APPROVED_COVER_LETTER_FILENAME);

        List<BulkPrintDocument> result = consentOrderApprovedDocumentService
            .addApprovedConsentOrderCoverLetter(finremCaseDetails, AUTH_TOKEN,DocumentHelper.PaperNotificationRecipient.RESPONDENT);

        assertThat(result).hasSize(1);
    }

    @Test
    void shouldAddApprovedConsentOrderCoverLetterForVariationOrder() {
        when(caseDataService.isPaperApplication(any(FinremCaseData.class))).thenReturn(true);
        when(documentHelper.prepareLetterTemplateData(any(FinremCaseDetails.class), any(DocumentHelper.PaperNotificationRecipient.class))).thenReturn(caseDetails);
        when(consentedApplicationHelper.isVariationOrder(any(FinremCaseData.class))).thenReturn(true);
        when(documentConfiguration.getApprovedVariationOrderNotificationFileName()).thenReturn("VariationOrderApprovedCoverLetter.pdf");

        List<BulkPrintDocument> result = consentOrderApprovedDocumentService
            .addApprovedConsentOrderCoverLetter(finremCaseDetails, AUTH_TOKEN, APPLICANT);

        assertThat(result).hasSize(1);
    }
}
