package uk.gov.hmcts.reform.finrem.caseorchestration.model.document;

import org.junit.Test;

import static org.junit.Assert.assertEquals;

public class ConsentOrderApprovedNotificationLetterTest {

    @Test
    public void checkAllStatusValues() {
        ConsentOrderApprovedNotificationLetter consentOrderApprovedNotificationLetter =
            ConsentOrderApprovedNotificationLetter.builder()
                .ccdNumber("ccdNumber")
                .recipientName("appRespondentFMName")
                .recipientRef("recipientRef")
                .applicantName("applicantName")
                .respondentName("respondentName")
                .letterCreatedDate("letterCreatedDate")
                .addressLine1("AddressLine1")
                .addressLine2("AddressLine2")
                .addressLine3("AddressLine3")
                .county("County")
                .country("Country")
                .postTown("PostTown")
                .postCode("PostCode")
                .build();
        assertEquals("ccdNumber", consentOrderApprovedNotificationLetter.getCcdNumber());
        assertEquals("appRespondentFMName", consentOrderApprovedNotificationLetter.getRecipientName());
        assertEquals("recipientRef", consentOrderApprovedNotificationLetter.getRecipientRef());
        assertEquals("applicantName", consentOrderApprovedNotificationLetter.getApplicantName());
        assertEquals("respondentName", consentOrderApprovedNotificationLetter.getRespondentName());
        assertEquals("letterCreatedDate", consentOrderApprovedNotificationLetter.getLetterCreatedDate());
        assertEquals("AddressLine1", consentOrderApprovedNotificationLetter.getAddressLine1());
        assertEquals("AddressLine2", consentOrderApprovedNotificationLetter.getAddressLine2());
        assertEquals("AddressLine3", consentOrderApprovedNotificationLetter.getAddressLine3());
        assertEquals("County", consentOrderApprovedNotificationLetter.getCounty());
        assertEquals("Country", consentOrderApprovedNotificationLetter.getCountry());
        assertEquals("PostTown", consentOrderApprovedNotificationLetter.getPostTown());
        assertEquals("PostCode", consentOrderApprovedNotificationLetter.getPostCode());
    }
}