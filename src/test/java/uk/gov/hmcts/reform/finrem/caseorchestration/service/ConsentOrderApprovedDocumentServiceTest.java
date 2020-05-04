package uk.gov.hmcts.reform.finrem.caseorchestration.service;

import org.junit.Before;
import org.junit.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.test.annotation.DirtiesContext;
import org.springframework.test.context.ActiveProfiles;
import uk.gov.hmcts.reform.ccd.client.model.CaseDetails;
import uk.gov.hmcts.reform.finrem.caseorchestration.BaseServiceTest;
import uk.gov.hmcts.reform.finrem.caseorchestration.client.DocumentClient;
import uk.gov.hmcts.reform.finrem.caseorchestration.config.DocumentConfiguration;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.CaseDocument;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.PensionCollectionData;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.document.Addressee;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static java.util.Arrays.asList;
import static org.junit.Assert.assertEquals;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.reset;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static uk.gov.hmcts.reform.finrem.caseorchestration.OrchestrationConstants.NO_VALUE;
import static uk.gov.hmcts.reform.finrem.caseorchestration.OrchestrationConstants.YES_VALUE;
import static uk.gov.hmcts.reform.finrem.caseorchestration.SetUpUtils.assertCaseDocument;
import static uk.gov.hmcts.reform.finrem.caseorchestration.SetUpUtils.caseDocument;
import static uk.gov.hmcts.reform.finrem.caseorchestration.SetUpUtils.document;
import static uk.gov.hmcts.reform.finrem.caseorchestration.SetUpUtils.pensionDocumentData;
import static uk.gov.hmcts.reform.finrem.caseorchestration.TestConstants.AUTH_TOKEN;
import static uk.gov.hmcts.reform.finrem.caseorchestration.TestConstants.TEST_SOLICITOR_NAME;
import static uk.gov.hmcts.reform.finrem.caseorchestration.TestConstants.TEST_SOLICITOR_REFERENCE;
import static uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.CCDConfigConstant.APPLICANT_ADDRESS;
import static uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.CCDConfigConstant.APPLICANT_FIRST_MIDDLE_NAME;
import static uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.CCDConfigConstant.APPLICANT_LAST_NAME;
import static uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.CCDConfigConstant.APPLICANT_REPRESENTED;
import static uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.CCDConfigConstant.APP_RESPONDENT_FIRST_MIDDLE_NAME;
import static uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.CCDConfigConstant.APP_RESPONDENT_LAST_NAME;
import static uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.CCDConfigConstant.APP_SOLICITOR_ADDRESS_CCD_FIELD;
import static uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.CCDConfigConstant.SOLICITOR_NAME;
import static uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.CCDConfigConstant.SOLICITOR_REFERENCE;

@ActiveProfiles("test-mock-document-client")
public class ConsentOrderApprovedDocumentServiceTest extends BaseServiceTest {

    @Autowired
    private DocumentClient documentClientMock;

    @Autowired
    private ConsentOrderApprovedDocumentService consentOrderApprovedDocumentService;

    private final FeatureToggleService featureToggleService = new FeatureToggleService();
    private CaseDetails caseDetails;

    @Before
    public void setUp() {
        DocumentConfiguration config = new DocumentConfiguration();
        config.setApprovedConsentOrderTemplate("FL-FRM-DEC-ENG-00071.docx");
        config.setApprovedConsentOrderFileName("ApprovedConsentOrderLetter.pdf");
        if (featureToggleService.isApprovedConsentOrderNotificationLetterEnabled()) {
            config.setApprovedConsentOrderNotificationTemplate("FL-FRM-LET-ENG-00095.docx");
            config.setApprovedConsentOrderNotificationFileName("ApprovedConsentOrderNotificationLetter.pdf");
        }

        Map<String, Object> applicantAddress = new HashMap<>();
        applicantAddress.put("AddressLine1", "50 Applicant Street");
        applicantAddress.put("AddressLine2", "Second Address Line");
        applicantAddress.put("AddressLine3", "Third Address Line");
        applicantAddress.put("County", "London");
        applicantAddress.put("Country", "England");
        applicantAddress.put("PostTown", "London");
        applicantAddress.put("PostCode", "SW1");

        Map<String, Object> caseData = new HashMap<>();
        caseData.put(APPLICANT_FIRST_MIDDLE_NAME, "James");
        caseData.put(APPLICANT_LAST_NAME, "Joyce");
        caseData.put(APPLICANT_ADDRESS, applicantAddress);
        caseData.put(APPLICANT_REPRESENTED, null);
        caseData.put(APP_RESPONDENT_FIRST_MIDDLE_NAME, "Jane");
        caseData.put(APP_RESPONDENT_LAST_NAME, "Doe");

        caseDetails = CaseDetails.builder()
                .id(123456789L)
                .data(caseData)
                .build();
    }

