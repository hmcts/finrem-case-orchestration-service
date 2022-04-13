package uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.events;

import lombok.Builder;
import lombok.Data;

import java.util.List;

@Data
@Builder(toBuilder = true)
public class AuditEventsResponse {
    private final List<AuditEvent> auditEvents;
}
