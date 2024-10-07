package uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.EqualsAndHashCode;

@JsonIgnoreProperties(ignoreUnknown = true)
@Data
@Builder
@AllArgsConstructor
@EqualsAndHashCode
public class PensionSharingAnnex implements HasCaseDocument {

    @JsonProperty("suggestedPensionSharingAnnexes")
    CaseDocument suggestedPensionSharingAnnexes;

}
