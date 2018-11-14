package uk.gov.hmcts.reform.finrem.caseorchestration.model.notification;

import org.junit.Test;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNull;

public class NotificationRequestTest {
    private NotificationRequest underTest;

    @Test
    public void shouldReturnNotificationRequestData() {
        underTest = new NotificationRequest("12345", "67890",
                "Padmaja", "test@test.com");
        assertEquals("12345", underTest.getCaseReferenceNumber());
        assertEquals("67890", underTest.getSolicitorReferenceNumber());
        assertEquals("Padmaja", underTest.getName());
        assertEquals("test@test.com", underTest.getNotificationEmail());
    }

    @Test
    public void shouldNotReturnNotificationRequestData() {
        underTest = new NotificationRequest();
        assertNull(underTest.getCaseReferenceNumber());
        assertNull(underTest.getSolicitorReferenceNumber());
        assertNull(underTest.getName());
        assertNull(underTest.getNotificationEmail());
    }

    @Test
    public void shouldSetAndGetNotificationRequestData() {
        underTest = new NotificationRequest();
        underTest.setName("Prashanth");
        underTest.setNotificationEmail("test1@test1.com");
        underTest.setSolicitorReferenceNumber("67891");
        underTest.setCaseReferenceNumber("54321");
        assertEquals("54321", underTest.getCaseReferenceNumber());
        assertEquals("67891", underTest.getSolicitorReferenceNumber());
        assertEquals("Prashanth", underTest.getName());
        assertEquals("test1@test1.com", underTest.getNotificationEmail());
    }
}