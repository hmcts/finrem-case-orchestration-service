package uk.gov.hmcts.reform.finrem.caseorchestration.handler;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import uk.gov.hmcts.reform.ccd.client.model.AboutToStartOrSubmitCallbackResponse;
import uk.gov.hmcts.reform.ccd.client.model.CallbackRequest;
import uk.gov.hmcts.reform.ccd.client.model.CaseDetails;
import uk.gov.hmcts.reform.finrem.caseorchestration.ccd.callback.CallbackType;
import uk.gov.hmcts.reform.finrem.caseorchestration.helper.ConsentedApplicationHelper;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.CaseType;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.EventType;
import uk.gov.hmcts.reform.finrem.caseorchestration.service.CaseManagementLocationService;

import java.util.Map;

import static uk.gov.hmcts.reform.finrem.caseorchestration.OrchestrationConstants.CV_LOWERCASE_LABEL_FIELD;
import static uk.gov.hmcts.reform.finrem.caseorchestration.OrchestrationConstants.CV_ORDER_CAMELCASE_LABEL_FIELD;
import static uk.gov.hmcts.reform.finrem.caseorchestration.OrchestrationConstants.CV_OTHER_DOC_LABEL_FIELD;

@Slf4j
@Service
@RequiredArgsConstructor
public class SolicitorCreateConsentedMidHandler implements CallbackHandler {

    private final ConsentedApplicationHelper consentedApplicationHelper;
    private final CaseManagementLocationService caseManagementLocationService;

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

        consentedApplicationHelper.setConsentVariationOrderLabelField(caseData);

        String camelCaseLabel = (String) caseData.get(CV_ORDER_CAMELCASE_LABEL_FIELD);
        String lowerCaseLabel = (String) caseData.get(CV_LOWERCASE_LABEL_FIELD);
        String otherCaseLabel = (String) caseData.get(CV_OTHER_DOC_LABEL_FIELD);

        log.info("Camelcase label '{}', lowercase label '{}' and other label '{}'", camelCaseLabel, lowerCaseLabel, otherCaseLabel);
        return caseManagementLocationService.setCaseManagementLocation(callbackRequest);
    }
}
