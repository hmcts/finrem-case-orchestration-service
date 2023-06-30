package uk.gov.hmcts.reform.finrem.caseorchestration.service.casedocuments.intervenerthree;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import uk.gov.hmcts.reform.finrem.caseorchestration.service.casedocuments.FormsHHandler;

import static uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.CaseDocumentParty.INTERVENER_THREE;
import static uk.gov.hmcts.reform.finrem.caseorchestration.model.document.ManageCaseDocumentsCollectionType.INTV_THREE_FORMS_H_COLLECTION;

@Component
public class IntervenerThreeFormsHHandler extends FormsHHandler {

    @Autowired
    public IntervenerThreeFormsHHandler() {
        super(INTV_THREE_FORMS_H_COLLECTION, INTERVENER_THREE);
    }
}
