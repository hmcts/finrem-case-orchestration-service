package uk.gov.hmcts.reform.finrem.caseorchestration.event.handler;

import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.event.EventListener;
import org.springframework.stereotype.Component;
import uk.gov.hmcts.reform.finrem.caseorchestration.event.StopRepresentingClientEvent;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.CaseRole;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.events.AuditEvent;
import uk.gov.hmcts.reform.finrem.caseorchestration.service.AuditEventService;
import uk.gov.hmcts.reform.finrem.caseorchestration.service.CaseRoleService;
import uk.gov.hmcts.reform.finrem.caseorchestration.service.CcdDataStoreService;
import uk.gov.hmcts.reform.finrem.caseorchestration.service.SystemUserService;

import static java.lang.String.format;
import static uk.gov.hmcts.reform.finrem.caseorchestration.model.EventType.STOP_REPRESENTING_CLIENT;

@Component
@RequiredArgsConstructor(onConstructor = @__(@Autowired))
public class StopRepresentingClientEventHandler {

    private final AuditEventService auditEventService;
    private final SystemUserService systemUserService;
    private final CcdDataStoreService ccdDataStoreService;
    private final CaseRoleService caseRoleService;

    @EventListener
    public void handleEvent(final StopRepresentingClientEvent event) {
        // Acc @Async  if success message screen needed
        String caseId = event.getCaseId();
        String sysAuthToken = systemUserService.getSysUserToken();
        CaseRole caseRole = caseRoleService.getUserCaseRole(caseId, event.getUserAuthorisation());
        ccdDataStoreService.removeUserCaseRole(caseId, sysAuthToken,
            getInvokerUserId(caseId),
            caseRole.getCcdCode());
    }

    private String getInvokerUserId(String caseId) {
        String event = STOP_REPRESENTING_CLIENT.getCcdType();
        AuditEvent auditEvent = auditEventService.getLatestAuditEventByName(caseId, event)
            .orElseThrow(() -> new IllegalStateException(format("%s - Could not find %s event in audit", caseId, event)));
        return auditEvent.getUserId();
    }
}
