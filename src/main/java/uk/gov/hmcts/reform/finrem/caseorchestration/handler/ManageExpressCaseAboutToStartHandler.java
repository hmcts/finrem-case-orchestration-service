package uk.gov.hmcts.reform.finrem.caseorchestration.handler;

import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import uk.gov.hmcts.reform.finrem.caseorchestration.ccd.callback.CallbackType;
import uk.gov.hmcts.reform.finrem.caseorchestration.controllers.GenericAboutToStartOrSubmitCallbackResponse;
import uk.gov.hmcts.reform.finrem.caseorchestration.mapper.FinremCaseDetailsMapper;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.EventType;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.CaseType;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.DynamicMultiSelectList;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.DynamicMultiSelectListElement;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.FinremCaseData;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.FinremCaseDetails;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.YesOrNo;

import java.util.List;

import static uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.ExpressCaseParticipation.ENROLLED;

@Slf4j
@Service
public class ManageExpressCaseAboutToStartHandler extends FinremCallbackHandler {

    public ManageExpressCaseAboutToStartHandler(FinremCaseDetailsMapper finremCaseDetailsMapper) {
        super(finremCaseDetailsMapper);
    }

    @Override
    public boolean canHandle(CallbackType callbackType, CaseType caseType, EventType eventType) {
        return CallbackType.ABOUT_TO_START.equals(callbackType)
            && CaseType.CONTESTED.equals(caseType)
            && EventType.MANAGE_EXPRESS_CASE.equals(eventType);
    }

    @Override
    public GenericAboutToStartOrSubmitCallbackResponse<FinremCaseData> handle(FinremCallbackRequest callbackRequest,
                                                                              String userAuthorisation) {
        log.info(CallbackHandlerLogger.aboutToStart(callbackRequest));
        FinremCaseDetails caseDetails = callbackRequest.getCaseDetails();
        FinremCaseData caseData = caseDetails.getData();

        caseData.getExpressCaseWrapper().setConfirmRemoveCaseFromExpressPilot(buildConfirmRemoveCaseFromExpressPilotEntry());
        caseData.getExpressCaseWrapper().setExpressPilotQuestion(getDefaultAnswerForExpressPilotQuestion(caseData));

        return GenericAboutToStartOrSubmitCallbackResponse.<FinremCaseData>builder().data(caseData).build();
    }

    private YesOrNo getDefaultAnswerForExpressPilotQuestion(FinremCaseData caseData) {
        return YesOrNo.forValue(ENROLLED == caseData.getExpressCaseWrapper().getExpressCaseParticipation());
    }

    private DynamicMultiSelectList buildConfirmRemoveCaseFromExpressPilotEntry() {
        return DynamicMultiSelectList.builder().listItems(List.of(DynamicMultiSelectListElement.builder()
            .code(YesOrNo.YES.getYesOrNo())
            .label("Confirm that this case should no longer be in the Express Financial Remedy Pilot")
            .build())).build();
    }
}
