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
import uk.gov.hmcts.reform.finrem.caseorchestration.BaseServiceTest;
import uk.gov.hmcts.reform.finrem.caseorchestration.config.DocumentConfiguration;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.document.FrcCourtDetails;
import uk.gov.hmcts.reform.finrem.ccd.domain.Document;
import uk.gov.hmcts.reform.finrem.ccd.domain.FinremCaseDetails;

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
import static uk.gov.hmcts.reform.finrem.caseorchestration.TestSetUpUtils.finremCaseDetailsFromResource;
import static uk.gov.hmcts.reform.finrem.caseorchestration.TestSetUpUtils.newDocument;
import static uk.gov.hmcts.reform.finrem.caseorchestration.helper.DocumentHelper.ADDRESSEE;
import static uk.gov.hmcts.reform.finrem.caseorchestration.helper.DocumentHelper.PaperNotificationRecipient.APPLICANT;

public class ManualPaymentDocumentServiceTest extends BaseServiceTest {

    @Autowired private ManualPaymentDocumentService manualPaymentDocumentService;
    @Autowired private ObjectMapper mapper;

    @MockBean private GenericDocumentService genericDocumentService;

    @Captor
    ArgumentCaptor<Map<String, Object>> placeholdersMapCaptor;

    private FinremCaseDetails caseDetails;

    @Before
    public void setUp() {
        DocumentConfiguration config = new DocumentConfiguration();
        config.setHelpWithFeesSuccessfulNotificationTemplate("FL-FRM-LET-ENG-00552.docx");
        config.setHelpWithFeesSuccessfulNotificationFileName("ManualPaymentLetter.pdf");
    }

    @Test
    public void shouldGenerateManualPaymentLetterForApplicantSolicitor() throws Exception {
        caseDetails = contestedPaperCaseDetails();
        when(genericDocumentService.generateDocumentFromPlaceholdersMap(any(), any(), any(), any())).thenReturn(newDocument());

        Document generatedManualPaymentLetter
            = manualPaymentDocumentService.generateManualPaymentLetter(caseDetails, AUTH_TOKEN, APPLICANT);

        assertCaseDocument(generatedManualPaymentLetter);

        verify(genericDocumentService, times(1)).generateDocumentFromPlaceholdersMap(any(),
            placeholdersMapCaptor.capture(), any(), any());

        Map<String, Object> caseData = getDataFromCaptor(placeholdersMapCaptor);

        Addressee addressee = mapper.convertValue(caseData.get(ADDRESSEE), Addressee.class);
        System.out.println(addressee);
        assertThat(addressee.getName(), is("Applicant Solicitor Firm"));
        assertThat(addressee.getFormattedAddress(), is("67 Pears Road\nNear Roundabout\nMiddlesex\nHounslow\nTW3 1SS"));

        FrcCourtDetails frcCourtDetails = convertToCourtDetails(caseData.get("courtDetails"));
        assertThat(frcCourtDetails, is(notNullValue()));
        assertThat(frcCourtDetails.getCourtName(), is("Port Talbot Justice Centre"));
        assertThat(frcCourtDetails.getCourtAddress(), is("Harbourside Road, Port Talbot, SA13 1SB"));
        assertThat(frcCourtDetails.getPhoneNumber(), is("01792 485 800"));
        assertThat(frcCourtDetails.getEmail(), is("FRCswansea@justice.gov.uk"));
    }

    @Test
    public void shouldGenerateManualPaymentLetterForApplicant() throws Exception {
        caseDetails = contestedPaperCaseDetailsWithoutSolicitors();
        when(genericDocumentService.generateDocumentFromPlaceholdersMap(any(), any(), any(), any())).thenReturn(newDocument());

        Document generatedManualPaymentLetter
            = manualPaymentDocumentService.generateManualPaymentLetter(caseDetails, AUTH_TOKEN, APPLICANT);

        assertCaseDocument(generatedManualPaymentLetter);

        verify(genericDocumentService, times(1))
            .generateDocumentFromPlaceholdersMap(any(),
            placeholdersMapCaptor.capture(), any(), any());

        Map<String, Object> caseData = getDataFromCaptor(placeholdersMapCaptor);

        Addressee addressee = mapper.convertValue(caseData.get(ADDRESSEE), Addressee.class);
        System.out.println(addressee);
        assertThat(addressee.getName(), is("Applicant Name"));
        assertThat(addressee.getFormattedAddress(), is("Buckingham Palace\nLondon\nSW1A 1AA"));

        FrcCourtDetails frcCourtDetails = convertToCourtDetails(caseData.get("courtDetails"));
        assertThat(frcCourtDetails, is(notNullValue()));
        assertThat(frcCourtDetails.getCourtName(), is("Horsham County Court And Family Court"));
        assertThat(frcCourtDetails.getCourtAddress(), is("The Law Courts, Hurst Road, Horsham, RH12 2ET"));
        assertThat(frcCourtDetails.getPhoneNumber(), is("01634 887900"));
        assertThat(frcCourtDetails.getEmail(), is("FRCKSS@justice.gov.uk"));
    }

    private FinremCaseDetails contestedPaperCaseDetails() throws Exception {
        return finremCaseDetailsFromResource(getResource("/fixtures/contested/paper-case.json"), mapper);
    }

    private FinremCaseDetails contestedPaperCaseDetailsWithoutSolicitors() throws Exception {
        return finremCaseDetailsFromResource(getResource("/fixtures/contested/paper-case-no-solicitors.json"), mapper);
    }

    private FrcCourtDetails convertToCourtDetails(Object object) {
        return mapper.convertValue(object, new TypeReference<>() {
        });
    }
}