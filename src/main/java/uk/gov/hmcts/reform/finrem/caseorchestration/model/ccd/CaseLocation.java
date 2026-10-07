package uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd;

import lombok.Builder;
import lombok.Value;

@Value
@Builder(toBuilder = true)
public class CaseLocation {
    String region;
    String baseLocation;
}
