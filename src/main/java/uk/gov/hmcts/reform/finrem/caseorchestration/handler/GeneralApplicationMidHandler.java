package uk.gov.hmcts.reform.finrem.caseorchestration.handler;

import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import uk.gov.hmcts.reform.finrem.caseorchestration.ccd.callback.CallbackType;
import uk.gov.hmcts.reform.finrem.caseorchestration.controllers.GenericAboutToStartOrSubmitCallbackResponse;
import uk.gov.hmcts.reform.finrem.caseorchestration.helper.GeneralApplicationHelper;
import uk.gov.hmcts.reform.finrem.caseorchestration.mapper.FinremCaseDetailsMapper;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.EventType;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.CaseType;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.FinremCaseDataContested;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.wrapper.GeneralApplicationsCollection;

import java.util.ArrayList;
import java.util.List;

@Slf4j
@Service
public class GeneralApplicationMidHandler extends FinremCallbackHandler<FinremCaseDataContested> {

    private final GeneralApplicationHelper helper;

    public GeneralApplicationMidHandler(FinremCaseDetailsMapper finremCaseDetailsMapper, GeneralApplicationHelper helper) {
        super(finremCaseDetailsMapper);
        this.helper = helper;
    }

    @Override
    public boolean canHandle(CallbackType callbackType, CaseType caseType, EventType eventType) {
        return CallbackType.MID_EVENT.equals(callbackType)
            && CaseType.CONTESTED.equals(caseType)
            && (EventType.GENERAL_APPLICATION.equals(eventType));
    }

    @Override
    public GenericAboutToStartOrSubmitCallbackResponse<FinremCaseDataContested> handle(FinremCallbackRequest<FinremCaseDataContested> callbackRequest,
                                                                                       String userAuthorisation) {

        FinremCaseDataContested caseData = callbackRequest.getCaseDetails().getData();
        log.info("Mid callback event type {} for case id: {}", EventType.GENERAL_APPLICATION, caseData.getCcdCaseId());
        List<String> errors = new ArrayList<>();

        List<GeneralApplicationsCollection> generalApplications = caseData.getGeneralApplicationWrapper().getGeneralApplications();
        if (generalApplications == null || generalApplications.isEmpty()) {
            log.info("Please complete the general application for case Id {}", caseData.getCcdCaseId());
            errors.add("Please complete the General Application. No information has been entered for this application.");
        }
        FinremCaseDataContested caseDataBefore = callbackRequest.getCaseDetailsBefore().getData();

        List<GeneralApplicationsCollection> generalApplicationsBefore = caseDataBefore.getGeneralApplicationWrapper().getGeneralApplications();

        if (generalApplicationsBefore != null && generalApplications != null && (generalApplicationsBefore.size() == generalApplications.size())) {
            log.info("Please complete the general application for case Id {}", caseData.getCcdCaseId());
            errors.add("Any changes to an existing General Applications will not be saved. "
                + "Please add a new General Application in order to progress.");
        }

        return GenericAboutToStartOrSubmitCallbackResponse.<FinremCaseDataContested>builder()
            .data(caseData).errors(errors).build();
    }
}
