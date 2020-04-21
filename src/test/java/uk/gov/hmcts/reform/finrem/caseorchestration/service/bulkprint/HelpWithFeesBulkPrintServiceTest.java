package uk.gov.hmcts.reform.finrem.caseorchestration.service.bulkprint;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.TestPropertySource;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;
import uk.gov.hmcts.reform.ccd.client.model.CallbackRequest;
import uk.gov.hmcts.reform.ccd.client.model.CaseDetails;
import uk.gov.hmcts.reform.finrem.caseorchestration.CaseOrchestrationApplication;
import uk.gov.hmcts.reform.finrem.caseorchestration.client.DocumentClient;
import uk.gov.hmcts.reform.finrem.caseorchestration.config.DocumentConfiguration;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.CaseDocument;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.document.Addressee;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.document.BulkPrintRequest;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.document.Document;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.document.DocumentGenerationRequest;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.document.DocumentValidationResponse;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.document.HelpWithFeesSuccessLetter;

import java.io.InputStream;
import java.util.UUID;

import static org.hamcrest.Matchers.is;
import static org.junit.Assert.assertThat;
import static uk.gov.hmcts.reform.finrem.caseorchestration.SetUpUtils.document;
import static uk.gov.hmcts.reform.finrem.caseorchestration.TestConstants.AUTH_TOKEN;
import static uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.CCDConfigConstant.HWF_SUCCESS_NOTIFICATION_LETTER;

@RunWith(SpringJUnit4ClassRunner.class)
@ContextConfiguration(classes = CaseOrchestrationApplication.class)
@TestPropertySource(locations = "/application.properties")
public class HelpWithFeesBulkPrintServiceTest {

    private final ObjectMapper mapper = new ObjectMapper();
    private HelpWithFeesBulkPrintService helpWithFeesBulkPrintService;

    @Before
    public void setUp() {
        DocumentConfiguration config = new DocumentConfiguration();
        config.setHelpWithFeesSuccessfulTemplate("FL-FRM-DEC-ENG-00096.docx");
        config.setHelpWithFeesSuccessfulFileName("HelpWithFeesSuccessfulLetter.pdf");

        DocumentClient generatorClient = new TestDocumentClient();
        helpWithFeesBulkPrintService = new HelpWithFeesBulkPrintService(generatorClient, config, mapper);
    }

    @Test
    public void shouldGenerateHwfSuccessfulPdf() throws Exception {
        CaseDocument caseDocument = helpWithFeesBulkPrintService.generateHwfSuccessfulLetter(AUTH_TOKEN, caseDetails());

        assertThat(document().getBinaryUrl(), is(caseDocument.getDocumentBinaryUrl()));
        assertThat(document().getFileName(), is(caseDocument.getDocumentFilename()));
        assertThat(document().getUrl(), is(caseDocument.getDocumentUrl()));
    }

    @Test
    public void shouldGenerateHwfSuccessfulLetterUsingApplicantAddressIfApplicantIsNotRepresented() throws Exception {
        CaseDetails caseDetails = caseDetails();
        helpWithFeesBulkPrintService.generateHwfSuccessfulLetter(AUTH_TOKEN, caseDetails);

        HelpWithFeesSuccessLetter helpWithFeesSuccessLetter
            = (HelpWithFeesSuccessLetter) caseDetails.getData().get(HWF_SUCCESS_NOTIFICATION_LETTER);

        Addressee testApplicantAddressee = Addressee.builder()
            .name("John Doe")
            .formattedAddress("1 Victoria Street"
                + "\nWestminster"
                + "\nGreater London"
                + "\nUK"
                + "\nLondon"
                + "\nSE1")
            .build();

        assertThat(helpWithFeesSuccessLetter.getCaseNumber(), is("1234567890"));
        assertThat(helpWithFeesSuccessLetter.getReference(), is(""));
        assertThat(helpWithFeesSuccessLetter.getAddressee(), is(testApplicantAddressee));
        assertThat(helpWithFeesSuccessLetter.getLetterDate(), is("2020-04-21"));
        assertThat(helpWithFeesSuccessLetter.getApplicantName(), is("John Doe"));
        assertThat(helpWithFeesSuccessLetter.getRespondentName(), is("Jane Doe"));
    }

    @Test
    public void shouldGenerateHwfSuccessfulLetterUsingApplicantSolicitorAddressIfApplicantIsRepresented() throws Exception {
        CaseDetails caseDetails = caseDetailsWithSolicitors();
        helpWithFeesBulkPrintService.generateHwfSuccessfulLetter(AUTH_TOKEN, caseDetails);

        HelpWithFeesSuccessLetter helpWithFeesSuccessLetter =
            (HelpWithFeesSuccessLetter) caseDetails.getData().get(HWF_SUCCESS_NOTIFICATION_LETTER);

        Addressee testSolicitorAddressee = Addressee.builder()
            .name("Mr J Solicitor")
            .formattedAddress("123 Applicant Solicitor Street"
                + "\nSecond Address Line"
                + "\nThird Address Line"
                + "\nLondon"
                + "\nUK"
                + "\nLondon"
                + "\nSE1")
            .build();

        assertThat(helpWithFeesSuccessLetter.getCaseNumber(), is("1234567890"));
        assertThat(helpWithFeesSuccessLetter.getReference(), is("RG-123456789"));
        assertThat(helpWithFeesSuccessLetter.getAddressee(), is(testSolicitorAddressee));
        assertThat(helpWithFeesSuccessLetter.getLetterDate(), is("2020-04-21"));
        assertThat(helpWithFeesSuccessLetter.getApplicantName(), is("John Doe"));
        assertThat(helpWithFeesSuccessLetter.getRespondentName(), is("Jane Doe"));
    }

    private CaseDetails caseDetails() throws Exception {
        try (InputStream resourceAsStream =
                 getClass().getResourceAsStream("/fixtures/bulkprint/bulk-print.json")) {
            return mapper.readValue(resourceAsStream, CallbackRequest.class).getCaseDetails();
        }
    }

    private CaseDetails caseDetailsWithSolicitors() throws Exception {
        try (InputStream resourceAsStream =
                 getClass().getResourceAsStream("/fixtures/bulkprint/bulk-print-with-solicitors.json")) {
            return mapper.readValue(resourceAsStream, CallbackRequest.class).getCaseDetails();
        }
    }

    private static class TestDocumentClient implements DocumentClient {

        @Override
        public Document generatePdf(DocumentGenerationRequest request, String authorizationToken) {
            assertThat(request.getTemplate(), is("FL-FRM-DEC-ENG-00096.docx"));
            assertThat(request.getFileName(), is("HelpWithFeesSuccessfulLetter.pdf"));
            return document();
        }

        @Override
        public UUID bulkPrint(BulkPrintRequest bulkPrintRequest) {
            throw new UnsupportedOperationException();
        }

        @Override
        public void deleteDocument(String fileUrl, String authorizationToken) {
            throw new UnsupportedOperationException();
        }

        @Override
        public DocumentValidationResponse checkUploadedFileType(String authorizationToken, String fileUrl) {
            throw new UnsupportedOperationException();
        }

        @Override
        public Document stampDocument(Document document, String authorizationToken) {
            throw new UnsupportedOperationException();
        }

        @Override
        public Document annexStampDocument(Document document, String authorizationToken) {
            throw new UnsupportedOperationException();
        }
    }
}
