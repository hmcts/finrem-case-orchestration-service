package uk.gov.hmcts.reform.finrem.caseorchestration.handler;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import uk.gov.hmcts.reform.ccd.client.model.CallbackRequest;
import uk.gov.hmcts.reform.ccd.client.model.CaseDetails;
import uk.gov.hmcts.reform.finrem.caseorchestration.ccd.callback.CallbackType;
import uk.gov.hmcts.reform.finrem.caseorchestration.ccd.domain.EventType;
import uk.gov.hmcts.reform.finrem.caseorchestration.controllers.GenericAboutToStartOrSubmitCallbackResponse;
import uk.gov.hmcts.reform.finrem.caseorchestration.helper.GeneralApplicationHelper;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.CaseType;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.DynamicList;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.GeneralApplicationCollectionData;

import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import static uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.CCDConfigConstant.GENERAL_APPLICATION_COLLECTION;
import static uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.CCDConfigConstant.GENERAL_APPLICATION_CREATED_BY;
import static uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.CCDConfigConstant.GENERAL_APPLICATION_LIST;

@Slf4j
@Service
@RequiredArgsConstructor
public class RejectGeneralApplicationAboutToSubmitHandler
    implements CallbackHandler<Map<String, Object>> {

    private final GeneralApplicationHelper helper;

    @Override
    public boolean canHandle(CallbackType callbackType, CaseType caseType, EventType eventType) {
        return CallbackType.ABOUT_TO_SUBMIT.equals(callbackType)
            && CaseType.CONTESTED.equals(caseType)
            && EventType.REJECT_GENERAL_APPLICATION.equals(eventType);
    }

    @Override
    public GenericAboutToStartOrSubmitCallbackResponse<Map<String, Object>> handle(
        CallbackRequest callbackRequest,
        String userAuthorisation) {
        CaseDetails caseDetails = callbackRequest.getCaseDetails();
        log.info("Received on submit request to reject general application for Case ID: {}", caseDetails.getId());
        Map<String, Object> caseData = caseDetails.getData();

        List<GeneralApplicationCollectionData> existingList = helper.getGeneralApplicationList(caseData);

        DynamicList dynamicList = helper.objectToDynamicList(caseData.get(GENERAL_APPLICATION_LIST));
        if (dynamicList == null) {
            return GenericAboutToStartOrSubmitCallbackResponse.<Map<String, Object>>builder().data(caseData)
                .errors(List.of("There is no general application available to reject.")).build();
        }

        if (existingList.isEmpty() && caseData.get(GENERAL_APPLICATION_CREATED_BY) != null) {
            helper.deleteNonCollectionGeneralApplication(caseData);
        } else {
            final String valueCode = dynamicList.getValueCode();
            log.info("selected dynamic list code : {}", valueCode);
            final List<GeneralApplicationCollectionData> applicationCollectionDataList
                = existingList.stream().filter(ga -> !ga.getId().equals(valueCode)).sorted(helper::getCompareTo).collect(Collectors.toList());
            log.info("applicationCollectionDataList : {}", applicationCollectionDataList.size());
            caseData.put(GENERAL_APPLICATION_COLLECTION, applicationCollectionDataList);
        }
        return GenericAboutToStartOrSubmitCallbackResponse.<Map<String, Object>>builder().data(caseData).build();
    }
}
