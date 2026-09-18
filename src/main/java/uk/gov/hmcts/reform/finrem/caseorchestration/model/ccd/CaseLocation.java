package uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd;

import lombok.Builder;
import lombok.Data;

@Data
@Builder(toBuilder = true)
public class CaseLocation {
    private final String region;
    private final String baseLocation;
}
