package uk.gov.hmcts.reform.finrem.caseorchestration.handler;

import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import uk.gov.hmcts.reform.finrem.caseorchestration.ccd.callback.CallbackType;
import uk.gov.hmcts.reform.finrem.caseorchestration.controllers.GenericAboutToStartOrSubmitCallbackResponse;
import uk.gov.hmcts.reform.finrem.caseorchestration.mapper.FinremCaseDetailsMapper;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.EventType;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.AccessCodeCollection;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.CaseType;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.FinremCaseData;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.FinremCaseDetails;
import uk.gov.hmcts.reform.finrem.caseorchestration.service.InvalidateAccessCodeService;

import java.util.List;

import static java.util.Optional.ofNullable;

@Slf4j
@Service
public class InvalidateRespondentAccessCodeAboutToSubmitHandler extends FinremCallbackHandler {

    private final InvalidateAccessCodeService invalidateAccessCodeService;

    public InvalidateRespondentAccessCodeAboutToSubmitHandler(FinremCaseDetailsMapper finremCaseDetailsMapper, InvalidateAccessCodeService invalidateAccessCodeService) {
        super(finremCaseDetailsMapper);
        this.invalidateAccessCodeService = invalidateAccessCodeService;
    }

    @Override
    public boolean canHandle(CallbackType callbackType, CaseType caseType, EventType eventType) {
        return CallbackType.ABOUT_TO_SUBMIT.equals(callbackType)
            && CaseType.CONTESTED.equals(caseType)
            && EventType.INVALIDATE_RESPONDENT_ACCESS_CODE.equals(eventType);
    }

    @Override
    public GenericAboutToStartOrSubmitCallbackResponse<FinremCaseData> handle(
        FinremCallbackRequest callbackRequest,
        String userAuthorisation) {

        log.info(CallbackHandlerLogger.aboutToSubmit(callbackRequest));

        FinremCaseDetails finremCaseDetails = callbackRequest.getCaseDetails();
        FinremCaseDetails finremCaseDetailsBefore = callbackRequest.getCaseDetailsBefore();

        FinremCaseData caseData = finremCaseDetails.getData();
        FinremCaseData caseDataBefore = finremCaseDetailsBefore.getData();


        List<AccessCodeCollection> merged =
            invalidateAccessCodeService.mergeForInvalidation(
                ofNullable(caseDataBefore.getRespondentAccessCodes()).orElse(List.of()),
                ofNullable(caseData.getRespondentAccessCodes()).orElse(List.of())
            );

        caseData.setRespondentAccessCodes(merged);

        return GenericAboutToStartOrSubmitCallbackResponse.<FinremCaseData>builder()
            .data(caseData)
            .build();
    }
}