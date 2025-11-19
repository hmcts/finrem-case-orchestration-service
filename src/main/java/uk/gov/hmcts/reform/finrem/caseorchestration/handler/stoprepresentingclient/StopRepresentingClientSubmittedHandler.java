package uk.gov.hmcts.reform.finrem.caseorchestration.handler.stoprepresentingclient;

import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import uk.gov.hmcts.reform.authorisation.generators.AuthTokenGenerator;
import uk.gov.hmcts.reform.finrem.caseorchestration.ccd.callback.CallbackType;
import uk.gov.hmcts.reform.finrem.caseorchestration.client.DataStoreClient;
import uk.gov.hmcts.reform.finrem.caseorchestration.controllers.GenericAboutToStartOrSubmitCallbackResponse;
import uk.gov.hmcts.reform.finrem.caseorchestration.handler.CallbackHandlerLogger;
import uk.gov.hmcts.reform.finrem.caseorchestration.handler.FinremCallbackHandler;
import uk.gov.hmcts.reform.finrem.caseorchestration.handler.FinremCallbackRequest;
import uk.gov.hmcts.reform.finrem.caseorchestration.mapper.FinremCaseDetailsMapper;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.EventType;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.CaseAssignedUserRole;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.CaseAssignedUserRolesResource;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.CaseType;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.FinremCaseData;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.events.AuditEvent;
import uk.gov.hmcts.reform.finrem.caseorchestration.service.AuditEventService;
import uk.gov.hmcts.reform.finrem.caseorchestration.service.CcdDataStoreService;
import uk.gov.hmcts.reform.finrem.caseorchestration.service.IdamAuthService;
import uk.gov.hmcts.reform.finrem.caseorchestration.service.SystemUserService;
import uk.gov.hmcts.reform.idam.client.models.UserDetails;

import java.util.Arrays;
import java.util.List;

import static java.lang.String.format;
import static uk.gov.hmcts.reform.finrem.caseorchestration.ccd.callback.CallbackType.SUBMITTED;
import static uk.gov.hmcts.reform.finrem.caseorchestration.model.EventType.STOP_REPRESENTING_CLIENT;
import static uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.CaseType.CONSENTED;
import static uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.CaseType.CONTESTED;

@Slf4j
@Service
public class StopRepresentingClientSubmittedHandler extends FinremCallbackHandler {

    private final CcdDataStoreService ccdDataStoreService;
    private final SystemUserService systemUserService;
    private final AuthTokenGenerator authTokenGenerator;
    private final DataStoreClient dataStoreClient;
    private final AuditEventService auditEventService;
    private final IdamAuthService idamClient;

    private static final String CONFIRMATION_HEADER = "# Notice of change request submitted";

    public StopRepresentingClientSubmittedHandler(FinremCaseDetailsMapper finremCaseDetailsMapper,
                                                  CcdDataStoreService ccdDataStoreService,
                                                  SystemUserService systemUserService,
                                                  AuthTokenGenerator authTokenGenerator,
                                                  DataStoreClient dataStoreClient,
                                                  AuditEventService auditEventService,
                                                  IdamAuthService idamClient) {
        super(finremCaseDetailsMapper);
        this.ccdDataStoreService = ccdDataStoreService;
        this.systemUserService = systemUserService;
        this.authTokenGenerator = authTokenGenerator;
        this.dataStoreClient = dataStoreClient;
        this.auditEventService = auditEventService;
        this.idamClient = idamClient;
    }

    @Override
    public boolean canHandle(CallbackType callbackType, CaseType caseType, EventType eventType) {
        return SUBMITTED.equals(callbackType)
            && Arrays.asList(CONTESTED, CONSENTED).contains(caseType)
            && STOP_REPRESENTING_CLIENT.equals(eventType);
    }

    @Override
    public GenericAboutToStartOrSubmitCallbackResponse<FinremCaseData> handle(FinremCallbackRequest callbackRequest,
                                                                              String userAuthorisation) {
        log.info(CallbackHandlerLogger.submitted(callbackRequest));
        String caseId = callbackRequest.getCaseDetails().getCaseIdAsString();

        List<CaseAssignedUserRole> caseAssignedUserRoles = getCaseAssignedUserRoles(userAuthorisation, caseId);

        String sysAuthToken = systemUserService.getSysUserToken();
        ccdDataStoreService.removeUserCaseRole(caseId,
            sysAuthToken, getInvokerDetails(userAuthorisation, caseId).getId(), "role");

        return GenericAboutToStartOrSubmitCallbackResponse.<FinremCaseData>builder()
            .confirmationHeader(CONFIRMATION_HEADER)
            .confirmationBody("<br /><br />")
            .build();
    }

    private UserDetails getInvokerDetails(String authToken, String caseId) {
        AuditEvent auditEvent = auditEventService.getLatestAuditEventByName(caseId, "FR_stopRepresentingClient")
            .orElseThrow(() -> new IllegalStateException(format("%s - Could not find %s event in audit", caseId, "FR_stopRepresentingClient")));

        return idamClient.getUserByUserId(authToken, auditEvent.getUserId());
    }

    private List<CaseAssignedUserRole> getCaseAssignedUserRoles(String authToken, String caseId) {
        String serviceToken = authTokenGenerator.generate();
        CaseAssignedUserRolesResource caseAssignedUserRolesResource = dataStoreClient.getUserRoles(authToken,
            serviceToken, caseId, null);

        return caseAssignedUserRolesResource.getCaseAssignedUserRoles();
    }
}
