package uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Data;

import java.util.List;

@JsonIgnoreProperties(ignoreUnknown = true)
@Data
public class HearingOrderCollectionData {
    @JsonProperty("id")
    private String id;
    @JsonProperty("value")
    private HearingOrderDocument hearingOrderDocuments;
}
