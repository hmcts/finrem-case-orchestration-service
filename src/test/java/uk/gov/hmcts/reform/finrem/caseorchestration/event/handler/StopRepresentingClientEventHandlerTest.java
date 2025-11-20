package uk.gov.hmcts.reform.finrem.caseorchestration.event.handler;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import uk.gov.hmcts.reform.finrem.caseorchestration.event.StopRepresentingClientEvent;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.events.AuditEvent;
import uk.gov.hmcts.reform.finrem.caseorchestration.service.AuditEventService;
import uk.gov.hmcts.reform.finrem.caseorchestration.service.CcdDataStoreService;
import uk.gov.hmcts.reform.finrem.caseorchestration.service.IdamAuthService;
import uk.gov.hmcts.reform.finrem.caseorchestration.service.SystemUserService;
import uk.gov.hmcts.reform.idam.client.models.UserDetails;

import java.util.Optional;

import static org.mockito.Mockito.mock;
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
    private IdamAuthService idamClient;
    @Mock
    private SystemUserService systemUserService;
    @Mock
    private CcdDataStoreService ccdDataStoreService;

    private StopRepresentingClientEventHandler underTest;

    @BeforeEach
    public void setup() {
        underTest = new StopRepresentingClientEventHandler(auditEventService, idamClient, systemUserService, ccdDataStoreService);
    }

    @Test
    void testHandleEvent() {
        when(systemUserService.getSysUserToken()).thenReturn(TEST_SYSTEM_TOKEN);
        when(auditEventService.getLatestAuditEventByName(CASE_ID, STOP_REPRESENTING_CLIENT.getCcdType()))
            .thenReturn(Optional.of(AuditEvent.builder().userId(TEST_USER_ID).build()));
        UserDetails mockedUserDetails = mock(UserDetails.class);
        when(mockedUserDetails.getId()).thenReturn("mockedUserId");
        when(idamClient.getUserByUserId(AUTH_TOKEN, TEST_USER_ID)).thenReturn(mockedUserDetails);

        underTest.handleEvent(StopRepresentingClientEvent.builder()
            .caseId(CASE_ID)
            .userAuthorisation(AUTH_TOKEN)
            .build());

        verify(ccdDataStoreService).removeUserCaseRole(CASE_ID, TEST_SYSTEM_TOKEN, "mockedUserId", APP_SOLICITOR.getCcdCode());
    }
}
