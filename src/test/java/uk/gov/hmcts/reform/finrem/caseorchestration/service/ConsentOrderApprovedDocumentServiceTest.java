package uk.gov.hmcts.reform.finrem.caseorchestration.service;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.Before;
import org.junit.Test;
import org.mockito.Mockito;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.test.annotation.DirtiesContext;
import org.springframework.test.context.ActiveProfiles;
import uk.gov.hmcts.reform.ccd.client.model.CaseDetails;
import uk.gov.hmcts.reform.finrem.caseorchestration.BaseServiceTest;
import uk.gov.hmcts.reform.finrem.caseorchestration.client.DocumentClient;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.ApprovedOrderData;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.CaseDocument;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.PensionCollectionData;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.document.BulkPrintDocument;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.document.Document;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static java.util.Arrays.asList;
import static org.hamcrest.Matchers.hasSize;
import static org.hamcrest.Matchers.is;
import static org.junit.Assert.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.atLeastOnce;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static uk.gov.hmcts.reform.finrem.caseorchestration.OrchestrationConstants.NO_VALUE;
import static uk.gov.hmcts.reform.finrem.caseorchestration.OrchestrationConstants.PAPER_APPLICATION;
import static uk.gov.hmcts.reform.finrem.caseorchestration.OrchestrationConstants.YES_VALUE;
import static uk.gov.hmcts.reform.finrem.caseorchestration.TestConstants.AUTH_TOKEN;
import static uk.gov.hmcts.reform.finrem.caseorchestration.TestConstants.TEST_SOLICITOR_NAME;
import static uk.gov.hmcts.reform.finrem.caseorchestration.TestConstants.TEST_SOLICITOR_REFERENCE;
import static uk.gov.hmcts.reform.finrem.caseorchestration.TestSetUpUtils.assertCaseDocument;
import static uk.gov.hmcts.reform.finrem.caseorchestration.TestSetUpUtils.caseDetailsFromResource;
import static uk.gov.hmcts.reform.finrem.caseorchestration.TestSetUpUtils.caseDocument;
import static uk.gov.hmcts.reform.finrem.caseorchestration.TestSetUpUtils.defaultCaseDetails;
import static uk.gov.hmcts.reform.finrem.caseorchestration.TestSetUpUtils.document;
import static uk.gov.hmcts.reform.finrem.caseorchestration.TestSetUpUtils.matchDocumentGenerationRequestTemplateAndFilename;
import static uk.gov.hmcts.reform.finrem.caseorchestration.TestSetUpUtils.pensionDocumentData;
import static uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.CCDConfigConstant.APPLICANT_REPRESENTED;
import static uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.CCDConfigConstant.CONSENTED_SOLICITOR_ADDRESS;
import static uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.CCDConfigConstant.CONSENTED_SOLICITOR_NAME;
import static uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.CCDConfigConstant.CONSENT_ORDER;
import static uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.CCDConfigConstant.CONTESTED_CONSENT_ORDER_COLLECTION;
import static uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.CCDConfigConstant.SOLICITOR_REFERENCE;

@ActiveProfiles("test-mock-document-client")
public class ConsentOrderApprovedDocumentServiceTest extends BaseServiceTest {

    private static final String DEFAULT_COVERSHEET_URL = "defaultCoversheetUrl";
    private static final String CONSENT_ORDER_APPROVED_COVER_LETTER_URL = "consentOrderApprovedCoverLetterUrl";

    @Autowired
    private ObjectMapper mapper;

    @Autowired
    private ConsentOrderApprovedDocumentService consentOrderApprovedDocumentService;

    @Autowired
    private DocumentClient documentClientMock;

    @MockBean
    private BulkPrintService bulkPrintService;

    @Value("${document.bulkPrintTemplate}") private String documentBulkPrintTemplate;
    @Value("${document.bulkPrintFileName}") private String documentBulkPrintFileName;

    @Value("${document.approvedConsentOrderTemplate}") private String documentApprovedConsentOrderTemplate;
    @Value("${document.approvedConsentOrderFileName}") private String documentApprovedConsentOrderFileName;

    @Value("${document.approvedConsentOrderNotificationTemplate}") private String documentApprovedConsentOrderNotificationTemplate;
    @Value("${document.approvedConsentOrderNotificationFileName}") private String documentApprovedConsentOrderNotificationFileName;

