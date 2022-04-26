package uk.gov.hmcts.reform.finrem.caseorchestration.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import uk.gov.hmcts.reform.authorisation.generators.AuthTokenGenerator;
import uk.gov.hmcts.reform.finrem.caseorchestration.client.CaseDataApiV2;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.events.AuditEvent;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.events.AuditEventsResponse;

import java.util.Comparator;
import java.util.Optional;
import java.util.function.Predicate;

@Service
@Slf4j
@RequiredArgsConstructor(onConstructor = @__(@Autowired))
public class AuditEventService {
    private final CaseDataApiV2 caseDataApi;
    private final AuthTokenGenerator authTokenGenerator;
    private final SystemUserService systemUserService;

    private static final String NOC_EVENT = "nocRequest";
    private static final String REMOVE_REPRESENTATION_EVENT = "removeRepresentation";

    private final Predicate<AuditEvent> isNocEvent = event -> NOC_EVENT.equals(event.getId())
        || REMOVE_REPRESENTATION_EVENT.equals(event.getId());

    public Optional<AuditEvent> getLatestNocAuditEventByName(String caseId) {
        String userToken = systemUserService.getSysUserToken();

        String authToken = authTokenGenerator.generate();
        AuditEventsResponse auditEventsResponse
            = caseDataApi.getAuditEvents(userToken, authToken, false, caseId);

        return auditEventsResponse.getAuditEvents().stream()
            .filter(isNocEvent)
            .max(Comparator.comparing(AuditEvent::getCreatedDate));
    }
}
