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
import uk.gov.hmcts.reform.finrem.caseorchestration.service.AdditionalHearingDocumentService;
import uk.gov.hmcts.reform.finrem.caseorchestration.service.AssignedToJudgeDocumentService;
import uk.gov.hmcts.reform.finrem.caseorchestration.service.BulkPrintService;
import uk.gov.hmcts.reform.finrem.caseorchestration.service.GeneralEmailService;
import uk.gov.hmcts.reform.finrem.caseorchestration.service.HearingDocumentService;
import uk.gov.hmcts.reform.finrem.caseorchestration.service.HelpWithFeesDocumentService;
import uk.gov.hmcts.reform.finrem.caseorchestration.service.ManualPaymentDocumentService;
import uk.gov.hmcts.reform.finrem.caseorchestration.service.NotificationService;

import java.io.File;
import java.io.IOException;
import java.net.URISyntaxException;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.never;
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
    private static final String CONSENT_IN_CONTESTED_ASSIGN_TO_JUDGE_CALLBACK_URL = "/case-orchestration/notify/assign-to-judge-consent-in-contested";
    private static final String CONSENT_ORDER_MADE_URL = "/case-orchestration/notify/consent-order-made";
    private static final String ORDER_NOT_APPROVED_URL = "/case-orchestration/notify/order-not-approved";
    private static final String CONSENT_ORDER_AVAILABLE_URL = "/case-orchestration/notify/consent-order-available";
    private static final String PREPARE_FOR_HEARING_CALLBACK_URL = "/case-orchestration/notify/prepare-for-hearing";
    private static final String PREPARE_FOR_HEARING_ORDER_SENT_CALLBACK_URL = "/case-orchestration/notify/prepare-for-hearing-order-sent";
    private static final String CONTESTED_APPLICATION_ISSUED_CALLBACK_URL = "/case-orchestration/notify/contest-application-issued";
    private static final String CONTEST_ORDER_APPROVED_CALLBACK_URL = "/case-orchestration/notify/contest-order-approved";
    private static final String CONTESTED_CONSENT_ORDER_APPROVED_CALLBACK_URL = "/case-orchestration/notify/contested-consent-order-approved";
    private static final String CONTESTED_GENERAL_APPLICATION_REFER_TO_JUDGE_CALLBACK_URL =
        "/case-orchestration/notify/general-application-refer-to-judge";
    private static final String CONTESTED_GENERAL_APPLICATION_OUTCOME_CALLBACK_URL = "/case-orchestration/notify/general-application-outcome";
    private static final String GENERAL_ORDER_RAISED_CALLBACK_URL = "/case-orchestration/notify/general-order-raised";
    private static final String CONTESTED_CONSENT_ORDER_NOT_APPROVED_CALLBACK_URL = "/case-orchestration/notify/contested-consent-order-not-approved";
    private static final String CONTESTED_DRAFT_ORDER_URL = "/case-orchestration/notify/draft-order";
    private static final String GENERAL_EMAIL_URL = "/case-orchestration/notify/general-email";
    private static final String CONTESTED_MANUAL_PAYMENT_URL = "/case-orchestration/notify/manual-payment";

    //JSON Data
    private static final String CCD_REQUEST_JSON = "/fixtures/model/ccd-request.json";
    private static final String CONSENTED_SOL_SUBSCRIBED_FOR_EMAILS_JSON = "/fixtures/consented-ccd-request-with-solicitor-agreed-to-emails.json";
    private static final String CONTESTED_SOL_SUBSCRIBED_FOR_EMAILS_JSON = "/fixtures/contested-ccd-request-with-solicitor-agreed-to-emails.json";
    private static final String CONTESTED_CONSENT_SOL_SUBSCRIBED_FOR_EMAILS_JSON =
        "/fixtures/contested-consent-ccd-request-with-solicitor-agreed-to-emails.json";
    private static final String BULK_PRINT_PAPER_APPLICATION_JSON = "/fixtures/bulkprint/bulk-print-paper-application.json";
    private static final String DRAFT_ORDER_SUCCESSFUL_APPLICANT_SOL = "/fixtures/applicant-solicitor-to-draft-order-with-email-consent.json";
    private static final String DRAFT_ORDER_UNSUCCESSFUL_APPLICANT_SOL = "/fixtures/applicant-solicitor-to-draft-order-without-email-consent.json";
    private static final String DRAFT_ORDER_UNSUCCESSFUL_RESPONDENT_SOL = "/fixtures/respondent-solicitor-to-draft-order-with-email-consent.json";
    private static final String GENERAL_EMAIL_CONSENTED = "/fixtures/general-email-consented.json";
    private static final String GENERAL_EMAIL_CONTESTED = "/fixtures/contested/general-email-contested.json";
    private static final String CONTESTED_PAPER_CASE_JSON = "/fixtures/contested/paper-case.json";
    private static final String CONTESTED_PAPER_APPLICATION_HEARING_JSON =
        "/fixtures/contested/validate-hearing-with-fastTrackDecision-paperApplication.json";

    @Autowired private WebApplicationContext applicationContext;

    @MockBean private NotificationService notificationService;
    @MockBean private ManualPaymentDocumentService manualPaymentDocumentService;
    @MockBean private BulkPrintService bulkPrintService;
    @MockBean private AssignedToJudgeDocumentService assignedToJudgeDocumentService;
    @MockBean private GeneralEmailService generalEmailService;
    @MockBean private HelpWithFeesDocumentService helpWithFeesDocumentService;
    @MockBean private HearingDocumentService hearingDocumentService;
    @MockBean private AdditionalHearingDocumentService additionalHearingDocumentService;

    private MockMvc mockMvc;
    private JsonNode requestContent;

    @Before
    public void setUp() {
        mockMvc = MockMvcBuilders.webAppContextSetup(applicationContext).build();
    }

    @Test
    public void sendHwfSuccessfulConfirmationEmailIfDigitalCase() throws Exception {
        buildCcdRequest(CONSENTED_SOL_SUBSCRIBED_FOR_EMAILS_JSON);
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
    public void sendHwfSuccessfulNotificationLetterIfIsConsentedAndIsPaperApplication() throws Exception {
        buildCcdRequest(BULK_PRINT_PAPER_APPLICATION_JSON);

        mockMvc.perform(post(HWF_SUCCESSFUL_CALLBACK_URL)
            .header(AUTHORIZATION_HEADER, AUTH_TOKEN)
            .content(requestContent.toString())
            .contentType(MediaType.APPLICATION_JSON_VALUE))
            .andExpect(status().isOk());

        verify(helpWithFeesDocumentService, times(1))
            .generateHwfSuccessfulNotificationLetter(any(CaseDetails.class),any());
        verify(bulkPrintService, times(1))
            .sendDocumentForPrint(any(),any());
        verifyNoInteractions(notificationService);
    }

    @Test
    public void sendAssignToJudgeConfirmationEmailIfDigitalCase() throws Exception {
        buildCcdRequest(CONSENTED_SOL_SUBSCRIBED_FOR_EMAILS_JSON);
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
    public void sendAssignToJudgeNotificationLetterIfIsPaperApplication() throws Exception {
        buildCcdRequest(BULK_PRINT_PAPER_APPLICATION_JSON);

        mockMvc.perform(post(ASSIGN_TO_JUDGE_CALLBACK_URL)
            .header(AUTHORIZATION_HEADER, AUTH_TOKEN)
            .content(requestContent.toString())
            .contentType(MediaType.APPLICATION_JSON_VALUE))
            .andExpect(status().isOk());

        verify(assignedToJudgeDocumentService, times(1))
            .generateAssignedToJudgeNotificationLetter(any(CaseDetails.class),any());
        verify(bulkPrintService, times(1))
            .sendDocumentForPrint(any(),any());
        verifyNoInteractions(notificationService);
    }

    @Test
    public void shouldNotSendApplicantConsentInContestedAssignToJudgeConfirmationEmail() throws Exception {
        buildCcdRequest(CCD_REQUEST_JSON);

        when(bulkPrintService.shouldPrintForApplicant(any(CaseDetails.class))).thenReturn(false);

        mockMvc.perform(post(CONSENT_IN_CONTESTED_ASSIGN_TO_JUDGE_CALLBACK_URL)
            .header(AUTHORIZATION_HEADER, AUTH_TOKEN)
            .content(requestContent.toString())
            .contentType(MediaType.APPLICATION_JSON_VALUE))
            .andExpect(status().isOk());

        verify(assignedToJudgeDocumentService, never())
            .generateApplicantConsentInContestedAssignedToJudgeNotificationLetter(any(CaseDetails.class),any());
        verify(assignedToJudgeDocumentService, times(1))
            .generateRespondentConsentInContestedAssignedToJudgeNotificationLetter(any(CaseDetails.class),any());
        verify(bulkPrintService, times(1))
            .sendDocumentForPrint(any(),any());
    }

    @Test
    public void sendConsentInContestedAssignToJudgeNotificationLetterIfShouldSend() throws Exception {
        buildCcdRequest(BULK_PRINT_PAPER_APPLICATION_JSON);

        when(bulkPrintService.shouldPrintForApplicant(any(CaseDetails.class))).thenReturn(true);

        mockMvc.perform(post(CONSENT_IN_CONTESTED_ASSIGN_TO_JUDGE_CALLBACK_URL)
            .header(AUTHORIZATION_HEADER, AUTH_TOKEN)
            .content(requestContent.toString())
            .contentType(MediaType.APPLICATION_JSON_VALUE))
            .andExpect(status().isOk());

        verify(assignedToJudgeDocumentService, times(1))
            .generateApplicantConsentInContestedAssignedToJudgeNotificationLetter(any(CaseDetails.class),any());
        verify(assignedToJudgeDocumentService, times(1))
            .generateRespondentConsentInContestedAssignedToJudgeNotificationLetter(any(CaseDetails.class),any());
        verify(bulkPrintService, times(2))
            .sendDocumentForPrint(any(),any());
    }

    @Test
    public void sendConsentOrderMadeConfirmationEmail() throws Exception {
        buildCcdRequest(CONSENTED_SOL_SUBSCRIBED_FOR_EMAILS_JSON);
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
        buildCcdRequest(CONSENTED_SOL_SUBSCRIBED_FOR_EMAILS_JSON);
        mockMvc.perform(post(ORDER_NOT_APPROVED_URL)
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
        mockMvc.perform(post(ORDER_NOT_APPROVED_URL)
            .header(AUTHORIZATION_HEADER, AUTH_TOKEN)
            .content(requestContent.toString())
            .contentType(MediaType.APPLICATION_JSON_VALUE))
            .andExpect(status().isOk());

        verifyNoInteractions(notificationService);
    }

    @Test
    public void sendConsentOrderAvailableEmail() throws Exception {
        buildCcdRequest(CONSENTED_SOL_SUBSCRIBED_FOR_EMAILS_JSON);
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
    public void givenSolAgreedToEmails_and_noPreviousHearing_shouldSendPrepareForHearingEmail_and_PrintHearingDocuments() throws Exception {
        buildCcdRequest(CONTESTED_PAPER_APPLICATION_HEARING_JSON);
        mockMvc.perform(post(PREPARE_FOR_HEARING_CALLBACK_URL)
            .content(requestContent.toString())
            .header(AUTHORIZATION_HEADER, AUTH_TOKEN)
            .contentType(MediaType.APPLICATION_JSON_VALUE))
            .andExpect(status().isOk());

        verify(notificationService, times(1)).sendPrepareForHearingEmail(any());
        verify(hearingDocumentService, times(1)).sendFormCAndGForBulkPrint(any(), eq(AUTH_TOKEN));
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
    public void givenHadPreviousHearing_whenNotifyHearingInvoked_thenPrintAdditionalHearingDocuments() throws Exception {
        when(hearingDocumentService.alreadyHadFirstHearing(any())).thenReturn(true);

        buildCcdRequest(CONTESTED_PAPER_APPLICATION_HEARING_JSON);
        mockMvc.perform(post(PREPARE_FOR_HEARING_CALLBACK_URL)
            .content(requestContent.toString())
            .header(AUTHORIZATION_HEADER, AUTH_TOKEN)
            .contentType(MediaType.APPLICATION_JSON_VALUE))
            .andExpect(status().isOk());

        verify(additionalHearingDocumentService, times(1)).sendAdditionalHearingDocuments(eq(AUTH_TOKEN), any());
    }

    @Test
    public void shouldSendPrepareForHearingOrderSentEmailWhenAgreed() throws Exception {
        buildCcdRequest(CONTESTED_SOL_SUBSCRIBED_FOR_EMAILS_JSON);
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
        buildCcdRequest(CONTESTED_SOL_SUBSCRIBED_FOR_EMAILS_JSON);
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
        buildCcdRequest(CONTESTED_SOL_SUBSCRIBED_FOR_EMAILS_JSON);
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
                .sendSolicitorToDraftOrderEmailApplicant(any(CallbackRequest.class));
    }

    @Test
    public void shouldNotSendDraftOrderEmailAsSolicitorOptedOutOfEmailComms() throws Exception {
        buildCcdRequest(DRAFT_ORDER_UNSUCCESSFUL_APPLICANT_SOL);
        mockMvc.perform(post(CONTESTED_DRAFT_ORDER_URL)
                .content(requestContent.toString())
                .contentType(MediaType.APPLICATION_JSON_VALUE))
                .andExpect(status().isOk());

        verify(notificationService, never()).sendSolicitorToDraftOrderEmailApplicant(any());
    }

    @Test
    public void shouldNotSendDraftOrderEmailAsRespondentSolicitorIsNominated() throws Exception {
        buildCcdRequest(DRAFT_ORDER_UNSUCCESSFUL_RESPONDENT_SOL);
        mockMvc.perform(post(CONTESTED_DRAFT_ORDER_URL)
                .content(requestContent.toString())
                .contentType(MediaType.APPLICATION_JSON_VALUE))
                .andExpect(status().isOk());

        verify(notificationService, never()).sendSolicitorToDraftOrderEmailRespondent(any());
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

    @Test
    public void sendContestOrderNotApprovedEmail() throws Exception {
        buildCcdRequest(CONTESTED_SOL_SUBSCRIBED_FOR_EMAILS_JSON);
        mockMvc.perform(post(ORDER_NOT_APPROVED_URL)
            .header(AUTHORIZATION_HEADER, AUTH_TOKEN)
            .content(requestContent.toString())
            .contentType(MediaType.APPLICATION_JSON_VALUE))
            .andExpect(status().isOk());

        verify(notificationService, times(1))
            .sendContestOrderNotApprovedEmail(any(CallbackRequest.class));
    }

    @Test
    public void shouldNotSendContestOrderNotApprovedEmail() throws Exception {
        buildCcdRequest(CCD_REQUEST_JSON);
        mockMvc.perform(post(ORDER_NOT_APPROVED_URL)
            .header(AUTHORIZATION_HEADER, AUTH_TOKEN)
            .content(requestContent.toString())
            .contentType(MediaType.APPLICATION_JSON_VALUE))
            .andExpect(status().isOk());

        verifyNoInteractions(notificationService);
    }

    @Test
    public void sendContestedConsentOrderApprovedEmail() throws Exception {
        buildCcdRequest(CONTESTED_SOL_SUBSCRIBED_FOR_EMAILS_JSON);
        mockMvc.perform(post(CONTESTED_CONSENT_ORDER_APPROVED_CALLBACK_URL)
            .header(AUTHORIZATION_HEADER, AUTH_TOKEN)
            .content(requestContent.toString())
            .contentType(MediaType.APPLICATION_JSON_VALUE))
            .andExpect(status().isOk());

        verify(notificationService, times(1))
            .sendContestedConsentOrderApprovedEmail(any(CallbackRequest.class));
    }

    @Test
    public void shouldNotSendGeneralOrderEmail() throws Exception {
        buildCcdRequest(CCD_REQUEST_JSON);
        mockMvc.perform(post(GENERAL_ORDER_RAISED_CALLBACK_URL)
            .header(AUTHORIZATION_HEADER, AUTH_TOKEN)
            .content(requestContent.toString())
            .contentType(MediaType.APPLICATION_JSON_VALUE))
            .andExpect(status().isOk());

        verifyNoInteractions(notificationService);
    }

    @Test
    public void sendContestedConsentGeneralOrderEmail() throws Exception {
        buildCcdRequest(CONTESTED_CONSENT_SOL_SUBSCRIBED_FOR_EMAILS_JSON);
        mockMvc.perform(post(GENERAL_ORDER_RAISED_CALLBACK_URL)
            .header(AUTHORIZATION_HEADER, AUTH_TOKEN)
            .content(requestContent.toString())
            .contentType(MediaType.APPLICATION_JSON_VALUE))
            .andExpect(status().isOk());

        verify(notificationService, times(1))
            .sendContestedConsentGeneralOrderEmail(any(CallbackRequest.class));
    }

    @Test
    public void sendContestedGeneralOrderEmail() throws Exception {
        buildCcdRequest(CONTESTED_SOL_SUBSCRIBED_FOR_EMAILS_JSON);
        mockMvc.perform(post(GENERAL_ORDER_RAISED_CALLBACK_URL)
            .header(AUTHORIZATION_HEADER, AUTH_TOKEN)
            .content(requestContent.toString())
            .contentType(MediaType.APPLICATION_JSON_VALUE))
            .andExpect(status().isOk());

        verify(notificationService, times(1))
            .sendContestedGeneralOrderEmail(any(CallbackRequest.class));
    }

    @Test
    public void sendConsentedGeneralOrderEmail() throws Exception {
        buildCcdRequest(CONSENTED_SOL_SUBSCRIBED_FOR_EMAILS_JSON);
        mockMvc.perform(post(GENERAL_ORDER_RAISED_CALLBACK_URL)
            .header(AUTHORIZATION_HEADER, AUTH_TOKEN)
            .content(requestContent.toString())
            .contentType(MediaType.APPLICATION_JSON_VALUE))
            .andExpect(status().isOk());

        verify(notificationService, times(1))
            .sendConsentedGeneralOrderEmail(any(CallbackRequest.class));
    }

    @Test
    public void shouldNotSendContestedConsentOrderApprovedEmail() throws Exception {
        buildCcdRequest(CCD_REQUEST_JSON);
        mockMvc.perform(post(CONTESTED_CONSENT_ORDER_APPROVED_CALLBACK_URL)
            .header(AUTHORIZATION_HEADER, AUTH_TOKEN)
            .content(requestContent.toString())
            .contentType(MediaType.APPLICATION_JSON_VALUE))
            .andExpect(status().isOk());

        verifyNoInteractions(notificationService);
    }

    @Test
    public void sendContestedGeneralApplicationReferToJudgeEmail() throws Exception {
        buildCcdRequest(CCD_REQUEST_JSON);
        mockMvc.perform(post(CONTESTED_GENERAL_APPLICATION_REFER_TO_JUDGE_CALLBACK_URL)
            .header(AUTHORIZATION_HEADER, AUTH_TOKEN)
            .content(requestContent.toString())
            .contentType(MediaType.APPLICATION_JSON_VALUE))
            .andExpect(status().isOk());

        verify(notificationService, times(1))
            .sendContestedGeneralApplicationReferToJudgeEmail(any(CallbackRequest.class));
    }

    @Test
    public void sendContestedConsentOrderNotApprovedEmail() throws Exception {
        buildCcdRequest(CONTESTED_SOL_SUBSCRIBED_FOR_EMAILS_JSON);
        mockMvc.perform(post(CONTESTED_CONSENT_ORDER_NOT_APPROVED_CALLBACK_URL)
            .header(AUTHORIZATION_HEADER, AUTH_TOKEN)
            .content(requestContent.toString())
            .contentType(MediaType.APPLICATION_JSON_VALUE))
            .andExpect(status().isOk());

        verify(notificationService, times(1))
            .sendContestedConsentOrderNotApprovedEmail(any(CallbackRequest.class));
    }

    @Test
    public void shouldNotSendContestedConsentOrderNotApprovedEmail() throws Exception {
        buildCcdRequest(CCD_REQUEST_JSON);
        mockMvc.perform(post(CONTESTED_CONSENT_ORDER_NOT_APPROVED_CALLBACK_URL)
            .header(AUTHORIZATION_HEADER, AUTH_TOKEN)
            .content(requestContent.toString())
            .contentType(MediaType.APPLICATION_JSON_VALUE))
            .andExpect(status().isOk());

        verifyNoInteractions(notificationService);
    }

    @Test
    public void sendContestedManualPaymentLetters() throws Exception {
        buildCcdRequest(CONTESTED_PAPER_CASE_JSON);
        mockMvc.perform(post(CONTESTED_MANUAL_PAYMENT_URL)
            .header(AUTHORIZATION_HEADER, AUTH_TOKEN)
            .content(requestContent.toString())
            .contentType(MediaType.APPLICATION_JSON_VALUE))
            .andExpect(status().isOk());

        verify(manualPaymentDocumentService, times(1))
            .generateApplicantManualPaymentLetter(any(CaseDetails.class), any());
        verify(bulkPrintService, times(1))
            .sendDocumentForPrint(any(),any());
        verifyNoInteractions(notificationService);
    }

    @Test
    public void sendContestedGeneralApplicationOutcomeEmail() throws Exception {
        buildCcdRequest(CONTESTED_SOL_SUBSCRIBED_FOR_EMAILS_JSON);
        mockMvc.perform(post(CONTESTED_GENERAL_APPLICATION_OUTCOME_CALLBACK_URL)
            .header(AUTHORIZATION_HEADER, AUTH_TOKEN)
            .content(requestContent.toString())
            .contentType(MediaType.APPLICATION_JSON_VALUE))
            .andExpect(status().isOk());

        verify(notificationService, times(1))
            .sendContestedGeneralApplicationOutcomeEmail(any(CallbackRequest.class));
    }

    private void buildCcdRequest(String fileName) throws IOException, URISyntaxException {
        ObjectMapper objectMapper = new ObjectMapper();
        requestContent = objectMapper.readTree(new File(getClass()
                .getResource(fileName).toURI()));
    }
}
