package uk.gov.hmcts.reform.finrem.caseorchestration.service;

import org.junit.Before;
import org.junit.Test;
import org.mockito.ArgumentCaptor;
import org.mockito.Captor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.test.annotation.DirtiesContext;
import org.springframework.test.context.ActiveProfiles;
import uk.gov.hmcts.reform.bsp.common.model.document.Addressee;
import uk.gov.hmcts.reform.bsp.common.model.document.CtscContactDetails;
import uk.gov.hmcts.reform.ccd.client.model.CaseDetails;
import uk.gov.hmcts.reform.finrem.caseorchestration.BaseServiceTest;
import uk.gov.hmcts.reform.finrem.caseorchestration.client.DocumentClient;
import uk.gov.hmcts.reform.finrem.caseorchestration.config.DocumentConfiguration;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.CaseDocument;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.document.DocumentGenerationRequest;

import java.time.LocalDate;
import java.util.HashMap;
import java.util.Map;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.is;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.eq;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static uk.gov.hmcts.reform.finrem.caseorchestration.OrchestrationConstants.YES_VALUE;
import static uk.gov.hmcts.reform.finrem.caseorchestration.TestConstants.AUTH_TOKEN;
import static uk.gov.hmcts.reform.finrem.caseorchestration.TestConstants.TEST_SOLICITOR_NAME;
import static uk.gov.hmcts.reform.finrem.caseorchestration.TestConstants.TEST_SOLICITOR_REFERENCE;
import static uk.gov.hmcts.reform.finrem.caseorchestration.TestSetUpUtils.assertCaseDocument;
import static uk.gov.hmcts.reform.finrem.caseorchestration.TestSetUpUtils.defaultConsentedCaseDetails;
import static uk.gov.hmcts.reform.finrem.caseorchestration.TestSetUpUtils.defaultContestedCaseDetails;
import static uk.gov.hmcts.reform.finrem.caseorchestration.TestSetUpUtils.document;
import static uk.gov.hmcts.reform.finrem.caseorchestration.helper.DocumentHelper.PaperNotificationRecipient.APPLICANT;
import static uk.gov.hmcts.reform.finrem.caseorchestration.helper.DocumentHelper.PaperNotificationRecipient.RESPONDENT;
import static uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.CCDConfigConstant.APPLICANT_REPRESENTED;
import static uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.CCDConfigConstant.CONSENTED_SOLICITOR_ADDRESS;
import static uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.CCDConfigConstant.CONSENTED_SOLICITOR_NAME;
import static uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.CCDConfigConstant.CONTESTED_RESPONDENT_REPRESENTED;
import static uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.CCDConfigConstant.CONTESTED_SOLICITOR_ADDRESS;
import static uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.CCDConfigConstant.CONTESTED_SOLICITOR_NAME;
import static uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.CCDConfigConstant.RESP_SOLICITOR_ADDRESS;
import static uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.CCDConfigConstant.RESP_SOLICITOR_NAME;
import static uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.CCDConfigConstant.SOLICITOR_REFERENCE;

@ActiveProfiles("test-mock-feign-clients")
public class AssignedToJudgeDocumentServiceTest extends BaseServiceTest {

    @Autowired private AssignedToJudgeDocumentService assignedToJudgeDocumentService;
    @Autowired private DocumentClient documentClientMock;

    private CaseDetails caseDetails;

    @Captor
    private ArgumentCaptor<DocumentGenerationRequest> documentGenerationRequestCaptor;

    @Before
    public void setUp() {
        DocumentConfiguration config = new DocumentConfiguration();
        config.setAssignedToJudgeNotificationTemplate("FL-FRM-LET-ENG-00318.docx");
        config.setAssignedToJudgeNotificationFileName("AssignedToJudgeNotificationLetter.pdf");
        config.setConsentInContestedAssignedToJudgeNotificationFileName("FL-FRM-LET-ENG-00578.docx");
    }

    @DirtiesContext(methodMode = DirtiesContext.MethodMode.AFTER_METHOD)
    @Test
    public void shouldGenerateAssignedToJudgeLetterForApplicant() {
        caseDetails = defaultConsentedCaseDetails();

        when(documentClientMock.generatePdf(any(), anyString())).thenReturn(document());

        CaseDocument generateAssignedToJudgeNotificationLetter
            = assignedToJudgeDocumentService.generateAssignedToJudgeNotificationLetter(caseDetails, AUTH_TOKEN, APPLICANT);

        assertCaseDocument(generateAssignedToJudgeNotificationLetter);
        verify(documentClientMock).generatePdf(any(), anyString());
    }

