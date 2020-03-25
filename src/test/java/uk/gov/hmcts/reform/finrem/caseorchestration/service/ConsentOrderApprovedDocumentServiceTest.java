
package uk.gov.hmcts.reform.finrem.caseorchestration.service;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.google.common.collect.ImmutableMap;
import org.junit.Before;
import org.junit.Test;
import org.mockito.Mock;
import uk.gov.hmcts.reform.ccd.client.model.CallbackRequest;
import uk.gov.hmcts.reform.ccd.client.model.CaseDetails;
import uk.gov.hmcts.reform.finrem.caseorchestration.client.DocumentClient;
import uk.gov.hmcts.reform.finrem.caseorchestration.config.DocumentConfiguration;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.CaseDocument;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.PensionCollectionData;

import java.io.InputStream;
import java.util.List;
import java.util.Map;

import static java.util.Arrays.asList;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static uk.gov.hmcts.reform.finrem.caseorchestration.SetUpUtils.caseDocument;
import static uk.gov.hmcts.reform.finrem.caseorchestration.SetUpUtils.doCaseDocumentAssert;
import static uk.gov.hmcts.reform.finrem.caseorchestration.SetUpUtils.document;
import static uk.gov.hmcts.reform.finrem.caseorchestration.SetUpUtils.pensionDocumentData;
import static uk.gov.hmcts.reform.finrem.caseorchestration.TestConstants.AUTH_TOKEN;

public class ConsentOrderApprovedDocumentServiceTest {

    @Mock
    private DocumentClient documentClientMock;

    private DocumentConfiguration config;

    private ObjectMapper mapper = new ObjectMapper();

    private ConsentOrderApprovedDocumentService service;

    @Before
    public void setUp() {
        config = new DocumentConfiguration();
        config.setApprovedConsentOrderTemplate("FL-FRM-DEC-ENG-00071.docx");
        config.setApprovedConsentOrderFileName("ApprovedConsentOrderLetter.pdf");
        config.setApprovedConsentOrderNotificationTemplate("FL-FRM-LET-ENG-00263.docx");
        config.setApprovedConsentOrderNotificationFileName("ApprovedConsentOrderNotificationLetter.pdf");

        documentClientMock = mock(DocumentClient.class);
        service = new ConsentOrderApprovedDocumentService(documentClientMock, config, mapper);
    }

    private CaseDetails caseDetails() throws Exception {
        try (InputStream resourceAsStream =
                     getClass().getResourceAsStream("/fixtures/bulk-print.json")) {
            return mapper.readValue(resourceAsStream, CallbackRequest.class).getCaseDetails();
        }
    }

    @Test
    public void shouldGenerateApprovedConsentOrderLetter() throws Exception {
        Map<String, Object> caseData = caseDetails().getData();

        /*
        String ccdNumber = String.valueOf(caseDetails.getId());
        String recipientReference = "reeeeference";
        String recipientName = "";
        String applicantName = join(nullToEmpty(caseData.get(APP_FIRST_AND_MIDDLE_NAME_CCD_FIELD)), " ",
                nullToEmpty(caseDetails.getData().get(APP_LAST_NAME_CCD_FIELD)));
        String respondentName = join(nullToEmpty(caseData.get(APP_RESP_FIRST_AND_MIDDLE_NAME_CCD_FIELD)), " ",
                nullToEmpty(caseDetails.getData().get(APP_RESP_LAST_NAME_CCD_FIELD)));
        String applicantRepresented = caseData.get(APPLICANT_REPRESENTED).toString();
         */

        when(documentClientMock.generatePdf(any(), anyString())).thenReturn(document());

        CaseDocument generatedApprovedConsentOrderLetter = service.generateApprovedConsentOrderLetter(
                CaseDetails.builder().data(caseData).build(), AUTH_TOKEN);

        doCaseDocumentAssert(generatedApprovedConsentOrderLetter);
        verify(documentClientMock, times(1)).generatePdf(any(), anyString());
    }

