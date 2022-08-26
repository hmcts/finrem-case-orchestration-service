package uk.gov.hmcts.reform.finrem.caseorchestration.controllers;

import com.fasterxml.jackson.core.JsonProcessingException;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.test.context.junit4.SpringRunner;
import uk.gov.hmcts.reform.ccd.client.model.CaseDetails;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.CaseDocument;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.HearingOrderCollectionData;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.HearingOrderDocument;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.OldCallbackRequest;
import uk.gov.hmcts.reform.finrem.caseorchestration.service.AdditionalHearingDocumentService;
import uk.gov.hmcts.reform.finrem.caseorchestration.service.CaseDataService;
import uk.gov.hmcts.reform.finrem.caseorchestration.service.FeatureToggleService;
import uk.gov.hmcts.reform.finrem.caseorchestration.service.GeneralEmailService;
import uk.gov.hmcts.reform.finrem.caseorchestration.service.HearingDocumentService;
import uk.gov.hmcts.reform.finrem.caseorchestration.service.HelpWithFeesDocumentService;
import uk.gov.hmcts.reform.finrem.caseorchestration.service.NotificationService;
import uk.gov.hmcts.reform.finrem.caseorchestration.service.PaperNotificationService;
import uk.gov.hmcts.reform.finrem.caseorchestration.service.TransferCourtService;
import uk.gov.hmcts.reform.finrem.caseorchestration.service.noc.NocLetterNotificationService;
import uk.gov.hmcts.reform.finrem.caseorchestration.service.noc.solicitors.CheckApplicantSolicitorIsDigitalService;
import uk.gov.hmcts.reform.finrem.caseorchestration.service.noc.solicitors.CheckRespondentSolicitorIsDigitalService;
import uk.gov.hmcts.reform.finrem.caseorchestration.service.serialisation.FinremCallbackRequestDeserializer;
import uk.gov.hmcts.reform.finrem.ccd.callback.CallbackRequest;
import uk.gov.hmcts.reform.finrem.ccd.domain.CaseType;
import uk.gov.hmcts.reform.finrem.ccd.domain.DirectionOrder;
import uk.gov.hmcts.reform.finrem.ccd.domain.DirectionOrderCollection;
import uk.gov.hmcts.reform.finrem.ccd.domain.Document;
import uk.gov.hmcts.reform.finrem.ccd.domain.EventType;
import uk.gov.hmcts.reform.finrem.ccd.domain.FinremCaseData;
import uk.gov.hmcts.reform.finrem.ccd.domain.FinremCaseDetails;
import uk.gov.hmcts.reform.finrem.ccd.domain.YesOrNo;

import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.ArgumentMatchers.isA;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.mockito.Mockito.when;
import static uk.gov.hmcts.reform.finrem.caseorchestration.TestConstants.AUTH_TOKEN;
import static uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.CCDConfigConstant.FINAL_ORDER_COLLECTION;


@RunWith(SpringRunner.class)
@WebMvcTest(NotificationsController.class)
public class NotificationsControllerTest extends BaseControllerTest {

    @Autowired private NotificationsController notificationsController;
    @MockBean private NocLetterNotificationService nocLetterNotificationService;
    @MockBean private NotificationService notificationService;
    @MockBean private PaperNotificationService paperNotificationService;
    @MockBean private GeneralEmailService generalEmailService;
    @MockBean private HelpWithFeesDocumentService helpWithFeesDocumentService;
    @MockBean private CaseDataService caseDataService;
    @MockBean private TransferCourtService transferCourtService;
    @MockBean private FeatureToggleService featureToggleService;
    @MockBean private FinremCallbackRequestDeserializer deserializer;
    @MockBean private CheckApplicantSolicitorIsDigitalService checkApplicantSolicitorIsDigitalService;
    @MockBean private CheckRespondentSolicitorIsDigitalService checkRespondentSolicitorIsDigitalService;
    @MockBean private HearingDocumentService hearingDocumentService;
    @MockBean private AdditionalHearingDocumentService additionalHearingDocumentService;

    @Test
    public void sendHwfSuccessfulConfirmationEmailIfDigitalCase() throws JsonProcessingException {
        when(caseDataService.isConsentedApplication(any())).thenReturn(true);
        when(caseDataService.isPaperApplication(any())).thenReturn(false);
        when(caseDataService.isApplicantSolicitorAgreeToReceiveEmails(any())).thenReturn(true);
        when(deserializer.deserialize(any())).thenReturn(getCallbackRequest(buildNewCallbackRequestStringConsented()));

        notificationsController.sendHwfSuccessfulConfirmationNotification(AUTH_TOKEN, buildNewCallbackRequestStringConsented());

        verify(notificationService).sendConsentedHWFSuccessfulConfirmationEmail(isA(FinremCaseDetails.class));
        verifyNoInteractions(helpWithFeesDocumentService);
    }

    @Test
    public void shouldNotSendHwfSuccessfulConfirmationEmail() throws JsonProcessingException {
        when(caseDataService.isConsentedApplication(any())).thenReturn(true);
        when(caseDataService.isPaperApplication(any())).thenReturn(false);
        when(caseDataService.isApplicantSolicitorAgreeToReceiveEmails(any())).thenReturn(false);
        when(deserializer.deserialize(any())).thenReturn(getCallbackRequest(buildNewCallbackRequestStringConsentedNoAgreeEmails()));

        notificationsController.sendHwfSuccessfulConfirmationNotification(AUTH_TOKEN, buildNewCallbackRequestString());

        verifyNoInteractions(notificationService);
    }

    @Test
    public void sendHwfSuccessfulNotificationLetterIfIsConsentedAndIsPaperApplication() throws JsonProcessingException {
        when(deserializer.deserialize(any())).thenReturn(getCallbackRequest(buildNewCallbackRequestStringConsentedPaper()));

        notificationsController.sendHwfSuccessfulConfirmationNotification(AUTH_TOKEN, buildNewCallbackRequestString());

        verify(paperNotificationService).printHwfSuccessfulNotification(any(FinremCaseDetails.class), eq(AUTH_TOKEN));
        verifyNoInteractions(notificationService);
    }

    @Test
    public void sendAssignToJudgeConfirmationEmailIfDigitalCase() throws JsonProcessingException {
        when(caseDataService.isApplicantSolicitorAgreeToReceiveEmails(any())).thenReturn(true);
        when(deserializer.deserialize(any())).thenReturn(getCallbackRequest(buildNewCallbackRequestString()));

        notificationsController.sendAssignToJudgeConfirmationNotification(AUTH_TOKEN, buildCallbackRequestString());

        verify(notificationService).sendAssignToJudgeConfirmationEmailToApplicantSolicitor(any());
    }

