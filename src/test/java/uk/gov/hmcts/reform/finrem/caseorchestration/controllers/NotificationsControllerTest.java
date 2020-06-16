package uk.gov.hmcts.reform.finrem.caseorchestration.controllers;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.test.context.junit4.SpringRunner;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;
import org.springframework.web.context.WebApplicationContext;
import uk.gov.hmcts.reform.ccd.client.model.CallbackRequest;
import uk.gov.hmcts.reform.ccd.client.model.CaseDetails;
import uk.gov.hmcts.reform.finrem.caseorchestration.service.AssignedToJudgeDocumentService;
import uk.gov.hmcts.reform.finrem.caseorchestration.service.BulkPrintService;
import uk.gov.hmcts.reform.finrem.caseorchestration.service.FeatureToggleService;
import uk.gov.hmcts.reform.finrem.caseorchestration.service.GeneralEmailService;
import uk.gov.hmcts.reform.finrem.caseorchestration.service.HelpWithFeesDocumentService;
import uk.gov.hmcts.reform.finrem.caseorchestration.service.NotificationService;

import java.io.File;
import java.io.IOException;
import java.net.URISyntaxException;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;
import static uk.gov.hmcts.reform.finrem.caseorchestration.OrchestrationConstants.AUTHORIZATION_HEADER;
import static uk.gov.hmcts.reform.finrem.caseorchestration.TestConstants.AUTH_TOKEN;

@RunWith(SpringRunner.class)
@WebMvcTest(NotificationsController.class)
public class NotificationsControllerTest {
    //URLs
    private static final String HWF_SUCCESSFUL_CALLBACK_URL = "/case-orchestration/notify/hwf-successful";
    private static final String ASSIGN_TO_JUDGE_CALLBACK_URL = "/case-orchestration/notify/assign-to-judge";
    private static final String CONSENT_ORDER_MADE_URL = "/case-orchestration/notify/consent-order-made";
    private static final String CONSENT_ORDER_NOT_APPROVED_URL = "/case-orchestration/notify/consent-order-not-approved";
    private static final String CONSENT_ORDER_AVAILABLE_URL = "/case-orchestration/notify/consent-order-available";
    private static final String PREPARE_FOR_HEARING_CALLBACK_URL = "/case-orchestration/notify/prepare-for-hearing";
    private static final String PREPARE_FOR_HEARING_ORDER_SENT_CALLBACK_URL = "/case-orchestration/notify/prepare-for-hearing-order-sent";
    private static final String CONTESTED_APPLICATION_ISSUED_CALLBACK_URL = "/case-orchestration/notify/contest-application-issued";
    private static final String CONTEST_ORDER_APPROVED_CALLBACK_URL = "/case-orchestration/notify/contest-order-approved";
    private static final String CONTESTED_DRAFT_ORDER_URL = "/case-orchestration/notify/draft-order";
    private static final String GENERAL_EMAIL_URL = "/case-orchestration/notify/general-email";

    //JSON Data
    private static final String CCD_REQUEST_JSON = "/fixtures/model/ccd-request.json";
    private static final String CCD_REQUEST_WITH_SOL_EMAIL_CONSENT_JSON = "/fixtures/ccd-request-with-solicitor-email-consent.json";
    private static final String BULK_PRINT_PAPER_APPLICATION_JSON = "/fixtures/bulkprint/bulk-print-paper-application.json";
    private static final String DRAFT_ORDER_SUCCESSFUL_APPLICANT_SOL = "/fixtures/applicant-solicitor-to-draft-order-with-email-consent.json";
    private static final String DRAFT_ORDER_UNSUCCESSFUL_APPLICANT_SOL = "/fixtures/applicant-solicitor-to-draft-order-without-email-consent.json";
    private static final String DRAFT_ORDER_UNSUCCESSFUL_RESPONDENT_SOL = "/fixtures/respondent-solicitor-to-draft-order-with-email-consent.json";
    private static final String GENERAL_EMAIL_CONSENTED = "/fixtures/general-email-consented.json";
    private static final String GENERAL_EMAIL_CONTESTED = "/fixtures/contested/general-email-contested.json";

