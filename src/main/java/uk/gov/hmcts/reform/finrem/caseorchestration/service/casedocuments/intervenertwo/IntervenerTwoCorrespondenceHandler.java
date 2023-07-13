package uk.gov.hmcts.reform.finrem.caseorchestration.service.casedocuments.intervenertwo;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import uk.gov.hmcts.reform.finrem.caseorchestration.service.casedocuments.CorrespondenceHandler;

import static uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.CaseDocumentParty.INTERVENER_TWO;
import static uk.gov.hmcts.reform.finrem.caseorchestration.model.document.CaseDocumentCollectionType.INTERVENER_TWO_CORRESPONDENCE_COLLECTION;

@Component
public class IntervenerTwoCorrespondenceHandler extends CorrespondenceHandler {

    @Autowired
    public IntervenerTwoCorrespondenceHandler() {
        super(INTERVENER_TWO_CORRESPONDENCE_COLLECTION, INTERVENER_TWO);
    }
}