    private CaseDetails caseDetails;
    public static final String ORDER_LETTER_URL = "orderLetterUrl";
    public static final String CONSENT_ORDER_URL = "consentOrderUrl";
    public static final String PENSION_DOCUMENT_URL = "pensionDocumentUrl";

    @Before
    public void setUp() {
        caseDetails = defaultCaseDetails();

        Document defaultCoversheet = document();
        defaultCoversheet.setBinaryUrl(DEFAULT_COVERSHEET_URL);

        when(documentClientMock.generatePdf(matchDocumentGenerationRequestTemplateAndFilename(documentBulkPrintTemplate,
            documentBulkPrintFileName), anyString())).thenReturn(defaultCoversheet);

        when(documentClientMock.generatePdf(matchDocumentGenerationRequestTemplateAndFilename(documentApprovedConsentOrderTemplate,
            documentApprovedConsentOrderFileName), anyString())).thenReturn(document());

        Document consentOrderApprovedCoverLetter = document();
        consentOrderApprovedCoverLetter.setBinaryUrl(CONSENT_ORDER_APPROVED_COVER_LETTER_URL);

        when(documentClientMock.generatePdf(matchDocumentGenerationRequestTemplateAndFilename(documentApprovedConsentOrderNotificationTemplate,
            documentApprovedConsentOrderNotificationFileName), anyString())).thenReturn(consentOrderApprovedCoverLetter);
    }

    @Test
    public void shouldGenerateApprovedConsentOrderLetterForConsented() {
        CaseDocument caseDocument = consentOrderApprovedDocumentService.generateApprovedConsentOrderLetter(caseDetails, AUTH_TOKEN);

        assertCaseDocument(caseDocument);
        verify(documentClientMock, atLeastOnce()).generatePdf(
            matchDocumentGenerationRequestTemplateAndFilename(documentApprovedConsentOrderTemplate, documentApprovedConsentOrderFileName),
            anyString());
    }

    @Test
    public void shouldGenerateAndPopulateApprovedConsentOrderLetterForConsentInContested() throws JsonProcessingException {
        Map<String, Object> data = consentOrderApprovedDocumentService.generateAndPopulateConsentOrderLetter(
            caseDetailsFromResource("/fixtures/contested/consent-in-contested-application-approved.json", mapper), AUTH_TOKEN);
        List<ApprovedOrderData> approvedOrders = getConsentInContestedApprovedOrderCollection(data);
        assertCaseDocument(approvedOrders.get(0).getApprovedOrder().getOrderLetter());
        verify(documentClientMock, atLeastOnce()).generatePdf(
            matchDocumentGenerationRequestTemplateAndFilename(documentApprovedConsentOrderTemplate, documentApprovedConsentOrderFileName),
            anyString());
    }

    @Test
    public void shouldGenerateApprovedConsentOrderLetterForContested() {
        CaseDetails contestedDetails = caseDetailsFromResource("/fixtures/contested/consent-in-contested-application-approved.json", mapper);
        CaseDocument caseDocument = consentOrderApprovedDocumentService.generateApprovedConsentOrderLetter(contestedDetails, AUTH_TOKEN);

        assertCaseDocument(caseDocument);
        verify(documentClientMock, atLeastOnce()).generatePdf(
            matchDocumentGenerationRequestTemplateAndFilename(documentApprovedConsentOrderTemplate, documentApprovedConsentOrderFileName),
            anyString());
    }

    @DirtiesContext(methodMode = DirtiesContext.MethodMode.AFTER_METHOD)
    @Test
    public void shouldGenerateApprovedConsentOrderCoverLetterForApplicant() {
        caseDetails.getData().put(APPLICANT_REPRESENTED, NO_VALUE);

        CaseDocument generatedApprovedConsentOrderNotificationLetter =
                consentOrderApprovedDocumentService.generateApprovedConsentOrderCoverLetter(caseDetails, AUTH_TOKEN);

        assertCaseDocument(generatedApprovedConsentOrderNotificationLetter, CONSENT_ORDER_APPROVED_COVER_LETTER_URL);
    }

    @Test
    public void shouldGenerateApprovedConsentOrderCoverLetterForApplicantSolicitor() {
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

        assertCaseDocument(generatedApprovedConsentOrderNotificationLetter, CONSENT_ORDER_APPROVED_COVER_LETTER_URL);
    }

