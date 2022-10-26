package uk.gov.hmcts.reform.finrem.caseorchestration.handler;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import uk.gov.hmcts.reform.ccd.client.model.CallbackRequest;
import uk.gov.hmcts.reform.ccd.client.model.CaseDetails;
import uk.gov.hmcts.reform.finrem.caseorchestration.ccd.callback.CallbackType;
import uk.gov.hmcts.reform.finrem.caseorchestration.controllers.GenericAboutToStartOrSubmitCallbackResponse;
import uk.gov.hmcts.reform.finrem.caseorchestration.helper.GeneralApplicationHelper;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.CaseType;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.EventType;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.GeneralApplicationStatus;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.DynamicList;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.GeneralApplicationCollectionData;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.GeneralApplicationItems;

import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.stream.Collectors;

import static uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.CCDConfigConstant.GENERAL_APPLICATION_COLLECTION;
import static uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.CCDConfigConstant.GENERAL_APPLICATION_CREATED_BY;
import static uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.CCDConfigConstant.GENERAL_APPLICATION_OUTCOME_DECISION;
import static uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.CCDConfigConstant.GENERAL_APPLICATION_OUTCOME_LIST;
import static uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.CCDConfigConstant.GENERAL_APPLICATION_OUTCOME_OTHER;

@Slf4j
@Service
@RequiredArgsConstructor
public class GeneralApplicationOutcomeAboutToSubmitHandler
    implements CallbackHandler<Map<String, Object>> {

    private final GeneralApplicationHelper helper;

    @Override
    public boolean canHandle(CallbackType callbackType, CaseType caseType, EventType eventType) {
        return CallbackType.ABOUT_TO_SUBMIT.equals(callbackType)
            && CaseType.CONTESTED.equals(caseType)
            && EventType.GENERAL_APPLICATION_OUTCOME.equals(eventType);
    }

    @Override
    public GenericAboutToStartOrSubmitCallbackResponse<Map<String, Object>> handle(
        CallbackRequest callbackRequest,
        String userAuthorisation) {
        CaseDetails caseDetails = callbackRequest.getCaseDetails();
        final String caseId = caseDetails.getId().toString();
        log.info("Received on start request to outcome decision general application for Case ID: {}", caseId);
        Map<String, Object> caseData = caseDetails.getData();

        List<GeneralApplicationCollectionData> existingList = helper.getGeneralApplicationList(caseData);
        if (existingList.isEmpty() && caseData.get(GENERAL_APPLICATION_CREATED_BY) != null) {
            log.info("outcome stage migrate existing general application for Case ID: {}", caseId);
            migrateExistingApplication(caseDetails, userAuthorisation);
        } else {
            DynamicList dynamicList = helper.objectToDynamicList(caseData.get(GENERAL_APPLICATION_OUTCOME_LIST));

            final String outcome = Objects.toString(caseData.get(GENERAL_APPLICATION_OUTCOME_DECISION), null);
            log.info("Outcome decision {} for general application for Case ID: {} Event type {}",
                outcome, caseId, EventType.GENERAL_APPLICATION_OUTCOME);

            final String valueCode = dynamicList.getValueCode();
            log.info("Selected dynamic list code : {} Case ID: {}", valueCode, caseId);
            final List<GeneralApplicationCollectionData> applicationCollectionDataList
                = existingList.stream().map(ga -> setStatusForElement(caseData, ga, valueCode, outcome)).sorted(helper::getCompareTo).collect(
                Collectors.toList());

            log.info("applicationCollectionDataList : {} caseId {}", applicationCollectionDataList.size(), caseId);
            caseData.put(GENERAL_APPLICATION_COLLECTION, applicationCollectionDataList);
            caseData.remove(GENERAL_APPLICATION_OUTCOME_LIST);
            caseData.remove(GENERAL_APPLICATION_OUTCOME_OTHER);
            caseData.remove(GENERAL_APPLICATION_OUTCOME_DECISION);
        }
        return GenericAboutToStartOrSubmitCallbackResponse.<Map<String, Object>>builder().data(caseData).build();
    }

    private void migrateExistingApplication(CaseDetails caseDetails, String userAuthorisation) {
        Map<String, Object> caseData = caseDetails.getData();
        List<GeneralApplicationCollectionData> existingGeneralApplication = helper.getGeneralApplicationList(caseData);
        GeneralApplicationCollectionData data = helper.migrateExistingGeneralApplication(caseData, userAuthorisation);
        if (data != null) {
            String status = Objects.toString(caseData.get(GENERAL_APPLICATION_OUTCOME_DECISION), null);
            log.info("In migration outcome decision {} for general application for Case ID: {} Event type {}",
                status, caseDetails.getId(), EventType.GENERAL_APPLICATION_OUTCOME);
            updateStatus(caseData, data, status);
            existingGeneralApplication.add(data);
            caseData.put(GENERAL_APPLICATION_COLLECTION, existingGeneralApplication);
        }
        helper.deleteNonCollectionGeneralApplication(caseData);
        caseData.remove(GENERAL_APPLICATION_OUTCOME_LIST);
    }

    private GeneralApplicationCollectionData setStatusForElement(Map<String, Object> caseData,
                                                                 GeneralApplicationCollectionData data,
                                                                 String code,
                                                                 String status) {
        if (code.equals(data.getId())) {
            return updateStatus(caseData, data, status);
        }
        return data;
    }

    private GeneralApplicationCollectionData updateStatus(Map<String, Object> caseData,
                                                          GeneralApplicationCollectionData data,
                                                          String status) {
        GeneralApplicationItems items = data.getGeneralApplicationItems();
        items.setGeneralApplicationOutcomeOther(Objects.toString(caseData.get(GENERAL_APPLICATION_OUTCOME_OTHER), null));
        switch (status) {
            case "Approved" -> items.setGeneralApplicationStatus(GeneralApplicationStatus.APPROVED.getId());
            case "Not Approved" -> items.setGeneralApplicationStatus(GeneralApplicationStatus.NOT_APPROVED.getId());
            case "Other" -> items.setGeneralApplicationStatus(GeneralApplicationStatus.OTHER.getId());
            default -> throw new IllegalStateException("Unexpected value: " + status);
        }
        return data;
    }
}