    @Test
    public void shouldGenerateApprovedConsentOrderLetter() {
        reset(documentClientMock);
        when(documentClientMock.generatePdf(any(), anyString())).thenReturn(document());

        CaseDocument caseDocument = consentOrderApprovedDocumentService.generateApprovedConsentOrderLetter(caseDetails, AUTH_TOKEN);

        assertCaseDocument(caseDocument);
        verify(documentClientMock, times(1)).generatePdf(any(), anyString());
    }

    @DirtiesContext(methodMode = DirtiesContext.MethodMode.AFTER_METHOD)
    @Test
    public void shouldGenerateApprovedConsentOrderNotificationLetterForApplicant() {
        when(documentClientMock.generatePdf(any(), anyString())).thenReturn(document());

        caseDetails.getData().put(APPLICANT_REPRESENTED, NO_VALUE);

        CaseDocument generatedApprovedConsentOrderNotificationLetter =
                consentOrderApprovedDocumentService.generateApprovedConsentOrderNotificationLetter(caseDetails, AUTH_TOKEN);

        assertCaseDocument(generatedApprovedConsentOrderNotificationLetter);

        Addressee addressee = Addressee.builder()
                .name("James Joyce")
                .formattedAddress("50 Applicant Street\nSecond Address Line\nThird Address Line\nLondon\nEngland\nLondon\nSW1")
                .build();
        assertEquals(addressee, caseDetails.getData().get("addressee"));
    }

    @Test
    public void shouldGenerateApprovedConsentOrderNotificationLetterForApplicantSolicitor() {
        when(documentClientMock.generatePdf(any(), anyString())).thenReturn(document());

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
                consentOrderApprovedDocumentService.generateApprovedConsentOrderNotificationLetter(caseDetails, AUTH_TOKEN);

        assertCaseDocument(generatedApprovedConsentOrderNotificationLetter);

        Addressee addressee = Addressee.builder()
                .name("Saul Goodman")
                .formattedAddress("123 Applicant Solicitor Street\nSecond Address Line\nThird Address Line\nLondon\nEngland\nLondon\nSE1")
                .build();
        assertEquals(addressee, caseDetails.getData().get("addressee"));
    }

    @Test
    public void shouldAnnexAndStampDocument() {
        reset(documentClientMock);
        when(documentClientMock.annexStampDocument(any(), anyString())).thenReturn(document());

        CaseDocument caseDocument = caseDocument();
        CaseDocument annexStampDocument = consentOrderApprovedDocumentService.annexStampDocument(caseDocument, AUTH_TOKEN);

        assertCaseDocument(annexStampDocument);
        verify(documentClientMock, times(1)).annexStampDocument(any(), anyString());
    }

    @Test
    public void shouldStampDocument() {
        reset(documentClientMock);
        when(documentClientMock.stampDocument(any(), anyString())).thenReturn(document());

        CaseDocument caseDocument = caseDocument();
        CaseDocument stampDocument = consentOrderApprovedDocumentService.stampDocument(caseDocument, AUTH_TOKEN);

        assertCaseDocument(stampDocument);
        verify(documentClientMock, times(1)).stampDocument(any(), anyString());
    }

    @Test
    public void shouldStampPensionDocuments() {
        reset(documentClientMock);
        when(documentClientMock.stampDocument(any(), anyString())).thenReturn(document());

        List<PensionCollectionData> pensionDocuments = asList(pensionDocumentData(), pensionDocumentData());
        List<PensionCollectionData> stampPensionDocuments = consentOrderApprovedDocumentService.stampPensionDocuments(pensionDocuments, AUTH_TOKEN);

        stampPensionDocuments.forEach(data -> assertCaseDocument(data.getTypedCaseDocument().getPensionDocument()));
        verify(documentClientMock, times(2)).stampDocument(any(), anyString());
    }
}
