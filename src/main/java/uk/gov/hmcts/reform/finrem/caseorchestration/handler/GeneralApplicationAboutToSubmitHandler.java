package uk.gov.hmcts.reform.finrem.caseorchestration.handler;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import uk.gov.hmcts.reform.ccd.client.model.AboutToStartOrSubmitCallbackResponse;
import uk.gov.hmcts.reform.ccd.client.model.CallbackRequest;
import uk.gov.hmcts.reform.ccd.client.model.CaseDetails;
import uk.gov.hmcts.reform.finrem.caseorchestration.ccd.callback.CallbackType;
import uk.gov.hmcts.reform.finrem.caseorchestration.helper.GeneralApplicationHelper;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.CaseType;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.EventType;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.GeneralApplicationData;
import uk.gov.hmcts.reform.finrem.caseorchestration.service.GeneralApplicationService;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Optional;

import static uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.CCDConfigConstant.GENERAL_APPLICATION_CREATED_BY;
import static uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.CCDConfigConstant.GENERAL_APPLICATION_DOCUMENT;
import static uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.CCDConfigConstant.GENERAL_APPLICATION_DOCUMENT_COLLECTION;
import static uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.CCDConfigConstant.GENERAL_APPLICATION_DOCUMENT_LATEST;
import static uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.CCDConfigConstant.GENERAL_APPLICATION_DOCUMENT_LATEST_DATE;
import static uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.CCDConfigConstant.GENERAL_APPLICATION_DRAFT_ORDER;
import static uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.CCDConfigConstant.GENERAL_APPLICATION_HEARING_REQUIRED;
import static uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.CCDConfigConstant.GENERAL_APPLICATION_RECEIVED_FROM;
import static uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.CCDConfigConstant.GENERAL_APPLICATION_SPECIAL_MEASURES;
import static uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.CCDConfigConstant.GENERAL_APPLICATION_TIME_ESTIMATE;
import static uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.CCDConfigConstant.GENERAL_APPLICATION_TRACKING;

@Slf4j
@Service
@RequiredArgsConstructor
public class GeneralApplicationAboutToSubmitHandler implements CallbackHandler {

    private final GeneralApplicationService service;
    private final GeneralApplicationHelper helper;

    @Override
    public boolean canHandle(CallbackType callbackType, CaseType caseType, EventType eventType) {
        return CallbackType.ABOUT_TO_SUBMIT.equals(callbackType)
            && CaseType.CONTESTED.equals(caseType)
            && EventType.GENERAL_APPLICATION.equals(eventType);
    }

    @Override
    public AboutToStartOrSubmitCallbackResponse handle(CallbackRequest callbackRequest,
                                                       String userAuthorisation) {
        CaseDetails caseDetails = callbackRequest.getCaseDetails();
        log.info("Received request to submit general application from Handler for Case ID: {}", caseDetails.getId());

        Map<String, Object> caseData
            = service.updateGeneralApplications(callbackRequest, userAuthorisation);

        log.info("Delete non collection general application casedata for Case ID: {}", caseDetails.getId());
        deleteNonCollectionGeneralApplication(caseData);

        return AboutToStartOrSubmitCallbackResponse.builder().data(caseData).build();
    }

    private void deleteNonCollectionGeneralApplication(Map<String, Object> caseData) {
        if (caseData.get(GENERAL_APPLICATION_CREATED_BY) != null) {
            caseData.remove(GENERAL_APPLICATION_RECEIVED_FROM);
            caseData.remove(GENERAL_APPLICATION_CREATED_BY);
            caseData.remove(GENERAL_APPLICATION_HEARING_REQUIRED);
            caseData.remove(GENERAL_APPLICATION_TIME_ESTIMATE);
            caseData.remove(GENERAL_APPLICATION_SPECIAL_MEASURES);
            caseData.remove(GENERAL_APPLICATION_DOCUMENT);
            caseData.remove(GENERAL_APPLICATION_DRAFT_ORDER);
            caseData.remove(GENERAL_APPLICATION_TRACKING);

            List<GeneralApplicationData> generalApplicationList
                = Optional.ofNullable(caseData.get(GENERAL_APPLICATION_DOCUMENT_COLLECTION))
                .map(helper::convertToGeneralApplicationDataList)
                .orElse(new ArrayList<>());

            if (generalApplicationList.size() == 1) {
                caseData.remove(GENERAL_APPLICATION_DOCUMENT_COLLECTION);
                caseData.remove(GENERAL_APPLICATION_DOCUMENT_LATEST_DATE);
                caseData.remove(GENERAL_APPLICATION_DOCUMENT_LATEST);
            }
        }
    }

}
