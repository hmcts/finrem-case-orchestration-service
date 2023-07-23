package uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd;

import com.fasterxml.jackson.databind.PropertyNamingStrategies;
import com.fasterxml.jackson.databind.annotation.JsonNaming;

@JsonNaming(PropertyNamingStrategies.SnakeCaseStrategy.class)
@SuppressWarnings("java:S115")
public enum ApplicantRepresentedPaper {
    FR_applicant_represented_1,
    FR_applicant_represented_2,
    FR_applicant_represented_3
}
