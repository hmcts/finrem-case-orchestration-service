package uk.gov.hmcts.reform.finrem.caseorchestration.handler;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.springframework.stereotype.Service;
import uk.gov.hmcts.reform.ccd.client.model.CallbackRequest;
import uk.gov.hmcts.reform.ccd.client.model.CaseDetails;
import uk.gov.hmcts.reform.finrem.caseorchestration.ccd.callback.CallbackType;
import uk.gov.hmcts.reform.finrem.caseorchestration.controllers.GenericAboutToStartOrSubmitCallbackResponse;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.CaseType;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.EventType;
import uk.gov.hmcts.reform.finrem.caseorchestration.service.HearingDocumentService;

import java.util.Collections;
import java.util.Map;

import static uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.CCDConfigConstant.HEARING_DATE;


@Slf4j
@Service
@RequiredArgsConstructor
public class ReGenerateFormCAboutToSubmitHandler implements CallbackHandler {

    public static final String THERE_IS_NO_HEARING_ON_THE_CASE_ERROR_MESSAGE = "There is no hearing on the case";

    private final HearingDocumentService hearingDocumentService;

    @Override
    public boolean canHandle(CallbackType callbackType, CaseType caseType, EventType eventType) {
        return CallbackType.ABOUT_TO_SUBMIT.equals(callbackType)
            && CaseType.CONTESTED.equals(caseType)
            && (EventType.REGENERATE_FORM_C.equals(eventType));
    }

    @Override
    public GenericAboutToStartOrSubmitCallbackResponse<Map<String, Object>> handle(CallbackRequest callbackRequest, String userAuthorisation) {
        CaseDetails caseDetails = callbackRequest.getCaseDetails();
        log.info("Received request to re-generate Form C on case with Case ID: {}", caseDetails.getId());

        Map<String, Object> caseData = caseDetails.getData();

        if (caseData.containsKey(HEARING_DATE) && StringUtils.isNotBlank(caseData.get(HEARING_DATE).toString())) {

            caseDetails.getData().putAll(
                hearingDocumentService.generateHearingDocuments(userAuthorisation, caseDetails));

            return GenericAboutToStartOrSubmitCallbackResponse.<Map<String, Object>>builder().data(caseData).build();
        } else {
            return GenericAboutToStartOrSubmitCallbackResponse.<Map<String, Object>>builder().data(caseData)
                .errors(Collections.singletonList(THERE_IS_NO_HEARING_ON_THE_CASE_ERROR_MESSAGE)).build();
        }
    }
}
