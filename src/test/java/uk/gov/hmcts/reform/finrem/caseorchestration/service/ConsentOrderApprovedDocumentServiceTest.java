package uk.gov.hmcts.reform.finrem.caseorchestration.service;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.google.common.collect.Iterables;
import org.junit.Before;
import org.junit.Test;
import org.mockito.Mockito;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.test.annotation.DirtiesContext;
import org.springframework.test.context.ActiveProfiles;
import uk.gov.hmcts.reform.finrem.caseorchestration.BaseServiceTest;
import uk.gov.hmcts.reform.finrem.caseorchestration.client.DocumentClient;
import uk.gov.hmcts.reform.finrem.caseorchestration.helper.DocumentHelper;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.document.BulkPrintDocument;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.document.DocumentClientDocument;
import uk.gov.hmcts.reform.finrem.ccd.domain.Address;
import uk.gov.hmcts.reform.finrem.ccd.domain.CaseType;
import uk.gov.hmcts.reform.finrem.ccd.domain.ConsentOrder;
import uk.gov.hmcts.reform.finrem.ccd.domain.ConsentOrderCollection;
import uk.gov.hmcts.reform.finrem.ccd.domain.Document;
import uk.gov.hmcts.reform.finrem.ccd.domain.FinremCaseData;
import uk.gov.hmcts.reform.finrem.ccd.domain.FinremCaseDetails;
import uk.gov.hmcts.reform.finrem.ccd.domain.PensionTypeCollection;
import uk.gov.hmcts.reform.finrem.ccd.domain.YesOrNo;

import java.io.IOException;
import java.util.List;

import static java.util.Arrays.asList;
import static java.util.Collections.singletonList;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.hasSize;
import static org.hamcrest.Matchers.is;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.atLeastOnce;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static uk.gov.hmcts.reform.finrem.caseorchestration.TestConstants.AUTH_TOKEN;
import static uk.gov.hmcts.reform.finrem.caseorchestration.TestConstants.TEST_SOLICITOR_NAME;
import static uk.gov.hmcts.reform.finrem.caseorchestration.TestConstants.TEST_SOLICITOR_REFERENCE;
import static uk.gov.hmcts.reform.finrem.caseorchestration.TestSetUpUtils.BINARY_URL;
import static uk.gov.hmcts.reform.finrem.caseorchestration.TestSetUpUtils.DOC_URL;
import static uk.gov.hmcts.reform.finrem.caseorchestration.TestSetUpUtils.VARIATION_FILE_NAME;
import static uk.gov.hmcts.reform.finrem.caseorchestration.TestSetUpUtils.assertCaseDocument;
import static uk.gov.hmcts.reform.finrem.caseorchestration.TestSetUpUtils.defaultConsentedCaseDetailsForVariationOrder;
import static uk.gov.hmcts.reform.finrem.caseorchestration.TestSetUpUtils.defaultConsentedFinremCaseDetails;
import static uk.gov.hmcts.reform.finrem.caseorchestration.TestSetUpUtils.finremCaseDetailsFromResource;
import static uk.gov.hmcts.reform.finrem.caseorchestration.TestSetUpUtils.matchDocumentGenerationRequestTemplateAndFilename;
import static uk.gov.hmcts.reform.finrem.caseorchestration.TestSetUpUtils.newDocument;
import static uk.gov.hmcts.reform.finrem.caseorchestration.TestSetUpUtils.newDocumentClientDocument;
import static uk.gov.hmcts.reform.finrem.caseorchestration.TestSetUpUtils.pensionTypeCollection;
import static uk.gov.hmcts.reform.finrem.caseorchestration.TestSetUpUtils.variationDocumentClientDocument;

@ActiveProfiles("test-mock-feign-clients")
public class ConsentOrderApprovedDocumentServiceTest extends BaseServiceTest {

    private static final String DEFAULT_COVERSHEET_URL = "defaultCoversheetUrl";
    private static final String CONSENT_ORDER_APPROVED_COVER_LETTER_URL = "consentOrderApprovedCoverLetterUrl";
    private static final String ORDER_LETTER_URL = "orderLetterUrl";
    private static final String CONSENT_ORDER_URL = "consentOrderUrl";
    private static final String PENSION_DOCUMENT_URL = "pensionDocumentUrl";

