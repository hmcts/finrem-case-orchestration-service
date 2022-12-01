package uk.gov.hmcts.reform.finrem.caseorchestration.service.casedocuments.respondent;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import uk.gov.hmcts.reform.finrem.caseorchestration.service.casedocuments.StatementExhibitsManager;

import static uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.CCDConfigConstant.RESP_STATEMENTS_EXHIBITS_COLLECTION;

@Component
public class RespondentStatementsExhibitsManager extends StatementExhibitsManager {

    @Autowired
    public RespondentStatementsExhibitsManager(ObjectMapper mapper) {
        super(RESP_STATEMENTS_EXHIBITS_COLLECTION, RESPONDENT, mapper);
    }
}
