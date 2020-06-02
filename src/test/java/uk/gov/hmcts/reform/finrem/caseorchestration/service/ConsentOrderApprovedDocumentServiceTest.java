package uk.gov.hmcts.reform.finrem.caseorchestration.service;

import com.google.common.collect.ImmutableMap;
import org.junit.Before;
import org.junit.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.test.annotation.DirtiesContext;
import org.springframework.test.context.ActiveProfiles;
import uk.gov.hmcts.reform.ccd.client.model.CaseDetails;
import uk.gov.hmcts.reform.finrem.caseorchestration.BaseServiceTest;
import uk.gov.hmcts.reform.finrem.caseorchestration.client.DocumentClient;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.CaseDocument;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.PensionCollectionData;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.document.BulkPrintDocument;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.document.Document;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static java.util.Arrays.asList;
import static org.hamcrest.Matchers.hasSize;
import static org.hamcrest.Matchers.is;
import static org.junit.Assert.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
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
import static uk.gov.hmcts.reform.finrem.caseorchestration.TestSetUpUtils.defaultCaseDetails;
import static uk.gov.hmcts.reform.finrem.caseorchestration.TestSetUpUtils.document;
import static uk.gov.hmcts.reform.finrem.caseorchestration.TestSetUpUtils.matchDocumentGenerationRequestTemplateAndFilename;
import static uk.gov.hmcts.reform.finrem.caseorchestration.TestSetUpUtils.pensionDocumentData;
import static uk.gov.hmcts.reform.finrem.caseorchestration.helper.DocumentHelper.DOCUMENT_BINARY_URL;
import static uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.CCDConfigConstant.APPLICANT_REPRESENTED;
import static uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.CCDConfigConstant.APPROVED_ORDER_COLLECTION;
import static uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.CCDConfigConstant.APP_SOLICITOR_ADDRESS_CCD_FIELD;
import static uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.CCDConfigConstant.SOLICITOR_NAME;
import static uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.CCDConfigConstant.SOLICITOR_REFERENCE;
import static uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.CCDConfigConstant.VALUE;

@ActiveProfiles("test-mock-document-client")
public class ConsentOrderApprovedDocumentServiceTest extends BaseServiceTest {

    private static final String DEFAULT_COVERSHEET_URL = "defaultCoversheetUrl";
    private static final String CONSENT_ORDER_APPROVED_COVER_LETTER_URL = "consentOrderApprovedCoverLetterUrl";

    @Autowired
    private ConsentOrderApprovedDocumentService consentOrderApprovedDocumentService;

    @MockBean
    private FeatureToggleService featureToggleService;

    @Autowired
    private DocumentClient documentClientMock;

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
    public void shouldGenerateApprovedConsentOrderLetter() {
        CaseDocument caseDocument = consentOrderApprovedDocumentService.generateApprovedConsentOrderLetter(caseDetails, AUTH_TOKEN);

        assertCaseDocument(caseDocument);
        verify(documentClientMock, times(1)).generatePdf(
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
        caseData.put(SOLICITOR_NAME, TEST_SOLICITOR_NAME);
        caseData.put(SOLICITOR_REFERENCE, TEST_SOLICITOR_REFERENCE);
        caseData.put(APP_SOLICITOR_ADDRESS_CCD_FIELD, solicitorAddress);

        CaseDocument generatedApprovedConsentOrderNotificationLetter =
                consentOrderApprovedDocumentService.generateApprovedConsentOrderCoverLetter(caseDetails, AUTH_TOKEN);

        assertCaseDocument(generatedApprovedConsentOrderNotificationLetter, CONSENT_ORDER_APPROVED_COVER_LETTER_URL);
    }

    @Test
    public void shouldStampPensionDocuments() {
        when(documentClientMock.stampDocument(any(), anyString())).thenReturn(document());

        List<PensionCollectionData> pensionDocuments = asList(pensionDocumentData(), pensionDocumentData());
        List<PensionCollectionData> stampPensionDocuments = consentOrderApprovedDocumentService.stampPensionDocuments(pensionDocuments, AUTH_TOKEN);

        stampPensionDocuments.forEach(data -> assertCaseDocument(data.getTypedCaseDocument().getPensionDocument()));
        verify(documentClientMock, times(2)).stampDocument(any(), anyString());
    }

    @Test
    public void givenCoverLetterFeatureTurnedOff_whenPreparingApplicantLetterPack_thenCoversheetAndOrderIsPresent() {
        when(featureToggleService.isApprovedConsentOrderNotificationLetterEnabled()).thenReturn(false);
        preparePaperCaseConsentOrderApprovedCaseData();

        List<BulkPrintDocument> documents = consentOrderApprovedDocumentService.prepareApplicantLetterPack(caseDetails, AUTH_TOKEN);

        assertThat(documents, hasSize(4));
        assertThat(documents.get(0).getBinaryFileUrl(), is(DEFAULT_COVERSHEET_URL));
        assertThat(documents.get(1).getBinaryFileUrl(), is(ORDER_LETTER_URL));
        assertThat(documents.get(2).getBinaryFileUrl(), is(CONSENT_ORDER_URL));
        assertThat(documents.get(3).getBinaryFileUrl(), is(PENSION_DOCUMENT_URL));
    }

    @Test
    public void givenCoverLetterFeatureTurnedOn_whenPreparingApplicantLetterPack_thenCoversheetAndOrderIsPresent() {
        when(featureToggleService.isApprovedConsentOrderNotificationLetterEnabled()).thenReturn(true);
        preparePaperCaseConsentOrderApprovedCaseData();

        List<BulkPrintDocument> documents = consentOrderApprovedDocumentService.prepareApplicantLetterPack(caseDetails, AUTH_TOKEN);

        System.out.println(documents);
        assertThat(documents, hasSize(5));
        assertThat(documents.get(0).getBinaryFileUrl(), is(DEFAULT_COVERSHEET_URL));
        assertThat(documents.get(1).getBinaryFileUrl(), is(CONSENT_ORDER_APPROVED_COVER_LETTER_URL));
        assertThat(documents.get(2).getBinaryFileUrl(), is(ORDER_LETTER_URL));
        assertThat(documents.get(3).getBinaryFileUrl(), is(CONSENT_ORDER_URL));
        assertThat(documents.get(4).getBinaryFileUrl(), is(PENSION_DOCUMENT_URL));
    }

    private void preparePaperCaseConsentOrderApprovedCaseData() {
        Map<String, Object> caseData = caseDetails.getData();
        caseData.put(APPROVED_ORDER_COLLECTION, asList(ImmutableMap.of(VALUE, ImmutableMap.of(
            "orderLetter", ImmutableMap.of(DOCUMENT_BINARY_URL, ORDER_LETTER_URL),
            "consentOrder", ImmutableMap.of(DOCUMENT_BINARY_URL, CONSENT_ORDER_URL),
            "pensionDocuments", asList(ImmutableMap.of(
                VALUE, ImmutableMap.of("uploadedDocument", ImmutableMap.of(
                    DOCUMENT_BINARY_URL, PENSION_DOCUMENT_URL))))))));
        caseData.put(PAPER_APPLICATION, YES_VALUE);
    }
}
