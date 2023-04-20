package uk.gov.hmcts.reform.finrem.caseorchestration.service;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.Before;
import org.junit.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.mock.mockito.MockBean;
import uk.gov.hmcts.reform.ccd.client.model.CallbackRequest;
import uk.gov.hmcts.reform.ccd.client.model.CaseDetails;
import uk.gov.hmcts.reform.finrem.caseorchestration.BaseServiceTest;
import uk.gov.hmcts.reform.finrem.caseorchestration.helper.ConsentedHearingHelper;
import uk.gov.hmcts.reform.finrem.caseorchestration.mapper.NotificationRequestMapper;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.Barrister;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.ConsentedHearingDataWrapper;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.FinremCaseData;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.FinremCaseDetails;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.InterimHearingData;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.InterimHearingItem;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.wrapper.ContactDetailsWrapper;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.notification.NotificationRequest;
import uk.gov.hmcts.reform.finrem.caseorchestration.notifications.service.EmailService;
import uk.gov.hmcts.reform.finrem.caseorchestration.service.noc.solicitors.CheckSolicitorIsDigitalService;

import java.io.IOException;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.stream.Collectors;

import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.timeout;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoMoreInteractions;
import static org.mockito.Mockito.when;
import static uk.gov.hmcts.reform.finrem.caseorchestration.OrchestrationConstants.YES_VALUE;
import static uk.gov.hmcts.reform.finrem.caseorchestration.TestConstants.TEST_SOLICITOR_NAME;
import static uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.CCDConfigConstant.APPLICANT_EMAIL;
import static uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.CCDConfigConstant.APP_SOLICITOR_AGREE_TO_RECEIVE_EMAILS_CONTESTED;
import static uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.CCDConfigConstant.CONTESTED_SOLICITOR_EMAIL;
import static uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.CCDConfigConstant.INTERIM_HEARING_COLLECTION;
import static uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.CCDConfigConstant.RESP_SOLICITOR_EMAIL;
import static uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.CCDConfigConstant.RESP_SOLICITOR_NOTIFICATIONS_EMAIL_CONSENT;
import static uk.gov.hmcts.reform.finrem.caseorchestration.notifications.domain.EmailTemplateNames.FR_ASSIGNED_TO_JUDGE;
import static uk.gov.hmcts.reform.finrem.caseorchestration.notifications.domain.EmailTemplateNames.FR_CONSENTED_GENERAL_ORDER;
import static uk.gov.hmcts.reform.finrem.caseorchestration.notifications.domain.EmailTemplateNames.FR_CONSENTED_LIST_FOR_HEARING;
import static uk.gov.hmcts.reform.finrem.caseorchestration.notifications.domain.EmailTemplateNames.FR_CONSENTED_NOC_CASEWORKER;
import static uk.gov.hmcts.reform.finrem.caseorchestration.notifications.domain.EmailTemplateNames.FR_CONSENTED_NOTICE_OF_CHANGE;
import static uk.gov.hmcts.reform.finrem.caseorchestration.notifications.domain.EmailTemplateNames.FR_CONSENT_GENERAL_EMAIL;
import static uk.gov.hmcts.reform.finrem.caseorchestration.notifications.domain.EmailTemplateNames.FR_CONSENT_ORDER_AVAILABLE;
import static uk.gov.hmcts.reform.finrem.caseorchestration.notifications.domain.EmailTemplateNames.FR_CONSENT_ORDER_AVAILABLE_CTSC;
import static uk.gov.hmcts.reform.finrem.caseorchestration.notifications.domain.EmailTemplateNames.FR_CONSENT_ORDER_MADE;
import static uk.gov.hmcts.reform.finrem.caseorchestration.notifications.domain.EmailTemplateNames.FR_CONSENT_ORDER_NOT_APPROVED;
import static uk.gov.hmcts.reform.finrem.caseorchestration.notifications.domain.EmailTemplateNames.FR_CONSENT_ORDER_NOT_APPROVED_SENT;
import static uk.gov.hmcts.reform.finrem.caseorchestration.notifications.domain.EmailTemplateNames.FR_CONTESTED_APPLICATION_ISSUED;
import static uk.gov.hmcts.reform.finrem.caseorchestration.notifications.domain.EmailTemplateNames.FR_CONTESTED_CONSENT_ORDER_APPROVED;
import static uk.gov.hmcts.reform.finrem.caseorchestration.notifications.domain.EmailTemplateNames.FR_CONTESTED_CONSENT_ORDER_NOT_APPROVED;
import static uk.gov.hmcts.reform.finrem.caseorchestration.notifications.domain.EmailTemplateNames.FR_CONTESTED_DRAFT_ORDER;
import static uk.gov.hmcts.reform.finrem.caseorchestration.notifications.domain.EmailTemplateNames.FR_CONTESTED_GENERAL_APPLICATION_OUTCOME;
import static uk.gov.hmcts.reform.finrem.caseorchestration.notifications.domain.EmailTemplateNames.FR_CONTESTED_GENERAL_EMAIL;
import static uk.gov.hmcts.reform.finrem.caseorchestration.notifications.domain.EmailTemplateNames.FR_CONTESTED_GENERAL_ORDER;
import static uk.gov.hmcts.reform.finrem.caseorchestration.notifications.domain.EmailTemplateNames.FR_CONTESTED_GENERAL_ORDER_CONSENT;
import static uk.gov.hmcts.reform.finrem.caseorchestration.notifications.domain.EmailTemplateNames.FR_CONTESTED_HWF_SUCCESSFUL;
import static uk.gov.hmcts.reform.finrem.caseorchestration.notifications.domain.EmailTemplateNames.FR_CONTESTED_INTERIM_HEARING;
import static uk.gov.hmcts.reform.finrem.caseorchestration.notifications.domain.EmailTemplateNames.FR_CONTESTED_NOC_CASEWORKER;
import static uk.gov.hmcts.reform.finrem.caseorchestration.notifications.domain.EmailTemplateNames.FR_CONTESTED_NOTICE_OF_CHANGE;
import static uk.gov.hmcts.reform.finrem.caseorchestration.notifications.domain.EmailTemplateNames.FR_CONTESTED_PREPARE_FOR_HEARING;
import static uk.gov.hmcts.reform.finrem.caseorchestration.notifications.domain.EmailTemplateNames.FR_CONTESTED_PREPARE_FOR_HEARING_ORDER_SENT;
import static uk.gov.hmcts.reform.finrem.caseorchestration.notifications.domain.EmailTemplateNames.FR_CONTESTED_UPDATE_FRC_COURT;
import static uk.gov.hmcts.reform.finrem.caseorchestration.notifications.domain.EmailTemplateNames.FR_CONTESTED_UPDATE_FRC_SOL;
import static uk.gov.hmcts.reform.finrem.caseorchestration.notifications.domain.EmailTemplateNames.FR_CONTEST_ORDER_APPROVED;
import static uk.gov.hmcts.reform.finrem.caseorchestration.notifications.domain.EmailTemplateNames.FR_CONTEST_ORDER_NOT_APPROVED;
import static uk.gov.hmcts.reform.finrem.caseorchestration.notifications.domain.EmailTemplateNames.FR_HWF_SUCCESSFUL;
import static uk.gov.hmcts.reform.finrem.caseorchestration.notifications.domain.EmailTemplateNames.FR_REJECT_GENERAL_APPLICATION;
import static uk.gov.hmcts.reform.finrem.caseorchestration.notifications.domain.EmailTemplateNames.FR_TRANSFER_TO_LOCAL_COURT;

public class NotificationServiceTest extends BaseServiceTest {

