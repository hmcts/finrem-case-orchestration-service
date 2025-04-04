package uk.gov.hmcts.reform.finrem.caseorchestration.handler;

import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.springframework.stereotype.Service;
import uk.gov.hmcts.reform.finrem.caseorchestration.ccd.callback.CallbackType;
import uk.gov.hmcts.reform.finrem.caseorchestration.controllers.GenericAboutToStartOrSubmitCallbackResponse;
import uk.gov.hmcts.reform.finrem.caseorchestration.mapper.FinremCaseDetailsMapper;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.EventType;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.CaseType;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.FinremCaseData;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.FinremCaseDetails;
import uk.gov.hmcts.reform.finrem.caseorchestration.service.NotificationService;

@Slf4j
@Service
public class GeneralApplicationReferToJudgeSubmittedHandler extends FinremCallbackHandler {

    private final NotificationService notificationService;

    public GeneralApplicationReferToJudgeSubmittedHandler(FinremCaseDetailsMapper finremCaseDetailsMapper,
                                                          NotificationService notificationService) {
        super(finremCaseDetailsMapper);
        this.notificationService = notificationService;
    }

    @Override
    public boolean canHandle(CallbackType callbackType, CaseType caseType, EventType eventType) {
        return CallbackType.SUBMITTED.equals(callbackType)
            && CaseType.CONTESTED.equals(caseType)
            && EventType.GENERAL_APPLICATION_REFER_TO_JUDGE.equals(eventType);
    }

    @Override
    public GenericAboutToStartOrSubmitCallbackResponse<FinremCaseData> handle(FinremCallbackRequest callbackRequest,
                                                                              String userAuthorisation) {
        CallbackHandlerLogger.submitted(callbackRequest);

        FinremCaseDetails caseDetails = callbackRequest.getCaseDetails();
        if (isJudgeEmailPresent(caseDetails.getData())) {
            notificationService.sendContestedGeneralApplicationReferToJudgeEmail(caseDetails);
        }

        return GenericAboutToStartOrSubmitCallbackResponse
            .<FinremCaseData>builder()
            .data(caseDetails.getData())
            .build();
    }

    private boolean isJudgeEmailPresent(FinremCaseData caseData) {
        return StringUtils.isNotEmpty(caseData.getGeneralApplicationWrapper().getGeneralApplicationReferToJudgeEmail());
    }
}

