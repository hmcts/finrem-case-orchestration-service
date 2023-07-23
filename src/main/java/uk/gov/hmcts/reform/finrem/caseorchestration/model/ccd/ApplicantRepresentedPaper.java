package uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd;

import com.fasterxml.jackson.databind.PropertyNamingStrategies;
import com.fasterxml.jackson.databind.annotation.JsonNaming;

@JsonNaming(PropertyNamingStrategies.SnakeCaseStrategy.class)
public enum ApplicantRepresentedPaper {
    FR_APPLICANT_REPRESENTED_1,
    FR_APPLICANT_REPRESENTED_2,
    FR_APPLICANT_REPRESENTED_3
}
