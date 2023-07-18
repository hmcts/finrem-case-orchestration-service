package uk.gov.hmcts.reform.finrem.caseorchestration.service.casedocuments.respondent;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import uk.gov.hmcts.reform.finrem.caseorchestration.service.casedocuments.CaseSummariesHandler;

import static uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.CCDConfigConstant.RESP_CASE_SUMMARIES_COLLECTION;

@Component
public class RespondentCaseSummariesHandler extends CaseSummariesHandler {

    @Autowired
    public RespondentCaseSummariesHandler(ObjectMapper mapper) {
        super(RESP_CASE_SUMMARIES_COLLECTION, RESPONDENT, mapper);
    }
}
