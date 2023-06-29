package uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.caseflag.caseflag;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@JsonIgnoreProperties(ignoreUnknown = true)
@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class FlagDetailData {
    @JsonProperty("id")
    private String id;
    @JsonProperty("value")
    private FlagDetail value;
}
