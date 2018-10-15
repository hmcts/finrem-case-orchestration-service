package uk.gov.hmcts.reform.finrem.finremcaseprogression.model.ccd;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Data;

@JsonIgnoreProperties(ignoreUnknown = true)
@Data
public class CaseDetails {
    @JsonProperty("jurisdiction")
    private String jurisdiction;
    @JsonProperty("state")
    private String state;
    @JsonProperty("case_data")
    private CaseData caseData;
}
