package uk.gov.hmcts.reform.finrem.caseorchestration.model.document;

import org.junit.Test;

import static org.junit.Assert.assertEquals;

public class CtscContactDetailsTest {

    @Test
    public void checkAllStatusValues() {

        CtscContactDetails ctscContactDetails = CtscContactDetails.builder()
            .serviceCentre("Courts and Tribunals Service Centre")
            .careOf("c/o HMCTS Digital Financial Remedy")
            .poBox("12746")
            .town("HARLOW")
            .postcode("CM20 9QZ")
            .emailAddress("HMCTSFinancialRemedy@justice.gov.uk")
            .phoneNumber("0300 303 0642")
            .openingHours("from 8.30am to 5pm")
            .build();

        assertEquals("Courts and Tribunals Service Centre", ctscContactDetails.getServiceCentre());
        assertEquals("c/o HMCTS Digital Financial Remedy", ctscContactDetails.getCareOf());
        assertEquals("12746", ctscContactDetails.getPoBox());
        assertEquals("HARLOW", ctscContactDetails.getTown());
        assertEquals("CM20 9QZ", ctscContactDetails.getPostcode());
        assertEquals("HMCTSFinancialRemedy@justice.gov.uk", ctscContactDetails.getEmailAddress());
        assertEquals("0300 303 0642", ctscContactDetails.getPhoneNumber());
        assertEquals("from 8.30am to 5pm", ctscContactDetails.getOpeningHours());
    }
}