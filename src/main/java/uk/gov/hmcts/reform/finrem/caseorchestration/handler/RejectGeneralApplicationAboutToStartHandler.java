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
import uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.DynamicList;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.DynamicListElement;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.GeneralApplicationCollectionData;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.GeneralApplicationItems;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.stream.Collectors;

import static uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.CCDConfigConstant.GENERAL_APPLICATION_CREATED_BY;
import static uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.CCDConfigConstant.GENERAL_APPLICATION_LIST;
import static uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.CCDConfigConstant.GENERAL_APPLICATION_REJECT_REASON;

@Slf4j
@Service
@RequiredArgsConstructor
public class RejectGeneralApplicationAboutToStartHandler
    implements CallbackHandler<Map<String, Object>>, GeneralApplicationHandler {

    private final GeneralApplicationHelper helper;

    @Override
    public boolean canHandle(CallbackType callbackType, CaseType caseType, EventType eventType) {
        return CallbackType.ABOUT_TO_START.equals(callbackType)
            && CaseType.CONTESTED.equals(caseType)
            && EventType.REJECT_GENERAL_APPLICATION.equals(eventType);
    }

    @Override
    public GenericAboutToStartOrSubmitCallbackResponse<Map<String, Object>> handle(
        CallbackRequest callbackRequest,
        String userAuthorisation) {
        CaseDetails caseDetails = callbackRequest.getCaseDetails();
        log.info("Received on start request to reject general application for Case ID: {}", caseDetails.getId());
        Map<String, Object> caseData = caseDetails.getData();

        List<GeneralApplicationCollectionData> existingGeneralApplicationList = helper.getReadyForRejectOrReadyForReferList(caseData);
        AtomicInteger index = new AtomicInteger(0);
        if (existingGeneralApplicationList.isEmpty() && caseData.get(GENERAL_APPLICATION_CREATED_BY) != null) {
            setNonCollectionGeneralApplication(caseData, index, userAuthorisation);
        } else {
            List<DynamicListElement> dynamicListElements = existingGeneralApplicationList.stream()
                .map(ga -> getDynamicListElements(ga.getId(), getLabel(ga.getGeneralApplicationItems(), index.incrementAndGet())))
                .collect(Collectors.toList());

            if (dynamicListElements.isEmpty()) {
                return GenericAboutToStartOrSubmitCallbackResponse.<Map<String, Object>>builder().data(caseData)
                    .errors(List.of("There is no general application available to reject.")).build();
            }

            DynamicList dynamicList = generateAvailableGeneralApplicationAsDynamicList(dynamicListElements);
            log.info("collection dynamicList {} for case id {}", dynamicList, caseDetails.getId());
            caseData.put(GENERAL_APPLICATION_LIST, dynamicList);
        }

        caseData.remove(GENERAL_APPLICATION_REJECT_REASON);
        return GenericAboutToStartOrSubmitCallbackResponse.<Map<String, Object>>builder().data(caseData).build();
    }

    private void setNonCollectionGeneralApplication(Map<String, Object> caseData,
                                                    AtomicInteger index,
                                                    String userAuthorisation) {
        GeneralApplicationItems applicationItems = helper.getApplicationItems(caseData, userAuthorisation);
        DynamicListElement dynamicListElements
            = getDynamicListElements(applicationItems.getGeneralApplicationCreatedBy(), getLabel(applicationItems, index.incrementAndGet()));

        List<DynamicListElement> dynamicListElementsList = new ArrayList<>();
        dynamicListElementsList.add(dynamicListElements);

        DynamicList dynamicList = generateAvailableGeneralApplicationAsDynamicList(dynamicListElementsList);
        caseData.put(GENERAL_APPLICATION_LIST, dynamicList);
    }
}
