package uk.gov.hmcts.reform.finrem.caseorchestration.service.casedocuments.respondent;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import uk.gov.hmcts.reform.finrem.caseorchestration.service.casedocuments.ExpertEvidenceHandler;

import static uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.CCDConfigConstant.RESP_EXPERT_EVIDENCE_COLLECTION;

@Component
public class RespondentExpertEvidenceHandler extends ExpertEvidenceHandler {

    @Autowired
    public RespondentExpertEvidenceHandler(ObjectMapper mapper) {
        super(RESP_EXPERT_EVIDENCE_COLLECTION, RESPONDENT, mapper);
    }
}
