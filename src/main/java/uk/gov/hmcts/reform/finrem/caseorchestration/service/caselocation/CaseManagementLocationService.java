package uk.gov.hmcts.reform.finrem.caseorchestration.service.caselocation;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.springframework.stereotype.Service;
import uk.gov.hmcts.reform.finrem.caseorchestration.config.CourtDetailsConfiguration;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.CourtRefData;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.CaseLocation;

import java.io.IOException;
import java.io.InputStream;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.function.Function;
import java.util.stream.Collectors;

/**
 * Service responsible for resolving CCD case location details
 * (base location and region) from the selected Financial Remedies Court (FRC).
 *
 * <p>The service loads court reference data from the
 * {@code /json/court-ref-data.json} configuration file during application startup
 * and creates an in-memory lookup for efficient court name searches.</p>
 *
 * <p>When a case contains a {@code consentOrderFRCName} value, the service
 * attempts to find the corresponding court reference data and returns a
 * {@link uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.CaseLocation}
 * populated with the court's Epimms base location and region identifier.</p>
 *
 * <p>If the court name is blank or no matching court is found,
 * {@code null} is returned.</p>
 */

@Service
@Slf4j
public class CaseManagementLocationService {

    private static final String CONSENT_ORDER_FRC_NAME = "consentOrderFRCName";
    private static final String COURT_REF_DATA_FILE = "/json/court-ref-data.json";

    private final Map<String, CourtRefData> courtsByName;

    public CaseManagementLocationService(ObjectMapper objectMapper) throws IOException {
        try (InputStream inputStream =
                 CourtDetailsConfiguration.class.getResourceAsStream(COURT_REF_DATA_FILE)) {

            if (inputStream == null) {
                throw new IOException("Unable to load court reference data from " + COURT_REF_DATA_FILE);
            }
            List<CourtRefData> courtsRefDataList = objectMapper.readValue(
                inputStream,
                new TypeReference<List<CourtRefData>>() {
                }
            );
            this.courtsByName = courtsRefDataList.stream()
                .collect(Collectors.toMap(
                    court -> court.getCourtName().toLowerCase(),
                    Function.identity()
                ));
        }
    }

    public CaseLocation getCaseLocation(Map<String, Object> caseData) {
        String courtName = (String) caseData.get(CONSENT_ORDER_FRC_NAME);
        if (StringUtils.isBlank(courtName)) {
            return null;
        }
        CourtRefData courtRefData = courtsByName.get(courtName.toLowerCase(Locale.UK));
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
