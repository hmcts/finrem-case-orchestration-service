package uk.gov.hmcts.reform.finrem.caseorchestration.handler;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import uk.gov.hmcts.reform.ccd.client.model.AboutToStartOrSubmitCallbackResponse;
import uk.gov.hmcts.reform.ccd.client.model.CallbackRequest;
import uk.gov.hmcts.reform.ccd.client.model.CaseDetails;
import uk.gov.hmcts.reform.finrem.caseorchestration.ccd.callback.CallbackType;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.CaseType;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.EventType;

import java.util.List;
import java.util.Map;

import static java.util.Objects.nonNull;
import static uk.gov.hmcts.reform.finrem.caseorchestration.OrchestrationConstants.CONSENT_ORDER_CAMELCASE_LABEL_VALUE;
import static uk.gov.hmcts.reform.finrem.caseorchestration.OrchestrationConstants.CONSENT_ORDER_LOWERCASE_LABEL_VALUE;
import static uk.gov.hmcts.reform.finrem.caseorchestration.OrchestrationConstants.CONSENT_OTHER_DOC_LABEL_VALUE;
import static uk.gov.hmcts.reform.finrem.caseorchestration.OrchestrationConstants.CV_LOWERCASE_LABEL_FIELD;
import static uk.gov.hmcts.reform.finrem.caseorchestration.OrchestrationConstants.CV_ORDER_CAMELCASE_LABEL_FIELD;
import static uk.gov.hmcts.reform.finrem.caseorchestration.OrchestrationConstants.CV_OTHER_DOC_LABEL_FIELD;
import static uk.gov.hmcts.reform.finrem.caseorchestration.OrchestrationConstants.CV_OTHER_DOC_LABEL_VALUE;
import static uk.gov.hmcts.reform.finrem.caseorchestration.OrchestrationConstants.VARIATION_ORDER_CAMELCASE_LABEL_VALUE;
import static uk.gov.hmcts.reform.finrem.caseorchestration.OrchestrationConstants.VARIATION_ORDER_LOWERCASE_LABEL_VALUE;

@Slf4j
@Service
@RequiredArgsConstructor
public class SolicitorCreateConsentedMidHandler implements CallbackHandler {

    @Override
    public boolean canHandle(CallbackType callbackType, CaseType caseType, EventType eventType) {
        return CallbackType.MID_EVENT.equals(callbackType)
            && CaseType.CONSENTED.equals(caseType)
            && (EventType.SOLICITOR_CREATE.equals(eventType)
            || EventType.AMEND_APP_DETAILS.equals(eventType));
    }

    @Override
    public AboutToStartOrSubmitCallbackResponse handle(CallbackRequest callbackRequest,
                                                       String userAuthorisation) {

        CaseDetails caseDetails = callbackRequest.getCaseDetails();
        Map<String, Object> caseData = caseDetails.getData();

        setConsentVariationOrderLabelField(caseData);

        return AboutToStartOrSubmitCallbackResponse.builder().data(caseData).build();
    }

    private void setConsentVariationOrderLabelField(Map<String, Object> caseData) {
        List<String> natureOfApplicationList = (List<String>) caseData.get("natureOfApplication2");
        if (nonNull(natureOfApplicationList) && natureOfApplicationList.contains("Variation Order")) {
            caseData.put(CV_ORDER_CAMELCASE_LABEL_FIELD, VARIATION_ORDER_CAMELCASE_LABEL_VALUE);
            caseData.put(CV_LOWERCASE_LABEL_FIELD, VARIATION_ORDER_LOWERCASE_LABEL_VALUE);
            caseData.put(CV_OTHER_DOC_LABEL_FIELD, CV_OTHER_DOC_LABEL_VALUE);
        } else {
            caseData.put(CV_ORDER_CAMELCASE_LABEL_FIELD, CONSENT_ORDER_CAMELCASE_LABEL_VALUE);
            caseData.put(CV_LOWERCASE_LABEL_FIELD, CONSENT_ORDER_LOWERCASE_LABEL_VALUE);
            caseData.put(CV_OTHER_DOC_LABEL_FIELD, CONSENT_OTHER_DOC_LABEL_VALUE);
        }
    }
}
