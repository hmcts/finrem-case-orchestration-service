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
import uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.CaseDocument;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.HearingOrderCollectionData;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.HearingOrderDocument;
import uk.gov.hmcts.reform.finrem.caseorchestration.service.AdditionalHearingDocumentService;
import uk.gov.hmcts.reform.finrem.caseorchestration.service.AssignedToJudgeDocumentService;
import uk.gov.hmcts.reform.finrem.caseorchestration.service.BulkPrintService;
import uk.gov.hmcts.reform.finrem.caseorchestration.service.CaseDataService;
import uk.gov.hmcts.reform.finrem.caseorchestration.service.FeatureToggleService;
import uk.gov.hmcts.reform.finrem.caseorchestration.service.GeneralEmailService;
import uk.gov.hmcts.reform.finrem.caseorchestration.service.HearingDocumentService;
import uk.gov.hmcts.reform.finrem.caseorchestration.service.HelpWithFeesDocumentService;
import uk.gov.hmcts.reform.finrem.caseorchestration.service.ManualPaymentDocumentService;
import uk.gov.hmcts.reform.finrem.caseorchestration.service.NotificationService;

import java.io.File;
import java.io.IOException;
import java.net.URISyntaxException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

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
import static uk.gov.hmcts.reform.finrem.caseorchestration.OrchestrationConstants.CASE_TYPE_ID_CONSENTED;
import static uk.gov.hmcts.reform.finrem.caseorchestration.OrchestrationConstants.CASE_TYPE_ID_CONTESTED;
import static uk.gov.hmcts.reform.finrem.caseorchestration.OrchestrationConstants.YES_VALUE;
import static uk.gov.hmcts.reform.finrem.caseorchestration.TestConstants.AUTH_TOKEN;
import static uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.CCDConfigConstant.APP_SOLICITOR_AGREE_TO_RECEIVE_EMAILS_CONTESTED;
import static uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.CCDConfigConstant.CONSENT_D81_QUESTION;
import static uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.CCDConfigConstant.FINAL_ORDER_COLLECTION;

@RunWith(SpringRunner.class)
@WebMvcTest(NotificationsController.class)
public class NotificationsControllerTest extends BaseControllerTest {

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
    @Autowired private NotificationsController notificationsController;

    @MockBean private NotificationService notificationService;
    @MockBean private ManualPaymentDocumentService manualPaymentDocumentService;
    @MockBean private BulkPrintService bulkPrintService;
    @MockBean private AssignedToJudgeDocumentService assignedToJudgeDocumentService;
    @MockBean private GeneralEmailService generalEmailService;
    @MockBean private HelpWithFeesDocumentService helpWithFeesDocumentService;
    @MockBean private HearingDocumentService hearingDocumentService;
    @MockBean private AdditionalHearingDocumentService additionalHearingDocumentService;
    @MockBean private CaseDataService caseDataService;
    @MockBean private FeatureToggleService featureToggleService;

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
            .sendConsentedHWFSuccessfulConfirmationEmail(any());
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
            .generateHwfSuccessfulNotificationLetter(any(CaseDetails.class), any());
        verify(bulkPrintService, times(1))
            .sendDocumentForPrint(any(), any());
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
            .sendAssignToJudgeConfirmationEmailToApplicantSolicitor(any());
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
            .generateAssignedToJudgeNotificationLetter(any(CaseDetails.class), any());
        verify(bulkPrintService, times(1))
            .sendDocumentForPrint(any(), any());
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
            .generateApplicantConsentInContestedAssignedToJudgeNotificationLetter(any(CaseDetails.class), any());
        verify(assignedToJudgeDocumentService, times(1))
            .generateRespondentConsentInContestedAssignedToJudgeNotificationLetter(any(CaseDetails.class), any());
        verify(bulkPrintService, times(1))
            .sendDocumentForPrint(any(), any());
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
            .generateApplicantConsentInContestedAssignedToJudgeNotificationLetter(any(), any());
        verify(assignedToJudgeDocumentService, times(1))
            .generateRespondentConsentInContestedAssignedToJudgeNotificationLetter(any(), any());
        verify(bulkPrintService, times(2))
            .sendDocumentForPrint(any(), any());
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
            .sendConsentOrderMadeConfirmationEmail(any());
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
            .sendConsentOrderNotApprovedEmail(any());
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
            .sendConsentOrderAvailableEmail(any());
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
            .sendContestedHwfSuccessfulConfirmationEmail(any());
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
        when(featureToggleService.isRespondentSolicitorEmailNotificationEnabled()).thenReturn(true);
        when(notificationService.shouldEmailRespondentSolicitor(any())).thenReturn(true);

