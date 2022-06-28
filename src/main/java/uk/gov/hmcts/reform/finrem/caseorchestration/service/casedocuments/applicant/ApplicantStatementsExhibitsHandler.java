package uk.gov.hmcts.reform.finrem.caseorchestration.service.casedocuments.applicant;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import uk.gov.hmcts.reform.finrem.caseorchestration.service.casedocuments.StatementExhibitsHandler;

import static uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.CCDConfigConstant.APP_STATEMENTS_EXHIBITS_COLLECTION;

@Component
public class ApplicantStatementsExhibitsHandler extends StatementExhibitsHandler {

    @Autowired
    public ApplicantStatementsExhibitsHandler(ObjectMapper mapper) {
        super(APP_STATEMENTS_EXHIBITS_COLLECTION, APPLICANT, mapper);
    }
}
