package uk.gov.hmcts.reform.finrem.caseorchestration.service.casedocuments.intervenerone;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import uk.gov.hmcts.reform.finrem.caseorchestration.service.casedocuments.FormEExhibitsHandler;

import static uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.CaseDocumentParty.INTERVENER_ONE;
import static uk.gov.hmcts.reform.finrem.caseorchestration.model.document.CaseDocumentCollectionType.INTERVENER_ONE_FORM_E_EXHIBITS_COLLECTION;

@Component
public class IntervenerOneFormEExhibitsHandler extends FormEExhibitsHandler {

    @Autowired
    public IntervenerOneFormEExhibitsHandler() {
        super(INTERVENER_ONE_FORM_E_EXHIBITS_COLLECTION, INTERVENER_ONE);
    }

}
