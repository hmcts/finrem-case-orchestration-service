package uk.gov.hmcts.reform.finrem.caseorchestration.service.casedocuments.intervenerthree;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import uk.gov.hmcts.reform.finrem.caseorchestration.service.casedocuments.CaseSummariesHandler;

import static uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.CCDConfigConstant.INTV_THREE_CASE_SUMMARIES_COLLECTION;

@Component
public class IntervenerThreeCaseSummariesHandler extends CaseSummariesHandler {

    @Autowired
    public IntervenerThreeCaseSummariesHandler(ObjectMapper mapper) {
        super(INTV_THREE_CASE_SUMMARIES_COLLECTION, INTERVENER_THREE, mapper);
    }
}
