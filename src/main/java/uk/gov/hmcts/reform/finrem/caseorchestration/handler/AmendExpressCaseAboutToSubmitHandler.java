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
import uk.gov.hmcts.reform.finrem.caseorchestration.service.express.ExpressCaseService;

@Slf4j
@Service
public class AmendExpressCaseAboutToSubmitHandler extends FinremCallbackHandler {
    private final ExpressCaseService expressCaseService;

    public AmendExpressCaseAboutToSubmitHandler(FinremCaseDetailsMapper finremCaseDetailsMapper,
                                                ExpressCaseService expressCaseService) {
        super(finremCaseDetailsMapper);
        this.expressCaseService = expressCaseService;
    }

    @Override
    public boolean canHandle(CallbackType callbackType, CaseType caseType, EventType eventType) {
        return CallbackType.ABOUT_TO_SUBMIT.equals(callbackType)
            && CaseType.CONTESTED.equals(caseType)
            && (EventType.AMEND_EXPRESS_CASE.equals(eventType));
    }

    @Override
    public GenericAboutToStartOrSubmitCallbackResponse<FinremCaseData> handle(FinremCallbackRequest callbackRequest,
                                                                              String userAuthorisation) {
        FinremCaseDetails caseDetails = callbackRequest.getCaseDetails();
        FinremCaseData caseData = caseDetails.getData();

        log.info(
                "Requesting that {} is no longer an Express case. {} about to submit callback",
                caseDetails.getId(),
                EventType.AMEND_EXPRESS_CASE
        );

        expressCaseService.setExpressCaseEnrollmentStatusToWithdrawn(caseData);

        return GenericAboutToStartOrSubmitCallbackResponse.<FinremCaseData>builder().data(caseData).build();
    }
}