    @Autowired
    private WebApplicationContext applicationContext;

    @MockBean
    private NotificationService notificationService;

    @MockBean
    private FeatureToggleService featureToggleService;

    @MockBean
    private BulkPrintService bulkPrintService;

    @MockBean
    private AssignedToJudgeDocumentService assignedToJudgeDocumentService;

    @MockBean
    private GeneralEmailService generalEmailService;

    @MockBean
    private HelpWithFeesDocumentService helpWithFeesDocumentService;

    private MockMvc mockMvc;
    private JsonNode requestContent;

    @Before
    public void setUp() {
        mockMvc = MockMvcBuilders.webAppContextSetup(applicationContext).build();
    }

    @Test
    public void sendHwfSuccessfulConfirmationEmailIfDigitalCase() throws Exception {
        buildCcdRequest(CCD_REQUEST_WITH_SOL_EMAIL_CONSENT_JSON);
        mockMvc.perform(post(HWF_SUCCESSFUL_CALLBACK_URL)
            .header(AUTHORIZATION_HEADER, AUTH_TOKEN)
            .content(requestContent.toString())
            .contentType(MediaType.APPLICATION_JSON_VALUE))
            .andExpect(status().isOk());

        verify(notificationService, times(1))
            .sendConsentedHWFSuccessfulConfirmationEmail(any(CallbackRequest.class));
        verifyNoInteractions(helpWithFeesDocumentService);
        verifyNoInteractions(bulkPrintService);
    }

    @Test
    public void shouldNotSendHwfSuccessfulConfirmationEmail() throws Exception {
        buildCcdRequest(CCD_REQUEST_JSON);
        mockMvc.perform(post(HWF_SUCCESSFUL_CALLBACK_URL)
            .header(AUTHORIZATION_HEADER, AUTH_TOKEN)
            .content(requestContent.toString())
            .contentType(MediaType.APPLICATION_JSON_VALUE))
            .andExpect(status().isOk());

        verifyNoInteractions(notificationService);
    }

    @Test
    public void sendHwfSuccessfulNotificationLetterIfIsConsentedAndIsPaperApplication_AndToggledOn() throws Exception {
        buildCcdRequest(BULK_PRINT_PAPER_APPLICATION_JSON);

        when(featureToggleService.isHwfSuccessfulNotificationLetterEnabled()).thenReturn(true);
        mockMvc.perform(post(HWF_SUCCESSFUL_CALLBACK_URL)
            .header(AUTHORIZATION_HEADER, AUTH_TOKEN)
            .content(requestContent.toString())
            .contentType(MediaType.APPLICATION_JSON_VALUE))
            .andExpect(status().isOk());

        verify(helpWithFeesDocumentService, times(1))
            .generateHwfSuccessfulNotificationLetter(any(CaseDetails.class),any());
        verify(bulkPrintService, times(1))
            .sendNotificationLetterForBulkPrint(any(),any());
        verifyNoInteractions(notificationService);
    }

    @Test
    public void shouldNotSendHwfSuccessfulNotificationLetterIfIsConsentedAndIsPaperApplication_AndToggledOff() throws Exception {
        buildCcdRequest(BULK_PRINT_PAPER_APPLICATION_JSON);

        when(featureToggleService.isHwfSuccessfulNotificationLetterEnabled()).thenReturn(false);
        mockMvc.perform(post(HWF_SUCCESSFUL_CALLBACK_URL)
            .header(AUTHORIZATION_HEADER, AUTH_TOKEN)
            .content(requestContent.toString())
            .contentType(MediaType.APPLICATION_JSON_VALUE))
            .andExpect(status().isOk());

        verifyNoInteractions(helpWithFeesDocumentService);
        verifyNoInteractions(bulkPrintService);
        verifyNoInteractions(notificationService);
    }