    private static final String END_POINT_HWF_SUCCESSFUL = "http://localhost:8086/notify/hwf-successful";
    private static final String END_POINT_ASSIGNED_TO_JUDGE = "http://localhost:8086/notify/assign-to-judge";
    private static final String END_POINT_CONSENT_ORDER_MADE = "http://localhost:8086/notify/consent-order-made";
    private static final String END_POINT_PREPARE_FOR_HEARING = "http://localhost:8086/notify/prepare-for-hearing";
    private static final String END_POINT_CONSENT_ORDER_NOT_APPROVED = "http://localhost:8086/notify/consent-order-not-approved";
    private static final String END_POINT_CONSENT_ORDER_NOT_APPROVED_SENT = "http://localhost:8086/notify/consent-order-not-approved-sent";
    private static final String END_POINT_CONSENT_ORDER_AVAILABLE = "http://localhost:8086/notify/consent-order-available";
    private static final String END_POINT_CONSENT_ORDER_AVAILABLE_CTSC = "http://localhost:8086/notify/consent-order-available-ctsc";
    private static final String END_POINT_CONTESTED_HWF_SUCCESSFUL = "http://localhost:8086/notify/contested/hwf-successful";
    private static final String END_POINT_CONTESTED_APPLICATION_ISSUED = "http://localhost:8086/notify/contested/application-issued";
    private static final String END_POINT_CONTEST_ORDER_APPROVED = "http://localhost:8086/notify/contested/order-approved";
    private static final String END_POINT_CONTESTED_DRAFT_ORDER = "http://localhost:8086/notify/contested/draft-order";
    private static final String END_POINT_GENERAL_EMAIL_CONSENT = "http://localhost:8086/notify/general-email";
    private static final String END_POINT_GENERAL_EMAIL_CONTESTED = "http://localhost:8086/notify/contested/general-email";
    private static final String END_POINT_CONTEST_ORDER_NOT_APPROVED = "http://localhost:8086/notify/contested/order-not-approved";
    private static final String END_POINT_CONTESTED_CONSENT_ORDER_APPROVED = "http://localhost:8086/notify/contested/consent-order-approved";
    private static final String END_POINT_CONTESTED_CONSENT_GENERAL_ORDER = "http://localhost:8086/notify/contested/consent-general-order";
    private static final String END_POINT_CONTESTED_GENERAL_ORDER = "http://localhost:8086/notify/contested/general-order";
    private static final String END_POINT_CONSENTED_GENERAL_ORDER = "http://localhost:8086/notify/general-order";
    private static final String END_POINT_CONTESTED_GENERAL_APPLICATION_REFER_TO_JUDGE =
        "http://localhost:8086/notify/contested/general-application-refer-to-judge";
    private static final String END_POINT_CONTESTED_GENERAL_APPLICATION_REJECTED =
        "http://localhost:8086/notify/contested/general-application-rejected";
    private static final String END_POINT_CONTESTED_GENERAL_APPLICATION_OUTCOME =
        "http://localhost:8086/notify/contested/general-application-outcome";
    private static final String END_POINT_CONTESTED_CONSENT_ORDER_NOT_APPROVED = "http://localhost:8086/notify/contested/consent-order-not-approved";
    private static final String END_POINT_CONTESTED_INTERIM_HEARING = "http://localhost:8086/notify/contested/prepare-for-interim-hearing-sent";
    private static final String END_POINT_TRANSFER_TO_LOCAL_COURT = "http://localhost:8086/notify/transfer-to-local-court";
    private static final String END_POINT_NOTICE_OF_CHANGE_CONSENTED = "http://localhost:8086/notify/notice-of-change";
    private static final String END_POINT_NOTICE_OF_CHANGE_CONTESTED = "http://localhost:8086/notify/contested/notice-of-change";
    private static final String END_POINT_NOC_CASEWORKER_CONTESTED = "http://localhost:8086/notify/contested/notice-of-change/caseworker";
    private static final String END_POINT_NOC_CASEWORKER_CONSENTED = "http://localhost:8086/notify/notice-of-change/caseworker";
    private static final String END_POINT_UPDATE_FRC_INFORMATION = "http://localhost:8086/notify/contested/update-frc-information";
    private static final String END_POINT_UPDATE_FRC_INFO_COURT = "http://localhost:8086/notify/contested/update-frc-information/court";
    private static final String END_POINT_LIST_FOR_HEARING_SUCCESSFUL = "http://localhost:8086/notify/list-for-hearing";
    private static final String ERROR_500_MESSAGE = "500 Internal Server Error";
    private static final String TEST_USER_EMAIL = "fr_applicant_sol@sharklasers.com";
    private static final String NOTTINGHAM_FRC_EMAIL = "FRCNottingham@justice.gov.uk";

    private static final String INTERIM_HEARING_JSON = "/fixtures/contested/interim-hearing-two-collection.json";
    private static final String CONSENTED_HEARING_JSON = "/fixtures/consented.listOfHearing/list-for-hearing.json";

    @Autowired
    private NotificationService notificationService;
    @Autowired
    private EmailService emailService;
    @Autowired
    private ConsentedHearingHelper helper;

    @MockBean
    private FeatureToggleService featureToggleService;
    @MockBean
    private NotificationRequestMapper notificationRequestMapper;
    @MockBean
    private CheckSolicitorIsDigitalService checkSolicitorIsDigitalService;
    @MockBean
    private CaseDataService caseDataService;

    private CallbackRequest callbackRequest;
    private NotificationRequest notificationRequest;


    @Before
    public void setUp() {
        callbackRequest = getConsentedCallbackRequest();
        notificationRequest = new NotificationRequest();
        when(notificationRequestMapper.getNotificationRequestForApplicantSolicitor(any(CaseDetails.class))).thenReturn(notificationRequest);
        when(notificationRequestMapper.getNotificationRequestForRespondentSolicitor(any(CaseDetails.class))).thenReturn(notificationRequest);
    }

    @Test
    public void sendHwfSuccessfulNotificationEmail() {
        notificationService.sendConsentedHWFSuccessfulConfirmationEmail(callbackRequest.getCaseDetails());
        verify(notificationRequestMapper).getNotificationRequestForApplicantSolicitor(callbackRequest.getCaseDetails());
        verify(emailService).sendConfirmationEmail(notificationRequest, FR_HWF_SUCCESSFUL);
    }


    @Test
    public void sendAssignToJudgeNotificationEmailToApplicantSolicitor() {
        notificationService.sendAssignToJudgeConfirmationEmailToApplicantSolicitor(callbackRequest.getCaseDetails());
        verify(notificationRequestMapper).getNotificationRequestForApplicantSolicitor(callbackRequest.getCaseDetails());
        verify(emailService).sendConfirmationEmail(notificationRequest, FR_ASSIGNED_TO_JUDGE);
    }

    @Test
    public void sendAssignToJudgeNotificationEmailToRespondentSolicitor() {
        notificationService.sendAssignToJudgeConfirmationEmailToRespondentSolicitor(callbackRequest.getCaseDetails());
        verify(notificationRequestMapper).getNotificationRequestForRespondentSolicitor(callbackRequest.getCaseDetails());
        verify(emailService).sendConfirmationEmail(notificationRequest, FR_ASSIGNED_TO_JUDGE);
    }

    @Test
    public void sendConsentOrderMadeConfirmationEmailToApplicantSolicitor() {
        notificationService.sendConsentOrderMadeConfirmationEmailToApplicantSolicitor(callbackRequest.getCaseDetails());
        verify(notificationRequestMapper).getNotificationRequestForApplicantSolicitor(callbackRequest.getCaseDetails());
        verify(emailService).sendConfirmationEmail(notificationRequest, FR_CONSENT_ORDER_MADE);
    }

