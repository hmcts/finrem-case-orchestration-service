package uk.gov.hmcts.reform.finrem.caseorchestration.service.caselocation;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.CourtRefData;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.CaseLocation;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.FinremCaseData;

import java.util.Locale;
import java.util.Map;
import java.util.Optional;

/**
 * Service responsible for determining CCD case location details
 * (base location and region) from the Financial Remedies Court
 * selected on a case.
 */
@Service
@Slf4j
@RequiredArgsConstructor
public class CaseManagementLocationService {

    private final Map<String, CourtRefData> courtReferenceDataByName;

    /**
     * Resolves the CCD case location from the selected court.
     *
     * @param finremCaseData CCD case data
     * @return CaseLocation containing base location and region,
     *         or null if no matching court is found
     */
    public CaseLocation getCaseLocation(FinremCaseData finremCaseData) {
        log.info("Getting case location for court reference data: {}", finremCaseData);

        String selectedAllocatedCourt = Optional.ofNullable(finremCaseData)
            .map(FinremCaseData::getSelectedAllocatedCourt)
            .map(String::trim)
            .filter(value -> !value.isEmpty())
            .orElse(null);

        if (selectedAllocatedCourt == null) {
            log.warn("No court set case id: {}", finremCaseData != null ? finremCaseData.getCcdCaseId() : null);
            return null;
        }

        String courtKey = selectedAllocatedCourt.toLowerCase(Locale.ROOT);
        CourtRefData courtRefData = courtReferenceDataByName.get(courtKey);

        if (courtRefData == null) {
            log.warn("No court reference data found for case id: {}, court name: {}",
                finremCaseData.getCcdCaseId(), selectedAllocatedCourt);
            return null;
        }

        log.info("Found court reference data for case id: {}, court name: {}: {}",
            finremCaseData.getCcdCaseId(), finremCaseData.getSelectedHearingCourt(), courtRefData);
        return CaseLocation.builder().baseLocation(courtRefData.getEpimmsId()).region(courtRefData.getRegionId()).build();
    }
}