    @Test
    public void shouldStampPensionDocuments() {
        Mockito.reset(documentClientMock);
        when(documentClientMock.stampDocument(any(), anyString())).thenReturn(document());

        List<PensionCollectionData> pensionDocuments = asList(pensionDocumentData(), pensionDocumentData());
        List<PensionCollectionData> stampPensionDocuments = consentOrderApprovedDocumentService.stampPensionDocuments(pensionDocuments, AUTH_TOKEN);

        stampPensionDocuments.forEach(data -> assertCaseDocument(data.getTypedCaseDocument().getPensionDocument()));
        verify(documentClientMock, times(2)).stampDocument(any(), anyString());
    }

    @Test
    public void whenPreparingApplicantLetterPack() {
        when(bulkPrintService.approvedOrderCollection(caseDetails)).thenReturn(preparePaperCaseConsentOrderApprovedCaseData());

        List<BulkPrintDocument> documents = consentOrderApprovedDocumentService.prepareApplicantLetterPack(caseDetails, AUTH_TOKEN);

        System.out.println(documents);
        assertThat(documents, hasSize(3));
        assertThat(documents.get(0).getBinaryFileUrl(), is(ORDER_LETTER_URL));
        assertThat(documents.get(1).getBinaryFileUrl(), is(CONSENT_ORDER_URL));
        assertThat(documents.get(2).getBinaryFileUrl(), is(PENSION_DOCUMENT_URL));
    }

    @Test
    public void whenPreparingApplicantLetterPack_paperApplication() {
        caseDetails.getData().put(PAPER_APPLICATION, YES_VALUE);
        when(bulkPrintService.approvedOrderCollection(caseDetails)).thenReturn(preparePaperCaseConsentOrderApprovedCaseData());

        List<BulkPrintDocument> documents = consentOrderApprovedDocumentService.prepareApplicantLetterPack(caseDetails, AUTH_TOKEN);

        System.out.println(documents);
        assertThat(documents, hasSize(4));
        assertThat(documents.get(0).getBinaryFileUrl(), is(CONSENT_ORDER_APPROVED_COVER_LETTER_URL));
        assertThat(documents.get(1).getBinaryFileUrl(), is(ORDER_LETTER_URL));
        assertThat(documents.get(2).getBinaryFileUrl(), is(CONSENT_ORDER_URL));
        assertThat(documents.get(3).getBinaryFileUrl(), is(PENSION_DOCUMENT_URL));
    }

    @Test
    public void stampsAndPopulatesCaseDataForContestedConsentOrder() {
        when(documentClientMock.stampDocument(any(), anyString())).thenReturn(document());
        when(documentClientMock.annexStampDocument(any(), anyString())).thenReturn(document());
        CaseDetails caseDetails = defaultCaseDetails();
        caseDetails.getData().put(CONSENT_ORDER, caseDocument());
        Map<String, Object> data = consentOrderApprovedDocumentService
            .stampAndPopulateContestedConsentApprovedOrderCollection(caseDetails.getData(), AUTH_TOKEN);
        assertThat(getDocumentList(data, CONTESTED_CONSENT_ORDER_COLLECTION), hasSize(1));

        caseDetails.setData(data);
        data = consentOrderApprovedDocumentService.stampAndPopulateContestedConsentApprovedOrderCollection(caseDetails.getData(), AUTH_TOKEN);
        assertThat(getDocumentList(data, CONTESTED_CONSENT_ORDER_COLLECTION), hasSize(2));
    }

    private List<CaseDocument> getDocumentList(Map<String, Object> data, String field) {
        return mapper.convertValue(data.get(field),
            new TypeReference<List<CaseDocument>>() {
            });
    }

    private List<BulkPrintDocument> preparePaperCaseConsentOrderApprovedCaseData() {
        List<BulkPrintDocument> bulkPrintDocuments = new ArrayList<>();

        bulkPrintDocuments.add(BulkPrintDocument.builder().binaryFileUrl(ORDER_LETTER_URL).build());
        bulkPrintDocuments.add(BulkPrintDocument.builder().binaryFileUrl(CONSENT_ORDER_URL).build());
        bulkPrintDocuments.add(BulkPrintDocument.builder().binaryFileUrl(PENSION_DOCUMENT_URL).build());

        return bulkPrintDocuments;
    }

    private List<ApprovedOrderData> getConsentInContestedApprovedOrderCollection(Map<String, Object> caseData) {
        return mapper.convertValue(caseData.get(CONTESTED_CONSENT_ORDER_COLLECTION), new TypeReference<List<ApprovedOrderData>>() {
        });
    }
}
