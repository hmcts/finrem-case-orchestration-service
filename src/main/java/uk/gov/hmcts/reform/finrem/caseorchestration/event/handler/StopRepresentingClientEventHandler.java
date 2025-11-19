package uk.gov.hmcts.reform.finrem.caseorchestration.event.handler;

import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.event.EventListener;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Component;
import uk.gov.hmcts.reform.finrem.caseorchestration.event.StopRepresentingClientEvent;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.events.AuditEvent;
import uk.gov.hmcts.reform.finrem.caseorchestration.service.AuditEventService;
import uk.gov.hmcts.reform.finrem.caseorchestration.service.CcdDataStoreService;
import uk.gov.hmcts.reform.finrem.caseorchestration.service.IdamAuthService;
import uk.gov.hmcts.reform.finrem.caseorchestration.service.SystemUserService;
import uk.gov.hmcts.reform.idam.client.models.UserDetails;

import static java.lang.String.format;
import static uk.gov.hmcts.reform.finrem.caseorchestration.model.EventType.STOP_REPRESENTING_CLIENT;
import static uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.CaseRole.APP_SOLICITOR;

@Component
@RequiredArgsConstructor(onConstructor = @__(@Autowired))
public class StopRepresentingClientEventHandler {

    private final AuditEventService auditEventService;
    private final IdamAuthService idamClient;
    private final SystemUserService systemUserService;
    private final CcdDataStoreService ccdDataStoreService;

    @EventListener
    @Async
    public void handleCaseDataChange(final StopRepresentingClientEvent event) {
        String caseId = event.getCaseId();
        String sysAuthToken = systemUserService.getSysUserToken();
        ccdDataStoreService.removeUserCaseRole(caseId,
            sysAuthToken, getInvokerDetails(event.getUserAuthorisation(), caseId).getId(), APP_SOLICITOR.getCcdCode());
    }

    private UserDetails getInvokerDetails(String authToken, String caseId) {
        String event = STOP_REPRESENTING_CLIENT.getCcdType();
        AuditEvent auditEvent = auditEventService.getLatestAuditEventByName(caseId, event)
            .orElseThrow(() -> new IllegalStateException(format("%s - Could not find %s event in audit", caseId,event)));
        return idamClient.getUserByUserId(authToken, auditEvent.getUserId());
    }
}
