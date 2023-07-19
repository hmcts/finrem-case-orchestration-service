package uk.gov.hmcts.reform.finrem.caseorchestration.handler;

import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import uk.gov.hmcts.reform.finrem.caseorchestration.controllers.GenericAboutToStartOrSubmitCallbackResponse;
import uk.gov.hmcts.reform.finrem.caseorchestration.error.AssignCaseAccessException;
import uk.gov.hmcts.reform.finrem.caseorchestration.mapper.FinremCaseDetailsMapper;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.FinremCaseData;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.FinremCaseDetails;
import uk.gov.hmcts.reform.finrem.caseorchestration.service.AssignApplicantSolicitorService;

import java.util.ArrayList;
import java.util.List;

@Slf4j
@Service
public abstract class AssignApplicantSolicitorHandler extends FinremCallbackHandler {

    private final AssignApplicantSolicitorService assignApplicantSolicitorService;

    protected AssignApplicantSolicitorHandler(FinremCaseDetailsMapper finremCaseDetailsMapper,
                                           AssignApplicantSolicitorService assignApplicantSolicitorService) {
        super(finremCaseDetailsMapper);
        this.assignApplicantSolicitorService = assignApplicantSolicitorService;
    }

    @Override
    public GenericAboutToStartOrSubmitCallbackResponse<FinremCaseData> handle(FinremCallbackRequest callbackRequest,
                                                                              String userAuthorisation) {
        FinremCaseDetails finremCaseDetails = callbackRequest.getCaseDetails();

        try {
            assignApplicantSolicitorService.setApplicantSolicitor(finremCaseDetails, userAuthorisation);
            return GenericAboutToStartOrSubmitCallbackResponse.<FinremCaseData>builder().build();
        } catch (AssignCaseAccessException e) {
            log.info("Assigning case access failed for Case ID: {}", callbackRequest.getCaseDetails().getId());

            List<String> err = new ArrayList<>();
            err.add(e.getMessage());
            err.add("Failed to assign applicant solicitor to case, please ensure you have selected the correct applicant organisation on case");

            return GenericAboutToStartOrSubmitCallbackResponse.<FinremCaseData>builder().errors(err).build();
        }

    }
}
