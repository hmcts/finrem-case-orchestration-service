package uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd;

import com.fasterxml.jackson.annotation.JsonValue;

public interface INatureOfApplication {
    @JsonValue
    String getValue();

}
