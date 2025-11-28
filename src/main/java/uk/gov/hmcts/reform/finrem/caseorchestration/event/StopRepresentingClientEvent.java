package uk.gov.hmcts.reform.finrem.caseorchestration.event;

import lombok.Builder;
import lombok.Getter;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.FinremCaseData;

@Getter
@Builder
public class StopRepresentingClientEvent {
    String userAuthorisation;
    FinremCaseData data;
    FinremCaseData originalData;
}
