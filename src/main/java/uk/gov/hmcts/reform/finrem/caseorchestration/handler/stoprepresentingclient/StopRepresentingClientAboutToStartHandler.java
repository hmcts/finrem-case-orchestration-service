package uk.gov.hmcts.reform.finrem.caseorchestration.handler.stoprepresentingclient;

import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import uk.gov.hmcts.reform.finrem.caseorchestration.ccd.callback.CallbackType;
import uk.gov.hmcts.reform.finrem.caseorchestration.controllers.GenericAboutToStartOrSubmitCallbackResponse;
import uk.gov.hmcts.reform.finrem.caseorchestration.handler.CallbackHandlerLogger;
import uk.gov.hmcts.reform.finrem.caseorchestration.handler.FinremCallbackHandler;
import uk.gov.hmcts.reform.finrem.caseorchestration.handler.FinremCallbackRequest;
import uk.gov.hmcts.reform.finrem.caseorchestration.mapper.FinremCaseDetailsMapper;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.EventType;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.CaseRole;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.CaseType;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.FinremCaseData;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.YesOrNo;
import uk.gov.hmcts.reform.finrem.caseorchestration.service.CaseRoleService;

import java.util.Arrays;

import static uk.gov.hmcts.reform.finrem.caseorchestration.ccd.callback.CallbackType.ABOUT_TO_START;
import static uk.gov.hmcts.reform.finrem.caseorchestration.model.EventType.STOP_REPRESENTING_CLIENT;
import static uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.CaseRole.APP_SOLICITOR;
import static uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.CaseRole.RESP_SOLICITOR;
import static uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.CaseType.CONSENTED;
import static uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.CaseType.CONTESTED;

@Slf4j
@Service
public class StopRepresentingClientAboutToStartHandler extends FinremCallbackHandler {

    private final CaseRoleService caseRoleService;

    public StopRepresentingClientAboutToStartHandler(FinremCaseDetailsMapper finremCaseDetailsMapper,
                                                     CaseRoleService caseRoleService) {
        super(finremCaseDetailsMapper);
        this.caseRoleService = caseRoleService;
    }

    @Override
    public boolean canHandle(CallbackType callbackType, CaseType caseType, EventType eventType) {
        return ABOUT_TO_START.equals(callbackType)
            && Arrays.asList(CONTESTED, CONSENTED).contains(caseType)
            && STOP_REPRESENTING_CLIENT.equals(eventType);
    }

    @Override
    public GenericAboutToStartOrSubmitCallbackResponse<FinremCaseData> handle(FinremCallbackRequest callbackRequest,
                                                                              String userAuthorisation) {
        log.info(CallbackHandlerLogger.aboutToStart(callbackRequest));
        String caseId = callbackRequest.getCaseDetails().getCaseIdAsString();

        FinremCaseData caseData = callbackRequest.getCaseDetails().getData();
        // TODO remove after DFR-4138 release
        caseData.setCcdCaseId(caseId);

        prepareSessionWrapper(caseData, userAuthorisation);
        
        return GenericAboutToStartOrSubmitCallbackResponse.<FinremCaseData>builder()
            .data(caseData)
            .build();
    }

    private void prepareSessionWrapper(FinremCaseData caseData, String userAuthorisation) {
        String caseId = caseData.getCcdCaseId();

        CaseRole caseRole = caseRoleService.getUserCaseRole(caseId, userAuthorisation);
        caseData.getSessionWrapper().setLoginAsApplicantSolicitor(YesOrNo.forValue(APP_SOLICITOR.equals(caseRole)));
        caseData.getSessionWrapper().setLoginAsRespondentSolicitor(YesOrNo.forValue(RESP_SOLICITOR.equals(caseRole)));
    }
}
