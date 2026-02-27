package uk.gov.hmcts.reform.finrem.caseorchestration.handler;

import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import uk.gov.hmcts.reform.finrem.caseorchestration.ccd.callback.CallbackType;
import uk.gov.hmcts.reform.finrem.caseorchestration.controllers.GenericAboutToStartOrSubmitCallbackResponse;
import uk.gov.hmcts.reform.finrem.caseorchestration.mapper.FinremCaseDetailsMapper;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.EventType;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.CaseType;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.FinremCaseData;
import uk.gov.hmcts.reform.finrem.caseorchestration.service.AssignPartiesAccessService;
import uk.gov.hmcts.reform.finrem.caseorchestration.service.CreateCaseService;

@Slf4j
@Service
public class PaperCaseCreateContestedSubmittedHandler extends FinremCallbackHandler {

    private final CreateCaseService createCaseService;

    private final AssignPartiesAccessService assignPartiesAccessService;

    public PaperCaseCreateContestedSubmittedHandler(FinremCaseDetailsMapper finremCaseDetailsMapper,
                                                    CreateCaseService createCaseService,
                                                    AssignPartiesAccessService assignPartiesAccessService) {
        super(finremCaseDetailsMapper);
        this.createCaseService = createCaseService;
        this.assignPartiesAccessService = assignPartiesAccessService;
    }

    @Override
    public boolean canHandle(CallbackType callbackType, CaseType caseType, EventType eventType) {
        return CallbackType.SUBMITTED.equals(callbackType)
            && CaseType.CONTESTED.equals(caseType)
            && EventType.NEW_PAPER_CASE.equals(eventType);
    }

    @Override
    public GenericAboutToStartOrSubmitCallbackResponse<FinremCaseData> handle(FinremCallbackRequest callbackRequest,
                                                                              String userAuthorisation) {
        log.info(CallbackHandlerLogger.submitted(callbackRequest));

        FinremCaseData finremCaseData = callbackRequest.getCaseDetails().getData();

        createCaseService.setSupplementaryData(callbackRequest, userAuthorisation);
        grantApplicantSolicitor(finremCaseData);

        return response(finremCaseData);
    }

    private void grantApplicantSolicitor(FinremCaseData caseData) {

        executeWithRetrySafely(log,
            () -> assignPartiesAccessService.grantApplicantSolicitor(caseData),
            caseData.getCcdCaseId(),
            "granting respondent solicitor",
            3
        );
    }
}
