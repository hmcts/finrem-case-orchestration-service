package uk.gov.hmcts.reform.finrem.caseorchestration.notifications.notifiers;

import lombok.Builder;
import lombok.Getter;

@Getter
@Builder(toBuilder = true)
public class SendCorrespondenceEventEnvelop {

    String description;
    SendCorrespondenceEvent event;
}
