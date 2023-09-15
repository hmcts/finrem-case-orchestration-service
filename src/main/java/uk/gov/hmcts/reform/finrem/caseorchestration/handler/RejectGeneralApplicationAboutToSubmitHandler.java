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
import uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.FinremCaseData;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.FinremCaseDetails;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.GeneralApplicationCollectionData;
import uk.gov.hmcts.reform.finrem.caseorchestration.service.GeneralApplicationService;

import java.util.List;
import java.util.Objects;

import static uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.CCDConfigConstant.GENERAL_APPLICATION_COLLECTION;

@Slf4j
@Service
public class RejectGeneralApplicationAboutToSubmitHandler
    extends FinremCallbackHandler {

    private final GeneralApplicationHelper helper;
    private final GeneralApplicationService generalApplicationService;

    public RejectGeneralApplicationAboutToSubmitHandler(FinremCaseDetailsMapper finremCaseDetailsMapper, GeneralApplicationHelper helper,
                                                        GeneralApplicationService service) {
        super(finremCaseDetailsMapper);
        this.helper = helper;
        this.generalApplicationService = service;
    }

    @Override
    public boolean canHandle(CallbackType callbackType, CaseType caseType, EventType eventType) {
        return CallbackType.ABOUT_TO_SUBMIT.equals(callbackType)
            && CaseType.CONTESTED.equals(caseType)
            && EventType.REJECT_GENERAL_APPLICATION.equals(eventType);
    }

    @Override
    public GenericAboutToStartOrSubmitCallbackResponse<FinremCaseData> handle(
        FinremCallbackRequest callbackRequest,
        String userAuthorisation) {
        FinremCaseDetails caseDetails = callbackRequest.getCaseDetails();
        log.info("Received on submit request to reject general application for Case ID: {}", caseDetails.getId());
        FinremCaseData caseData = caseDetails.getData();

        List<GeneralApplicationCollectionData> existingList = helper.getGeneralApplicationList(caseData, GENERAL_APPLICATION_COLLECTION);

        DynamicList dynamicList = helper.objectToDynamicList(caseData.getGeneralApplicationWrapper().getGeneralApplicationList());
        if (dynamicList == null) {
            return GenericAboutToStartOrSubmitCallbackResponse.<FinremCaseData>builder().data(caseData)
                .errors(List.of("There is no general application available to reject.")).build();
        }

        if (existingList.isEmpty() && caseData.getGeneralApplicationWrapper().getGeneralApplicationCreatedBy() != null) {
            helper.deleteNonCollectionGeneralApplication(caseData);
        } else {
            final String valueCode = dynamicList.getValueCode();
            log.info("selected dynamic list code : {}", valueCode);
            final List<GeneralApplicationCollectionData> applicationCollectionDataList
                = existingList.stream().filter(ga -> !ga.getId().equals(valueCode)).sorted(helper::getCompareTo).toList();
            log.info("applicationCollectionDataList : {}", applicationCollectionDataList.size());
            generalApplicationService.updateGeneralApplicationCollectionData(applicationCollectionDataList, caseData);
            caseData.getGeneralApplicationWrapper().getGeneralApplications().forEach(
                ga -> ga.getValue().setAppRespGeneralApplicationReceivedFrom(null));
        }
        String previousState = Objects.toString(caseDetails.getData().getGeneralApplicationWrapper()
            .getGeneralApplicationPreState(), caseDetails.getState().getStateId());
        log.info("Previous state : {} for caseId {}", previousState, caseDetails.getId());
        return GenericAboutToStartOrSubmitCallbackResponse.<FinremCaseData>builder().data(caseData).state(previousState).build();
    }
}
