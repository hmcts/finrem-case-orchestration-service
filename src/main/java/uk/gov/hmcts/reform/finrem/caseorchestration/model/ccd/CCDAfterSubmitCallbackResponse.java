package uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Builder;
import lombok.Data;

@Data
@Builder
@JsonIgnoreProperties(ignoreUnknown = true)
public class CCDAfterSubmitCallbackResponse {

    @JsonProperty(value = "confirmation_header")
    private final String confirmationHeader;

    @JsonProperty(value = "confirmation_body")
    private final String confirmationBody;
}