    @DirtiesContext(methodMode = DirtiesContext.MethodMode.AFTER_METHOD)
    @Test
    public void shouldGenerateAssignedToJudgeLetterForApplicantSolicitor() {
        caseDetails = defaultConsentedCaseDetails();

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
        caseData.put(CONSENTED_SOLICITOR_NAME, TEST_SOLICITOR_NAME);
        caseData.put(SOLICITOR_REFERENCE, TEST_SOLICITOR_REFERENCE);
        caseData.put(CONSENTED_SOLICITOR_ADDRESS, solicitorAddress);

        CaseDocument generatedAssignedToJudgeNotificationLetter
            = assignedToJudgeDocumentService.generateAssignedToJudgeNotificationLetter(caseDetails, AUTH_TOKEN, APPLICANT);

        assertCaseDocument(generatedAssignedToJudgeNotificationLetter);
    }

    @DirtiesContext(methodMode = DirtiesContext.MethodMode.AFTER_METHOD)
    @Test
    public void shouldGenerateApplicantConsentInContestedAssignedToJudgeNotificationLetter() {
        caseDetails = defaultContestedCaseDetails();

        when(documentClientMock.generatePdf(any(), anyString())).thenReturn(document());

        CaseDocument generateAssignedToJudgeNotificationLetter
            = assignedToJudgeDocumentService.generateConsentInContestedAssignedToJudgeNotificationLetter(caseDetails, AUTH_TOKEN, APPLICANT);

        assertCaseDocument(generateAssignedToJudgeNotificationLetter);
        verify(documentClientMock).generatePdf(documentGenerationRequestCaptor.capture(), eq(AUTH_TOKEN));

        CaseDetails caseDetails = (CaseDetails) documentGenerationRequestCaptor.getValue().getValues().get("caseDetails");

        Addressee addressee = (Addressee) caseDetails.getData().get("addressee");
        assertThat(addressee.getName(), is("James Joyce"));
        assertThat(addressee.getFormattedAddress(), is("50 Applicant Street\nSecond Address Line\nLondon\nLondon\nSW1"));

        CtscContactDetails ctscContactDetails = (CtscContactDetails) caseDetails.getData().get("ctscContactDetails");
        assertThat(ctscContactDetails.getServiceCentre(), is("Courts and Tribunals Service Centre"));

        assertThat(caseDetails.getData().get("letterDate"), is(String.valueOf(LocalDate.now())));
        assertThat(caseDetails.getData().get("applicantName"), is("James Joyce"));
        assertThat(caseDetails.getData().get("respondentName"), is("Jane Doe"));
        assertThat(caseDetails.getData().get("caseNumber"), is("987654321"));

        Map<String, Object> courtDetails = (Map<String, Object>) caseDetails.getData().get("courtDetails");
        assertThat(courtDetails.get("courtName"), is("Mansfield Magistrates And County Court"));
    }

    @DirtiesContext(methodMode = DirtiesContext.MethodMode.AFTER_METHOD)
    @Test
    public void shouldGenerateApplicantConsentInContestedAssignedToJudgeNotificationLetterForApplicantSolicitor() {
        caseDetails = defaultContestedCaseDetails();

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
        caseData.put(CONTESTED_SOLICITOR_NAME, TEST_SOLICITOR_NAME);
        caseData.put(SOLICITOR_REFERENCE, TEST_SOLICITOR_REFERENCE);
        caseData.put(CONTESTED_SOLICITOR_ADDRESS, solicitorAddress);

        CaseDocument generatedAssignedToJudgeNotificationLetter
            = assignedToJudgeDocumentService.generateConsentInContestedAssignedToJudgeNotificationLetter(caseDetails, AUTH_TOKEN, APPLICANT);

        assertCaseDocument(generatedAssignedToJudgeNotificationLetter);
        verify(documentClientMock).generatePdf(documentGenerationRequestCaptor.capture(), eq(AUTH_TOKEN));

        CaseDetails caseDetails = (CaseDetails) documentGenerationRequestCaptor.getValue().getValues().get("caseDetails");

        Addressee addressee = (Addressee) caseDetails.getData().get("addressee");
        assertThat(addressee.getName(), is("Saul Goodman"));
        assertThat(addressee.getFormattedAddress(), is("123 Applicant Solicitor Street\nSecond Address Line\nLondon\nLondon\nSE1"));

        CtscContactDetails ctscContactDetails = (CtscContactDetails) caseDetails.getData().get("ctscContactDetails");
        assertThat(ctscContactDetails.getServiceCentre(), is("Courts and Tribunals Service Centre"));

        assertThat(caseDetails.getData().get("letterDate"), is(String.valueOf(LocalDate.now())));
        assertThat(caseDetails.getData().get("applicantName"), is("James Joyce"));
        assertThat(caseDetails.getData().get("respondentName"), is("Jane Doe"));
        assertThat(caseDetails.getData().get("caseNumber"), is("987654321"));

        Map<String, Object> courtDetails = (Map<String, Object>) caseDetails.getData().get("courtDetails");
        assertThat(courtDetails.get("courtName"), is("Mansfield Magistrates And County Court"));
    }

