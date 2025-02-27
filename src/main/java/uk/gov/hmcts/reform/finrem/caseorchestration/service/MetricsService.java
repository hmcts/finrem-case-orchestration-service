package uk.gov.hmcts.reform.finrem.caseorchestration.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import uk.gov.hmcts.reform.finrem.caseorchestration.config.CourtDetails;
import uk.gov.hmcts.reform.finrem.caseorchestration.config.CourtDetailsConfiguration;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.FinremCaseData;

import java.util.Optional;
import java.util.function.Consumer;

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
    public void setCourtMetrics(FinremCaseData caseData) {

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
