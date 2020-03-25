package uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;

@Builder
@Getter
@AllArgsConstructor
public class ChildInfo {

    private final String name;
    private final String dateOfBirth;
    private final String gender;
    private final String relationshipToApplicant;
    private final String relationshipToRespondent;
    private final String countryOfResidence;
}