    @Test
    public void sendConsentOrderMadeConfirmationEmailToRespondentSolicitor() {
        notificationService.sendConsentOrderMadeConfirmationEmailToRespondentSolicitor(callbackRequest.getCaseDetails());
        verify(notificationRequestMapper).getNotificationRequestForRespondentSolicitor(callbackRequest.getCaseDetails());
        verify(emailService).sendConfirmationEmail(notificationRequest, FR_CONSENT_ORDER_MADE);
    }

    @Test
    public void sendPrepareForHearingNotificationEmailToApplicantSolicitor() {
        callbackRequest = getContestedCallbackRequest();
        notificationService.sendPrepareForHearingEmailApplicant(callbackRequest.getCaseDetails());

        verify(notificationRequestMapper).getNotificationRequestForApplicantSolicitor(callbackRequest.getCaseDetails());
        verify(emailService).sendConfirmationEmail(notificationRequest, FR_CONTESTED_PREPARE_FOR_HEARING);
    }

    @Test
    public void sendPrepareForHearingNotificationEmailToRespondentSolicitor() {
        callbackRequest = getContestedCallbackRequest();
        notificationService.sendPrepareForHearingEmailRespondent(callbackRequest.getCaseDetails());

        verify(notificationRequestMapper).getNotificationRequestForRespondentSolicitor(callbackRequest.getCaseDetails());
        verify(emailService).sendConfirmationEmail(notificationRequest, FR_CONTESTED_PREPARE_FOR_HEARING);
    }


    @Test
    public void sendPrepareForHearingAfterSentNotificationEmailApplicant() {
        callbackRequest = getContestedCallbackRequest();
        notificationService.sendPrepareForHearingOrderSentEmailApplicant(callbackRequest.getCaseDetails());

        verify(notificationRequestMapper).getNotificationRequestForApplicantSolicitor(callbackRequest.getCaseDetails());
        verify(emailService).sendConfirmationEmail(notificationRequest, FR_CONTESTED_PREPARE_FOR_HEARING_ORDER_SENT);
    }

    @Test
    public void sendPrepareForHearingAfterSentNotificationEmailRespondent() {
        callbackRequest = getContestedCallbackRequest();
        notificationService.sendPrepareForHearingOrderSentEmailRespondent(callbackRequest.getCaseDetails());

        verify(notificationRequestMapper).getNotificationRequestForRespondentSolicitor(callbackRequest.getCaseDetails());
        verify(emailService).sendConfirmationEmail(notificationRequest, FR_CONTESTED_PREPARE_FOR_HEARING_ORDER_SENT);
    }

    @Test
    public void sendConsentOrderNotApprovedNotificationEmailToApplicantSolicitor() {
        notificationService.sendConsentOrderNotApprovedEmailToApplicantSolicitor(callbackRequest.getCaseDetails());

        verify(notificationRequestMapper).getNotificationRequestForApplicantSolicitor(callbackRequest.getCaseDetails());
        verify(emailService).sendConfirmationEmail(notificationRequest, FR_CONSENT_ORDER_NOT_APPROVED);
    }

    @Test
    public void sendConsentOrderNotApprovedNotificationEmailToRespondentSolicitor() {
        notificationService.sendConsentOrderNotApprovedEmailToRespondentSolicitor(callbackRequest.getCaseDetails());

        verify(notificationRequestMapper).getNotificationRequestForRespondentSolicitor(callbackRequest.getCaseDetails());
        verify(emailService).sendConfirmationEmail(notificationRequest, FR_CONSENT_ORDER_NOT_APPROVED);
    }

    @Test
    public void sendConsentOrderAvailableNotificationEmail() {
        notificationService.sendConsentOrderAvailableEmailToApplicantSolicitor(callbackRequest.getCaseDetails());

        verify(notificationRequestMapper).getNotificationRequestForApplicantSolicitor(callbackRequest.getCaseDetails());
        verify(emailService).sendConfirmationEmail(notificationRequest, FR_CONSENT_ORDER_AVAILABLE);
    }

    @Test
    public void sendConsentOrderAvailableNotificationCtscEmail() {
        notificationService.sendConsentOrderAvailableCtscEmail(callbackRequest.getCaseDetails());

        verify(notificationRequestMapper).getNotificationRequestForApplicantSolicitor(callbackRequest.getCaseDetails());
        verify(emailService).sendConfirmationEmail(notificationRequest, FR_CONSENT_ORDER_AVAILABLE_CTSC);
    }


    @Test
    public void sendContestedApplicationIssuedEmail() {
        notificationService.sendContestedApplicationIssuedEmailToApplicantSolicitor(callbackRequest.getCaseDetails());

        verify(notificationRequestMapper).getNotificationRequestForApplicantSolicitor(callbackRequest.getCaseDetails());
        verify(emailService).sendConfirmationEmail(notificationRequest, FR_CONTESTED_APPLICATION_ISSUED);
    }


    @Test
    public void sendContestOrderApprovedEmailApplicantSolicitor() {
        notificationService.sendContestOrderApprovedEmailApplicant(callbackRequest.getCaseDetails());

        verify(notificationRequestMapper, timeout(100).times(1))
            .getNotificationRequestForApplicantSolicitor(callbackRequest.getCaseDetails());
        verify(emailService).sendConfirmationEmail(notificationRequest, FR_CONTEST_ORDER_APPROVED);
    }


    @Test
    public void sendContestOrderApprovedEmailRespondentSolicitor() {
        notificationService.sendContestOrderApprovedEmailRespondent(callbackRequest.getCaseDetails());

        verify(notificationRequestMapper, timeout(100).times(1))
            .getNotificationRequestForRespondentSolicitor(callbackRequest.getCaseDetails());
        verify(emailService).sendConfirmationEmail(notificationRequest, FR_CONTEST_ORDER_APPROVED);
    }


    @Test
    public void sendSolicitorToDraftOrderEmailRespondent() {
        notificationService.sendSolicitorToDraftOrderEmailRespondent(callbackRequest.getCaseDetails());

        verify(notificationRequestMapper).getNotificationRequestForRespondentSolicitor(callbackRequest.getCaseDetails());
        verify(emailService).sendConfirmationEmail(notificationRequest, FR_CONTESTED_DRAFT_ORDER);
    }


    @Test
    public void sendSolicitorToDraftOrderEmailApplicant() {
        notificationService.sendSolicitorToDraftOrderEmailApplicant(callbackRequest.getCaseDetails());

        verify(notificationRequestMapper).getNotificationRequestForApplicantSolicitor(callbackRequest.getCaseDetails());
        verify(emailService).sendConfirmationEmail(notificationRequest, FR_CONTESTED_DRAFT_ORDER);
    }

    @Test
    public void sendContestOrderApprovedEmail() {
        notificationService.sendContestOrderApprovedEmailApplicant(callbackRequest.getCaseDetails());

        verify(notificationRequestMapper, timeout(100).times(1))
            .getNotificationRequestForApplicantSolicitor(callbackRequest.getCaseDetails());
        verify(emailService).sendConfirmationEmail(notificationRequest, FR_CONTEST_ORDER_APPROVED);
    }

    @Test
    public void sendContestedHwfSuccessfulNotificationEmail() {
        callbackRequest = getContestedCallbackRequest();
        notificationService.sendContestedHwfSuccessfulConfirmationEmail(callbackRequest.getCaseDetails());

        verify(notificationRequestMapper).getNotificationRequestForApplicantSolicitor(callbackRequest.getCaseDetails());
        verify(emailService).sendConfirmationEmail(notificationRequest, FR_CONTESTED_HWF_SUCCESSFUL);
    }

    @Test
    public void sendGeneralEmailConsented() {
        notificationService.sendConsentGeneralEmail(callbackRequest.getCaseDetails());

        verify(notificationRequestMapper).getNotificationRequestForApplicantSolicitor(callbackRequest.getCaseDetails());
        verify(emailService).sendConfirmationEmail(notificationRequest, FR_CONSENT_GENERAL_EMAIL);
    }

