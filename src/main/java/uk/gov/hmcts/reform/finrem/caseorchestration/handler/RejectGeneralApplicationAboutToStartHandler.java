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
import uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.FinremCaseDetails;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.GeneralApplicationCollectionData;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.GeneralApplicationItems;
import uk.gov.hmcts.reform.finrem.caseorchestration.service.GeneralApplicationService;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.atomic.AtomicInteger;


@Slf4j
@Service
public class RejectGeneralApplicationAboutToStartHandler extends FinremCallbackHandler implements GeneralApplicationHandler {

    private final GeneralApplicationHelper helper;
    private final GeneralApplicationService service;

    public RejectGeneralApplicationAboutToStartHandler(FinremCaseDetailsMapper finremCaseDetailsMapper, GeneralApplicationHelper helper,
                                                       GeneralApplicationService service) {
        super(finremCaseDetailsMapper);
        this.helper = helper;
        this.service = service;
    }

    @Override
    public boolean canHandle(CallbackType callbackType, CaseType caseType, EventType eventType) {
        return CallbackType.ABOUT_TO_START.equals(callbackType)
            && CaseType.CONTESTED.equals(caseType)
            && EventType.REJECT_GENERAL_APPLICATION.equals(eventType);
    }

    @Override
    public GenericAboutToStartOrSubmitCallbackResponse<FinremCaseData> handle(
        FinremCallbackRequest callbackRequest,
        String userAuthorisation) {
        FinremCaseDetails caseDetails = callbackRequest.getCaseDetails();
        String caseId = caseDetails.getId().toString();
        log.info("Received on start request to reject general application for Case ID: {}", caseId);
        FinremCaseData caseData = caseDetails.getData();

        helper.populateGeneralApplicationSender(caseData, caseData.getGeneralApplicationWrapper().getGeneralApplications());

        List<GeneralApplicationCollectionData> existingGeneralApplicationList = helper.getReadyForRejectOrReadyForReferList(caseData);
        AtomicInteger index = new AtomicInteger(0);
        if (existingGeneralApplicationList.isEmpty() && caseData.getGeneralApplicationWrapper().getGeneralApplicationCreatedBy() != null) {
            setNonCollectionGeneralApplication(caseData, index, userAuthorisation, caseId);
        } else {
            List<DynamicListElement> dynamicListElements = existingGeneralApplicationList.stream()
                .map(ga -> getDynamicListElements(ga.getId(), getLabel(ga.getGeneralApplicationItems(), index.incrementAndGet())))
                .toList();

            if (dynamicListElements.isEmpty()) {
                return GenericAboutToStartOrSubmitCallbackResponse.<FinremCaseData>builder().data(caseData)
                    .errors(List.of("There is no general application available to reject.")).build();
            }

            DynamicList dynamicList = generateAvailableGeneralApplicationAsDynamicList(dynamicListElements);
            log.info("collection dynamicList {} for case id {}", dynamicList, caseId);
            caseData.getGeneralApplicationWrapper().setGeneralApplicationList(dynamicList);
        }

        caseData.getGeneralApplicationWrapper().setGeneralApplicationRejectReason(null);
        return GenericAboutToStartOrSubmitCallbackResponse.<FinremCaseData>builder().data(caseData).build();
    }

    private void setNonCollectionGeneralApplication(FinremCaseData caseData,
                                                    AtomicInteger index,
                                                    String userAuthorisation,
                                                    String caseId) {
        GeneralApplicationItems applicationItems = helper.getApplicationItems(caseData, userAuthorisation, caseId);
        DynamicListElement dynamicListElements
            = getDynamicListElements(applicationItems.getGeneralApplicationCreatedBy(), getLabel(applicationItems, index.incrementAndGet()));

        List<DynamicListElement> dynamicListElementsList = new ArrayList<>();
        dynamicListElementsList.add(dynamicListElements);

        DynamicList dynamicList = generateAvailableGeneralApplicationAsDynamicList(dynamicListElementsList);
        caseData.getGeneralApplicationWrapper().setGeneralApplicationList(dynamicList);
    }
}
