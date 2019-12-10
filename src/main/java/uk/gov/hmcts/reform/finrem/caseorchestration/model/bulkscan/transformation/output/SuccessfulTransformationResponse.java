package uk.gov.hmcts.reform.finrem.caseorchestration.model.bulkscan.transformation.output;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Builder;
import lombok.Data;

import java.util.ArrayList;
import java.util.List;

@Data
@Builder
public class SuccessfulTransformationResponse {

    @JsonProperty("case_creation_details")
    private final CaseCreationDetails caseCreationDetails;

    @JsonProperty("warnings")
    private final List<String> warnings = new ArrayList<>();

}