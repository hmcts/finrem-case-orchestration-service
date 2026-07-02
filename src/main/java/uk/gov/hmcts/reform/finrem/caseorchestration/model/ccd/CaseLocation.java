package uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd;

import lombok.Builder;
import lombok.Data;

@Data
@Builder(toBuilder = true)
public class CaseLocation {
    private final String region;
    private final String regionId;
    private final String baseLocation;
    private final String baseLocationId;
    private final String regionName;
    private final String baseLocationName;
}
