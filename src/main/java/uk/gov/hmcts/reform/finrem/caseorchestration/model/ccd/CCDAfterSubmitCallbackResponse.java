package uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

@Data
@Builder
@JsonIgnoreProperties(ignoreUnknown = true)
public class CCDAfterSubmitCallbackResponse {

    @JsonProperty(value = "confirmation_header")
    private final String confirmationHeader;

    @JsonProperty(value = "confirmation_body")
    private final String confirmationBody;
}