    @Autowired private ConsentOrderApprovedDocumentService consentOrderApprovedDocumentService;
    @Autowired private ObjectMapper mapper;
    @Autowired private DocumentHelper documentHelper;
    @Autowired private DocumentClient documentClientMock;

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

    private FinremCaseDetails caseDetails;

    @Before
    public void setUp() {
        caseDetails = defaultConsentedFinremCaseDetails();

        DocumentClientDocument defaultCoversheetResponse = newDocumentClientDocument();
        defaultCoversheetResponse.setBinaryUrl(DEFAULT_COVERSHEET_URL);

        when(documentClientMock.generatePdf(matchDocumentGenerationRequestTemplateAndFilename(documentBulkPrintTemplate,
            documentBulkPrintFileName), anyString())).thenReturn(defaultCoversheetResponse);

        when(documentClientMock.generatePdf(matchDocumentGenerationRequestTemplateAndFilename(documentApprovedConsentOrderTemplate,
            documentApprovedConsentOrderFileName), anyString())).thenReturn(newDocumentClientDocument());

        DocumentClientDocument consentOrderApprovedCoverLetterResponse = newDocumentClientDocument();
        consentOrderApprovedCoverLetterResponse.setBinaryUrl(CONSENT_ORDER_APPROVED_COVER_LETTER_URL);

        when(documentClientMock.generatePdf(matchDocumentGenerationRequestTemplateAndFilename(documentApprovedConsentOrderNotificationTemplate,
            documentApprovedConsentOrderNotificationFileName), anyString())).thenReturn(consentOrderApprovedCoverLetterResponse);
    }

    @Test
    public void shouldGenerateApprovedConsentOrderLetterForConsented() {
        Document caseDocument = consentOrderApprovedDocumentService.generateApprovedConsentOrderLetter(caseDetails, AUTH_TOKEN);

        assertCaseDocument(caseDocument);
        verify(documentClientMock, atLeastOnce()).generatePdf(
            matchDocumentGenerationRequestTemplateAndFilename(documentApprovedConsentOrderTemplate, documentApprovedConsentOrderFileName),
            anyString());
    }

    @Test
    public void shouldGenerateApprovedVariationOrderLetterForConsented() {
        caseDetails = defaultConsentedCaseDetailsForVariationOrder();
        when(documentClientMock.generatePdf(matchDocumentGenerationRequestTemplateAndFilename(documentApprovedConsentOrderTemplate,
            approvedVariationOrderFileName), anyString())).thenReturn(variationDocumentClientDocument());
        Document caseDocument = consentOrderApprovedDocumentService.generateApprovedConsentOrderLetter(caseDetails, AUTH_TOKEN);

        assertThat(caseDocument.getFilename(), is(VARIATION_FILE_NAME));
        assertThat(caseDocument.getUrl(), is(DOC_URL));
        assertThat(caseDocument.getBinaryUrl(), is(BINARY_URL));

        verify(documentClientMock, atLeastOnce()).generatePdf(
            matchDocumentGenerationRequestTemplateAndFilename(documentApprovedConsentOrderTemplate, approvedVariationOrderFileName),
            anyString());
    }

    @Test
    public void shouldGenerateAndPopulateApprovedConsentOrderLetterForConsentInContested() throws IOException {
        FinremCaseDetails caseDetails = finremCaseDetailsFromResource(
            getResource("/fixtures/contested/consent-in-contested-application-approved.json"), mapper);
        consentOrderApprovedDocumentService.generateAndPopulateConsentOrderLetter(caseDetails, AUTH_TOKEN);
        List<ConsentOrderCollection> approvedOrders = caseDetails.getCaseData().getConsentOrderWrapper()
            .getContestedConsentedApprovedOrders();

        assertCaseDocument(Iterables.getLast(approvedOrders).getValue().getOrderLetter());
        verify(documentClientMock, atLeastOnce()).generatePdf(
            matchDocumentGenerationRequestTemplateAndFilename(documentApprovedConsentOrderTemplate, documentApprovedConsentOrderFileName),
            anyString());
    }