    @Test
    public void sendGeneralEmailContested() {
        notificationService.sendContestedGeneralEmail(callbackRequest.getCaseDetails());

        verify(notificationRequestMapper).getNotificationRequestForApplicantSolicitor(callbackRequest.getCaseDetails());
        verify(emailService).sendConfirmationEmail(notificationRequest, FR_CONTESTED_GENERAL_EMAIL);
    }

    @Test
    public void sendContestOrderNotApprovedNotificationEmailApplicant() {

        notificationService.sendContestOrderNotApprovedEmailApplicant(callbackRequest.getCaseDetails());

        verify(notificationRequestMapper).getNotificationRequestForApplicantSolicitor(callbackRequest.getCaseDetails());
        verify(emailService).sendConfirmationEmail(notificationRequest, FR_CONTEST_ORDER_NOT_APPROVED);
    }

    @Test
    public void sendContestOrderNotApprovedNotificationEmailRespondent() {
        notificationService.sendContestOrderNotApprovedEmailRespondent(callbackRequest.getCaseDetails());

        verify(notificationRequestMapper).getNotificationRequestForRespondentSolicitor(callbackRequest.getCaseDetails());
        verify(emailService).sendConfirmationEmail(notificationRequest, FR_CONTEST_ORDER_NOT_APPROVED);
    }

    @Test
    public void sendContestedConsentOrderApprovedNotificationEmailToApplicantSolicitor() {
        notificationService.sendContestedConsentOrderApprovedEmailToApplicantSolicitor(callbackRequest.getCaseDetails());

        verify(notificationRequestMapper).getNotificationRequestForApplicantSolicitor(callbackRequest.getCaseDetails());
        verify(emailService).sendConfirmationEmail(notificationRequest, FR_CONTESTED_CONSENT_ORDER_APPROVED);
    }

    @Test
    public void sendContestedConsentOrderApprovedNotificationEmailToRespondentSolicitor() {

        notificationService.sendContestedConsentOrderApprovedEmailToRespondentSolicitor(callbackRequest.getCaseDetails());

        verify(notificationRequestMapper).getNotificationRequestForRespondentSolicitor(callbackRequest.getCaseDetails());
        verify(emailService).sendConfirmationEmail(notificationRequest, FR_CONTESTED_CONSENT_ORDER_APPROVED);
    }


    @Test
    public void sendContestedGeneralApplicationOutcomeNotificationEmailWhenSendToFRCToggleTrue() throws IOException {
        when(featureToggleService.isSendToFRCEnabled()).thenReturn(true);

        callbackRequest = getContestedCallbackRequest();
        notificationService.sendContestedGeneralApplicationOutcomeEmail(callbackRequest.getCaseDetails());

        verify(notificationRequestMapper).getNotificationRequestForApplicantSolicitor(callbackRequest.getCaseDetails());
        verify(emailService).sendConfirmationEmail(notificationRequest, FR_CONTESTED_GENERAL_APPLICATION_OUTCOME);
    }

    @Test
    public void sendContestedConsentGeneralOrderNotificationEmailApplicantSolicitor() {
        notificationService.sendContestedConsentGeneralOrderEmailApplicantSolicitor(callbackRequest.getCaseDetails());

        verify(notificationRequestMapper).getNotificationRequestForApplicantSolicitor(callbackRequest.getCaseDetails());
        verify(emailService).sendConfirmationEmail(notificationRequest, FR_CONTESTED_GENERAL_ORDER_CONSENT);
    }

    @Test
    public void sendContestedConsentGeneralOrderNotificationEmailRespondentSolicitor() {
        notificationService.sendContestedConsentGeneralOrderEmailRespondentSolicitor(callbackRequest.getCaseDetails());

        verify(notificationRequestMapper).getNotificationRequestForRespondentSolicitor(callbackRequest.getCaseDetails());
        verify(emailService).sendConfirmationEmail(notificationRequest, FR_CONTESTED_GENERAL_ORDER_CONSENT);
    }

    @Test
    public void sendContestedGeneralOrderNotificationEmailApplicant() {
        notificationService.sendContestedGeneralOrderEmailApplicant(callbackRequest.getCaseDetails());

        verify(notificationRequestMapper).getNotificationRequestForApplicantSolicitor(callbackRequest.getCaseDetails());
        verify(emailService).sendConfirmationEmail(notificationRequest, FR_CONTESTED_GENERAL_ORDER);
    }

    @Test
    public void sendContestedGeneralOrderNotificationEmailRespondent() {
        notificationService.sendContestedGeneralOrderEmailRespondent(callbackRequest.getCaseDetails());

        verify(notificationRequestMapper).getNotificationRequestForRespondentSolicitor(callbackRequest.getCaseDetails());
        verify(emailService).sendConfirmationEmail(notificationRequest, FR_CONTESTED_GENERAL_ORDER);
    }

    @Test
    public void sendConsentedGeneralOrderNotificationEmailApplicant() {
        notificationService.sendConsentedGeneralOrderEmailToApplicantSolicitor(callbackRequest.getCaseDetails());

        verify(notificationRequestMapper).getNotificationRequestForApplicantSolicitor(callbackRequest.getCaseDetails());
        verify(emailService).sendConfirmationEmail(notificationRequest, FR_CONSENTED_GENERAL_ORDER);
    }

    @Test
    public void sendConsentedGeneralOrderNotificationEmailRespondent() {
        notificationService.sendConsentedGeneralOrderEmailToRespondentSolicitor(callbackRequest.getCaseDetails());

        verify(notificationRequestMapper).getNotificationRequestForRespondentSolicitor(callbackRequest.getCaseDetails());
        verify(emailService).sendConfirmationEmail(notificationRequest, FR_CONSENTED_GENERAL_ORDER);
    }

    @Test
    public void sendContestedConsentOrderNotApprovedNotificationEmail() {
        notificationService.sendContestedConsentOrderNotApprovedEmailApplicantSolicitor(callbackRequest.getCaseDetails());

        verify(notificationRequestMapper).getNotificationRequestForApplicantSolicitor(callbackRequest.getCaseDetails());
        verify(emailService).sendConfirmationEmail(notificationRequest, FR_CONTESTED_CONSENT_ORDER_NOT_APPROVED);
    }

    @Test
    public void sendContestedConsentOrderNotApprovedNotificationEmailToRespondentSolicitor() {
        notificationService.sendContestedConsentOrderNotApprovedEmailRespondentSolicitor(callbackRequest.getCaseDetails());

        verify(notificationRequestMapper).getNotificationRequestForRespondentSolicitor(callbackRequest.getCaseDetails());
        verify(emailService).sendConfirmationEmail(notificationRequest, FR_CONTESTED_CONSENT_ORDER_NOT_APPROVED);
    }

    @Test
    public void sendConsentOrderNotApprovedSentEmailToApplicantSolicitor() {
        notificationService.sendConsentOrderNotApprovedSentEmailToApplicantSolicitor(callbackRequest.getCaseDetails());

        verify(notificationRequestMapper).getNotificationRequestForApplicantSolicitor(callbackRequest.getCaseDetails());
        verify(emailService).sendConfirmationEmail(notificationRequest, FR_CONSENT_ORDER_NOT_APPROVED_SENT);
    }

    @Test
    public void sendConsentOrderNotApprovedSentEmailToRespondentSolicitor() {
        notificationService.sendConsentOrderNotApprovedSentEmailToRespondentSolicitor(callbackRequest.getCaseDetails());

        verify(notificationRequestMapper).getNotificationRequestForRespondentSolicitor(callbackRequest.getCaseDetails());
        verify(emailService).sendConfirmationEmail(notificationRequest, FR_CONSENT_ORDER_NOT_APPROVED_SENT);
    }

