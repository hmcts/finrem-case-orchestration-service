package uk.gov.hmcts.reform.finrem.caseorchestration.service;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.Before;
import org.junit.Test;
import org.mockito.ArgumentCaptor;
import org.mockito.Captor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.test.context.ActiveProfiles;

import uk.gov.hmcts.reform.ccd.client.model.CallbackRequest;
import uk.gov.hmcts.reform.ccd.client.model.CaseDetails;
import uk.gov.hmcts.reform.finrem.caseorchestration.BaseServiceTest;
import uk.gov.hmcts.reform.finrem.caseorchestration.config.DocumentConfiguration;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.CaseDocument;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.PensionCollectionData;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.document.CourtDetails;

import java.io.InputStream;
import java.util.List;

import static org.hamcrest.Matchers.is;
import static org.hamcrest.Matchers.notNullValue;
import static org.junit.Assert.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static uk.gov.hmcts.reform.finrem.caseorchestration.TestConstants.AUTH_TOKEN;
import static uk.gov.hmcts.reform.finrem.caseorchestration.TestSetUpUtils.assertCaseDocument;
import static uk.gov.hmcts.reform.finrem.caseorchestration.TestSetUpUtils.caseDocument;

@ActiveProfiles("test-mock-document-client")
public class ManualPaymentDocumentServiceTest extends BaseServiceTest {

    @Autowired
    private ObjectMapper mapper;

    @Autowired
    private ManualPaymentDocumentService manualPaymentDocumentService;

    @MockBean
    private GenericDocumentService genericDocumentService;

    @Captor
    ArgumentCaptor<CaseDetails> documentGenerationRequestCaseDetailsCaptor;

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
        when(genericDocumentService.generateDocument(any(), any(), any(), any())).thenReturn(caseDocument());

        CaseDocument generatedManualPaymentLetter
            = manualPaymentDocumentService.generateManualPaymentLetter(caseDetails, AUTH_TOKEN);

        assertCaseDocument(generatedManualPaymentLetter);

        verify(genericDocumentService, times(1)).generateDocument(any(),
            documentGenerationRequestCaseDetailsCaptor.capture(), any(), any());

        CourtDetails courtDetails = convertToCourtDetails(
            documentGenerationRequestCaseDetailsCaptor.getValue().getData().get("courtDetails"));

        assertThat(courtDetails, is(notNullValue()));
        assertThat(courtDetails.getCourtName(), is("Port Talbot Justice Centre"));
        assertThat(courtDetails.getCourtAddress(), is("Harbourside Road, Port Talbot, SA13 1SB"));
        assertThat(courtDetails.getPhoneNumber(), is("01792 485 800"));
        assertThat(courtDetails.getEmail(), is("FRCswansea@justice.gov.uk"));
    }

    private CaseDetails contestedPaperCaseDetails() throws Exception {
        try (InputStream resourceAsStream = getClass().getResourceAsStream("/fixtures/contested/paper-case.json")) {
            return mapper.readValue(resourceAsStream, CallbackRequest.class).getCaseDetails();
        }
    }

    private CourtDetails convertToCourtDetails(Object object) {
        return mapper.convertValue(object, new TypeReference<>() {
        });
    }
}