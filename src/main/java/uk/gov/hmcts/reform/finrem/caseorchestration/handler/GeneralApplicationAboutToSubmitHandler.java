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
import uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.GeneralApplicationCollectionData;

import java.util.List;
import java.util.Map;

import static uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.CCDConfigConstant.GENERAL_APPLICATION_COLLECTION;
import static uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.CCDConfigConstant.GENERAL_APPLICATION_CREATED_BY;
import static uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.CCDConfigConstant.GENERAL_APPLICATION_DOCUMENT;
import static uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.CCDConfigConstant.GENERAL_APPLICATION_DOCUMENT_LATEST_DATE;
import static uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.CCDConfigConstant.GENERAL_APPLICATION_DRAFT_ORDER;
import static uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.CCDConfigConstant.GENERAL_APPLICATION_HEARING_REQUIRED;
import static uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.CCDConfigConstant.GENERAL_APPLICATION_RECEIVED_FROM;
import static uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.CCDConfigConstant.GENERAL_APPLICATION_SPECIAL_MEASURES;
import static uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.CCDConfigConstant.GENERAL_APPLICATION_TIME_ESTIMATE;

@Slf4j
@Service
@RequiredArgsConstructor
public class GeneralApplicationAboutToSubmitHandler implements CallbackHandler {

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

        deleteNonCollectionGeneralApplication(caseDetails.getData());

        CaseDetails caseDetailsBefore = callbackRequest.getCaseDetailsBefore();
        log.info("Received request to start general application for Case ID: {}", caseDetails.getId());

        List<GeneralApplicationCollectionData> generalApplicationListBefore = helper.getGeneralApplicationList(caseDetailsBefore.getData());
        log.info("generalApplicationListBefore : {}", generalApplicationListBefore.size());

        List<GeneralApplicationCollectionData> generalApplicationList = helper.getGeneralApplicationList(caseDetails.getData());

        List<GeneralApplicationCollectionData> applicationCollectionDataList = generalApplicationList.stream()
            .sorted((e1, e2) -> e2.getGeneralApplicationItems().getGeneralApplicationCreatedDate()
                .compareTo(e1.getGeneralApplicationItems().getGeneralApplicationCreatedDate()))
            .toList();
        caseDetails.getData().put(GENERAL_APPLICATION_COLLECTION, applicationCollectionDataList);
        log.info("generalApplicationList : {}", generalApplicationList.size());

        return AboutToStartOrSubmitCallbackResponse.builder().data(caseDetails.getData()).build();
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
            caseData.remove(GENERAL_APPLICATION_DOCUMENT_LATEST_DATE);
        }
    }
}