    @Test
    public void shouldEmailRespondentSolicitor() {
        Map<String, Object> caseData = new HashMap<>();
        caseData.put(RESP_SOLICITOR_EMAIL, TEST_USER_EMAIL);
        caseData.put(RESP_SOLICITOR_NOTIFICATIONS_EMAIL_CONSENT, YES_VALUE);

        when(caseDataService.isPaperApplication(any())).thenReturn(false);
        when(caseDataService.isRespondentRepresentedByASolicitor(any())).thenReturn(true);
        when(caseDataService.isNotEmpty(RESP_SOLICITOR_EMAIL, caseData)).thenReturn(true);

        assertTrue(notificationService.isRespondentSolicitorEmailCommunicationEnabled(caseData));
    }

    @Test
    public void shouldNotEmailRespondentSolicitor() {
        when(caseDataService.isPaperApplication(any())).thenReturn(true);
        when(caseDataService.isRespondentRepresentedByASolicitor(any())).thenReturn(false);

        assertFalse(notificationService.isRespondentSolicitorEmailCommunicationEnabled(any()));
    }

    @Test
    public void shouldEmailRespondentSolicitorWhenNullEmailConsent() {
        when(caseDataService.isPaperApplication(any())).thenReturn(false);
        when(caseDataService.isRespondentRepresentedByASolicitor(any())).thenReturn(true);
        when(caseDataService.isNotEmpty(any(), any())).thenReturn(true);

        Map<String, Object> caseData = new HashMap<>();
        caseData.put(RESP_SOLICITOR_NOTIFICATIONS_EMAIL_CONSENT, null);

        boolean isRespondentCommunicationEnabled = notificationService.isRespondentSolicitorEmailCommunicationEnabled(caseData);

        assertTrue(isRespondentCommunicationEnabled);
    }

    @Test
    public void shouldEmailContestedAppSolicitor() {
        Map<String, Object> caseData = new HashMap<>();
        caseData.put(CONTESTED_SOLICITOR_EMAIL, TEST_USER_EMAIL);
        caseData.put(APP_SOLICITOR_AGREE_TO_RECEIVE_EMAILS_CONTESTED, YES_VALUE);

        when(caseDataService.isPaperApplication(any())).thenReturn(false);
        when(caseDataService.isApplicantRepresentedByASolicitor(any())).thenReturn(true);
        when(caseDataService.isNotEmpty(CONTESTED_SOLICITOR_EMAIL, caseData)).thenReturn(true);

        assertTrue(notificationService.isContestedApplicantSolicitorEmailCommunicationEnabled(caseData));
    }

    @Test
    public void shouldNotEmailContestedAppSolicitor() {
        when(caseDataService.isPaperApplication(any())).thenReturn(true);
        when(caseDataService.isApplicantRepresentedByASolicitor(any())).thenReturn(false);

        assertFalse(notificationService.isContestedApplicantSolicitorEmailCommunicationEnabled(any()));
    }

    @Test
    public void sendTransferToCourtEmailConsented() {
        notificationService.sendTransferToLocalCourtEmail(callbackRequest.getCaseDetails());

        verify(notificationRequestMapper).getNotificationRequestForApplicantSolicitor(callbackRequest.getCaseDetails());
        verify(emailService).sendConfirmationEmail(notificationRequest, FR_TRANSFER_TO_LOCAL_COURT);
    }

    @Test
    public void sendInterimNotificationEmailToApplicantSolicitor() {
        callbackRequest = getContestedCallbackRequest();
        notificationService.sendInterimNotificationEmailToApplicantSolicitor(callbackRequest.getCaseDetails());

        verify(notificationRequestMapper).getNotificationRequestForApplicantSolicitor(callbackRequest.getCaseDetails());
        verify(emailService).sendConfirmationEmail(notificationRequest, FR_CONTESTED_INTERIM_HEARING);
    }

    @Test
    public void sendInterimNotificationEmailToRespondentSolicitor() {
        callbackRequest = getContestedCallbackRequest();
        notificationService.sendInterimNotificationEmailToRespondentSolicitor(callbackRequest.getCaseDetails());

        verify(notificationRequestMapper).getNotificationRequestForRespondentSolicitor(callbackRequest.getCaseDetails());
        verify(emailService).sendConfirmationEmail(notificationRequest, FR_CONTESTED_INTERIM_HEARING);
    }

    @Test
    public void givenContestedCaseWhenSendNoticeOfChangeEmailThenSendNoticeOfChangeContestedEmail() {
        NotificationRequest notificationRequest = new NotificationRequest();
        notificationRequest.setName(TEST_SOLICITOR_NAME);
        when(notificationRequestMapper.getNotificationRequestForNoticeOfChange(any())).thenReturn(notificationRequest);
        when(checkSolicitorIsDigitalService.isApplicantSolicitorDigital(any())).thenReturn(true);


        notificationService.sendNoticeOfChangeEmail(getContestedCallbackRequest().getCaseDetails());

        verify(notificationRequestMapper).getNotificationRequestForNoticeOfChange(getContestedCallbackRequest().getCaseDetails());
        //  verify(notificationServiceConfiguration).getContestedNoticeOfChange();
        verify(emailService).sendConfirmationEmail(notificationRequest, FR_CONTESTED_NOTICE_OF_CHANGE);
    }

    @Test
    public void givenConsentedCaseWhenSendNoticeOfChangeEmailThenSendNoticeOfChangeContestedEmail() {
        NotificationRequest notificationRequest = new NotificationRequest();
        notificationRequest.setName(TEST_SOLICITOR_NAME);
        when(notificationRequestMapper.getNotificationRequestForNoticeOfChange(any())).thenReturn(notificationRequest);
        when(checkSolicitorIsDigitalService.isApplicantSolicitorDigital(any())).thenReturn(true);
        when(caseDataService.isConsentedApplication(any())).thenReturn(true);

        notificationService.sendNoticeOfChangeEmail(getConsentedCallbackRequest().getCaseDetails());

        verify(notificationRequestMapper).getNotificationRequestForNoticeOfChange(getConsentedCallbackRequest().getCaseDetails());
        verify(emailService).sendConfirmationEmail(notificationRequest, FR_CONSENTED_NOTICE_OF_CHANGE);
    }

    @Test
    public void givenContestedCaseAndNonDigitalSol_whenSendNocEmail_thenNotSendContestedEmail() {
        NotificationRequest notificationRequest = new NotificationRequest();
        notificationRequest.setName(TEST_SOLICITOR_NAME);
        when(notificationRequestMapper.getNotificationRequestForNoticeOfChange(any())).thenReturn(notificationRequest);
        when(checkSolicitorIsDigitalService.isApplicantSolicitorDigital(any())).thenReturn(false);


        notificationService.sendNoticeOfChangeEmail(getContestedCallbackRequest().getCaseDetails());

        verify(notificationRequestMapper).getNotificationRequestForNoticeOfChange(getContestedCallbackRequest().getCaseDetails());
        verifyNoMoreInteractions(emailService);
    }

    @Test
    public void givenContestedCase_whenSendNoCCaseworkerEmail_thenSendContestedEmail() {
        NotificationRequest notificationRequest = new NotificationRequest();
        notificationRequest.setName(TEST_SOLICITOR_NAME);
        when(notificationRequestMapper.getNotificationRequestForNoticeOfChange(any())).thenReturn(notificationRequest);
        when(checkSolicitorIsDigitalService.isApplicantSolicitorDigital(any())).thenReturn(true);

        notificationService.sendNoticeOfChangeEmailCaseworker(getContestedCallbackRequestUpdateDetails()
            .getCaseDetails());

        verify(notificationRequestMapper).getNotificationRequestForNoticeOfChange(getContestedCallbackRequestUpdateDetails()
            .getCaseDetails());
        verify(emailService).sendConfirmationEmail(notificationRequest, FR_CONTESTED_NOC_CASEWORKER);
    }

