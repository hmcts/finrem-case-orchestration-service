package uk.gov.hmcts.reform.finrem.caseorchestration.handler;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import uk.gov.hmcts.reform.ccd.client.model.CallbackRequest;
import uk.gov.hmcts.reform.finrem.caseorchestration.ccd.callback.CallbackType;
import uk.gov.hmcts.reform.finrem.caseorchestration.controllers.GenericAboutToStartOrSubmitCallbackResponse;
import uk.gov.hmcts.reform.finrem.caseorchestration.error.AssignCaseAccessException;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.CaseType;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.EventType;
import uk.gov.hmcts.reform.finrem.caseorchestration.service.AssignApplicantSolicitorService;
import uk.gov.hmcts.reform.finrem.caseorchestration.service.CreateCaseService;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

@Slf4j
@Service
@RequiredArgsConstructor
public class SolicitorCreateConsentedSubmittedHandler implements CallbackHandler {


    private final CreateCaseService createCaseService;
    private final AssignApplicantSolicitorService assignApplicantSolicitorService;

    @Override
    public boolean canHandle(CallbackType callbackType, CaseType caseType, EventType eventType) {
        return CallbackType.SUBMITTED.equals(callbackType)
            && CaseType.CONSENTED.equals(caseType)
            && (EventType.SOLICITOR_CREATE.equals(eventType));
    }

    @Override
    public GenericAboutToStartOrSubmitCallbackResponse<Map<String, Object>> handle(CallbackRequest callbackRequest,
                                                                              String userAuthorisation) {
        log.info("Processing Submitted callback for event {} with Case ID : {}",
            EventType.SOLICITOR_CREATE, callbackRequest.getCaseDetails().getId());

        createCaseService.setSupplementaryData(callbackRequest, userAuthorisation);
        try {
            assignApplicantSolicitorService.setApplicantSolicitor(callbackRequest, userAuthorisation);
            return GenericAboutToStartOrSubmitCallbackResponse.<Map<String, Object>>builder().build();
        } catch (AssignCaseAccessException e) {
            log.info("Assigning case access failed for Case ID: {}", callbackRequest.getCaseDetails().getId());

            List<String> err = new ArrayList<>();
            err.add(e.getMessage());
            err.add("Failed to assign applicant solicitor to case, please ensure you have selected the correct applicant organisation on case");

            return GenericAboutToStartOrSubmitCallbackResponse.<Map<String, Object>>builder().errors(err).build();
        }
    }

}

