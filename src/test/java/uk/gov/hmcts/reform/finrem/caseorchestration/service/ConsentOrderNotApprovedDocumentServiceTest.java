package uk.gov.hmcts.reform.finrem.caseorchestration.service;

import com.google.common.collect.ImmutableMap;
import org.junit.Before;
import org.junit.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.test.context.ActiveProfiles;
import uk.gov.hmcts.reform.ccd.client.model.CaseDetails;
import uk.gov.hmcts.reform.finrem.caseorchestration.BaseServiceTest;
import uk.gov.hmcts.reform.finrem.caseorchestration.TestSetUpUtils;
import uk.gov.hmcts.reform.finrem.caseorchestration.client.DocumentClient;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.GeneralOrderConsented;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.GeneralOrderConsentedData;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.document.BulkPrintDocument;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.document.Document;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.UUID;

import static org.hamcrest.Matchers.hasSize;
import static org.hamcrest.Matchers.is;
import static org.hamcrest.Matchers.notNullValue;
import static org.junit.Assert.assertThat;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.when;
import static uk.gov.hmcts.reform.finrem.caseorchestration.OrchestrationConstants.PAPER_APPLICATION;
import static uk.gov.hmcts.reform.finrem.caseorchestration.OrchestrationConstants.YES_VALUE;
import static uk.gov.hmcts.reform.finrem.caseorchestration.TestConstants.AUTH_TOKEN;
import static uk.gov.hmcts.reform.finrem.caseorchestration.TestSetUpUtils.caseDocument;
import static uk.gov.hmcts.reform.finrem.caseorchestration.TestSetUpUtils.defaultConsentedCaseDetails;
import static uk.gov.hmcts.reform.finrem.caseorchestration.TestSetUpUtils.defaultContestedCaseDetails;
import static uk.gov.hmcts.reform.finrem.caseorchestration.TestSetUpUtils.document;
import static uk.gov.hmcts.reform.finrem.caseorchestration.TestSetUpUtils.matchDocumentGenerationRequestTemplateAndFilename;
import static uk.gov.hmcts.reform.finrem.caseorchestration.helper.DocumentHelper.DOCUMENT_BINARY_URL;
import static uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.CCDConfigConstant.BULK_PRINT_COVER_SHEET_APP;
import static uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.CCDConfigConstant.CONTESTED_CONSENT_ORDER_NOT_APPROVED_COLLECTION;
import static uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.CCDConfigConstant.GENERAL_ORDER_COLLECTION_CONSENTED;
import static uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.CCDConfigConstant.GENERAL_ORDER_LATEST_DOCUMENT;
import static uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.CCDConfigConstant.UPLOAD_ORDER;

@ActiveProfiles("test-mock-document-client")
public class ConsentOrderNotApprovedDocumentServiceTest extends BaseServiceTest {

    private static final String COVER_LETTER_URL = "cover_letter_url";
    private static final String GENERAL_ORDER_URL = "general_letter_url";
    private static final String REPLY_COVERSHEET_URL = "reply_coversheet_url";
    private static final String DEFAULT_COVERSHEET_URL = "default_coversheet_url";

    private static final String CONSENT_ORDER_NOT_APPROVED_COVER_LETTER_TEMPLATE = "FL-FRM-LET-ENG-00319.docx";
    private static final String CONSENT_ORDER_NOT_APPROVED_COVER_LETTER_FILENAME = "consentOrderNotApprovedCoverLetter.pdf";

    private static final String CONSENT_ORDER_NOT_APPROVED_REPLY_COVERSHEET_TEMPLATE = "FL-FRM-LET-ENG-00320.docx";
    private static final String CONSENT_ORDER_NOT_APPROVED_REPLY_COVERSHEET_FILENAME = "consentOrderNotApprovedReplyCoversheet.pdf";

    private static final String BULK_PRINT_TEMPLATE = "FL-FRM-LET-ENG-00522.docx";
    private static final String BULK_PRINT_FILENAME = "BulkPrintCoverSheet.pdf";

    @Autowired
    private DocumentClient documentClient;

    @Autowired
    private ConsentOrderNotApprovedDocumentService consentOrderNotApprovedDocumentService;

    private CaseDetails caseDetails;

    @Before
    public void setupDocumentGenerationMocks() {
        mockDocumentClientToReturnUrlForDocumentGenerationRequest(CONSENT_ORDER_NOT_APPROVED_COVER_LETTER_TEMPLATE,
            CONSENT_ORDER_NOT_APPROVED_COVER_LETTER_FILENAME, COVER_LETTER_URL);
        mockDocumentClientToReturnUrlForDocumentGenerationRequest(CONSENT_ORDER_NOT_APPROVED_REPLY_COVERSHEET_TEMPLATE,
            CONSENT_ORDER_NOT_APPROVED_REPLY_COVERSHEET_FILENAME, REPLY_COVERSHEET_URL);
        mockDocumentClientToReturnUrlForDocumentGenerationRequest(BULK_PRINT_TEMPLATE, BULK_PRINT_FILENAME, DEFAULT_COVERSHEET_URL);
    }