    @Test
    public void sendAssignToJudgeConfirmationEmailIfDigitalCase() throws Exception {
        buildCcdRequest(CCD_REQUEST_WITH_SOL_EMAIL_CONSENT_JSON);
        mockMvc.perform(post(ASSIGN_TO_JUDGE_CALLBACK_URL)
            .header(AUTHORIZATION_HEADER, AUTH_TOKEN)
            .content(requestContent.toString())
            .contentType(MediaType.APPLICATION_JSON_VALUE))
            .andExpect(status().isOk());

        verify(notificationService, times(1))
                .sendAssignToJudgeConfirmationEmail(any(CallbackRequest.class));
        verifyNoInteractions(assignedToJudgeDocumentService);
        verifyNoInteractions(bulkPrintService);
    }

    @Test
    public void shouldNotSendAssignToJudgeConfirmationEmail() throws Exception {
        buildCcdRequest(CCD_REQUEST_JSON);
        mockMvc.perform(post(ASSIGN_TO_JUDGE_CALLBACK_URL)
            .header(AUTHORIZATION_HEADER, AUTH_TOKEN)
            .content(requestContent.toString())
            .contentType(MediaType.APPLICATION_JSON_VALUE))
            .andExpect(status().isOk());

        verifyNoInteractions(notificationService);
    }

    @Test
    public void sendAssignToJudgeNotificationLetterIfIsPaperApplication_AndToggledOn() throws Exception {
        buildCcdRequest(BULK_PRINT_PAPER_APPLICATION_JSON);

        when(featureToggleService.isAssignedToJudgeNotificationLetterEnabled()).thenReturn(true);
        mockMvc.perform(post(ASSIGN_TO_JUDGE_CALLBACK_URL)
            .header(AUTHORIZATION_HEADER, AUTH_TOKEN)
            .content(requestContent.toString())
            .contentType(MediaType.APPLICATION_JSON_VALUE))
            .andExpect(status().isOk());

        verify(assignedToJudgeDocumentService, times(1))
            .generateAssignedToJudgeNotificationLetter(any(CaseDetails.class),any());
        verify(bulkPrintService, times(1))
            .sendNotificationLetterForBulkPrint(any(),any());
        verifyNoInteractions(notificationService);
    }

    @Test
    public void shouldNotSendAssignToJudgeNotificationLetterIfIsPaperApplication_AndToggledOff() throws Exception {
        buildCcdRequest(BULK_PRINT_PAPER_APPLICATION_JSON);

        when(featureToggleService.isAssignedToJudgeNotificationLetterEnabled()).thenReturn(false);
        mockMvc.perform(post(ASSIGN_TO_JUDGE_CALLBACK_URL)
            .header(AUTHORIZATION_HEADER, AUTH_TOKEN)
            .content(requestContent.toString())
            .contentType(MediaType.APPLICATION_JSON_VALUE))
            .andExpect(status().isOk());

        verifyNoInteractions(assignedToJudgeDocumentService);
        verifyNoInteractions(bulkPrintService);
        verifyNoInteractions(notificationService);
    }

    @Test
    public void sendConsentOrderMadeConfirmationEmail() throws Exception {
        buildCcdRequest(CCD_REQUEST_WITH_SOL_EMAIL_CONSENT_JSON);
        mockMvc.perform(post(CONSENT_ORDER_MADE_URL)
            .header(AUTHORIZATION_HEADER, AUTH_TOKEN)
            .content(requestContent.toString())
            .contentType(MediaType.APPLICATION_JSON_VALUE))
            .andExpect(status().isOk());

        verify(notificationService, times(1))
            .sendConsentOrderMadeConfirmationEmail(any(CallbackRequest.class));
    }

