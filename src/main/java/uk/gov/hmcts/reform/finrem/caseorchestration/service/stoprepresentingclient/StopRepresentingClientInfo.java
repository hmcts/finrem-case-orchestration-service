package uk.gov.hmcts.reform.finrem.caseorchestration.service.stoprepresentingclient;

import lombok.Builder;
import lombok.Getter;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.FinremCaseDetails;

@Getter
@Builder
public class StopRepresentingClientInfo {
    boolean invokedByIntervenerBarrister;
    String userAuthorisation;
    FinremCaseDetails caseDetails;
    FinremCaseDetails caseDetailsBefore;
}
