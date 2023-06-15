package uk.gov.hmcts.reform.finrem.caseorchestration.handler;

import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import uk.gov.hmcts.reform.finrem.caseorchestration.ccd.callback.CallbackType;
import uk.gov.hmcts.reform.finrem.caseorchestration.controllers.GenericAboutToStartOrSubmitCallbackResponse;
import uk.gov.hmcts.reform.finrem.caseorchestration.helper.GeneralApplicationHelper;
import uk.gov.hmcts.reform.finrem.caseorchestration.mapper.FinremCaseDetailsMapper;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.EventType;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.GeneralApplicationStatus;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.CaseType;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.FinremCaseData;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.FinremCaseDetails;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.GeneralApplicationCollectionData;
import uk.gov.hmcts.reform.finrem.caseorchestration.service.GeneralApplicationService;

import java.util.List;

import static uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.CCDConfigConstant.GENERAL_APPLICATION_COLLECTION;

@Slf4j
@Service
public class UpdateGeneralApplicationStatusAboutToSubmitHandler extends FinremCallbackHandler {

    private final GeneralApplicationService service;
    private final GeneralApplicationHelper helper;

    public UpdateGeneralApplicationStatusAboutToSubmitHandler(
        FinremCaseDetailsMapper finremCaseDetailsMapper, GeneralApplicationService service, GeneralApplicationHelper helper) {
        super(finremCaseDetailsMapper);
        this.service = service;
        this.helper = helper;
    }

    @Override
    public boolean canHandle(CallbackType callbackType, CaseType caseType, EventType eventType) {
        return CallbackType.ABOUT_TO_SUBMIT.equals(callbackType)
            && CaseType.CONTESTED.equals(caseType)
            && EventType.UPDATE_CONTESTED_GENERAL_APPLICATION.equals(eventType);
    }

    @Override
    public GenericAboutToStartOrSubmitCallbackResponse<FinremCaseData> handle(FinremCallbackRequest callbackRequest,
                                                                              String userAuthorisation) {
        FinremCaseDetails caseDetails = callbackRequest.getCaseDetails();
        log.info("About to Submit callback event type {} for case id: {}", EventType.UPDATE_CONTESTED_GENERAL_APPLICATION, caseDetails.getId());

        FinremCaseData caseData
            = service.updateGeneralApplications(callbackRequest, userAuthorisation);

        List<GeneralApplicationCollectionData> generalApplicationList = helper.getGeneralApplicationList(caseData, GENERAL_APPLICATION_COLLECTION);
        if (!generalApplicationList.isEmpty()) {
            List<GeneralApplicationCollectionData> list = generalApplicationList.stream().map(this::updateStatus).toList();
            service.updateGeneralApplicationCollectionData(list, caseData);

        }
        helper.deleteNonCollectionGeneralApplication(caseData);
        return GenericAboutToStartOrSubmitCallbackResponse.<FinremCaseData>builder().data(caseData).build();
    }

    private GeneralApplicationCollectionData updateStatus(GeneralApplicationCollectionData item) {
        item.getGeneralApplicationItems().setGeneralApplicationStatus(GeneralApplicationStatus.REFERRED.getId());
        return item;
    }

}
