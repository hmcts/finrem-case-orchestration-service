package uk.gov.hmcts.reform.finrem.caseorchestration.handler;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.springframework.stereotype.Service;
import uk.gov.hmcts.reform.ccd.client.model.CallbackRequest;
import uk.gov.hmcts.reform.ccd.client.model.CaseDetails;
import uk.gov.hmcts.reform.finrem.caseorchestration.ccd.callback.CallbackType;
import uk.gov.hmcts.reform.finrem.caseorchestration.controllers.GenericAboutToStartOrSubmitCallbackResponse;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.EventType;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.CaseType;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.HearingTypeDirection;
import uk.gov.hmcts.reform.finrem.caseorchestration.service.HearingDocumentService;

import java.util.Collections;
import java.util.Map;

import static uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.CCDConfigConstant.HEARING_DATE;
import static uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.CCDConfigConstant.HEARING_TYPE;


@Slf4j
@Service
@RequiredArgsConstructor
@SuppressWarnings("java:S3740")
public class ReGenerateFormCAboutToSubmitHandler implements CallbackHandler {

    public static final String THERE_IS_NO_HEARING_ON_THE_CASE_ERROR_MESSAGE = "There is no hearing on the case.";
    private static final String NO_FDA_HEARING_TYPE =
        "Form C can only be regenerated with First Directions Appointment Hearings.";

    private final HearingDocumentService hearingDocumentService;

    @Override
    public boolean canHandle(CallbackType callbackType, CaseType caseType, EventType eventType) {
        return CallbackType.ABOUT_TO_SUBMIT.equals(callbackType)
            && CaseType.CONTESTED.equals(caseType)
            && (EventType.REGENERATE_FORM_C.equals(eventType));
    }

    @Override
    public GenericAboutToStartOrSubmitCallbackResponse<Map<String, Object>> handle(
        CallbackRequest callbackRequest, String userAuthorisation) {

        CaseDetails caseDetails = callbackRequest.getCaseDetails();
        log.info("Received request to re-generate Form C on case with Case ID: {}", caseDetails.getId());

        Map<String, Object> caseData = caseDetails.getData();

        if (isHearingDatePresent(caseData) && isHearingTypeFda(caseData)) {

            caseDetails.getData().putAll(
                hearingDocumentService.generateHearingDocuments(userAuthorisation, caseDetails));

            return GenericAboutToStartOrSubmitCallbackResponse.<Map<String, Object>>builder().data(caseData).build();
        } else if (!isHearingTypeFda(caseData)) {
            return GenericAboutToStartOrSubmitCallbackResponse.<Map<String, Object>>builder().data(caseData)
                .errors(Collections.singletonList(NO_FDA_HEARING_TYPE)).build();
        } else {
            return GenericAboutToStartOrSubmitCallbackResponse.<Map<String, Object>>builder().data(caseData)
                .errors(Collections.singletonList(THERE_IS_NO_HEARING_ON_THE_CASE_ERROR_MESSAGE)).build();
        }
    }

    private boolean isHearingTypeFda(Map<String, Object> caseData) {
        return caseData.containsKey(HEARING_TYPE)
            && StringUtils.isNotBlank(caseData.get(HEARING_TYPE).toString())
            && caseData.get(HEARING_TYPE).equals(HearingTypeDirection.FDA.getId());
    }

    private static boolean isHearingDatePresent(Map<String, Object> caseData) {
        return caseData.containsKey(HEARING_DATE) && StringUtils.isNotBlank(caseData.get(HEARING_DATE).toString());
    }
}
