package uk.gov.hmcts.reform.finrem.caseorchestration.service.caselocation;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.CourtRefData;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.CaseLocation;

import java.util.Locale;
import java.util.Map;

/**
 * Service responsible for determining CCD case location details
 * (base location and region) from the Financial Remedies Court
 * selected on a case.
 */
@Service
@Slf4j
@RequiredArgsConstructor
public class CaseManagementLocationService {

    private static final String COURT_REF_DATA_FILE = "/json/fr-court-to-refdata-location-mappings.json";
    private static final String COURT_LIST_SUFFIX = "CourtList";

    private final Map<String, CourtRefData> courtReferenceDataByName;

    /**
     * Resolves the CCD case location from the selected court.
     *
     * @param caseData CCD case data
     * @return CaseLocation containing base location and region,
     *         or null if no matching court is found
     */
    public CaseLocation getCaseLocation(Map<String, Object> caseData) {

        Map.Entry<String, Object> courtEntry = caseData.entrySet()
            .stream()
            .filter(entry -> entry.getKey().endsWith(COURT_LIST_SUFFIX))
            .findFirst()
            .orElse(null);

        if (courtEntry == null || courtEntry.getValue() == null) {
            log.warn("No court list field found in case data");
            return null;
        }

        String courtName = courtEntry.getValue()
            .toString()
            .trim()
            .toLowerCase(Locale.UK);

        CourtRefData courtRefData = courtReferenceDataByName.get(courtName);

        if (courtRefData == null) {
            log.warn("No court reference data found for court name: {}", courtName);
            return null;
        }

        return CaseLocation.builder().baseLocation(courtRefData.getEpimmsId()).region(courtRefData.getRegionId()).build();
    }

}
