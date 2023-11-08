package uk.gov.hmcts.reform.finrem.caseorchestration.service;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.Before;
import org.junit.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.test.context.ActiveProfiles;
import uk.gov.hmcts.reform.ccd.client.model.CaseDetails;
import uk.gov.hmcts.reform.finrem.caseorchestration.BaseServiceTest;
import uk.gov.hmcts.reform.finrem.caseorchestration.helper.DocumentHelper;
import uk.gov.hmcts.reform.finrem.caseorchestration.mapper.FinremCaseDetailsMapper;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.ApprovedOrder;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.CaseDocument;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.ConsentOrderCollection;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.ContestedConsentOrderData;
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
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.UUID;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.equalTo;
import static org.hamcrest.Matchers.hasSize;
import static org.junit.Assert.assertNull;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.when;
import static uk.gov.hmcts.reform.finrem.caseorchestration.TestConstants.AUTH_TOKEN;
import static uk.gov.hmcts.reform.finrem.caseorchestration.TestSetUpUtils.caseDocument;
import static uk.gov.hmcts.reform.finrem.caseorchestration.TestSetUpUtils.defaultContestedCaseDetails;
import static uk.gov.hmcts.reform.finrem.caseorchestration.TestSetUpUtils.defaultContestedFinremCaseDetails;

@ActiveProfiles("test-mock-feign-clients")
public class ConsentOrderNotApprovedDocumentServiceTest extends BaseServiceTest {

    private static final String COVER_LETTER_URL = "cover_letter_url";

    private static final String COVER_LETTER_BINARY_URL = "cover_letter_url/binary";
    private static final String GENERAL_ORDER_URL = "general_letter_url";

    @MockBean
    private DocumentOrderingService documentOrderingService;

    @MockBean
    private DocumentHelper documentHelper;

    @MockBean
    private GenericDocumentService genericDocumentService;

    @InjectMocks
    private ObjectMapper objectMapper;

    @Mock
    private FinremCaseDetailsMapper finremCaseDetailsMapper;

    @Autowired private ConsentOrderNotApprovedDocumentService consentOrderNotApprovedDocumentService;

    @Autowired private EvidenceManagementUploadService evidenceManagementUploadService;

    @Autowired private DocmosisPdfGenerationService docmosisPdfGenerationServiceMock;

    @Autowired private CaseDataService caseDataService;

    private FinremCaseDetails finremCaseDetails;
    private CaseDetails caseDetails;

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
        caseDetails = defaultContestedCaseDetails();
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
    public void whenApplicantLetterPackIsPrepared_thenItHasExpectedDocumentsAndCaseDataIsUpdated() {
        setupContestedCase();

        CaseDocument caseDocument = caseDocument("docurl1", "file1", "binurl1");
        CaseDocument caseDocument2 = caseDocument("docurl2", "file2", "binurl2");
        addConsentedInContestedConsentOrderNotApproved();
        ApprovedOrder approvedOrder2 = ApprovedOrder.builder().consentOrder(caseDocument).orderLetter(caseDocument2).build();
        ConsentOrderCollection collection2 = ConsentOrderCollection.builder().approvedOrder(approvedOrder2).id(UUID.randomUUID().toString()).build();
        when(documentHelper.prepareLetterTemplateData(
            any(FinremCaseDetails.class), any(DocumentHelper.PaperNotificationRecipient.class))).thenReturn(caseDetails);
        when(documentHelper.getLatestGeneralOrder(any(FinremCaseData.class))).thenReturn(caseDocument2);
        when(genericDocumentService.generateDocument(anyString(), any(CaseDetails.class), anyString(), anyString())).thenReturn(caseDocument);
        BulkPrintDocument bulkPrintDocument = BulkPrintDocument.builder().fileName("file1").binaryFileUrl("binurl1").build();
        BulkPrintDocument bulkPrintDocument2 = BulkPrintDocument.builder().fileName("file2").binaryFileUrl("binurl2").build();
        when(documentHelper.getCaseDocumentAsBulkPrintDocument(any(CaseDocument.class))).thenReturn(bulkPrintDocument);
        when(documentHelper.getCaseDocumentsAsBulkPrintDocuments(any())).thenReturn(List.of(bulkPrintDocument2));
        when(documentOrderingService.isDocumentModifiedLater(any(), any(), anyString())).thenReturn(true);
        when(documentHelper.convertToContestedConsentOrderData(any())).thenReturn(convertToContestedConsentOrderData(List.of(collection2)));
        List<BulkPrintDocument> generatedDocuments = consentOrderNotApprovedDocumentService.prepareApplicantLetterPack(
            finremCaseDetails, AUTH_TOKEN);

        assertThat(generatedDocuments, hasSize(2));
        assertThat(generatedDocuments.get(0).getBinaryFileUrl(), equalTo("binurl1"));
        assertThat(generatedDocuments.get(1).getBinaryFileUrl(), equalTo("binurl2"));

        assertNull(finremCaseDetails.getData().getBulkPrintCoverSheetApp());
    }

