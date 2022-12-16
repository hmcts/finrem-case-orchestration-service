package uk.gov.hmcts.reform.finrem.caseorchestration.handler;

import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import uk.gov.hmcts.reform.ccd.client.model.CallbackRequest;
import uk.gov.hmcts.reform.finrem.caseorchestration.ccd.callback.CallbackType;
import uk.gov.hmcts.reform.finrem.caseorchestration.controllers.GenericAboutToStartOrSubmitCallbackResponse;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.EventType;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.CaseType;
import uk.gov.hmcts.reform.finrem.caseorchestration.service.AssignApplicantSolicitorService;
import uk.gov.hmcts.reform.finrem.caseorchestration.service.CaseDataService;
import uk.gov.hmcts.reform.finrem.caseorchestration.service.OnStartDefaultValueService;

import java.util.Map;

@Slf4j
@Service
public class SolicitorCreateContestedAboutToStartHandler extends AssignApplicantSolicitorHandler {

    private final OnStartDefaultValueService service;
    private final CaseDataService caseDataService;

    public SolicitorCreateContestedAboutToStartHandler(AssignApplicantSolicitorService assignApplicantSolicitorService,
                                                       CaseDataService caseDataService, OnStartDefaultValueService service) {
        super(assignApplicantSolicitorService);
        this.service = service;
        this.caseDataService = caseDataService;
    }

    @Override
    public boolean canHandle(CallbackType callbackType, CaseType caseType, EventType eventType) {
        return CallbackType.ABOUT_TO_START.equals(callbackType)
            && CaseType.CONTESTED.equals(caseType)
            && (EventType.SOLICITOR_CREATE.equals(eventType));
    }

    @Override
    public GenericAboutToStartOrSubmitCallbackResponse<Map<String, Object>> handle(CallbackRequest callbackRequest,
                                                                                   String userAuthorisation) {
        service.defaultCivilPartnershipField(callbackRequest);
        service.defaultTypeOfApplication(callbackRequest);
        if(!caseDataService.isContestedPaperApplication(callbackRequest.getCaseDetails()))
        {
            return super.handle(callbackRequest, userAuthorisation);
        }
        return GenericAboutToStartOrSubmitCallbackResponse.<Map<String, Object>>builder().data(callbackRequest
            .getCaseDetails().getData()).build();
    }
}
