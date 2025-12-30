package uk.gov.hmcts.reform.finrem.caseorchestration.event;

import lombok.Builder;
import lombok.Getter;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.FinremCaseDetails;

@Getter
@Builder
public class StopRepresentingClientInfo {
    boolean invokedByIntervener;
    String userAuthorisation;
    FinremCaseDetails caseDetails;
    FinremCaseDetails caseDetailsBefore;
}
