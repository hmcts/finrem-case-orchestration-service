package uk.gov.hmcts.reform.finrem.caseorchestration.service.casedocuments.intervenertwo;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import uk.gov.hmcts.reform.finrem.caseorchestration.service.casedocuments.ExpertEvidenceHandler;

import static uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.CCDConfigConstant.INTV_TWO_EVIDENCE_COLLECTION;

@Component
public class IntervenerTwoExpertEvidenceHandler extends ExpertEvidenceHandler {

    @Autowired
    public IntervenerTwoExpertEvidenceHandler(ObjectMapper mapper) {
        super(INTV_TWO_EVIDENCE_COLLECTION, INTERVENER_TWO, mapper);
    }
}