    @Test
    public void shouldSendAssignToJudgeConfirmationEmailIfRespondentSolicitorIsAcceptingEmail() throws JsonProcessingException {
        when(notificationService.isRespondentSolicitorEmailCommunicationEnabled(isA(FinremCaseData.class))).thenReturn(true);
        when(deserializer.deserialize(any())).thenReturn(getCallbackRequest(buildNewCallbackRequestString()));

        notificationsController.sendAssignToJudgeConfirmationNotification(AUTH_TOKEN, buildNewCallbackRequestString());

        verify(notificationService).sendAssignToJudgeConfirmationEmailToRespondentSolicitor(any());
    }

    @Test
    public void shouldNotSendAssignToJudgeConfirmationEmailIfRespondentSolicitorIsAcceptingEmail() throws JsonProcessingException {
        when(notificationService.isRespondentSolicitorEmailCommunicationEnabled(isA(FinremCaseData.class))).thenReturn(false);
        when(deserializer.deserialize(any())).thenReturn(getCallbackRequest(buildNewCallbackRequestString()));

        notificationsController.sendAssignToJudgeConfirmationNotification(AUTH_TOKEN, buildNewCallbackRequestString());

        verify(notificationService, never()).sendAssignToJudgeConfirmationEmailToRespondentSolicitor(any());
    }

    @Test
    public void whenIsConsentedAndSolicitorAgreedToEmail_sendConsentOrderMadeConfirmationEmail() throws JsonProcessingException {
        when(caseDataService.isConsentedApplication(any())).thenReturn(true);
        when(notificationService.isApplicantSolicitorRegisteredAndEmailCommunicationEnabled(isA(FinremCaseDetails.class))).thenReturn(true);
        when(deserializer.deserialize(any())).thenReturn(buildNewCallbackRequestConsented());

        notificationsController.sendConsentOrderMadeConfirmationEmail(buildNewCallbackRequestStringConsented());

        verify(notificationService).sendConsentOrderMadeConfirmationEmailToApplicantSolicitor(any());
    }

    @Test
    public void whenIsConsentedAndSolicitorNotAgreedToEmail_shouldNotSendConsentOrderMadeConfirmationEmail() throws JsonProcessingException {
        when(caseDataService.isConsentedApplication(any())).thenReturn(true);
        when(caseDataService.isApplicantSolicitorAgreeToReceiveEmails(any())).thenReturn(false);
        when(deserializer.deserialize(any())).thenReturn(buildNewCallbackRequestConsented());

        notificationsController.sendConsentOrderMadeConfirmationEmail(buildNewCallbackRequestStringConsented());

        verify(notificationService, never()).sendConsentOrderMadeConfirmationEmailToApplicantSolicitor(any());
        verify(notificationService, never()).sendConsentOrderMadeConfirmationEmailToRespondentSolicitor(any());
    }

    @Test
    public void whenIsNotConsentedAndSolicitorAgreedToEmail_sendConsentOrderMadeConfirmationEmail() throws JsonProcessingException {
        when(notificationService.isApplicantSolicitorRegisteredAndEmailCommunicationEnabled(isA(FinremCaseDetails.class))).thenReturn(true);
        when(deserializer.deserialize(any())).thenReturn(buildNewCallbackRequest());

        notificationsController.sendConsentOrderMadeConfirmationEmail(buildNewCallbackRequestString());

        verify(notificationService, never()).sendConsentOrderMadeConfirmationEmailToApplicantSolicitor(any());
        verify(notificationService, never()).sendConsentOrderMadeConfirmationEmailToRespondentSolicitor(any());
    }

    @Test
    public void whenIsNotConsentedAndSolicitorNotAgreedToEmail_shouldNotSendConsentOrderMadeConfirmationEmail() throws JsonProcessingException {
        when(caseDataService.isConsentedApplication(any())).thenReturn(false);
        when(caseDataService.isApplicantSolicitorAgreeToReceiveEmails(any())).thenReturn(false);
        when(deserializer.deserialize(any())).thenReturn(buildNewCallbackRequest());

        notificationsController.sendConsentOrderMadeConfirmationEmail(buildNewCallbackRequestString());

        verify(notificationService, never()).sendConsentOrderMadeConfirmationEmailToApplicantSolicitor(any());
        verify(notificationService, never()).sendConsentOrderMadeConfirmationEmailToRespondentSolicitor(any());
    }

    @Test
    public void sendConsentOrderNotApprovedEmail() throws JsonProcessingException {
        when(caseDataService.isConsentedApplication(any())).thenReturn(true);
        when(notificationService.isApplicantSolicitorRegisteredAndEmailCommunicationEnabled(isA(FinremCaseDetails.class))).thenReturn(true);
        when(deserializer.deserialize(any())).thenReturn(buildNewCallbackRequestConsented());

        notificationsController.sendConsentOrderNotApprovedEmail(buildNewCallbackRequestStringConsented());

        verify(notificationService).sendConsentOrderNotApprovedEmailToApplicantSolicitor(any());
    }

    @Test
    public void shouldNotSendConsentOrderNotApprovedEmail() throws JsonProcessingException {
        when(notificationService.isApplicantSolicitorRegisteredAndEmailCommunicationEnabled(isA(FinremCaseDetails.class)))
            .thenReturn(false);
        when(deserializer.deserialize(any())).thenReturn(buildNewCallbackRequestConsented());

        notificationsController.sendConsentOrderNotApprovedEmail(buildNewCallbackRequestStringConsented());

        verify(notificationService, never()).sendConsentOrderNotApprovedEmailToApplicantSolicitor(any());
        verify(notificationService, never()).sendContestOrderNotApprovedEmailApplicant(any());
    }

    @Test
    public void sendConsentOrderAvailableEmail() throws JsonProcessingException {
        when(caseDataService.isConsentedApplication(any())).thenReturn(true);
        when(caseDataService.isApplicantSolicitorAgreeToReceiveEmails(any())).thenReturn(true);
        when(deserializer.deserialize(any())).thenReturn(getCallbackRequest(buildNewCallbackRequestStringConsented()));

        notificationsController.sendConsentOrderAvailableEmail(buildCallbackRequestString());

        verify(notificationService).sendConsentOrderAvailableEmailToApplicantSolicitor(any());
    }

    @Test
    public void shouldNotSendConsentOrderAvailableEmail() throws JsonProcessingException {
        when(caseDataService.isConsentedApplication(any())).thenReturn(true);
        when(caseDataService.isApplicantSolicitorAgreeToReceiveEmails(any())).thenReturn(false);
        when(deserializer.deserialize(any())).thenReturn(getCallbackRequest(buildNewCallbackRequestStringConsentedNoAgreeEmails()));

        notificationsController.sendConsentOrderAvailableEmail(buildCallbackRequestString());

        verify(notificationService, never()).sendConsentOrderAvailableEmailToApplicantSolicitor(any());
        verify(notificationService, never()).sendConsentOrderAvailableEmailToRespondentSolicitor(any());
    }

