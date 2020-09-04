package uk.gov.hmcts.reform.finrem.caseorchestration.service;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.Before;
import org.junit.Test;
import org.springframework.beans.factory.annotation.Autowired;
import uk.gov.hmcts.reform.ccd.client.model.CallbackRequest;
import uk.gov.hmcts.reform.ccd.client.model.CaseDetails;
import uk.gov.hmcts.reform.finrem.caseorchestration.BaseServiceTest;
import uk.gov.hmcts.reform.finrem.caseorchestration.client.DocumentClient;
import uk.gov.hmcts.reform.finrem.caseorchestration.config.DocumentConfiguration;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.CaseDocument;

import java.io.InputStream;
import java.util.Map;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertThat;
import static uk.gov.hmcts.reform.finrem.caseorchestration.TestConstants.AUTH_TOKEN;

public class ManualPaymentDocumentServiceTest extends BaseServiceTest {

    @Autowired
    private ObjectMapper mapper;

    @Autowired
    private DocumentClient documentClient;

    @Autowired
    private ManualPaymentDocumentService service;

    private CaseDetails caseDetails;

    @Before
    public void setUp() throws Exception {
        DocumentConfiguration config = new DocumentConfiguration();
        config.setHelpWithFeesSuccessfulNotificationTemplate("FL-FRM-LET-ENG-00552.docx");
        config.setHelpWithFeesSuccessfulNotificationFileName("ManualPaymentLetter.pdf");

        caseDetails = contestedPaperCaseDetails();
    }

    @Test
    public void shouldGenerateManualPaymentLetterForApplicant() {

        CaseDocument document = service.generateManualPaymentLetter(caseDetails, AUTH_TOKEN);

        Map<String, Object> caseData = caseDetails.getData();

        assertEquals(caseData.get("courtDetails"), "");

    }


    private CaseDetails contestedPaperCaseDetails() throws Exception {
        try (InputStream resourceAsStream = getClass().getResourceAsStream("/fixtures/contested/paper-case.json")) {
            ///fixtures/contested/paper-case-with-applicant-represented
            return mapper.readValue(resourceAsStream, CallbackRequest.class).getCaseDetails();
        }
    }
}