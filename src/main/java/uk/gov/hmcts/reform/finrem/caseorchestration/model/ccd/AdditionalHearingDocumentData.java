package uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Builder;
import lombok.Getter;

@JsonIgnoreProperties(ignoreUnknown = true)
@Builder
@Getter
public class AdditionalHearingDocumentData {
    @JsonProperty("id")
    private String id;
    @JsonProperty("value")
    private AdditionalHearingDocument additionalHearingDocument;
}