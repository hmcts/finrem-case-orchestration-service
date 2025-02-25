package uk.gov.hmcts.reform.finrem.caseorchestration.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import uk.gov.hmcts.reform.finrem.caseorchestration.config.CourtDetails;
import uk.gov.hmcts.reform.finrem.caseorchestration.config.CourtDetailsConfiguration;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.FinremCaseData;

import java.util.Optional;

import static java.util.Optional.ofNullable;

@Service
@Slf4j
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
    public void  setCourtMetrics (FinremCaseData caseData) {

        String selectedAllocatedCourt = caseData.getSelectedAllocatedCourt();

        Optional<CourtDetails> courtDetails = ofNullable(courtDetailsConfiguration.getCourts().get(selectedAllocatedCourt));

        String getEmailForSelectedCourt = courtDetails.map(CourtDetails::getEmail).orElse("");
        String getNameForSelectedCourt = courtDetails.map(CourtDetails::getCourtName).orElse("");
        String getAddressForSelectedCourt = courtDetails.map(CourtDetails::getCourtAddress).orElse("");
        String getPhoneForSelectedCourt = courtDetails.map(CourtDetails::getPhoneNumber).orElse("");

        caseData.getConsentOrderWrapper().setConsentOrderFrcName(getNameForSelectedCourt);
        caseData.getConsentOrderWrapper().setConsentOrderFrcAddress(getAddressForSelectedCourt);
        caseData.getConsentOrderWrapper().setConsentOrderFrcEmail(getEmailForSelectedCourt);
        caseData.getConsentOrderWrapper().setConsentOrderFrcPhone(getPhoneForSelectedCourt);
    }
}
