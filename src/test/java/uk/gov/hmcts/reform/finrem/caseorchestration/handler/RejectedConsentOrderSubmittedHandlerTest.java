package uk.gov.hmcts.reform.finrem.caseorchestration.handler;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.MockitoJUnitRunner;
import uk.gov.hmcts.reform.finrem.caseorchestration.service.NotificationService;
import uk.gov.hmcts.reform.finrem.ccd.callback.CallbackRequest;
import uk.gov.hmcts.reform.finrem.ccd.callback.CallbackType;
import uk.gov.hmcts.reform.finrem.ccd.domain.CaseType;
import uk.gov.hmcts.reform.finrem.ccd.domain.EventType;
import uk.gov.hmcts.reform.finrem.ccd.domain.FinremCaseData;
import uk.gov.hmcts.reform.finrem.ccd.domain.FinremCaseDetails;
import uk.gov.hmcts.reform.finrem.ccd.domain.NatureApplication;
import uk.gov.hmcts.reform.finrem.ccd.domain.YesOrNo;

import java.util.List;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.is;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static uk.gov.hmcts.reform.finrem.caseorchestration.TestConstants.TEST_DIVORCE_CASE_NUMBER;
import static uk.gov.hmcts.reform.finrem.caseorchestration.TestConstants.TEST_RESP_SOLICITOR_EMAIL;
import static uk.gov.hmcts.reform.finrem.caseorchestration.TestConstants.TEST_RESP_SOLICITOR_NAME;
import static uk.gov.hmcts.reform.finrem.caseorchestration.TestConstants.TEST_RESP_SOLICITOR_REFERENCE;
import static uk.gov.hmcts.reform.finrem.caseorchestration.TestConstants.TEST_SOLICITOR_EMAIL;
import static uk.gov.hmcts.reform.finrem.caseorchestration.TestConstants.TEST_SOLICITOR_NAME;
import static uk.gov.hmcts.reform.finrem.caseorchestration.TestConstants.TEST_SOLICITOR_REFERENCE;

@RunWith(MockitoJUnitRunner.class)
public class RejectedConsentOrderSubmittedHandlerTest {

    public static final String AUTH_TOKEN = "tokien:)";

    @InjectMocks
    private RejectedConsentOrderSubmittedHandler handler;

    @Mock
    private NotificationService notificationService;


    @Test
    public void givenACcdCallbackConsentCase_WhenSubmitEventRejectOrder_thenHandlerCanHandle() {
        assertThat(handler
                .canHandle(CallbackType.SUBMITTED, CaseType.CONSENTED, EventType.ORDER_REFUSAL),
            is(true));
    }

    @Test
    public void givenACcdCallbackConsentedCase_WhenAnAboutToSubmitCallback_thenHandlerCanNotHandle() {
        assertThat(handler
                .canHandle(CallbackType.ABOUT_TO_SUBMIT, CaseType.CONSENTED, EventType.ORDER_REFUSAL),
            is(false));
    }

    @Test
    public void givenACcdCallbackConsentedCase_WhenContestedCaseType_thenHandlerCanNotHandle() {
        assertThat(handler
                .canHandle(CallbackType.SUBMITTED, CaseType.CONTESTED, EventType.ORDER_REFUSAL),
            is(false));
    }

    @Test
    public void givenACcdCallbackConsentedCase_WhenEventIsClose_thenHandlerCanNotHandle() {
        assertThat(handler
                .canHandle(CallbackType.SUBMITTED, CaseType.CONSENTED, EventType.CLOSE),
            is(false));
    }

    @Test
    public void givenConsentOrderCase_WhenAppSolAgreeToSendEmail_ThenSendConsentOrderNotApprovedEmail() {
        CallbackRequest callbackRequest = getConsentedCallbackRequestForConsentOrder();
        when(notificationService.isApplicantSolicitorRegisteredAndEmailCommunicationEnabled(callbackRequest.getCaseDetails())).thenReturn(true);
        when(notificationService.isRespondentSolicitorEmailCommunicationEnabled(callbackRequest.getCaseDetails().getCaseData())).thenReturn(true);

        handler.handle(callbackRequest, AUTH_TOKEN);

        verify(notificationService).sendConsentOrderNotApprovedEmailToApplicantSolicitor(any());
        verify(notificationService).sendConsentOrderNotApprovedEmailToRespondentSolicitor(any());
    }

