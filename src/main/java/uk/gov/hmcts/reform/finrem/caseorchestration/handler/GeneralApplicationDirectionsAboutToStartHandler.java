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
import uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.DynamicListElement;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.GeneralApplicationCollectionData;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.GeneralApplicationItems;
import uk.gov.hmcts.reform.finrem.caseorchestration.service.GeneralApplicationDirectionsService;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.stream.Collectors;

import static uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.CCDConfigConstant.GENERAL_APPLICATION_CREATED_BY;
import static uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.CCDConfigConstant.GENERAL_APPLICATION_DIRECTIONS_LIST;

@Slf4j
@Service
@RequiredArgsConstructor
public class GeneralApplicationDirectionsAboutToStartHandler implements CallbackHandler<Map<String, Object>>, GeneralApplicationHandler {

    private final GeneralApplicationHelper helper;
    private final GeneralApplicationDirectionsService service;

    @Override
    public boolean canHandle(CallbackType callbackType, CaseType caseType, EventType eventType) {
        return CallbackType.ABOUT_TO_START.equals(callbackType)
            && CaseType.CONTESTED.equals(caseType)
            && EventType.GENERAL_APPLICATION_DIRECTIONS.equals(eventType);
    }

    @Override
    public GenericAboutToStartOrSubmitCallbackResponse<Map<String, Object>> handle(CallbackRequest callbackRequest,
                                                                                   String userAuthorisation) {
        CaseDetails caseDetails = callbackRequest.getCaseDetails();
        log.info("About to Start callback event type {} for case id: {}", EventType.GENERAL_APPLICATION_DIRECTIONS, caseDetails.getId());

        Map<String, Object> caseData = caseDetails.getData();
        service.startGeneralApplicationDirections(caseDetails);

        List<GeneralApplicationCollectionData> outcomeList = helper.getOutcomeList(caseData);
        AtomicInteger index = new AtomicInteger(0);
        if (outcomeList.isEmpty() && caseData.get(GENERAL_APPLICATION_CREATED_BY) != null) {
            log.info("setting direction list if existing ga not moved to collection for Case ID: {}", caseDetails.getId());
            setDirectionListForNonCollectionGeneralApplication(caseData, index, userAuthorisation);
        } else {
            if (outcomeList.isEmpty()) {
                return GenericAboutToStartOrSubmitCallbackResponse.<Map<String, Object>>builder().data(caseData)
                    .errors(List.of("There are no general application available for issue direction.")).build();
            }
            List<DynamicListElement> dynamicListElements = outcomeList.stream()
                .map(ga -> getDynamicListElements(ga.getId() + "#" + ga.getGeneralApplicationItems().getGeneralApplicationStatus(),
                    getLabel(ga.getGeneralApplicationItems(), index.incrementAndGet())))
                .collect(Collectors.toList());

            DynamicList dynamicList = generateAvailableGeneralApplicationAsDynamicList(dynamicListElements);
            caseData.put(GENERAL_APPLICATION_DIRECTIONS_LIST, dynamicList);
        }
        return GenericAboutToStartOrSubmitCallbackResponse.<Map<String, Object>>builder().data(caseData).build();
    }

    private void setDirectionListForNonCollectionGeneralApplication(Map<String, Object> caseData,
                                                                    AtomicInteger index,
                                                                    String userAuthorisation) {
        GeneralApplicationItems applicationItems = helper.getApplicationItems(caseData, userAuthorisation);
        DynamicListElement dynamicListElements
            = getDynamicListElements(applicationItems.getGeneralApplicationCreatedBy(), getLabel(applicationItems, index.incrementAndGet()));

        List<DynamicListElement> dynamicListElementsList = new ArrayList<>();
        dynamicListElementsList.add(dynamicListElements);

        DynamicList dynamicList = generateAvailableGeneralApplicationAsDynamicList(dynamicListElementsList);
        caseData.put(GENERAL_APPLICATION_DIRECTIONS_LIST, dynamicList);
    }

}
