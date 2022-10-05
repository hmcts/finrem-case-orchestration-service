package uk.gov.hmcts.reform.finrem.caseorchestration.model;

import lombok.Builder;
import lombok.Getter;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.caseflag.Flags;

@Builder
@Getter
public class CaseFlags {
    private Flags caseflags;
}
