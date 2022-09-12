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
import uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.DynamicListElement;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.GeneralApplicationCollectionData;

import java.util.List;
import java.util.Map;
import java.util.concurrent.atomic.AtomicInteger;

import static uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.CCDConfigConstant.GENERAL_APPLICATION_CREATED_BY;
import static uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.CCDConfigConstant.GENERAL_APPLICATION_OUTCOME_DECISION;
import static uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.CCDConfigConstant.GENERAL_APPLICATION_OUTCOME_LIST;

@Slf4j
@Service
@RequiredArgsConstructor
public class GeneralApplicationOutcomeAboutToStartHandler implements CallbackHandler, GeneralApplicationHandler {

    private final GeneralApplicationHelper helper;

    @Override
    public boolean canHandle(CallbackType callbackType, CaseType caseType, EventType eventType) {
        return CallbackType.ABOUT_TO_START.equals(callbackType)
            && CaseType.CONTESTED.equals(caseType)
            && EventType.GENERAL_APPLICATION_OUTCOME.equals(eventType);
    }

    @Override
    public AboutToStartOrSubmitCallbackResponse handle(CallbackRequest callbackRequest,
                                                       String userAuthorisation) {
        CaseDetails caseDetails = callbackRequest.getCaseDetails();
        log.info("Received on start request to outcome general application for Case ID: {}", caseDetails.getId());
        Map<String, Object> caseData = caseDetails.getData();

        List<GeneralApplicationCollectionData> referredList = helper.getReferredList(caseData);

        if (referredList.isEmpty()) {
            return AboutToStartOrSubmitCallbackResponse.builder().data(caseData)
                .errors(List.of("There are no general application available for decision.")).build();
        }

        AtomicInteger index = new AtomicInteger(0);

        log.info("existingGeneralApplicationList size {}", referredList.size());
        log.info("caseData.get(GENERAL_APPLICATION_CREATED_BY) ::{}", helper.objectToString(caseData.get(GENERAL_APPLICATION_CREATED_BY)));
        log.info("existingGeneralApplicationList I should not be here ");
        List<DynamicListElement> dynamicListElements = referredList.stream()
            .map(ga -> getDynamicListElements(ga.getId(), getLabel(ga.getGeneralApplicationItems(), index.incrementAndGet())))
            .toList();

        DynamicList dynamicList = generateAvailableGeneralApplicationAsDynamicList(dynamicListElements);
        log.info("collection dynamicList {} for case id {}", dynamicList, caseDetails.getId());
        caseData.put(GENERAL_APPLICATION_OUTCOME_LIST, dynamicList);
        caseData.remove(GENERAL_APPLICATION_OUTCOME_DECISION);
        return AboutToStartOrSubmitCallbackResponse.builder().data(caseData).build();
    }
}
