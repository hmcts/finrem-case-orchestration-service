package uk.gov.hmcts.reform.finrem.caseorchestration.service;

import org.junit.Before;
import org.junit.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.test.context.ActiveProfiles;
import uk.gov.hmcts.reform.finrem.caseorchestration.BaseServiceTest;
import uk.gov.hmcts.reform.finrem.caseorchestration.TestSetUpUtils;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.ApprovedOrder;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.CaseDocument;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.ConsentOrderCollection;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.FinremCaseData;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.FinremCaseDetails;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.GeneralOrder;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.GeneralOrderCollectionItem;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.UploadOrder;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.UploadOrderCollection;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.YesOrNo;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.document.BulkPrintDocument;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.evidence.FileUploadResponse;
import uk.gov.hmcts.reform.finrem.caseorchestration.service.evidencemanagement.EvidenceManagementUploadService;

import java.nio.charset.StandardCharsets;
import java.util.Collections;
import java.util.List;
import java.util.UUID;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.equalTo;
import static org.hamcrest.Matchers.hasSize;
import static org.junit.Assert.assertNull;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;
import static uk.gov.hmcts.reform.finrem.caseorchestration.TestConstants.AUTH_TOKEN;
import static uk.gov.hmcts.reform.finrem.caseorchestration.TestSetUpUtils.caseDocument;
import static uk.gov.hmcts.reform.finrem.caseorchestration.TestSetUpUtils.defaultConsentedFinremCaseDetails;
import static uk.gov.hmcts.reform.finrem.caseorchestration.TestSetUpUtils.defaultContestedFinremCaseDetails;

@ActiveProfiles("test-mock-feign-clients")
public class ConsentOrderNotApprovedDocumentServiceTest extends BaseServiceTest {

    private static final String COVER_LETTER_URL = "cover_letter_url";

    private static final String COVER_LETTER_BINARY_URL = "cover_letter_url/binary";
    private static final String GENERAL_ORDER_URL = "general_letter_url";

    @Autowired private ConsentOrderNotApprovedDocumentService consentOrderNotApprovedDocumentService;

    @Autowired private EvidenceManagementUploadService evidenceManagementUploadService;

    @Autowired private DocmosisPdfGenerationService docmosisPdfGenerationServiceMock;

    private FinremCaseDetails finremCaseDetails;

    @Before
    public void setupDocumentGenerationMocks() {

        when(evidenceManagementUploadService.upload(any(), any(), any()))
            .thenReturn(Collections.singletonList(
                FileUploadResponse.builder()
                    .fileName("app_docs.pdf")
                    .fileUrl(COVER_LETTER_URL)
                    .build()));

        when(docmosisPdfGenerationServiceMock.generateDocFrom(any(), any()))
            .thenReturn("".getBytes(StandardCharsets.UTF_8));
    }

    public void setupContestedCase() {
        finremCaseDetails = defaultContestedFinremCaseDetails();

        FinremCaseData caseData = finremCaseDetails.getData();
        caseData.getGeneralOrderWrapper().setGeneralOrderLatestDocument(caseDocument());
        caseData.setPaperApplication(YesOrNo.YES);
        CaseDocument caseDocument = CaseDocument.builder().documentUrl("mock_url").documentFilename("mock_file_name").build();
        UploadOrder uploadOrder = UploadOrder.builder().documentFileName("mock_file_name").documentLink(caseDocument).build();
        UploadOrderCollection collection = UploadOrderCollection.builder().value(uploadOrder).id(UUID.randomUUID().toString()).build();
        caseData.setUploadOrder(List.of(collection));

        GeneralOrder generalOrder = GeneralOrder.builder().generalOrderDocumentUpload(caseDocument).build();
        GeneralOrderCollectionItem generalOrderCollectionItem = GeneralOrderCollectionItem.builder().id("123").generalOrder(generalOrder).build();
        caseData.getGeneralOrderWrapper().setGeneralOrderCollection(List.of(generalOrderCollectionItem));
    }

    @Test
    public void whenApplicantLetterPackIsPrepared_thenItHasExpectedDocuments_and_caseDataIsUpdated() {
        setupContestedCase();

        List<BulkPrintDocument> generatedDocuments = consentOrderNotApprovedDocumentService.prepareApplicantLetterPack(
            finremCaseDetails, AUTH_TOKEN);

        assertThat(generatedDocuments, hasSize(2));
        assertThat(generatedDocuments.get(0).getBinaryFileUrl(), equalTo(COVER_LETTER_BINARY_URL));
        assertThat(generatedDocuments.get(1).getBinaryFileUrl(), equalTo(TestSetUpUtils.BINARY_URL));

        assertNull(finremCaseDetails.getData().getBulkPrintCoverSheetApp());
    }

    @Test
    public void whenNoNotApprovedConsentOrderIsFound_thenApplicantPackPrintsWithoutIt() {
        setupContestedCase();
        finremCaseDetails.getData().setUploadOrder(null);

        List<BulkPrintDocument> generatedDocuments = consentOrderNotApprovedDocumentService.prepareApplicantLetterPack(
            finremCaseDetails, AUTH_TOKEN);

        assertThat(generatedDocuments, hasSize(2));
        assertThat(generatedDocuments.get(0).getBinaryFileUrl(), equalTo(COVER_LETTER_BINARY_URL));
        assertThat(generatedDocuments.get(1).getBinaryFileUrl(), equalTo(TestSetUpUtils.BINARY_URL));

        assertNull(finremCaseDetails.getData().getBulkPrintCoverSheetApp());
    }

    @Test
    public void getApplicantLetterPackWithNoConsentOrderAndNoGeneralOrdersReturnsEmptyList_withConsentDocGenOn() {
        finremCaseDetails = defaultConsentedFinremCaseDetails();
        finremCaseDetails.getData().setUploadOrder(null);

        List<BulkPrintDocument> generatedDocuments = consentOrderNotApprovedDocumentService.prepareApplicantLetterPack(
            finremCaseDetails, AUTH_TOKEN);

        assertThat(generatedDocuments, hasSize(0));

        assertNull(finremCaseDetails.getData().getBulkPrintCoverSheetApp());
    }

    @Test
    public void givenConsentedInContestedCase_whenConsentOrderWasNotApproved_expectedDocumentsArePrinted() {
        finremCaseDetails = defaultContestedFinremCaseDetails();
        addConsentedInContestedConsentOrderNotApproved();

        List<BulkPrintDocument> generatedDocuments = consentOrderNotApprovedDocumentService.prepareApplicantLetterPack(
            finremCaseDetails, AUTH_TOKEN);

        assertThat(generatedDocuments, hasSize(2));
        assertNull(finremCaseDetails.getData().getBulkPrintCoverSheetApp());
    }

    private void addConsentedInContestedConsentOrderNotApproved() {
        FinremCaseData caseData = finremCaseDetails.getData();
        CaseDocument caseDocument = CaseDocument.builder().documentBinaryUrl("test_url_").build();
        ApprovedOrder approvedOrder = ApprovedOrder.builder().consentOrder(caseDocument).orderLetter(caseDocument).build();
        ConsentOrderCollection collection1 = ConsentOrderCollection.builder().approvedOrder(approvedOrder).id(UUID.randomUUID().toString()).build();
        ConsentOrderCollection collection2 = ConsentOrderCollection.builder().approvedOrder(approvedOrder).id(UUID.randomUUID().toString()).build();

        caseData.getConsentOrderWrapper().setConsentedNotApprovedOrders(List.of(collection1, collection2));

    }
}
