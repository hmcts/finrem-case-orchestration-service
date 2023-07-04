package uk.gov.hmcts.reform.finrem.caseorchestration.service.casedocuments.intervenerfour;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import uk.gov.hmcts.reform.finrem.caseorchestration.service.casedocuments.ExpertEvidenceHandler;

import static uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.CCDConfigConstant.INTV_FOUR_EVIDENCE_COLLECTION;

@Component
public class IntervenerFourExpertEvidenceHandler extends ExpertEvidenceHandler {

    @Autowired
    public IntervenerFourExpertEvidenceHandler(ObjectMapper mapper) {
        super(INTV_FOUR_EVIDENCE_COLLECTION, INTERVENER_FOUR, mapper);
    }
}
