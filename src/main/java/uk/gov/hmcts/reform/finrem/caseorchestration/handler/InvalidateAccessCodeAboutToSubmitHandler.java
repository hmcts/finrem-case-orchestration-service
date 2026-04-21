package uk.gov.hmcts.reform.finrem.caseorchestration.handler;

import lombok.extern.slf4j.Slf4j;
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
public abstract class InvalidateAccessCodeAboutToSubmitHandler extends FinremCallbackHandler {

    protected final InvalidateAccessCodeService invalidateAccessCodeService;

    protected InvalidateAccessCodeAboutToSubmitHandler(FinremCaseDetailsMapper finremCaseDetailsMapper,
                                                       InvalidateAccessCodeService invalidateAccessCodeService) {
        super(finremCaseDetailsMapper);
        this.invalidateAccessCodeService = invalidateAccessCodeService;
    }

    @Override
    public boolean canHandle(CallbackType callbackType,
                             CaseType caseType,
                             EventType eventType) {
        return CallbackType.ABOUT_TO_SUBMIT.equals(callbackType)
            && CaseType.CONTESTED.equals(caseType)
            && handledEventType().equals(eventType);
    }

    @Override
    public GenericAboutToStartOrSubmitCallbackResponse<FinremCaseData> handle(FinremCallbackRequest callbackRequest,
                                                                              String userAuthorisation) {
        log.info(CallbackHandlerLogger.aboutToSubmit(callbackRequest));

        FinremCaseDetails details = callbackRequest.getCaseDetails();
        FinremCaseDetails detailsBefore = callbackRequest.getCaseDetailsBefore();

        FinremCaseData data = details.getData();
        FinremCaseData dataBefore = detailsBefore.getData();

        List<AccessCodeCollection> merged =
            invalidateAccessCodeService.mergeForInvalidation(
                ofNullable(getAccessCodes(dataBefore)).orElse(List.of()),
                ofNullable(getAccessCodes(data)).orElse(List.of())
            );

        setAccessCodes(data, merged);

        return GenericAboutToStartOrSubmitCallbackResponse.<FinremCaseData>builder()
            .data(data)
            .build();
    }

    protected abstract EventType handledEventType();

    protected abstract List<AccessCodeCollection> getAccessCodes(FinremCaseData data);

    protected abstract void setAccessCodes(FinremCaseData data, List<AccessCodeCollection> accessCodes);
}
