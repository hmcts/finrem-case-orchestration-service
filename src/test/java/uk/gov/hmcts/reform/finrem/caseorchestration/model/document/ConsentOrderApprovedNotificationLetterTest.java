package uk.gov.hmcts.reform.finrem.caseorchestration.model.document;

import org.junit.Test;

import static org.junit.Assert.assertEquals;

public class ConsentOrderApprovedNotificationLetterTest {

    @Test
    public void checkAllStatusValues() {

        Addressee addressee = Addressee.builder()
                .name("appRespondentFMName")
                .formattedAddress("1 Victoria Street" + "\n" + "Westminster" + "\n" + "" + "\n" + "Greater London"
                        + "\n" + "UK" + "\n" + "London" + "\n" + "SE1")
                .build();

        ConsentOrderApprovedNotificationLetter consentOrderApprovedNotificationLetter =
            ConsentOrderApprovedNotificationLetter.builder()
                .caseNumber("ccdNumber")
                .addressee(addressee)
                .reference("recipientRef")
                .applicantName("applicantName")
                .respondentName("respondentName")
                .letterDate("letterCreatedDate")
                .build();

        assertEquals("ccdNumber", consentOrderApprovedNotificationLetter.getCaseNumber());
        assertEquals("appRespondentFMName", consentOrderApprovedNotificationLetter.getAddressee().getName());
        assertEquals("recipientRef", consentOrderApprovedNotificationLetter.getReference());
        assertEquals("applicantName", consentOrderApprovedNotificationLetter.getApplicantName());
        assertEquals("respondentName", consentOrderApprovedNotificationLetter.getRespondentName());
        assertEquals("letterCreatedDate", consentOrderApprovedNotificationLetter.getLetterDate());
        assertEquals("1 Victoria Street" + "\n" + "Westminster" + "\n" + "" + "\n"
                + "Greater London" + "\n" + "UK" + "\n" + "London" + "\n"
                + "SE1", consentOrderApprovedNotificationLetter.getAddressee().getFormattedAddress());
    }
}