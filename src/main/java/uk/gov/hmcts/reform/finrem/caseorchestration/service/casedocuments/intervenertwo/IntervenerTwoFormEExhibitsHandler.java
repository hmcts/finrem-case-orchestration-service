package uk.gov.hmcts.reform.finrem.caseorchestration.service.casedocuments.intervenertwo;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import uk.gov.hmcts.reform.finrem.caseorchestration.service.casedocuments.FormEExhibitsHandler;

import static uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.CaseDocumentParty.INTERVENER_TWO;
import static uk.gov.hmcts.reform.finrem.caseorchestration.model.document.CaseDocumentCollectionType.INTERVENER_TWO_FORM_E_EXHIBITS_COLLECTION;

@Component
public class IntervenerTwoFormEExhibitsHandler extends FormEExhibitsHandler {

    @Autowired
    public IntervenerTwoFormEExhibitsHandler() {
        super(INTERVENER_TWO_FORM_E_EXHIBITS_COLLECTION, INTERVENER_TWO);
    }

}
