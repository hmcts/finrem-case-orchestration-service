package uk.gov.hmcts.reform.finrem.caseorchestration.model.caseflag;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Data;

import java.util.List;

@JsonIgnoreProperties(ignoreUnknown = true)
@Data
public class FlagDetailsData {
    @JsonProperty("partyName")
    private String partyName;
    @JsonProperty("roleOnCase")
    private String roleOnCase;
    @JsonProperty("details")
    private List<FlagDetails> details;

}

