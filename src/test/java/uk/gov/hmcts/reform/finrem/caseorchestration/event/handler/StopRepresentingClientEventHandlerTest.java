package uk.gov.hmcts.reform.finrem.caseorchestration.event.handler;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import uk.gov.hmcts.reform.finrem.caseorchestration.event.StopRepresentingClientEvent;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.events.AuditEvent;
import uk.gov.hmcts.reform.finrem.caseorchestration.service.AuditEventService;
import uk.gov.hmcts.reform.finrem.caseorchestration.service.CaseRoleService;
import uk.gov.hmcts.reform.finrem.caseorchestration.service.CcdDataStoreService;
import uk.gov.hmcts.reform.finrem.caseorchestration.service.SystemUserService;

import java.util.Optional;

import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static uk.gov.hmcts.reform.finrem.caseorchestration.TestConstants.AUTH_TOKEN;
import static uk.gov.hmcts.reform.finrem.caseorchestration.TestConstants.CASE_ID;
import static uk.gov.hmcts.reform.finrem.caseorchestration.TestConstants.TEST_SYSTEM_TOKEN;
import static uk.gov.hmcts.reform.finrem.caseorchestration.TestConstants.TEST_USER_ID;
import static uk.gov.hmcts.reform.finrem.caseorchestration.model.EventType.STOP_REPRESENTING_CLIENT;
import static uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.CaseRole.APP_SOLICITOR;

@ExtendWith(MockitoExtension.class)
class StopRepresentingClientEventHandlerTest {

    @Mock
    private AuditEventService auditEventService;
    @Mock
    private SystemUserService systemUserService;
    @Mock
    private CcdDataStoreService ccdDataStoreService;
    @Mock
    private CaseRoleService caseRoleService;

    private StopRepresentingClientEventHandler underTest;

    @BeforeEach
    public void setup() {
        underTest = new StopRepresentingClientEventHandler(auditEventService, systemUserService, ccdDataStoreService,
            caseRoleService);
    }

    @Test
    void testHandleEvent() {
        when(systemUserService.getSysUserToken()).thenReturn(TEST_SYSTEM_TOKEN);
        when(auditEventService.getLatestAuditEventByName(CASE_ID, STOP_REPRESENTING_CLIENT.getCcdType()))
            .thenReturn(Optional.of(AuditEvent.builder().userId(TEST_USER_ID).build()));
        when(caseRoleService.getUserCaseRole(CASE_ID, AUTH_TOKEN)).thenReturn(APP_SOLICITOR);

        underTest.handleEvent(StopRepresentingClientEvent.builder()
            .caseId(CASE_ID)
            .userAuthorisation(AUTH_TOKEN)
            .build());

        verify(ccdDataStoreService).removeUserCaseRole(CASE_ID, TEST_SYSTEM_TOKEN, TEST_USER_ID, APP_SOLICITOR.getCcdCode());
    }
}
