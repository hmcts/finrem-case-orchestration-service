package uk.gov.hmcts.reform.finrem.caseorchestration.model.ff4j;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import lombok.Data;

@Data
@JsonIgnoreProperties(ignoreUnknown = true)
public class FeatureToggle {

    private String uid;
    private String enable;
    private String description;
}
