package uk.gov.hmcts.reform.finrem.caseorchestration.service.casedocuments.intervenerfour;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import uk.gov.hmcts.reform.finrem.caseorchestration.service.casedocuments.FormEExhibitsHandler;

import static uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.CaseDocumentParty.INTERVENER_FOUR;
import static uk.gov.hmcts.reform.finrem.caseorchestration.model.document.ManageCaseDocumentsCollectionType.INTV_FOUR_FORM_E_EXHIBITS_COLLECTION;

@Component
public class IntervenerFourFormEExhibitsHandler extends FormEExhibitsHandler {

    @Autowired
    public IntervenerFourFormEExhibitsHandler() {
        super(INTV_FOUR_FORM_E_EXHIBITS_COLLECTION, INTERVENER_FOUR);
    }

}
