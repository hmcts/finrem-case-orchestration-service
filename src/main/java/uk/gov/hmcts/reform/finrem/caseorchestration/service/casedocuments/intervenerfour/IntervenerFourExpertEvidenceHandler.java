package uk.gov.hmcts.reform.finrem.caseorchestration.service.casedocuments.intervenerfour;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import uk.gov.hmcts.reform.finrem.caseorchestration.service.casedocuments.ExpertEvidenceHandler;

import static uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.CaseDocumentParty.INTERVENER_FOUR;
import static uk.gov.hmcts.reform.finrem.caseorchestration.model.document.CaseDocumentCollectionType.INTERVENER_FOUR_EXPERT_EVIDENCE_COLLECTION;

@Component
public class IntervenerFourExpertEvidenceHandler extends ExpertEvidenceHandler {

    @Autowired
    public IntervenerFourExpertEvidenceHandler() {
        super(INTERVENER_FOUR_EXPERT_EVIDENCE_COLLECTION, INTERVENER_FOUR);
    }
}
