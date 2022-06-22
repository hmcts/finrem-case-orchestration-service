package uk.gov.hmcts.reform.finrem.caseorchestration.service.casedocuments.applicant;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import uk.gov.hmcts.reform.finrem.caseorchestration.service.casedocuments.ExpertEvidenceHandler;

import static uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.CCDConfigConstant.APP_EXPERT_EVIDENCE_COLLECTION;

@Component
public class ApplicantExpertEvidenceHandler extends ExpertEvidenceHandler {

    @Autowired
    public ApplicantExpertEvidenceHandler(ObjectMapper mapper) {
        super(APP_EXPERT_EVIDENCE_COLLECTION, APPLICANT, mapper);
    }
}