    @Test
    public void sendConsentedHwfSuccessfulConfirmationEmail() throws JsonProcessingException {
        when(caseDataService.isConsentedApplication(any())).thenReturn(false);
        when(caseDataService.isApplicantSolicitorAgreeToReceiveEmails(any())).thenReturn(true);
        when(deserializer.deserialize(any())).thenReturn(getCallbackRequest(buildNewCallbackRequestString()));

        notificationsController.sendHwfSuccessfulConfirmationNotification(AUTH_TOKEN, buildCallbackRequestString());

        verify(notificationService).sendContestedHwfSuccessfulConfirmationEmail(any());
    }

    @Test
    public void shouldNotSendContestedHwfSuccessfulEmail() throws JsonProcessingException {
        when(caseDataService.isConsentedApplication(any())).thenReturn(false);
        when(caseDataService.isApplicantSolicitorAgreeToReceiveEmails(any())).thenReturn(false);
        when(deserializer.deserialize(any())).thenReturn(getCallbackRequest(buildNewCallbackRequestStringContestedNoAgree()));

        notificationsController.sendHwfSuccessfulConfirmationNotification(AUTH_TOKEN, buildCallbackRequestString());

        verifyNoInteractions(notificationService);
    }

    @Test
    public void givenApplicantSolicitorIsRegisteredAndAgreedToEmails_shouldSendPrepareForHearingEmail() throws JsonProcessingException {
        when(notificationService.isApplicantSolicitorRegisteredAndEmailCommunicationEnabled(isA(FinremCaseDetails.class))).thenReturn(true);
        when(deserializer.deserialize(any())).thenReturn(getCallbackRequest(buildNewCallbackRequestString()));

        notificationsController.sendPrepareForHearingEmail(AUTH_TOKEN, buildNewCallbackRequestString());

        verify(notificationService).sendPrepareForHearingEmailApplicant(any());
    }

    @Test
    public void givenSolAgreedToEmails_and_noPreviousHearing_shouldSendPrepareForHearingEmail_and_PrintHearingDocuments()
        throws JsonProcessingException {
        when(notificationService.isApplicantSolicitorRegisteredAndEmailCommunicationEnabled(isA(FinremCaseDetails.class)))
            .thenReturn(true);
        when(notificationService.isRespondentSolicitorRegisteredAndEmailCommunicationEnabled(isA(FinremCaseDetails.class)))
            .thenReturn(true);
        when(hearingDocumentService.alreadyHadFirstHearing(isA(FinremCaseDetails.class))).thenReturn(false);
        when(deserializer.deserialize(any()))
            .thenReturn(getCallbackRequest(buildCallbackRequestWithBeforeCaseDetailsStringPaper()));

        notificationsController.sendPrepareForHearingEmail(AUTH_TOKEN, buildCallbackRequestString());

        verify(notificationService).sendPrepareForHearingEmailApplicant(any());
        verify(notificationService).sendPrepareForHearingEmailRespondent(any());
    }

    @Test
    public void shouldNotSendPrepareForHearingEmailToApplicantSolicitorWhenNotAgreed() throws JsonProcessingException {
        when(caseDataService.isApplicantSolicitorAgreeToReceiveEmails(any())).thenReturn(false);
        when(checkApplicantSolicitorIsDigitalService.isSolicitorDigital(isA(FinremCaseDetails.class))).thenReturn(false);
        when(deserializer.deserialize(any())).thenReturn(getCallbackRequest(buildNewCallbackRequestStringContestedNoAgree()));

        notificationsController.sendPrepareForHearingEmail(AUTH_TOKEN, buildCallbackRequestString());

        verify(notificationService, never()).sendPrepareForHearingEmailApplicant(any());
        verify(notificationService, never()).sendPrepareForHearingEmailRespondent(any());
    }

    @Test
    public void shouldSendPrepareForHearingOrderSentEmailWhenAgreed() {
        when(caseDataService.isConsentedApplication(any())).thenReturn(false);
        when(caseDataService.isApplicantSolicitorAgreeToReceiveEmails(any())).thenReturn(true);

        notificationsController.sendPrepareForHearingOrderSentEmail(buildCallbackRequest());

        verify(notificationService).sendPrepareForHearingOrderSentEmailApplicant(any());
    }

    @Test
    public void shouldSendPrepareForHearingOrderSentEmailWhenRespondentIsRegisteredAndAgreedToEmails() throws JsonProcessingException {
        when(caseDataService.isConsentedApplication(any())).thenReturn(false);
        when(notificationService.isRespondentSolicitorRegisteredAndEmailCommunicationEnabled(isA(FinremCaseDetails.class))).thenReturn(true);
        when(deserializer.deserialize(any())).thenReturn(getCallbackRequest(buildNewCallbackRequestString()));

        notificationsController.sendPrepareForHearingEmail(AUTH_TOKEN, buildNewCallbackRequestString());

        verify(notificationService).sendPrepareForHearingEmailRespondent(any());
    }

    @Test
    public void shouldNotSendPrepareForHearingOrderSentEmailWhenRespondentAgreedButNotRegistered() throws JsonProcessingException {
        when(caseDataService.isConsentedApplication(any())).thenReturn(false);
        when(notificationService.isRespondentSolicitorEmailCommunicationEnabled(isA(FinremCaseData.class))).thenReturn(true);
        when(checkRespondentSolicitorIsDigitalService.isSolicitorDigital(isA(FinremCaseDetails.class))).thenReturn(false);
        when(deserializer.deserialize(any())).thenReturn(getCallbackRequest(buildNewCallbackRequestString()));

        notificationsController.sendPrepareForHearingEmail(AUTH_TOKEN, buildNewCallbackRequestString());

        verify(notificationService, never()).sendPrepareForHearingEmailApplicant(any());
        verify(notificationService, never()).sendPrepareForHearingEmailRespondent(any());
    }

    @Test
    public void shouldNotSendPrepareForHearingOrderSentEmailWhenNotAgreed() {
        //when(caseDataService.isConsentedApplication(any())).thenReturn(true);
        when(caseDataService.isApplicantSolicitorAgreeToReceiveEmails(any())).thenReturn(false);

        notificationsController.sendPrepareForHearingOrderSentEmail(buildCallbackRequest());

        verify(notificationService, never()).sendPrepareForHearingOrderSentEmailApplicant(any());
    }

    @Test
    public void sendPrepareForHearingOrderSentEmail_shouldSendRespondentEmail() {
        when(notificationService.isRespondentSolicitorEmailCommunicationEnabled(isA(Map.class))).thenReturn(true);

        notificationsController.sendPrepareForHearingOrderSentEmail(buildCallbackRequest());

        verify(notificationService).sendPrepareForHearingOrderSentEmailRespondent(any());
    }

    @Test
    public void sendPrepareForHearingOrderSentEmail_shouldNotSendRespondentEmail() {
        when(notificationService.isRespondentSolicitorEmailCommunicationEnabled(isA(HashMap.class))).thenReturn(false);

        notificationsController.sendPrepareForHearingOrderSentEmail(buildCallbackRequest());

        verify(notificationService, never()).sendPrepareForHearingOrderSentEmailRespondent(any());
    }


