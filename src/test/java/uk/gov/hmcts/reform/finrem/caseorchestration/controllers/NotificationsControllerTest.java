package uk.gov.hmcts.reform.finrem.caseorchestration.controllers;

import com.fasterxml.jackson.core.JsonProcessingException;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
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
import uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.FinremCaseData;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.FinremCaseDetails;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.HearingOrderCollectionData;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.HearingOrderDocument;
import uk.gov.hmcts.reform.finrem.caseorchestration.service.AssignedToJudgeDocumentService;
import uk.gov.hmcts.reform.finrem.caseorchestration.service.BulkPrintService;
import uk.gov.hmcts.reform.finrem.caseorchestration.service.CaseDataService;
import uk.gov.hmcts.reform.finrem.caseorchestration.service.FeatureToggleService;
import uk.gov.hmcts.reform.finrem.caseorchestration.service.GenericDocumentService;
import uk.gov.hmcts.reform.finrem.caseorchestration.service.HelpWithFeesDocumentService;
import uk.gov.hmcts.reform.finrem.caseorchestration.service.InternationalPostalService;
import uk.gov.hmcts.reform.finrem.caseorchestration.service.NotificationService;
import uk.gov.hmcts.reform.finrem.caseorchestration.service.TransferCourtService;
import uk.gov.hmcts.reform.finrem.caseorchestration.service.correspondence.consentorder.ConsentOrderAvailableCorresponder;
import uk.gov.hmcts.reform.finrem.caseorchestration.service.correspondence.consentorder.ConsentOrderNotApprovedCorresponder;
import uk.gov.hmcts.reform.finrem.caseorchestration.service.correspondence.consentorder.ConsentOrderNotApprovedSentCorresponder;
import uk.gov.hmcts.reform.finrem.caseorchestration.service.correspondence.consentorder.ContestedConsentOrderApprovedCorresponder;
import uk.gov.hmcts.reform.finrem.caseorchestration.service.correspondence.consentorder.ContestedConsentOrderNotApprovedCorresponder;
import uk.gov.hmcts.reform.finrem.caseorchestration.service.correspondence.consentorder.ContestedIntermHearingCorresponder;
import uk.gov.hmcts.reform.finrem.caseorchestration.service.correspondence.generalorder.GeneralOrderRaisedCorresponder;
import uk.gov.hmcts.reform.finrem.caseorchestration.service.correspondence.hwf.HwfConsentedApplicantCorresponder;
import uk.gov.hmcts.reform.finrem.caseorchestration.service.correspondence.hwf.HwfContestedApplicantCorresponder;
import uk.gov.hmcts.reform.finrem.caseorchestration.service.correspondence.hwf.HwfCorrespondenceService;
import uk.gov.hmcts.reform.finrem.caseorchestration.service.correspondence.updatefrc.UpdateFrcCorrespondenceService;
import uk.gov.hmcts.reform.finrem.caseorchestration.service.correspondence.updatefrc.UpdateFrcLetterOrEmailAllSolicitorsCorresponder;
import uk.gov.hmcts.reform.finrem.caseorchestration.service.noc.NocLetterNotificationService;
import uk.gov.hmcts.reform.finrem.caseorchestration.service.updatefrc.generators.UpdateFrcInfoLetterDetailsGenerator;
import uk.gov.hmcts.reform.finrem.caseorchestration.service.updatefrc.service.UpdateFrcInfoRespondentDocumentService;

import java.io.IOException;
import java.util.ArrayList;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static uk.gov.hmcts.reform.finrem.caseorchestration.OrchestrationConstants.YES_VALUE;
import static uk.gov.hmcts.reform.finrem.caseorchestration.TestConstants.AUTH_TOKEN;
import static uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.CCDConfigConstant.FINAL_ORDER_COLLECTION;
import static uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.CCDConfigConstant.IS_NOC_REJECTED;

@RunWith(SpringRunner.class)
@WebMvcTest(NotificationsController.class)
@ContextConfiguration(classes = {
    AssignedToJudgeDocumentService.class,
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
    FinremCaseDetailsMapper.class,
    DocumentHelper.class,
    LetterAddresseeGeneratorMapper.class,
    ApplicantLetterAddresseeGenerator.class,
    RespondentLetterAddresseeGenerator.class,
    IntervenerOneLetterAddresseeGenerator.class,
    IntervenerTwoLetterAddresseeGenerator.class,
    IntervenerThreeLetterAddresseeGenerator.class,
    IntervenerFourLetterAddresseeGenerator.class,
    InternationalPostalService.class
})
public class NotificationsControllerTest extends BaseControllerTest {

    @Autowired
    private NotificationsController notificationsController;
    @MockitoBean
    private NocLetterNotificationService nocLetterNotificationService;
    @MockitoBean
    private NotificationService notificationService;
    @MockitoBean
    private HelpWithFeesDocumentService helpWithFeesDocumentService;
    @MockitoBean
    private CaseDataService caseDataService;
    @MockitoBean
    private TransferCourtService transferCourtService;
    @MockitoBean
    private FeatureToggleService featureToggleService;
    @MockitoBean
    private BulkPrintService bulkPrintService;
    @MockitoBean
    private GenericDocumentService genericDocumentService;
    @MockitoBean
    private DocumentConfiguration documentConfiguration;
    @MockitoBean
    private DocumentHelper documentHelper;
    @MockitoBean
    private GeneralOrderRaisedCorresponder generalOrderRaisedCorresponder;
    @MockitoBean
    private FinremCaseDetailsMapper finremCaseDetailsMapper;
    @MockitoBean
    private InternationalPostalService postalService;

    @Override
    @Before
    public void setUp() {
        when(finremCaseDetailsMapper.mapToFinremCaseDetails(any(CaseDetails.class)))
            .thenReturn(getFinremCaseDetailsFromCaseDetails());
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
        when(caseDataService.isApplicantSolicitorAgreeToReceiveEmails(any(CaseDetails.class))).thenReturn(false);

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
    public void sendContestedGeneralApplicationOutcomeEmail() throws IOException {
        when(caseDataService.isConsentedApplication(any(CaseDetails.class))).thenReturn(false);
        when(caseDataService.isApplicantSolicitorAgreeToReceiveEmails(any(CaseDetails.class))).thenReturn(true);

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
        return FinremCaseDetails.builder().data(FinremCaseData.builder().build()).build();
    }
}
