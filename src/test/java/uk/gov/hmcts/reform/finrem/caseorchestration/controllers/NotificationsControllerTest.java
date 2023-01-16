package uk.gov.hmcts.reform.finrem.caseorchestration.controllers;

import com.fasterxml.jackson.core.JsonProcessingException;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringRunner;
import uk.gov.hmcts.reform.ccd.client.model.CallbackRequest;
import uk.gov.hmcts.reform.ccd.client.model.CaseDetails;
import uk.gov.hmcts.reform.finrem.caseorchestration.config.DocumentConfiguration;
import uk.gov.hmcts.reform.finrem.caseorchestration.helper.DocumentHelper;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.CaseDocument;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.CaseType;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.HearingOrderCollectionData;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.HearingOrderDocument;
import uk.gov.hmcts.reform.finrem.caseorchestration.service.BulkPrintService;
import uk.gov.hmcts.reform.finrem.caseorchestration.service.CaseDataService;
import uk.gov.hmcts.reform.finrem.caseorchestration.service.FeatureToggleService;
import uk.gov.hmcts.reform.finrem.caseorchestration.service.GeneralEmailService;
import uk.gov.hmcts.reform.finrem.caseorchestration.service.GenericDocumentService;
import uk.gov.hmcts.reform.finrem.caseorchestration.service.HelpWithFeesDocumentService;
import uk.gov.hmcts.reform.finrem.caseorchestration.service.NotificationService;
import uk.gov.hmcts.reform.finrem.caseorchestration.service.PaperNotificationService;
import uk.gov.hmcts.reform.finrem.caseorchestration.service.TransferCourtService;
import uk.gov.hmcts.reform.finrem.caseorchestration.service.correspondence.hwf.HwfConsentedApplicantCorresponder;
import uk.gov.hmcts.reform.finrem.caseorchestration.service.correspondence.hwf.HwfContestedApplicantCorresponder;
import uk.gov.hmcts.reform.finrem.caseorchestration.service.correspondence.hwf.HwfCorrespondenceService;
import uk.gov.hmcts.reform.finrem.caseorchestration.service.correspondence.updatefrc.UpdateFrcCorrespondenceService;
import uk.gov.hmcts.reform.finrem.caseorchestration.service.correspondence.updatefrc.UpdateFrcLetterOrEmailAllSolicitorsCorresponder;
import uk.gov.hmcts.reform.finrem.caseorchestration.service.noc.NocLetterNotificationService;
import uk.gov.hmcts.reform.finrem.caseorchestration.service.noc.solicitors.CheckApplicantSolicitorIsDigitalService;
import uk.gov.hmcts.reform.finrem.caseorchestration.service.noc.solicitors.CheckRespondentSolicitorIsDigitalService;
import uk.gov.hmcts.reform.finrem.caseorchestration.service.updatefrc.generators.UpdateFrcInfoLetterDetailsGenerator;
import uk.gov.hmcts.reform.finrem.caseorchestration.service.updatefrc.service.UpdateFrcInfoRespondentDocumentService;

import java.io.IOException;
import java.util.ArrayList;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.mockito.Mockito.verifyNoMoreInteractions;
import static org.mockito.Mockito.when;
import static uk.gov.hmcts.reform.finrem.caseorchestration.OrchestrationConstants.YES_VALUE;
import static uk.gov.hmcts.reform.finrem.caseorchestration.TestConstants.AUTH_TOKEN;
import static uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.CCDConfigConstant.FINAL_ORDER_COLLECTION;
import static uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.CCDConfigConstant.IS_NOC_REJECTED;


@RunWith(SpringRunner.class)
@WebMvcTest(NotificationsController.class)
@ContextConfiguration(classes = {HwfCorrespondenceService.class,
    HwfConsentedApplicantCorresponder.class,
    HwfContestedApplicantCorresponder.class,
    UpdateFrcCorrespondenceService.class,
    UpdateFrcLetterOrEmailAllSolicitorsCorresponder.class,
    UpdateFrcInfoRespondentDocumentService.class,
    UpdateFrcInfoLetterDetailsGenerator.class,
    DocumentHelper.class})
