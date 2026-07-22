package uk.gov.hmcts.reform.finrem.caseorchestration.handler;

import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import uk.gov.hmcts.reform.finrem.caseorchestration.ccd.callback.CallbackType;
import uk.gov.hmcts.reform.finrem.caseorchestration.controllers.GenericAboutToStartOrSubmitCallbackResponse;
import uk.gov.hmcts.reform.finrem.caseorchestration.mapper.FinremCaseDetailsMapper;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.EventType;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.CaseType;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.DynamicMultiSelectListElement;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.ExpressCaseParticipation;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.FinremCaseData;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.YesOrNo;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.wrapper.ExpressCaseWrapper;
import uk.gov.hmcts.reform.finrem.caseorchestration.service.express.ExpressCaseService;

import java.util.List;

@Slf4j
@Service
public class ManageExpressCaseAboutToSubmitHandler extends FinremAboutToSubmitCallbackHandler {

    private final ExpressCaseService expressCaseService;

    public ManageExpressCaseAboutToSubmitHandler(FinremCaseDetailsMapper finremCaseDetailsMapper,
                                                 ExpressCaseService expressCaseService) {
        super(finremCaseDetailsMapper);
        this.expressCaseService = expressCaseService;
    }

    @Override
    public boolean canHandle(CallbackType callbackType, CaseType caseType, EventType eventType) {
        return CallbackType.ABOUT_TO_SUBMIT.equals(callbackType)
            && CaseType.CONTESTED.equals(caseType)
            && List.of(EventType.MANAGE_EXPRESS_CASE, EventType.MANAGE_EXPRESS_CASE_V2).contains(eventType);
    }

    @Override
    public GenericAboutToStartOrSubmitCallbackResponse<FinremCaseData> handle(FinremCallbackRequest callbackRequest, String userAuthorisation) {
        log.info(CallbackHandlerLogger.aboutToSubmit(callbackRequest));
        FinremCaseData caseData = callbackRequest.getFinremCaseData();
        ExpressCaseWrapper expressPilotWrapper = caseData.getExpressCaseWrapper();

        // Keeping legacy logic
        if (EventType.MANAGE_EXPRESS_CASE.equals(callbackRequest.getEventType())) {
            if (ExpressCaseParticipation.ENROLLED.equals(expressPilotWrapper.getExpressCaseParticipation())
                && YesOrNo.isNo(expressPilotWrapper.getExpressPilotQuestion())
                && isUserConfirmed(expressPilotWrapper)) {
                expressCaseService.setExpressCaseEnrollmentStatusToWithdrawn(caseData);
            }
        } else { // V2
            setExpressPilotStatus(caseData);
        }

        return response(caseData);
    }

    private boolean isUserConfirmed(ExpressCaseWrapper expressPilotWrapper) {
        return expressPilotWrapper.getConfirmRemoveCaseFromExpressPilot().getValue().stream().map(DynamicMultiSelectListElement::getCode)
            .anyMatch(YesOrNo::isYes);
    }

    private void setExpressPilotStatus(FinremCaseData finremCaseData) {
        ExpressCaseWrapper expressCaseWrapper = finremCaseData.getExpressCaseWrapper();
        if (YesOrNo.isYes(expressCaseWrapper.getShouldAllocateToExpressPilot())) {
            expressCaseService.setExpressCaseEnrollmentStatus(finremCaseData);
            log.info("{} - Setting express case enrollment status to {}", finremCaseData.getCcdCaseId(),
                expressCaseWrapper.getExpressCaseParticipation());
        }
    }
}
