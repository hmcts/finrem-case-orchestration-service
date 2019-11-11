package uk.gov.hmcts.reform.finrem.caseorchestration.model.bulkscan.transformation.output;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Data;

import java.util.List;

@Data
public class SuccessfulTransformationResponse {

    @JsonProperty("case_creation_details")
    public final CaseCreationDetails caseCreationDetails;

    @JsonProperty("warnings")
    public final List<String> warnings;

    public SuccessfulTransformationResponse(
        CaseCreationDetails caseCreationDetails,
        List<String> warnings) {
        this.caseCreationDetails = caseCreationDetails;
        this.warnings = warnings;
    }

}