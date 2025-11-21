package uk.gov.hmcts.reform.finrem.caseorchestration.event;

import lombok.Builder;
import lombok.Getter;

@Getter
@Builder
public class StopRepresentingClientEvent {
    String userAuthorisation;
    String caseId;
}