    @Test
    public void givenConsentOrderCase_WhenNoConsentToEmail_ThenNoNotificationSent() {
        CallbackRequest callbackRequest = getConsentedCallbackRequestForConsentOrder();

        when(notificationService.isApplicantSolicitorRegisteredAndEmailCommunicationEnabled(callbackRequest.getCaseDetails())).thenReturn(false);
        when(notificationService.isRespondentSolicitorEmailCommunicationEnabled(callbackRequest.getCaseDetails().getCaseData())).thenReturn(false);

        handler.handle(callbackRequest, AUTH_TOKEN);

        verify(notificationService, never()).sendConsentOrderNotApprovedEmailToApplicantSolicitor(any());
        verify(notificationService, never()).sendConsentOrderNotApprovedEmailToRespondentSolicitor(any());
    }

    @Test
    public void givenVariationOrderCase_WhenAppSolAgreeToSendEmail_ThenSendConsentOrderNotApprovedEmail() {
        CallbackRequest callbackRequest = getConsentedCallbackRequestForVariationOrder();
        when(notificationService.isApplicantSolicitorRegisteredAndEmailCommunicationEnabled(callbackRequest.getCaseDetails())).thenReturn(true);
        when(notificationService.isRespondentSolicitorEmailCommunicationEnabled(callbackRequest.getCaseDetails().getCaseData())).thenReturn(true);

        handler.handle(callbackRequest, AUTH_TOKEN);

        verify(notificationService).sendConsentOrderNotApprovedEmailToApplicantSolicitor(any());
        verify(notificationService).sendConsentOrderNotApprovedEmailToRespondentSolicitor(any());
    }

    @Test
    public void givenVariationOrderCase_WhenNoConsentToEmail_ThenNoNotificationSent() {
        CallbackRequest callbackRequest = getConsentedCallbackRequestForVariationOrder();

        when(notificationService.isApplicantSolicitorRegisteredAndEmailCommunicationEnabled(callbackRequest.getCaseDetails())).thenReturn(false);
        when(notificationService.isRespondentSolicitorEmailCommunicationEnabled(callbackRequest.getCaseDetails().getCaseData())).thenReturn(false);

        handler.handle(callbackRequest, AUTH_TOKEN);

        verify(notificationService, never()).sendConsentOrderNotApprovedEmailToApplicantSolicitor(any());
        verify(notificationService, never()).sendConsentOrderNotApprovedEmailToRespondentSolicitor(any());
    }


    @Test
    public void givenContestCase_WhenAppSolAgreeToSendEmail_ThenSendConsentOrderNotApprovedEmail() {
        CallbackRequest callbackRequest = getConsentedCallbackRequestForConsentOrder();
        when(notificationService.isApplicantSolicitorRegisteredAndEmailCommunicationEnabled(callbackRequest.getCaseDetails())).thenReturn(true);
        when(notificationService.isRespondentSolicitorEmailCommunicationEnabled(callbackRequest.getCaseDetails().getCaseData())).thenReturn(true);

        handler.handle(callbackRequest, AUTH_TOKEN);

        verify(notificationService).sendContestOrderNotApprovedEmailApplicant(any());
        verify(notificationService).sendContestOrderNotApprovedEmailRespondent(any());
    }

    @Test
    public void givenContestCase_WhenBothSolNoTAgreeToSendEmail_ThenNoEmailSent() {
        CallbackRequest callbackRequest = getConsentedCallbackRequestForConsentOrder();
        when(notificationService.isApplicantSolicitorRegisteredAndEmailCommunicationEnabled(callbackRequest.getCaseDetails())).thenReturn(false);
        when(notificationService.isRespondentSolicitorEmailCommunicationEnabled(callbackRequest.getCaseDetails().getCaseData())).thenReturn(false);

        handler.handle(callbackRequest, AUTH_TOKEN);

        verify(notificationService, never()).sendContestOrderNotApprovedEmailApplicant(any());
        verify(notificationService, never()).sendContestOrderNotApprovedEmailRespondent(any());
    }

