package uk.gov.hmcts.reform.finrem.caseorchestration.model.notification;

import org.junit.Test;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNull;
import static uk.gov.hmcts.reform.finrem.caseorchestration.OrchestrationConstants.PHONE_OPENING_HOURS;

public class NotificationRequestTest {
    private NotificationRequest underTest;

    @Test
    public void shouldReturnNotificationRequestData() {
        underTest = new NotificationRequest("12345", "67890", "D123",
            "Padmaja", "test@test.com", "nottingham", "consented", "general body",
            PHONE_OPENING_HOURS);
        assertEquals("12345", underTest.getCaseReferenceNumber());
        assertEquals("67890", underTest.getSolicitorReferenceNumber());
        assertEquals("D123", underTest.getDivorceCaseNumber());
        assertEquals("Padmaja", underTest.getName());
        assertEquals("test@test.com", underTest.getNotificationEmail());
        assertEquals("nottingham", underTest.getSelectedCourt());
        assertEquals("consented", underTest.getCaseType());
        assertEquals("general body", underTest.getGeneralEmailBody());
        assertEquals(PHONE_OPENING_HOURS, underTest.getPhoneOpeningHours());
    }

    @Test
    public void shouldNotReturnNotificationRequestData() {
        underTest = new NotificationRequest();
        assertNull(underTest.getCaseReferenceNumber());
        assertNull(underTest.getSolicitorReferenceNumber());
        assertNull(underTest.getDivorceCaseNumber());
        assertNull(underTest.getName());
        assertNull(underTest.getNotificationEmail());
        assertNull(underTest.getCaseType());
        assertNull(underTest.getGeneralEmailBody());
        assertNull(underTest.getPhoneOpeningHours());
    }

    @Test
    public void shouldSetAndGetNotificationRequestData() {
        underTest = new NotificationRequest();
        underTest.setName("Prashanth");
        underTest.setNotificationEmail("test1@test1.com");
        underTest.setSolicitorReferenceNumber("67891");
        underTest.setDivorceCaseNumber("D456");
        underTest.setCaseReferenceNumber("54321");
        underTest.setSelectedCourt("nottingham");
        underTest.setCaseType("consented");
        underTest.setGeneralEmailBody("general body");
        underTest.setPhoneOpeningHours(PHONE_OPENING_HOURS);
        assertEquals("54321", underTest.getCaseReferenceNumber());
        assertEquals("67891", underTest.getSolicitorReferenceNumber());
        assertEquals("D456", underTest.getDivorceCaseNumber());
        assertEquals("Prashanth", underTest.getName());
        assertEquals("test1@test1.com", underTest.getNotificationEmail());
        assertEquals("nottingham", underTest.getSelectedCourt());
        assertEquals("consented", underTest.getCaseType());
        assertEquals("general body", underTest.getGeneralEmailBody());
        assertEquals(PHONE_OPENING_HOURS, PHONE_OPENING_HOURS);
    }
}