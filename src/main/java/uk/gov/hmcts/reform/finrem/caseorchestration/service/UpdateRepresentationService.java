package uk.gov.hmcts.reform.finrem.caseorchestration.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import uk.gov.hmcts.reform.ccd.client.model.CaseDetails;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.events.AuditEvent;
import uk.gov.hmcts.reform.idam.client.IdamClient;
import uk.gov.hmcts.reform.idam.client.models.UserDetails;

import java.util.Map;

@Slf4j
@Service
@RequiredArgsConstructor(onConstructor = @__(@Autowired))
public class UpdateRepresentationService {
    private final AuditEventService auditEventService;
    private final IdamClient idamClient;
    private static final String NOC_EVENT = "nocRequest";

    public Map<String, Object> updateRepresentation(CaseDetails caseDetails, String authToken) {
        Map<String, Object> caseData = caseDetails.getData();

        AuditEvent auditEvent = auditEventService.getLatestAuditEventByName(caseDetails.getId().toString(), NOC_EVENT)
            .orElseThrow(() -> new IllegalStateException(String.format("Could not find %s event in audit", NOC_EVENT)));

        UserDetails solicitor = idamClient.getUserByUserId(authToken, auditEvent.getUserId());

        return caseData;
    }

    private Map<String, Object> generateCOR(CaseDetails caseDetails) {
        return null;
    }
}