    @Test
    public void shouldSendContestedApplicationIssuedEmailWhenAgreed() {
        when(caseDataService.isApplicantSolicitorAgreeToReceiveEmails(any())).thenReturn(true);

        notificationsController.sendContestedApplicationIssuedEmail(buildCallbackRequest());

        verify(notificationService).sendContestedApplicationIssuedEmailToApplicantSolicitor(any());
    }

    @Test
    public void shouldNotSendContestedApplicationIssuedEmailWhenNotAgreed() {
        when(caseDataService.isApplicantSolicitorAgreeToReceiveEmails(any())).thenReturn(false);

        notificationsController.sendContestedApplicationIssuedEmail(buildCallbackRequest());

        verify(notificationService, never()).sendContestedApplicationIssuedEmailToApplicantSolicitor(any());
    }

    @Test
    public void shouldSendContestedApplicationIssuedEmailWhenAgreed_andNotifyRespondentSolicitorWhenShould() {
        when(notificationService.isRespondentSolicitorEmailCommunicationEnabled(isA(Map.class))).thenReturn(true);

        notificationsController.sendContestedApplicationIssuedEmail(buildCallbackRequest());

        verify(notificationService).sendContestedApplicationIssuedEmailToRespondentSolicitor(any());
    }

    @Test
    public void shouldNotSendContestedApplicationIssuedEmailWhenNotAgreed_andDontNotifyRespondentSolicitorWhenShouldNot() {
        when(notificationService.isRespondentSolicitorEmailCommunicationEnabled(isA(Map.class))).thenReturn(false);

        notificationsController.sendContestedApplicationIssuedEmail(buildCallbackRequest());

        verify(notificationService, never()).sendContestedApplicationIssuedEmailToRespondentSolicitor(any());
    }

    @Test
    public void sendDraftOrderEmailWhenApplicantSolicitorIsNominatedAndIsAcceptingEmails() {
        when(caseDataService.isConsentedApplication(any())).thenReturn(true);
        when(caseDataService.isApplicantSolicitorAgreeToReceiveEmails(any())).thenReturn(true);
        when(caseDataService.isApplicantSolicitorResponsibleToDraftOrder(any())).thenReturn(true);

        notificationsController.sendDraftOrderEmail(createCallbackRequestWithFinalOrder());

        verify(notificationService).sendSolicitorToDraftOrderEmailApplicant(any());
    }

    @Test
    public void shouldNotSendDraftOrderEmailAsSolicitorOptedOutOfEmailComms() {
        when(caseDataService.isConsentedApplication(any())).thenReturn(true);
        when(caseDataService.isApplicantSolicitorAgreeToReceiveEmails(any())).thenReturn(false);

        notificationsController.sendDraftOrderEmail(createCallbackRequestWithFinalOrder());

        verify(notificationService, never()).sendSolicitorToDraftOrderEmailApplicant(any());
    }

    @Test
    public void shouldNotSendDraftOrderEmailAsRespondentSolicitorIsNominated() {
        when(caseDataService.isConsentedApplication(any())).thenReturn(true);
        when(caseDataService.isApplicantSolicitorResponsibleToDraftOrder(any())).thenReturn(false);

        notificationsController.sendDraftOrderEmail(createCallbackRequestWithFinalOrder());

        verify(notificationService, never()).sendSolicitorToDraftOrderEmailApplicant(any());
    }

    @Test
    public void shouldSendSolicitorToDraftOrderEmailRespondent() {
        when(notificationService.isRespondentSolicitorEmailCommunicationEnabled(isA(Map.class))).thenReturn(true);
        when(caseDataService.isRespondentSolicitorResponsibleToDraftOrder(any())).thenReturn(true);

        notificationsController.sendDraftOrderEmail(buildCallbackRequest());

        verify(notificationService).sendSolicitorToDraftOrderEmailRespondent(any());
    }

    @Test
    public void shouldSendSolicitorToDraftOrderEmailRespondent_shouldNotSendEmail() {
        when(notificationService.isRespondentSolicitorEmailCommunicationEnabled(isA(Map.class))).thenReturn(false);
        when(caseDataService.isRespondentSolicitorResponsibleToDraftOrder(any())).thenReturn(true);

        notificationsController.sendDraftOrderEmail(buildCallbackRequest());

        verify(notificationService, never()).sendSolicitorToDraftOrderEmailRespondent(any());
    }

    @Test
    public void shouldSendSolicitorToDraftOrderEmailRespondent_respSolicitorNotResponsible() {
        when(notificationService.isRespondentSolicitorEmailCommunicationEnabled(isA(Map.class))).thenReturn(true);
        when(caseDataService.isRespondentSolicitorResponsibleToDraftOrder(any())).thenReturn(false);
        when(featureToggleService.isRespondentJourneyEnabled()).thenReturn(true);

        notificationsController.sendDraftOrderEmail(buildCallbackRequest());

        verify(notificationService, never()).sendSolicitorToDraftOrderEmailRespondent(any());
    }

    @Test
    public void sendGeneralEmailConsented() {
        when(caseDataService.isConsentedApplication(any())).thenReturn(true);

        notificationsController.sendGeneralEmail(createCallbackRequestWithFinalOrder());

        verify(notificationService).sendConsentGeneralEmail(any());
        verify(generalEmailService).storeGeneralEmail(any());
    }

    @Test
    public void sendGeneralEmailContested() {
        when(caseDataService.isConsentedApplication(any())).thenReturn(false);

        notificationsController.sendGeneralEmail(createCallbackRequestWithFinalOrder());

        verify(notificationService).sendContestedGeneralEmail(any());
        verify(generalEmailService).storeGeneralEmail(any(CaseDetails.class));
    }

    @Test
    public void sendContestOrderNotApprovedEmail() throws JsonProcessingException {
        when(caseDataService.isConsentedApplication(any())).thenReturn(false);
        when(notificationService.isApplicantSolicitorRegisteredAndEmailCommunicationEnabled(isA(FinremCaseDetails.class))).thenReturn(true);
        when(deserializer.deserialize(any())).thenReturn(createNewCallbackRequestWithFinalOrder());

        notificationsController.sendConsentOrderNotApprovedEmail(createCallbackRequestWithFinalOrderString());

        verify(notificationService).sendContestOrderNotApprovedEmailApplicant(any());
    }

