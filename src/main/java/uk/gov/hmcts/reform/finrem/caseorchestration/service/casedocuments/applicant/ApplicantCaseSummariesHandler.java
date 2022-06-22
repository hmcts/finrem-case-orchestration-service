package uk.gov.hmcts.reform.finrem.caseorchestration.service.casedocuments.applicant;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import uk.gov.hmcts.reform.finrem.caseorchestration.service.casedocuments.CaseSummariesHandler;

import static uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.CCDConfigConstant.APP_CASE_SUMMARIES_COLLECTION;

@Component
public class ApplicantCaseSummariesHandler extends CaseSummariesHandler {

    @Autowired
    public ApplicantCaseSummariesHandler(ObjectMapper mapper) {
        super(APP_CASE_SUMMARIES_COLLECTION, APPLICANT, mapper);
    }
}
