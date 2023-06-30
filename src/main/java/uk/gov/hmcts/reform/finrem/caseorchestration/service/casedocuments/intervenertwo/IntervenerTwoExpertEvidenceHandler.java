package uk.gov.hmcts.reform.finrem.caseorchestration.service.casedocuments.intervenertwo;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import uk.gov.hmcts.reform.finrem.caseorchestration.service.casedocuments.ExpertEvidenceHandler;

import static uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.CaseDocumentParty.INTERVENER_TWO;
import static uk.gov.hmcts.reform.finrem.caseorchestration.model.document.ManageCaseDocumentsCollectionType.INTV_TWO_EVIDENCE_COLLECTION;

@Component
public class IntervenerTwoExpertEvidenceHandler extends ExpertEvidenceHandler {

    @Autowired
    public IntervenerTwoExpertEvidenceHandler() {
        super(INTV_TWO_EVIDENCE_COLLECTION, INTERVENER_TWO);
    }
}