    /*
    Add unit tests for both Applicant & Applicant Solicitor
     */
    @Test
    public void shouldGenerateApprovedConsentOrderNotificationLetterForApplicant() throws Exception {
        Map<String, Object> caseData = caseDetails().getData();

        /*
        String ccdNumber = String.valueOf(caseDetails.getId());
        String recipientReference = "reeeeference";
        String recipientName = "";
        String applicantName = join(nullToEmpty(caseData.get(APP_FIRST_AND_MIDDLE_NAME_CCD_FIELD)), " ",
                nullToEmpty(caseDetails.getData().get(APP_LAST_NAME_CCD_FIELD)));
        String respondentName = join(nullToEmpty(caseData.get(APP_RESP_FIRST_AND_MIDDLE_NAME_CCD_FIELD)), " ",
                nullToEmpty(caseDetails.getData().get(APP_RESP_LAST_NAME_CCD_FIELD)));
        String applicantRepresented = caseData.get(APPLICANT_REPRESENTED).toString();
         */

        when(documentClientMock.generatePdf(any(), anyString())).thenReturn(document());

        CaseDocument generatedApprovedConsentOrderNotificationLetter = service.generateApprovedConsentOrderNotificationLetter(
                CaseDetails.builder().data(caseData).build(), AUTH_TOKEN);

        doCaseDocumentAssert(generatedApprovedConsentOrderNotificationLetter);
        verify(documentClientMock, times(1)).generatePdf(any(), anyString());
    }

    @Test
    public void shouldGenerateApprovedConsentOrderNotificationLetterForApplicantSolicitor() throws Exception {
        Map<String, Object> caseData = caseDetails().getData();

        /*
        String ccdNumber = String.valueOf(caseDetails.getId());
        String recipientReference = "reeeeference";
        String recipientName = "";
        String applicantName = join(nullToEmpty(caseData.get(APP_FIRST_AND_MIDDLE_NAME_CCD_FIELD)), " ",
                nullToEmpty(caseDetails.getData().get(APP_LAST_NAME_CCD_FIELD)));
        String respondentName = join(nullToEmpty(caseData.get(APP_RESP_FIRST_AND_MIDDLE_NAME_CCD_FIELD)), " ",
                nullToEmpty(caseDetails.getData().get(APP_RESP_LAST_NAME_CCD_FIELD)));
        String applicantRepresented = caseData.get(APPLICANT_REPRESENTED).toString();
         */

        when(documentClientMock.generatePdf(any(), anyString())).thenReturn(document());

        CaseDocument generatedApprovedConsentOrderNotificationLetter = service.generateApprovedConsentOrderNotificationLetter(
                CaseDetails.builder().data(caseData).build(), AUTH_TOKEN);

        doCaseDocumentAssert(generatedApprovedConsentOrderNotificationLetter);
        verify(documentClientMock, times(1)).generatePdf(any(), anyString());
    }

    @Test
    public void shouldAnnexAndStampDocument() {
        CaseDocument caseDocument = caseDocument();

        when(documentClientMock.annexStampDocument(any(), anyString()))
                .thenReturn(document());

        CaseDocument annexStampDocument = service.annexStampDocument(caseDocument, AUTH_TOKEN);

        doCaseDocumentAssert(annexStampDocument);
        verify(documentClientMock, times(1)).annexStampDocument(any(), anyString());
    }

    @Test
    public void shouldStampDocument() {
        CaseDocument caseDocument = caseDocument();

        when(documentClientMock.stampDocument(any(), anyString()))
                .thenReturn(document());

        CaseDocument stampDocument = service.stampDocument(caseDocument, AUTH_TOKEN);

        doCaseDocumentAssert(stampDocument);
        verify(documentClientMock, times(1)).stampDocument(any(), anyString());
    }

    @Test
    public void shouldStampPensionDocuments() {
        List<PensionCollectionData> pensionDocuments = asList(pensionDocumentData(), pensionDocumentData());

        when(documentClientMock.stampDocument(any(), anyString()))
                .thenReturn(document());

        List<PensionCollectionData> stampPensionDocuments = service.stampPensionDocuments(pensionDocuments, AUTH_TOKEN);

        stampPensionDocuments.forEach(data -> doCaseDocumentAssert(data.getPensionDocumentData().getPensionDocument()));
        verify(documentClientMock, times(2)).stampDocument(any(), anyString());
    }
}
