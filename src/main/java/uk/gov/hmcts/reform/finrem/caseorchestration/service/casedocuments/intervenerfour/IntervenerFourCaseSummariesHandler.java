package uk.gov.hmcts.reform.finrem.caseorchestration.service.casedocuments.intervenerfour;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import uk.gov.hmcts.reform.finrem.caseorchestration.service.casedocuments.CaseSummariesHandler;

import static uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.CCDConfigConstant.INTV_FOUR_CASE_SUMMARIES_COLLECTION;

@Component
public class IntervenerFourCaseSummariesHandler extends CaseSummariesHandler {

    @Autowired
    public IntervenerFourCaseSummariesHandler(ObjectMapper mapper) {
        super(INTV_FOUR_CASE_SUMMARIES_COLLECTION, INTERVENER_FOUR, mapper);
    }
}
