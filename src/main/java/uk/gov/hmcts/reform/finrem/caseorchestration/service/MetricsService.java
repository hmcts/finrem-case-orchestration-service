package uk.gov.hmcts.reform.finrem.caseorchestration.service;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import uk.gov.hmcts.reform.finrem.caseorchestration.config.CourtDetails;
import uk.gov.hmcts.reform.finrem.caseorchestration.config.CourtDetailsConfiguration;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.FinremCaseData;

import java.util.Optional;

import static java.util.Optional.ofNullable;

@Service
@RequiredArgsConstructor
public class MetricsService {

    private final CourtDetailsConfiguration courtDetailsConfiguration;

    /**
    * Sets PowerBI fields used for tracking and gather statistics.
    * Though the fields are prefixed Consent_Order_FRC, these tracking fields are used for both consented and
    * contested applications.
    *
    * @param caseData instance of FinremCaseData
    */
    public void  setCourtMetrics(FinremCaseData caseData) {

        String selectedCourtId = caseData.getSelectedAllocatedCourt();

        Optional<CourtDetails> selectedCourtDetails = ofNullable(courtDetailsConfiguration.getCourts().get(selectedCourtId));

        String getEmailForSelectedCourt = selectedCourtDetails.map(CourtDetails::getEmail).orElse("");
        String getNameForSelectedCourt = selectedCourtDetails.map(CourtDetails::getCourtName).orElse("");
        String getAddressForSelectedCourt = selectedCourtDetails.map(CourtDetails::getCourtAddress).orElse("");
        String getPhoneForSelectedCourt = selectedCourtDetails.map(CourtDetails::getPhoneNumber).orElse("");

        caseData.getConsentOrderWrapper().setConsentOrderFrcName(getNameForSelectedCourt);
        caseData.getConsentOrderWrapper().setConsentOrderFrcAddress(getAddressForSelectedCourt);
        caseData.getConsentOrderWrapper().setConsentOrderFrcEmail(getEmailForSelectedCourt);
        caseData.getConsentOrderWrapper().setConsentOrderFrcPhone(getPhoneForSelectedCourt);
    }
}
