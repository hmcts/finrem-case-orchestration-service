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
import org.springframework.test.annotation.DirtiesContext;
import org.springframework.test.context.ActiveProfiles;
import uk.gov.hmcts.reform.ccd.client.model.CaseDetails;
import uk.gov.hmcts.reform.finrem.caseorchestration.BaseServiceTest;
import uk.gov.hmcts.reform.finrem.caseorchestration.TestSetUpUtils;
import uk.gov.hmcts.reform.finrem.caseorchestration.helper.DocumentHelper;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.ApprovedOrder;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.CaseDocument;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.CollectionElement;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.FinremCaseDetails;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.PensionTypeCollection;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.document.Document;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.evidence.FileUploadResponse;
import uk.gov.hmcts.reform.finrem.caseorchestration.service.evidencemanagement.EvidenceManagementUploadService;

import java.nio.charset.StandardCharsets;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static java.util.Arrays.asList;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.hasSize;
import static org.hamcrest.Matchers.is;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.atLeastOnce;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static uk.gov.hmcts.reform.finrem.caseorchestration.OrchestrationConstants.NO_VALUE;
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
    @Autowired
    private DocumentHelper documentHelper;
    @Autowired
    private EvidenceManagementUploadService evidenceManagementUploadService;
    @Autowired
    private PdfStampingService pdfStampingServiceMock;
    @Autowired
    private DocmosisPdfGenerationService docmosisPdfGenerationServiceMock;

    @Value("${document.approvedConsentOrderTemplate}")
    private String documentApprovedConsentOrderTemplate;

    private CaseDetails caseDetails;

    @Before
    public void setUp() {
        caseDetails = defaultConsentedCaseDetails();

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

        caseDetails.getData().put(APPLICANT_REPRESENTED, NO_VALUE);

        CaseDocument generatedApprovedConsentOrderNotificationLetter =
            consentOrderApprovedDocumentService.generateApprovedConsentOrderCoverLetter(caseDetails, AUTH_TOKEN);

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
            consentOrderApprovedDocumentService.generateApprovedConsentOrderCoverLetter(caseDetails, AUTH_TOKEN);

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

    private List<CaseDocument> getDocumentList(Map<String, Object> data) {
        return mapper.convertValue(data.get(CONTESTED_CONSENT_ORDER_COLLECTION), new TypeReference<>() {
        });
    }

    private CaseDetails caseDetails() {
        return TestSetUpUtils.caseDetailsFromResource("/fixtures/bulkprint/bulk-print.json", mapper);
    }

    private FinremCaseDetails finremCaseDetails() {
        return TestSetUpUtils.finremCaseDetailsFromResource(
            "/fixtures/approvedOrder/consentedApprovedOrder.json", mapper);
    }
}
