package uk.gov.hmcts.reform.finrem.caseorchestration.handler;

import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import uk.gov.hmcts.reform.finrem.caseorchestration.ccd.callback.CallbackType;
import uk.gov.hmcts.reform.finrem.caseorchestration.controllers.GenericAboutToStartOrSubmitCallbackResponse;
import uk.gov.hmcts.reform.finrem.caseorchestration.mapper.FinremCaseDetailsMapper;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.EventType;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.CaseType;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.FinremCaseData;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.FinremCaseDataContested;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.FinremCaseDetails;
import uk.gov.hmcts.reform.finrem.caseorchestration.service.GeneralOrderService;
import uk.gov.hmcts.reform.finrem.caseorchestration.service.PartyService;

@Slf4j
@Service
public class SendOrderContestedAboutToStartHandler extends FinremCallbackHandler<FinremCaseDataContested> {

    private final GeneralOrderService generalOrderService;
    private final PartyService partyService;

    public SendOrderContestedAboutToStartHandler(FinremCaseDetailsMapper finremCaseDetailsMapper,
                                                 GeneralOrderService generalOrderService,
                                                 PartyService partyService) {
        super(finremCaseDetailsMapper);
        this.generalOrderService = generalOrderService;
        this.partyService = partyService;
    }

    @Override
    public boolean canHandle(CallbackType callbackType, CaseType caseType, EventType eventType) {
        return CallbackType.ABOUT_TO_START.equals(callbackType)
            && CaseType.CONTESTED.equals(caseType)
            && EventType.SEND_ORDER.equals(eventType);
    }

    @Override
    public GenericAboutToStartOrSubmitCallbackResponse<FinremCaseDataContested> handle(
        FinremCallbackRequest<FinremCaseDataContested> callbackRequest, String userAuthorisation) {
        FinremCaseDetails<FinremCaseDataContested> caseDetails = callbackRequest.getCaseDetails();
        log.info("Invoking contested {} about to start callback for case id: {}",
            callbackRequest.getEventType(), caseDetails.getId());
        FinremCaseDataContested finremCaseData = caseDetails.getData();

        generalOrderService.setOrderList(caseDetails);

        finremCaseData.setPartiesOnCase(partyService.getAllActivePartyList(caseDetails));

        return GenericAboutToStartOrSubmitCallbackResponse.<FinremCaseDataContested>builder()
            .data(finremCaseData).build();
    }
}
