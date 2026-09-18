package uk.gov.hmcts.reform.finrem.caseorchestration.service.caselocation;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import uk.gov.hmcts.reform.finrem.caseorchestration.config.CourtDetailsConfiguration;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.CourtRefData;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.CaseLocation;

import java.io.IOException;
import java.io.InputStream;
import java.util.Locale;
import java.util.Map;
import java.util.stream.Collectors;

/**
 * Service responsible for determining CCD case location details
 * (base location and region) from the Financial Remedies Court
 * selected on a case.
 */
@Service
@Slf4j
public class CaseManagementLocationService {

    private static final String COURT_REF_DATA_FILE = "/json/court-ref-data.json";
    private static final String COURT_LIST_SUFFIX = "CourtList";

    private final Map<String, CourtRefData> courtsByName;

    public CaseManagementLocationService(ObjectMapper objectMapper) throws IOException {
        try (InputStream inputStream =
                 CourtDetailsConfiguration.class.getResourceAsStream(COURT_REF_DATA_FILE)) {

            if (inputStream == null) {
                throw new IOException(
                    "Unable to load court reference data from " + COURT_REF_DATA_FILE
                );
            }

            this.courtsByName = objectMapper.readValue(
                    inputStream,
                    new TypeReference<Map<String, CourtRefData>>() {
                    })
                .entrySet()
                .stream()
                .collect(Collectors.toMap(
                    entry -> entry.getKey().toLowerCase(Locale.UK),
                    Map.Entry::getValue
                ));
        }
    }

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

        CourtRefData courtRefData = courtsByName.get(courtName);

        if (courtRefData == null) {
            log.warn("No court reference data found for court name: {}", courtName);
            return null;
        }

        return buildCaseLocation(courtRefData);
    }

    private CaseLocation buildCaseLocation(CourtRefData courtRefData) {
        return CaseLocation.builder()
            .baseLocation(courtRefData.getEpimmsId())
            .region(courtRefData.getRegionId())
            .build();
    }
}
