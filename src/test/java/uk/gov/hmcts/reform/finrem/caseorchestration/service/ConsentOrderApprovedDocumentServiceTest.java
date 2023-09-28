package uk.gov.hmcts.reform.finrem.caseorchestration.service;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.Before;
import org.junit.Test;
import org.mockito.ArgumentCaptor;
import org.mockito.Captor;
import org.mockito.Mockito;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.test.annotation.DirtiesContext;
import org.springframework.test.context.ActiveProfiles;
import uk.gov.hmcts.reform.ccd.client.model.CaseDetails;
import uk.gov.hmcts.reform.finrem.caseorchestration.BaseServiceTest;
import uk.gov.hmcts.reform.finrem.caseorchestration.TestSetUpUtils;
import uk.gov.hmcts.reform.finrem.caseorchestration.config.DocumentConfiguration;
import uk.gov.hmcts.reform.finrem.caseorchestration.helper.DocumentHelper;
import uk.gov.hmcts.reform.finrem.caseorchestration.mapper.letterdetails.bulkprint.BulkPrintCoverLetterDetailsMapper;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.ApplicantRepresentedPaper;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.ApprovedOrder;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.CaseDocument;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.CollectionElement;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.ConsentOrderCollection;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.FinremCaseDetails;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.PensionTypeCollection;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.RoleApprovedOrder;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.RoleConsentOrderCollection;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.UnapproveOrder;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.UnapprovedOrderCollection;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.wrapper.ConsentOrderWrapper;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.document.Document;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.evidence.FileUploadResponse;
import uk.gov.hmcts.reform.finrem.caseorchestration.service.evidencemanagement.EvidenceManagementAuditService;
import uk.gov.hmcts.reform.finrem.caseorchestration.service.evidencemanagement.EvidenceManagementUploadService;

import java.nio.charset.StandardCharsets;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;

import static java.util.Arrays.asList;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.equalTo;
import static org.hamcrest.Matchers.hasItem;
import static org.hamcrest.Matchers.hasSize;
import static org.hamcrest.Matchers.is;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.atLeastOnce;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static uk.gov.hmcts.reform.finrem.caseorchestration.OrchestrationConstants.YES_VALUE;
import static uk.gov.hmcts.reform.finrem.caseorchestration.TestConstants.AUTH_TOKEN;
import static uk.gov.hmcts.reform.finrem.caseorchestration.TestConstants.TEST_SOLICITOR_NAME;
import static uk.gov.hmcts.reform.finrem.caseorchestration.TestConstants.TEST_SOLICITOR_REFERENCE;
import static uk.gov.hmcts.reform.finrem.caseorchestration.TestSetUpUtils.BINARY_URL;
import static uk.gov.hmcts.reform.finrem.caseorchestration.TestSetUpUtils.DOC_URL;
import static uk.gov.hmcts.reform.finrem.caseorchestration.TestSetUpUtils.FILE_NAME;
import static uk.gov.hmcts.reform.finrem.caseorchestration.TestSetUpUtils.VARIATION_FILE_NAME;
import static uk.gov.hmcts.reform.finrem.caseorchestration.TestSetUpUtils.assertCaseDocument;
import static uk.gov.hmcts.reform.finrem.caseorchestration.TestSetUpUtils.caseDetailsFromResource;
import static uk.gov.hmcts.reform.finrem.caseorchestration.TestSetUpUtils.caseDocument;
import static uk.gov.hmcts.reform.finrem.caseorchestration.TestSetUpUtils.defaultConsentedCaseDetails;
import static uk.gov.hmcts.reform.finrem.caseorchestration.TestSetUpUtils.defaultConsentedCaseDetailsForVariationOrder;
import static uk.gov.hmcts.reform.finrem.caseorchestration.TestSetUpUtils.defaultContestedFinremCaseDetails;
import static uk.gov.hmcts.reform.finrem.caseorchestration.TestSetUpUtils.document;
import static uk.gov.hmcts.reform.finrem.caseorchestration.TestSetUpUtils.pensionDocumentData;
import static uk.gov.hmcts.reform.finrem.caseorchestration.TestSetUpUtils.variationDocument;
import static uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.CCDConfigConstant.APPLICANT_REPRESENTED;
import static uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.CCDConfigConstant.CONSENTED_SOLICITOR_ADDRESS;
import static uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.CCDConfigConstant.CONSENTED_SOLICITOR_NAME;
import static uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.CCDConfigConstant.CONSENT_ORDER;
import static uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.CCDConfigConstant.CONTESTED_CONSENT_ORDER_COLLECTION;
import static uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.CCDConfigConstant.SOLICITOR_REFERENCE;

