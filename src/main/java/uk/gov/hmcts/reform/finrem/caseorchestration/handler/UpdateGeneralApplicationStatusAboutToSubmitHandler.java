package uk.gov.hmcts.reform.finrem.caseorchestration.handler;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import uk.gov.hmcts.reform.ccd.client.model.CallbackRequest;
import uk.gov.hmcts.reform.ccd.client.model.CaseDetails;
import uk.gov.hmcts.reform.finrem.caseorchestration.ccd.callback.CallbackType;
import uk.gov.hmcts.reform.finrem.caseorchestration.controllers.GenericAboutToStartOrSubmitCallbackResponse;
import uk.gov.hmcts.reform.finrem.caseorchestration.helper.GeneralApplicationHelper;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.EventType;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.GeneralApplicationStatus;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.CaseType;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.GeneralApplicationCollectionData;
import uk.gov.hmcts.reform.finrem.caseorchestration.service.GeneralApplicationService;

import java.util.List;
import java.util.Map;

import static uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.CCDConfigConstant.GENERAL_APPLICATION_COLLECTION;

@Slf4j
@Service
@RequiredArgsConstructor
public class UpdateGeneralApplicationStatusAboutToSubmitHandler implements CallbackHandler<Map<String, Object>> {

    private final GeneralApplicationService service;
    private final GeneralApplicationHelper helper;

    @Override
    public boolean canHandle(CallbackType callbackType, CaseType caseType, EventType eventType) {
        return CallbackType.ABOUT_TO_SUBMIT.equals(callbackType)
            && CaseType.CONTESTED.equals(caseType)
            && EventType.UPDATE_CONTESTED_GENERAL_APPLICATION.equals(eventType);
    }

    @Override
    public GenericAboutToStartOrSubmitCallbackResponse<Map<String, Object>> handle(CallbackRequest callbackRequest,
                                                                                   String userAuthorisation) {
        CaseDetails caseDetails = callbackRequest.getCaseDetails();
        log.info("About to Submit callback event type {} for case id: {}", EventType.UPDATE_CONTESTED_GENERAL_APPLICATION, caseDetails.getId());

        Map<String, Object> caseData
            = service.updateGeneralApplications(callbackRequest, userAuthorisation);

        List<GeneralApplicationCollectionData> generalApplicationList = helper.getGeneralApplicationList(caseDetails.getData());
        if (!generalApplicationList.isEmpty()) {
            List<GeneralApplicationCollectionData> list = generalApplicationList.stream().map(this::updateStatus).toList();
            caseData.put(GENERAL_APPLICATION_COLLECTION, list);
        }
        helper.deleteNonCollectionGeneralApplication(caseData);
        return GenericAboutToStartOrSubmitCallbackResponse.<Map<String, Object>>builder().data(caseData).build();
    }

    private GeneralApplicationCollectionData updateStatus(GeneralApplicationCollectionData item) {
        item.getGeneralApplicationItems().setGeneralApplicationStatus(GeneralApplicationStatus.REFERRED.getId());
        return item;
    }

}
