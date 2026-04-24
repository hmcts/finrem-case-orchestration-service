package uk.gov.hmcts.reform.finrem.caseorchestration.handler;

import lombok.extern.slf4j.Slf4j;
import uk.gov.hmcts.reform.finrem.caseorchestration.ccd.callback.CallbackType;
import uk.gov.hmcts.reform.finrem.caseorchestration.controllers.GenericAboutToStartOrSubmitCallbackResponse;
import uk.gov.hmcts.reform.finrem.caseorchestration.mapper.FinremCaseDetailsMapper;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.EventType;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.AccessCodeCollection;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.CaseType;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.FinremCaseData;
import uk.gov.hmcts.reform.finrem.caseorchestration.service.InvalidateAccessCodeService;

import java.util.List;

import static org.apache.commons.collections4.ListUtils.emptyIfNull;

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

        FinremCaseData data = callbackRequest.getFinremCaseData();
        FinremCaseData dataBefore = callbackRequest.getFinremCaseDataBefore();

        List<AccessCodeCollection> merged =
            invalidateAccessCodeService.mergeForInvalidation(
                emptyIfNull(getAccessCodes(dataBefore)),
                emptyIfNull(getAccessCodes(data))
            );

        setAccessCodes(data, merged);

        return response(data);
    }

    protected abstract EventType handledEventType();

    protected abstract List<AccessCodeCollection> getAccessCodes(FinremCaseData data);

    protected abstract void setAccessCodes(FinremCaseData data, List<AccessCodeCollection> accessCodes);
}