@ActiveProfiles("test-mock-feign-clients")
public class ConsentOrderApprovedDocumentServiceTest extends BaseServiceTest {

    private static final String CONSENT_ORDER_APPROVED_COVER_LETTER_URL = "consentOrderApprovedCoverLetterUrl";

    @Captor
    private ArgumentCaptor<String> templateArgumentCaptor;

    @Autowired
    private ConsentOrderApprovedDocumentService consentOrderApprovedDocumentService;
    @Autowired
    private ObjectMapper mapper;
    @MockBean
    private BulkPrintCoverLetterDetailsMapper bulkPrintCoverLetterDetailsMapper;
    @MockBean
    private DocumentHelper documentHelper;
    @MockBean
    private DocumentConfiguration documentConfiguration;
    @MockBean
    private GenericDocumentService genericDocumentService;
    @MockBean
    private EvidenceManagementAuditService evidenceManagementAuditService;
    @Autowired
    private EvidenceManagementUploadService evidenceManagementUploadService;
    @Autowired
    private PdfStampingService pdfStampingServiceMock;
    @Autowired
    private DocmosisPdfGenerationService docmosisPdfGenerationServiceMock;
    @Autowired
    private DocumentOrderingService documentOrderingService;

    @Value("${document.approvedConsentOrderTemplate}")
    private String documentApprovedConsentOrderTemplate;

    private CaseDetails caseDetails;
    private FinremCaseDetails finremCaseDetails;

    @Before
    public void setUp() {
        caseDetails = defaultConsentedCaseDetails();
        finremCaseDetails = defaultContestedFinremCaseDetails();

        when(evidenceManagementUploadService.upload(any(), any(), any()))
            .thenReturn(Collections.singletonList(
                FileUploadResponse.builder()
                    .fileName(FILE_NAME)
                    .fileUrl(DOC_URL)
                    .build()));

        when(docmosisPdfGenerationServiceMock.generateDocFrom(any(), any()))
            .thenReturn("".getBytes(StandardCharsets.UTF_8));
    }

    @Test
    public void shouldGenerateApprovedConsentOrderLetterForConsented() {

        CaseDocument caseDocument = consentOrderApprovedDocumentService
            .generateApprovedConsentOrderLetter(caseDetails, AUTH_TOKEN);

        assertCaseDocument(caseDocument);
        verify(docmosisPdfGenerationServiceMock)
            .generateDocFrom(templateArgumentCaptor.capture(), any());
        assertThat(templateArgumentCaptor.getValue(), is(documentApprovedConsentOrderTemplate));
    }

    @Test
    public void shouldGenerateApprovedVariationOrderLetterForConsented() {

        when(evidenceManagementUploadService.upload(any(), any(), any()))
            .thenReturn(Collections.singletonList(
                FileUploadResponse.builder()
                    .fileName(VARIATION_FILE_NAME)
                    .fileUrl(DOC_URL)
                    .build()));

        caseDetails = defaultConsentedCaseDetailsForVariationOrder();
        when(docmosisPdfGenerationServiceMock
            .generateDocFrom(documentApprovedConsentOrderTemplate, caseDetails.getData()))
            .thenReturn(variationDocument().getBinaryUrl().getBytes(StandardCharsets.UTF_8));
        CaseDocument caseDocument = consentOrderApprovedDocumentService.generateApprovedConsentOrderLetter(caseDetails, AUTH_TOKEN);

        assertThat(caseDocument.getDocumentFilename(), is(VARIATION_FILE_NAME));
        assertThat(caseDocument.getDocumentUrl(), is(DOC_URL));
        assertThat(caseDocument.getDocumentBinaryUrl(), is(BINARY_URL));

        verify(docmosisPdfGenerationServiceMock, atLeastOnce())
            .generateDocFrom(templateArgumentCaptor.capture(), any());
        assertThat(templateArgumentCaptor.getAllValues().stream()
            .anyMatch(value -> value.equals(documentApprovedConsentOrderTemplate)), is(true));
    }

