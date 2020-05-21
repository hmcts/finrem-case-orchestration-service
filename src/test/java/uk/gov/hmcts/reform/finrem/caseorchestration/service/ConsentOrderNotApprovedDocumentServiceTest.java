package uk.gov.hmcts.reform.finrem.caseorchestration.service;

import com.google.common.collect.ImmutableMap;
import org.junit.Before;
import org.junit.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;
import uk.gov.hmcts.reform.ccd.client.model.CaseDetails;
import uk.gov.hmcts.reform.finrem.caseorchestration.BaseServiceTest;
import uk.gov.hmcts.reform.finrem.caseorchestration.client.DocumentClient;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.document.BulkPrintDocument;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.document.Document;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.document.DocumentGenerationRequest;

import java.util.Collections;
import java.util.List;
import java.util.Map;

import static org.hamcrest.Matchers.hasSize;
import static org.hamcrest.Matchers.is;
import static org.junit.Assert.assertThat;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.argThat;
import static org.mockito.Mockito.when;
import static uk.gov.hmcts.reform.finrem.caseorchestration.OrchestrationConstants.PAPER_APPLICATION;
import static uk.gov.hmcts.reform.finrem.caseorchestration.OrchestrationConstants.YES_VALUE;
import static uk.gov.hmcts.reform.finrem.caseorchestration.TestConstants.AUTH_TOKEN;
import static uk.gov.hmcts.reform.finrem.caseorchestration.TestSetUpUtils.defaultCaseDetails;
import static uk.gov.hmcts.reform.finrem.caseorchestration.TestSetUpUtils.document;
import static uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.CCDConfigConstant.UPLOAD_ORDER;

@ActiveProfiles("test-mock-document-client")
@SpringBootTest(properties = {"feature.toggle.consent_order_not_approved_applicant_document_generation=true"})
public class ConsentOrderNotApprovedDocumentServiceTest extends BaseServiceTest {

    private static final String COVER_LETTER_URL = "cover_letter_url";
    private static final String GENERAL_ORDER_URL = "general_letter_url";
    private static final String REPLY_COVERSHEET_URL = "reply_coversheet_url";

    private static final String CONSENT_ORDER_NOT_APPROVED_COVER_LETTER_TEMPLATE = "FL-FRM-LET-ENG-00319.docx";
    private static final String CONSENT_ORDER_NOT_APPROVED_COVER_LETTER_FILENAME = "consentOrderNotApprovedCoverLetter.pdf";

    private static final String CONSENT_ORDER_NOT_APPROVED_REPLY_COVERSHEET_TEMPLATE = "FL-FRM-LET-ENG-00320.docx";
    private static final String CONSENT_ORDER_NOT_APPROVED_REPLY_COVERSHEET_FILENAME = "consentOrderNotApprovedReplyCoversheet.pdf";

    @Autowired
    private DocumentClient documentClient;

    @Autowired
    private ConsentOrderNotApprovedDocumentService consentOrderNotApprovedDocumentService;

    private CaseDetails caseDetails;
    private List<BulkPrintDocument> generatedDocuments;

    @Before
    public void setup() {
        caseDetails = defaultCaseDetails();

        Map<String, Object> caseData = caseDetails.getData();
        caseData.put(PAPER_APPLICATION, YES_VALUE);
        caseData.put(UPLOAD_ORDER, Collections.singletonList(
            ImmutableMap.of("value", ImmutableMap.of(
                "DocumentLink", ImmutableMap.of(
                    "document_binary_url", GENERAL_ORDER_URL)))));

        Document coverLetter = document();
        coverLetter.setBinaryUrl(COVER_LETTER_URL);
        when(documentClient.generatePdf(
            matchDocumentGenerationRequestTemplateAndFilename(
                CONSENT_ORDER_NOT_APPROVED_COVER_LETTER_TEMPLATE,
                CONSENT_ORDER_NOT_APPROVED_COVER_LETTER_FILENAME),
            anyString()))
            .thenReturn(coverLetter);

        Document replyCoversheet = document();
        replyCoversheet.setBinaryUrl(REPLY_COVERSHEET_URL);
        when(documentClient.generatePdf(
            matchDocumentGenerationRequestTemplateAndFilename(
                CONSENT_ORDER_NOT_APPROVED_REPLY_COVERSHEET_TEMPLATE,
                CONSENT_ORDER_NOT_APPROVED_REPLY_COVERSHEET_FILENAME),
            anyString()))
            .thenReturn(replyCoversheet);

        generatedDocuments = consentOrderNotApprovedDocumentService.generateApplicantDocuments(caseDetails, AUTH_TOKEN);
    }

    @Test
    public void whenApplicantDocumentsGenerated_thenItHasThreeDocuments() {
        assertThat(generatedDocuments, hasSize(3));
    }

    @Test
    public void whenApplicantDocumentsGenerated_thenCoverLetterIsFirstDocument() {
        BulkPrintDocument coverLetter = generatedDocuments.get(0);
        assertThat(coverLetter.getBinaryFileUrl(), is(COVER_LETTER_URL));
    }

    @Test
    public void whenApplicantDocumentsGenerated_thenGeneralOrderIsSecondDocument() {
        assertThat(generatedDocuments.get(1).getBinaryFileUrl(), is(GENERAL_ORDER_URL));
    }

    @Test
    public void whenApplicantDocumentsGenerated_thenCoverLetterIsThirdDocument() {
        assertThat(generatedDocuments.get(2).getBinaryFileUrl(), is(REPLY_COVERSHEET_URL));
    }

    private DocumentGenerationRequest matchDocumentGenerationRequestTemplateAndFilename(String template, String filename) {
        return argThat(
            documentGenerationRequest -> documentGenerationRequest != null
                && template.equals(documentGenerationRequest.getTemplate())
                && filename.equals(documentGenerationRequest.getFileName()));
    }
}
