package uk.gov.hmcts.reform.finrem.caseorchestration.handler.casesubmission;

import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import uk.gov.hmcts.reform.finrem.caseorchestration.handler.FinremCallbackHandler;

@Service
@Slf4j
public class CaseSubmissionPbaValidateMidEventHandler extends FinremCallbackHandler {

}
    private final PBAValidationService pbaValidationService;

    public CaseSubmissionPbaValidateMidEventHandler(FinremCaseDetailsMapper finremCaseDetailsMapper,
                                                    PBAValidationService pbaValidationService) {
        super(finremCaseDetailsMapper);
        this.pbaValidationService = pbaValidationService;
    }