    @Test
    public void shouldGenerateAndPopulateApprovedConsentOrderLetterForConsentInContested() {
        CaseDetails caseDetails = caseDetailsFromResource("/fixtures/contested/consent-in-contested-application-approved.json", mapper);
        consentOrderApprovedDocumentService.generateAndPopulateConsentOrderLetter(caseDetails, AUTH_TOKEN);
        List<CollectionElement<ApprovedOrder>> approvedOrders = consentOrderApprovedDocumentService.getConsentInContestedApprovedOrderCollection(
            caseDetails.getData());
        assertCaseDocument(approvedOrders.get(approvedOrders.size() - 1).getValue().getOrderLetter());

        verify(docmosisPdfGenerationServiceMock, atLeastOnce())
            .generateDocFrom(templateArgumentCaptor.capture(), any());
        assertThat(templateArgumentCaptor.getValue(), is(documentApprovedConsentOrderTemplate));
    }

    @Test
    public void shouldGenerateApprovedConsentOrderLetterForContested() {
        CaseDetails contestedDetails = caseDetailsFromResource("/fixtures/contested/consent-in-contested-application-approved.json", mapper);
        CaseDocument caseDocument = consentOrderApprovedDocumentService.generateApprovedConsentOrderLetter(contestedDetails, AUTH_TOKEN);

        assertCaseDocument(caseDocument);
        verify(docmosisPdfGenerationServiceMock, atLeastOnce()).generateDocFrom(
            templateArgumentCaptor.capture(), any());
        assertThat(templateArgumentCaptor.getValue(), is(documentApprovedConsentOrderTemplate));
    }

    @DirtiesContext(methodMode = DirtiesContext.MethodMode.AFTER_METHOD)
    @Test
    public void shouldGenerateApprovedConsentOrderCoverLetterForApplicant() {
        when(evidenceManagementUploadService.upload(any(), any(), any()))
            .thenReturn(Collections.singletonList(
                FileUploadResponse.builder()
                    .fileName(FILE_NAME)
                    .fileUrl(CONSENT_ORDER_APPROVED_COVER_LETTER_URL)
                    .build()));

        finremCaseDetails.getData().setApplicantRepresentedPaper(ApplicantRepresentedPaper.FR_applicant_represented_1);

        CaseDocument generatedApprovedConsentOrderNotificationLetter =
            consentOrderApprovedDocumentService.generateApprovedConsentOrderCoverLetter(finremCaseDetails, AUTH_TOKEN);

        assertThat(generatedApprovedConsentOrderNotificationLetter.getDocumentFilename(), is(FILE_NAME));
        assertThat(generatedApprovedConsentOrderNotificationLetter.getDocumentUrl(),
            is(CONSENT_ORDER_APPROVED_COVER_LETTER_URL));
        assertThat(generatedApprovedConsentOrderNotificationLetter.getDocumentBinaryUrl(),
            is(CONSENT_ORDER_APPROVED_COVER_LETTER_URL + "/binary"));

    }

    @Test
    public void shouldGenerateApprovedConsentOrderCoverLetterForApplicantSolicitor() {

        when(evidenceManagementUploadService.upload(any(), any(), any()))
            .thenReturn(Collections.singletonList(
                FileUploadResponse.builder()
                    .fileName(document().getFileName())
                    .fileUrl(CONSENT_ORDER_APPROVED_COVER_LETTER_URL)
                    .build()));

        Map<String, Object> solicitorAddress = new HashMap<>();
        solicitorAddress.put("AddressLine1", "123 Applicant Solicitor Street");
        solicitorAddress.put("AddressLine2", "Second Address Line");
        solicitorAddress.put("AddressLine3", "Third Address Line");
        solicitorAddress.put("County", "London");
        solicitorAddress.put("Country", "England");
        solicitorAddress.put("PostTown", "London");
        solicitorAddress.put("PostCode", "SE1");

        Map<String, Object> caseData = caseDetails.getData();
        caseData.replace(APPLICANT_REPRESENTED, YES_VALUE);
        caseData.put(CONSENTED_SOLICITOR_NAME, TEST_SOLICITOR_NAME);
        caseData.put(SOLICITOR_REFERENCE, TEST_SOLICITOR_REFERENCE);
        caseData.put(CONSENTED_SOLICITOR_ADDRESS, solicitorAddress);

        CaseDocument generatedApprovedConsentOrderNotificationLetter =
            consentOrderApprovedDocumentService.generateApprovedConsentOrderCoverLetter(finremCaseDetails, AUTH_TOKEN);

        assertThat(generatedApprovedConsentOrderNotificationLetter.getDocumentFilename(), is(FILE_NAME));
        assertThat(generatedApprovedConsentOrderNotificationLetter.getDocumentUrl(), is(CONSENT_ORDER_APPROVED_COVER_LETTER_URL));
        assertThat(generatedApprovedConsentOrderNotificationLetter.getDocumentBinaryUrl(), is(CONSENT_ORDER_APPROVED_COVER_LETTER_URL + "/binary"));
    }

