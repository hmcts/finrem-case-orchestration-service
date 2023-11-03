package uk.gov.hmcts.reform.finrem.caseorchestration.service;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.Before;
import org.junit.Test;
import org.mockito.ArgumentCaptor;
import org.mockito.Captor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.mock.mockito.MockBean;
import uk.gov.hmcts.reform.bsp.common.model.document.Addressee;
import uk.gov.hmcts.reform.ccd.client.model.CaseDetails;
import uk.gov.hmcts.reform.finrem.caseorchestration.BaseServiceTest;
import uk.gov.hmcts.reform.finrem.caseorchestration.config.DocumentConfiguration;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.CaseDocument;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.FinremCaseDetails;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.document.CourtDetailsTemplateFields;

import java.util.Map;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.is;
import static org.hamcrest.Matchers.notNullValue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static uk.gov.hmcts.reform.finrem.caseorchestration.TestConstants.AUTH_TOKEN;
import static uk.gov.hmcts.reform.finrem.caseorchestration.TestSetUpUtils.assertCaseDocument;
import static uk.gov.hmcts.reform.finrem.caseorchestration.TestSetUpUtils.caseDocument;
import static uk.gov.hmcts.reform.finrem.caseorchestration.helper.DocumentHelper.ADDRESSEE;
import static uk.gov.hmcts.reform.finrem.caseorchestration.helper.DocumentHelper.PaperNotificationRecipient.APPLICANT;

public class ManualPaymentDocumentServiceTest extends BaseServiceTest {

    @Autowired
    private ManualPaymentDocumentService manualPaymentDocumentService;
    @Autowired
    private ObjectMapper mapper;
    @MockBean
    private GenericDocumentService genericDocumentService;
    @Captor
    ArgumentCaptor<CaseDetails> documentGenerationRequestCaseDetailsCaptor;

    private FinremCaseDetails finremCaseDetails;

    @Before
    public void setUp() {
        DocumentConfiguration config = new DocumentConfiguration();
        config.setHelpWithFeesSuccessfulNotificationTemplate("FL-FRM-LET-ENG-00552.docx");
        config.setHelpWithFeesSuccessfulNotificationFileName("ManualPaymentLetter.pdf");
    }

    @Test
    public void shouldGenerateManualPaymentLetterForApplicantSolicitor() throws Exception {
        finremCaseDetails = buildFinremCallbackRequest("/fixtures/contested/paper-case.json").getCaseDetails();
        when(genericDocumentService.generateDocument(any(), any(), any(), any())).thenReturn(caseDocument());

        CaseDocument generatedManualPaymentLetter
            = manualPaymentDocumentService.generateManualPaymentLetter(finremCaseDetails, AUTH_TOKEN, APPLICANT);

        assertCaseDocument(generatedManualPaymentLetter);

        verify(genericDocumentService, times(1)).generateDocument(any(),
            documentGenerationRequestCaseDetailsCaptor.capture(), any(), any());

        Map<String, Object> caseData = documentGenerationRequestCaseDetailsCaptor.getValue().getData();

        Addressee addressee = (Addressee) caseData.get(ADDRESSEE);
        assertThat(addressee.getName(), is("Applicant Solicitor Firm"));
        assertThat(addressee.getFormattedAddress(), is("67 Pears Road\nNear Roundabout\nOpposite Tesco\nMiddlesex\nHounslow\nTW3 1SS"));

        CourtDetailsTemplateFields frcCourtDetails = convertToCourtDetails(caseData.get("courtDetails"));
        assertThat(frcCourtDetails, is(notNullValue()));
        assertThat(frcCourtDetails.getCourtName(), is("Port Talbot Justice Centre"));
        assertThat(frcCourtDetails.getCourtAddress(), is("Harbourside Road, Port Talbot, SA13 1SB"));
        assertThat(frcCourtDetails.getPhoneNumber(), is("01639 642267"));
        assertThat(frcCourtDetails.getEmail(), is("ptjc.familyteam@justice.gov.uk"));
    }

    @Test
    public void shouldGenerateManualPaymentLetterForApplicant() throws Exception {
        finremCaseDetails = buildFinremCallbackRequest("/fixtures/contested/paper-case-no-solicitors.json")
            .getCaseDetails();
        when(genericDocumentService.generateDocument(any(), any(), any(), any())).thenReturn(caseDocument());

        CaseDocument generatedManualPaymentLetter
            = manualPaymentDocumentService.generateManualPaymentLetter(finremCaseDetails, AUTH_TOKEN, APPLICANT);

        assertCaseDocument(generatedManualPaymentLetter);

        verify(genericDocumentService, times(1)).generateDocument(any(),
            documentGenerationRequestCaseDetailsCaptor.capture(), any(), any());
        Map<String, Object> caseData = documentGenerationRequestCaseDetailsCaptor.getValue().getData();

        Addressee addressee = (Addressee) caseData.get(ADDRESSEE);
        assertThat(addressee.getName(), is("Applicant Name"));
        assertThat(addressee.getFormattedAddress(), is("Buckingham Palace\nLondon\nSW1A 1AA"));

        CourtDetailsTemplateFields frcCourtDetails = convertToCourtDetails(caseData.get("courtDetails"));
        assertThat(frcCourtDetails, is(notNullValue()));
        assertThat(frcCourtDetails.getCourtName(), is("Horsham County Court And Family Court"));
        assertThat(frcCourtDetails.getCourtAddress(), is("The Law Courts, Hurst Road, Horsham, RH12 2ET"));
        assertThat(frcCourtDetails.getPhoneNumber(), is("0300 1235577"));
        assertThat(frcCourtDetails.getEmail(), is("sussexfamily@Justice.gov.uk"));
    }

    private FinremCaseDetails contestedPaperFinremCaseDetails() throws Exception {
        try (InputStream resourceAsStream = getClass().getResourceAsStream("/fixtures/contested/paper-case.json")) {
            return mapper.readValue(resourceAsStream, FinremCallbackRequest.class).getCaseDetails();
        }
    }

    private FinremCaseDetails contestedPaperCaseDetailsWithoutSolicitors() throws Exception {
        try (InputStream resourceAsStream = getClass().getResourceAsStream(
            "/fixtures/contested/paper-case-no-solicitors.json")) {
            return mapper.readValue(resourceAsStream, FinremCallbackRequest.class).getCaseDetails();
        }
    }

    private CourtDetailsTemplateFields convertToCourtDetails(Object object) {
        return mapper.convertValue(object, new TypeReference<>() {
        });
    }
}