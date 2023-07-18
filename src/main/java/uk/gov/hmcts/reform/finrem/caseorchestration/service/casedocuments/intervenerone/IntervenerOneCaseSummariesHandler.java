package uk.gov.hmcts.reform.finrem.caseorchestration.service.casedocuments.intervenerone;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import uk.gov.hmcts.reform.finrem.caseorchestration.service.casedocuments.CaseSummariesHandler;

import static uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.CCDConfigConstant.INTV_ONE_CASE_SUMMARIES_COLLECTION;

@Component
public class IntervenerOneCaseSummariesHandler extends CaseSummariesHandler {

    @Autowired
    public IntervenerOneCaseSummariesHandler(ObjectMapper mapper) {
        super(INTV_ONE_CASE_SUMMARIES_COLLECTION, INTERVENER_ONE, mapper);
    }
}