    @Test
    public void shouldStampPensionDocuments() throws Exception {
        Mockito.reset(pdfStampingServiceMock);
        when(pdfStampingServiceMock.stampDocument(document(), AUTH_TOKEN, false, StampType.FAMILY_COURT_STAMP, caseId)).thenReturn(document());

        List<PensionTypeCollection> pensionDocuments = asList(pensionDocumentData(), pensionDocumentData());
        List<PensionTypeCollection> stampPensionDocuments = consentOrderApprovedDocumentService
            .stampPensionDocuments(pensionDocuments, AUTH_TOKEN, StampType.FAMILY_COURT_STAMP, caseId);

        stampPensionDocuments.forEach(data -> assertCaseDocument(data.getTypedCaseDocument().getPensionDocument()));
        verify(pdfStampingServiceMock, times(2))
            .stampDocument(document(), AUTH_TOKEN, false, StampType.FAMILY_COURT_STAMP, caseId);
    }

    @Test
    public void givenNullDocumentInPensionDocuments_whenStampingDocuments_thenTheNullValueIsIgnored() throws Exception {
        Mockito.reset(pdfStampingServiceMock);
        when(pdfStampingServiceMock.stampDocument(
            document(), AUTH_TOKEN, false, StampType.FAMILY_COURT_STAMP, caseId))
            .thenReturn(document());
        when(pdfStampingServiceMock.stampDocument(
            document(), AUTH_TOKEN, false, StampType.FAMILY_COURT_STAMP, caseId)).thenReturn(document());

        PensionTypeCollection pensionCollectionDataWithNullDocument = pensionDocumentData();
        pensionCollectionDataWithNullDocument.getTypedCaseDocument().setPensionDocument(null);
        List<PensionTypeCollection> pensionDocuments =
            asList(pensionDocumentData(), pensionCollectionDataWithNullDocument);

        List<PensionTypeCollection> stampPensionDocuments = consentOrderApprovedDocumentService
            .stampPensionDocuments(pensionDocuments, AUTH_TOKEN, StampType.FAMILY_COURT_STAMP, caseId);

        assertThat(stampPensionDocuments, hasSize(1));
        stampPensionDocuments.forEach(data -> assertCaseDocument(data.getTypedCaseDocument().getPensionDocument()));
        verify(pdfStampingServiceMock, times(1))
            .stampDocument(document(), AUTH_TOKEN, false, StampType.FAMILY_COURT_STAMP, caseId);
    }

    @Test
    public void stampsAndPopulatesCaseDataForContestedConsentOrder() throws Exception {
        when(pdfStampingServiceMock.stampDocument(document(), AUTH_TOKEN, false, StampType.FAMILY_COURT_STAMP, caseId))
            .thenReturn(document());
        when(pdfStampingServiceMock.stampDocument(document(), AUTH_TOKEN, true, StampType.FAMILY_COURT_STAMP, caseId))
            .thenReturn(document());

        CaseDetails caseDetails = defaultConsentedCaseDetails();
        Map<String, Object> caseData = caseDetails.getData();
        caseData.put(CONSENT_ORDER, caseDocument());

        consentOrderApprovedDocumentService.stampAndPopulateContestedConsentApprovedOrderCollection(caseData, AUTH_TOKEN, caseId);
        assertThat(getDocumentList(caseData), hasSize(1));

        consentOrderApprovedDocumentService.stampAndPopulateContestedConsentApprovedOrderCollection(caseData, AUTH_TOKEN, caseId);
        assertThat(getDocumentList(caseData), hasSize(2));
    }

