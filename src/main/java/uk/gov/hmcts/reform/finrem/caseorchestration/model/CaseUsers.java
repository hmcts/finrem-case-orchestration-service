package uk.gov.hmcts.reform.finrem.caseorchestration.model;

import lombok.Builder;
import lombok.Getter;

@Builder
@Getter
public class CaseUsers {
    private String caseId;
    private String userId;
    private String caseRole;
}
