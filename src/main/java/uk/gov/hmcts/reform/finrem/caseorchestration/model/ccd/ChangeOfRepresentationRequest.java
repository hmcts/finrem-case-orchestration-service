package uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd;

import lombok.Builder;
import lombok.Value;


@Value
@Builder(toBuilder = true)
public class ChangeOfRepresentationRequest {

    RepresentationUpdateHistory current;
    String party;
    String clientName;
    ChangedRepresentative addedRepresentative;
    ChangedRepresentative removedRepresentative;
    String by;

}
