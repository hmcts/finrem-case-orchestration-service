package uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@JsonIgnoreProperties(ignoreUnknown = true)
@Builder
@Getter
@NoArgsConstructor
@AllArgsConstructor
public class AdditionalHearingDocument {
    @JsonProperty("additionalHearingDocument")
    private CaseDocument document;
    @JsonProperty("additionalHearingDocumentDate")
    private LocalDateTime additionalHearingDocumentDate;
}
