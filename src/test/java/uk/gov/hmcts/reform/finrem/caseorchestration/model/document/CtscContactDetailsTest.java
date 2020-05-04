package uk.gov.hmcts.reform.finrem.caseorchestration.model.document;

import org.junit.Test;

import static org.junit.Assert.assertEquals;
import static uk.gov.hmcts.reform.finrem.caseorchestration.OrchestrationConstants.CTSC_CARE_OF;
import static uk.gov.hmcts.reform.finrem.caseorchestration.OrchestrationConstants.CTSC_EMAIL_ADDRESS;
import static uk.gov.hmcts.reform.finrem.caseorchestration.OrchestrationConstants.CTSC_OPENING_HOURS;
import static uk.gov.hmcts.reform.finrem.caseorchestration.OrchestrationConstants.CTSC_PHONE_NUMBER;
import static uk.gov.hmcts.reform.finrem.caseorchestration.OrchestrationConstants.CTSC_POSTCODE;
import static uk.gov.hmcts.reform.finrem.caseorchestration.OrchestrationConstants.CTSC_PO_BOX;
import static uk.gov.hmcts.reform.finrem.caseorchestration.OrchestrationConstants.CTSC_SERVICE_CENTRE;
import static uk.gov.hmcts.reform.finrem.caseorchestration.OrchestrationConstants.CTSC_TOWN;

public class CtscContactDetailsTest {

    @Test
    public void checkAllStatusValues() {

        CtscContactDetails ctscContactDetails = CtscContactDetails.builder()
            .serviceCentre(CTSC_SERVICE_CENTRE)
            .careOf(CTSC_CARE_OF)
            .poBox(CTSC_PO_BOX)
            .town(CTSC_TOWN)
            .postcode(CTSC_POSTCODE)
            .emailAddress(CTSC_EMAIL_ADDRESS)
            .phoneNumber(CTSC_PHONE_NUMBER)
            .openingHours(CTSC_OPENING_HOURS)
            .build();

        assertEquals(CTSC_SERVICE_CENTRE, ctscContactDetails.getServiceCentre());
        assertEquals(CTSC_CARE_OF, ctscContactDetails.getCareOf());
        assertEquals(CTSC_PO_BOX, ctscContactDetails.getPoBox());
        assertEquals(CTSC_TOWN, ctscContactDetails.getTown());
        assertEquals(CTSC_POSTCODE, ctscContactDetails.getPostcode());
        assertEquals(CTSC_EMAIL_ADDRESS, ctscContactDetails.getEmailAddress());
        assertEquals(CTSC_PHONE_NUMBER, ctscContactDetails.getPhoneNumber());
        assertEquals(CTSC_OPENING_HOURS, ctscContactDetails.getOpeningHours());
    }
}