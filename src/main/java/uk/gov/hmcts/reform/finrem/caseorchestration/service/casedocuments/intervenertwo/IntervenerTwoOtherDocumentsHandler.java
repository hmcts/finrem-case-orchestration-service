package uk.gov.hmcts.reform.finrem.caseorchestration.service.casedocuments.intervenertwo;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import uk.gov.hmcts.reform.finrem.caseorchestration.service.casedocuments.OtherDocumentsHandler;

import static uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.CaseDocumentParty.INTERVENER_TWO;
import static uk.gov.hmcts.reform.finrem.caseorchestration.model.document.CaseDocumentCollectionType.INTERVENER_TWO_OTHER_COLLECTION;

@Component
public class IntervenerTwoOtherDocumentsHandler extends OtherDocumentsHandler {

    @Autowired
    public IntervenerTwoOtherDocumentsHandler() {
        super(INTERVENER_TWO_OTHER_COLLECTION, INTERVENER_TWO);
    }
}
