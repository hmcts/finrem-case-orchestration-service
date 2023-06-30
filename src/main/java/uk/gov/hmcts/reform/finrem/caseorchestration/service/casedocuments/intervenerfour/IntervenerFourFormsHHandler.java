package uk.gov.hmcts.reform.finrem.caseorchestration.service.casedocuments.intervenerfour;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import uk.gov.hmcts.reform.finrem.caseorchestration.service.casedocuments.FormsHHandler;

import static uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.CaseDocumentParty.INTERVENER_FOUR;
import static uk.gov.hmcts.reform.finrem.caseorchestration.model.document.ManageCaseDocumentsCollectionType.INTV_FOUR_FORMS_H_COLLECTION;

@Component
public class IntervenerFourFormsHHandler extends FormsHHandler {

    @Autowired
    public IntervenerFourFormsHHandler() {
        super(INTV_FOUR_FORMS_H_COLLECTION, INTERVENER_FOUR);
    }
}
