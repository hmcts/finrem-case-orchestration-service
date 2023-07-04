package uk.gov.hmcts.reform.finrem.caseorchestration.service.casedocuments.intervenerthree;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import uk.gov.hmcts.reform.finrem.caseorchestration.service.casedocuments.StatementExhibitsHandler;

import static uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.CCDConfigConstant.INTV_THREE_STATEMENTS_EXHIBITS_COLLECTION;

@Component
public class IntervenerThreeStatementsExhibitsHandler extends StatementExhibitsHandler {

    @Autowired
    public IntervenerThreeStatementsExhibitsHandler(ObjectMapper mapper) {
        super(INTV_THREE_STATEMENTS_EXHIBITS_COLLECTION, INTERVENER_THREE, mapper);
    }
}
