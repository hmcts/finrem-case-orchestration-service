package uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd;

import com.fasterxml.jackson.databind.PropertyNamingStrategies;
import com.fasterxml.jackson.databind.annotation.JsonNaming;

@JsonNaming(PropertyNamingStrategies.SnakeCaseStrategy.class)
@SuppressWarnings("java:S115")
public enum ApplicantRole {
    FR_ApplicantsRoleInDivorce_1,
    FR_ApplicantsRoleInDivorce_2
}
