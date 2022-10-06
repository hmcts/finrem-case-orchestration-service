package uk.gov.hmcts.reform.finrem.caseorchestration.model.caseflag;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Data;

import java.util.List;

@JsonIgnoreProperties(ignoreUnknown = true)
@Data
public class FlagDetailsData {

    @JsonProperty("id")
    private String id;
    @JsonProperty("value")
    private List<FlagDetails> details;

    @JsonProperty("partyName")
    private String partyName;
    @JsonProperty("roleOnCase")
    private String roleOnCase;
}

