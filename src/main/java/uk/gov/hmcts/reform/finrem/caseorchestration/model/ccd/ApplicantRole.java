package uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd;

import com.fasterxml.jackson.databind.PropertyNamingStrategies;
import com.fasterxml.jackson.databind.annotation.JsonNaming;

@JsonNaming(PropertyNamingStrategies.SnakeCaseStrategy.class)
public enum ApplicantRole {
    FR_APPLICANTS_ROLE_IN_DIVORCE_1,
    FR_APPLICANTS_ROLE_IN_DIVORCE_2
}
