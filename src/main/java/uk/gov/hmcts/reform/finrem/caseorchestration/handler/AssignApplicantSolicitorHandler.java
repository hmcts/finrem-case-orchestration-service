package uk.gov.hmcts.reform.finrem.caseorchestration.handler;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import uk.gov.hmcts.reform.ccd.client.model.CallbackRequest;
import uk.gov.hmcts.reform.finrem.caseorchestration.controllers.GenericAboutToStartOrSubmitCallbackResponse;
import uk.gov.hmcts.reform.finrem.caseorchestration.error.AssignCaseAccessException;
import uk.gov.hmcts.reform.finrem.caseorchestration.service.AssignApplicantSolicitorService;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

@Slf4j
@Service
@RequiredArgsConstructor
public abstract class AssignApplicantSolicitorHandler implements CallbackHandler {

    private final AssignApplicantSolicitorService assignApplicantSolicitorService;

    @Override
    public GenericAboutToStartOrSubmitCallbackResponse<Map<String, Object>> handle(CallbackRequest callbackRequest,
                                                                                   String userAuthorisation) {
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
