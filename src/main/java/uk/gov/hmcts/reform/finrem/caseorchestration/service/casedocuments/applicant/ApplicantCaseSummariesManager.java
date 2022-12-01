package uk.gov.hmcts.reform.finrem.caseorchestration.service.casedocuments.applicant;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import uk.gov.hmcts.reform.finrem.caseorchestration.service.casedocuments.CaseSummariesManager;

import static uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.CCDConfigConstant.APP_CASE_SUMMARIES_COLLECTION;

@Component
public class ApplicantCaseSummariesManager extends CaseSummariesManager {

    @Autowired
    public ApplicantCaseSummariesManager(ObjectMapper mapper) {
        super(APP_CASE_SUMMARIES_COLLECTION, APPLICANT, mapper);
    }
}
