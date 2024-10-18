package uk.gov.hmcts.reform.finrem.caseorchestration.handler;

import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import uk.gov.hmcts.reform.finrem.caseorchestration.ccd.callback.CallbackType;
import uk.gov.hmcts.reform.finrem.caseorchestration.controllers.GenericAboutToStartOrSubmitCallbackResponse;
import uk.gov.hmcts.reform.finrem.caseorchestration.mapper.FinremCaseDetailsMapper;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.EventType;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.CaseType;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.FinremCaseData;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.FinremCaseDetails;
import uk.gov.hmcts.reform.finrem.caseorchestration.service.correspondence.hearing.ContestedListForHearingCorrespondenceService;

@Slf4j
@Service
public class ListForHearingContestedSubmittedHandler extends FinremCallbackHandler {

    private final ContestedListForHearingCorrespondenceService contestedListForHearingCorrespondenceService;

    public ListForHearingContestedSubmittedHandler(FinremCaseDetailsMapper finremCaseDetailsMapper,
                                                   ContestedListForHearingCorrespondenceService contestedListForHearingCorrespondenceService) {
        super(finremCaseDetailsMapper);
        this.contestedListForHearingCorrespondenceService = contestedListForHearingCorrespondenceService;
    }

    @Override
    public boolean canHandle(CallbackType callbackType, CaseType caseType, EventType eventType) {
        return CallbackType.SUBMITTED.equals(callbackType)
            && CaseType.CONTESTED.equals(caseType)
            && EventType.LIST_FOR_HEARING.equals(eventType);
    }

    @Override
    public GenericAboutToStartOrSubmitCallbackResponse<FinremCaseData> handle(FinremCallbackRequest callbackRequest,
                                                                              String userAuthorisation) {
        validateCaseData(callbackRequest);
        FinremCaseDetails finremCaseDetails = callbackRequest.getCaseDetails();
        FinremCaseData finremCaseData = finremCaseDetails.getData();
        String caseId = finremCaseDetails.getId().toString();
        log.info("Invoking contested {} submitted callback for Case ID: {}",
            callbackRequest.getEventType(), caseId);
        contestedListForHearingCorrespondenceService.sendHearingCorrespondence(callbackRequest, userAuthorisation);

        return GenericAboutToStartOrSubmitCallbackResponse.<FinremCaseData>builder().data(finremCaseData).build();
    }

}