    @Test
    public void givenConsentedCase_whenSendNoCCaseworkerEmail_thenSendConsentedEmail() {
        NotificationRequest notificationRequest = new NotificationRequest();
        notificationRequest.setName(TEST_SOLICITOR_NAME);
        when(notificationRequestMapper.getNotificationRequestForNoticeOfChange(any())).thenReturn(notificationRequest);
        when(checkSolicitorIsDigitalService.isApplicantSolicitorDigital(any())).thenReturn(true);
        when(caseDataService.isConsentedApplication(any())).thenReturn(true);

        notificationService.sendNoticeOfChangeEmailCaseworker(getConsentedCallbackRequestUpdateDetails()
            .getCaseDetails());

        verify(notificationRequestMapper).getNotificationRequestForNoticeOfChange(getConsentedCallbackRequestUpdateDetails()
            .getCaseDetails());
        verify(emailService).sendConfirmationEmail(notificationRequest, FR_CONSENTED_NOC_CASEWORKER);
    }

    @Test
    public void givenContestedCaseAndNonDigitalSol_whenSendNocEmail_thenNotSendContestedEmailCaseworker() {
        NotificationRequest notificationRequest = new NotificationRequest();
        notificationRequest.setName(TEST_SOLICITOR_NAME);
        when(notificationRequestMapper.getNotificationRequestForNoticeOfChange(any())).thenReturn(notificationRequest);
        when(checkSolicitorIsDigitalService.isApplicantSolicitorDigital(any())).thenReturn(false);

        notificationService.sendNoticeOfChangeEmailCaseworker(getContestedCallbackRequest().getCaseDetails());

        verify(notificationRequestMapper).getNotificationRequestForNoticeOfChange(getContestedCallbackRequest().getCaseDetails());
        verifyNoMoreInteractions(emailService);
    }

    public void sendUpdateFrcInformationEmailToAppSolicitor() {
        callbackRequest = getContestedCallbackRequest();
        notificationService.sendUpdateFrcInformationEmailToAppSolicitor(callbackRequest.getCaseDetails());
        verify(notificationRequestMapper).getNotificationRequestForApplicantSolicitor(callbackRequest.getCaseDetails());
        verify(emailService).sendConfirmationEmail(notificationRequest, FR_CONTESTED_UPDATE_FRC_SOL);
    }

    @Test
    public void sendUpdateFrcInformationEmailToRespSolicitor() {
        callbackRequest = getContestedCallbackRequest();

        notificationService.sendUpdateFrcInformationEmailToRespondentSolicitor(callbackRequest.getCaseDetails());
        verify(notificationRequestMapper).getNotificationRequestForRespondentSolicitor(callbackRequest.getCaseDetails());
        verify(emailService).sendConfirmationEmail(notificationRequest, FR_CONTESTED_UPDATE_FRC_SOL);
    }

    @Test
    public void sendUpdateFrcInformationEmailToCourt() throws JsonProcessingException {
        when(featureToggleService.isSendToFRCEnabled()).thenReturn(true);
        callbackRequest = getContestedCallbackRequest();

        notificationService.sendUpdateFrcInformationEmailToCourt(callbackRequest.getCaseDetails());
        verify(notificationRequestMapper).getNotificationRequestForApplicantSolicitor(callbackRequest.getCaseDetails());
        verify(emailService).sendConfirmationEmail(notificationRequest, FR_CONTESTED_UPDATE_FRC_COURT);
    }

    @Test
    public void sendGeneralApplicationRejectionEmailApplicantSolicitor() {
        callbackRequest = getContestedCallbackRequest();

        notificationService.sendGeneralApplicationRejectionEmailToAppSolicitor(callbackRequest.getCaseDetails());
        verify(notificationRequestMapper).getNotificationRequestForApplicantSolicitor(callbackRequest.getCaseDetails());
        verify(emailService).sendConfirmationEmail(notificationRequest, FR_REJECT_GENERAL_APPLICATION);
    }

    @Test
    public void sendGeneralApplicationRejectionEmailRespondentSolicitor() {
        callbackRequest = getContestedCallbackRequest();

        notificationService.sendGeneralApplicationRejectionEmailToResSolicitor(callbackRequest.getCaseDetails());
        verify(notificationRequestMapper).getNotificationRequestForRespondentSolicitor(callbackRequest.getCaseDetails());
        verify(emailService).sendConfirmationEmail(notificationRequest, FR_REJECT_GENERAL_APPLICATION);
    }

    @Test
    public void shouldEmailApplicantSolicitorWhenApplicantSolicitorIsRegisteredAndAcceptingEmails() {
        when(checkSolicitorIsDigitalService.isApplicantSolicitorDigital(any())).thenReturn(true);
        when(caseDataService.isApplicantSolicitorEmailPopulated(any())).thenReturn(true);

        assertTrue(notificationService.isApplicantSolicitorDigitalAndEmailPopulated(CaseDetails.builder()
            .id(123450L).build()));
    }


    @Test
    public void shouldEmailApplicantSolicitorWhenApplicantSolicitorIsDigitalAndEmailIsPopulated() {
        FinremCaseData caseData = FinremCaseData.builder().contactDetailsWrapper(
            ContactDetailsWrapper.builder().applicantSolicitorEmail(APPLICANT_EMAIL).build()).build();
        FinremCaseDetails caseDetails = FinremCaseDetails.builder().id(123456780L).data(caseData).build();

        when(checkSolicitorIsDigitalService.isApplicantSolicitorDigital(caseDetails.getId().toString())).thenReturn(true);

        assertTrue(notificationService.isApplicantSolicitorDigitalAndEmailPopulated(caseDetails));
    }

    @Test
    public void shouldNotEmailApplicantSolicitorWhenApplicantSolicitorIsNotDigitalAndEmailIsPopulatedFinrem() {
        FinremCaseData caseData = FinremCaseData.builder().contactDetailsWrapper(
            ContactDetailsWrapper.builder().respondentSolicitorEmail(APPLICANT_EMAIL).build()).build();
        FinremCaseDetails caseDetails = FinremCaseDetails.builder().id(123456780L).data(caseData).build();

        when(checkSolicitorIsDigitalService.isApplicantSolicitorDigital(caseDetails.getId().toString())).thenReturn(false);

        assertFalse(notificationService.isApplicantSolicitorDigitalAndEmailPopulated(caseDetails));
    }

    @Test
    public void shouldNotEmailApplicantSolicitorWhenApplicantSolicitorIsDigitalAndEmailIsNotPopulatedFinrem() {
        FinremCaseData caseData = FinremCaseData.builder().contactDetailsWrapper(
            ContactDetailsWrapper.builder().applicantSolicitorEmail(null).build()).build();
        FinremCaseDetails caseDetails = FinremCaseDetails.builder().id(123456780L).data(caseData).build();

        when(checkSolicitorIsDigitalService.isApplicantSolicitorDigital(caseDetails.getId().toString())).thenReturn(false);

        assertFalse(notificationService.isApplicantSolicitorDigitalAndEmailPopulated(caseDetails));
    }


    @Test
    public void shouldNotEmailApplicantSolicitorWhenApplicantSolicitorIsNotRegisteredButIsAcceptingEmails() {
        when(checkSolicitorIsDigitalService.isApplicantSolicitorDigital(any())).thenReturn(false);
        when(caseDataService.isApplicantSolicitorAgreeToReceiveEmails(any())).thenReturn(true);

        assertFalse(notificationService.isApplicantSolicitorDigitalAndEmailPopulated(CaseDetails.builder()
            .id(1234567890L).build()));
    }

