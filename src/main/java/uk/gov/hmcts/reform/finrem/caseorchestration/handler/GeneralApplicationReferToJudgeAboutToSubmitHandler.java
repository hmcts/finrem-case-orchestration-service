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
import uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.DynamicList;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.GeneralApplicationCollectionData;

import java.util.List;
import java.util.Map;

import static uk.gov.hmcts.reform.finrem.caseorchestration.model.GeneralApplicationStatus.REFERRED;
import static uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.CCDConfigConstant.GENERAL_APPLICATION_COLLECTION;
import static uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.CCDConfigConstant.GENERAL_APPLICATION_CREATED_BY;
import static uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.CCDConfigConstant.GENERAL_APPLICATION_REFERRED_DETAIL;
import static uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.CCDConfigConstant.GENERAL_APPLICATION_REFER_LIST;

@Slf4j
@Service
@RequiredArgsConstructor
public class GeneralApplicationReferToJudgeAboutToSubmitHandler implements CallbackHandler {

    private final GeneralApplicationHelper helper;

    @Override
    public boolean canHandle(CallbackType callbackType, CaseType caseType, EventType eventType) {
        return CallbackType.ABOUT_TO_SUBMIT.equals(callbackType)
            && CaseType.CONTESTED.equals(caseType)
            && EventType.GENERAL_APPLICATION_REFER_TO_JUDGE.equals(eventType);
    }

    @Override
    public AboutToStartOrSubmitCallbackResponse handle(CallbackRequest callbackRequest,
                                                       String userAuthorisation) {
        CaseDetails caseDetails = callbackRequest.getCaseDetails();
        final String caseId = caseDetails.getId().toString();
        log.info("Received on start request to refer to judge general application for Case ID: {}", caseId);
        Map<String, Object> caseData = caseDetails.getData();

        List<GeneralApplicationCollectionData> existingList = helper.getGeneralApplicationList(caseData);
        DynamicList dynamicList = helper.objectToDynamicList(caseData.get(GENERAL_APPLICATION_REFER_LIST));

        if (existingList.isEmpty() && caseData.get(GENERAL_APPLICATION_CREATED_BY) != null) {
            List<GeneralApplicationCollectionData> existingGeneralApplication = helper.getGeneralApplicationList(caseData);
            GeneralApplicationCollectionData data = helper.migrateExistingGeneralApplication(caseData);
            if (data != null) {
                log.info("data ={}=caseId {}", data, caseId);
                data.getGeneralApplicationItems().setGeneralApplicationStatus(REFERRED.getId());
                existingGeneralApplication.add(data);
                caseData.put(GENERAL_APPLICATION_COLLECTION,existingGeneralApplication);
            }
            helper.deleteNonCollectionGeneralApplication(caseData);

        } else {
            if (dynamicList == null) {
                return AboutToStartOrSubmitCallbackResponse.builder().data(caseData)
                    .errors(List.of("There is no general application available to refer.")).build();
            }
            final String valueCode = dynamicList.getValueCode();
            String label  = dynamicList.getValue().getLabel();
            String referredApplicationDetails = label.substring(label.indexOf("-") + 1);
            caseData.put(GENERAL_APPLICATION_REFERRED_DETAIL, referredApplicationDetails);
            log.info("selected dynamicList.getValue() : {} Case ID: {}", dynamicList.getValue().getLabel(), caseId);
            log.info("selected dynamic list code : {} Case ID: {}", valueCode, caseId);
            final List<GeneralApplicationCollectionData> applicationCollectionDataList
                = existingList.stream().map(ga -> setStatus(ga, valueCode)).sorted(helper::getCompareTo).toList();

            log.info("applicationCollectionDataList : {} caseId {}", applicationCollectionDataList.size(), caseId);
            caseData.put(GENERAL_APPLICATION_COLLECTION, applicationCollectionDataList);
        }
        return AboutToStartOrSubmitCallbackResponse.builder().data(caseData).build();
    }

    private GeneralApplicationCollectionData setStatus(GeneralApplicationCollectionData data, String code) {
        if (code.equals(data.getId())) {
            data.getGeneralApplicationItems().setGeneralApplicationStatus(REFERRED.getId());
        }
        return data;
    }
}
