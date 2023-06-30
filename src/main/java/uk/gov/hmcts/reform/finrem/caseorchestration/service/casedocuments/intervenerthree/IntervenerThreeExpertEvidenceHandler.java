package uk.gov.hmcts.reform.finrem.caseorchestration.service.casedocuments.intervenerthree;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import uk.gov.hmcts.reform.finrem.caseorchestration.service.casedocuments.ExpertEvidenceHandler;

import static uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.CaseDocumentParty.INTERVENER_THREE;
import static uk.gov.hmcts.reform.finrem.caseorchestration.model.document.ManageCaseDocumentsCollectionType.INTV_THREE_EVIDENCE_COLLECTION;

@Component
public class IntervenerThreeExpertEvidenceHandler extends ExpertEvidenceHandler {

    @Autowired
    public IntervenerThreeExpertEvidenceHandler() {
        super(INTV_THREE_EVIDENCE_COLLECTION, INTERVENER_THREE);
    }
}