    @Test
    public void shouldNotEmailApplicantSolicitorWhenApplicantSolicitorIsNotDigital() {
        when(checkSolicitorIsDigitalService.isApplicantSolicitorDigital(any())).thenReturn(false);
        when(caseDataService.isApplicantSolicitorAgreeToReceiveEmails(any())).thenReturn(true);

        assertFalse(notificationService.isApplicantSolicitorDigitalAndEmailPopulated(CaseDetails.builder()
            .id(1234567890L).build()));
    }

    @Test
    public void shouldEmailRespondentSolicitorWhenRespondentSolicitorIsRegisteredAndAcceptingEmails() {
        when(checkSolicitorIsDigitalService.isRespondentSolicitorDigital(any())).thenReturn(true);

        Map<String, Object> caseData = new HashMap<>();

        when(caseDataService.isPaperApplication(caseData)).thenReturn(false);
        when(caseDataService.isRespondentRepresentedByASolicitor(caseData)).thenReturn(true);
        when(caseDataService.isNotEmpty(RESP_SOLICITOR_EMAIL, caseData)).thenReturn(true);

        caseData.put(RESP_SOLICITOR_NOTIFICATIONS_EMAIL_CONSENT, YES_VALUE);
        CaseDetails caseDetails = CaseDetails.builder().id(123456780L).data(caseData).build();

        assertTrue(notificationService.isRespondentSolicitorRegisteredAndEmailCommunicationEnabled(caseDetails));
    }

    @Test
    public void shouldEmailRespondentSolicitorWhenRespondentSolicitorIsDigitalAndEmailPopulated() {

        FinremCaseData caseData = FinremCaseData.builder().contactDetailsWrapper(
            ContactDetailsWrapper.builder().respondentSolicitorEmail(RESP_SOLICITOR_EMAIL).build()).build();
        FinremCaseDetails caseDetails = FinremCaseDetails.builder().id(123456780L).data(caseData).build();

        when(checkSolicitorIsDigitalService.isRespondentSolicitorDigital(caseDetails.getId().toString())).thenReturn(true);

        assertTrue(notificationService.isRespondentSolicitorDigitalAndEmailPopulated(caseDetails));
    }

    @Test
    public void shouldNotEmailRespondentSolicitorWhenRespondentSolicitorIsNotRegisteredAndAcceptingEmails() {
        when(checkSolicitorIsDigitalService.isRespondentSolicitorDigital(any())).thenReturn(false);

        Map<String, Object> caseData = new HashMap<>();

        when(caseDataService.isPaperApplication(caseData)).thenReturn(false);
        when(caseDataService.isRespondentRepresentedByASolicitor(caseData)).thenReturn(true);
        when(caseDataService.isNotEmpty(RESP_SOLICITOR_EMAIL, caseData)).thenReturn(true);

        caseData.put(RESP_SOLICITOR_NOTIFICATIONS_EMAIL_CONSENT, YES_VALUE);
        CaseDetails caseDetails = CaseDetails.builder().id(123450L).data(caseData).build();

        assertFalse(notificationService.isRespondentSolicitorRegisteredAndEmailCommunicationEnabled(caseDetails));
    }

    @Test
    public void shouldEmailRespondentSolicitorWhenRespondentSolicitorIsDigitalAndEmailIsPopulated() {
        when(checkSolicitorIsDigitalService.isRespondentSolicitorDigital(any())).thenReturn(true);
        Map<String, Object> caseData = new HashMap<>();
        caseData.put(RESP_SOLICITOR_EMAIL, "someemailaddress@email.com");
        when(caseDataService.isNotEmpty(RESP_SOLICITOR_EMAIL, caseData)).thenReturn(true);
        CaseDetails caseDetails = CaseDetails.builder().id(123456780L).data(caseData).build();

        assertTrue(notificationService.isRespondentSolicitorDigitalAndEmailPopulated(caseDetails));
    }


    @Test
    public void shouldNotEmailRespondentSolicitorWhenRespondentSolicitorIsNotDigitalAndEmailIsPopulated() {
        when(checkSolicitorIsDigitalService.isRespondentSolicitorDigital(any())).thenReturn(false);
        Map<String, Object> caseData = new HashMap<>();
        caseData.put(RESP_SOLICITOR_EMAIL, "someemailaddress@email.com");
        when(caseDataService.isNotEmpty(RESP_SOLICITOR_EMAIL, caseData)).thenReturn(true);
        CaseDetails caseDetails = CaseDetails.builder().id(123456780L).data(caseData).build();

        assertFalse(notificationService.isRespondentSolicitorDigitalAndEmailPopulated(caseDetails));
    }

    @Test
    public void shouldNotEmailRespondentSolicitorWhenRespondentSolicitorIsNotDigitalAndEmailIsPopulatedFinrem() {
        FinremCaseData caseData = FinremCaseData.builder().contactDetailsWrapper(
            ContactDetailsWrapper.builder().respondentSolicitorEmail(RESP_SOLICITOR_EMAIL).build()).build();
        FinremCaseDetails caseDetails = FinremCaseDetails.builder().id(123456780L).data(caseData).build();

        when(checkSolicitorIsDigitalService.isRespondentSolicitorDigital(caseDetails.getId().toString())).thenReturn(false);

        assertFalse(notificationService.isRespondentSolicitorDigitalAndEmailPopulated(caseDetails));
    }

    @Test
    public void shouldNotEmailRespondentSolicitorWhenRespondentSolicitorIsDigitalAndEmailIsNotPopulatedFinrem() {
        FinremCaseData caseData = FinremCaseData.builder().contactDetailsWrapper(
            ContactDetailsWrapper.builder().respondentSolicitorEmail(null).build()).build();
        FinremCaseDetails caseDetails = FinremCaseDetails.builder().id(123456780L).data(caseData).build();

        when(checkSolicitorIsDigitalService.isRespondentSolicitorDigital(caseDetails.getId().toString())).thenReturn(false);

        assertFalse(notificationService.isRespondentSolicitorDigitalAndEmailPopulated(caseDetails));
    }

    @Test
    public void givenAppIsContestedAndApplicantSolicitorIsNotRegisteredOrAcceptingEmails_shouldSendLettersApplicantSolicitor() {
        when(caseDataService.isContestedPaperApplication(any())).thenReturn(true);
        when(notificationService.isApplicantSolicitorDigitalAndEmailPopulated(any(CaseDetails.class))).thenReturn(false);
        when(checkSolicitorIsDigitalService.isApplicantSolicitorDigital(any())).thenReturn(false);

        assertTrue(notificationService.isContestedApplicationAndApplicantOrRespondentSolicitorsIsNotRegisteredOrAcceptingEmails(any()));
    }

    @Test
    public void givenAppIsNotContestedAndApplicantSolicitorIsRegisteredAndAcceptingEmails_shouldNotSendLetters() {
        when(caseDataService.isContestedApplication(any())).thenReturn(false);
        when(notificationService.isApplicantSolicitorDigitalAndEmailPopulated(any(CaseDetails.class))).thenReturn(true);
        when(checkSolicitorIsDigitalService.isApplicantSolicitorDigital(any())).thenReturn(true);

        Map<String, Object> caseData = new HashMap<>();
        caseData.put(RESP_SOLICITOR_NOTIFICATIONS_EMAIL_CONSENT, YES_VALUE);

        when(checkSolicitorIsDigitalService.isRespondentSolicitorDigital(any())).thenReturn(true);
        when(caseDataService.isNotEmpty(RESP_SOLICITOR_EMAIL, caseData)).thenReturn(true);
        when(caseDataService.isPaperApplication(any())).thenReturn(false);
        when(caseDataService.isRespondentRepresentedByASolicitor(any())).thenReturn(true);

        CaseDetails caseDetails = CaseDetails.builder().data(caseData).build();

        assertFalse(notificationService.isContestedApplicationAndApplicantOrRespondentSolicitorsIsNotRegisteredOrAcceptingEmails(caseDetails));
    }