    @Test
    public void shouldNotSendContestOrderNotApprovedEmail() throws JsonProcessingException {
        when(caseDataService.isConsentedApplication(any())).thenReturn(false);
        when(notificationService.isApplicantSolicitorRegisteredAndEmailCommunicationEnabled(isA(FinremCaseDetails.class)))
            .thenReturn(false);
        when(deserializer.deserialize(any())).thenReturn(createNewCallbackRequestWithFinalOrder());

        notificationsController.sendConsentOrderNotApprovedEmail(createCallbackRequestWithFinalOrderString());

        verify(notificationService, never()).sendConsentOrderNotApprovedEmailToApplicantSolicitor(any());
        verify(notificationService, never()).sendContestOrderNotApprovedEmailApplicant(any());
    }

    @Test
    public void sendContestedConsentOrderApprovedEmailToApplicantSolicitor() {
        when(caseDataService.isApplicantSolicitorAgreeToReceiveEmails(any())).thenReturn(true);

        notificationsController.sendContestedConsentOrderApprovedEmail(buildCallbackRequest());

        verify(notificationService).sendContestedConsentOrderApprovedEmailToApplicantSolicitor(any());
    }

    @Test
    public void sendContestedConsentOrderApprovedEmailToRespondentSolicitor() {
        when(notificationService.isRespondentSolicitorEmailCommunicationEnabled(isA(Map.class))).thenReturn(true);

        notificationsController.sendContestedConsentOrderApprovedEmail(buildCallbackRequest());

        verify(notificationService).sendContestedConsentOrderApprovedEmailToRespondentSolicitor(any());
    }

    @Test
    public void givenContestedCase_whenShouldSendRespondentNotification_thenShouldTriggerRespondentEmail() throws JsonProcessingException {
        when(notificationService.isRespondentSolicitorEmailCommunicationEnabled(isA(FinremCaseData.class))).thenReturn(true);
        when(deserializer.deserialize(any())).thenReturn(buildNewCallbackRequest());


        notificationsController.sendConsentOrderNotApprovedEmail(buildNewCallbackRequestString());

        verify(notificationService).sendContestOrderNotApprovedEmailRespondent(any());
        verify(notificationService, never()).sendConsentOrderNotApprovedEmailToRespondentSolicitor(any());
    }

    @Test
    public void givenConsentedCase_whenShouldSendRespondentNotification_thenShouldNotTriggerContestedRespondentEmail() throws JsonProcessingException {
        when(caseDataService.isConsentedApplication(any())).thenReturn(true);
        when(notificationService.isRespondentSolicitorEmailCommunicationEnabled(isA(FinremCaseData.class))).thenReturn(true);
        when(deserializer.deserialize(any())).thenReturn(buildNewCallbackRequestConsented());

        notificationsController.sendConsentOrderNotApprovedEmail(buildNewCallbackRequestString());

        verify(notificationService, never()).sendContestOrderNotApprovedEmailRespondent(any());
    }

    @Test
    public void givenConsentedCase_whenSendConsentOrderNotApproved_thenShouldTriggerConsentedRespondentEmail() throws JsonProcessingException {
        when(caseDataService.isConsentedApplication(any())).thenReturn(true);
        when(notificationService.isRespondentSolicitorEmailCommunicationEnabled(isA(FinremCaseData.class))).thenReturn(true);
        when(deserializer.deserialize(any())).thenReturn(buildNewCallbackRequestConsented());

        notificationsController.sendConsentOrderNotApprovedEmail(buildNewCallbackRequestStringConsented());

        verify(notificationService).sendConsentOrderNotApprovedEmailToRespondentSolicitor(any());
    }

    @Test
    public void sendContestedConsentOrderApprovedEmail() {
        when(caseDataService.isConsentedApplication(any())).thenReturn(false);
        when(caseDataService.isApplicantSolicitorAgreeToReceiveEmails(any())).thenReturn(true);

        notificationsController.sendContestedConsentOrderApprovedEmail(buildCallbackRequest());

        verify(notificationService).sendContestedConsentOrderApprovedEmailToApplicantSolicitor(any());
    }

    @Test
    public void shouldNotSendContestedConsentOrderApprovedEmailToRespondentSolicitorWhenRespSolShouldNotReceiveEmail() {
        when(notificationService.isRespondentSolicitorEmailCommunicationEnabled(isA(HashMap.class))).thenReturn(false);

        notificationsController.sendContestedConsentOrderApprovedEmail(buildCallbackRequest());

        verify(notificationService,
            never()).sendContestedConsentOrderApprovedEmailToRespondentSolicitor(any());
    }


    @Test
    public void shouldNotSendGeneralOrderEmail() {
        when(caseDataService.isConsentedApplication(any())).thenReturn(true);
        when(caseDataService.isApplicantSolicitorAgreeToReceiveEmails(any())).thenReturn(false);

        notificationsController.sendGeneralOrderRaisedEmail(buildCallbackRequest());

        verify(notificationService, never()).sendConsentedGeneralOrderEmailToRespondentSolicitor(any());
        verify(notificationService, never()).sendContestedConsentGeneralOrderEmailRespondentSolicitor(any());
        verify(notificationService, never()).sendContestedGeneralOrderEmailRespondent(any());
    }

    @Test
    public void sendContestedConsentGeneralOrderEmail() {
        when(caseDataService.isConsentedApplication(any())).thenReturn(false);
        when(caseDataService.isConsentedInContestedCase(any())).thenReturn(true);
        when(caseDataService.isApplicantSolicitorAgreeToReceiveEmails(any())).thenReturn(true);

        notificationsController.sendGeneralOrderRaisedEmail(buildCallbackRequest());

        verify(notificationService).sendContestedConsentGeneralOrderEmailApplicantSolicitor(any());
    }

    @Test
    public void sendContestedGeneralOrderEmails() {
        when(caseDataService.isConsentedApplication(any())).thenReturn(false);
        when(caseDataService.isConsentedInContestedCase(any())).thenReturn(false);
        when(caseDataService.isApplicantSolicitorAgreeToReceiveEmails(any())).thenReturn(true);
        when(notificationService.isRespondentSolicitorEmailCommunicationEnabled(isA(Map.class))).thenReturn(true);

        notificationsController.sendGeneralOrderRaisedEmail(buildCallbackRequest());

        verify(notificationService).sendContestedGeneralOrderEmailApplicant(any());
        verify(notificationService).sendContestedGeneralOrderEmailRespondent(any());
    }

    @Test
    public void whenShouldNotSendContestedGeneralOrderEmailToRespondent_ThenTheEmailIsNotIssued() {
        when(notificationService.isRespondentSolicitorEmailCommunicationEnabled(isA(Map.class))).thenReturn(false);

        notificationsController.sendGeneralOrderRaisedEmail(buildCallbackRequest());

        verify(notificationService, never()).sendContestedConsentGeneralOrderEmailRespondentSolicitor(any());
        verify(notificationService, never()).sendContestedGeneralOrderEmailRespondent(any());
    }

