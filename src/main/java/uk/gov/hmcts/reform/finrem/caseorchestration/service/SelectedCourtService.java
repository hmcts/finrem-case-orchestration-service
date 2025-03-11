package uk.gov.hmcts.reform.finrem.caseorchestration.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import uk.gov.hmcts.reform.finrem.caseorchestration.config.CourtDetails;
import uk.gov.hmcts.reform.finrem.caseorchestration.config.CourtDetailsConfiguration;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.FinremCaseData;

import java.util.Optional;
import java.util.Set;
import java.util.function.Consumer;

import static java.util.Optional.ofNullable;

@Service
@Slf4j
@RequiredArgsConstructor
public class SelectedCourtService {

    private static final String ROYAL_COURT = "THE ROYAL COURTS OF JUSTICE";
    private static final String HIGH_COURT = "HIGH COURT FAMILY DIVISION";

    private final CourtDetailsConfiguration courtDetailsConfiguration;

    /**
     * Sets fields used for Power BI Metrics and showing the User Court information.
     * Though the fields are prefixed Consent_Order_FRC, these fields are used for both consented and
     * contested applications.
     * Checks that a region has been selected before calling getSelectedAllocatedCourt, as an indication
     * that the User has selected their court.  Without that, mapping in getSelectedAllocatedCourt fails.
     * Logs a warning if any information missing for the selected court.
     *
     * @param caseData instance of FinremCaseData
    */
    public void setSelectedCourtDetailsIfPresent(FinremCaseData caseData) {

        if (caseData.getRegionWrapper().getAllocatedRegionWrapper().getRegionList() == null) {
            return;
        }

        String selectedCourtId = caseData.getSelectedAllocatedCourt();

        Optional<CourtDetails> selectedCourtDetails =
                ofNullable(courtDetailsConfiguration.getCourts().get(selectedCourtId));

        setIfPresent(caseData.getConsentOrderWrapper()::setConsentOrderFrcName,
                selectedCourtDetails.map(CourtDetails::getCourtName),
                "Court Name",
                selectedCourtId);

        setIfPresent(caseData.getConsentOrderWrapper()::setConsentOrderFrcAddress,
                selectedCourtDetails.map(CourtDetails::getCourtAddress),
                "Court Address",
                selectedCourtId);

        setIfPresent(caseData.getConsentOrderWrapper()::setConsentOrderFrcEmail,
                selectedCourtDetails.map(CourtDetails::getEmail),
                "Court Email",
                selectedCourtId);

        setIfPresent(caseData.getConsentOrderWrapper()::setConsentOrderFrcPhone,
                selectedCourtDetails.map(CourtDetails::getPhoneNumber),
                "Court Phone",
                selectedCourtId);
    }

    /**
     * Contested Form A creation does not permit these courts to chosen.
     * This simple check returns true if caseData shows that a User has picked either of
     * these courts.
     *
     * @param caseData instance of FinremCaseData
     */
    public boolean royalCourtOrHighCourtChosen(FinremCaseData caseData) {
        String courtName = caseData.getConsentOrderWrapper().getConsentOrderFrcName();
        return courtName != null && Set.of(HIGH_COURT, ROYAL_COURT).contains(courtName.toUpperCase());
    }

    private <T> void setIfPresent(Consumer<T> setter, Optional<T> value, String fieldName, String selectedCourtId) {
        if (value.isPresent()) {
            setter.accept(value.get());
        } else {
            log.warn("Warning: {} is missing a value for {} so the consentOrderFRC value will not be set.",
                    selectedCourtId,
                    fieldName);
        }
    }
}
