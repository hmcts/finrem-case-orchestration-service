package uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd;

import lombok.Builder;
import lombok.Getter;

@Builder
@Getter
public class ChildInfo {

    private final String name;
    private final String dateOfBirth;
    private final String gender;
    private final String relationshipToApplicant;
    private final String relationshipToRespondent;
    private final String countryOfResidence;

    public ChildInfo(String name,
                     String dateOfBirth,
                     String gender,
                     String relationshipToApplicant,
                     String relationshipToRespondent,
                     String countryOfResidence) {
        this.name = name;
        this.dateOfBirth = dateOfBirth;
        this.gender = gender;
        this.relationshipToApplicant = relationshipToApplicant;
        this.relationshipToRespondent = relationshipToRespondent;
        this.countryOfResidence = countryOfResidence;
    }
}