    @Test
    public void shouldNotSendEmailToRespSolicitor() throws JsonProcessingException {
        when(notificationService.isRespondentSolicitorEmailCommunicationEnabled(isA(FinremCaseData.class))).thenReturn(false);
        when(deserializer.deserialize(any())).thenReturn(getCallbackRequest(buildNewCallbackRequestString()));

        notificationsController.sendAssignToJudgeConfirmationNotification(AUTH_TOKEN, buildCallbackRequestString());

        verify(notificationService, never()).sendAssignToJudgeConfirmationEmailToRespondentSolicitor(any());
    }

    @Test
    public void shouldSendContestedConsentGeneralOrderEmailToRespondentInConsentedInContestedCase() {
        when(caseDataService.isConsentedApplication(any())).thenReturn(false);
        when(caseDataService.isConsentedInContestedCase(any())).thenReturn(true);
        when(notificationService.isRespondentSolicitorEmailCommunicationEnabled(isA(Map.class))).thenReturn(true);

        notificationsController.sendGeneralOrderRaisedEmail(buildCallbackRequest());

        verify(notificationService).sendContestedConsentGeneralOrderEmailRespondentSolicitor(any());
        verify(notificationService, never()).sendContestedGeneralOrderEmailRespondent(any());
        verify(notificationService, never()).sendConsentedGeneralOrderEmailToRespondentSolicitor(any());
    }

    @Test
    public void sendConsentedGeneralOrderEmail() {
        when(caseDataService.isConsentedApplication(any())).thenReturn(true);
        when(caseDataService.isApplicantSolicitorAgreeToReceiveEmails(any())).thenReturn(true);
        when(notificationService.isRespondentSolicitorEmailCommunicationEnabled(isA(Map.class))).thenReturn(true);

        notificationsController.sendGeneralOrderRaisedEmail(buildCallbackRequest());

        verify(notificationService).sendConsentedGeneralOrderEmailToApplicantSolicitor(any());
        verify(notificationService).sendConsentedGeneralOrderEmailToRespondentSolicitor(any());
    }

    @Test
    public void shouldNotSendContestedConsentOrderApprovedEmail() {
        when(caseDataService.isConsentedApplication(any())).thenReturn(false);

        notificationsController.sendContestedConsentOrderApprovedEmail(buildCallbackRequest());

        verify(notificationService, never()).sendContestedConsentOrderApprovedEmailToApplicantSolicitor(any());
        verify(notificationService, never()).sendContestedConsentOrderApprovedEmailToRespondentSolicitor(any());
    }

    @Test
    public void sendContestedGeneralApplicationReferToJudgeEmail() {
        when(caseDataService.isConsentedApplication(any())).thenReturn(false);

        notificationsController.sendGeneralApplicationReferToJudgeEmail(buildCallbackRequest());

        verify(notificationService).sendContestedGeneralApplicationReferToJudgeEmail(any());
    }

    @Test
    public void sendContestedConsentOrderNotApprovedEmail() {
        when(caseDataService.isConsentedApplication(any())).thenReturn(false);
        when(caseDataService.isPaperApplication(any())).thenReturn(false);
        when(caseDataService.isApplicantSolicitorAgreeToReceiveEmails(any())).thenReturn(true);
        when(notificationService.isRespondentSolicitorEmailCommunicationEnabled(isA(Map.class))).thenReturn(true);

        notificationsController.sendContestedConsentOrderNotApprovedEmail(buildCallbackRequest());

        verify(notificationService).sendContestedConsentOrderNotApprovedEmailApplicantSolicitor(any());
        verify(notificationService).sendContestedConsentOrderNotApprovedEmailRespondentSolicitor(any());
    }

    @Test
    public void shouldNotSendContestedConsentOrderNotApprovedEmail() {
        when(notificationService.isRespondentSolicitorEmailCommunicationEnabled(isA(Map.class))).thenReturn(false);

        notificationsController.sendContestedConsentOrderNotApprovedEmail(buildCallbackRequest());

        verify(notificationService, never()).sendContestedConsentOrderNotApprovedEmailApplicantSolicitor(any());
        verify(notificationService, never()).sendContestedConsentOrderNotApprovedEmailRespondentSolicitor(any());
    }

    @Test
    public void sendContestedGeneralApplicationOutcomeEmail() throws IOException {
        when(caseDataService.isConsentedApplication(any())).thenReturn(false);
        when(caseDataService.isApplicantSolicitorAgreeToReceiveEmails(any())).thenReturn(true);

        notificationsController.sendGeneralApplicationOutcomeEmail(buildCallbackRequest());

        verify(notificationService).sendContestedGeneralApplicationOutcomeEmail(any());
    }

    @Test
    public void givenConsentedCase_whenToggleEnabledAndShouldSendEmailToRespSolicitor_thenSendsEmail()
        throws JsonProcessingException {
        when(notificationService.isRespondentSolicitorEmailCommunicationEnabled(isA(FinremCaseData.class))).thenReturn(true);
        when(deserializer.deserialize(any())).thenReturn(getCallbackRequest(buildNewCallbackRequestStringConsented()));

        notificationsController.sendConsentOrderAvailableEmail(buildNewCallbackRequestStringConsented());
        verify(notificationService).sendConsentOrderAvailableEmailToRespondentSolicitor(isA(FinremCaseDetails.class));
    }

    @Test
    public void sendConsentOrderMadeEmailToRespSolicitor() throws JsonProcessingException {
        when(notificationService.isRespondentSolicitorEmailCommunicationEnabled(isA(FinremCaseData.class))).thenReturn(true);
        when(deserializer.deserialize(any())).thenReturn(buildNewCallbackRequest());
        notificationsController.sendConsentOrderMadeConfirmationEmail(buildNewCallbackRequestString());
        verify(notificationService).sendConsentOrderMadeConfirmationEmailToRespondentSolicitor(isA(FinremCaseDetails.class));
    }

    @Test
    public void doesNotSendConsentOrderMadeEmailToRespSolicitor() throws JsonProcessingException {
        when(notificationService.isRespondentSolicitorEmailCommunicationEnabled(isA(FinremCaseData.class))).thenReturn(false);
        when(deserializer.deserialize(any())).thenReturn(buildNewCallbackRequestConsented());
        notificationsController.sendConsentOrderMadeConfirmationEmail(buildNewCallbackRequestStringConsented());
        verify(notificationService, never()).sendConsentOrderMadeConfirmationEmailToRespondentSolicitor(isA(FinremCaseDetails.class));
    }

    @Test
    public void whenConsentOrderNotApprovedSentEmail_thenNotificationEmailsSentToSolicitors() {
        when(caseDataService.isApplicantSolicitorAgreeToReceiveEmails(any())).thenReturn(true);
        when(notificationService.isRespondentSolicitorEmailCommunicationEnabled(isA(Map.class))).thenReturn(true);

        notificationsController.sendConsentOrderNotApprovedSentEmail(buildCallbackRequest());

        verify(notificationService).sendConsentOrderNotApprovedSentEmailToApplicantSolicitor(any());
        verify(notificationService).sendConsentOrderNotApprovedSentEmailToRespondentSolicitor(any());
    }

