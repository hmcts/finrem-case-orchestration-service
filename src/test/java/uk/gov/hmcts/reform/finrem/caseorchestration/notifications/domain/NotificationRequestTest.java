package uk.gov.hmcts.reform.finrem.caseorchestration.notifications.domain;

import org.junit.Test;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.notification.NotificationRequest;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNull;
import static uk.gov.hmcts.reform.finrem.caseorchestration.notifications.NotificationConstants.PHONE_OPENING_HOURS;
import static uk.gov.hmcts.reform.finrem.caseorchestration.notifications.TestConstants.CONTESTED;

public class NotificationRequestTest {

    private NotificationRequest underTest;

    @Test
    public void shouldGetHwfNotificationRequestData() {
        underTest = new NotificationRequest("123456",
            "45623", "D123", "Padmaja", "test@test.com",
            "nottingham", CONTESTED, "body", PHONE_OPENING_HOURS, "consent",
            "Consent",
            "rejectedReason", "app", "res",
            "1234567890", "", "", "",
            "", null, null, "1999-08-06");
        assertEquals("123456", underTest.getCaseReferenceNumber());
        assertEquals("45623", underTest.getSolicitorReferenceNumber());
        assertEquals("D123", underTest.getDivorceCaseNumber());
        assertEquals("Padmaja", underTest.getName());
        assertEquals("test@test.com", underTest.getNotificationEmail());
        assertEquals("nottingham", underTest.getSelectedCourt());
        assertEquals("body", underTest.getGeneralEmailBody());
        assertEquals("app", underTest.getApplicantName());
        assertEquals("res", underTest.getRespondentName());
        assertEquals(PHONE_OPENING_HOURS, underTest.getPhoneOpeningHours());
        assertEquals("rejectedReason", underTest.getGeneralApplicationRejectionReason());
        assertEquals("1234567890", underTest.getBarristerReferenceNumber());
        assertEquals("", underTest.getHearingType());
        assertEquals("1999-08-06", underTest.getHearingDate());
    }

    @Test
    public void shouldNotGetHwfNotificationRequestData() {
        underTest = new NotificationRequest();
        assertNull(underTest.getCaseReferenceNumber());
        assertNull(underTest.getSolicitorReferenceNumber());
        assertNull(underTest.getDivorceCaseNumber());
        assertNull(underTest.getName());
        assertNull(underTest.getNotificationEmail());
        assertNull(underTest.getSelectedCourt());
        assertNull(underTest.getGeneralEmailBody());
        assertNull(underTest.getPhoneOpeningHours());
    }

    @Test
    public void shouldSetAndGetHwfNotificationRequestData() {
        underTest = new NotificationRequest();
        setNotificationData();
        assertNotificationData();
    }

    private void assertNotificationData() {
        assertEquals("case1234", underTest.getCaseReferenceNumber());
        assertEquals("123123", underTest.getSolicitorReferenceNumber());
        assertEquals("D456", underTest.getDivorceCaseNumber());
        assertEquals("Prashanth", underTest.getName());
        assertEquals("test1@test1.com", underTest.getNotificationEmail());
        assertEquals(PHONE_OPENING_HOURS, underTest.getPhoneOpeningHours());
    }

    @Test
    public void shouldSetAndGetHwfNotificationRequestDataForContested() {
        underTest = new NotificationRequest();
        setNotificationData();
        underTest.setSelectedCourt("nottingham");
        assertNotificationData();
        assertEquals("nottingham", underTest.getSelectedCourt());
    }

    private void setNotificationData() {
        underTest.setName("Prashanth");
        underTest.setSolicitorReferenceNumber("123123");
        underTest.setDivorceCaseNumber("D456");
        underTest.setCaseReferenceNumber("case1234");
        underTest.setNotificationEmail("test1@test1.com");
        underTest.setPhoneOpeningHours(PHONE_OPENING_HOURS);
    }
}
