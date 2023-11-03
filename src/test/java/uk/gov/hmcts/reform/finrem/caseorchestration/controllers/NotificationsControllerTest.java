package uk.gov.hmcts.reform.finrem.caseorchestration.controllers;

import com.fasterxml.jackson.core.JsonProcessingException;
import org.junit.Before;
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
import uk.gov.hmcts.reform.finrem.caseorchestration.mapper.FinremCaseDetailsMapper;
import uk.gov.hmcts.reform.finrem.caseorchestration.mapper.letterdetails.address.ApplicantLetterAddresseeGenerator;
import uk.gov.hmcts.reform.finrem.caseorchestration.mapper.letterdetails.address.IntervenerFourLetterAddresseeGenerator;
import uk.gov.hmcts.reform.finrem.caseorchestration.mapper.letterdetails.address.IntervenerOneLetterAddresseeGenerator;
import uk.gov.hmcts.reform.finrem.caseorchestration.mapper.letterdetails.address.IntervenerThreeLetterAddresseeGenerator;
import uk.gov.hmcts.reform.finrem.caseorchestration.mapper.letterdetails.address.IntervenerTwoLetterAddresseeGenerator;
import uk.gov.hmcts.reform.finrem.caseorchestration.mapper.letterdetails.address.LetterAddresseeGeneratorMapper;
import uk.gov.hmcts.reform.finrem.caseorchestration.mapper.letterdetails.address.RespondentLetterAddresseeGenerator;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.CaseDocument;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.CaseType;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.FinremCaseDataContested;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.FinremCaseDetails;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.HearingOrderCollectionData;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.HearingOrderDocument;
import uk.gov.hmcts.reform.finrem.caseorchestration.service.AssignedToJudgeDocumentService;
import uk.gov.hmcts.reform.finrem.caseorchestration.service.BulkPrintService;
import uk.gov.hmcts.reform.finrem.caseorchestration.service.CaseDataService;
import uk.gov.hmcts.reform.finrem.caseorchestration.service.FeatureToggleService;
import uk.gov.hmcts.reform.finrem.caseorchestration.service.GeneralEmailService;
import uk.gov.hmcts.reform.finrem.caseorchestration.service.GenericDocumentService;
import uk.gov.hmcts.reform.finrem.caseorchestration.service.HelpWithFeesDocumentService;
import uk.gov.hmcts.reform.finrem.caseorchestration.service.NotificationService;
import uk.gov.hmcts.reform.finrem.caseorchestration.service.PaperNotificationService;
import uk.gov.hmcts.reform.finrem.caseorchestration.service.TransferCourtService;
import uk.gov.hmcts.reform.finrem.caseorchestration.service.correspondence.assigntojudge.AssignToJudgeCorresponder;
import uk.gov.hmcts.reform.finrem.caseorchestration.service.correspondence.consentorder.ConsentOrderAvailableCorresponder;
import uk.gov.hmcts.reform.finrem.caseorchestration.service.correspondence.consentorder.ConsentOrderNotApprovedCorresponder;
import uk.gov.hmcts.reform.finrem.caseorchestration.service.correspondence.consentorder.ConsentOrderNotApprovedSentCorresponder;
import uk.gov.hmcts.reform.finrem.caseorchestration.service.correspondence.consentorder.ContestedConsentOrderApprovedCorresponder;
import uk.gov.hmcts.reform.finrem.caseorchestration.service.correspondence.consentorder.ContestedConsentOrderNotApprovedCorresponder;
import uk.gov.hmcts.reform.finrem.caseorchestration.service.correspondence.consentorder.ContestedDraftOrderCorresponder;
import uk.gov.hmcts.reform.finrem.caseorchestration.service.correspondence.consentorder.ContestedIntermHearingCorresponder;
import uk.gov.hmcts.reform.finrem.caseorchestration.service.correspondence.generalorder.GeneralOrderRaisedCorresponder;
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
@ContextConfiguration(classes = {
    AssignedToJudgeDocumentService.class,
    AssignToJudgeCorresponder.class,
    HwfCorrespondenceService.class,
    HwfConsentedApplicantCorresponder.class,
    HwfContestedApplicantCorresponder.class,
    UpdateFrcCorrespondenceService.class,
    UpdateFrcLetterOrEmailAllSolicitorsCorresponder.class,
    UpdateFrcInfoRespondentDocumentService.class,
    UpdateFrcInfoLetterDetailsGenerator.class,
    ConsentOrderNotApprovedCorresponder.class,
    ContestedConsentOrderApprovedCorresponder.class,
    ContestedConsentOrderNotApprovedCorresponder.class,
    ConsentOrderAvailableCorresponder.class,
    ConsentOrderNotApprovedSentCorresponder.class,
    ContestedIntermHearingCorresponder.class,
    ContestedDraftOrderCorresponder.class,
    FinremCaseDetailsMapper.class,
    DocumentHelper.class,
    LetterAddresseeGeneratorMapper.class,
    ApplicantLetterAddresseeGenerator.class,
    RespondentLetterAddresseeGenerator.class,
    IntervenerOneLetterAddresseeGenerator.class,
    IntervenerTwoLetterAddresseeGenerator.class,
    IntervenerThreeLetterAddresseeGenerator.class,
    IntervenerFourLetterAddresseeGenerator.class
})
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
    @MockBean
    private DocumentHelper documentHelper;
    @MockBean
    private GeneralOrderRaisedCorresponder generalOrderRaisedCorresponder;
    @MockBean
    private ContestedIntermHearingCorresponder contestedIntermHearingCorresponder;
    @MockBean
    private ContestedDraftOrderCorresponder contestedDraftOrderCorresponder;
    @MockBean
    private FinremCaseDetailsMapper finremCaseDetailsMapper;

    @Override
    @Before
    public void setUp() {
        when(finremCaseDetailsMapper.mapToFinremCaseDetails(any(CaseDetails.class)))
            .thenReturn(getFinremCaseDetailsFromCaseDetails());
    }

    @Test
    public void sendHwfSuccessfulConfirmationEmailIfDigitalCase() {
        when(caseDataService.isConsentedApplication(any(CaseDetails.class))).thenReturn(true);
        when(notificationService.isApplicantSolicitorDigitalAndEmailPopulated(any(CaseDetails.class))).thenReturn(true);

        notificationsController.sendHwfSuccessfulConfirmationNotification(AUTH_TOKEN, buildCallbackRequest());

        verify(notificationService).sendConsentedHWFSuccessfulConfirmationEmail(any(CaseDetails.class));
        verifyNoInteractions(helpWithFeesDocumentService);
    }

    @Test
    public void shouldNotSendHwfSuccessfulConfirmationEmail() {
        when(caseDataService.isConsentedApplication(any(CaseDetails.class))).thenReturn(true);
        when(notificationService.isApplicantSolicitorDigitalAndEmailPopulated(any(CaseDetails.class))).thenReturn(false);

        notificationsController.sendHwfSuccessfulConfirmationNotification(AUTH_TOKEN, buildCallbackRequest());

        verify(notificationService).isApplicantSolicitorDigitalAndEmailPopulated(any(CaseDetails.class));
        verifyNoMoreInteractions(notificationService);
    }

    @Test
    public void sendHwfSuccessfulNotificationLetterIfIsConsentedAndIsPaperApplication() {
        when(caseDataService.isConsentedApplication(any(CaseDetails.class))).thenReturn(true);
        when(notificationService.isApplicantSolicitorDigitalAndEmailPopulated(any(CaseDetails.class))).thenReturn(false);

        notificationsController.sendHwfSuccessfulConfirmationNotification(AUTH_TOKEN, buildCallbackRequest());

        verify(notificationService).isApplicantSolicitorDigitalAndEmailPopulated(any(CaseDetails.class));
        verifyNoMoreInteractions(notificationService);
        verify(bulkPrintService).sendDocumentForPrint(any(), any(CaseDetails.class), anyString(), any());
    }

    @Test
    public void sendAssignToJudgeConfirmationEmailIfDigitalCase() {
        when(notificationService.isApplicantSolicitorDigitalAndEmailPopulated(any(CaseDetails.class))).thenReturn(true);

        notificationsController.sendAssignToJudgeConfirmationNotification(AUTH_TOKEN, buildCallbackRequest());

        verify(notificationService).sendAssignToJudgeConfirmationEmailToApplicantSolicitor(any(CaseDetails.class));
    }


    @Test
    public void shouldSendAssignToJudgeConfirmationEmailIfRespondentSolicitorIsAcceptingEmail() {
        when(notificationService.isRespondentSolicitorDigitalAndEmailPopulated(any(CaseDetails.class))).thenReturn(true);

        notificationsController.sendAssignToJudgeConfirmationNotification(AUTH_TOKEN, buildCallbackRequest());

        verify(notificationService).sendAssignToJudgeConfirmationEmailToRespondentSolicitor(any(CaseDetails.class));
    }

    @Test
    public void shouldNotSendAssignToJudgeConfirmationEmailIfRespondentSolicitorIsAcceptingEmail() {
        when(notificationService.isRespondentSolicitorEmailCommunicationEnabled(any())).thenReturn(false);

        verify(notificationService, never()).sendAssignToJudgeConfirmationEmailToRespondentSolicitor(any(CaseDetails.class));
    }

    @Test
    public void sendConsentOrderNotApprovedEmail() {
        when(caseDataService.isConsentedApplication(any(CaseDetails.class))).thenReturn(true);
        when(notificationService.isApplicantSolicitorDigitalAndEmailPopulated(any(CaseDetails.class))).thenReturn(true);

        notificationsController.sendConsentOrderNotApprovedEmail(buildCallbackRequest());

        verify(notificationService).sendConsentOrderNotApprovedEmailToApplicantSolicitor(any(CaseDetails.class));
    }

    @Test
    public void shouldNotSendConsentOrderNotApprovedEmail() {
        when(caseDataService.isConsentedApplication(any(CaseDetails.class))).thenReturn(true);
        when(caseDataService.isApplicantSolicitorAgreeToReceiveEmails(any())).thenReturn(false);

        notificationsController.sendConsentOrderNotApprovedEmail(buildCallbackRequest());

        verify(notificationService, never()).sendConsentOrderNotApprovedEmailToApplicantSolicitor(any(CaseDetails.class));
        verify(notificationService, never()).sendContestOrderNotApprovedEmailApplicant(any(CaseDetails.class));
    }

    @Test
    public void sendConsentOrderAvailableEmail() {
        when(caseDataService.isConsentedApplication(any(CaseDetails.class))).thenReturn(true);
        when(notificationService.isApplicantSolicitorDigitalAndEmailPopulated(any(CaseDetails.class))).thenReturn(true);

        notificationsController.sendConsentOrderAvailableEmail(buildCallbackRequest());

        verify(notificationService).sendConsentOrderAvailableEmailToApplicantSolicitor(any(CaseDetails.class));
    }

    @Test
    public void shouldNotSendConsentOrderAvailableEmail() {
        when(caseDataService.isConsentedApplication(any(CaseDetails.class))).thenReturn(true);
        when(notificationService.isApplicantSolicitorDigitalAndEmailPopulated(any(CaseDetails.class))).thenReturn(false);
        when(notificationService.isRespondentSolicitorDigitalAndEmailPopulated(any(CaseDetails.class))).thenReturn(false);

        notificationsController.sendConsentOrderAvailableEmail(buildCallbackRequest());

        verify(notificationService, never()).sendConsentOrderAvailableEmailToApplicantSolicitor(any(CaseDetails.class));
        verify(notificationService, never()).sendConsentOrderAvailableEmailToRespondentSolicitor(any(CaseDetails.class));
    }

    @Test
    public void sendContestedHwfSuccessfulConfirmationEmail() {
        when(caseDataService.isContestedApplication(any(CaseDetails.class))).thenReturn(true);
        when(notificationService.isApplicantSolicitorDigitalAndEmailPopulated(any(CaseDetails.class))).thenReturn(true);
        notificationsController.sendHwfSuccessfulConfirmationNotification(AUTH_TOKEN, buildCallbackRequest());

        verify(notificationService).sendContestedHwfSuccessfulConfirmationEmail(any(CaseDetails.class));
    }

    @Test
    public void shouldNotSendContestedHwfSuccessfulEmail() {
        when(caseDataService.isConsentedApplication(any(CaseDetails.class))).thenReturn(false);
        when(notificationService.isApplicantSolicitorDigitalAndEmailPopulated(any(CaseDetails.class))).thenReturn(true);

        notificationsController.sendHwfSuccessfulConfirmationNotification(AUTH_TOKEN, buildCallbackRequest());

        verifyNoInteractions(notificationService);
    }

    @Test
    public void givenApplicantSolicitorIsRegisteredAndAgreedToEmails_shouldSendPrepareForHearingEmail() {
        when(notificationService.isApplicantSolicitorDigitalAndEmailPopulated(any(CaseDetails.class))).thenReturn(true);

        notificationsController.sendPrepareForHearingEmail(AUTH_TOKEN, buildCallbackRequest());

        verify(notificationService).sendPrepareForHearingEmailApplicant(any(CaseDetails.class));
    }

    @Test
    public void shouldNotSendPrepareForHearingEmailToApplicantSolicitorWhenNotAgreed() {
        when(caseDataService.isApplicantSolicitorAgreeToReceiveEmails(any())).thenReturn(false);
        when(checkApplicantSolicitorIsDigitalService.isSolicitorDigital(any())).thenReturn(false);

        notificationsController.sendPrepareForHearingEmail(AUTH_TOKEN, buildCallbackRequest());

        verify(notificationService, never()).sendPrepareForHearingEmailApplicant(any(CaseDetails.class));
    }

    @Test
    public void shouldSendPrepareForHearingOrderSentEmailWhenRespondentIsRegisteredAndAgreedToEmails() {
        when(caseDataService.isConsentedApplication(any(CaseDetails.class))).thenReturn(false);
        when(notificationService.isRespondentSolicitorRegisteredAndEmailCommunicationEnabled(any())).thenReturn(true);
        notificationsController.sendPrepareForHearingEmail(AUTH_TOKEN, buildCallbackRequest());

        verify(notificationService).sendPrepareForHearingEmailRespondent(any(CaseDetails.class));
    }

    @Test
    public void shouldNotSendPrepareForHearingOrderSentEmailWhenRespondentAgreedButNotRegistered() {
        when(caseDataService.isConsentedApplication(any(CaseDetails.class))).thenReturn(false);
        when(notificationService.isRespondentSolicitorEmailCommunicationEnabled(any())).thenReturn(true);
        when(checkRespondentSolicitorIsDigitalService.isSolicitorDigital(any())).thenReturn(false);
        notificationsController.sendPrepareForHearingEmail(AUTH_TOKEN, buildCallbackRequest());

        verify(notificationService, never()).sendPrepareForHearingEmailApplicant(any(CaseDetails.class));
        verify(notificationService, never()).sendPrepareForHearingEmailRespondent(any(CaseDetails.class));
    }

    @Test
    public void sendDraftOrderEmailWhenApplicantSolicitorIsNominatedAndIsAcceptingEmails() {
        when(caseDataService.isConsentedApplication(any(CaseDetails.class))).thenReturn(true);
        when(caseDataService.isApplicantSolicitorAgreeToReceiveEmails(any())).thenReturn(true);
        when(caseDataService.isApplicantSolicitorResponsibleToDraftOrder(any())).thenReturn(true);

        notificationsController.sendDraftOrderEmail(createCallbackRequestWithFinalOrder());

        verify(contestedDraftOrderCorresponder).sendCorrespondence(any(CaseDetails.class));
    }

    @Test
    public void shouldSendSolicitorToDraftOrderEmailRespondent() {
        when(notificationService.isRespondentSolicitorEmailCommunicationEnabled(any())).thenReturn(true);
        when(caseDataService.isRespondentSolicitorResponsibleToDraftOrder(any())).thenReturn(true);

        notificationsController.sendDraftOrderEmail(buildCallbackRequest());

        verify(contestedDraftOrderCorresponder).sendCorrespondence(any(CaseDetails.class));
    }

    @Test
    public void sendContestOrderNotApprovedEmail() {
        when(caseDataService.isConsentedApplication(any(CaseDetails.class))).thenReturn(false);
        when(notificationService.isApplicantSolicitorDigitalAndEmailPopulated(any(CaseDetails.class))).thenReturn(true);

        notificationsController.sendConsentOrderNotApprovedEmail(createCallbackRequestWithFinalOrder());

        verify(notificationService).sendContestOrderNotApprovedEmailApplicant(any(CaseDetails.class));
    }

    @Test
    public void shouldNotSendContestOrderNotApprovedEmail() {
        when(caseDataService.isConsentedApplication(any(CaseDetails.class))).thenReturn(false);
        when(notificationService.isApplicantSolicitorDigitalAndEmailPopulated(any(CaseDetails.class))).thenReturn(false);

        notificationsController.sendConsentOrderNotApprovedEmail(createCallbackRequestWithFinalOrder());

        verify(notificationService, never()).sendConsentOrderNotApprovedEmailToApplicantSolicitor(any(CaseDetails.class));
        verify(notificationService, never()).sendContestOrderNotApprovedEmailApplicant(any(CaseDetails.class));
    }

    @Test
    public void sendContestedConsentOrderApprovedEmailToApplicantSolicitor() {
        when(notificationService.isApplicantSolicitorDigitalAndEmailPopulated(any(CaseDetails.class))).thenReturn(true);

        notificationsController.sendContestedConsentOrderApprovedEmail(buildCallbackRequest());

        verify(notificationService).sendContestedConsentOrderApprovedEmailToApplicantSolicitor(any(CaseDetails.class));
    }

    @Test
    public void sendContestedConsentOrderApprovedEmailToRespondentSolicitor() {
        when(notificationService.isRespondentSolicitorDigitalAndEmailPopulated(any(CaseDetails.class))).thenReturn(true);

        notificationsController.sendContestedConsentOrderApprovedEmail(buildCallbackRequest());

        verify(notificationService).sendContestedConsentOrderApprovedEmailToRespondentSolicitor(any(CaseDetails.class));
    }

    @Test
    public void givenContestedCase_whenShouldSendRespondentNotification_thenShouldTriggerRespondentEmail() {
        when(notificationService.isRespondentSolicitorDigitalAndEmailPopulated(any(CaseDetails.class))).thenReturn(true);

        CallbackRequest callbackRequest = buildCallbackRequest();
        callbackRequest.getCaseDetails().setCaseTypeId(CaseType.CONTESTED.getCcdType());
        notificationsController.sendConsentOrderNotApprovedEmail(callbackRequest);

        verify(notificationService).sendContestOrderNotApprovedEmailRespondent(any(CaseDetails.class));
        verify(notificationService, never()).sendConsentOrderNotApprovedEmailToRespondentSolicitor(any(CaseDetails.class));
    }

    @Test
    public void givenConsentedCase_whenShouldSendRespondentNotification_thenShouldNotTriggerContestedRespondentEmail() {
        when(caseDataService.isConsentedApplication(any(CaseDetails.class))).thenReturn(true);
        when(notificationService.isRespondentSolicitorEmailCommunicationEnabled(any())).thenReturn(true);

        notificationsController.sendConsentOrderNotApprovedEmail(buildCallbackRequest());

        verify(notificationService, never()).sendContestOrderNotApprovedEmailRespondent(any(CaseDetails.class));
    }

    @Test
    public void givenConsentedCase_whenSendConsentOrderNotApproved_thenShouldTriggerConsentedRespondentEmail() {
        when(caseDataService.isConsentedApplication(any(CaseDetails.class))).thenReturn(true);
        when(notificationService.isRespondentSolicitorDigitalAndEmailPopulated(any(CaseDetails.class))).thenReturn(true);

        CallbackRequest callbackRequest = buildCallbackRequest();
        callbackRequest.getCaseDetails().setCaseTypeId(CaseType.CONSENTED.getCcdType());
        notificationsController.sendConsentOrderNotApprovedEmail(callbackRequest);

        verify(notificationService).sendConsentOrderNotApprovedEmailToRespondentSolicitor(any(CaseDetails.class));
    }

    @Test
    public void sendContestedConsentOrderApprovedEmail() {
        when(notificationService.isApplicantSolicitorDigitalAndEmailPopulated(any(CaseDetails.class))).thenReturn(true);

        notificationsController.sendContestedConsentOrderApprovedEmail(buildCallbackRequest());

        verify(notificationService).sendContestedConsentOrderApprovedEmailToApplicantSolicitor(any(CaseDetails.class));
    }

    @Test
    public void shouldNotSendContestedConsentOrderApprovedEmailToRespondentSolicitorWhenRespSolShouldNotReceiveEmail() {
        when(notificationService.isRespondentSolicitorRegisteredAndEmailCommunicationEnabled(any())).thenReturn(false);

        notificationsController.sendContestedConsentOrderApprovedEmail(buildCallbackRequest());

        verify(notificationService,
            never()).sendContestedConsentOrderApprovedEmailToRespondentSolicitor(any(CaseDetails.class));
    }

    @Test
    public void shouldNotSendContestedConsentOrderApprovedEmailToApplicantSolicitorWhenRespSolShouldNotReceiveEmail() {
        when(notificationService.isApplicantSolicitorDigitalAndEmailPopulated(any(CaseDetails.class))).thenReturn(false);

        notificationsController.sendContestedConsentOrderApprovedEmail(buildCallbackRequest());

        verify(notificationService,
            never()).sendContestedConsentOrderApprovedEmailToRespondentSolicitor(any(CaseDetails.class));
    }


    @Test
    public void shouldSendGeneralOrderCorrespondence() {

        CallbackRequest callbackRequest = buildCallbackRequest();
        notificationsController.sendGeneralOrderRaisedEmail(callbackRequest);
        verify(generalOrderRaisedCorresponder).sendCorrespondence(callbackRequest.getCaseDetails());

    }


    @Test
    public void shouldNotSendEmailToRespSolicitor() {
        when(notificationService.isRespondentSolicitorEmailCommunicationEnabled(any())).thenReturn(false);

        notificationsController.sendAssignToJudgeConfirmationNotification(AUTH_TOKEN, buildCallbackRequest());

        verify(notificationService, never()).sendAssignToJudgeConfirmationEmailToRespondentSolicitor(any(CaseDetails.class));
    }


    @Test
    public void sendContestedGeneralApplicationReferToJudgeEmail() {
        when(caseDataService.isConsentedApplication(any(CaseDetails.class))).thenReturn(false);

        notificationsController.sendGeneralApplicationReferToJudgeEmail(buildCallbackRequest());

        verify(notificationService).sendContestedGeneralApplicationReferToJudgeEmail(any(CaseDetails.class));
    }

    @Test
    public void sendContestedConsentOrderNotApprovedEmail() {
        when(notificationService.isApplicantSolicitorDigitalAndEmailPopulated(any(CaseDetails.class))).thenReturn(true);
        when(notificationService.isRespondentSolicitorDigitalAndEmailPopulated(any(CaseDetails.class))).thenReturn(true);

        notificationsController.sendContestedConsentOrderNotApprovedEmail(buildCallbackRequest());

        verify(notificationService).sendContestedConsentOrderNotApprovedEmailApplicantSolicitor(any(CaseDetails.class));
        verify(notificationService).sendContestedConsentOrderNotApprovedEmailRespondentSolicitor(any(CaseDetails.class));
    }

    @Test
    public void shouldNotSendContestedConsentOrderNotApprovedEmail() {
        when(notificationService.isRespondentSolicitorDigitalAndEmailPopulated(any(CaseDetails.class))).thenReturn(false);

        notificationsController.sendContestedConsentOrderNotApprovedEmail(buildCallbackRequest());

        verify(notificationService, never()).sendContestedConsentOrderNotApprovedEmailApplicantSolicitor(any(CaseDetails.class));
        verify(notificationService, never()).sendContestedConsentOrderNotApprovedEmailRespondentSolicitor(any(CaseDetails.class));
    }

    @Test
    public void sendContestedGeneralApplicationOutcomeEmail() throws IOException {
        when(caseDataService.isConsentedApplication(any(CaseDetails.class))).thenReturn(false);
        when(caseDataService.isApplicantSolicitorAgreeToReceiveEmails(any())).thenReturn(true);

        notificationsController.sendGeneralApplicationOutcomeEmail(buildCallbackRequest());

        verify(notificationService).sendContestedGeneralApplicationOutcomeEmail(any(CaseDetails.class));
    }

    @Test
    public void givenConsentedCase_whenToggleEnabledAndShouldSendEmailToRespSolicitor_thenSendsEmail() {
        CallbackRequest callbackRequest = buildCallbackRequest();
        callbackRequest.getCaseDetails().setCaseTypeId(CaseType.CONSENTED.getCcdType());
        when(notificationService.isRespondentSolicitorDigitalAndEmailPopulated(any(CaseDetails.class))).thenReturn(true);
        notificationsController.sendConsentOrderAvailableEmail(callbackRequest);
        verify(notificationService).sendConsentOrderAvailableEmailToRespondentSolicitor(callbackRequest.getCaseDetails());
    }

    @Test
    public void whenConsentOrderNotApprovedSentEmail_thenNotificationEmailsSentToSolicitors() {
        when(notificationService.isApplicantSolicitorDigitalAndEmailPopulated(any(CaseDetails.class))).thenReturn(true);
        when(notificationService.isRespondentSolicitorDigitalAndEmailPopulated(any(CaseDetails.class))).thenReturn(true);

        notificationsController.sendConsentOrderNotApprovedSentEmail(buildCallbackRequest());

        verify(notificationService).sendConsentOrderNotApprovedSentEmailToApplicantSolicitor(any(CaseDetails.class));
        verify(notificationService).sendConsentOrderNotApprovedSentEmailToRespondentSolicitor(any(CaseDetails.class));
    }

    @Test
    public void whenConsentOrderApprovedAndSolicitorEmailsNotEnabled_thenDoNotEmailSolicitors() {
        when(notificationService.isApplicantSolicitorDigitalAndEmailPopulated(any(CaseDetails.class))).thenReturn(false);
        when(notificationService.isRespondentSolicitorDigitalAndEmailPopulated(any(CaseDetails.class))).thenReturn(false);

        notificationsController.sendConsentOrderNotApprovedSentEmail(buildCallbackRequest());

        verify(notificationService, never()).sendConsentOrderNotApprovedSentEmailToApplicantSolicitor(any(CaseDetails.class));
        verify(notificationService, never()).sendConsentOrderNotApprovedSentEmailToRespondentSolicitor(any(CaseDetails.class));
    }

    @Test
    public void sendTransferToLocalCourtEmailConsented() {
        when(caseDataService.isConsentedApplication(any(CaseDetails.class))).thenReturn(true);

        notificationsController.sendTransferCourtsEmail(buildCallbackRequest());

        verify(notificationService).sendTransferToLocalCourtEmail(any(CaseDetails.class));
        verify(transferCourtService).storeTransferToCourtEmail(any(CaseDetails.class));
    }

    @Test
    public void givenNoticeOfChangeWhenSendNoticeOfChangeNotificationsThenSendNoticeOfChangeServiceCalled() {
        notificationsController.sendNoticeOfChangeNotifications("authToken", buildCallbackRequestWithBeforeCaseDetails());

        verify(notificationService, times(1)).sendNoticeOfChangeEmail(any(CaseDetails.class));

        verify(nocLetterNotificationService, times(1)).sendNoticeOfChangeLetters(any(CaseDetails.class), any(CaseDetails.class), anyString());
    }

    @Test
    public void givenNoticeOfChangeRejected_whenSendNoticeOfChangeNotifications_thenSendNoticeOfChangeServiceNotCalled() {
        CallbackRequest callbackRequest = buildCallbackRequestWithBeforeCaseDetails();
        callbackRequest.getCaseDetails().getData().put(IS_NOC_REJECTED, YES_VALUE);
        notificationsController.sendNoticeOfChangeNotifications("authToken", callbackRequest);

        verify(notificationService, never()).sendNoticeOfChangeEmail(any(CaseDetails.class));

        verify(nocLetterNotificationService, never()).sendNoticeOfChangeLetters(any(CaseDetails.class), any(CaseDetails.class), anyString());
    }

    @Test
    public void givenNoticeOfChangeAsCaseworker_whenSendNoCNotifications_ThenSendNoticeOfChangeServiceCalled() {
        when(featureToggleService.isCaseworkerNoCEnabled()).thenReturn(true);
        notificationsController.sendNoticeOfChangeNotificationsCaseworker("authtoken",
            buildNoCCaseworkerCallbackRequest());

        verify(notificationService, times(1)).sendNoticeOfChangeEmailCaseworker(any(CaseDetails.class));

        verify(nocLetterNotificationService, times(1))
            .sendNoticeOfChangeLetters(any(CaseDetails.class), any(CaseDetails.class), anyString());
    }

    @Test
    public void givenUpdateFrc_whenSendEmail_thenNotificationServiceCalledThreeTimes() throws JsonProcessingException {

        when(notificationService.isApplicantSolicitorDigitalAndEmailPopulated(any(CaseDetails.class))).thenReturn(true);
        when(notificationService.isRespondentSolicitorDigitalAndEmailPopulated(any(CaseDetails.class))).thenReturn(true);

        notificationsController.sendUpdateFrcNotifications(AUTH_TOKEN, buildCallbackRequest());
        verify(notificationService, times(1)).sendUpdateFrcInformationEmailToAppSolicitor(any(CaseDetails.class));
        verify(notificationService, times(1)).sendUpdateFrcInformationEmailToRespondentSolicitor(any(CaseDetails.class));
        verify(notificationService, times(1)).sendUpdateFrcInformationEmailToCourt(any(CaseDetails.class));
    }

    @Test
    public void givenUpdateFrc_whenAppSolNotAgreeToReceiveEmails_thenNotificationServiceCalledTwice() throws JsonProcessingException {
        when(notificationService.isApplicantSolicitorDigitalAndEmailPopulated(any(CaseDetails.class))).thenReturn(false);
        when(notificationService.isRespondentSolicitorDigitalAndEmailPopulated(any(CaseDetails.class))).thenReturn(true);

        notificationsController.sendUpdateFrcNotifications(AUTH_TOKEN, buildCallbackRequest());
        verify(notificationService, never()).sendUpdateFrcInformationEmailToAppSolicitor(any(CaseDetails.class));
        verify(notificationService, times(1)).sendUpdateFrcInformationEmailToRespondentSolicitor(any(CaseDetails.class));
        verify(notificationService, times(1)).sendUpdateFrcInformationEmailToCourt(any(CaseDetails.class));

    }

    @Test
    public void givenUpdateFrc_whenRespSolNotAgreeToReceiveEmails_thenNotificationServiceCalledTwice() throws JsonProcessingException {
        when(notificationService.isApplicantSolicitorDigitalAndEmailPopulated(any(CaseDetails.class))).thenReturn(true);
        when(notificationService.isRespondentSolicitorDigitalAndEmailPopulated(any(CaseDetails.class))).thenReturn(false);

        notificationsController.sendUpdateFrcNotifications(AUTH_TOKEN, buildCallbackRequest());
        verify(notificationService, times(1)).sendUpdateFrcInformationEmailToAppSolicitor(any(CaseDetails.class));
        verify(notificationService, never()).sendUpdateFrcInformationEmailToRespondentSolicitor(any(CaseDetails.class));
        verify(notificationService, times(1)).sendUpdateFrcInformationEmailToCourt(any(CaseDetails.class));
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

    private FinremCaseDetails getFinremCaseDetailsFromCaseDetails() {
        return FinremCaseDetails.builder().data(FinremCaseDataContested.builder().build()).build();
    }
}
