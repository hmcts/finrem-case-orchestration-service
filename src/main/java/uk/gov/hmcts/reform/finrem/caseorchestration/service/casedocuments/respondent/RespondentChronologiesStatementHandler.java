package uk.gov.hmcts.reform.finrem.caseorchestration.service.casedocuments.respondent;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import uk.gov.hmcts.reform.finrem.caseorchestration.service.casedocuments.ChronologiesStatementsHandler;

import static uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.CCDConfigConstant.RESP_CHRONOLOGIES_STATEMENTS_COLLECTION;

@Component
public class RespondentChronologiesStatementHandler extends ChronologiesStatementsHandler {

    @Autowired
    public RespondentChronologiesStatementHandler(ObjectMapper mapper) {
        super(RESP_CHRONOLOGIES_STATEMENTS_COLLECTION, RESPONDENT, mapper);
    }
}
