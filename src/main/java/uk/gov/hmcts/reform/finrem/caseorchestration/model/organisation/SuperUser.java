package uk.gov.hmcts.reform.finrem.caseorchestration.model.organisation;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class SuperUser {
    private String email;
    private String firstName;
    private String lastName;
}