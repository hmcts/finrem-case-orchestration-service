
package uk.gov.hmcts.reform.finrem.caseorchestration.service;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.Before;
import org.junit.Test;
import org.mockito.Mock;
import uk.gov.hmcts.reform.ccd.client.model.CaseDetails;
import uk.gov.hmcts.reform.finrem.caseorchestration.client.DocumentClient;
import uk.gov.hmcts.reform.finrem.caseorchestration.config.DocumentConfiguration;
import uk.gov.hmcts.reform.finrem.caseorchestration.helper.LetterAddressHelper;
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
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static uk.gov.hmcts.reform.finrem.caseorchestration.OrchestrationConstants.NO_VALUE;
import static uk.gov.hmcts.reform.finrem.caseorchestration.SetUpUtils.caseDocument;
import static uk.gov.hmcts.reform.finrem.caseorchestration.SetUpUtils.doCaseDocumentAssert;
import static uk.gov.hmcts.reform.finrem.caseorchestration.SetUpUtils.document;
import static uk.gov.hmcts.reform.finrem.caseorchestration.SetUpUtils.pensionDocumentData;
import static uk.gov.hmcts.reform.finrem.caseorchestration.TestConstants.AUTH_TOKEN;
import static uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.CCDConfigConstant.APPLICANT_REPRESENTED;
import static uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.CCDConfigConstant.CONSENT_ORDER_APPROVED_NOTIFICATION_LETTER;

public class ConsentOrderApprovedDocumentServiceTest {

    @Mock
    private DocumentClient documentClientMock;

    private ObjectMapper mapper = new ObjectMapper();
    private ConsentOrderApprovedDocumentService service;
    private LetterAddressHelper letterAddressHelper = new LetterAddressHelper();
    private CaseDetails caseDetails;

    @Before
    public void setUp() {
        DocumentConfiguration config = new DocumentConfiguration();
        config.setApprovedConsentOrderTemplate("FL-FRM-DEC-ENG-00071.docx");
        config.setApprovedConsentOrderFileName("ApprovedConsentOrderLetter.pdf");
        config.setApprovedConsentOrderNotificationTemplate("FL-FRM-LET-ENG-00095.docx");
        config.setApprovedConsentOrderNotificationFileName("ApprovedConsentOrderNotificationLetter.pdf");
        documentClientMock = mock(DocumentClient.class);
        service = new ConsentOrderApprovedDocumentService(documentClientMock, config, mapper, letterAddressHelper);

        Map<String, Object> applicantAddress = new HashMap<>();
        applicantAddress.put("AddressLine1", "50 Applicant Street");
        applicantAddress.put("AddressLine2", "Second Address Line");
        applicantAddress.put("AddressLine3", "Third Address Line");
        applicantAddress.put("County", "London");
        applicantAddress.put("Country", "England");
        applicantAddress.put("PostTown", "London");
        applicantAddress.put("PostCode", "SW1");

        Map<String, Object> caseData = new HashMap<>();
        /*
        caseData.put(APPLICANT_FIRST_MIDDLE_NAME, "James");
        caseData.put(APPLICANT_LAST_NAME, "Joyce");
        caseData.put(APPLICANT_ADDRESS, applicantAddress);
        caseData.put(APPLICANT_REPRESENTED, null);
        caseData.put(APP_RESPONDENT_FIRST_MIDDLE_NAME, "Jane");
        caseData.put(APP_RESPONDENT_LAST_NAME, "Doe");
        */

        caseDetails = CaseDetails.builder()
                .id(123456789L)
                .data(caseData)
                .build();
    }

    @Test
    public void shouldGenerateApprovedConsentOrderLetter() {
        when(documentClientMock.generatePdf(any(), anyString())).thenReturn(document());

        CaseDocument caseDocument = service.generateApprovedConsentOrderLetter(caseDetails, AUTH_TOKEN);

        doCaseDocumentAssert(caseDocument);
        verify(documentClientMock, times(1)).generatePdf(any(), anyString());
    }

    @Test
    public void shouldGenerateApprovedConsentOrderNotificationLetterForApplicant() {
        when(documentClientMock.generatePdf(any(), anyString())).thenReturn(document());

        caseDetails.getData().put(APPLICANT_REPRESENTED, NO_VALUE);

        CaseDocument generatedApprovedConsentOrderNotificationLetter =
                service.generateApprovedConsentOrderNotificationLetter(caseDetails, AUTH_TOKEN);

        doCaseDocumentAssert(generatedApprovedConsentOrderNotificationLetter);

        Map<String, Object> caseData = caseDetails.getData();


        String addresseeName = "test name pls";
        String addressToSendTo = "test address";

        Addressee addressee = Addressee.builder()
                .name(addresseeName)
                .formattedAddress(addressToSendTo)
                .build();

        assertEquals(caseData.get(CONSENT_ORDER_APPROVED_NOTIFICATION_LETTER), addressee);
    }