    @Test
    public void shouldNotSendConsentOrderMadeConfirmationEmail() throws Exception {
        buildCcdRequest(CCD_REQUEST_JSON);
        mockMvc.perform(post(CONSENT_ORDER_MADE_URL)
            .header(AUTHORIZATION_HEADER, AUTH_TOKEN)
            .content(requestContent.toString())
            .contentType(MediaType.APPLICATION_JSON_VALUE))
            .andExpect(status().isOk());

        verifyNoInteractions(notificationService);
    }

    @Test
    public void sendConsentOrderNotApprovedEmail() throws Exception {
        buildCcdRequest(CCD_REQUEST_WITH_SOL_EMAIL_CONSENT_JSON);
        mockMvc.perform(post(CONSENT_ORDER_NOT_APPROVED_URL)
            .header(AUTHORIZATION_HEADER, AUTH_TOKEN)
            .content(requestContent.toString())
            .contentType(MediaType.APPLICATION_JSON_VALUE))
            .andExpect(status().isOk());

        verify(notificationService, times(1))
            .sendConsentOrderNotApprovedEmail(any(CallbackRequest.class));
    }

    @Test
    public void shouldNotSendConsentOrderNotApprovedEmail() throws Exception {
        buildCcdRequest(CCD_REQUEST_JSON);
        mockMvc.perform(post(CONSENT_ORDER_NOT_APPROVED_URL)
            .header(AUTHORIZATION_HEADER, AUTH_TOKEN)
            .content(requestContent.toString())
            .contentType(MediaType.APPLICATION_JSON_VALUE))
            .andExpect(status().isOk());

        verifyNoInteractions(notificationService);
    }

    @Test
    public void sendConsentOrderAvailableEmail() throws Exception {
        buildCcdRequest(CCD_REQUEST_WITH_SOL_EMAIL_CONSENT_JSON);
        mockMvc.perform(post(CONSENT_ORDER_AVAILABLE_URL)
            .header(AUTHORIZATION_HEADER, AUTH_TOKEN)
            .content(requestContent.toString())
            .contentType(MediaType.APPLICATION_JSON_VALUE))
            .andExpect(status().isOk());

        verify(notificationService, times(1))
            .sendConsentOrderAvailableEmail(any(CallbackRequest.class));
    }

    @Test
    public void shouldNotSendConsentOrderAvailableEmail() throws Exception {
        buildCcdRequest(CCD_REQUEST_JSON);
        mockMvc.perform(post(CONSENT_ORDER_AVAILABLE_URL)
            .header(AUTHORIZATION_HEADER, AUTH_TOKEN)
            .content(requestContent.toString())
            .contentType(MediaType.APPLICATION_JSON_VALUE))
            .andExpect(status().isOk());

        verifyNoInteractions(notificationService);
    }

    @Test
    public void sendConsentedHwfSuccessfulConfirmationEmail() throws Exception {
        buildCcdRequest("/fixtures/contested/hwf.json");
        mockMvc.perform(post(HWF_SUCCESSFUL_CALLBACK_URL)
            .header(AUTHORIZATION_HEADER, AUTH_TOKEN)
            .content(requestContent.toString())
            .contentType(MediaType.APPLICATION_JSON_VALUE))
            .andExpect(status().isOk());

        verify(notificationService, times(1))
            .sendContestedHwfSuccessfulConfirmationEmail(any(CallbackRequest.class));
    }

    @Test
    public void shouldNotSendContestedHwfSuccessfulEmail() throws Exception {
        buildCcdRequest("/fixtures/contested/contested-hwf-without-solicitor-consent.json");
        mockMvc.perform(post(HWF_SUCCESSFUL_CALLBACK_URL)
            .header(AUTHORIZATION_HEADER, AUTH_TOKEN)
            .content(requestContent.toString())
            .contentType(MediaType.APPLICATION_JSON_VALUE))
            .andExpect(status().isOk());

        verifyNoInteractions(notificationService);
    }