    protected CallbackRequest getConsentedCallbackRequestForConsentOrder() {
        FinremCaseData caseData = new FinremCaseData();
        caseData.getContactDetailsWrapper().setSolicitorEmail(TEST_SOLICITOR_EMAIL);
        caseData.getContactDetailsWrapper().setSolicitorName(TEST_SOLICITOR_NAME);
        caseData.getContactDetailsWrapper().setSolicitorReference(TEST_SOLICITOR_REFERENCE);

        caseData.getContactDetailsWrapper().setRespondentSolicitorEmail(TEST_RESP_SOLICITOR_EMAIL);
        caseData.getContactDetailsWrapper().setRespondentSolicitorName(TEST_RESP_SOLICITOR_NAME);
        caseData.getContactDetailsWrapper().setSolicitorAgreeToReceiveEmails(YesOrNo.YES);
        caseData.getContactDetailsWrapper().setRespondentSolicitorReference(TEST_RESP_SOLICITOR_REFERENCE);
        caseData.getContactDetailsWrapper().setConsentedRespondentRepresented(YesOrNo.YES);
        caseData.setRespSolNotificationsEmailConsent(YesOrNo.YES);

        caseData.setDivorceCaseNumber(TEST_DIVORCE_CASE_NUMBER);
        caseData.setCcdCaseType(CaseType.CONSENTED);

        caseData.getNatureApplicationWrapper().setNatureOfApplication2(List.of(
            NatureApplication.LUMP_SUM_ORDER,
            NatureApplication.PERIODICAL_PAYMENT_ORDER,
            NatureApplication.PENSION_SHARING_ORDER,
            NatureApplication.PENSION_ATTACHMENT_ORDER,
            NatureApplication.PENSION_COMPENSATION_SHARING_ORDER,
            NatureApplication.PENSION_COMPENSATION_ATTACHMENT_ORDER,
            NatureApplication.A_SETTLEMENT_OR_A_TRANSFER_OF_PROPERTY,
            NatureApplication.PROPERTY_ADJUSTMENT_ORDER
            ));

        return CallbackRequest.builder()
            .caseDetails(FinremCaseDetails.builder()
                .caseType(CaseType.CONSENTED)
                .id(12345L)
                .caseData(caseData)
                .build())
            .build();
    }

    protected CallbackRequest getConsentedCallbackRequestForVariationOrder() {
        FinremCaseData caseData = new FinremCaseData();
        caseData.getContactDetailsWrapper().setSolicitorEmail(TEST_SOLICITOR_EMAIL);
        caseData.getContactDetailsWrapper().setSolicitorName(TEST_SOLICITOR_NAME);
        caseData.getContactDetailsWrapper().setSolicitorReference(TEST_SOLICITOR_REFERENCE);
        caseData.getContactDetailsWrapper().setRespondentSolicitorEmail(TEST_RESP_SOLICITOR_EMAIL);
        caseData.getContactDetailsWrapper().setRespondentSolicitorName(TEST_RESP_SOLICITOR_NAME);
        caseData.getContactDetailsWrapper().setRespondentSolicitorReference(TEST_RESP_SOLICITOR_REFERENCE);
        caseData.setDivorceCaseNumber(TEST_DIVORCE_CASE_NUMBER);
        caseData.setCcdCaseType(CaseType.CONSENTED);

        caseData.getNatureApplicationWrapper().setNatureOfApplication2(List.of(
            NatureApplication.LUMP_SUM_ORDER,
            NatureApplication.PERIODICAL_PAYMENT_ORDER,
            NatureApplication.PENSION_SHARING_ORDER,
            NatureApplication.PENSION_ATTACHMENT_ORDER,
            NatureApplication.PENSION_COMPENSATION_SHARING_ORDER,
            NatureApplication.PENSION_COMPENSATION_ATTACHMENT_ORDER,
            NatureApplication.A_SETTLEMENT_OR_A_TRANSFER_OF_PROPERTY,
            NatureApplication.VARIATION_ORDER,
            NatureApplication.PROPERTY_ADJUSTMENT_ORDER
        ));

        return CallbackRequest.builder()
            .caseDetails(FinremCaseDetails.builder()
                .caseType(CaseType.CONSENTED)
                .id(12345L)
                .caseData(caseData)
                .build())
            .build();
    }
}