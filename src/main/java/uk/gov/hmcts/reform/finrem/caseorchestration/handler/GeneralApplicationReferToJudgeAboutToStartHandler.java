package uk.gov.hmcts.reform.finrem.caseorchestration.handler;

import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import uk.gov.hmcts.reform.finrem.caseorchestration.ccd.callback.CallbackType;
import uk.gov.hmcts.reform.finrem.caseorchestration.controllers.GenericAboutToStartOrSubmitCallbackResponse;
import uk.gov.hmcts.reform.finrem.caseorchestration.helper.GeneralApplicationHelper;
import uk.gov.hmcts.reform.finrem.caseorchestration.mapper.FinremCaseDetailsMapper;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.EventType;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.CaseType;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.DynamicList;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.DynamicListElement;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.FinremCaseData;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.FinremCaseDataContested;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.FinremCaseDetails;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.GeneralApplicationCollectionData;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.GeneralApplicationItems;
import uk.gov.hmcts.reform.finrem.caseorchestration.service.GeneralApplicationService;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.concurrent.atomic.AtomicInteger;

@Slf4j
@Service
public class GeneralApplicationReferToJudgeAboutToStartHandler extends FinremCallbackHandler<FinremCaseDataContested>
    implements GeneralApplicationHandler {

    private final GeneralApplicationHelper helper;
    private final GeneralApplicationService service;

    public GeneralApplicationReferToJudgeAboutToStartHandler(FinremCaseDetailsMapper finremCaseDetailsMapper, GeneralApplicationHelper helper,
                                        GeneralApplicationService service) {
        super(finremCaseDetailsMapper);
        this.helper = helper;
        this.service = service;
    }

    @Override
    public boolean canHandle(CallbackType callbackType, CaseType caseType, EventType eventType) {
        return CallbackType.ABOUT_TO_START.equals(callbackType)
            && CaseType.CONTESTED.equals(caseType)
            && EventType.GENERAL_APPLICATION_REFER_TO_JUDGE.equals(eventType);
    }

    @Override
    public GenericAboutToStartOrSubmitCallbackResponse<FinremCaseDataContested> handle(
        FinremCallbackRequest<FinremCaseDataContested> callbackRequest,
        String userAuthorisation) {
        FinremCaseDetails<FinremCaseDataContested> caseDetails = callbackRequest.getCaseDetails();
        String caseId = caseDetails.getId().toString();
        log.info("Received on start request to refer general application for Case ID: {}", caseId);

        FinremCaseDataContested caseData = caseDetails.getData();
        caseData.getGeneralApplicationWrapper().setGeneralApplicationReferList(null);

        helper.populateGeneralApplicationSender(caseData, caseData.getGeneralApplicationWrapper().getGeneralApplications());

        List<GeneralApplicationCollectionData> existingGeneralApplicationList = helper.getReadyForRejectOrReadyForReferList(caseData);
        AtomicInteger index = new AtomicInteger(0);
        if (existingGeneralApplicationList.isEmpty() && caseData.getGeneralApplicationWrapper().getGeneralApplicationCreatedBy() != null) {
            String judgeEmail = Objects.toString(caseData.getGeneralApplicationWrapper().getGeneralApplicationReferToJudgeEmail(), null);
            log.info("general application has referred to judge while existing ga not moved to collection for Case ID: {}",
                caseDetails.getId());
            if (existingGeneralApplicationList.isEmpty() && judgeEmail != null) {
                List<DynamicListElement> dynamicListElements = getDynamicListElements(existingGeneralApplicationList, index);
                if (dynamicListElements.isEmpty()) {
                    return GenericAboutToStartOrSubmitCallbackResponse.<FinremCaseDataContested>builder().data(caseData)
                        .errors(List.of("There are no general application available to refer.")).build();
                }
            }
            log.info("setting refer list if existing ga not moved to collection for Case ID: {}", caseDetails.getId());

            setReferListForNonCollectionGeneralApplication(caseData, index, userAuthorisation, caseId);

        } else {
            log.info("setting refer list for Case ID: {}", caseDetails.getId());
            List<DynamicListElement> dynamicListElements = getDynamicListElements(existingGeneralApplicationList, index);
            if (dynamicListElements.isEmpty()) {
                return GenericAboutToStartOrSubmitCallbackResponse.<FinremCaseDataContested>builder().data(caseData)
                    .errors(List.of("There are no general application available to refer.")).build();
            }
            DynamicList dynamicList = generateAvailableGeneralApplicationAsDynamicList(dynamicListElements);
            caseData.getGeneralApplicationWrapper().setGeneralApplicationReferList(dynamicList);
        }
        caseData.getGeneralApplicationWrapper().setGeneralApplicationReferToJudgeEmail(null);
        caseData.getGeneralApplicationWrapper().setGeneralApplicationReferDetail(null);
        return GenericAboutToStartOrSubmitCallbackResponse.<FinremCaseDataContested>builder().data(caseData).build();
    }

    private List<DynamicListElement> getDynamicListElements(List<GeneralApplicationCollectionData> existingGeneralApplicationList,
                                                            AtomicInteger index) {
        return existingGeneralApplicationList.stream()
            .map(ga -> getDynamicListElements(ga.getId(), getLabel(ga.getGeneralApplicationItems(), index.incrementAndGet())))
            .toList();
    }

    private void setReferListForNonCollectionGeneralApplication(FinremCaseDataContested caseData,
                                                                AtomicInteger index,
                                                                String userAuthorisation, String caseId) {
        GeneralApplicationItems applicationItems = helper.getApplicationItems(caseData, userAuthorisation, caseId);
        DynamicListElement dynamicListElements
            = getDynamicListElements(applicationItems.getGeneralApplicationCreatedBy(), getLabel(applicationItems, index.incrementAndGet()));

        List<DynamicListElement> dynamicListElementsList = new ArrayList<>();
        dynamicListElementsList.add(dynamicListElements);

        DynamicList dynamicList = generateAvailableGeneralApplicationAsDynamicList(dynamicListElementsList);
        caseData.getGeneralApplicationWrapper().setGeneralApplicationReferList(dynamicList);
    }
}
