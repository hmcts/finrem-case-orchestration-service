package uk.gov.hmcts.reform.finrem.caseorchestration.service.casedocuments.intervenerfour;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import uk.gov.hmcts.reform.finrem.caseorchestration.service.casedocuments.ChronologiesStatementsHandler;

import static uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.CCDConfigConstant.INTV_FOUR_CHRONOLOGIES_STATEMENTS_COLLECTION;

@Component
public class IntervenerFourChronologiesStatementHandler extends ChronologiesStatementsHandler {

    @Autowired
    public IntervenerFourChronologiesStatementHandler(ObjectMapper mapper) {
        super(INTV_FOUR_CHRONOLOGIES_STATEMENTS_COLLECTION, INTERVENER_FOUR, mapper);
    }
}