    @Test
    public void shouldSendPrepareForHearingEmailWhenAgreed() throws Exception {
        buildCcdRequest(CCD_REQUEST_WITH_SOL_EMAIL_CONSENT_JSON);
        mockMvc.perform(post(PREPARE_FOR_HEARING_CALLBACK_URL)
            .content(requestContent.toString())
            .header(AUTHORIZATION_HEADER, AUTH_TOKEN)
            .contentType(MediaType.APPLICATION_JSON_VALUE))
            .andExpect(status().isOk());

        verify(notificationService, times(1)).sendPrepareForHearingEmail(any());
    }

    @Test
    public void shouldNotSendPrepareForHearingEmailWhenNotAgreed() throws Exception {
        buildCcdRequest(CCD_REQUEST_JSON);
        mockMvc.perform(post(PREPARE_FOR_HEARING_CALLBACK_URL)
            .content(requestContent.toString())
            .header(AUTHORIZATION_HEADER, AUTH_TOKEN)
            .contentType(MediaType.APPLICATION_JSON_VALUE))
            .andExpect(status().isOk());

        verifyNoInteractions(notificationService);
    }

    @Test
    public void shouldSendPrepareForHearingOrderSentEmailWhenAgreed() throws Exception {
        buildCcdRequest(CCD_REQUEST_WITH_SOL_EMAIL_CONSENT_JSON);
        mockMvc.perform(post(PREPARE_FOR_HEARING_ORDER_SENT_CALLBACK_URL)
            .content(requestContent.toString())
            .header(AUTHORIZATION_HEADER, AUTH_TOKEN)
            .contentType(MediaType.APPLICATION_JSON_VALUE))
            .andExpect(status().isOk());

        verify(notificationService, times(1)).sendPrepareForHearingOrderSentEmail(any());
    }

    @Test
    public void shouldNotSendPrepareForHearingOrderSentEmailWhenNotAgreed() throws Exception {
        buildCcdRequest(CCD_REQUEST_JSON);
        mockMvc.perform(post(PREPARE_FOR_HEARING_ORDER_SENT_CALLBACK_URL)
            .content(requestContent.toString())
            .header(AUTHORIZATION_HEADER, AUTH_TOKEN)
            .contentType(MediaType.APPLICATION_JSON_VALUE))
            .andExpect(status().isOk());

        verifyNoInteractions(notificationService);
    }

    @Test
    public void shouldSendContestedApplicationIssuedEmailWhenAgreed() throws Exception {
        buildCcdRequest(CCD_REQUEST_WITH_SOL_EMAIL_CONSENT_JSON);
        mockMvc.perform(post(CONTESTED_APPLICATION_ISSUED_CALLBACK_URL)
            .content(requestContent.toString())
            .header(AUTHORIZATION_HEADER, AUTH_TOKEN)
            .contentType(MediaType.APPLICATION_JSON_VALUE))
            .andExpect(status().isOk());

        verify(notificationService, times(1)).sendContestedApplicationIssuedEmail(any());
    }

    @Test
    public void shouldNotSendContestedApplicationIssuedEmailWhenNotAgreed() throws Exception {
        buildCcdRequest(CCD_REQUEST_JSON);
        mockMvc.perform(post(CONTESTED_APPLICATION_ISSUED_CALLBACK_URL)
            .content(requestContent.toString())
            .header(AUTHORIZATION_HEADER, AUTH_TOKEN)
            .contentType(MediaType.APPLICATION_JSON_VALUE))
            .andExpect(status().isOk());

        verifyNoInteractions(notificationService);
    }

    @Test
    public void shouldSendContestOrderApprovedEmailWhenAgreed() throws Exception {
        buildCcdRequest(CCD_REQUEST_WITH_SOL_EMAIL_CONSENT_JSON);
        mockMvc.perform(post(CONTEST_ORDER_APPROVED_CALLBACK_URL)
            .content(requestContent.toString())
            .header(AUTHORIZATION_HEADER, AUTH_TOKEN)
            .contentType(MediaType.APPLICATION_JSON_VALUE))
            .andExpect(status().isOk());

        verify(notificationService, times(1)).sendContestOrderApprovedEmail(any());
    }

