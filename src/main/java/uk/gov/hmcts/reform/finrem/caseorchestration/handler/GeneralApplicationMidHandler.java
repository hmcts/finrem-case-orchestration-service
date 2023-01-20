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
import uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.CaseType;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.GeneralApplicationCollectionData;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

@Slf4j
@Service
@RequiredArgsConstructor
public class GeneralApplicationMidHandler implements CallbackHandler<Map<String, Object>> {

    private final GeneralApplicationHelper helper;

    @Override
    public boolean canHandle(CallbackType callbackType, CaseType caseType, EventType eventType) {
        return CallbackType.MID_EVENT.equals(callbackType)
            && CaseType.CONTESTED.equals(caseType)
            && (EventType.GENERAL_APPLICATION.equals(eventType));
    }

    @Override
    public GenericAboutToStartOrSubmitCallbackResponse<Map<String, Object>> handle(CallbackRequest callbackRequest,
                                                                                   String userAuthorisation) {
        CaseDetails caseDetails = callbackRequest.getCaseDetails();
        log.info("Mid callback event type {} for case id: {}", EventType.GENERAL_APPLICATION, caseDetails.getId());
        Map<String, Object> caseData = caseDetails.getData();
        List<String> errors  = new ArrayList<>();

        List<GeneralApplicationCollectionData> currentGeneralApplication = helper.getGeneralApplicationList(caseData);
        log.info("Please complete the general application for size {}", currentGeneralApplication.size());
        if (currentGeneralApplication.isEmpty()) {
            log.info("Please complete the general application for case Id {}", caseDetails.getId());
            errors.add("Please complete the General Application. No information has been entered for this application.");
        }

        List<GeneralApplicationCollectionData> beforeGeneralApplication =
            helper.getGeneralApplicationList(callbackRequest.getCaseDetailsBefore().getData());
        if (!beforeGeneralApplication.isEmpty() && beforeGeneralApplication.size() == currentGeneralApplication.size()) {
            log.info("Please complete the general application for case Id {}", caseDetails.getId());
            errors.add("Any changes to an existing General Applications will not be saved. "
                + "Please add a new General Application in order to progress.");
        }

        return GenericAboutToStartOrSubmitCallbackResponse.<Map<String, Object>>builder().data(caseData).errors(errors).build();
    }
}
