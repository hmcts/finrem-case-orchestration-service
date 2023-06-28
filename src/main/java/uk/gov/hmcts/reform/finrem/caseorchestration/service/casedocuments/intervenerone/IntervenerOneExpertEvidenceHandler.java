package uk.gov.hmcts.reform.finrem.caseorchestration.service.casedocuments.intervenerone;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import uk.gov.hmcts.reform.finrem.caseorchestration.service.casedocuments.ExpertEvidenceHandler;

import static uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.CCDConfigConstant.INTV_ONE_EVIDENCE_COLLECTION;

@Component
public class IntervenerOneExpertEvidenceHandler extends ExpertEvidenceHandler {

    @Autowired
    public IntervenerOneExpertEvidenceHandler(ObjectMapper mapper) {
        super(INTV_ONE_EVIDENCE_COLLECTION, INTERVENER_ONE, mapper);
    }
}