    @Test
    public void whenConsentOrderApprovedAndSolicitorEmailsNotEnabled_thenDoNotEmailSolicitors() {
        when(caseDataService.isApplicantSolicitorAgreeToReceiveEmails(any())).thenReturn(false);
        when(notificationService.isRespondentSolicitorEmailCommunicationEnabled(isA(FinremCaseData.class))).thenReturn(false);

        notificationsController.sendConsentOrderNotApprovedSentEmail(buildCallbackRequest());

        verify(notificationService, never()).sendConsentOrderNotApprovedSentEmailToApplicantSolicitor(any());
        verify(notificationService, never()).sendConsentOrderNotApprovedSentEmailToRespondentSolicitor(any());
    }

    @Test
    public void sendTransferToLocalCourtEmailConsented() {
        when(caseDataService.isConsentedApplication(any())).thenReturn(true);

        notificationsController.sendTransferCourtsEmail(buildCallbackRequest());

        verify(notificationService).sendTransferToLocalCourtEmail(any());
        verify(transferCourtService).storeTransferToCourtEmail(any());
    }

    @Test
    public void shouldSendInterimHearingWhenAgreed() {
        when(caseDataService.isPaperApplication(any())).thenReturn(false);
        when(caseDataService.isContestedApplication(any())).thenReturn(true);
        when(caseDataService.isApplicantSolicitorAgreeToReceiveEmails(any())).thenReturn(true);
        when(notificationService.isRespondentSolicitorEmailCommunicationEnabled(isA(Map.class))).thenReturn(true);

        notificationsController.sendInterimHearingNotification(buildCallbackInterimRequest());

        verify(notificationService, times(1)).sendInterimNotificationEmailToApplicantSolicitor(any());
        verify(notificationService, times(1)).sendInterimNotificationEmailToRespondentSolicitor(any());
    }

    @Test
    public void shouldNotSendInterimHearingWhenNotAgreed() {
        when(caseDataService.isPaperApplication(any())).thenReturn(false);
        when(caseDataService.isContestedApplication(any())).thenReturn(true);
        when(caseDataService.isApplicantSolicitorAgreeToReceiveEmails(any())).thenReturn(false);
        when(notificationService.isRespondentSolicitorEmailCommunicationEnabled(isA(Map.class))).thenReturn(false);

        notificationsController.sendInterimHearingNotification(buildCallbackInterimRequest());

        verify(notificationService, never()).sendInterimNotificationEmailToApplicantSolicitor(any());
        verify(notificationService, never()).sendInterimNotificationEmailToRespondentSolicitor(any());
    }

    @Test
    public void shouldSendInterimHearingNotificationWhenApplicantAgreedButRespondentNotAgreed() {
        when(caseDataService.isPaperApplication(any())).thenReturn(false);
        when(caseDataService.isContestedApplication(any())).thenReturn(true);
        when(caseDataService.isApplicantSolicitorAgreeToReceiveEmails(any())).thenReturn(true);
        when(notificationService.isRespondentSolicitorEmailCommunicationEnabled(isA(Map.class))).thenReturn(false);

        notificationsController.sendInterimHearingNotification(buildCallbackInterimRequest());

        verify(notificationService, times(1)).sendInterimNotificationEmailToApplicantSolicitor(any());
        verify(notificationService, never()).sendInterimNotificationEmailToRespondentSolicitor(any());
    }

    @Test
    public void shouldSendInterimHearingNotificationWhenApplicantNotAgreedButRespondentAgreed() {
        when(caseDataService.isPaperApplication(any())).thenReturn(false);
        when(caseDataService.isContestedApplication(any())).thenReturn(true);
        when(caseDataService.isApplicantSolicitorAgreeToReceiveEmails(any())).thenReturn(false);
        when(notificationService.isRespondentSolicitorEmailCommunicationEnabled(isA(Map.class))).thenReturn(true);

        notificationsController.sendInterimHearingNotification(buildCallbackInterimRequest());

        verify(notificationService, never()).sendInterimNotificationEmailToApplicantSolicitor(any());
        verify(notificationService, times(1)).sendInterimNotificationEmailToRespondentSolicitor(any());
    }

    @Test
    public void givenNoticeOfChangeWhenSendNoticeOfChangeNotificationsThenSendNoticeOfChangeServiceCalled() throws JsonProcessingException {
        when(deserializer.deserialize(any())).thenReturn(getCallbackRequest(buildCallbackRequestWithBeforeCaseDetailsString()));
        notificationsController.sendNoticeOfChangeNotifications("authToken", buildCallbackRequestWithBeforeCaseDetailsString());

        verify(notificationService, times(1)).sendNoticeOfChangeEmail(any());

        verify(nocLetterNotificationService, times(1)).sendNoticeOfChangeLetters(any(FinremCaseDetails.class),
            any(FinremCaseDetails.class), anyString());
    }

    @Test
    public void givenNoticeOfChangeAsCaseworker_whenSendNoCNotifications_ThenSendNoticeOfChangeServiceCalled() throws JsonProcessingException {
        when(featureToggleService.isCaseworkerNoCEnabled()).thenReturn(true);
        when(deserializer.deserialize(any())).thenReturn(getCallbackRequest(buildNoCCaseworkerCallbackRequestString()));

        notificationsController.sendNoticeOfChangeNotificationsCaseworker("authtoken",
            buildNoCCaseworkerCallbackRequestString());

        verify(notificationService, times(1)).sendNoticeOfChangeEmailCaseworker(any());

        verify(nocLetterNotificationService, times(1))
            .sendNoticeOfChangeLetters(any(FinremCaseDetails.class), any(FinremCaseDetails.class), anyString());
    }

    @Test
    public void givenUpdateFrc_whenSendEmail_thenNotificationServiceCalledThreeTimes() throws JsonProcessingException {
        when(caseDataService.isApplicantSolicitorAgreeToReceiveEmails(any(CaseDetails.class))).thenReturn(true);
        when(notificationService.isRespondentSolicitorEmailCommunicationEnabled(isA(FinremCaseData.class))).thenReturn(true);
        when(deserializer.deserialize(any())).thenReturn(getCallbackRequest(buildNewCallbackRequestString()));

        notificationsController.sendUpdateFrcNotifications(AUTH_TOKEN, buildNewCallbackRequestString());
        verify(notificationService, times(1)).sendUpdateFrcInformationEmailToAppSolicitor(any());
        verify(notificationService, times(1)).sendUpdateFrcInformationEmailToRespondentSolicitor(any());
        verify(notificationService, times(1)).sendUpdateFrcInformationEmailToCourt(any());
        verify(paperNotificationService, times(1)).printUpdateFrcInformationNotification(any(), any());
    }

