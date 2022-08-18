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
import static uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.CCDConfigConstant.GENERAL_APPLICATION_LIST;
import static uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.CCDConfigConstant.GENERAL_APPLICATION_REJECT_REASON;

@Slf4j
@Service
@RequiredArgsConstructor
public class RejectGeneralApplicationAboutToStartHandler implements CallbackHandler {

    private final GeneralApplicationHelper helper;

    @Override
    public boolean canHandle(CallbackType callbackType, CaseType caseType, EventType eventType) {
        return CallbackType.ABOUT_TO_START.equals(callbackType)
            && CaseType.CONTESTED.equals(caseType)
            && EventType.REJECT_GENERAL_APPLICATION.equals(eventType);
    }

    @Override
    public AboutToStartOrSubmitCallbackResponse handle(CallbackRequest callbackRequest,
                                                       String userAuthorisation) {
        CaseDetails caseDetails = callbackRequest.getCaseDetails();
        log.info("Received on start request to reject general application for Case ID: {}", caseDetails.getId());
        Map<String, Object> caseData = caseDetails.getData();

        List<GeneralApplicationCollectionData> existingGeneralApplicationList = helper.getGeneralApplicationList(caseData);
        AtomicInteger index = new AtomicInteger(0);

        if (existingGeneralApplicationList.isEmpty() && caseData.get(GENERAL_APPLICATION_CREATED_BY) != null) {
            GeneralApplicationItems applicationItems = helper.getApplicationItems(caseData);
            DynamicListElement dynamicListElements
                = getDynamicListElements(applicationItems.getGeneralApplicationCreatedBy(), getLabel(applicationItems, index.incrementAndGet()));

            List<DynamicListElement> dynamicListElementsList = new ArrayList<>();
            dynamicListElementsList.add(dynamicListElements);

            DynamicList dynamicList = generateAvailableGeneralApplicationAsDynamicList(dynamicListElementsList);
            log.info("non collection dynamicList {} for case id {}", dynamicList, caseDetails.getId());
            caseData.put(GENERAL_APPLICATION_LIST, dynamicList);
        } else {
            List<DynamicListElement> dynamicListElements = existingGeneralApplicationList.stream()
                .map(ga -> getDynamicListElements(ga.getId(), getLabel(ga.getGeneralApplicationItems(), index.incrementAndGet())))
                .toList();

            DynamicList dynamicList = generateAvailableGeneralApplicationAsDynamicList(dynamicListElements);
            log.info("collection dynamicList {} for case id {}", dynamicList, caseDetails.getId());
            caseData.put(GENERAL_APPLICATION_LIST, dynamicList);
        }

        caseData.remove(GENERAL_APPLICATION_REJECT_REASON);
        return AboutToStartOrSubmitCallbackResponse.builder().data(caseData).build();
    }

    private String getLabel(GeneralApplicationItems items, int index) {
        return "General Application "
            + index
            + " - Received from - "
            + items.getGeneralApplicationReceivedFrom()
            + " - Created Date - "
            + items.getGeneralApplicationCreatedDate()
            + " - Hearing Required - "
            + items.getGeneralApplicationHearingRequired();
    }

    private DynamicListElement getDynamicListElements(String code, String label) {
        log.info("Code {} Label {}", code, label);
        return DynamicListElement.builder()
            .code(code)
            .label(label)
            .build();

    }

    private DynamicList generateAvailableGeneralApplicationAsDynamicList(List<DynamicListElement> dynamicListElement) {
        if (dynamicListElement.isEmpty()) {
            DynamicListElement elements =
                DynamicListElement.builder().code("-").label("There is no general application available to reject.").build();
            return DynamicList.builder()
                .value(elements)
                .listItems(List.of(elements))
                .build();
        }
        return DynamicList.builder()
            .value(dynamicListElement.get(0))
            .listItems(dynamicListElement)
            .build();
    }
}
