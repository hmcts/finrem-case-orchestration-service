package uk.gov.hmcts.reform.finrem.caseorchestration.service.casedocuments.respondent;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import uk.gov.hmcts.reform.finrem.caseorchestration.service.casedocuments.CaseSummariesManager;

import static uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.CCDConfigConstant.RESP_CASE_SUMMARIES_COLLECTION;

@Component
public class RespondentCaseSummariesManager extends CaseSummariesManager {

    @Autowired
    public RespondentCaseSummariesManager(ObjectMapper mapper) {
        super(RESP_CASE_SUMMARIES_COLLECTION, RESPONDENT, mapper);
    }
}
