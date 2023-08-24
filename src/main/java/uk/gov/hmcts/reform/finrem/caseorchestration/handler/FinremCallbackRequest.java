package uk.gov.hmcts.reform.finrem.caseorchestration.handler;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Builder;
import lombok.Data;
import lombok.extern.jackson.Jacksonized;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.EventType;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.FinremCaseData;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.FinremCaseDetails;

@Data
@Builder
@JsonIgnoreProperties(ignoreUnknown = true)
@Jacksonized
public class FinremCallbackRequest<T extends FinremCaseData> {

    @JsonProperty("case_details")
    private FinremCaseDetails<T> caseDetails;

    @JsonProperty("case_details_before")
    private FinremCaseDetails<T> caseDetailsBefore;

    @JsonProperty("event_id")
    private EventType eventType;
}
