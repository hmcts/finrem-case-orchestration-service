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
import uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.YesOrNo;
import uk.gov.hmcts.reform.finrem.caseorchestration.service.express.ExpressCaseService;

import java.util.List;

import static uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.ExpressCaseParticipation.ENROLLED;

@Slf4j
@Service
public class ManageExpressCaseAboutToStartHandler extends FinremCallbackHandler {

    private final ExpressCaseService expressCaseService;

    public ManageExpressCaseAboutToStartHandler(FinremCaseDetailsMapper finremCaseDetailsMapper,
                                                ExpressCaseService expressCaseService) {
        super(finremCaseDetailsMapper);
        this.expressCaseService = expressCaseService;
    }

    @Override
    public boolean canHandle(CallbackType callbackType, CaseType caseType, EventType eventType) {
        return CallbackType.ABOUT_TO_START.equals(callbackType)
            && CaseType.CONTESTED.equals(caseType)
            && List.of(EventType.MANAGE_EXPRESS_CASE, EventType.MANAGE_EXPRESS_CASE_V2).contains(eventType);
    }

    @Override
    public GenericAboutToStartOrSubmitCallbackResponse<FinremCaseData> handle(FinremCallbackRequest callbackRequest,
                                                                              String userAuthorisation) {
        log.info(CallbackHandlerLogger.aboutToStart(callbackRequest));
        FinremCaseData caseData = callbackRequest.getFinremCaseData();

        caseData.getExpressCaseWrapper().setConfirmRemoveCaseFromExpressPilot(buildConfirmRemoveCaseFromExpressPilotEntry());

        if (v2EventResponseNeeded(callbackRequest)) {
            return v2EventResponse(caseData);
        }

        return v1EventResponse(caseData);
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

    private boolean v2EventResponseNeeded(FinremCallbackRequest callbackRequest) {
        return !EventType.MANAGE_EXPRESS_CASE.equals(callbackRequest.getEventType());
    }

    private GenericAboutToStartOrSubmitCallbackResponse<FinremCaseData> v2EventResponse(FinremCaseData caseData) {
        if (notEnrolled(caseData)) {
            boolean canSetExpressPilotStatus = expressCaseService.canSetExpressPilotStatus(caseData, false);
            if (!canSetExpressPilotStatus) {
                return response(caseData, null,
                    List.of("This case is not enrolled in the Express Financial Remedy Pilot and does meet the criteria to be enrolled"));
            }
        }
        return response(caseData);
    }

    private boolean notEnrolled(FinremCaseData caseData) {
        return !expressCaseService.isExpressCase(caseData);
    }

    private GenericAboutToStartOrSubmitCallbackResponse<FinremCaseData> v1EventResponse(FinremCaseData caseData) {
        caseData.getExpressCaseWrapper().setExpressPilotQuestion(getDefaultAnswerForExpressPilotQuestion(caseData));
        return response(caseData);
    }
}
