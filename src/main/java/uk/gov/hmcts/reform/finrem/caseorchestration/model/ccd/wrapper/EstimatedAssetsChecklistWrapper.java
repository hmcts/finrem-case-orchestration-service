package uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.wrapper;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonInclude;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.EstimatedAssetsChecklistVersion;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.TemporaryField;

// PT todo - remove builder unless used in tests
@JsonIgnoreProperties(ignoreUnknown = true)
@Data
@Builder(toBuilder = true)
@AllArgsConstructor
@NoArgsConstructor
@JsonInclude(JsonInclude.Include.NON_NULL)
public class EstimatedAssetsChecklistWrapper {
    // use_estimatedAssetsChecklistV* values are used by EXUI to determine which version of the checklist to display.
    @TemporaryField
    private EstimatedAssetsChecklistVersion estimatedAssetsChecklistVersion; // = v2 v3 etc
}
