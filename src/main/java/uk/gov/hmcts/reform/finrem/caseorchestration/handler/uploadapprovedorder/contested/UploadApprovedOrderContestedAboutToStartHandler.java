package uk.gov.hmcts.reform.finrem.caseorchestration.handler.uploadapprovedorder.contested;

import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import uk.gov.hmcts.reform.finrem.caseorchestration.ccd.callback.CallbackType;
import uk.gov.hmcts.reform.finrem.caseorchestration.controllers.GenericAboutToStartOrSubmitCallbackResponse;
import uk.gov.hmcts.reform.finrem.caseorchestration.handler.CallbackHandlerLogger;
import uk.gov.hmcts.reform.finrem.caseorchestration.handler.FinremCallbackHandler;
import uk.gov.hmcts.reform.finrem.caseorchestration.handler.FinremCallbackRequest;
import uk.gov.hmcts.reform.finrem.caseorchestration.mapper.FinremCaseDetailsMapper;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.EventType;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.CaseType;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.DirectionOrderCollection;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.FinremCaseData;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.managehearings.HearingType;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.managehearings.WorkingHearing;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.wrapper.ManageHearingsWrapper;
import uk.gov.hmcts.reform.finrem.caseorchestration.service.PartyService;

import java.util.ArrayList;
import java.util.List;

@Slf4j
@Service
public class UploadApprovedOrderContestedAboutToStartHandler extends FinremCallbackHandler {
    private final PartyService partyService;

    public UploadApprovedOrderContestedAboutToStartHandler(FinremCaseDetailsMapper finremCaseDetailsMapper, PartyService partyService) {
        super(finremCaseDetailsMapper);
        this.partyService = partyService;
    }

    @Override
    public boolean canHandle(CallbackType callbackType, CaseType caseType, EventType eventType) {
        return CallbackType.ABOUT_TO_START.equals(callbackType)
            && CaseType.CONTESTED.equals(caseType)
            && EventType.UPLOAD_APPROVED_ORDER_MH.equals(eventType);
    }

    @Override
    public GenericAboutToStartOrSubmitCallbackResponse<FinremCaseData> handle(FinremCallbackRequest callbackRequest,
                                                                              String userAuthorisation) {
        log.info(CallbackHandlerLogger.aboutToStart(callbackRequest));

        FinremCaseData caseData = callbackRequest.getCaseDetails().getData();
        ManageHearingsWrapper manageHearingsWrapper = caseData.getManageHearingsWrapper();

        manageHearingsWrapper.setWorkingHearing(WorkingHearing
            .builder()
            .partiesOnCaseMultiSelectList(partyService.getAllActivePartyList(caseData))
            .withHearingTypes(HearingType.values())
            .build());

        manageHearingsWrapper.setIsFinalOrder(null);
        manageHearingsWrapper.setIsAddHearingChosen(null);
        caseData.setOrderApprovedJudgeName(null);
        caseData.setOrderApprovedJudgeType(null);
        caseData.setOrderApprovedDate(null);
        caseData.setHearingNoticeDocumentPack(new ArrayList<>());
        prepareCwApprovedOrderCollection(caseData);

        return GenericAboutToStartOrSubmitCallbackResponse.<FinremCaseData>builder()
            .data(caseData).build();
    }

    private void prepareCwApprovedOrderCollection(FinremCaseData finremCaseData) {
        // Create an empty object to save the user from clicking the “Add New” button.
        finremCaseData.getDraftDirectionWrapper().setCwApprovedOrderCollection(List.of(
            DirectionOrderCollection.EMPTY_COLLECTION
        ));
    }
}
