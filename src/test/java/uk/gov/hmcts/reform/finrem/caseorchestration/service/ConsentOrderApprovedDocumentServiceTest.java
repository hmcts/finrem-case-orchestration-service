package uk.gov.hmcts.reform.finrem.caseorchestration.service;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.Before;
import org.junit.Test;
import org.mockito.Mockito;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.test.annotation.DirtiesContext;
import org.springframework.test.context.ActiveProfiles;
import uk.gov.hmcts.reform.ccd.client.model.CaseDetails;
import uk.gov.hmcts.reform.finrem.caseorchestration.BaseServiceTest;
import uk.gov.hmcts.reform.finrem.caseorchestration.TestSetUpUtils;
import uk.gov.hmcts.reform.finrem.caseorchestration.client.DocumentClient;
import uk.gov.hmcts.reform.finrem.caseorchestration.helper.DocumentHelper;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.ApprovedOrder;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.CaseDocument;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.CollectionElement;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.FinremCaseDetails;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.PensionTypeCollection;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.document.Document;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static java.util.Arrays.asList;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.hasSize;
import static org.hamcrest.Matchers.is;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
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
import static uk.gov.hmcts.reform.finrem.caseorchestration.TestSetUpUtils.VARIATION_FILE_NAME;
import static uk.gov.hmcts.reform.finrem.caseorchestration.TestSetUpUtils.assertCaseDocument;
import static uk.gov.hmcts.reform.finrem.caseorchestration.TestSetUpUtils.caseDetailsFromResource;
import static uk.gov.hmcts.reform.finrem.caseorchestration.TestSetUpUtils.caseDocument;
import static uk.gov.hmcts.reform.finrem.caseorchestration.TestSetUpUtils.defaultConsentedCaseDetails;
import static uk.gov.hmcts.reform.finrem.caseorchestration.TestSetUpUtils.defaultConsentedCaseDetailsForVariationOrder;
import static uk.gov.hmcts.reform.finrem.caseorchestration.TestSetUpUtils.document;
import static uk.gov.hmcts.reform.finrem.caseorchestration.TestSetUpUtils.matchDocumentGenerationRequestTemplateAndFilename;
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

    private static final String DEFAULT_COVERSHEET_URL = "defaultCoversheetUrl";
    private static final String CONSENT_ORDER_APPROVED_COVER_LETTER_URL = "consentOrderApprovedCoverLetterUrl";

    @Autowired
    private ConsentOrderApprovedDocumentService consentOrderApprovedDocumentService;
    @Autowired
    private ObjectMapper mapper;
    @Autowired
    private DocumentHelper documentHelper;
    @Autowired
    private DocumentClient documentClientMock;
    @Autowired
    private GenericDocumentService genericDocumentService;

    @Value("${document.bulkPrintTemplate}")
    private String documentBulkPrintTemplate;
    @Value("${document.bulkPrintFileName}")
    private String documentBulkPrintFileName;

    @Value("${document.approvedConsentOrderTemplate}")
    private String documentApprovedConsentOrderTemplate;
    @Value("${document.approvedConsentOrderFileName}")
    private String documentApprovedConsentOrderFileName;

    @Value("${document.approvedVariationOrderFileName}")
    private String approvedVariationOrderFileName;

    @Value("${document.approvedConsentOrderNotificationTemplate}")
    private String documentApprovedConsentOrderNotificationTemplate;
    @Value("${document.approvedConsentOrderNotificationFileName}")
    private String documentApprovedConsentOrderNotificationFileName;

    private CaseDetails caseDetails;

    @Before
    public void setUp() {
        caseDetails = defaultConsentedCaseDetails();

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
    public void shouldGenerateApprovedVariationOrderLetterForConsented() {
        caseDetails = defaultConsentedCaseDetailsForVariationOrder();
        when(documentClientMock.generatePdf(matchDocumentGenerationRequestTemplateAndFilename(documentApprovedConsentOrderTemplate,
            approvedVariationOrderFileName), anyString())).thenReturn(variationDocument());
        CaseDocument caseDocument = consentOrderApprovedDocumentService.generateApprovedConsentOrderLetter(caseDetails, AUTH_TOKEN);

        assertThat(caseDocument.getDocumentFilename(), is(VARIATION_FILE_NAME));
        assertThat(caseDocument.getDocumentUrl(), is(DOC_URL));
        assertThat(caseDocument.getDocumentBinaryUrl(), is(BINARY_URL));

        verify(documentClientMock, atLeastOnce()).generatePdf(
            matchDocumentGenerationRequestTemplateAndFilename(documentApprovedConsentOrderTemplate, approvedVariationOrderFileName),
            anyString());
    }

    @Test
    public void shouldGenerateAndPopulateApprovedConsentOrderLetterForConsentInContested() {
        CaseDetails caseDetails = caseDetailsFromResource("/fixtures/contested/consent-in-contested-application-approved.json", mapper);
        consentOrderApprovedDocumentService.generateAndPopulateConsentOrderLetter(caseDetails, AUTH_TOKEN);
        List<CollectionElement<ApprovedOrder>> approvedOrders = consentOrderApprovedDocumentService.getConsentInContestedApprovedOrderCollection(
            caseDetails.getData());
        assertCaseDocument(approvedOrders.get(approvedOrders.size() - 1).getValue().getOrderLetter());
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

        List<PensionTypeCollection> pensionDocuments = asList(pensionDocumentData(), pensionDocumentData());
        List<PensionTypeCollection> stampPensionDocuments = consentOrderApprovedDocumentService.stampPensionDocuments(pensionDocuments, AUTH_TOKEN);

        stampPensionDocuments.forEach(data -> assertCaseDocument(data.getTypedCaseDocument().getPensionDocument()));
        verify(documentClientMock, times(2)).stampDocument(any(), anyString());
    }

    @Test
    public void givenNullDocumentInPensionDocuments_whenStampingDocuments_thenTheNullValueIsIgnored() {
        Mockito.reset(documentClientMock);
        when(documentClientMock.stampDocument(any(), anyString())).thenReturn(document());

        PensionTypeCollection pensionCollectionDataWithNullDocument = pensionDocumentData();
        pensionCollectionDataWithNullDocument.getTypedCaseDocument().setPensionDocument(null);
        List<PensionTypeCollection> pensionDocuments = asList(pensionDocumentData(), pensionCollectionDataWithNullDocument);

        List<PensionTypeCollection> stampPensionDocuments = consentOrderApprovedDocumentService.stampPensionDocuments(pensionDocuments, AUTH_TOKEN);

        assertThat(stampPensionDocuments, hasSize(1));
        stampPensionDocuments.forEach(data -> assertCaseDocument(data.getTypedCaseDocument().getPensionDocument()));
        verify(documentClientMock, times(1)).stampDocument(any(), anyString());
    }

    @Test
    public void stampsAndPopulatesCaseDataForContestedConsentOrder() {
        when(documentClientMock.stampDocument(any(), anyString())).thenReturn(document());
        when(documentClientMock.annexStampDocument(any(), anyString())).thenReturn(document());

        CaseDetails caseDetails = defaultConsentedCaseDetails();
        Map<String, Object> caseData = caseDetails.getData();
        caseData.put(CONSENT_ORDER, caseDocument());

        consentOrderApprovedDocumentService.stampAndPopulateContestedConsentApprovedOrderCollection(caseData, AUTH_TOKEN);
        assertThat(getDocumentList(caseData), hasSize(1));

        consentOrderApprovedDocumentService.stampAndPopulateContestedConsentApprovedOrderCollection(caseData, AUTH_TOKEN);
        assertThat(getDocumentList(caseData), hasSize(2));
    }

    @Test
    public void shouldConvertCollectionDocument() {
        List<CaseDocument> documents = consentOrderApprovedDocumentService.approvedOrderCollection(caseDetails(), AUTH_TOKEN);

        assertThat(documents, hasSize(3));
    }

    @Test
    public void givenFinremCaseDetails_whenAddGenApprovedDocs_thenCaseDocsAdded() {
        FinremCaseDetails finremCaseDetails = finremCaseDetails();

        when(documentClientMock.stampDocument(any(), anyString())).thenReturn(document());
        when(documentClientMock.annexStampDocument(any(), anyString())).thenReturn(document());

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
