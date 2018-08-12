package uk.gov.hmcts.reform.finrem.finremcaseprogression.ccd;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import uk.gov.hmcts.reform.finrem.finremcaseprogression.model.CaseDetails;

@Data
@AllArgsConstructor
@NoArgsConstructor
@JsonIgnoreProperties(ignoreUnknown = true)
public class CCDRequest {
    private String token;

    @JsonProperty("event_id")
    private String eventId;

    @JsonProperty("case_details")
    private CaseDetails caseDetails;
}
