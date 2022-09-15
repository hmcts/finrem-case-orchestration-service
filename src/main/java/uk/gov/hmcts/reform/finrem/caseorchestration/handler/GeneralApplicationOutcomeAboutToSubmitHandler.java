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
import uk.gov.hmcts.reform.finrem.caseorchestration.model.GeneralApplicationStatus;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.DynamicList;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.GeneralApplicationCollectionData;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.GeneralApplicationItems;

import java.util.List;
import java.util.Map;
import java.util.Objects;

import static uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.CCDConfigConstant.GENERAL_APPLICATION_COLLECTION;
import static uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.CCDConfigConstant.GENERAL_APPLICATION_OUTCOME_DECISION;
import static uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.CCDConfigConstant.GENERAL_APPLICATION_OUTCOME_LIST;
import static uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.CCDConfigConstant.GENERAL_APPLICATION_OUTCOME_OTHER;

@Slf4j
@Service
@RequiredArgsConstructor
public class GeneralApplicationOutcomeAboutToSubmitHandler implements CallbackHandler {

    private final GeneralApplicationHelper helper;

    @Override
    public boolean canHandle(CallbackType callbackType, CaseType caseType, EventType eventType) {
        return CallbackType.ABOUT_TO_SUBMIT.equals(callbackType)
            && CaseType.CONTESTED.equals(caseType)
            && EventType.GENERAL_APPLICATION_OUTCOME.equals(eventType);
    }

    @Override
    public AboutToStartOrSubmitCallbackResponse handle(CallbackRequest callbackRequest,
                                                       String userAuthorisation) {
        CaseDetails caseDetails = callbackRequest.getCaseDetails();
        final String caseId = caseDetails.getId().toString();
        log.info("Received on start request to outcome decision general application for Case ID: {}", caseId);
        Map<String, Object> caseData = caseDetails.getData();

        List<GeneralApplicationCollectionData> existingList = helper.getGeneralApplicationList(caseData);
        DynamicList dynamicList = helper.objectToDynamicList(caseData.get(GENERAL_APPLICATION_OUTCOME_LIST));

        final String outcome  = Objects.toString(caseData.get(GENERAL_APPLICATION_OUTCOME_DECISION), null);
        log.info("outcome decision {} general application for Case ID: {}", outcome, caseId);

        final String valueCode = dynamicList.getValueCode();
        log.info("selected dynamic list code : {} Case ID: {}", valueCode, caseId);
        final List<GeneralApplicationCollectionData> applicationCollectionDataList
            = existingList.stream().map(ga -> setStatus(caseData, ga, valueCode, outcome)).sorted(helper::getCompareTo).toList();

        log.info("applicationCollectionDataList : {} caseId {}", applicationCollectionDataList.size(), caseId);
        caseData.put(GENERAL_APPLICATION_COLLECTION, applicationCollectionDataList);
        caseData.remove(GENERAL_APPLICATION_OUTCOME_LIST);
        caseData.remove(GENERAL_APPLICATION_OUTCOME_OTHER);
        return AboutToStartOrSubmitCallbackResponse.builder().data(caseData).build();
    }

    private GeneralApplicationCollectionData setStatus(Map<String, Object> caseData,
                                                       GeneralApplicationCollectionData data,
                                                       String code,
                                                       String outcome) {
        if (code.equals(data.getId())) {
            GeneralApplicationItems items = data.getGeneralApplicationItems();
            items.setGeneralApplicationOutcomeOther(Objects.toString(caseData.get(GENERAL_APPLICATION_OUTCOME_OTHER), null));
            switch (outcome) {
                case "Approved" -> items.setGeneralApplicationStatus(GeneralApplicationStatus.APPROVED.getId());
                case "Not Approved" -> items.setGeneralApplicationStatus(GeneralApplicationStatus.NOT_APPROVED.getId());
                case "Other" -> items.setGeneralApplicationStatus(GeneralApplicationStatus.OTHER.getId());
                default -> throw new IllegalStateException("Unexpected value: " + outcome);
            }
        }
        return data;
    }
}