    /*
    @Test
    public void shouldGenerateApprovedConsentOrderNotificationLetterForApplicant() {
        when(documentClientMock.generatePdf(any(), anyString())).thenReturn(document());

        caseDetails.getData().put(APPLICANT_REPRESENTED, NO_VALUE);

        CaseDocument generatedApprovedConsentOrderNotificationLetter =
                service.generateApprovedConsentOrderNotificationLetter(caseDetails, AUTH_TOKEN);

        doCaseDocumentAssert(generatedApprovedConsentOrderNotificationLetter);

        ConsentOrderApprovedNotificationLetter consentOrderApprovedNotificationLetter
                = (ConsentOrderApprovedNotificationLetter) caseDetails.getData().get(CONSENT_ORDER_APPROVED_NOTIFICATION_LETTER);

        assertThat(consentOrderApprovedNotificationLetter.getAddressee().getName(), is("James Joyce"));
        assertThat(consentOrderApprovedNotificationLetter.getReference(), is(""));
        assertThat(consentOrderApprovedNotificationLetter.getApplicantName(), is("James Joyce"));
        assertThat(consentOrderApprovedNotificationLetter.getRespondentName(), is("Jane Doe"));
        assertThat(consentOrderApprovedNotificationLetter.getLetterDate(), is(String.valueOf(LocalDate.now())));
        assertThat(consentOrderApprovedNotificationLetter.getAddressee().getFormattedAddress(), is(
                "50 Applicant Street\nSecond Address Line\nThird Address Line\nLondon\nEngland\nLondon\nSW1"));
        assertThat(consentOrderApprovedNotificationLetter.getCaseNumber(), is("123456789"));
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
                service.generateApprovedConsentOrderNotificationLetter(caseDetails, AUTH_TOKEN);

        doCaseDocumentAssert(generatedApprovedConsentOrderNotificationLetter);
        verify(documentClientMock, times(1)).generatePdf(any(), anyString());

        ConsentOrderApprovedNotificationLetter consentOrderApprovedNotificationLetter
                = (ConsentOrderApprovedNotificationLetter) caseDetails.getData().get(CONSENT_ORDER_APPROVED_NOTIFICATION_LETTER);

        assertThat(consentOrderApprovedNotificationLetter.getAddressee().getName(), is("Saul Goodman"));
        assertThat(consentOrderApprovedNotificationLetter.getReference(), is("RG-123456789"));
        assertThat(consentOrderApprovedNotificationLetter.getApplicantName(), is("James Joyce"));
        assertThat(consentOrderApprovedNotificationLetter.getRespondentName(), is("Jane Doe"));
        assertThat(consentOrderApprovedNotificationLetter.getLetterDate(), is(String.valueOf(LocalDate.now())));
        assertThat(consentOrderApprovedNotificationLetter.getAddressee().getFormattedAddress(), is(
                "123 Applicant Solicitor Street\nSecond Address Line\nThird Address Line\nLondon\nEngland\nLondon\nSE1"));
        assertThat(consentOrderApprovedNotificationLetter.getCaseNumber(), is("123456789"));
    }
 */

    @Test
    public void shouldAnnexAndStampDocument() {
        CaseDocument caseDocument = caseDocument();

        when(documentClientMock.annexStampDocument(any(), anyString())).thenReturn(document());

        CaseDocument annexStampDocument = service.annexStampDocument(caseDocument, AUTH_TOKEN);

        doCaseDocumentAssert(annexStampDocument);
        verify(documentClientMock, times(1)).annexStampDocument(any(), anyString());
    }

    @Test
    public void shouldStampDocument() {
        CaseDocument caseDocument = caseDocument();

        when(documentClientMock.stampDocument(any(), anyString())).thenReturn(document());

        CaseDocument stampDocument = service.stampDocument(caseDocument, AUTH_TOKEN);

        doCaseDocumentAssert(stampDocument);
        verify(documentClientMock, times(1)).stampDocument(any(), anyString());
    }

    @Test
    public void shouldStampPensionDocuments() {
        List<PensionCollectionData> pensionDocuments = asList(pensionDocumentData(), pensionDocumentData());

        when(documentClientMock.stampDocument(any(), anyString())).thenReturn(document());

        List<PensionCollectionData> stampPensionDocuments = service.stampPensionDocuments(pensionDocuments, AUTH_TOKEN);

        stampPensionDocuments.forEach(data -> doCaseDocumentAssert(data.getPensionDocumentData().getPensionDocument()));
        verify(documentClientMock, times(2)).stampDocument(any(), anyString());
    }
}
