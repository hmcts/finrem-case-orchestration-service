package uk.gov.hmcts.reform.finrem.caseorchestration.service.casedocuments.intervenertwo;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import uk.gov.hmcts.reform.finrem.caseorchestration.service.casedocuments.ChronologiesStatementsHandler;

import static uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.CCDConfigConstant.INTV_TWO_CHRONOLOGIES_STATEMENTS_COLLECTION;

@Component
public class IntervenerTwoChronologiesStatementHandler extends ChronologiesStatementsHandler {

    @Autowired
    public IntervenerTwoChronologiesStatementHandler(ObjectMapper mapper) {
        super(INTV_TWO_CHRONOLOGIES_STATEMENTS_COLLECTION, INTERVENER_TWO, mapper);
    }
}
