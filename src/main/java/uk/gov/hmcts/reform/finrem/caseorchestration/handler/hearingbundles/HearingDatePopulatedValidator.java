package uk.gov.hmcts.reform.finrem.caseorchestration.handler.hearingbundles;

import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.FinremCaseData;

import java.util.ArrayList;
import java.util.List;

@Component
@Slf4j
public class HearingDatePopulatedValidator {

    public List<String> validateHearingDate(FinremCaseData finremCaseData) {
        List<String> errors = new ArrayList<>();
        if (finremCaseData.getHearingDate() == null) {
            log.info("Hearing date for Case ID: {} not found", finremCaseData.getCcdCaseId());
            errors.add("Missing hearing date.");
        }
        return errors;
    }
}
