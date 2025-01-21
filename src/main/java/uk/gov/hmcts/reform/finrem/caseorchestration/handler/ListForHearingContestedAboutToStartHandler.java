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
import uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.YesOrNo;
import uk.gov.hmcts.reform.finrem.caseorchestration.service.PartyService;


@Slf4j
@Service
public class ListForHearingContestedAboutToStartHandler extends FinremCallbackHandler {
    private final PartyService partyService;

    public ListForHearingContestedAboutToStartHandler(FinremCaseDetailsMapper finremCaseDetailsMapper, PartyService partyService) {
        super(finremCaseDetailsMapper);
        this.partyService = partyService;
    }

    @Override
    public boolean canHandle(CallbackType callbackType, CaseType caseType, EventType eventType) {
        return CallbackType.ABOUT_TO_START.equals(callbackType)
            && CaseType.CONTESTED.equals(caseType)
            && EventType.LIST_FOR_HEARING.equals(eventType);
    }


    @Override
    public GenericAboutToStartOrSubmitCallbackResponse<FinremCaseData> handle(FinremCallbackRequest callbackRequest,
                                                                              String userAuthorisation) {
        log.info("Handling contested event {} about to start callback for Case ID: {}",
            EventType.LIST_FOR_HEARING, callbackRequest.getCaseDetails().getId());

        FinremCaseDetails caseDetails = callbackRequest.getCaseDetails();
        FinremCaseData caseData = callbackRequest.getCaseDetails().getData();
        if (caseData.getListForHearingWrapper().getAdditionalHearingDocumentsOption() == null) {
            caseData.getListForHearingWrapper().setAdditionalHearingDocumentsOption(YesOrNo.NO);
        }
        caseData.getSendOrderWrapper().setPartiesOnCase(partyService.getAllActivePartyList(caseDetails));
        return GenericAboutToStartOrSubmitCallbackResponse.<FinremCaseData>builder().data(caseData).build();
    }
}
