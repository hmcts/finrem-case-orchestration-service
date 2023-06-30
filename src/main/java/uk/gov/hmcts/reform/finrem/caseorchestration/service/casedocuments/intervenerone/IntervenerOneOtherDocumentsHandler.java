package uk.gov.hmcts.reform.finrem.caseorchestration.service.casedocuments.intervenerone;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import uk.gov.hmcts.reform.finrem.caseorchestration.service.casedocuments.OtherDocumentsHandler;

import static uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.CaseDocumentParty.INTERVENER_ONE;
import static uk.gov.hmcts.reform.finrem.caseorchestration.model.document.ManageCaseDocumentsCollectionType.INTV_ONE_OTHER_COLLECTION;

@Component
public class IntervenerOneOtherDocumentsHandler extends OtherDocumentsHandler {

    @Autowired
    public IntervenerOneOtherDocumentsHandler() {
        super(INTV_ONE_OTHER_COLLECTION, INTERVENER_ONE);
    }
}