    @Test
    public void shouldNotSendContestOrderApprovedEmailWhenNotAgreed() throws Exception {
        buildCcdRequest(CCD_REQUEST_JSON);
        mockMvc.perform(post(CONTEST_ORDER_APPROVED_CALLBACK_URL)
            .content(requestContent.toString())
            .header(AUTHORIZATION_HEADER, AUTH_TOKEN)
            .contentType(MediaType.APPLICATION_JSON_VALUE))
            .andExpect(status().isOk());

        verifyNoInteractions(notificationService);
    }

    @Test
    public void sendDraftOrderEmailWhenApplicantSolicitorIsNominatedAndIsAcceptingEmails() throws Exception {
        buildCcdRequest(DRAFT_ORDER_SUCCESSFUL_APPLICANT_SOL);
        mockMvc.perform(post(CONTESTED_DRAFT_ORDER_URL)
                .content(requestContent.toString())
                .contentType(MediaType.APPLICATION_JSON_VALUE))
                .andExpect(status().isOk());

        verify(notificationService, times(1))
                .sendSolicitorToDraftOrderEmail(any(CallbackRequest.class));
    }

    @Test
    public void shouldNotSendDraftOrderEmailAsSolicitorOptedOutOfEmailComms() throws Exception {
        buildCcdRequest(DRAFT_ORDER_UNSUCCESSFUL_APPLICANT_SOL);
        mockMvc.perform(post(CONTESTED_DRAFT_ORDER_URL)
                .content(requestContent.toString())
                .contentType(MediaType.APPLICATION_JSON_VALUE))
                .andExpect(status().isOk());

        verifyNoInteractions(notificationService);
    }

    @Test
    public void shouldNotSendDraftOrderEmailAsRespondentSolicitorIsNominated() throws Exception {
        buildCcdRequest(DRAFT_ORDER_UNSUCCESSFUL_RESPONDENT_SOL);
        mockMvc.perform(post(CONTESTED_DRAFT_ORDER_URL)
                .content(requestContent.toString())
                .contentType(MediaType.APPLICATION_JSON_VALUE))
                .andExpect(status().isOk());

        verifyNoInteractions(notificationService);
    }

    @Test
    public void sendGeneralEmailConsented() throws Exception {
        buildCcdRequest(GENERAL_EMAIL_CONSENTED);
        when(generalEmailService.storeGeneralEmail(any(CaseDetails.class)))
            .thenReturn(CaseDetails.builder().build());
        mockMvc.perform(post(GENERAL_EMAIL_URL)
            .content(requestContent.toString())
            .contentType(MediaType.APPLICATION_JSON_VALUE))
            .andExpect(status().isOk());

        verify(notificationService, times(1))
            .sendConsentGeneralEmail(any(CallbackRequest.class));

        verify(generalEmailService, times(1))
            .storeGeneralEmail(any(CaseDetails.class));
    }

    @Test
    public void sendGeneralEmailContested() throws Exception {
        buildCcdRequest(GENERAL_EMAIL_CONTESTED);
        when(generalEmailService.storeGeneralEmail(any(CaseDetails.class)))
            .thenReturn(CaseDetails.builder().build());
        mockMvc.perform(post(GENERAL_EMAIL_URL)
            .content(requestContent.toString())
            .contentType(MediaType.APPLICATION_JSON_VALUE))
            .andExpect(status().isOk());

        verify(notificationService, times(1))
            .sendContestedGeneralEmail(any(CallbackRequest.class));

        verify(generalEmailService, times(1))
            .storeGeneralEmail(any(CaseDetails.class));
    }

    private void buildCcdRequest(String fileName) throws IOException, URISyntaxException {
        ObjectMapper objectMapper = new ObjectMapper();
        requestContent = objectMapper.readTree(new File(getClass()
                .getResource(fileName).toURI()));
    }
}