package uk.gov.hmcts.reform.finrem.caseorchestration.handler;

import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import uk.gov.hmcts.reform.finrem.caseorchestration.ccd.callback.CallbackType;
import uk.gov.hmcts.reform.finrem.caseorchestration.controllers.GenericAboutToStartOrSubmitCallbackResponse;
import uk.gov.hmcts.reform.finrem.caseorchestration.mapper.FinremCaseDetailsMapper;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.EventType;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.CaseType;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.FinremCaseData;
import uk.gov.hmcts.reform.finrem.caseorchestration.service.AssignApplicantSolicitorService;
import uk.gov.hmcts.reform.finrem.caseorchestration.service.CaseDataService;
import uk.gov.hmcts.reform.finrem.caseorchestration.service.CreateCaseService;

@Slf4j
@Service
public class SolicitorCreateConsentedSubmittedHandler extends AssignApplicantSolicitorHandler {

    private final CreateCaseService createCaseService;
    private final CaseDataService caseDataService;

    public SolicitorCreateConsentedSubmittedHandler(FinremCaseDetailsMapper finremCaseDetailsMapper,
                                                    AssignApplicantSolicitorService assignApplicantSolicitorService,
                                                    CreateCaseService createCaseService,
                                                    CaseDataService caseDataService) {
        super(finremCaseDetailsMapper, assignApplicantSolicitorService);
        this.createCaseService = createCaseService;
        this.caseDataService = caseDataService;
    }

    @Override
    public boolean canHandle(CallbackType callbackType, CaseType caseType, EventType eventType) {
        return CallbackType.SUBMITTED.equals(callbackType)
            && CaseType.CONSENTED.equals(caseType)
            && (EventType.SOLICITOR_CREATE.equals(eventType));
    }

    @Override
    public GenericAboutToStartOrSubmitCallbackResponse<FinremCaseData> handle(FinremCallbackRequest callbackRequest,
                                                                              String userAuthorisation) {
        log.info("Processing Submitted callback for event {} with Case ID : {}",
            EventType.SOLICITOR_CREATE, callbackRequest.getCaseDetails().getId());
        if (!caseDataService.isPaperApplicationFinremCaseData(callbackRequest.getCaseDetails().getData())) {
            super.handle(callbackRequest, userAuthorisation);
        }
        createCaseService.setSupplementaryData(callbackRequest, userAuthorisation);

        return GenericAboutToStartOrSubmitCallbackResponse.<FinremCaseData>builder()
            .data(callbackRequest.getCaseDetails().getData()).build();
    }
}
