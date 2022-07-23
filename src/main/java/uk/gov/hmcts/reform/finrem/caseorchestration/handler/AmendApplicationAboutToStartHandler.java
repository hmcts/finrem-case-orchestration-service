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

import java.util.List;
import java.util.Map;
import java.util.Objects;

import static uk.gov.hmcts.reform.finrem.caseorchestration.OrchestrationConstants.VARIATION_ORDER;
import static uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.CCDConfigConstant.CONSENTED_NATURE_OF_APPLICATION;
import static uk.gov.hmcts.reform.finrem.caseorchestration.service.bulkscan.OcrFieldName.APPLICANT_INTENDS_TO;

@Slf4j
@Service
@RequiredArgsConstructor
public class AmendApplicationAboutToStartHandler implements CallbackHandler {

    private final ConsentedApplicationHelper helper;

    @Override
    public boolean canHandle(CallbackType callbackType, CaseType caseType, EventType eventType) {
        return CallbackType.ABOUT_TO_START.equals(callbackType)
            && CaseType.CONSENTED.equals(caseType)
            && (EventType.AMEND_APP_DETAILS.equals(eventType));
    }

    @Override
    public AboutToStartOrSubmitCallbackResponse handle(CallbackRequest callbackRequest,
                                                       String userAuthorisation) {
        CaseDetails caseDetails = callbackRequest.getCaseDetails();
        log.info("Received request to set nature of application for consented case with Case ID: {}", caseDetails.getId());

        Map<String, Object> caseData = caseDetails.getData();
        final String intends = Objects.toString(caseData.get(APPLICANT_INTENDS_TO), "");
        log.info("Applicant intends to {} for case id: {}",intends, caseDetails.getId());

        if (intends.equalsIgnoreCase("ApplyToVary"))  {
            log.info("Add applicant intends to {} to nature of application",intends);
            List<String> natureOfApplicationList = helper.getNatureOfApplicationList(caseData);
            natureOfApplicationList.add(VARIATION_ORDER);
            caseData.put(CONSENTED_NATURE_OF_APPLICATION,natureOfApplicationList);
            log.info("paper case {} marked as variation order",caseDetails.getId());
        }

        return AboutToStartOrSubmitCallbackResponse.builder().data(caseData).build();
    }
}
