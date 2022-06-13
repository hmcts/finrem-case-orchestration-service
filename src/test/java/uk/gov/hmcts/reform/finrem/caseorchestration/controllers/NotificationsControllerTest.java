package uk.gov.hmcts.reform.finrem.caseorchestration.controllers;

import com.fasterxml.jackson.core.JsonProcessingException;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.ArgumentCaptor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.test.context.junit4.SpringRunner;
import uk.gov.hmcts.reform.ccd.client.model.CallbackRequest;
import uk.gov.hmcts.reform.ccd.client.model.CaseDetails;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.CaseDocument;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.HearingOrderCollectionData;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.HearingOrderDocument;
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


import java.io.IOException;
import java.util.ArrayList;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.is;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.mockito.Mockito.when;
import static uk.gov.hmcts.reform.finrem.caseorchestration.OrchestrationConstants.CASE_TYPE_ID_CONSENTED;
import static uk.gov.hmcts.reform.finrem.caseorchestration.OrchestrationConstants.CASE_TYPE_ID_CONTESTED;
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
    @MockBean private HearingDocumentService hearingDocumentService;
    @MockBean private AdditionalHearingDocumentService additionalHearingDocumentService;
    @MockBean private CaseDataService caseDataService;
    @MockBean private TransferCourtService transferCourtService;
    @MockBean private FeatureToggleService featureToggleService;

    @Test
    public void sendHwfSuccessfulConfirmationEmailIfDigitalCase() {
        when(caseDataService.isConsentedApplication(any())).thenReturn(true);
        when(caseDataService.isPaperApplication(any())).thenReturn(false);
        when(caseDataService.isApplicantSolicitorAgreeToReceiveEmails(any())).thenReturn(true);

        notificationsController.sendHwfSuccessfulConfirmationNotification(AUTH_TOKEN, buildCallbackRequest());

        verify(notificationService).sendConsentedHWFSuccessfulConfirmationEmail(any());
        verifyNoInteractions(helpWithFeesDocumentService);
    }

    @Test
    public void shouldNotSendHwfSuccessfulConfirmationEmail() {
        when(caseDataService.isConsentedApplication(any())).thenReturn(true);
        when(caseDataService.isPaperApplication(any())).thenReturn(false);
        when(caseDataService.isApplicantSolicitorAgreeToReceiveEmails(any())).thenReturn(false);

        notificationsController.sendHwfSuccessfulConfirmationNotification(AUTH_TOKEN, buildCallbackRequest());

        verifyNoInteractions(notificationService);
    }

    @Test
    public void sendHwfSuccessfulNotificationLetterIfIsConsentedAndIsPaperApplication() {
        when(caseDataService.isConsentedApplication(any())).thenReturn(true);
        when(caseDataService.isPaperApplication(any())).thenReturn(true);

        notificationsController.sendHwfSuccessfulConfirmationNotification(AUTH_TOKEN, buildCallbackRequest());

        verify(paperNotificationService).printHwfSuccessfulNotification(any(CaseDetails.class), eq(AUTH_TOKEN));
        verifyNoInteractions(notificationService);
    }

    @Test
    public void sendAssignToJudgeConfirmationEmailIfDigitalCase() {
        when(caseDataService.isConsentedApplication(any())).thenReturn(true);
        when(caseDataService.isPaperApplication(any())).thenReturn(false);
        when(caseDataService.isApplicantSolicitorAgreeToReceiveEmails(any())).thenReturn(true);

        notificationsController.sendAssignToJudgeConfirmationNotification(AUTH_TOKEN, buildCallbackRequest());

        verify(notificationService).sendAssignToJudgeConfirmationEmailToApplicantSolicitor(any());
    }

    @Test
    public void shouldNotSendAssignToJudgeConfirmationEmail() {
        when(caseDataService.isConsentedApplication(any())).thenReturn(true);
        when(caseDataService.isPaperApplication(any())).thenReturn(true);

        notificationsController.sendAssignToJudgeConfirmationNotification(AUTH_TOKEN, buildCallbackRequest());

        verifyNoInteractions(notificationService);
    }

    @Test
    public void whenIsConsentedAndSolicitorAgreedToEmail_sendConsentOrderMadeConfirmationEmail() {
        when(caseDataService.isConsentedApplication(any())).thenReturn(true);
        when(caseDataService.isApplicantSolicitorAgreeToReceiveEmails(any())).thenReturn(true);

        notificationsController.sendConsentOrderMadeConfirmationEmail(buildCallbackRequest());

        verify(notificationService).sendConsentOrderMadeConfirmationEmailToApplicantSolicitor(any());
    }

    @Test
    public void whenIsConsentedAndSolicitorNotAgreedToEmail_shouldNotSendConsentOrderMadeConfirmationEmail() {
        when(caseDataService.isConsentedApplication(any())).thenReturn(true);
        when(caseDataService.isApplicantSolicitorAgreeToReceiveEmails(any())).thenReturn(false);

        notificationsController.sendConsentOrderMadeConfirmationEmail(buildCallbackRequest());

        verifyNoInteractions(notificationService);
    }

    @Test
    public void whenIsNotConsentedAndSolicitorAgreedToEmail_sendConsentOrderMadeConfirmationEmail() {
        when(caseDataService.isConsentedApplication(any())).thenReturn(false);
        when(caseDataService.isApplicantSolicitorAgreeToReceiveEmails(any())).thenReturn(true);

        notificationsController.sendConsentOrderMadeConfirmationEmail(buildCallbackRequest());

        verifyNoInteractions(notificationService);
    }

    @Test
    public void whenIsNotConsentedAndSolicitorNotAgreedToEmail_shouldNotSendConsentOrderMadeConfirmationEmail() {
        when(caseDataService.isConsentedApplication(any())).thenReturn(false);
        when(caseDataService.isApplicantSolicitorAgreeToReceiveEmails(any())).thenReturn(false);

        notificationsController.sendConsentOrderMadeConfirmationEmail(buildCallbackRequest());

        verifyNoInteractions(notificationService);
    }

    @Test
    public void sendConsentOrderNotApprovedEmail() {
        when(caseDataService.isConsentedApplication(any())).thenReturn(true);
        when(caseDataService.isApplicantSolicitorAgreeToReceiveEmails(any())).thenReturn(true);

        notificationsController.sendConsentOrderNotApprovedEmail(buildCallbackRequest());

        verify(notificationService).sendConsentOrderNotApprovedEmailToApplicantSolicitor(any());
    }

    @Test
    public void shouldNotSendConsentOrderNotApprovedEmail() {
        when(caseDataService.isConsentedApplication(any())).thenReturn(true);
        when(caseDataService.isApplicantSolicitorAgreeToReceiveEmails(any())).thenReturn(false);

        notificationsController.sendConsentOrderNotApprovedEmail(buildCallbackRequest());

        verifyNoInteractions(notificationService);
    }

    @Test
    public void sendConsentOrderAvailableEmail() {
        when(caseDataService.isConsentedApplication(any())).thenReturn(true);
        when(caseDataService.isApplicantSolicitorAgreeToReceiveEmails(any())).thenReturn(true);

        notificationsController.sendConsentOrderAvailableEmail(buildCallbackRequest());

        verify(notificationService).sendConsentOrderAvailableEmailToApplicantSolicitor(any());
    }

    @Test
    public void shouldNotSendConsentOrderAvailableEmail() {
        when(caseDataService.isConsentedApplication(any())).thenReturn(true);
        when(caseDataService.isApplicantSolicitorAgreeToReceiveEmails(any())).thenReturn(false);

        notificationsController.sendConsentOrderAvailableEmail(buildCallbackRequest());

        verifyNoInteractions(notificationService);
    }

    @Test
    public void sendConsentedHwfSuccessfulConfirmationEmail() {
        when(caseDataService.isConsentedApplication(any())).thenReturn(false);
        when(caseDataService.isApplicantSolicitorAgreeToReceiveEmails(any())).thenReturn(true);

        notificationsController.sendHwfSuccessfulConfirmationNotification(AUTH_TOKEN, buildCallbackRequest());

        verify(notificationService).sendContestedHwfSuccessfulConfirmationEmail(any());
    }

    @Test
    public void shouldNotSendContestedHwfSuccessfulEmail() {
        when(caseDataService.isConsentedApplication(any())).thenReturn(false);
        when(caseDataService.isApplicantSolicitorAgreeToReceiveEmails(any())).thenReturn(false);

        notificationsController.sendHwfSuccessfulConfirmationNotification(AUTH_TOKEN, buildCallbackRequest());

        verifyNoInteractions(notificationService);
    }

    @Test
    public void givenSolAgreedToEmails_and_noPreviousHearing_shouldSendPrepareForHearingEmail_and_PrintHearingDocuments() {
        when(caseDataService.isApplicantSolicitorAgreeToReceiveEmails(any())).thenReturn(true);
        when(caseDataService.isContestedPaperApplication(any())).thenReturn(true);
        when(featureToggleService.isRespondentJourneyEnabled()).thenReturn(true);
        when(notificationService.shouldEmailRespondentSolicitor(any())).thenReturn(true);
        when(hearingDocumentService.alreadyHadFirstHearing(any())).thenReturn(false);

        notificationsController.sendPrepareForHearingEmail(AUTH_TOKEN, buildCallbackRequest());

        verify(notificationService).sendPrepareForHearingEmailApplicant(any());
        verify(notificationService).sendPrepareForHearingEmailRespondent(any());
        verify(hearingDocumentService).sendFormCAndGForBulkPrint(any(), eq(AUTH_TOKEN));
    }

    @Test
    public void shouldNotSendPrepareForHearingEmailWhenNotAgreed() {
        when(featureToggleService.isRespondentJourneyEnabled()).thenReturn(true);
        when(notificationService.shouldEmailRespondentSolicitor(any())).thenReturn(false);

        notificationsController.sendPrepareForHearingEmail(AUTH_TOKEN, buildCallbackRequest());

        verify(notificationService, never()).sendPrepareForHearingEmailApplicant(any());
        verify(notificationService, never()).sendPrepareForHearingEmailRespondent(any());
    }

    @Test
    public void givenHadPreviousHearing_whenNotifyHearingInvoked_thenPrintAdditionalHearingDocuments() {
        when(caseDataService.isContestedPaperApplication(any())).thenReturn(true);
        when(hearingDocumentService.alreadyHadFirstHearing(any())).thenReturn(true);

        notificationsController.sendPrepareForHearingEmail(AUTH_TOKEN, buildCallbackRequest());

        verify(additionalHearingDocumentService).sendAdditionalHearingDocuments(eq(AUTH_TOKEN), any());
    }

    @Test
    public void shouldSendPrepareForHearingOrderSentEmailWhenAgreed() {
        when(caseDataService.isConsentedApplication(any())).thenReturn(false);
        when(caseDataService.isApplicantSolicitorAgreeToReceiveEmails(any())).thenReturn(true);

        notificationsController.sendPrepareForHearingOrderSentEmail(buildCallbackRequest());

        verify(notificationService).sendPrepareForHearingOrderSentEmailApplicant(any());
    }

    @Test
    public void shouldNotSendPrepareForHearingOrderSentEmailWhenNotAgreed() {
        when(caseDataService.isConsentedApplication(any())).thenReturn(true);
        when(caseDataService.isApplicantSolicitorAgreeToReceiveEmails(any())).thenReturn(false);

        notificationsController.sendPrepareForHearingOrderSentEmail(buildCallbackRequest());

        verify(notificationService, never()).sendPrepareForHearingOrderSentEmailApplicant(any());
    }

    @Test
    public void sendPrepareForHearingOrderSentEmail_shouldSendRespondentEmail() {
        when(notificationService.shouldEmailRespondentSolicitor(any())).thenReturn(true);
        when(featureToggleService.isRespondentJourneyEnabled()).thenReturn(true);

        notificationsController.sendPrepareForHearingOrderSentEmail(buildCallbackRequest());

        verify(notificationService).sendPrepareForHearingOrderSentEmailRespondent(any());
    }

    @Test
    public void sendPrepareForHearingOrderSentEmail_shouldNotSendRespondentEmail() {
        when(notificationService.shouldEmailRespondentSolicitor(any())).thenReturn(false);
        when(featureToggleService.isRespondentJourneyEnabled()).thenReturn(true);

        notificationsController.sendPrepareForHearingOrderSentEmail(buildCallbackRequest());

        verify(notificationService, never()).sendPrepareForHearingOrderSentEmailRespondent(any());
    }

    @Test
    public void sendPrepareForHearingOrderSentEmail_toggledOff() {
        when(notificationService.shouldEmailRespondentSolicitor(any())).thenReturn(true);
        when(featureToggleService.isRespondentJourneyEnabled()).thenReturn(false);

        notificationsController.sendPrepareForHearingOrderSentEmail(buildCallbackRequest());

        verify(notificationService, never()).sendPrepareForHearingOrderSentEmailRespondent(any());
    }

    @Test
    public void shouldSendContestedApplicationIssuedEmailWhenAgreed() {
        when(caseDataService.isConsentedApplication(any())).thenReturn(false);
        when(caseDataService.isApplicantSolicitorAgreeToReceiveEmails(any())).thenReturn(true);

        notificationsController.sendContestedApplicationIssuedEmail(buildCallbackRequest());

        verify(notificationService).sendContestedApplicationIssuedEmailToApplicantSolicitor(any());
    }

    @Test
    public void shouldNotSendContestedApplicationIssuedEmailWhenNotAgreed() {
        when(caseDataService.isConsentedApplication(any())).thenReturn(true);
        when(caseDataService.isApplicantSolicitorAgreeToReceiveEmails(any())).thenReturn(false);

        notificationsController.sendContestedApplicationIssuedEmail(buildCallbackRequest());

        verify(notificationService, never()).sendContestedApplicationIssuedEmailToApplicantSolicitor(any());
    }

    @Test
    public void shouldSendContestedApplicationIssuedEmailWhenAgreed_andNotifyRespondentSolicitorWhenShould() {
        when(featureToggleService.isRespondentJourneyEnabled()).thenReturn(true);
        when(notificationService.shouldEmailRespondentSolicitor(any())).thenReturn(true);

        notificationsController.sendContestedApplicationIssuedEmail(buildCallbackRequest());

        verify(notificationService).sendContestedApplicationIssuedEmailToRespondentSolicitor(any());
    }

    @Test
    public void shouldSendContestedApplicationIssuedEmailWhenAgreed_andNotSendRespondentNotificationWhenToggledOff() {
        when(featureToggleService.isRespondentJourneyEnabled()).thenReturn(false);
        when(notificationService.shouldEmailRespondentSolicitor(any())).thenReturn(true);

        notificationsController.sendContestedApplicationIssuedEmail(buildCallbackRequest());

        verify(notificationService, never()).sendContestedApplicationIssuedEmailToRespondentSolicitor(any());
    }

    @Test
    public void shouldNotSendContestedApplicationIssuedEmailWhenNotAgreed_andDontNotifyRespondentSolicitorWhenShouldNot() {
        when(featureToggleService.isRespondentJourneyEnabled()).thenReturn(true);
        when(notificationService.shouldEmailRespondentSolicitor(any())).thenReturn(false);

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
        when(notificationService.shouldEmailRespondentSolicitor(any())).thenReturn(true);
        when(caseDataService.isRespondentSolicitorResponsibleToDraftOrder(any())).thenReturn(true);
        when(featureToggleService.isRespondentJourneyEnabled()).thenReturn(true);

        notificationsController.sendDraftOrderEmail(buildCallbackRequest());

        verify(notificationService).sendSolicitorToDraftOrderEmailRespondent(any());
    }

    @Test
    public void shouldSendSolicitorToDraftOrderEmailRespondent_shouldNotSendEmail() {
        when(notificationService.shouldEmailRespondentSolicitor(any())).thenReturn(false);
        when(caseDataService.isRespondentSolicitorResponsibleToDraftOrder(any())).thenReturn(true);
        when(featureToggleService.isRespondentJourneyEnabled()).thenReturn(true);

        notificationsController.sendDraftOrderEmail(buildCallbackRequest());

        verify(notificationService, never()).sendSolicitorToDraftOrderEmailRespondent(any());
    }

    @Test
    public void shouldSendSolicitorToDraftOrderEmailRespondent_respSolicitorNotResponsible() {
        when(notificationService.shouldEmailRespondentSolicitor(any())).thenReturn(true);
        when(caseDataService.isRespondentSolicitorResponsibleToDraftOrder(any())).thenReturn(false);
        when(featureToggleService.isRespondentJourneyEnabled()).thenReturn(true);

        notificationsController.sendDraftOrderEmail(buildCallbackRequest());

        verify(notificationService, never()).sendSolicitorToDraftOrderEmailRespondent(any());
    }

    @Test
    public void shouldSendSolicitorToDraftOrderEmailRespondent_toggledOff() {
        when(notificationService.shouldEmailRespondentSolicitor(any())).thenReturn(true);
        when(caseDataService.isRespondentSolicitorResponsibleToDraftOrder(any())).thenReturn(true);
        when(featureToggleService.isRespondentJourneyEnabled()).thenReturn(false);

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
    public void sendContestOrderNotApprovedEmail() {
        when(caseDataService.isConsentedApplication(any())).thenReturn(false);
        when(caseDataService.isApplicantSolicitorAgreeToReceiveEmails(any())).thenReturn(true);

        notificationsController.sendConsentOrderNotApprovedEmail(createCallbackRequestWithFinalOrder());

        verify(notificationService).sendContestOrderNotApprovedEmailApplicant(any());
    }

    @Test
    public void shouldNotSendContestOrderNotApprovedEmail() {
        when(caseDataService.isConsentedApplication(any())).thenReturn(false);
        when(caseDataService.isApplicantSolicitorAgreeToReceiveEmails(any())).thenReturn(false);

        notificationsController.sendConsentOrderNotApprovedEmail(createCallbackRequestWithFinalOrder());

        verifyNoInteractions(notificationService);
    }

    @Test
    public void sendContestedConsentOrderApprovedEmailToApplicantSolicitor() {
        when(caseDataService.isApplicantSolicitorAgreeToReceiveEmails(any())).thenReturn(true);

        notificationsController.sendContestedConsentOrderApprovedEmail(buildCallbackRequest());

        verify(notificationService).sendContestedConsentOrderApprovedEmailToApplicantSolicitor(any());
    }

    @Test
    public void sendContestedConsentOrderApprovedEmailToRespondentSolicitor() {
        when(featureToggleService.isRespondentJourneyEnabled()).thenReturn(true);
        when(notificationService.shouldEmailRespondentSolicitor(any())).thenReturn(true);

        notificationsController.sendContestedConsentOrderApprovedEmail(buildCallbackRequest());

        verify(notificationService).sendContestedConsentOrderApprovedEmailToRespondentSolicitor(any());
    }

    @Test
    public void givenContestedCase_whenShouldSendRespondentNotification_thenShouldTriggerRespondentEmail() {
        when(featureToggleService.isRespondentJourneyEnabled()).thenReturn(true);
        when(notificationService.shouldEmailRespondentSolicitor(any())).thenReturn(true);

        CallbackRequest callbackRequest = buildCallbackRequest();
        callbackRequest.getCaseDetails().setCaseTypeId(CASE_TYPE_ID_CONTESTED);
        notificationsController.sendConsentOrderNotApprovedEmail(callbackRequest);

        verify(notificationService).sendContestOrderNotApprovedEmailRespondent(any());
        verify(notificationService, never()).sendConsentOrderNotApprovedEmailToRespondentSolicitor(any());
    }

    @Test
    public void givenConsentedCase_whenShouldSendRespondentNotification_thenShouldNotTriggerContestedRespondentEmail() {
        when(caseDataService.isConsentedApplication(any())).thenReturn(true);
        when(featureToggleService.isRespondentJourneyEnabled()).thenReturn(true);
        when(notificationService.shouldEmailRespondentSolicitor(any())).thenReturn(true);

        notificationsController.sendConsentOrderNotApprovedEmail(buildCallbackRequest());

        verify(notificationService, never()).sendContestOrderNotApprovedEmailRespondent(any());
    }

    @Test
    public void givenConsentedCase_whenSendConsentOrderNotApproved_thenShouldTriggerConsentedRespondentEmail() {
        when(caseDataService.isConsentedApplication(any())).thenReturn(true);
        when(featureToggleService.isRespondentJourneyEnabled()).thenReturn(true);
        when(notificationService.shouldEmailRespondentSolicitor(any())).thenReturn(true);

        CallbackRequest callbackRequest = buildCallbackRequest();
        callbackRequest.getCaseDetails().setCaseTypeId(CASE_TYPE_ID_CONSENTED);
        notificationsController.sendConsentOrderNotApprovedEmail(callbackRequest);

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
        when(featureToggleService.isRespondentJourneyEnabled()).thenReturn(true);
        when(notificationService.shouldEmailRespondentSolicitor(any())).thenReturn(false);

        notificationsController.sendContestedConsentOrderApprovedEmail(buildCallbackRequest());

        verify(notificationService,
            never()).sendContestedConsentOrderApprovedEmailToRespondentSolicitor(any());
    }

    @Test
    public void shouldNotSendContestedConsentOrderApprovedEmailToRespondentSolicitorWhenRespSolNotificationsToggledOff() {
        when(featureToggleService.isRespondentJourneyEnabled()).thenReturn(false);
        when(notificationService.shouldEmailRespondentSolicitor(any())).thenReturn(true);

        notificationsController.sendContestedConsentOrderApprovedEmail(buildCallbackRequest());

        verify(notificationService,
            never()).sendContestedConsentOrderApprovedEmailToRespondentSolicitor(any());
    }

    @Test
    public void shouldNotSendGeneralOrderEmail() {
        when(caseDataService.isConsentedApplication(any())).thenReturn(true);
        when(caseDataService.isApplicantSolicitorAgreeToReceiveEmails(any())).thenReturn(false);

        notificationsController.sendGeneralOrderRaisedEmail(buildCallbackRequest());

        verifyNoInteractions(notificationService);
    }

    @Test
    public void sendContestedConsentGeneralOrderEmail() {
        when(caseDataService.isConsentedApplication(any())).thenReturn(false);
        when(caseDataService.isConsentedInContestedCase(any())).thenReturn(true);
        when(caseDataService.isApplicantSolicitorAgreeToReceiveEmails(any())).thenReturn(true);
        when(featureToggleService.isRespondentJourneyEnabled()).thenReturn(false);

        notificationsController.sendGeneralOrderRaisedEmail(buildCallbackRequest());

        verify(notificationService).sendContestedConsentGeneralOrderEmailApplicantSolicitor(any());
    }

    @Test
    public void sendContestedGeneralOrderEmails() {
        when(caseDataService.isConsentedApplication(any())).thenReturn(false);
        when(caseDataService.isConsentedInContestedCase(any())).thenReturn(false);
        when(caseDataService.isApplicantSolicitorAgreeToReceiveEmails(any())).thenReturn(true);
        when(featureToggleService.isRespondentJourneyEnabled()).thenReturn(true);
        when(notificationService.shouldEmailRespondentSolicitor(any())).thenReturn(true);

        notificationsController.sendGeneralOrderRaisedEmail(buildCallbackRequest());

        verify(notificationService).sendContestedGeneralOrderEmailApplicant(any());
        verify(notificationService).sendContestedGeneralOrderEmailRespondent(any());
    }

    @Test
    public void whenShouldNotSendContestedGeneralOrderEmailToRespondent_ThenTheEmailIsNotIssued() {
        when(featureToggleService.isRespondentJourneyEnabled()).thenReturn(true);
        when(notificationService.shouldEmailRespondentSolicitor(any())).thenReturn(false);

        notificationsController.sendGeneralOrderRaisedEmail(buildCallbackRequest());

        verify(notificationService, never()).sendContestedConsentGeneralOrderEmailRespondentSolicitor(any());
        verify(notificationService, never()).sendContestedGeneralOrderEmailRespondent(any());
    }

    @Test
    public void whenToggleEnabledAndShouldSendEmailToRespSolicitor_thenSendsEmail() {
        final ArgumentCaptor<CaseDetails> requestCaptor = ArgumentCaptor.forClass(CaseDetails.class);

        when(featureToggleService.isRespondentJourneyEnabled()).thenReturn(true);
        when(notificationService.shouldEmailRespondentSolicitor(any())).thenReturn(true);

        notificationsController.sendAssignToJudgeConfirmationNotification(AUTH_TOKEN, buildCallbackRequest());

        verify(notificationService).sendAssignToJudgeConfirmationEmailToRespondentSolicitor(any());

        verify(notificationService).sendAssignToJudgeConfirmationEmailToRespondentSolicitor(requestCaptor.capture());
        assertThat(requestCaptor.getValue().getId(), is(123L));
    }

    @Test
    public void whenToggleEnabledAndShouldNotSendEmailToRespSolicitor_thenDoesNotSendEmail() {
        when(featureToggleService.isRespondentJourneyEnabled()).thenReturn(true);
        when(notificationService.shouldEmailRespondentSolicitor(any())).thenReturn(false);

        notificationsController.sendAssignToJudgeConfirmationNotification(AUTH_TOKEN, buildCallbackRequest());

        verify(notificationService, never()).sendAssignToJudgeConfirmationEmailToRespondentSolicitor(any());
    }

    @Test
    public void whenToggleDisabledAndShouldSendEmailToRespSolicitor_thenDoesNotSendEmail() {
        when(featureToggleService.isRespondentJourneyEnabled()).thenReturn(false);
        when(notificationService.shouldEmailRespondentSolicitor(any())).thenReturn(true);

        notificationsController.sendAssignToJudgeConfirmationNotification(AUTH_TOKEN, buildCallbackRequest());

        verify(notificationService, never()).sendAssignToJudgeConfirmationEmailToRespondentSolicitor(any());
    }

    @Test
    public void whenToggleDisabledAndShouldNotSendEmailToRespSolicitor_thenDoesNotSendEmail() {
        when(featureToggleService.isRespondentJourneyEnabled()).thenReturn(false);
        when(notificationService.shouldEmailRespondentSolicitor(any())).thenReturn(false);

        notificationsController.sendAssignToJudgeConfirmationNotification(AUTH_TOKEN, buildCallbackRequest());

        verify(notificationService, never()).sendAssignToJudgeConfirmationEmailToRespondentSolicitor(any());
    }

    @Test
    public void shouldSendContestedConsentGeneralOrderEmailToRespondentInConsentedInContestedCase() {
        when(caseDataService.isConsentedApplication(any())).thenReturn(false);
        when(caseDataService.isConsentedInContestedCase(any())).thenReturn(true);
        when(featureToggleService.isRespondentJourneyEnabled()).thenReturn(true);
        when(notificationService.shouldEmailRespondentSolicitor(any())).thenReturn(true);

        notificationsController.sendGeneralOrderRaisedEmail(buildCallbackRequest());

        verify(notificationService).sendContestedConsentGeneralOrderEmailRespondentSolicitor(any());
        verify(notificationService, never()).sendContestedGeneralOrderEmailRespondent(any());
        verify(notificationService, never()).sendConsentedGeneralOrderEmailToRespondentSolicitor(any());
    }

    @Test
    public void sendConsentedGeneralOrderEmail() {
        when(caseDataService.isConsentedApplication(any())).thenReturn(true);
        when(caseDataService.isApplicantSolicitorAgreeToReceiveEmails(any())).thenReturn(true);
        when(featureToggleService.isRespondentJourneyEnabled()).thenReturn(true);
        when(notificationService.shouldEmailRespondentSolicitor(any())).thenReturn(true);

        notificationsController.sendGeneralOrderRaisedEmail(buildCallbackRequest());

        verify(notificationService).sendConsentedGeneralOrderEmailToApplicantSolicitor(any());
        verify(notificationService).sendConsentedGeneralOrderEmailToRespondentSolicitor(any());
    }

    @Test
    public void shouldNotSendContestedConsentOrderApprovedEmail() {
        when(caseDataService.isConsentedApplication(any())).thenReturn(false);

        notificationsController.sendContestedConsentOrderApprovedEmail(buildCallbackRequest());

        verifyNoInteractions(notificationService);
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
        when(featureToggleService.isRespondentJourneyEnabled()).thenReturn(true);
        when(notificationService.shouldEmailRespondentSolicitor(any())).thenReturn(true);

        notificationsController.sendContestedConsentOrderNotApprovedEmail(buildCallbackRequest());

        verify(notificationService).sendContestedConsentOrderNotApprovedEmailApplicantSolicitor(any());
        verify(notificationService).sendContestedConsentOrderNotApprovedEmailRespondentSolicitor(any());
    }

    @Test
    public void shouldNotSendContestedConsentOrderNotApprovedEmail() {
        when(featureToggleService.isRespondentJourneyEnabled()).thenReturn(true);
        when(notificationService.shouldEmailRespondentSolicitor(any())).thenReturn(false);

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
    public void givenConsentedCase_whenToggleEnabledAndShouldSendEmailToRespSolicitor_thenSendsEmail() {
        CallbackRequest callbackRequest = buildCallbackRequest();
        callbackRequest.getCaseDetails().setCaseTypeId(CASE_TYPE_ID_CONSENTED);
        when(featureToggleService.isRespondentJourneyEnabled()).thenReturn(true);
        when(notificationService.shouldEmailRespondentSolicitor(any())).thenReturn(true);
        notificationsController.sendConsentOrderAvailableEmail(callbackRequest);
        verify(notificationService).sendConsentOrderAvailableEmailToRespondentSolicitor(callbackRequest.getCaseDetails());
    }

    @Test
    public void givenConsentedCase_whenToggleEnabledAndShouldNotSendEmailToRespSolicitor_thenDoesNotSendEmail() {
        when(featureToggleService.isRespondentJourneyEnabled()).thenReturn(true);
        when(notificationService.shouldEmailRespondentSolicitor(any())).thenReturn(false);

        notificationsController.sendConsentOrderAvailableEmail(buildCallbackRequest());

        verify(notificationService, never()).sendConsentOrderAvailableEmailToRespondentSolicitor(any());
    }

    @Test
    public void givenConsentedCase_whenToggleDisabledAndShouldSendEmailToRespSolicitor_thenDoesNotSendEmail() {
        when(featureToggleService.isRespondentJourneyEnabled()).thenReturn(false);
        when(notificationService.shouldEmailRespondentSolicitor(any())).thenReturn(true);

        notificationsController.sendConsentOrderAvailableEmail(buildCallbackRequest());

        verify(notificationService, never()).sendConsentOrderAvailableEmailToRespondentSolicitor(any());
    }

    @Test
    public void givenConsentedCase_whenToggleDisabledAndShouldNotSendEmailToRespSolicitor_thenDoesNotSendEmail() {
        when(featureToggleService.isRespondentJourneyEnabled()).thenReturn(false);
        when(notificationService.shouldEmailRespondentSolicitor(any())).thenReturn(false);

        notificationsController.sendConsentOrderAvailableEmail(buildCallbackRequest());

        verify(notificationService, never()).sendConsentOrderAvailableEmailToRespondentSolicitor(any());
    }

    @Test
    public void sendConsentOrderMadeEmailToRespSolicitor() {
        CallbackRequest callbackRequest = buildCallbackRequest();
        when(featureToggleService.isRespondentJourneyEnabled()).thenReturn(true);
        when(notificationService.shouldEmailRespondentSolicitor(any())).thenReturn(true);
        notificationsController.sendConsentOrderMadeConfirmationEmail(callbackRequest);
        verify(notificationService).sendConsentOrderMadeConfirmationEmailToRespondentSolicitor(callbackRequest.getCaseDetails());
    }

    @Test
    public void doesNotSendConsentOrderMadeEmailToRespSolicitor() {
        CallbackRequest callbackRequest = buildCallbackRequest();
        callbackRequest.getCaseDetails().setCaseTypeId(CASE_TYPE_ID_CONSENTED);
        when(featureToggleService.isRespondentJourneyEnabled()).thenReturn(true);
        when(notificationService.shouldEmailRespondentSolicitor(any())).thenReturn(false);
        notificationsController.sendConsentOrderMadeConfirmationEmail(callbackRequest);
        verify(notificationService, never()).sendConsentOrderMadeConfirmationEmailToRespondentSolicitor(callbackRequest.getCaseDetails());
    }

    @Test
    public void doesNotSendConsentOrderMadeEmailToRespSolicitor_toggledOffAndgivenSolAgreedToEmails() {
        CallbackRequest callbackRequest = buildCallbackRequest();
        callbackRequest.getCaseDetails().setCaseTypeId(CASE_TYPE_ID_CONSENTED);
        when(featureToggleService.isRespondentJourneyEnabled()).thenReturn(false);
        when(notificationService.shouldEmailRespondentSolicitor(any())).thenReturn(true);
        notificationsController.sendConsentOrderMadeConfirmationEmail(callbackRequest);
        verify(notificationService, never()).sendConsentOrderMadeConfirmationEmailToRespondentSolicitor(callbackRequest.getCaseDetails());
    }

    @Test
    public void doesNotSendConsentOrderMadeEmailToRespSolicitor_toggledOff() {
        CallbackRequest callbackRequest = buildCallbackRequest();
        callbackRequest.getCaseDetails().setCaseTypeId(CASE_TYPE_ID_CONSENTED);
        when(featureToggleService.isRespondentJourneyEnabled()).thenReturn(false);
        when(notificationService.shouldEmailRespondentSolicitor(any())).thenReturn(false);
        notificationsController.sendConsentOrderMadeConfirmationEmail(callbackRequest);
        verify(notificationService, never()).sendConsentOrderMadeConfirmationEmailToRespondentSolicitor(callbackRequest.getCaseDetails());
    }

    @Test
    public void whenConsentOrderNotApprovedSentEmail_thenNotificationEmailsSentToSolicitors() {
        when(featureToggleService.isRespondentJourneyEnabled()).thenReturn(true);
        when(notificationService.shouldEmailApplicantSolicitor(any())).thenReturn(true);
        when(notificationService.shouldEmailRespondentSolicitor(any())).thenReturn(true);

        notificationsController.sendConsentOrderNotApprovedSentEmail(buildCallbackRequest());

        verify(notificationService).sendConsentOrderNotApprovedSentEmailToApplicantSolicitor(any());
        verify(notificationService).sendConsentOrderNotApprovedSentEmailToRespondentSolicitor(any());
    }

    @Test
    public void given_RespondentJourneyIsToggledOff_whenConsentOrderNotApprovedSentEmail_thenNoEmailsSentToSolicitors() {
        when(featureToggleService.isRespondentJourneyEnabled()).thenReturn(false);
        when(notificationService.shouldEmailApplicantSolicitor(any())).thenReturn(true);
        when(notificationService.shouldEmailRespondentSolicitor(any())).thenReturn(true);

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
        when(notificationService.shouldEmailRespondentSolicitor(any())).thenReturn(true);

        notificationsController.sendInterimHearingNotification(buildCallbackInterimRequest());

        verify(notificationService, times(1)).sendInterimNotificationEmailToApplicantSolicitor(any());
        verify(notificationService, times(1)).sendInterimNotificationEmailToRespondentSolicitor(any());
    }

    @Test
    public void shouldNotSendInterimHearingWhenNotAgreed() {
        when(caseDataService.isPaperApplication(any())).thenReturn(false);
        when(caseDataService.isContestedApplication(any())).thenReturn(true);
        when(caseDataService.isApplicantSolicitorAgreeToReceiveEmails(any())).thenReturn(false);
        when(notificationService.shouldEmailRespondentSolicitor(any())).thenReturn(false);

        notificationsController.sendInterimHearingNotification(buildCallbackInterimRequest());

        verify(notificationService, never()).sendInterimNotificationEmailToApplicantSolicitor(any());
        verify(notificationService, never()).sendInterimNotificationEmailToRespondentSolicitor(any());
    }

    @Test
    public void shouldSendInterimHearingNotificationWhenApplicantAgreedButRespondentNotAgreed() {
        when(caseDataService.isPaperApplication(any())).thenReturn(false);
        when(caseDataService.isContestedApplication(any())).thenReturn(true);
        when(caseDataService.isApplicantSolicitorAgreeToReceiveEmails(any())).thenReturn(true);
        when(notificationService.shouldEmailRespondentSolicitor(any())).thenReturn(false);

        notificationsController.sendInterimHearingNotification(buildCallbackInterimRequest());

        verify(notificationService, times(1)).sendInterimNotificationEmailToApplicantSolicitor(any());
        verify(notificationService, never()).sendInterimNotificationEmailToRespondentSolicitor(any());
    }

    @Test
    public void shouldSendInterimHearingNotificationWhenApplicantNotAgreedButRespondentAgreed() {
        when(caseDataService.isPaperApplication(any())).thenReturn(false);
        when(caseDataService.isContestedApplication(any())).thenReturn(true);
        when(caseDataService.isApplicantSolicitorAgreeToReceiveEmails(any())).thenReturn(false);
        when(notificationService.shouldEmailRespondentSolicitor(any())).thenReturn(true);

        notificationsController.sendInterimHearingNotification(buildCallbackInterimRequest());

        verify(notificationService, never()).sendInterimNotificationEmailToApplicantSolicitor(any());
        verify(notificationService, times(1)).sendInterimNotificationEmailToRespondentSolicitor(any());
    }

    @Test
    public void givenNoticeOfChangeWhenSendNoticeOfChangeNotificationsThenSendNoticeOfChangeServiceCalled() {

        notificationsController.sendNoticeOfChangeNotifications("authToken", buildCallbackRequestWithBeforeCaseDetails());

        verify(notificationService, times(1)).sendNoticeOfChangeEmail(any());

        verify(nocLetterNotificationService, times(1)).sendNoticeOfChangeLetters(any(CaseDetails.class), any(CaseDetails.class), anyString());
    }

    @Test
    public void givenNoticeOfChangeAsCaseworker_whenSendNoCNotifications_ThenSendNoticeOfChangeServiceCalled() {
        when(featureToggleService.isCaseworkerNoCEnabled()).thenReturn(true);
        notificationsController.sendNoticeOfChangeNotificationsCaseworker("authtoken",
            buildNoCCaseworkerCallbackRequest());

        verify(notificationService, times(1)).sendNoticeOfChangeEmailCaseworker(any());

        verify(nocLetterNotificationService, times(1))
            .sendNoticeOfChangeLetters(any(CaseDetails.class), any(CaseDetails.class), anyString());
    }

    @Test
    public void givenUpdateFrc_whenSendEmail_thenNotificationServiceCalledThreeTimes() throws JsonProcessingException {
        when(featureToggleService.isRespondentJourneyEnabled()).thenReturn(true);
        when(caseDataService.isApplicantSolicitorAgreeToReceiveEmails(any(CaseDetails.class))).thenReturn(true);
        when(notificationService.shouldEmailRespondentSolicitor(any())).thenReturn(true);

        notificationsController.sendUpdateFrcNotifications(AUTH_TOKEN, buildCallbackRequest());
        verify(notificationService, times(1)).sendUpdateFrcInformationEmailToAppSolicitor(any());
        verify(notificationService, times(1)).sendUpdateFrcInformationEmailToRespondentSolicitor(any());
        verify(notificationService, times(1)).sendUpdateFrcInformationEmailToCourt(any());
        verify(paperNotificationService, times(1)).printUpdateFrcInformationNotification(any(), any());
    }

    @Test
    public void givenUpdateFrc_whenAppSolNotAgreeToReceiveEmails_thenNotificationServiceCalledTwice() throws JsonProcessingException {
        when(featureToggleService.isRespondentJourneyEnabled()).thenReturn(true);
        when(caseDataService.isApplicantSolicitorAgreeToReceiveEmails(any(CaseDetails.class))).thenReturn(false);
        when(notificationService.shouldEmailRespondentSolicitor(any())).thenReturn(true);

        notificationsController.sendUpdateFrcNotifications(AUTH_TOKEN, buildCallbackRequest());
        verify(notificationService, never()).sendUpdateFrcInformationEmailToAppSolicitor(any());
        verify(notificationService, times(1)).sendUpdateFrcInformationEmailToRespondentSolicitor(any());
        verify(notificationService, times(1)).sendUpdateFrcInformationEmailToCourt(any());
        verify(paperNotificationService, times(1)).printUpdateFrcInformationNotification(any(), any());
    }

    @Test
    public void givenUpdateFrc_whenRespSolNotAgreeToReceiveEmails_thenNotificationServiceCalledTwice() throws JsonProcessingException {
        when(featureToggleService.isRespondentJourneyEnabled()).thenReturn(true);
        when(caseDataService.isApplicantSolicitorAgreeToReceiveEmails(any(CaseDetails.class))).thenReturn(true);
        when(notificationService.shouldEmailRespondentSolicitor(any())).thenReturn(false);

        notificationsController.sendUpdateFrcNotifications(AUTH_TOKEN, buildCallbackRequest());
        verify(notificationService, times(1)).sendUpdateFrcInformationEmailToAppSolicitor(any());
        verify(notificationService, never()).sendUpdateFrcInformationEmailToRespondentSolicitor(any());
        verify(notificationService, times(1)).sendUpdateFrcInformationEmailToCourt(any());
        verify(paperNotificationService, times(1)).printUpdateFrcInformationNotification(any(), any());
    }

    private CallbackRequest createCallbackRequestWithFinalOrder() {
        CallbackRequest callbackRequest = buildCallbackRequest();

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
}
