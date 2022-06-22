package uk.gov.hmcts.reform.finrem.caseorchestration.service.casedocuments.respondent;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import uk.gov.hmcts.reform.finrem.caseorchestration.service.casedocuments.StatementExhibitsHandler;

import static uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.CCDConfigConstant.RESP_STATEMENTS_EXHIBITS_COLLECTION;

@Component
public class RespondentStatementsExhibitsHandler extends StatementExhibitsHandler {

    @Autowired
    public RespondentStatementsExhibitsHandler(ObjectMapper mapper) {
        super(RESP_STATEMENTS_EXHIBITS_COLLECTION, RESPONDENT, mapper);
    }
}