    @DirtiesContext(methodMode = DirtiesContext.MethodMode.AFTER_METHOD)
    @Test
    public void shouldGenerateRespondentConsentInContestedAssignedToJudgeNotificationLetter() {
        caseDetails = defaultContestedCaseDetails();

        when(documentClientMock.generatePdf(any(), anyString())).thenReturn(document());

        CaseDocument generateAssignedToJudgeNotificationLetter
            = assignedToJudgeDocumentService.generateConsentInContestedAssignedToJudgeNotificationLetter(caseDetails, AUTH_TOKEN, RESPONDENT);

        assertCaseDocument(generateAssignedToJudgeNotificationLetter);
        verify(documentClientMock).generatePdf(documentGenerationRequestCaptor.capture(), eq(AUTH_TOKEN));

        CaseDetails caseDetails = (CaseDetails) documentGenerationRequestCaptor.getValue().getValues().get("caseDetails");

        Addressee addressee = (Addressee) caseDetails.getData().get("addressee");
        assertThat(addressee.getName(), is("Jane Doe"));
        assertThat(addressee.getFormattedAddress(), is("50 Respondent Street\nContested\nLondon\nLondon\nSW1"));

        CtscContactDetails ctscContactDetails = (CtscContactDetails) caseDetails.getData().get("ctscContactDetails");
        assertThat(ctscContactDetails.getServiceCentre(), is("Courts and Tribunals Service Centre"));

        assertThat(caseDetails.getData().get("letterDate"), is(String.valueOf(LocalDate.now())));
        assertThat(caseDetails.getData().get("applicantName"), is("James Joyce"));
        assertThat(caseDetails.getData().get("respondentName"), is("Jane Doe"));
        assertThat(caseDetails.getData().get("caseNumber"), is("987654321"));

        Map<String, Object> courtDetails = (Map<String, Object>) caseDetails.getData().get("courtDetails");
        assertThat(courtDetails.get("courtName"), is("Mansfield Magistrates And County Court"));
    }

    @DirtiesContext(methodMode = DirtiesContext.MethodMode.AFTER_METHOD)
    @Test
    public void shouldGenerateRespondentConsentInContestedAssignedToJudgeNotificationLetterForApplicantSolicitor() {
        caseDetails = defaultContestedCaseDetails();

        when(documentClientMock.generatePdf(any(), anyString())).thenReturn(document());

        Map<String, Object> solicitorAddress = new HashMap<>();
        solicitorAddress.put("AddressLine1", "123 Respondent Solicitor Street");
        solicitorAddress.put("AddressLine2", "Second Address Line");
        solicitorAddress.put("AddressLine3", "Third Address Line");
        solicitorAddress.put("County", "London");
        solicitorAddress.put("Country", "England");
        solicitorAddress.put("PostTown", "London");
        solicitorAddress.put("PostCode", "SE1");

        Map<String, Object> caseData = caseDetails.getData();
        caseData.replace(CONTESTED_RESPONDENT_REPRESENTED, YES_VALUE);
        caseData.put(RESP_SOLICITOR_NAME, TEST_SOLICITOR_NAME);
        caseData.put(SOLICITOR_REFERENCE, TEST_SOLICITOR_REFERENCE);
        caseData.put(RESP_SOLICITOR_ADDRESS, solicitorAddress);

        CaseDocument generatedAssignedToJudgeNotificationLetter
            = assignedToJudgeDocumentService.generateConsentInContestedAssignedToJudgeNotificationLetter(caseDetails, AUTH_TOKEN, RESPONDENT);

        assertCaseDocument(generatedAssignedToJudgeNotificationLetter);
        verify(documentClientMock).generatePdf(documentGenerationRequestCaptor.capture(), eq(AUTH_TOKEN));

        CaseDetails caseDetails = (CaseDetails) documentGenerationRequestCaptor.getValue().getValues().get("caseDetails");

        Addressee addressee = (Addressee) caseDetails.getData().get("addressee");
        assertThat(addressee.getName(), is("Saul Goodman"));
        assertThat(addressee.getFormattedAddress(), is("123 Respondent Solicitor Street\nSecond Address Line\nLondon\nLondon\nSE1"));

        CtscContactDetails ctscContactDetails = (CtscContactDetails) caseDetails.getData().get("ctscContactDetails");
        assertThat(ctscContactDetails.getServiceCentre(), is("Courts and Tribunals Service Centre"));

        assertThat(caseDetails.getData().get("letterDate"), is(String.valueOf(LocalDate.now())));
        assertThat(caseDetails.getData().get("applicantName"), is("James Joyce"));
        assertThat(caseDetails.getData().get("respondentName"), is("Jane Doe"));
        assertThat(caseDetails.getData().get("caseNumber"), is("987654321"));

        Map<String, Object> courtDetails = (Map<String, Object>) caseDetails.getData().get("courtDetails");
        assertThat(courtDetails.get("courtName"), is("Mansfield Magistrates And County Court"));
    }
}
