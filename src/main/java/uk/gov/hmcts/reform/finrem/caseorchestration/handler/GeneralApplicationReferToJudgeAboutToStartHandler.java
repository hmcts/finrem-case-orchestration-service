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
import uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.GeneralApplicationItems;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.concurrent.atomic.AtomicInteger;

import static uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.CCDConfigConstant.GENERAL_APPLICATION_CREATED_BY;
import static uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.CCDConfigConstant.GENERAL_APPLICATION_REFERRED_DETAIL;
import static uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.CCDConfigConstant.GENERAL_APPLICATION_REFER_LIST;
import static uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.CCDConfigConstant.GENERAL_APPLICATION_REFER_TO_JUDGE_EMAIL;

@Slf4j
@Service
@RequiredArgsConstructor
public class GeneralApplicationReferToJudgeAboutToStartHandler implements CallbackHandler, GeneralApplicationHandler {

    private final GeneralApplicationHelper helper;

    @Override
    public boolean canHandle(CallbackType callbackType, CaseType caseType, EventType eventType) {
        return CallbackType.ABOUT_TO_START.equals(callbackType)
            && CaseType.CONTESTED.equals(caseType)
            && EventType.GENERAL_APPLICATION_REFER_TO_JUDGE.equals(eventType);
    }

    @Override
    public AboutToStartOrSubmitCallbackResponse handle(CallbackRequest callbackRequest,
                                                       String userAuthorisation) {
        CaseDetails caseDetails = callbackRequest.getCaseDetails();
        log.info("Received on start request to refer general application for Case ID: {}", caseDetails.getId());
        Map<String, Object> caseData = caseDetails.getData();
        caseData.remove(GENERAL_APPLICATION_REFER_LIST);
        List<GeneralApplicationCollectionData> existingGeneralApplicationList = helper.getReadyForRejectOrReadyForReferList(caseData);
        AtomicInteger index = new AtomicInteger(0);

        if (existingGeneralApplicationList.isEmpty() && caseData.get(GENERAL_APPLICATION_CREATED_BY) != null) {
            log.info("existingGeneralApplicationList If refer general application for Case ID: {}", caseDetails.getId());
            GeneralApplicationItems applicationItems = helper.getApplicationItems(caseData);
            DynamicListElement dynamicListElements
                = getDynamicListElements(applicationItems.getGeneralApplicationCreatedBy(), getLabel(applicationItems, index.incrementAndGet()));

            List<DynamicListElement> dynamicListElementsList = new ArrayList<>();
            dynamicListElementsList.add(dynamicListElements);

            DynamicList dynamicList = generateAvailableGeneralApplicationAsDynamicList(dynamicListElementsList);
            log.info("non collection dynamicList {} for case id {}", dynamicList, caseDetails.getId());
            caseData.put(GENERAL_APPLICATION_REFER_LIST, dynamicList);
        } else {
            log.info("existingGeneralApplicationList Else refer general application for Case ID: {}", caseDetails.getId());
            List<DynamicListElement> dynamicListElements = existingGeneralApplicationList.stream()
                .map(ga -> getDynamicListElements(ga.getId(), getLabel(ga.getGeneralApplicationItems(), index.incrementAndGet())))
                .toList();

            if (dynamicListElements.isEmpty()) {
                return AboutToStartOrSubmitCallbackResponse.builder().data(caseData)
                    .errors(List.of("There are no general application available to refer.")).build();
            }

            DynamicList dynamicList = generateAvailableGeneralApplicationAsDynamicList(dynamicListElements);
            log.info("collection dynamicList {} for case id {}", dynamicList, caseDetails.getId());
            caseData.put(GENERAL_APPLICATION_REFER_LIST, dynamicList);
        }
        caseData.remove(GENERAL_APPLICATION_REFER_TO_JUDGE_EMAIL);
        caseData.remove(GENERAL_APPLICATION_REFERRED_DETAIL);
        return AboutToStartOrSubmitCallbackResponse.builder().data(caseData).build();
    }
}
