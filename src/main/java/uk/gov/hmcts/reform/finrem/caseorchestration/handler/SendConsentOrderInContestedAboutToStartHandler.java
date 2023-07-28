package uk.gov.hmcts.reform.finrem.caseorchestration.handler;

import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import uk.gov.hmcts.reform.finrem.caseorchestration.ccd.callback.CallbackType;
import uk.gov.hmcts.reform.finrem.caseorchestration.controllers.GenericAboutToStartOrSubmitCallbackResponse;
import uk.gov.hmcts.reform.finrem.caseorchestration.mapper.FinremCaseDetailsMapper;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.EventType;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.CaseType;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.DynamicMultiSelectList;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.FinremCaseData;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.FinremCaseDetails;
import uk.gov.hmcts.reform.finrem.caseorchestration.service.GeneralOrderService;
import uk.gov.hmcts.reform.finrem.caseorchestration.service.PartyService;

@Slf4j
@Service
public class SendConsentOrderInContestedAboutToStartHandler extends FinremCallbackHandler {

    private final GeneralOrderService generalOrderService;
    private final PartyService partyService;

    public SendConsentOrderInContestedAboutToStartHandler(FinremCaseDetailsMapper finremCaseDetailsMapper,
                                                          GeneralOrderService generalOrderService, PartyService partyService) {
        super(finremCaseDetailsMapper);
        this.generalOrderService = generalOrderService;
        this.partyService = partyService;
    }

    @Override
    public boolean canHandle(CallbackType callbackType, CaseType caseType, EventType eventType) {
        return CallbackType.ABOUT_TO_START.equals(callbackType)
            && CaseType.CONTESTED.equals(caseType)
            && EventType.SEND_CONSENT_IN_CONTESTED_ORDER.equals(eventType);
    }

    @Override
    public GenericAboutToStartOrSubmitCallbackResponse<FinremCaseData> handle(FinremCallbackRequest callbackRequest,
                                                                              String userAuthorisation) {
        FinremCaseDetails caseDetails = callbackRequest.getCaseDetails();
        log.info("Invoking contested {} about to start callback for case id: {}",
            callbackRequest.getEventType(), caseDetails.getId());
        FinremCaseData finremCaseData = caseDetails.getData();
        generalOrderService.setConsentInContestedOrderList(caseDetails);

        DynamicMultiSelectList roleList = partyService.getAllActivePartyList(caseDetails);
        finremCaseData.setPartiesOnCase(roleList);

        finremCaseData.setAdditionalConsentInContestedDocument(null);
        return GenericAboutToStartOrSubmitCallbackResponse.<FinremCaseData>builder()
            .data(finremCaseData).build();
    }

}
