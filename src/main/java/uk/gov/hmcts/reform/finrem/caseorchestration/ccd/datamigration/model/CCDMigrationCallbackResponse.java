package uk.gov.hmcts.reform.finrem.caseorchestration.ccd.datamigration.model;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import uk.gov.hmcts.reform.finrem.caseorchestration.ccd.datamigration.model.v1.CaseData;

import java.util.List;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
@JsonIgnoreProperties(ignoreUnknown = true)
public class CCDMigrationCallbackResponse {

    private CaseData data;
    private List<String> errors;
    private List<String> warnings;
}

