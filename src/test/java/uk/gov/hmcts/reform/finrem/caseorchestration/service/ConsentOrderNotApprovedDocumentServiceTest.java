package uk.gov.hmcts.reform.finrem.caseorchestration.service;

import org.junit.Before;
import org.junit.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.test.context.ActiveProfiles;
import uk.gov.hmcts.reform.finrem.caseorchestration.BaseServiceTest;
import uk.gov.hmcts.reform.finrem.caseorchestration.TestSetUpUtils;
import uk.gov.hmcts.reform.finrem.caseorchestration.client.DocumentClient;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.document.BulkPrintDocument;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.document.DocumentGenerationRequest;
import uk.gov.hmcts.reform.finrem.ccd.domain.ConsentOrder;
import uk.gov.hmcts.reform.finrem.ccd.domain.ConsentOrderCollection;
import uk.gov.hmcts.reform.finrem.ccd.domain.Document;
import uk.gov.hmcts.reform.finrem.ccd.domain.FinremCaseData;
import uk.gov.hmcts.reform.finrem.ccd.domain.FinremCaseDetails;
import uk.gov.hmcts.reform.finrem.ccd.domain.GeneralOrder;
import uk.gov.hmcts.reform.finrem.ccd.domain.GeneralOrderCollection;
import uk.gov.hmcts.reform.finrem.ccd.domain.UploadOrder;
import uk.gov.hmcts.reform.finrem.ccd.domain.UploadOrderCollection;
import uk.gov.hmcts.reform.finrem.ccd.domain.YesOrNo;

import java.util.Collections;
import java.util.List;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.hasSize;
import static org.hamcrest.Matchers.is;
import static org.hamcrest.Matchers.nullValue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.when;
import static uk.gov.hmcts.reform.finrem.caseorchestration.TestConstants.AUTH_TOKEN;
import static uk.gov.hmcts.reform.finrem.caseorchestration.TestSetUpUtils.defaultConsentedFinremCaseDetails;
import static uk.gov.hmcts.reform.finrem.caseorchestration.TestSetUpUtils.defaultContestedFinremCaseDetails;
import static uk.gov.hmcts.reform.finrem.caseorchestration.TestSetUpUtils.newDocument;

@ActiveProfiles("test-mock-feign-clients")
public class ConsentOrderNotApprovedDocumentServiceTest extends BaseServiceTest {

    private static final String COVER_LETTER_URL = "cover_letter_url";
    private static final String GENERAL_ORDER_URL = "general_letter_url";

    @Autowired private ConsentOrderNotApprovedDocumentService consentOrderNotApprovedDocumentService;
    @Autowired private DocumentClient documentClientMock;

    private FinremCaseDetails caseDetails;

    @Before
    public void setupDocumentGenerationMocks() {
        Document generatedDocument = newDocument();
        generatedDocument.setBinaryUrl(COVER_LETTER_URL);

        when(documentClientMock.generatePdf(any(DocumentGenerationRequest.class), eq(AUTH_TOKEN))).thenReturn(generatedDocument);
    }

    public void setupContestedCase() {
        caseDetails = defaultContestedFinremCaseDetails();

        FinremCaseData caseData = caseDetails.getCaseData();
        caseData.getGeneralOrderWrapper().setGeneralOrderLatestDocument(newDocument());
        caseData.setPaperApplication(YesOrNo.YES);
        caseData.setUploadOrder(Collections.singletonList(
            UploadOrderCollection.builder()
                .value(UploadOrder.builder()
                    .documentLink(Document.builder()
                        .filename("mockFilename")
                        .binaryUrl(GENERAL_ORDER_URL)
                        .url("mockUrl")
                        .build())
                    .build())
                .build()
        ));

        caseData.getGeneralOrderWrapper().setGeneralOrderCollection(List.of(GeneralOrderCollection.builder()
            .value(GeneralOrder.builder()
                .generalOrderDocumentUpload(newDocument())
                .build())
            .build()));
    }

    @Test
    public void whenApplicantLetterPackIsPrepared_thenItHasExpectedDocuments_and_caseDataIsUpdated() {
        setupContestedCase();

        List<BulkPrintDocument> generatedDocuments = consentOrderNotApprovedDocumentService.prepareApplicantLetterPack(
            caseDetails, AUTH_TOKEN);

        assertThat(generatedDocuments, hasSize(2));
        assertThat(generatedDocuments.get(0).getBinaryFileUrl(), is(COVER_LETTER_URL));
        assertThat(generatedDocuments.get(1).getBinaryFileUrl(), is(TestSetUpUtils.BINARY_URL));

        assertThat(caseDetails.getCaseData().getBulkPrintCoverSheetApp(), is(nullValue()));
    }

    @Test
    public void whenNoNotApprovedConsentOrderIsFound_thenApplicantPackPrintsWithoutIt() {
        setupContestedCase();
        caseDetails.getCaseData().setUploadOrder(null);

        List<BulkPrintDocument> generatedDocuments = consentOrderNotApprovedDocumentService.prepareApplicantLetterPack(
            caseDetails, AUTH_TOKEN);

        assertThat(generatedDocuments, hasSize(2));
        assertThat(generatedDocuments.get(0).getBinaryFileUrl(), is(COVER_LETTER_URL));
        assertThat(generatedDocuments.get(1).getBinaryFileUrl(), is(TestSetUpUtils.BINARY_URL));

        assertThat(caseDetails.getCaseData().getBulkPrintCoverSheetApp(), is(nullValue()));
    }

    @Test
    public void getApplicantLetterPackWithNoConsentOrderAndNoGeneralOrdersReturnsEmptyList_withConsentDocGenOn() {
        caseDetails = defaultConsentedFinremCaseDetails();
        caseDetails.getCaseData().setUploadOrder(null);

        List<BulkPrintDocument> generatedDocuments = consentOrderNotApprovedDocumentService.prepareApplicantLetterPack(
            caseDetails, AUTH_TOKEN);

        assertThat(generatedDocuments, hasSize(0));

        assertThat(caseDetails.getCaseData().getBulkPrintCoverSheetApp(), is(nullValue()));
    }

    @Test
    public void givenConsentedInContestedCase_whenConsentOrderWasNotApproved_expectedDocumentsArePrinted() {
        caseDetails = defaultContestedFinremCaseDetails();
        addConsentedInContestedConsentOrderNotApproved();

        List<BulkPrintDocument> generatedDocuments = consentOrderNotApprovedDocumentService.prepareApplicantLetterPack(
            caseDetails, AUTH_TOKEN);

        assertThat(generatedDocuments, hasSize(2));
        assertThat(caseDetails.getCaseData().getBulkPrintCoverSheetApp(), is(nullValue()));
    }

    private void addConsentedInContestedConsentOrderNotApproved() {
        FinremCaseData caseData = caseDetails.getCaseData();

        caseData.getConsentOrderWrapper().setConsentedNotApprovedOrders(List.of(
            ConsentOrderCollection.builder()
                .value(ConsentOrder.builder()
                    .consentOrder(Document.builder()
                        .binaryUrl("test_url_")
                        .build())
                    .build())
                .build()
        ));
    }
}