    @Test
    public void givenUpdateFrc_whenAppSolNotAgreeToReceiveEmails_thenNotificationServiceCalledTwice() throws JsonProcessingException {
        when(caseDataService.isApplicantSolicitorAgreeToReceiveEmails(any(CaseDetails.class))).thenReturn(false);
        when(notificationService.isRespondentSolicitorEmailCommunicationEnabled(isA(FinremCaseData.class))).thenReturn(true);
        when(deserializer.deserialize(any())).thenReturn(getCallbackRequest(buildNewCallbackRequestStringContestedNoAgree()));

        notificationsController.sendUpdateFrcNotifications(AUTH_TOKEN, buildNewCallbackRequestStringNoAppSolConsent());
        verify(notificationService, never()).sendUpdateFrcInformationEmailToAppSolicitor(any());
        verify(notificationService, times(1)).sendUpdateFrcInformationEmailToRespondentSolicitor(any());
        verify(notificationService, times(1)).sendUpdateFrcInformationEmailToCourt(any());
        verify(paperNotificationService, times(1)).printUpdateFrcInformationNotification(any(), any());
    }

    @Test
    public void givenUpdateFrc_whenRespSolNotAgreeToReceiveEmails_thenNotificationServiceCalledTwice() throws JsonProcessingException {
        when(caseDataService.isApplicantSolicitorAgreeToReceiveEmails(any(CaseDetails.class))).thenReturn(true);
        when(notificationService.isRespondentSolicitorEmailCommunicationEnabled(isA(FinremCaseData.class))).thenReturn(false);
        when(deserializer.deserialize(any())).thenReturn(getCallbackRequest(buildNewCallbackRequestString()));

        notificationsController.sendUpdateFrcNotifications(AUTH_TOKEN, buildNewCallbackRequestString());
        verify(notificationService, times(1)).sendUpdateFrcInformationEmailToAppSolicitor(any());
        verify(notificationService, never()).sendUpdateFrcInformationEmailToRespondentSolicitor(any());
        verify(notificationService, times(1)).sendUpdateFrcInformationEmailToCourt(any());
        verify(paperNotificationService, times(1)).printUpdateFrcInformationNotification(any(), any());
    }

    private CallbackRequest createNewCallbackRequestWithFinalOrder() {
        CallbackRequest callbackRequest = buildNewCallbackRequest();

        ArrayList<DirectionOrderCollection> finalOrderCollection = new ArrayList<>();
        finalOrderCollection.add(DirectionOrderCollection.builder()
            .value(DirectionOrder.builder()
                .uploadDraftDocument(new Document())
                .build())
            .build());

        callbackRequest.getCaseDetails().getCaseData().setFinalOrderCollection(finalOrderCollection);

        return callbackRequest;
    }

    private OldCallbackRequest createCallbackRequestWithFinalOrder() {
        OldCallbackRequest callbackRequest = buildCallbackRequest();

        ArrayList<HearingOrderCollectionData> finalOrderCollection = new ArrayList<>();
        finalOrderCollection.add(HearingOrderCollectionData.builder()
            .hearingOrderDocuments(HearingOrderDocument
                .builder()
                .uploadDraftDocument(new CaseDocument())
                .build())
            .build());

        callbackRequest.getCaseDetails().getData().put(FINAL_ORDER_COLLECTION, finalOrderCollection);

        return callbackRequest;
    }

    private String createCallbackRequestWithFinalOrderString() throws JsonProcessingException {
        return objectMapper.writeValueAsString(createCallbackRequestWithFinalOrder());
    }

    protected String buildNewCallbackRequestStringConsentedNoAgreeEmails() throws JsonProcessingException {
        FinremCaseData caseData = new FinremCaseData();
        caseData.setCcdCaseType(CaseType.CONSENTED);
        caseData.getContactDetailsWrapper().setApplicantSolicitorConsentForEmails(YesOrNo.NO);
        caseData.getContactDetailsWrapper().setSolicitorAgreeToReceiveEmails(YesOrNo.NO);
        caseData.setPaperApplication(YesOrNo.NO);
        FinremCaseDetails caseDetails = FinremCaseDetails.builder().caseType(CaseType.CONSENTED).id(123L)
                .caseData(caseData).build();
        return objectMapper.writeValueAsString(
            CallbackRequest.builder()
                .eventType(EventType.PREPARE_FOR_HEARING)
                .caseDetails(caseDetails)
                .build());
    }

    protected String buildNewCallbackRequestStringConsentedPaper() throws JsonProcessingException {
        FinremCaseData caseData = new FinremCaseData();
        caseData.setCcdCaseType(CaseType.CONSENTED);
        caseData.getContactDetailsWrapper().setApplicantSolicitorConsentForEmails(YesOrNo.YES);
        caseData.getContactDetailsWrapper().setSolicitorAgreeToReceiveEmails(YesOrNo.YES);
        caseData.setPaperApplication(YesOrNo.YES);
        FinremCaseDetails caseDetails = FinremCaseDetails.builder().caseType(CaseType.CONSENTED).id(123L)
                .caseData(caseData).build();
        return objectMapper.writeValueAsString(
            CallbackRequest.builder()
                .eventType(EventType.PREPARE_FOR_HEARING)
                .caseDetails(caseDetails)
                .build());
    }

    protected String buildNewCallbackRequestStringContestedPaper() throws JsonProcessingException {
        FinremCaseData caseData = new FinremCaseData();
        caseData.setCcdCaseType(CaseType.CONTESTED);
        caseData.getContactDetailsWrapper().setApplicantSolicitorConsentForEmails(YesOrNo.NO);
        caseData.getContactDetailsWrapper().setSolicitorAgreeToReceiveEmails(YesOrNo.NO);
        caseData.setPaperApplication(YesOrNo.YES);
        FinremCaseDetails caseDetails = FinremCaseDetails.builder().caseType(CaseType.CONTESTED).id(123L)
                .caseData(caseData).build();
        return objectMapper.writeValueAsString(
            CallbackRequest.builder()
                .eventType(EventType.PREPARE_FOR_HEARING)
                .caseDetails(caseDetails)
                .caseDetailsBefore(caseDetails)
                .build());
    }

    protected String buildNewCallbackRequestStringContestedNoAgree() throws JsonProcessingException {
        FinremCaseData caseData = new FinremCaseData();
        caseData.setCcdCaseType(CaseType.CONTESTED);
        caseData.getContactDetailsWrapper().setApplicantSolicitorConsentForEmails(YesOrNo.NO);
        caseData.getContactDetailsWrapper().setSolicitorAgreeToReceiveEmails(YesOrNo.NO);
        caseData.setPaperApplication(YesOrNo.NO);
        FinremCaseDetails caseDetails = FinremCaseDetails.builder().caseType(CaseType.CONTESTED).id(123L)
                .caseData(caseData).build();
        return objectMapper.writeValueAsString(
            CallbackRequest.builder()
                .eventType(EventType.PREPARE_FOR_HEARING)
                .caseDetails(caseDetails)
                .build());
    }
}