    @Test
    public void shouldGenerateApprovedConsentOrderLetterForContested() throws IOException {
        FinremCaseDetails contestedDetails =
            finremCaseDetailsFromResource(getResource("/fixtures/contested/consent-in-contested-application-approved.json"), mapper);
        Document caseDocument = consentOrderApprovedDocumentService.generateApprovedConsentOrderLetter(contestedDetails, AUTH_TOKEN);

        assertCaseDocument(caseDocument);
        verify(documentClientMock, atLeastOnce()).generatePdf(
            matchDocumentGenerationRequestTemplateAndFilename(documentApprovedConsentOrderTemplate, documentApprovedConsentOrderFileName),
            anyString());
    }

    @DirtiesContext(methodMode = DirtiesContext.MethodMode.AFTER_METHOD)
    @Test
    public void shouldGenerateApprovedConsentOrderCoverLetterForApplicant() {
        caseDetails.getCaseData().getContactDetailsWrapper().setApplicantRepresented(YesOrNo.NO);

        Document generatedApprovedConsentOrderNotificationLetter =
            consentOrderApprovedDocumentService.generateApprovedConsentOrderCoverLetter(caseDetails, AUTH_TOKEN);

        assertCaseDocument(generatedApprovedConsentOrderNotificationLetter, CONSENT_ORDER_APPROVED_COVER_LETTER_URL);
    }

    @Test
    public void shouldGenerateApprovedConsentOrderCoverLetterForApplicantSolicitor() {
        FinremCaseData caseData = caseDetails.getCaseData();
        caseData.setCcdCaseType(CaseType.CONSENTED);
        caseData.getContactDetailsWrapper().setApplicantRepresented(YesOrNo.YES);
        caseData.getContactDetailsWrapper().setSolicitorName(TEST_SOLICITOR_NAME);
        caseData.getContactDetailsWrapper().setSolicitorReference(TEST_SOLICITOR_REFERENCE);
        Address solicitorAddress = Address.builder()
            .addressLine1("123 Applicant Solicitor Street")
            .addressLine2("Second Address Line")
            .addressLine3("Third Address Line")
            .county("London")
            .country("England")
            .postTown("London")
            .postCode("SE1")
            .build();
        caseData.getContactDetailsWrapper().setSolicitorAddress(solicitorAddress);

        Document generatedApprovedConsentOrderNotificationLetter =
            consentOrderApprovedDocumentService.generateApprovedConsentOrderCoverLetter(caseDetails, AUTH_TOKEN);

        assertCaseDocument(generatedApprovedConsentOrderNotificationLetter, CONSENT_ORDER_APPROVED_COVER_LETTER_URL);
    }

    @Test
    public void shouldStampPensionDocuments() {
        Mockito.reset(documentClientMock);
        when(documentClientMock.stampDocument(any(), anyString())).thenReturn(newDocumentClientDocument());

        List<PensionTypeCollection> pensionDocuments = asList(pensionTypeCollection(), pensionTypeCollection());
        List<PensionTypeCollection> stampPensionDocuments = consentOrderApprovedDocumentService.stampPensionDocuments(pensionDocuments, AUTH_TOKEN);

        stampPensionDocuments.forEach(data -> assertCaseDocument(data.getValue().getUploadedDocument()));
        verify(documentClientMock, times(2)).stampDocument(any(), anyString());
    }

    @Test
    public void givenNullDocumentInPensionDocuments_whenStampingDocuments_thenTheNullValueIsIgnored() {
        Mockito.reset(documentClientMock);
        when(documentClientMock.stampDocument(any(), anyString())).thenReturn(newDocumentClientDocument());

        PensionTypeCollection pensionCollectionDataWithNullDocument = pensionTypeCollection();
        pensionCollectionDataWithNullDocument.getValue().setUploadedDocument(null);
        List<PensionTypeCollection> pensionDocuments = asList(pensionTypeCollection(), pensionCollectionDataWithNullDocument);

        List<PensionTypeCollection> stampPensionDocuments = consentOrderApprovedDocumentService.stampPensionDocuments(pensionDocuments, AUTH_TOKEN);

        assertThat(stampPensionDocuments, hasSize(1));
        stampPensionDocuments.forEach(data -> assertCaseDocument(data.getValue().getUploadedDocument()));
        verify(documentClientMock, times(1)).stampDocument(any(), anyString());
    }

