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
import uk.gov.hmcts.reform.finrem.caseorchestration.service.PartyService;

import java.util.List;

@Slf4j
@Service
public class SendConsentOrderInContestedAboutToStartHandler extends FinremCallbackHandler {

    private final PartyService partyService;

    public SendConsentOrderInContestedAboutToStartHandler(FinremCaseDetailsMapper finremCaseDetailsMapper,
                                                          PartyService partyService) {
        super(finremCaseDetailsMapper);
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

        DynamicMultiSelectList roleList = partyService.getAllActivePartyList(caseDetails);
        roleList.setValue(List.of(roleList.getListItems().get(0), roleList.getListItems().get(1)));
        finremCaseData.setPartiesOnCase(roleList);

        finremCaseData.setAdditionalCicDocuments(null);
        return GenericAboutToStartOrSubmitCallbackResponse.<FinremCaseData>builder()
            .data(finremCaseData).build();
    }

}