public class NotificationsControllerTest extends BaseControllerTest {

    @Autowired
    private NotificationsController notificationsController;
    @MockBean
    private NocLetterNotificationService nocLetterNotificationService;
    @MockBean
    private NotificationService notificationService;
    @MockBean
    private PaperNotificationService paperNotificationService;
    @MockBean
    private GeneralEmailService generalEmailService;
    @MockBean
    private HelpWithFeesDocumentService helpWithFeesDocumentService;
    @MockBean
    private CaseDataService caseDataService;
    @MockBean
    private TransferCourtService transferCourtService;
    @MockBean
    private FeatureToggleService featureToggleService;
    @MockBean
    private CheckApplicantSolicitorIsDigitalService checkApplicantSolicitorIsDigitalService;
    @MockBean
    private CheckRespondentSolicitorIsDigitalService checkRespondentSolicitorIsDigitalService;
    @MockBean
    private BulkPrintService bulkPrintService;
    @MockBean
    private GenericDocumentService genericDocumentService;
    @MockBean
    private DocumentConfiguration documentConfiguration;

    @Test
    public void sendHwfSuccessfulConfirmationEmailIfDigitalCase() {
        when(caseDataService.isConsentedApplication(any())).thenReturn(true);
        when(notificationService.isApplicantSolicitorDigitalAndEmailPopulated(any())).thenReturn(true);

        notificationsController.sendHwfSuccessfulConfirmationNotification(AUTH_TOKEN, buildCallbackRequest());

        verify(notificationService).sendConsentedHWFSuccessfulConfirmationEmail(any());
        verifyNoInteractions(helpWithFeesDocumentService);
    }

    @Test
    public void shouldNotSendHwfSuccessfulConfirmationEmail() {
        when(caseDataService.isConsentedApplication(any())).thenReturn(true);
        when(notificationService.isApplicantSolicitorDigitalAndEmailPopulated(any())).thenReturn(false);

        notificationsController.sendHwfSuccessfulConfirmationNotification(AUTH_TOKEN, buildCallbackRequest());

        verify(notificationService).isApplicantSolicitorDigitalAndEmailPopulated(any());
        verifyNoMoreInteractions(notificationService);
    }