    public void setupConsentedCase() {
        caseDetails = defaultConsentedCaseDetails();

        Map<String, Object> caseData = caseDetails.getData();
        caseData.put(GENERAL_ORDER_LATEST_DOCUMENT, caseDocument());
        caseData.put(PAPER_APPLICATION, YES_VALUE);
        caseData.put(UPLOAD_ORDER, Collections.singletonList(
            ImmutableMap.of("value", ImmutableMap.of(
                "DocumentLink", ImmutableMap.of(
                    DOCUMENT_BINARY_URL, GENERAL_ORDER_URL)))));

        List<GeneralOrderConsentedData> generalOrders = new ArrayList<>();
        GeneralOrderConsented generalOrder = new GeneralOrderConsented();
        generalOrder.setGeneralOrder(caseDocument());
        generalOrders.add(new GeneralOrderConsentedData("123", generalOrder));
        caseData.put(GENERAL_ORDER_COLLECTION_CONSENTED, generalOrders);
    }

    private void mockDocumentClientToReturnUrlForDocumentGenerationRequest(String requestedTemplate, String requestedFilename,
                                                                           String generatedDocumentUrl) {
        Document generatedDocument = document();
        generatedDocument.setBinaryUrl(generatedDocumentUrl);
        when(documentClient.generatePdf(matchDocumentGenerationRequestTemplateAndFilename(requestedTemplate, requestedFilename),
            anyString())).thenReturn(generatedDocument);
    }

    @Test
    public void whenApplicantLetterPackIsPrepared_thenItHasExpectedDocuments_and_caseDataIsUpdated() {
        setupConsentedCase();

        List<BulkPrintDocument> generatedDocuments = consentOrderNotApprovedDocumentService.prepareApplicantLetterPack(
            caseDetails, AUTH_TOKEN);

        assertThat(generatedDocuments, hasSize(4));
        assertThat(generatedDocuments.get(0).getBinaryFileUrl(), is(COVER_LETTER_URL));
        assertThat(generatedDocuments.get(1).getBinaryFileUrl(), is(GENERAL_ORDER_URL));
        assertThat(generatedDocuments.get(2).getBinaryFileUrl(), is(TestSetUpUtils.BINARY_URL));
        assertThat(generatedDocuments.get(3).getBinaryFileUrl(), is(REPLY_COVERSHEET_URL));

        assertThat(caseDetails.getData().get(BULK_PRINT_COVER_SHEET_APP), is(notNullValue()));
    }

    @Test
    public void whenNoNotApprovedConsentOrderIsFound_thenApplicantPackPrintsWithoutIt() {
        setupConsentedCase();
        caseDetails.getData().put(UPLOAD_ORDER, null);

        List<BulkPrintDocument> generatedDocuments = consentOrderNotApprovedDocumentService.prepareApplicantLetterPack(
            caseDetails, AUTH_TOKEN);

        assertThat(generatedDocuments, hasSize(3));
        assertThat(generatedDocuments.get(0).getBinaryFileUrl(), is(COVER_LETTER_URL));
        assertThat(generatedDocuments.get(1).getBinaryFileUrl(), is(TestSetUpUtils.BINARY_URL));
        assertThat(generatedDocuments.get(2).getBinaryFileUrl(), is(REPLY_COVERSHEET_URL));

        assertThat(caseDetails.getData().get(BULK_PRINT_COVER_SHEET_APP), is(notNullValue()));
    }

    @Test
    public void getApplicantLetterPackWithNoConsentOrderAndNoGeneralOrdersReturnsEmptyList_withConsentDocGenOn() {
        caseDetails = defaultConsentedCaseDetails();
        caseDetails.getData().put(UPLOAD_ORDER, null);

        List<BulkPrintDocument> generatedDocuments = consentOrderNotApprovedDocumentService.prepareApplicantLetterPack(
            caseDetails, AUTH_TOKEN);

        assertThat(generatedDocuments, hasSize(0));

        assertThat(caseDetails.getData().get(BULK_PRINT_COVER_SHEET_APP), is(notNullValue()));
    }

    @Test
    public void givenConsentedInContestedCase_whenConsentOrderWasNotApproved_expectedDocumentsArePrinted() {
        caseDetails = defaultContestedCaseDetails();
        addConsentedInContestedConsentOrderNotApproved();

        List<BulkPrintDocument> generatedDocuments = consentOrderNotApprovedDocumentService.prepareApplicantLetterPack(
            caseDetails, AUTH_TOKEN);

        assertThat(generatedDocuments, hasSize(3));
        assertThat(caseDetails.getData().get(BULK_PRINT_COVER_SHEET_APP), is(notNullValue()));
    }

    private void addConsentedInContestedConsentOrderNotApproved() {
        Map<String, Object> caseData = caseDetails.getData();

        caseData.put(CONTESTED_CONSENT_ORDER_NOT_APPROVED_COLLECTION, Arrays.asList(ImmutableMap.of(
            "id", UUID.randomUUID().toString(),
            "value", ImmutableMap.of(
                "consentOrder", ImmutableMap.of(
                    "document_binary_url", "test_url_"
        )))));

    }
}