    @Test
    public void whenApplicantLetterPackWithNoConsentOrderAndNoGeneralOrders_thenReturnEmptyList() {
        setupContestedCase();

        BulkPrintDocument bulkPrintDocument = BulkPrintDocument.builder().fileName("file1").binaryFileUrl("binurl1").build();
        BulkPrintDocument bulkPrintDocument2 = BulkPrintDocument.builder().fileName("file2").binaryFileUrl("binurl2").build();
        CaseDocument caseDocument = caseDocument("docurl1", "file1", "binurl1");
        CaseDocument caseDocument2 = caseDocument("docurl2", "file2", "binurl2");
        ApprovedOrder approvedOrder2 = ApprovedOrder.builder().consentOrder(caseDocument).orderLetter(caseDocument2).build();
        ConsentOrderCollection collection2 = ConsentOrderCollection.builder().approvedOrder(approvedOrder2).id(UUID.randomUUID().toString()).build();
        when(documentHelper.prepareLetterTemplateData(
            any(FinremCaseDetails.class), any(DocumentHelper.PaperNotificationRecipient.class))).thenReturn(caseDetails);
        when(documentHelper.getLatestGeneralOrder(any(FinremCaseData.class))).thenReturn(null);
        when(genericDocumentService.generateDocument(anyString(), any(CaseDetails.class), anyString(), anyString())).thenReturn(caseDocument);
        when(documentHelper.getCaseDocumentAsBulkPrintDocument(any(CaseDocument.class))).thenReturn(bulkPrintDocument);
        when(documentHelper.getCaseDocumentsAsBulkPrintDocuments(any())).thenReturn(List.of(bulkPrintDocument2));
        when(documentOrderingService.isDocumentModifiedLater(any(), any(), anyString())).thenReturn(true);
        when(documentHelper.convertToContestedConsentOrderData(any())).thenReturn(convertToContestedConsentOrderData(List.of(collection2)));
        List<BulkPrintDocument> generatedDocuments = consentOrderNotApprovedDocumentService.prepareApplicantLetterPack(
            finremCaseDetails, AUTH_TOKEN);

        assertThat(generatedDocuments, hasSize(0));

        assertNull(finremCaseDetails.getData().getBulkPrintCoverSheetApp());
    }

    @Test
    public void givenOrderDocumentsOnCase_whenGettingOrderDocument_thenShouldGetLatest() {
        CaseDocument firstCaseDocument = caseDocument("url", "filename", "binary");
        CaseDocument secondCaseDocument = caseDocument("url2", "filename2", "binary2");
        when(documentOrderingService.isDocumentModifiedLater(any(), any(), anyString())).thenReturn(true);
        CaseDocument result = consentOrderNotApprovedDocumentService.getLatestOrderDocument(secondCaseDocument, firstCaseDocument, "token");
        assertThat(result, equalTo(firstCaseDocument));
        CaseDocument result2 = consentOrderNotApprovedDocumentService.getLatestOrderDocument(null, secondCaseDocument, "token");
        assertThat(result2, equalTo(secondCaseDocument));
        CaseDocument result3 = consentOrderNotApprovedDocumentService.getLatestOrderDocument(firstCaseDocument, null, "token");
        assertThat(result3, equalTo(firstCaseDocument));
    }

    @Test
    public void givenOrderDocumentsOnCase_whenNotApprovedConsentCoverLetterAdded_shouldAddAppropriateLetter() {
        setupContestedCase();
        List<CaseDocument> documents = new ArrayList<>();
        CaseDocument docToAdd = caseDocument("url", "filename", "binary");
        when(genericDocumentService.generateDocument(any(), any(), any(), any())).thenReturn(docToAdd);
        when(documentHelper.prepareLetterTemplateData(
            finremCaseDetails, DocumentHelper.PaperNotificationRecipient.APPLICANT)).thenReturn(caseDetails);
        consentOrderNotApprovedDocumentService.addNotApprovedConsentCoverLetter(
            finremCaseDetails, documents, AUTH_TOKEN, DocumentHelper.PaperNotificationRecipient.APPLICANT);
        List<CaseDocument> expectedDocuments = List.of(docToAdd);
        assertThat(expectedDocuments, equalTo(documents));
    }

    private void addConsentedInContestedConsentOrderNotApproved() {
        FinremCaseData caseData = finremCaseDetails.getData();
        CaseDocument caseDocument = CaseDocument.builder().documentBinaryUrl("test_url_").build();
        ApprovedOrder approvedOrder = ApprovedOrder.builder().consentOrder(caseDocument).orderLetter(caseDocument).build();
        ConsentOrderCollection collection1 = ConsentOrderCollection.builder().approvedOrder(approvedOrder).id(UUID.randomUUID().toString()).build();
        ConsentOrderCollection collection2 = ConsentOrderCollection.builder().approvedOrder(approvedOrder).id(UUID.randomUUID().toString()).build();

        caseData.getConsentOrderWrapper().setConsentedNotApprovedOrders(List.of(collection1, collection2));
    }

    public List<ContestedConsentOrderData> convertToContestedConsentOrderData(Object object) {
        return objectMapper.convertValue(object, new TypeReference<>() {
        });
    }




}
