package uk.gov.hmcts.reform.finrem.caseorchestration.service.casedocuments.applicant;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import uk.gov.hmcts.reform.finrem.caseorchestration.service.casedocuments.ChronologiesStatementsManager;

import static uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.CCDConfigConstant.APP_CHRONOLOGIES_STATEMENTS_COLLECTION;

@Component
public class ApplicantChronologiesStatementManager extends ChronologiesStatementsManager {

    @Autowired
    public ApplicantChronologiesStatementManager(ObjectMapper mapper) {
        super(APP_CHRONOLOGIES_STATEMENTS_COLLECTION, APPLICANT, mapper);
    }
}