    @Test
    public void sendHwfSuccessfulNotificationLetterIfIsConsentedAndIsPaperApplication() {
        when(caseDataService.isConsentedApplication(any())).thenReturn(true);
        when(notificationService.isApplicantSolicitorDigitalAndEmailPopulated(any())).thenReturn(false);

        notificationsController.sendHwfSuccessfulConfirmationNotification(AUTH_TOKEN, buildCallbackRequest());

        verify(notificationService).isApplicantSolicitorDigitalAndEmailPopulated(any());
        verifyNoMoreInteractions(notificationService);
        verify(bulkPrintService).sendDocumentForPrint(any(), any());
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
    public void shouldSendAssignToJudgeConfirmationEmailIfRespondentSolicitorIsAcceptingEmail() {
        when(notificationService.isRespondentSolicitorEmailCommunicationEnabled(any())).thenReturn(true);

        notificationsController.sendAssignToJudgeConfirmationNotification(AUTH_TOKEN, buildCallbackRequest());

        verify(notificationService).sendAssignToJudgeConfirmationEmailToRespondentSolicitor(any());
    }

    @Test
    public void shouldNotSendAssignToJudgeConfirmationEmailIfRespondentSolicitorIsAcceptingEmail() {
        when(notificationService.isRespondentSolicitorEmailCommunicationEnabled(any())).thenReturn(false);

        verify(notificationService, never()).sendAssignToJudgeConfirmationEmailToRespondentSolicitor(any());
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

        verify(notificationService, never()).sendConsentOrderMadeConfirmationEmailToApplicantSolicitor(any());
        verify(notificationService, never()).sendConsentOrderMadeConfirmationEmailToRespondentSolicitor(any());
    }

    @Test
    public void whenIsNotConsentedAndSolicitorAgreedToEmail_sendConsentOrderMadeConfirmationEmail() {
        when(caseDataService.isConsentedApplication(any())).thenReturn(false);
        when(caseDataService.isApplicantSolicitorAgreeToReceiveEmails(any())).thenReturn(true);

        notificationsController.sendConsentOrderMadeConfirmationEmail(buildCallbackRequest());

        verify(notificationService, never()).sendConsentOrderMadeConfirmationEmailToApplicantSolicitor(any());
        verify(notificationService, never()).sendConsentOrderMadeConfirmationEmailToRespondentSolicitor(any());
    }

    @Test
    public void whenIsNotConsentedAndSolicitorNotAgreedToEmail_shouldNotSendConsentOrderMadeConfirmationEmail() {
        when(caseDataService.isConsentedApplication(any())).thenReturn(false);
        when(caseDataService.isApplicantSolicitorAgreeToReceiveEmails(any())).thenReturn(false);

        notificationsController.sendConsentOrderMadeConfirmationEmail(buildCallbackRequest());

        verify(notificationService, never()).sendConsentOrderMadeConfirmationEmailToApplicantSolicitor(any());
        verify(notificationService, never()).sendConsentOrderMadeConfirmationEmailToRespondentSolicitor(any());
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

        verify(notificationService, never()).sendConsentOrderNotApprovedEmailToApplicantSolicitor(any());
        verify(notificationService, never()).sendContestOrderNotApprovedEmailApplicant(any());
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

        verify(notificationService, never()).sendConsentOrderAvailableEmailToApplicantSolicitor(any());
        verify(notificationService, never()).sendConsentOrderAvailableEmailToRespondentSolicitor(any());
    }

    @Test
    public void sendContestedHwfSuccessfulConfirmationEmail() {
        when(caseDataService.isContestedApplication(any())).thenReturn(true);
        when(notificationService.isApplicantSolicitorDigitalAndEmailPopulated(any())).thenReturn(true);
        notificationsController.sendHwfSuccessfulConfirmationNotification(AUTH_TOKEN, buildCallbackRequest());

        verify(notificationService).sendContestedHwfSuccessfulConfirmationEmail(any());
    }

    @Test
    public void shouldNotSendContestedHwfSuccessfulEmail() {
        when(caseDataService.isConsentedApplication(any())).thenReturn(false);
        when(notificationService.isApplicantSolicitorDigitalAndEmailPopulated(any())).thenReturn(true);

        notificationsController.sendHwfSuccessfulConfirmationNotification(AUTH_TOKEN, buildCallbackRequest());

        verifyNoInteractions(notificationService);
    }

    @Test
    public void givenApplicantSolicitorIsRegisteredAndAgreedToEmails_shouldSendPrepareForHearingEmail() {
        when(notificationService.isApplicantSolicitorDigitalAndEmailPopulated(any())).thenReturn(true);

        notificationsController.sendPrepareForHearingEmail(AUTH_TOKEN, buildCallbackRequest());

        verify(notificationService).sendPrepareForHearingEmailApplicant(any());
    }

    @Test
    public void shouldNotSendPrepareForHearingEmailToApplicantSolicitorWhenNotAgreed() {
        when(caseDataService.isApplicantSolicitorAgreeToReceiveEmails(any())).thenReturn(false);
        when(checkApplicantSolicitorIsDigitalService.isSolicitorDigital(any())).thenReturn(false);

        notificationsController.sendPrepareForHearingEmail(AUTH_TOKEN, buildCallbackRequest());

        verify(notificationService, never()).sendPrepareForHearingEmailApplicant(any());
    }

    @Test
    public void shouldSendPrepareForHearingOrderSentEmailWhenAgreed() {
        when(caseDataService.isConsentedApplication(any())).thenReturn(false);
        when(caseDataService.isApplicantSolicitorAgreeToReceiveEmails(any())).thenReturn(true);

        notificationsController.sendPrepareForHearingOrderSentEmail(buildCallbackRequest());

        verify(notificationService).sendPrepareForHearingOrderSentEmailApplicant(any());
    }

    @Test
    public void shouldSendPrepareForHearingOrderSentEmailWhenRespondentIsRegisteredAndAgreedToEmails() {
        when(caseDataService.isConsentedApplication(any())).thenReturn(false);
        when(notificationService.isRespondentSolicitorRegisteredAndEmailCommunicationEnabled(any())).thenReturn(true);
        notificationsController.sendPrepareForHearingEmail(AUTH_TOKEN, buildCallbackRequest());

        verify(notificationService).sendPrepareForHearingEmailRespondent(any());
    }

    @Test
    public void shouldNotSendPrepareForHearingOrderSentEmailWhenRespondentAgreedButNotRegistered() {
        when(caseDataService.isConsentedApplication(any())).thenReturn(false);
        when(notificationService.isRespondentSolicitorEmailCommunicationEnabled(any())).thenReturn(true);
        when(checkRespondentSolicitorIsDigitalService.isSolicitorDigital(any())).thenReturn(false);
        notificationsController.sendPrepareForHearingEmail(AUTH_TOKEN, buildCallbackRequest());

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
        when(notificationService.isRespondentSolicitorEmailCommunicationEnabled(any())).thenReturn(true);

        notificationsController.sendPrepareForHearingOrderSentEmail(buildCallbackRequest());

        verify(notificationService).sendPrepareForHearingOrderSentEmailRespondent(any());
    }

    @Test
    public void sendPrepareForHearingOrderSentEmail_shouldNotSendRespondentEmail() {
        when(notificationService.isRespondentSolicitorEmailCommunicationEnabled(any())).thenReturn(false);

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
        when(notificationService.isRespondentSolicitorEmailCommunicationEnabled(any())).thenReturn(true);
        when(caseDataService.isRespondentSolicitorResponsibleToDraftOrder(any())).thenReturn(true);

        notificationsController.sendDraftOrderEmail(buildCallbackRequest());

        verify(notificationService).sendSolicitorToDraftOrderEmailRespondent(any());
    }

    @Test
    public void shouldSendSolicitorToDraftOrderEmailRespondent_shouldNotSendEmail() {
        when(notificationService.isRespondentSolicitorEmailCommunicationEnabled(any())).thenReturn(false);
        when(caseDataService.isRespondentSolicitorResponsibleToDraftOrder(any())).thenReturn(true);

        notificationsController.sendDraftOrderEmail(buildCallbackRequest());

        verify(notificationService, never()).sendSolicitorToDraftOrderEmailRespondent(any());
    }

    @Test
    public void shouldSendSolicitorToDraftOrderEmailRespondent_respSolicitorNotResponsible() {
        when(notificationService.isRespondentSolicitorEmailCommunicationEnabled(any())).thenReturn(true);
        when(caseDataService.isRespondentSolicitorResponsibleToDraftOrder(any())).thenReturn(false);

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
        when(notificationService.isRespondentSolicitorEmailCommunicationEnabled(any())).thenReturn(true);

        notificationsController.sendContestedConsentOrderApprovedEmail(buildCallbackRequest());

        verify(notificationService).sendContestedConsentOrderApprovedEmailToRespondentSolicitor(any());
    }

    @Test
    public void givenContestedCase_whenShouldSendRespondentNotification_thenShouldTriggerRespondentEmail() {
        when(notificationService.isRespondentSolicitorEmailCommunicationEnabled(any())).thenReturn(true);

        CallbackRequest callbackRequest = buildCallbackRequest();
        callbackRequest.getCaseDetails().setCaseTypeId(CaseType.CONTESTED.getCcdType());
        notificationsController.sendConsentOrderNotApprovedEmail(callbackRequest);

        verify(notificationService).sendContestOrderNotApprovedEmailRespondent(any());
        verify(notificationService, never()).sendConsentOrderNotApprovedEmailToRespondentSolicitor(any());
    }

    @Test
    public void givenConsentedCase_whenShouldSendRespondentNotification_thenShouldNotTriggerContestedRespondentEmail() {
        when(caseDataService.isConsentedApplication(any())).thenReturn(true);
        when(notificationService.isRespondentSolicitorEmailCommunicationEnabled(any())).thenReturn(true);

        notificationsController.sendConsentOrderNotApprovedEmail(buildCallbackRequest());

        verify(notificationService, never()).sendContestOrderNotApprovedEmailRespondent(any());
    }

    @Test
    public void givenConsentedCase_whenSendConsentOrderNotApproved_thenShouldTriggerConsentedRespondentEmail() {
        when(caseDataService.isConsentedApplication(any())).thenReturn(true);
        when(notificationService.isRespondentSolicitorEmailCommunicationEnabled(any())).thenReturn(true);

        CallbackRequest callbackRequest = buildCallbackRequest();
        callbackRequest.getCaseDetails().setCaseTypeId(CaseType.CONSENTED.getCcdType());
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
        when(notificationService.isRespondentSolicitorEmailCommunicationEnabled(any())).thenReturn(false);

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
        when(notificationService.isRespondentSolicitorEmailCommunicationEnabled(any())).thenReturn(true);

        notificationsController.sendGeneralOrderRaisedEmail(buildCallbackRequest());

        verify(notificationService).sendContestedGeneralOrderEmailApplicant(any());
        verify(notificationService).sendContestedGeneralOrderEmailRespondent(any());
    }

    @Test
    public void whenShouldNotSendContestedGeneralOrderEmailToRespondent_ThenTheEmailIsNotIssued() {
        when(notificationService.isRespondentSolicitorEmailCommunicationEnabled(any())).thenReturn(false);

        notificationsController.sendGeneralOrderRaisedEmail(buildCallbackRequest());

        verify(notificationService, never()).sendContestedConsentGeneralOrderEmailRespondentSolicitor(any());
        verify(notificationService, never()).sendContestedGeneralOrderEmailRespondent(any());
    }

    @Test
    public void shouldNotSendEmailToRespSolicitor() {
        when(notificationService.isRespondentSolicitorEmailCommunicationEnabled(any())).thenReturn(false);

        notificationsController.sendAssignToJudgeConfirmationNotification(AUTH_TOKEN, buildCallbackRequest());

        verify(notificationService, never()).sendAssignToJudgeConfirmationEmailToRespondentSolicitor(any());
    }

    @Test
    public void shouldSendContestedConsentGeneralOrderEmailToRespondentInConsentedInContestedCase() {
        when(caseDataService.isConsentedApplication(any())).thenReturn(false);
        when(caseDataService.isConsentedInContestedCase(any())).thenReturn(true);
        when(notificationService.isRespondentSolicitorEmailCommunicationEnabled(any())).thenReturn(true);

        notificationsController.sendGeneralOrderRaisedEmail(buildCallbackRequest());

        verify(notificationService).sendContestedConsentGeneralOrderEmailRespondentSolicitor(any());
        verify(notificationService, never()).sendContestedGeneralOrderEmailRespondent(any());
        verify(notificationService, never()).sendConsentedGeneralOrderEmailToRespondentSolicitor(any());
    }

    @Test
    public void sendConsentedGeneralOrderEmail() {
        when(caseDataService.isConsentedApplication(any())).thenReturn(true);
        when(caseDataService.isApplicantSolicitorAgreeToReceiveEmails(any())).thenReturn(true);
        when(notificationService.isRespondentSolicitorEmailCommunicationEnabled(any())).thenReturn(true);

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
        when(notificationService.isRespondentSolicitorEmailCommunicationEnabled(any())).thenReturn(true);

        notificationsController.sendContestedConsentOrderNotApprovedEmail(buildCallbackRequest());

        verify(notificationService).sendContestedConsentOrderNotApprovedEmailApplicantSolicitor(any());
        verify(notificationService).sendContestedConsentOrderNotApprovedEmailRespondentSolicitor(any());
    }

    @Test
    public void shouldNotSendContestedConsentOrderNotApprovedEmail() {
        when(notificationService.isRespondentSolicitorEmailCommunicationEnabled(any())).thenReturn(false);

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
        callbackRequest.getCaseDetails().setCaseTypeId(CaseType.CONSENTED.getCcdType());
        when(notificationService.isRespondentSolicitorEmailCommunicationEnabled(any())).thenReturn(true);
        notificationsController.sendConsentOrderAvailableEmail(callbackRequest);
        verify(notificationService).sendConsentOrderAvailableEmailToRespondentSolicitor(callbackRequest.getCaseDetails());
    }

    @Test
    public void sendConsentOrderMadeEmailToRespSolicitor() {
        CallbackRequest callbackRequest = buildCallbackRequest();
        when(notificationService.isRespondentSolicitorEmailCommunicationEnabled(any())).thenReturn(true);
        notificationsController.sendConsentOrderMadeConfirmationEmail(callbackRequest);
        verify(notificationService).sendConsentOrderMadeConfirmationEmailToRespondentSolicitor(callbackRequest.getCaseDetails());
    }

    @Test
    public void doesNotSendConsentOrderMadeEmailToRespSolicitor() {
        CallbackRequest callbackRequest = buildCallbackRequest();
        callbackRequest.getCaseDetails().setCaseTypeId(CaseType.CONSENTED.getCcdType());
        when(notificationService.isRespondentSolicitorEmailCommunicationEnabled(any())).thenReturn(false);
        notificationsController.sendConsentOrderMadeConfirmationEmail(callbackRequest);
        verify(notificationService, never()).sendConsentOrderMadeConfirmationEmailToRespondentSolicitor(callbackRequest.getCaseDetails());
    }

    @Test
    public void whenConsentOrderNotApprovedSentEmail_thenNotificationEmailsSentToSolicitors() {
        when(caseDataService.isApplicantSolicitorAgreeToReceiveEmails(any())).thenReturn(true);
        when(notificationService.isRespondentSolicitorEmailCommunicationEnabled(any())).thenReturn(true);

        notificationsController.sendConsentOrderNotApprovedSentEmail(buildCallbackRequest());

        verify(notificationService).sendConsentOrderNotApprovedSentEmailToApplicantSolicitor(any());
        verify(notificationService).sendConsentOrderNotApprovedSentEmailToRespondentSolicitor(any());
    }

    @Test
    public void whenConsentOrderApprovedAndSolicitorEmailsNotEnabled_thenDoNotEmailSolicitors() {
        when(caseDataService.isApplicantSolicitorAgreeToReceiveEmails(any())).thenReturn(false);
        when(notificationService.isRespondentSolicitorEmailCommunicationEnabled(any())).thenReturn(false);

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
        when(notificationService.isRespondentSolicitorEmailCommunicationEnabled(any())).thenReturn(true);

        notificationsController.sendInterimHearingNotification(buildCallbackInterimRequest());

        verify(notificationService, times(1)).sendInterimNotificationEmailToApplicantSolicitor(any());
        verify(notificationService, times(1)).sendInterimNotificationEmailToRespondentSolicitor(any());
    }

    @Test
    public void shouldNotSendInterimHearingWhenNotAgreed() {
        when(caseDataService.isPaperApplication(any())).thenReturn(false);
        when(caseDataService.isContestedApplication(any())).thenReturn(true);
        when(caseDataService.isApplicantSolicitorAgreeToReceiveEmails(any())).thenReturn(false);
        when(notificationService.isRespondentSolicitorEmailCommunicationEnabled(any())).thenReturn(false);

        notificationsController.sendInterimHearingNotification(buildCallbackInterimRequest());

        verify(notificationService, never()).sendInterimNotificationEmailToApplicantSolicitor(any());
        verify(notificationService, never()).sendInterimNotificationEmailToRespondentSolicitor(any());
    }

    @Test
    public void shouldSendInterimHearingNotificationWhenApplicantAgreedButRespondentNotAgreed() {
        when(caseDataService.isPaperApplication(any())).thenReturn(false);
        when(caseDataService.isContestedApplication(any())).thenReturn(true);
        when(caseDataService.isApplicantSolicitorAgreeToReceiveEmails(any())).thenReturn(true);
        when(notificationService.isRespondentSolicitorEmailCommunicationEnabled(any())).thenReturn(false);

        notificationsController.sendInterimHearingNotification(buildCallbackInterimRequest());

        verify(notificationService, times(1)).sendInterimNotificationEmailToApplicantSolicitor(any());
        verify(notificationService, never()).sendInterimNotificationEmailToRespondentSolicitor(any());
    }

    @Test
    public void shouldSendInterimHearingNotificationWhenApplicantNotAgreedButRespondentAgreed() {
        when(caseDataService.isPaperApplication(any())).thenReturn(false);
        when(caseDataService.isContestedApplication(any())).thenReturn(true);
        when(caseDataService.isApplicantSolicitorAgreeToReceiveEmails(any())).thenReturn(false);
        when(notificationService.isRespondentSolicitorEmailCommunicationEnabled(any())).thenReturn(true);

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
    public void givenNoticeOfChangeRejected_whenSendNoticeOfChangeNotifications_thenSendNoticeOfChangeServiceNotCalled() {
        CallbackRequest callbackRequest = buildCallbackRequestWithBeforeCaseDetails();
        callbackRequest.getCaseDetails().getData().put(IS_NOC_REJECTED, YES_VALUE);
        notificationsController.sendNoticeOfChangeNotifications("authToken", callbackRequest);

        verify(notificationService, never()).sendNoticeOfChangeEmail(any());

        verify(nocLetterNotificationService, never()).sendNoticeOfChangeLetters(any(CaseDetails.class), any(CaseDetails.class), anyString());
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

        when(notificationService.isApplicantSolicitorDigitalAndEmailPopulated(any())).thenReturn(true);
        when(notificationService.isRespondentSolicitorDigitalAndEmailPopulated(any())).thenReturn(true);

        notificationsController.sendUpdateFrcNotifications(AUTH_TOKEN, buildCallbackRequest());
        verify(notificationService, times(1)).sendUpdateFrcInformationEmailToAppSolicitor(any());
        verify(notificationService, times(1)).sendUpdateFrcInformationEmailToRespondentSolicitor(any());
        verify(notificationService, times(1)).sendUpdateFrcInformationEmailToCourt(any());
    }

    @Test
    public void givenUpdateFrc_whenAppSolNotAgreeToReceiveEmails_thenNotificationServiceCalledTwice() throws JsonProcessingException {
        when(notificationService.isApplicantSolicitorDigitalAndEmailPopulated(any())).thenReturn(false);
        when(notificationService.isRespondentSolicitorDigitalAndEmailPopulated(any())).thenReturn(true);

        notificationsController.sendUpdateFrcNotifications(AUTH_TOKEN, buildCallbackRequest());
        verify(notificationService, never()).sendUpdateFrcInformationEmailToAppSolicitor(any());
        verify(notificationService, times(1)).sendUpdateFrcInformationEmailToRespondentSolicitor(any());
        verify(notificationService, times(1)).sendUpdateFrcInformationEmailToCourt(any());

    }

    @Test
    public void givenUpdateFrc_whenRespSolNotAgreeToReceiveEmails_thenNotificationServiceCalledTwice() throws JsonProcessingException {
        when(notificationService.isApplicantSolicitorDigitalAndEmailPopulated(any())).thenReturn(true);
        when(notificationService.isRespondentSolicitorDigitalAndEmailPopulated(any())).thenReturn(false);

        notificationsController.sendUpdateFrcNotifications(AUTH_TOKEN, buildCallbackRequest());
        verify(notificationService, times(1)).sendUpdateFrcInformationEmailToAppSolicitor(any());
        verify(notificationService, never()).sendUpdateFrcInformationEmailToRespondentSolicitor(any());
        verify(notificationService, times(1)).sendUpdateFrcInformationEmailToCourt(any());
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
