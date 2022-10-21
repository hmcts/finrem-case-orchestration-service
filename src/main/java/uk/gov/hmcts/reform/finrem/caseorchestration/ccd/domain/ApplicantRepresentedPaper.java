package uk.gov.hmcts.reform.finrem.caseorchestration.ccd.domain;

import com.fasterxml.jackson.databind.PropertyNamingStrategy;
import com.fasterxml.jackson.databind.annotation.JsonNaming;

@JsonNaming(PropertyNamingStrategy.SnakeCaseStrategy.class)
public enum ApplicantRepresentedPaper {
    FR_applicant_represented_1,
    FR_applicant_represented_2,
    FR_applicant_represented_3
}
