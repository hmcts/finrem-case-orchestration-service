package uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.managehearings;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Builder
@Data
@AllArgsConstructor
@NoArgsConstructor
public class PartyOnCase {
    private String role;
    private String label;
}