    @Test
    public void givenFinremCaseDetails_whenAddGenApprovedDocs_thenCaseDocsAdded() {
        FinremCaseDetails finremCaseDetails = finremCaseDetails();

        when(pdfStampingServiceMock.stampDocument(
            any(Document.class), eq(AUTH_TOKEN), eq(false), eq(StampType.FAMILY_COURT_STAMP), eq(caseId)))
            .thenReturn(document());
        when(pdfStampingServiceMock.stampDocument(
            any(Document.class), eq(AUTH_TOKEN), eq(true), eq(StampType.FAMILY_COURT_STAMP), eq(caseId)))
            .thenReturn(document());

        consentOrderApprovedDocumentService
           .addGeneratedApprovedConsentOrderDocumentsToCase(AUTH_TOKEN, finremCaseDetails);

        assertThat(finremCaseDetails.getData().getApprovedOrderCollection(), hasSize(1));
    }

    @Test
    public void givenFinremCaseDetails_whenAddApprovedConsentCoverLetter_thenCaseDocsAdded() {
        CaseDocument coverLetter = CaseDocument.builder()
            .documentFilename("approvedConsentOrderNotificationFileName")
            .documentUrl("approvedConsentOrderNotificationUrl")
            .documentBinaryUrl("approvedConsentOrderNotificationBinaryUrl")
            .build();
        when(documentHelper.prepareLetterTemplateData(
            finremCaseDetails, DocumentHelper.PaperNotificationRecipient.APPLICANT)).thenReturn(caseDetails);
        when(documentConfiguration.getApprovedConsentOrderNotificationFileName()).thenReturn("approvedConsentOrderNotificationFileName");
        when(genericDocumentService.generateDocument(eq(AUTH_TOKEN), any(CaseDetails.class), anyString(), anyString())).thenReturn(coverLetter);
        when(documentConfiguration.getApprovedConsentOrderNotificationTemplate()).thenReturn("approvedConsentOrderNotificationTemplate");
        List<CaseDocument> documents = new ArrayList<>();
        consentOrderApprovedDocumentService.addApprovedConsentCoverLetter(
            finremCaseDetails, documents, AUTH_TOKEN, DocumentHelper.PaperNotificationRecipient.APPLICANT);
        assertThat(documents, hasSize(1));
        assertThat(documents, hasItem(coverLetter));
    }

    @Test
    public void givenApprovedOrderModifiedLatest_whenThereIsANotApprovedOrder_thenReturnTrue() {
        CaseDocument caseDocument = CaseDocument.builder().documentBinaryUrl("test_url_").build();
        CaseDocument caseDocument2 = CaseDocument.builder().documentBinaryUrl("test_url_2").build();
        RoleApprovedOrder approvedOrder = RoleApprovedOrder.builder().consentOrder(caseDocument).orderLetter(caseDocument).build();
        UnapproveOrder approvedOrder2 = UnapproveOrder.builder().caseDocument(caseDocument2).orderReceivedAt(LocalDateTime.now()).build();
        RoleConsentOrderCollection collection1 = RoleConsentOrderCollection.builder()
            .approvedOrder(approvedOrder).id(UUID.randomUUID().toString()).build();
        UnapprovedOrderCollection collection2 = UnapprovedOrderCollection.builder()
            .value(approvedOrder2).id(UUID.randomUUID().toString()).build();
        ConsentOrderWrapper wrapper = ConsentOrderWrapper.builder()
            .appConsentApprovedOrders(List.of(collection1)).appRefusedOrderCollection(List.of(collection2)).build();
        consentOrderApprovedDocumentService.getApprovedOrderModifiedAfterNotApprovedOrder(wrapper, AUTH_TOKEN);
        when(evidenceManagementAuditService.audit(any(), eq(AUTH_TOKEN))).thenReturn(asList(
            FileUploadResponse.builder().modifiedOn(LocalDateTime.now().toString()).build(),
            FileUploadResponse.builder().modifiedOn(LocalDateTime.now().minusDays(2).toString()).build()));
        assertThat(documentOrderingService.isDocumentModifiedLater(caseDocument, caseDocument2, AUTH_TOKEN), is(true));
    }