    @Test
    public void isContestedAndRespondentSolicitorIsNotRegisteredOrAcceptingEmails() {
        when(caseDataService.isContestedPaperApplication(any())).thenReturn(true);
        when(checkSolicitorIsDigitalService.isRespondentSolicitorDigital(any())).thenReturn(true);
        when(caseDataService.isRespondentRepresentedByASolicitor(any())).thenReturn(false);

        Map<String, Object> caseData = new HashMap<>();
        when(caseDataService.isNotEmpty(RESP_SOLICITOR_EMAIL, caseData)).thenReturn(false);
        caseData.put(RESP_SOLICITOR_NOTIFICATIONS_EMAIL_CONSENT, null);
        CaseDetails caseDetails = CaseDetails.builder().data(caseData).build();

        assertTrue(notificationService.isContestedApplicationAndApplicantOrRespondentSolicitorsIsNotRegisteredOrAcceptingEmails(caseDetails));
    }

    @Test
    public void givenBarristerAdded_sendAddedEmail() {
        Barrister barrister = new Barrister().toBuilder().build();
        CaseDetails caseDetails = CaseDetails.builder().build();
        when(notificationRequestMapper.buildInterimHearingNotificationRequest(caseDetails, barrister)).thenReturn(notificationRequest);
        notificationService.sendBarristerAddedEmail(caseDetails, barrister);
        verify(notificationRequestMapper).buildInterimHearingNotificationRequest(caseDetails, barrister);
    }

    @Test
    public void givenBarristerRemoved_sendRemovedEmail() {
        Barrister barrister = new Barrister().toBuilder().build();
        CaseDetails caseDetails = CaseDetails.builder().build();
        when(notificationRequestMapper.buildInterimHearingNotificationRequest(caseDetails, barrister)).thenReturn(notificationRequest);
        notificationService.sendBarristerRemovedEmail(caseDetails, barrister);
        verify(notificationRequestMapper).buildInterimHearingNotificationRequest(caseDetails, barrister);
    }

    @Test
    public void sendInterimHearingNotificationEmailToApplicantSolicitor() {
        CallbackRequest callbackRequest = buildHearingCallbackRequest(INTERIM_HEARING_JSON);
        Map<String, Object> caseData = callbackRequest.getCaseDetails().getData();

        List<InterimHearingData> interimHearingList = Optional.ofNullable(caseData.get(INTERIM_HEARING_COLLECTION))
            .map(this::convertToInterimHearingDataList).orElse(Collections.emptyList());

        List<InterimHearingItem> interimHearingItems
            = interimHearingList.stream().map(InterimHearingData::getValue).collect(Collectors.toList());

        List<Map<String, Object>> interimDataMap = interimHearingItems.stream()
            .map(obj -> new ObjectMapper().convertValue(obj, new TypeReference<Map<String, Object>>() {
            })).collect(Collectors.toList());

        when(notificationRequestMapper.getNotificationRequestForApplicantSolicitor(any(CaseDetails.class), any())).thenReturn(notificationRequest);

        interimDataMap.forEach(data -> {
            notificationService.sendInterimHearingNotificationEmailToApplicantSolicitor(callbackRequest.getCaseDetails(), data);
            verify(notificationRequestMapper).getNotificationRequestForApplicantSolicitor(callbackRequest.getCaseDetails(), data);

        });
        verify(emailService, times(2)).sendConfirmationEmail(notificationRequest, FR_CONTESTED_INTERIM_HEARING);
    }

    @Test
    public void sendInterimHearingNotificationEmailToRespondentSolicitor() {
        CallbackRequest callbackRequest = buildHearingCallbackRequest(INTERIM_HEARING_JSON);
        Map<String, Object> caseData = callbackRequest.getCaseDetails().getData();

        List<InterimHearingData> interimHearingList = Optional.ofNullable(caseData.get(INTERIM_HEARING_COLLECTION))
            .map(this::convertToInterimHearingDataList).orElse(Collections.emptyList());

        List<InterimHearingItem> interimHearingItems
            = interimHearingList.stream().map(InterimHearingData::getValue).collect(Collectors.toList());

        List<Map<String, Object>> interimDataMap = interimHearingItems.stream()
            .map(obj -> new ObjectMapper().convertValue(obj, new TypeReference<Map<String, Object>>() {
            })).collect(Collectors.toList());

        when(notificationRequestMapper.getNotificationRequestForRespondentSolicitor(any(CaseDetails.class), any())).thenReturn(notificationRequest);

        interimDataMap.forEach(data -> {
            notificationService.sendInterimHearingNotificationEmailToRespondentSolicitor(callbackRequest.getCaseDetails(), data);
            verify(notificationRequestMapper).getNotificationRequestForRespondentSolicitor(callbackRequest.getCaseDetails(), data);
        });
        verify(emailService, times(2)).sendConfirmationEmail(notificationRequest, FR_CONTESTED_INTERIM_HEARING);
    }

    @Test
    public void sendConsentedHearingNotificationEmailToApplicantSolicitor() {
        CallbackRequest callbackRequest = buildHearingCallbackRequest(CONSENTED_HEARING_JSON);
        Map<String, Object> caseData = callbackRequest.getCaseDetails().getData();

        List<ConsentedHearingDataWrapper> hearings = helper.getHearings(caseData);
        List<String> hearingIdsToProcess = List.of("1f7e210d-87d8-4e98-8c48-db15d1dc0d14");
        when(notificationRequestMapper.getNotificationRequestForConsentApplicantSolicitor(any(CaseDetails.class), any())).thenReturn(
            notificationRequest);

        hearings.forEach(hearingData -> {
            if (hearingIdsToProcess.contains(hearingData.getId())) {
                Map<String, Object> data = helper.convertToMap(hearingData.getValue());
                notificationService.sendConsentHearingNotificationEmailToApplicantSolicitor(callbackRequest.getCaseDetails(), data);
                verify(notificationRequestMapper).getNotificationRequestForConsentApplicantSolicitor(callbackRequest.getCaseDetails(), data);
                verify(emailService).sendConfirmationEmail(notificationRequest, FR_CONSENTED_LIST_FOR_HEARING);
            }
        });
    }

    @Test
    public void sendConsentedHearingNotificationEmailToRespondentSolicitor() {
        CallbackRequest callbackRequest = buildHearingCallbackRequest(CONSENTED_HEARING_JSON);
        Map<String, Object> caseData = callbackRequest.getCaseDetails().getData();

        List<ConsentedHearingDataWrapper> hearings = helper.getHearings(caseData);
        List<String> hearingIdsToProcess = List.of("1f7e210d-87d8-4e98-8c48-db15d1dc0d14");
        when(notificationRequestMapper.getNotificationRequestForRespondentSolicitor(any(CaseDetails.class), any())).thenReturn(notificationRequest);


        hearings.forEach(hearingData -> {
            if (hearingIdsToProcess.contains(hearingData.getId())) {
                Map<String, Object> data = helper.convertToMap(hearingData.getValue());
                notificationService.sendConsentHearingNotificationEmailToRespondentSolicitor(callbackRequest.getCaseDetails(), data);
                verify(notificationRequestMapper).getNotificationRequestForRespondentSolicitor(callbackRequest.getCaseDetails(), data);
                verify(emailService).sendConfirmationEmail(notificationRequest, FR_CONSENTED_LIST_FOR_HEARING);
            }
        });
    }

}
