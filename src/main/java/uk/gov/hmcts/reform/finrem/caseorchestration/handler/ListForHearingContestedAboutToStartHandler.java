package uk.gov.hmcts.reform.finrem.caseorchestration.handler;

import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import uk.gov.hmcts.reform.finrem.caseorchestration.ccd.callback.CallbackType;
import uk.gov.hmcts.reform.finrem.caseorchestration.controllers.GenericAboutToStartOrSubmitCallbackResponse;
import uk.gov.hmcts.reform.finrem.caseorchestration.mapper.FinremCaseDetailsMapper;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.EventType;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.CaseType;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.FinremCaseDataContested;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.YesOrNo;
import uk.gov.hmcts.reform.finrem.caseorchestration.service.PartyService;


@Slf4j
@Service
public class ListForHearingContestedAboutToStartHandler extends FinremCallbackHandler<FinremCaseDataContested> {
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
    public GenericAboutToStartOrSubmitCallbackResponse<FinremCaseDataContested> handle(
        FinremCallbackRequest<FinremCaseDataContested> callbackRequest, String userAuthorisation) {
        log.info("Handling contested event {} about to start callback for case id: {}",
            EventType.LIST_FOR_HEARING, callbackRequest.getCaseDetails().getId());

        FinremCaseDataContested caseData = callbackRequest.getCaseDetails().getData();
        if (caseData.getAdditionalHearingDocumentsOption() == null) {
            caseData.setAdditionalHearingDocumentsOption(YesOrNo.NO);
        }
        caseData.setPartiesOnCase(partyService.getAllActivePartyList(callbackRequest.getCaseDetails()));
        return GenericAboutToStartOrSubmitCallbackResponse.<FinremCaseDataContested>builder().data(caseData).build();
    }
}