        buildCcdRequest(CONTESTED_PAPER_APPLICATION_HEARING_JSON);
        mockMvc.perform(post(PREPARE_FOR_HEARING_CALLBACK_URL)
            .content(requestContent.toString())
            .header(AUTHORIZATION_HEADER, AUTH_TOKEN)
            .contentType(MediaType.APPLICATION_JSON_VALUE))
            .andExpect(status().isOk());

        verify(notificationService, times(1)).sendPrepareForHearingEmailApplicant(any());
        verify(notificationService, times(1)).sendPrepareForHearingEmailRespondent(any());
        verify(hearingDocumentService, times(1)).sendFormCAndGForBulkPrint(any(), eq(AUTH_TOKEN));
    }

    @Test
    public void shouldNotSendPrepareForHearingEmailWhenNotAgreed() throws Exception {
        when(featureToggleService.isRespondentSolicitorEmailNotificationEnabled()).thenReturn(true);
        when(notificationService.shouldEmailRespondentSolicitor(any())).thenReturn(false);

        buildCcdRequest(CCD_REQUEST_JSON);
        mockMvc.perform(post(PREPARE_FOR_HEARING_CALLBACK_URL)
            .content(requestContent.toString())
            .header(AUTHORIZATION_HEADER, AUTH_TOKEN)
            .contentType(MediaType.APPLICATION_JSON_VALUE))
            .andExpect(status().isOk());

        verify(notificationService, never()).sendPrepareForHearingEmailApplicant(any());
        verify(notificationService, never()).sendPrepareForHearingEmailRespondent(any());
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

        verify(notificationService, times(1)).sendPrepareForHearingOrderSentEmailApplicant(any());
    }

    @Test
    public void shouldNotSendPrepareForHearingOrderSentEmailWhenNotAgreed() throws Exception {
        buildCcdRequest(CCD_REQUEST_JSON);
        mockMvc.perform(post(PREPARE_FOR_HEARING_ORDER_SENT_CALLBACK_URL)
            .content(requestContent.toString())
            .header(AUTHORIZATION_HEADER, AUTH_TOKEN)
            .contentType(MediaType.APPLICATION_JSON_VALUE))
            .andExpect(status().isOk());

        verify(notificationService, never()).sendPrepareForHearingOrderSentEmailApplicant(any());
    }

    @Test
    public void sendPrepareForHearingOrderSentEmail_shouldSendRespondentEmail() {
        when(notificationService.shouldEmailRespondentSolicitor(any())).thenReturn(true);
        when(featureToggleService.isRespondentSolicitorEmailNotificationEnabled()).thenReturn(true);

        notificationsController.sendPrepareForHearingOrderSentEmail(buildCallbackRequest());

        verify(notificationService, times(1)).sendPrepareForHearingOrderSentEmailRespondent(any());
    }

    @Test
    public void sendPrepareForHearingOrderSentEmail_shouldNotSendRespondentEmail() {
        when(notificationService.shouldEmailRespondentSolicitor(any())).thenReturn(false);
        when(featureToggleService.isRespondentSolicitorEmailNotificationEnabled()).thenReturn(true);

        notificationsController.sendPrepareForHearingOrderSentEmail(buildCallbackRequest());

        verify(notificationService, never()).sendPrepareForHearingOrderSentEmailRespondent(any());
    }

    @Test
    public void sendPrepareForHearingOrderSentEmail_toggledOff() {
        when(notificationService.shouldEmailRespondentSolicitor(any())).thenReturn(true);
        when(featureToggleService.isRespondentSolicitorEmailNotificationEnabled()).thenReturn(false);

        notificationsController.sendPrepareForHearingOrderSentEmail(buildCallbackRequest());

        verify(notificationService, never()).sendPrepareForHearingOrderSentEmailRespondent(any());
    }

    @Test
    public void shouldSendContestedApplicationIssuedEmailWhenAgreed() throws Exception {
        buildCcdRequest(CONTESTED_SOL_SUBSCRIBED_FOR_EMAILS_JSON);
        mockMvc.perform(post(CONTESTED_APPLICATION_ISSUED_CALLBACK_URL)
            .content(requestContent.toString())
            .header(AUTHORIZATION_HEADER, AUTH_TOKEN)
            .contentType(MediaType.APPLICATION_JSON_VALUE))
            .andExpect(status().isOk());

        verify(notificationService, times(1)).sendContestedApplicationIssuedEmailToApplicantSolicitor(any());
    }

    @Test
    public void shouldNotSendContestedApplicationIssuedEmailWhenNotAgreed() throws Exception {
        buildCcdRequest(CCD_REQUEST_JSON);
        mockMvc.perform(post(CONTESTED_APPLICATION_ISSUED_CALLBACK_URL)
            .content(requestContent.toString())
            .header(AUTHORIZATION_HEADER, AUTH_TOKEN)
            .contentType(MediaType.APPLICATION_JSON_VALUE))
            .andExpect(status().isOk());

        verify(notificationService, never()).sendContestedApplicationIssuedEmailToApplicantSolicitor(any());
    }

    @Test
    public void shouldSendContestedApplicationIssuedEmailWhenAgreed_andNotifyRespondentSolicitorWhenShould() {
        when(featureToggleService.isRespondentSolicitorEmailNotificationEnabled()).thenReturn(true);
        when(notificationService.shouldEmailRespondentSolicitor(any())).thenReturn(true);

        notificationsController.sendContestedApplicationIssuedEmail(buildCallbackRequest());

        verify(notificationService, times(1)).sendContestedApplicationIssuedEmailToRespondentSolicitor(any());
    }

    @Test
    public void shouldSendContestedApplicationIssuedEmailWhenAgreed_andNotSendRespondentNotificationWhenToggledOff() {
        when(featureToggleService.isRespondentSolicitorEmailNotificationEnabled()).thenReturn(false);
        when(notificationService.shouldEmailRespondentSolicitor(any())).thenReturn(true);

        notificationsController.sendContestedApplicationIssuedEmail(buildCallbackRequest());

        verify(notificationService, never()).sendContestedApplicationIssuedEmailToRespondentSolicitor(any());
    }

    @Test
    public void shouldNotSendContestedApplicationIssuedEmailWhenNotAgreed_andDontNotifyRespondentSolicitorWhenShouldNot() {
        when(featureToggleService.isRespondentSolicitorEmailNotificationEnabled()).thenReturn(true);
        when(notificationService.shouldEmailRespondentSolicitor(any())).thenReturn(false);

        notificationsController.sendContestedApplicationIssuedEmail(buildCallbackRequest());

        verify(notificationService, never()).sendContestedApplicationIssuedEmailToRespondentSolicitor(any());
    }

    @Test
    public void shouldSendContestOrderApprovedEmailWhenAgreed() throws Exception {
        buildCcdRequest(CONTESTED_SOL_SUBSCRIBED_FOR_EMAILS_JSON);
        mockMvc.perform(post(CONTEST_ORDER_APPROVED_CALLBACK_URL)
            .content(requestContent.toString())
            .header(AUTHORIZATION_HEADER, AUTH_TOKEN)
            .contentType(MediaType.APPLICATION_JSON_VALUE))
            .andExpect(status().isOk());

        verify(notificationService, times(1)).sendContestOrderApprovedEmailApplicant(any());
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
    public void shouldSendContestOrderApprovedEmailWhenAgreed_andNotifyRespondentSolicitorWhenShould() {
        when(featureToggleService.isRespondentSolicitorEmailNotificationEnabled()).thenReturn(true);
        when(notificationService.shouldEmailRespondentSolicitor(any())).thenReturn(true);

        notificationsController.sendContestOrderApprovedEmail(createCallbackRequestWithFinalOrder());

        verify(notificationService, times(1)).sendContestOrderApprovedEmailRespondent(any());
    }

    @Test
    public void shouldSendContestOrderApprovedEmailWhenAgreed_andNotSendRespondentNotificationWhenToggledOff() {
        when(featureToggleService.isRespondentSolicitorEmailNotificationEnabled()).thenReturn(false);
        when(notificationService.shouldEmailRespondentSolicitor(any())).thenReturn(true);

        notificationsController.sendContestOrderApprovedEmail(createCallbackRequestWithFinalOrder());

        verify(notificationService, never()).sendContestOrderApprovedEmailRespondent(any());
    }

    @Test
    public void shouldNotSendContestOrderApprovedEmailWhenNotAgreed_andDontNotifyRespondentSolicitor() {
        when(featureToggleService.isRespondentSolicitorEmailNotificationEnabled()).thenReturn(true);
        when(notificationService.shouldEmailRespondentSolicitor(any())).thenReturn(false);

        notificationsController.sendContestOrderApprovedEmail(createCallbackRequestWithFinalOrder());

        verify(notificationService, never()).sendContestOrderApprovedEmailRespondent(any());
    }


    @Test
    public void sendDraftOrderEmailWhenApplicantSolicitorIsNominatedAndIsAcceptingEmails() throws Exception {
        buildCcdRequest(DRAFT_ORDER_SUCCESSFUL_APPLICANT_SOL);
        mockMvc.perform(post(CONTESTED_DRAFT_ORDER_URL)
            .content(requestContent.toString())
            .contentType(MediaType.APPLICATION_JSON_VALUE))
            .andExpect(status().isOk());

        verify(notificationService, times(1))
            .sendSolicitorToDraftOrderEmailApplicant(any());
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

        verify(notificationService, never()).sendSolicitorToDraftOrderEmailApplicant(any());
    }

    @Test
    public void shouldSendSolicitorToDraftOrderEmailRespondent() {
        when(notificationService.shouldEmailRespondentSolicitor(any())).thenReturn(true);
        when(caseDataService.isRespondentSolicitorResponsibleToDraftOrder(any())).thenReturn(true);
        when(featureToggleService.isRespondentSolicitorEmailNotificationEnabled()).thenReturn(true);

        notificationsController.sendDraftOrderEmail(buildCallbackRequest());

        verify(notificationService, times(1)).sendSolicitorToDraftOrderEmailRespondent(any());
    }

    @Test
    public void shouldSendSolicitorToDraftOrderEmailRespondent_shouldNotSendEmail() {
        when(notificationService.shouldEmailRespondentSolicitor(any())).thenReturn(false);
        when(caseDataService.isRespondentSolicitorResponsibleToDraftOrder(any())).thenReturn(true);
        when(featureToggleService.isRespondentSolicitorEmailNotificationEnabled()).thenReturn(true);

        notificationsController.sendDraftOrderEmail(buildCallbackRequest());

        verify(notificationService, never()).sendSolicitorToDraftOrderEmailRespondent(any());
    }

    @Test
    public void shouldSendSolicitorToDraftOrderEmailRespondent_respSolicitorNotResponsible() {
        when(notificationService.shouldEmailRespondentSolicitor(any())).thenReturn(true);
        when(caseDataService.isRespondentSolicitorResponsibleToDraftOrder(any())).thenReturn(false);
        when(featureToggleService.isRespondentSolicitorEmailNotificationEnabled()).thenReturn(true);

        notificationsController.sendDraftOrderEmail(buildCallbackRequest());

        verify(notificationService, never()).sendSolicitorToDraftOrderEmailRespondent(any());
    }

    @Test
    public void shouldSendSolicitorToDraftOrderEmailRespondent_toggledOff() {
        when(notificationService.shouldEmailRespondentSolicitor(any())).thenReturn(true);
        when(caseDataService.isRespondentSolicitorResponsibleToDraftOrder(any())).thenReturn(true);
        when(featureToggleService.isRespondentSolicitorEmailNotificationEnabled()).thenReturn(false);

        notificationsController.sendDraftOrderEmail(buildCallbackRequest());

        verify(notificationService, never()).sendSolicitorToDraftOrderEmailRespondent(any());
    }

    @Test
    public void sendGeneralEmailConsented() throws Exception {
        buildCcdRequest(GENERAL_EMAIL_CONSENTED);
        mockMvc.perform(post(GENERAL_EMAIL_URL)
            .content(requestContent.toString())
            .contentType(MediaType.APPLICATION_JSON_VALUE))
            .andExpect(status().isOk());

        verify(notificationService, times(1))
            .sendConsentGeneralEmail(any());

        verify(generalEmailService, times(1))
            .storeGeneralEmail(any());
    }

    @Test
    public void sendGeneralEmailContested() throws Exception {
        buildCcdRequest(GENERAL_EMAIL_CONTESTED);
        mockMvc.perform(post(GENERAL_EMAIL_URL)
            .content(requestContent.toString())
            .contentType(MediaType.APPLICATION_JSON_VALUE))
            .andExpect(status().isOk());

        verify(notificationService, times(1))
            .sendContestedGeneralEmail(any());

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
            .sendContestOrderNotApprovedEmailApplicant(any());
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
    public void sendContestedConsentOrderApprovedEmailToApplicantSolicitor() {
        Map<String, Object> caseData = new HashMap<>();
        caseData.put(APP_SOLICITOR_AGREE_TO_RECEIVE_EMAILS_CONTESTED, YES_VALUE);
        CaseDetails caseDetails = CaseDetails.builder().caseTypeId(CASE_TYPE_ID_CONTESTED).id(123L).data(caseData).build();
        CallbackRequest testData = CallbackRequest.builder().caseDetails(caseDetails).build();

        notificationsController.sendContestedConsentOrderApprovedEmail(testData);

        verify(notificationService, times(1))
            .sendContestedConsentOrderApprovedEmailToApplicantSolicitor(any());
    }

    @Test
    public void sendContestedConsentOrderApprovedEmailToRespondentSolicitor() {
        when(featureToggleService.isRespondentSolicitorEmailNotificationEnabled()).thenReturn(true);
        when(notificationService.shouldEmailRespondentSolicitor(any())).thenReturn(true);

        notificationsController.sendContestedConsentOrderApprovedEmail(buildCallbackRequest());

        verify(notificationService, times(1))
            .sendContestedConsentOrderApprovedEmailToRespondentSolicitor(any());
    }

    public void whenShouldSendRespondentNotification_andCaseIsContested_thenShouldTriggerRespondentEmail() {
        when(featureToggleService.isRespondentSolicitorEmailNotificationEnabled()).thenReturn(true);
        when(notificationService.shouldEmailRespondentSolicitor(any())).thenReturn(true);

        CallbackRequest callbackRequest = buildCallbackRequest();
        callbackRequest.getCaseDetails().setCaseTypeId(CASE_TYPE_ID_CONTESTED);
        notificationsController.sendConsentOrderNotApprovedEmail(callbackRequest);

        verify(notificationService, times(1)).sendContestOrderNotApprovedEmailRespondent(any());
    }

    @Test
    public void whenShouldSendRespondentNotification_andCaseIsConsented_thenShouldNotTriggerRespondentEmail() {
        when(featureToggleService.isRespondentSolicitorEmailNotificationEnabled()).thenReturn(true);
        when(notificationService.shouldEmailRespondentSolicitor(any())).thenReturn(true);

        CallbackRequest callbackRequest = buildCallbackRequest();
        callbackRequest.getCaseDetails().setCaseTypeId(CASE_TYPE_ID_CONSENTED);
        notificationsController.sendConsentOrderNotApprovedEmail(callbackRequest);

        verify(notificationService, never()).sendContestOrderNotApprovedEmailRespondent(any());
    }

    @Test
    public void sendContestedConsentOrderApprovedEmail() throws Exception {
        buildCcdRequest(CONTESTED_SOL_SUBSCRIBED_FOR_EMAILS_JSON);
        mockMvc.perform(post(CONTESTED_CONSENT_ORDER_APPROVED_CALLBACK_URL)
            .header(AUTHORIZATION_HEADER, AUTH_TOKEN)
            .content(requestContent.toString())
            .contentType(MediaType.APPLICATION_JSON_VALUE))
            .andExpect(status().isOk());
    }

    @Test
    public void shouldNotSendContestedConsentOrderApprovedEmailToRespondentSolicitorWhenRespSolShouldNotReceiveEmail() {
        when(featureToggleService.isRespondentSolicitorEmailNotificationEnabled()).thenReturn(true);
        when(notificationService.shouldEmailRespondentSolicitor(any())).thenReturn(false);

        notificationsController.sendContestedConsentOrderApprovedEmail(buildCallbackRequest());

        verify(notificationService,
            never()).sendContestedConsentOrderApprovedEmailToRespondentSolicitor(any());
    }

    @Test
    public void shouldNotSendContestedConsentOrderApprovedEmailToRespondentSolicitorWhenRespSolNotificationsToggledOff() {
        when(featureToggleService.isRespondentSolicitorEmailNotificationEnabled()).thenReturn(false);
        when(notificationService.shouldEmailRespondentSolicitor(any())).thenReturn(true);

        notificationsController.sendContestedConsentOrderApprovedEmail(buildCallbackRequest());

        verify(notificationService,
            never()).sendContestedConsentOrderApprovedEmailToRespondentSolicitor(any());
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
            .sendContestedConsentGeneralOrderEmailApplicantSolicitor(any());
    }

    @Test
    public void sendContestedGeneralOrderEmails() throws Exception {
        when(featureToggleService.isRespondentSolicitorEmailNotificationEnabled()).thenReturn(true);
        when(notificationService.shouldEmailRespondentSolicitor(any())).thenReturn(true);

        buildCcdRequest(CONTESTED_SOL_SUBSCRIBED_FOR_EMAILS_JSON);
        mockMvc.perform(post(GENERAL_ORDER_RAISED_CALLBACK_URL)
            .header(AUTHORIZATION_HEADER, AUTH_TOKEN)
            .content(requestContent.toString())
            .contentType(MediaType.APPLICATION_JSON_VALUE))
            .andExpect(status().isOk());

        verify(notificationService, times(1)).sendContestedGeneralOrderEmailApplicant(any());
        verify(notificationService, times(1)).sendContestedGeneralOrderEmailRespondent(any());
    }

    @Test
    public void whenShouldNotSendContestedGeneralOrderEmailToRespondent_ThenTheEmailIsNotIssued() {
        when(featureToggleService.isRespondentSolicitorEmailNotificationEnabled()).thenReturn(true);
        when(notificationService.shouldEmailRespondentSolicitor(any())).thenReturn(false);

        notificationsController.sendGeneralOrderRaisedEmail(buildCallbackRequest());

        verify(notificationService, never()).sendContestedConsentGeneralOrderEmailRespondentSolicitor(any());
        verify(notificationService, never()).sendContestedGeneralOrderEmailRespondent(any());
    }

    @Test
    public void whenToggleEnabledAndShouldSendEmailToRespSolicitor_thenSendsEmail() {
        when(featureToggleService.isRespondentSolicitorEmailNotificationEnabled()).thenReturn(true);
        when(notificationService.shouldEmailRespondentSolicitor(any())).thenReturn(true);

        notificationsController.sendAssignToJudgeConfirmationEmail(AUTH_TOKEN, buildCallbackRequest());

        verify(notificationService).sendAssignToJudgeConfirmationEmailToRespondentSolicitor(any());
    }

    @Test
    public void whenToggleEnabledAndShouldNotSendEmailToRespSolicitor_thenDoesNotSendEmail() {
        when(featureToggleService.isRespondentSolicitorEmailNotificationEnabled()).thenReturn(true);
        when(notificationService.shouldEmailRespondentSolicitor(any())).thenReturn(false);

        notificationsController.sendAssignToJudgeConfirmationEmail(AUTH_TOKEN, buildCallbackRequest());

        verify(notificationService, never()).sendAssignToJudgeConfirmationEmailToRespondentSolicitor(any());
    }

    @Test
    public void whenToggleDisabledAndShouldSendEmailToRespSolicitor_thenDoesNotSendEmail() {
        when(featureToggleService.isRespondentSolicitorEmailNotificationEnabled()).thenReturn(false);
        when(notificationService.shouldEmailRespondentSolicitor(any())).thenReturn(true);

        notificationsController.sendAssignToJudgeConfirmationEmail(AUTH_TOKEN, buildCallbackRequest());

        verify(notificationService, never()).sendAssignToJudgeConfirmationEmailToRespondentSolicitor(any());
    }

    @Test
    public void whenToggleDisabledAndShouldNotSendEmailToRespSolicitor_thenDoesNotSendEmail() {
        when(featureToggleService.isRespondentSolicitorEmailNotificationEnabled()).thenReturn(false);
        when(notificationService.shouldEmailRespondentSolicitor(any())).thenReturn(false);

        notificationsController.sendAssignToJudgeConfirmationEmail(AUTH_TOKEN, buildCallbackRequest());

        verify(notificationService, never()).sendAssignToJudgeConfirmationEmailToRespondentSolicitor(any());
    }

    @Test
    public void shouldSendContestedConsentGeneralOrderEmailToRespondentInConsentedInContestedCase() {
        when(featureToggleService.isRespondentSolicitorEmailNotificationEnabled()).thenReturn(true);
        when(notificationService.shouldEmailRespondentSolicitor(any())).thenReturn(true);

        CallbackRequest callbackRequest = buildCallbackRequest();
        callbackRequest.getCaseDetails().setCaseTypeId(CASE_TYPE_ID_CONTESTED);
        callbackRequest.getCaseDetails().getData().put(CONSENT_D81_QUESTION, YES_VALUE);
        notificationsController.sendGeneralOrderRaisedEmail(callbackRequest);

        verify(notificationService, times(1)).sendContestedConsentGeneralOrderEmailRespondentSolicitor(any());
        verify(notificationService, never()).sendContestedGeneralOrderEmailRespondent(any());
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
            .sendConsentedGeneralOrderEmail(any());
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
            .sendContestedGeneralApplicationReferToJudgeEmail(any());
    }

    @Test
    public void sendContestedConsentOrderNotApprovedEmail() throws Exception {
        when(featureToggleService.isRespondentSolicitorEmailNotificationEnabled()).thenReturn(true);
        when(notificationService.shouldEmailRespondentSolicitor(any())).thenReturn(true);

        buildCcdRequest(CONTESTED_SOL_SUBSCRIBED_FOR_EMAILS_JSON);
        mockMvc.perform(post(CONTESTED_CONSENT_ORDER_NOT_APPROVED_CALLBACK_URL)
            .header(AUTHORIZATION_HEADER, AUTH_TOKEN)
            .content(requestContent.toString())
            .contentType(MediaType.APPLICATION_JSON_VALUE))
            .andExpect(status().isOk());

        verify(notificationService, times(1)).sendContestedConsentOrderNotApprovedEmailApplicantSolicitor(any());
        verify(notificationService, times(1)).sendContestedConsentOrderNotApprovedEmailRespondentSolicitor(any());
    }

    @Test
    public void shouldNotSendContestedConsentOrderNotApprovedEmail() throws Exception {
        when(featureToggleService.isRespondentSolicitorEmailNotificationEnabled()).thenReturn(true);
        when(notificationService.shouldEmailRespondentSolicitor(any())).thenReturn(false);

        buildCcdRequest(CCD_REQUEST_JSON);
        mockMvc.perform(post(CONTESTED_CONSENT_ORDER_NOT_APPROVED_CALLBACK_URL)
            .header(AUTHORIZATION_HEADER, AUTH_TOKEN)
            .content(requestContent.toString())
            .contentType(MediaType.APPLICATION_JSON_VALUE))
            .andExpect(status().isOk());

        verify(notificationService, never()).sendContestedConsentOrderNotApprovedEmailApplicantSolicitor(any());
        verify(notificationService, never()).sendContestedConsentOrderNotApprovedEmailRespondentSolicitor(any());
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
            .sendDocumentForPrint(any(), any());
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
            .sendContestedGeneralApplicationOutcomeEmail(any());
    }

    private void buildCcdRequest(String fileName) throws IOException, URISyntaxException {
        ObjectMapper objectMapper = new ObjectMapper();
        requestContent = objectMapper.readTree(new File(getClass()
            .getResource(fileName).toURI()));
    }

    private CallbackRequest createCallbackRequestWithFinalOrder() {
        CallbackRequest callbackRequest = buildCallbackRequest();

        ArrayList finalOrderCollection = new ArrayList<>();
        finalOrderCollection.add(HearingOrderCollectionData.builder()
                .hearingOrderDocuments(HearingOrderDocument
                    .builder()
                    .uploadDraftDocument(new CaseDocument())
                    .build())
                .build());

        callbackRequest.getCaseDetails().getData().put(FINAL_ORDER_COLLECTION, finalOrderCollection);

        return callbackRequest;
    }
}
