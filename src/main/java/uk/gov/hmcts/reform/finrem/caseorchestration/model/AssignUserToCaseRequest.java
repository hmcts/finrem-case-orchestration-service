package uk.gov.hmcts.reform.finrem.caseorchestration.model;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Builder;
import lombok.Getter;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.CaseRole;

@Builder
@Getter
public class AssignUserToCaseRequest {

    @NotNull
    private Long caseId;

    @NotBlank
    private String userId;

    @NotNull
    private CaseRole caseRole;
}
