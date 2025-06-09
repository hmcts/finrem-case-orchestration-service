package uk.gov.hmcts.reform.finrem.caseorchestration.handler;

import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.springframework.stereotype.Service;
import uk.gov.hmcts.reform.finrem.caseorchestration.ccd.callback.CallbackType;
import uk.gov.hmcts.reform.finrem.caseorchestration.controllers.GenericAboutToStartOrSubmitCallbackResponse;
import uk.gov.hmcts.reform.finrem.caseorchestration.helper.ConsentedHearingHelper;
import uk.gov.hmcts.reform.finrem.caseorchestration.mapper.FinremCaseDetailsMapper;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.EventType;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.CaseType;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.ConsentedHearingDataElement;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.ConsentedHearingDataWrapper;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.FinremCaseData;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.FinremCaseDetails;

import java.util.List;

@Slf4j
@Service
public class ReadyForHearingConsentedAboutToStartHandler extends FinremCallbackHandler {

    private final ConsentedHearingHelper consentedHearingHelper;

    public ReadyForHearingConsentedAboutToStartHandler(FinremCaseDetailsMapper mapper,
                                                       ConsentedHearingHelper consentedHearingHelper) {
        super(mapper);
        this.consentedHearingHelper = consentedHearingHelper;
    }

    @Override
    public boolean canHandle(CallbackType callbackType, CaseType caseType, EventType eventType) {
        return CallbackType.ABOUT_TO_START.equals(callbackType)
            && CaseType.CONSENTED.equals(caseType)
            && EventType.READY_FOR_HEARING.equals(eventType);
    }

    @Override
    public GenericAboutToStartOrSubmitCallbackResponse<FinremCaseData> handle(FinremCallbackRequest callbackRequest,
                                                                              String userAuthorisation) {
        CallbackHandlerLogger.aboutToStart(callbackRequest);

        FinremCaseDetails caseDetails = callbackRequest.getCaseDetails();
        FinremCaseData caseData = caseDetails.getData();

        if (!isHearingDatePresent(caseData)) {
            return GenericAboutToStartOrSubmitCallbackResponse.<FinremCaseData>builder().data(caseData)
                .errors(List.of("There is no hearing on the case.")).build();
        }
        return GenericAboutToStartOrSubmitCallbackResponse.<FinremCaseData>builder().data(caseData).build();
    }

    private boolean isHearingDatePresent(FinremCaseData caseData) {
        return consentedHearingHelper.getHearings(caseData).stream()
            .map(ConsentedHearingDataWrapper::getValue)
            .map(ConsentedHearingDataElement::getHearingDate)
            .anyMatch(StringUtils::isNotBlank);
    }
}
