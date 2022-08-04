package uk.gov.hmcts.reform.finrem.caseorchestration.handler;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import uk.gov.hmcts.reform.finrem.caseorchestration.helper.ConsentedApplicationHelper;
import uk.gov.hmcts.reform.finrem.ccd.callback.AboutToStartOrSubmitCallbackResponse;
import uk.gov.hmcts.reform.finrem.ccd.callback.CallbackRequest;
import uk.gov.hmcts.reform.finrem.ccd.callback.CallbackType;
import uk.gov.hmcts.reform.finrem.ccd.domain.CaseType;
import uk.gov.hmcts.reform.finrem.ccd.domain.EventType;
import uk.gov.hmcts.reform.finrem.ccd.domain.FinremCaseData;
import uk.gov.hmcts.reform.finrem.ccd.domain.FinremCaseDetails;

@Slf4j
@Service
@RequiredArgsConstructor
public class SolicitorCreateConsentedMidHandler implements CallbackHandler {

    private final ConsentedApplicationHelper contsentedApplicationHelper;

    @Override
    public boolean canHandle(CallbackType callbackType, CaseType caseType, EventType eventType) {
        return CallbackType.MID_EVENT.equals(callbackType)
            && CaseType.CONSENTED.equals(caseType)
            && (EventType.SOLICITOR_CREATE.equals(eventType)
            || EventType.AMEND_APPLICATION_DETAILS.equals(eventType));
    }

    @Override
    public AboutToStartOrSubmitCallbackResponse handle(CallbackRequest callbackRequest,
                                                       String userAuthorisation) {

        FinremCaseDetails caseDetails = callbackRequest.getCaseDetails();
        FinremCaseData caseData = caseDetails.getCaseData();

        contsentedApplicationHelper.setConsentVariationOrderLabelField(caseData);

        String camelCaseLabel = caseData.getConsentOrderWrapper().getConsentVariationOrderLabelC();
        String lowerCaseLabel = caseData.getConsentOrderWrapper().getConsentVariationOrderLabelL();
        String otherCaseLabel = caseData.getConsentOrderWrapper().getOtherDocLabel();

        log.info("Camelcase label '{}', lowercase label '{}' and other label '{}'", camelCaseLabel, lowerCaseLabel, otherCaseLabel);
        return AboutToStartOrSubmitCallbackResponse.builder().data(caseData).build();
    }
}
