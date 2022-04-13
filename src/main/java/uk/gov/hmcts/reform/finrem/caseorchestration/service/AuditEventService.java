package uk.gov.hmcts.reform.finrem.caseorchestration.service;

import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import uk.gov.hmcts.reform.authorisation.generators.AuthTokenGenerator;
import uk.gov.hmcts.reform.finrem.caseorchestration.client.CaseDataApiV2;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.events.AuditEvent;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.events.AuditEventsResponse;

import java.util.Comparator;
import java.util.Optional;

@Service
@RequiredArgsConstructor(onConstructor = @__(@Autowired))
public class AuditEventService {
    private final CaseDataApiV2 caseDataApi;
    private final AuthTokenGenerator authTokenGenerator;
    private final SystemUserService systemUserService;

    public Optional<AuditEvent> getLatestAuditEventByName(String caseId, String eventName) {
        String userToken = systemUserService.getSysUserToken();

        AuditEventsResponse auditEventsResponse
            = caseDataApi.getAuditEvents(userToken, authTokenGenerator.generate(), false, caseId);

        return auditEventsResponse.getAuditEvents().stream()
            .filter(auditEvent -> eventName.equals(auditEvent.getId()))
            .max(Comparator.comparing(AuditEvent::getCreatedDate));
    }
}
