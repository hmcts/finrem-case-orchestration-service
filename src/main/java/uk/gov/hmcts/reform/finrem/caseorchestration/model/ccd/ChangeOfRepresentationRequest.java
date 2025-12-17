package uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd;

import lombok.Builder;
import lombok.Value;

@Value
@Builder(toBuilder = true)
public class ChangeOfRepresentationRequest {

    public static final String APPLICANT_PARTY = "Applicant";
    public static final String RESPONDENT_PARTY = "Respondent";

    RepresentationUpdateHistory current;
    String party;
    String clientName;
    ChangedRepresentative addedRepresentative;
    ChangedRepresentative removedRepresentative;
    String by;
    String via;
}