    @Test
    public void givenNotApprovedOrderModifiedLatest_whenThereIsAApprovedOrder_thenReturnFalse() {
        CaseDocument caseDocument = CaseDocument.builder().documentBinaryUrl("test_url_").build();
        CaseDocument caseDocument2 = CaseDocument.builder().documentBinaryUrl("test_url_2").build();
        RoleApprovedOrder approvedOrder = RoleApprovedOrder.builder().consentOrder(caseDocument).orderLetter(caseDocument).build();
        UnapproveOrder approvedOrder2 = UnapproveOrder.builder().caseDocument(caseDocument2).orderReceivedAt(LocalDateTime.now()).build();
        RoleConsentOrderCollection collection1 = RoleConsentOrderCollection.builder()
            .approvedOrder(approvedOrder).id(UUID.randomUUID().toString()).build();
        UnapprovedOrderCollection collection2 = UnapprovedOrderCollection.builder().value(approvedOrder2).id(UUID.randomUUID().toString()).build();
        ConsentOrderWrapper wrapper = ConsentOrderWrapper.builder().appConsentApprovedOrders(List.of(collection1))
            .appRefusedOrderCollection(List.of(collection2)).build();
        consentOrderApprovedDocumentService.getApprovedOrderModifiedAfterNotApprovedOrder(wrapper, AUTH_TOKEN);
        when(evidenceManagementAuditService.audit(any(), eq(AUTH_TOKEN))).thenReturn(asList(
            FileUploadResponse.builder().modifiedOn(LocalDateTime.now().minusDays(2).toString()).build(),
            FileUploadResponse.builder().modifiedOn(LocalDateTime.now().toString()).build()
        ));
        assertThat(documentOrderingService.isDocumentModifiedLater(caseDocument, caseDocument2, AUTH_TOKEN), is(false));
    }

    @Test
    public void givenNoNotApprovedOrder_whenThereIsAApprovedOrder_thenReturnTrue() {
        CaseDocument caseDocument = CaseDocument.builder().documentBinaryUrl("test_url_").build();
        ApprovedOrder approvedOrder = ApprovedOrder.builder().consentOrder(caseDocument).orderLetter(caseDocument).build();
        ConsentOrderCollection collection1 = ConsentOrderCollection.builder().approvedOrder(approvedOrder).id(UUID.randomUUID().toString()).build();
        ConsentOrderWrapper wrapper = ConsentOrderWrapper.builder().contestedConsentedApprovedOrders(List.of(collection1)).build();
        assertThat(consentOrderApprovedDocumentService.getApprovedOrderModifiedAfterNotApprovedOrder(wrapper, AUTH_TOKEN), equalTo(true));
    }

    @Test
    public void givenNoApprovedOrder_whenThereIsANotApprovedOrder_thenReturnFalse() {
        CaseDocument caseDocument = CaseDocument.builder().documentBinaryUrl("test_url_2").build();
        ApprovedOrder approvedOrder = ApprovedOrder.builder().consentOrder(caseDocument).build();
        ConsentOrderCollection collection = ConsentOrderCollection.builder().approvedOrder(approvedOrder).id(UUID.randomUUID().toString()).build();
        ConsentOrderWrapper wrapper = ConsentOrderWrapper.builder().consentedNotApprovedOrders(List.of(collection)).build();
        assertThat(consentOrderApprovedDocumentService.getApprovedOrderModifiedAfterNotApprovedOrder(wrapper, AUTH_TOKEN), equalTo(false));
    }

    private List<CaseDocument> getDocumentList(Map<String, Object> data) {
        return mapper.convertValue(data.get(CONTESTED_CONSENT_ORDER_COLLECTION), new TypeReference<>() {
        });
    }

    private FinremCaseDetails finremCaseDetails() {
        return TestSetUpUtils.finremCaseDetailsFromResource(
            "/fixtures/approvedOrder/consentedApprovedOrder.json", mapper);
    }
}
