package uk.gov.hmcts.reform.finrem.caseorchestration.model;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Builder;
import lombok.Data;
import lombok.extern.jackson.Jacksonized;
import uk.gov.hmcts.reform.finrem.caseorchestration.ccd.domain.EventType;
import uk.gov.hmcts.reform.finrem.caseorchestration.ccd.domain.FinremCaseDetails;

@Data
@Builder
@JsonIgnoreProperties(ignoreUnknown = true)
@Jacksonized
public class CallbackRequestWithoutMap {

    @JsonProperty("case_details")
    private FinremCaseDetails caseDetails;

    @JsonProperty("case_details_before")
    private FinremCaseDetails caseDetailsBefore;

    @JsonProperty("event_id")
    private EventType eventType;
}
