package uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Data;

@Data
public class DraftHearingOrder {

    @JsonProperty("purposeOfDocument")
    String purposeOfDocument;

    @JsonProperty("uploadDraftDocument")
    CaseDocument caseDocument;
}
