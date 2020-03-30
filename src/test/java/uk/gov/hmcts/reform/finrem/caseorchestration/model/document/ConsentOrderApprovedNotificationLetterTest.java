package uk.gov.hmcts.reform.finrem.caseorchestration.model.document;

import org.junit.Test;

import static org.junit.Assert.assertEquals;

public class ConsentOrderApprovedNotificationLetterTest {

    @Test
    public void checkAllStatusValues() {
        ConsentOrderApprovedNotificationLetter consentOrderApprovedNotificationLetter =
            ConsentOrderApprovedNotificationLetter.builder()
                .caseNumber("ccdNumber")
                .addressee("appRespondentFMName")
                .reference("recipientRef")
                .applicantName("applicantName")
                .respondentName("respondentName")
                .letterDate("letterCreatedDate")
                .formattedAddress("1 Victoria Street" + "\n" + "Westminster" + "\n" + "" + "\n" + "Greater London"
                        + "\n" + "UK" + "\n" + "London" + "\n" + "SE1")
                .build();

        assertEquals("ccdNumber", consentOrderApprovedNotificationLetter.getCaseNumber());
        assertEquals("appRespondentFMName", consentOrderApprovedNotificationLetter.getAddressee());
        assertEquals("recipientRef", consentOrderApprovedNotificationLetter.getReference());
        assertEquals("applicantName", consentOrderApprovedNotificationLetter.getApplicantName());
        assertEquals("respondentName", consentOrderApprovedNotificationLetter.getRespondentName());
        assertEquals("letterCreatedDate", consentOrderApprovedNotificationLetter.getLetterDate());
        assertEquals("1 Victoria Street" + "\n" + "Westminster" + "\n" + "" + "\n"
                + "Greater London" + "\n" + "UK" + "\n" + "London" + "\n"
                + "SE1", consentOrderApprovedNotificationLetter.getFormattedAddress());
    }
}