package uk.gov.hmcts.reform.finrem.caseorchestration.handler;

import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import uk.gov.hmcts.reform.finrem.caseorchestration.ccd.callback.CallbackType;
import uk.gov.hmcts.reform.finrem.caseorchestration.controllers.GenericAboutToStartOrSubmitCallbackResponse;
import uk.gov.hmcts.reform.finrem.caseorchestration.helper.GeneralApplicationHelper;
import uk.gov.hmcts.reform.finrem.caseorchestration.mapper.FinremCaseDetailsMapper;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.EventType;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.CaseType;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.FinremCaseData;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.FinremCaseDetails;
import uk.gov.hmcts.reform.finrem.caseorchestration.service.GeneralApplicationService;

import java.util.Collections;
import java.util.Optional;

import static org.apache.commons.collections4.ListUtils.emptyIfNull;

@Slf4j
@Service
public class GeneralApplicationAboutToSubmitHandler extends FinremCallbackHandler {

    private final GeneralApplicationHelper helper;
    private final GeneralApplicationService service;

    public GeneralApplicationAboutToSubmitHandler(FinremCaseDetailsMapper finremCaseDetailsMapper, GeneralApplicationHelper helper,
                                                       GeneralApplicationService service) {
        super(finremCaseDetailsMapper);
        this.helper = helper;
        this.service = service;
    }

    @Override
    public boolean canHandle(CallbackType callbackType, CaseType caseType, EventType eventType) {
        return CallbackType.ABOUT_TO_SUBMIT.equals(callbackType)
            && CaseType.CONTESTED.equals(caseType)
            && EventType.GENERAL_APPLICATION.equals(eventType);
    }

    @Override
    public GenericAboutToStartOrSubmitCallbackResponse<FinremCaseData> handle(FinremCallbackRequest callbackRequest,
                                                                                   String userAuthorisation) {
        FinremCaseDetails caseDetails = callbackRequest.getCaseDetails();
        String caseId = String.valueOf(caseDetails.getId());
        log.info("About to Submit callback event type {} for Case ID: {}",
            EventType.GENERAL_APPLICATION, caseId);

        logAppRespCollection(caseId, caseDetails.getData(), "1");

        FinremCaseData caseData = service.updateGeneralApplications(callbackRequest, userAuthorisation);

        logAppRespCollection(caseId, caseData, "2");

        helper.deleteNonCollectionGeneralApplication(caseData);

        logAppRespCollection(caseId, caseData, "3");
        logNoChangeInAppRespCollection(caseId, callbackRequest.getCaseDetailsBefore().getData(), caseData);

        return GenericAboutToStartOrSubmitCallbackResponse.<FinremCaseData>builder().data(caseData).build();
    }

    private void logAppRespCollection(String caseId, FinremCaseData caseData, String message) {
        int size = emptyIfNull(caseData.getGeneralApplicationWrapper().getAppRespGeneralApplications())
            .size();

        log.info("Case ID: {} AppResp GA {} {}", caseId, message, size);
    }

    private void logNoChangeInAppRespCollection(String caseId, FinremCaseData caseDataBefore, FinremCaseData caseData) {
        int size = Optional.ofNullable(caseData.getGeneralApplicationWrapper().getAppRespGeneralApplications())
            .orElse(Collections.emptyList())
            .size();

        int sizeBefore = Optional.ofNullable(caseDataBefore.getGeneralApplicationWrapper().getAppRespGeneralApplications())
            .orElse(Collections.emptyList())
            .size();

        if (size == sizeBefore) {
            log.error("Case ID: {} DFR-2698 AppResp GA no change {}", caseId, size);
        }
    }

}
