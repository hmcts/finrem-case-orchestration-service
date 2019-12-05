package uk.gov.hmcts.reform.finrem.caseorchestration.model.scan.transformation.out;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Builder;
import lombok.Getter;

import java.util.Map;

@Builder
@Getter
public class CaseCreationDetails {

    @JsonProperty("case_type_id")
    private final String caseTypeId;

    @JsonProperty("event_id")
    private final String eventId;

    @JsonProperty("case_data")
    private final Map<String, Object> caseData;

    public CaseCreationDetails(
        String caseTypeId,
        String eventId,
        Map<String, Object> caseData
    ) {
        this.caseTypeId = caseTypeId;
        this.eventId = eventId;
        this.caseData = caseData;
    }
}