    @Test
    public void whenPreparingApplicantLetterPack() {
        FinremCaseDetails caseDetailsTemp = documentHelper.deepCopy(caseDetails, FinremCaseDetails.class);
        addConsentOrderApprovedDataToCaseDetails(caseDetailsTemp);

        List<BulkPrintDocument> documents = consentOrderApprovedDocumentService.prepareApplicantLetterPack(caseDetailsTemp, AUTH_TOKEN);

        assertThat(documents, hasSize(3));
        assertThat(documents.get(0).getBinaryFileUrl(), is(ORDER_LETTER_URL));
        assertThat(documents.get(1).getBinaryFileUrl(), is(CONSENT_ORDER_URL));
        assertThat(documents.get(2).getBinaryFileUrl(), is(PENSION_DOCUMENT_URL));
    }

    @Test
    public void whenPreparingApplicantLetterPack_paperApplication() {
        FinremCaseDetails caseDetailsTemp = documentHelper.deepCopy(caseDetails, FinremCaseDetails.class);
        caseDetailsTemp.getCaseData().setPaperApplication(YesOrNo.YES);
        caseDetailsTemp.getCaseData().setCcdCaseType(CaseType.CONSENTED);
        addConsentOrderApprovedDataToCaseDetails(caseDetailsTemp);

        List<BulkPrintDocument> documents = consentOrderApprovedDocumentService.prepareApplicantLetterPack(caseDetailsTemp, AUTH_TOKEN);

        assertThat(documents, hasSize(4));
        assertThat(documents.get(0).getBinaryFileUrl(), is(CONSENT_ORDER_APPROVED_COVER_LETTER_URL));
        assertThat(documents.get(1).getBinaryFileUrl(), is(ORDER_LETTER_URL));
        assertThat(documents.get(2).getBinaryFileUrl(), is(CONSENT_ORDER_URL));
        assertThat(documents.get(3).getBinaryFileUrl(), is(PENSION_DOCUMENT_URL));
    }

    @Test
    public void stampsAndPopulatesCaseDataForContestedConsentOrder() {
        when(documentClientMock.stampDocument(any(), anyString())).thenReturn(newDocumentClientDocument());
        when(documentClientMock.annexStampDocument(any(), anyString())).thenReturn(newDocumentClientDocument());

        FinremCaseDetails caseDetails = defaultConsentedFinremCaseDetails();
        FinremCaseData caseData = caseDetails.getCaseData();
        caseData.setConsentOrder(newDocument());

        consentOrderApprovedDocumentService.stampAndPopulateContestedConsentApprovedOrderCollection(caseData, AUTH_TOKEN);
        assertThat(caseData.getConsentOrderWrapper().getContestedConsentedApprovedOrders(), hasSize(1));

        consentOrderApprovedDocumentService.stampAndPopulateContestedConsentApprovedOrderCollection(caseData, AUTH_TOKEN);
        assertThat(caseData.getConsentOrderWrapper().getContestedConsentedApprovedOrders(), hasSize(2));
    }

    @Test
    public void shouldConvertCollectionDocument() throws IOException {
        List<Document> documents = consentOrderApprovedDocumentService.approvedOrderCollection(caseDetails());

        assertThat(documents, hasSize(3));
    }

    private void addConsentOrderApprovedDataToCaseDetails(FinremCaseDetails caseDetails) {
        PensionTypeCollection pensionData = pensionTypeCollection();
        pensionData.getValue().getUploadedDocument().setBinaryUrl(PENSION_DOCUMENT_URL);

        Document consentOrderDoc = Document.builder()
            .url(CONSENT_ORDER_URL)
            .binaryUrl(CONSENT_ORDER_URL)
            .filename(CONSENT_ORDER_URL)
            .build();

        Document orderLetterDoc = Document.builder()
            .url(ORDER_LETTER_URL)
            .binaryUrl(ORDER_LETTER_URL)
            .filename(ORDER_LETTER_URL)
            .build();

        ConsentOrderCollection approvedOrder = ConsentOrderCollection
            .builder()
            .value(ConsentOrder.builder()
                .consentOrder(consentOrderDoc)
                .pensionDocuments(singletonList(pensionData))
                .orderLetter(orderLetterDoc)
                .build())
            .build();

        caseDetails.getCaseData().setApprovedOrderCollection(singletonList(approvedOrder));
    }

    private FinremCaseDetails caseDetails() throws IOException {
        return finremCaseDetailsFromResource(getResource("/fixtures/bulkprint/bulk-print.json"), mapper);
    }
}
