package uk.gov.hmcts.reform.finrem.caseorchestration.service.bulkprint;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.Before;
import org.junit.Test;
import uk.gov.hmcts.reform.ccd.client.model.CallbackRequest;
import uk.gov.hmcts.reform.ccd.client.model.CaseDetails;
import uk.gov.hmcts.reform.finrem.caseorchestration.client.DocumentClient;
import uk.gov.hmcts.reform.finrem.caseorchestration.config.DocumentConfiguration;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.CaseDocument;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.document.Addressee;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.document.AssignedToJudgeLetter;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.document.BulkPrintRequest;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.document.CtscContactDetails;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.document.Document;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.document.DocumentGenerationRequest;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.document.DocumentValidationResponse;

import java.io.InputStream;
import java.util.UUID;

import static org.hamcrest.Matchers.is;
import static org.junit.Assert.assertThat;
import static uk.gov.hmcts.reform.finrem.caseorchestration.SetUpUtils.document;
import static uk.gov.hmcts.reform.finrem.caseorchestration.TestConstants.AUTH_TOKEN;
import static uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.CCDConfigConstant.ASSIGNED_TO_JUDGE_NOTIFICATION_LETTER;

public class AssignedToJudgeBulkPrintServiceTest {

    private final ObjectMapper mapper = new ObjectMapper();
    private AssignedToJudgeBulkPrintService assignedToJudgeBulkPrintService;
    private CtscContactDetails ctscContactDetails;

    @Before
    public void setUp() {
        DocumentConfiguration config = new DocumentConfiguration();
        config.setApplicationAssignedToJudgeFileName("ApplicationHasBeenAssignedToJudge.pdf");
        config.setApplicationAssignedToJudgeTemplate("FL-FRM-LET-ENG-00318.docx");

        DocumentClient generatorClient = new TestDocumentClient();
        assignedToJudgeBulkPrintService = new AssignedToJudgeBulkPrintService(generatorClient, config, mapper);

        ctscContactDetails = CtscContactDetails.builder()
            .serviceCentre("Courts and Tribunals Service Centre")
            .careOf("c/o HMCTS Digital Financial Remedy")
            .poBox("12746")
            .town("HARLOW")
            .postcode("CM20 9QZ")
            .emailAddress("HMCTSFinancialRemedy@justice.gov.uk")
            .phoneNumber("0300 303 0642")
            .openingHours("from 8.30am to 5pm")
            .build();
    }

    @Test
    public void shouldGenerateApplicationHasBeenAssignedToJudgePdf() throws Exception {
        CaseDocument caseDocument = assignedToJudgeBulkPrintService.generateJudgeAssignedToCaseLetter(AUTH_TOKEN, caseDetails());

        assertThat(document().getBinaryUrl(), is(caseDocument.getDocumentBinaryUrl()));
        assertThat(document().getFileName(), is(caseDocument.getDocumentFilename()));
        assertThat(document().getUrl(), is(caseDocument.getDocumentUrl()));
    }

    @Test
    public void shouldGenerateAssignedToJudgeLetterUsingApplicantAddressIfApplicantIsNotRepresented() throws Exception {
        CaseDetails caseDetails = caseDetails();
        assignedToJudgeBulkPrintService.generateJudgeAssignedToCaseLetter(AUTH_TOKEN, caseDetails);

        AssignedToJudgeLetter assignedToJudgeLetter = (AssignedToJudgeLetter) caseDetails.getData().get(ASSIGNED_TO_JUDGE_NOTIFICATION_LETTER);

        Addressee testApplicantAddressee = Addressee.builder()
            .name("John Doe")
            .formattedAddress("1 Victoria Street"
                + "\nWestminster"
                + "\nGreater London"
                + "\nUK"
                + "\nLondon"
                + "\nSE1")
            .build();

        assertThat(assignedToJudgeLetter.getCaseNumber(), is("1234567890"));
        assertThat(assignedToJudgeLetter.getReference(), is(""));
        assertThat(assignedToJudgeLetter.getAddressee(), is(testApplicantAddressee));
        assertThat(assignedToJudgeLetter.getLetterDate(), is("2020-04-21"));
        assertThat(assignedToJudgeLetter.getApplicantName(), is("John Doe"));
        assertThat(assignedToJudgeLetter.getRespondentName(), is("Jane Doe"));
        assertThat(assignedToJudgeLetter.getCtscContactDetails(), is(ctscContactDetails));
    }

    @Test
    public void shouldGenerateAssignedToJudgeLetterUsingApplicantSolicitorAddressIfApplicantIsRepresented() throws Exception {
        CaseDetails caseDetails = caseDetailsWithSolicitors();
        assignedToJudgeBulkPrintService.generateJudgeAssignedToCaseLetter(AUTH_TOKEN, caseDetails);

        AssignedToJudgeLetter assignedToJudgeLetter =
            (AssignedToJudgeLetter) caseDetails.getData().get(ASSIGNED_TO_JUDGE_NOTIFICATION_LETTER);

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

        assertThat(assignedToJudgeLetter.getCaseNumber(), is("1234567890"));
        assertThat(assignedToJudgeLetter.getReference(), is("RG-123456789"));
        assertThat(assignedToJudgeLetter.getAddressee(), is(testSolicitorAddressee));
        assertThat(assignedToJudgeLetter.getLetterDate(), is("2020-04-21"));
        assertThat(assignedToJudgeLetter.getApplicantName(), is("John Doe"));
        assertThat(assignedToJudgeLetter.getRespondentName(), is("Jane Doe"));
        assertThat(assignedToJudgeLetter.getCtscContactDetails(), is(ctscContactDetails));
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
            assertThat(request.getTemplate(), is("FL-FRM-LET-ENG-00318.docx"));
            assertThat(request.getFileName(), is("ApplicationHasBeenAssignedToJudge.pdf"));
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

