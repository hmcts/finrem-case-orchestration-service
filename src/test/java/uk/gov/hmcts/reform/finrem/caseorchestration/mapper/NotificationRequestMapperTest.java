package uk.gov.hmcts.reform.finrem.caseorchestration.mapper;

import org.junit.Test;
import org.springframework.beans.factory.annotation.Autowired;
import uk.gov.hmcts.reform.ccd.client.model.CallbackRequest;
import uk.gov.hmcts.reform.finrem.caseorchestration.BaseServiceTest;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.notification.NotificationRequest;

import static org.junit.Assert.assertEquals;
import static uk.gov.hmcts.reform.finrem.caseorchestration.TestConstants.TEST_DIVORCE_CASE_NUMBER;
import static uk.gov.hmcts.reform.finrem.caseorchestration.TestConstants.TEST_RESP_SOLICITOR_EMAIL;
import static uk.gov.hmcts.reform.finrem.caseorchestration.TestConstants.TEST_RESP_SOLICITOR_NAME;
import static uk.gov.hmcts.reform.finrem.caseorchestration.TestConstants.TEST_RESP_SOLICITOR_REFERENCE;
import static uk.gov.hmcts.reform.finrem.caseorchestration.TestConstants.TEST_SOLICITOR_EMAIL;
import static uk.gov.hmcts.reform.finrem.caseorchestration.TestConstants.TEST_SOLICITOR_NAME;
import static uk.gov.hmcts.reform.finrem.caseorchestration.TestConstants.TEST_SOLICITOR_REFERENCE;

public class NotificationRequestMapperTest extends BaseServiceTest {

    @Autowired
    NotificationRequestMapper notificationRequestMapper;

    @Test
    public void shouldCreateNotificationRequestForAppSolicitorForConsentedJourney() {
        NotificationRequest notificationRequest = notificationRequestMapper.createNotificationRequestForAppSolicitor(getConsentedCallbackRequest());

        assertEquals("12345", notificationRequest.getCaseReferenceNumber());
        assertEquals(TEST_SOLICITOR_REFERENCE, notificationRequest.getSolicitorReferenceNumber());
        assertEquals(TEST_DIVORCE_CASE_NUMBER, notificationRequest.getDivorceCaseNumber());
        assertEquals(TEST_SOLICITOR_NAME, notificationRequest.getName());
        assertEquals(TEST_SOLICITOR_EMAIL, notificationRequest.getNotificationEmail());
        assertEquals("consented", notificationRequest.getCaseType());
    }

    @Test
    public void shouldCreateNotificationRequestForAppSolicitorForContestedJourney() {
        CallbackRequest callbackRequest = getContestedCallbackRequest();

        NotificationRequest notificationRequest = notificationRequestMapper.createNotificationRequestForAppSolicitor(callbackRequest);

        assertEquals("12345", notificationRequest.getCaseReferenceNumber());
        assertEquals(TEST_SOLICITOR_REFERENCE, notificationRequest.getSolicitorReferenceNumber());
        assertEquals(TEST_DIVORCE_CASE_NUMBER, notificationRequest.getDivorceCaseNumber());
        assertEquals(TEST_SOLICITOR_NAME, notificationRequest.getName());
        assertEquals(TEST_SOLICITOR_EMAIL, notificationRequest.getNotificationEmail());
        assertEquals("contested", notificationRequest.getCaseType());
        assertEquals("Nottingham County Court and Family Court", notificationRequest.getSelectedCourt());
    }

    @Test
    public void shouldCreateNotificationRequestForRespSolicitorForConsentedJourney() {
        CallbackRequest callbackRequest = getConsentedCallbackRequest();
        NotificationRequest notificationRequest = notificationRequestMapper.createNotificationRequestForRespSolicitor(callbackRequest);

        assertEquals("12345", notificationRequest.getCaseReferenceNumber());
        assertEquals(TEST_RESP_SOLICITOR_REFERENCE, notificationRequest.getSolicitorReferenceNumber());
        assertEquals(TEST_DIVORCE_CASE_NUMBER, notificationRequest.getDivorceCaseNumber());
        assertEquals(TEST_RESP_SOLICITOR_NAME, notificationRequest.getName());
        assertEquals(TEST_RESP_SOLICITOR_EMAIL, notificationRequest.getNotificationEmail());
        assertEquals("consented", notificationRequest.getCaseType());
    }

    @Test
    public void shouldCreateNotificationRequestForRespSolicitorForContestedJourney() {
        CallbackRequest callbackRequest = getContestedCallbackRequest();

        NotificationRequest notificationRequest = notificationRequestMapper.createNotificationRequestForRespSolicitor(callbackRequest);

        assertEquals("12345", notificationRequest.getCaseReferenceNumber());
        assertEquals(TEST_RESP_SOLICITOR_REFERENCE, notificationRequest.getSolicitorReferenceNumber());
        assertEquals(TEST_DIVORCE_CASE_NUMBER, notificationRequest.getDivorceCaseNumber());
        assertEquals(TEST_RESP_SOLICITOR_NAME, notificationRequest.getName());
        assertEquals(TEST_RESP_SOLICITOR_EMAIL, notificationRequest.getNotificationEmail());
        assertEquals("contested", notificationRequest.getCaseType());
        assertEquals("Nottingham County Court and Family Court", notificationRequest.getSelectedCourt());
    }
}