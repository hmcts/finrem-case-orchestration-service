package uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Data;

@JsonIgnoreProperties(ignoreUnknown = true)
@Data
@JsonInclude(JsonInclude.Include.NON_NULL)
public class TransferLocalCourtEmailData {
    @JsonProperty("transferLocalCourtName")
    private String courtName;
    @JsonProperty("transferLocalCourtEmail")
    private String courtEmail;
    @JsonProperty("transferLocalCourtInstructions")
    private String courtInstructions;
}
