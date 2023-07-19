package uk.gov.hmcts.reform.finrem.caseorchestration.service.casedocuments.intervenerthree;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import uk.gov.hmcts.reform.finrem.caseorchestration.service.casedocuments.FormsHHandler;

import static uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.CaseDocumentParty.INTERVENER_THREE;
import static uk.gov.hmcts.reform.finrem.caseorchestration.model.document.CaseDocumentCollectionType.INTERVENER_THREE_FORM_H_COLLECTION;

@Component
public class IntervenerThreeFormsHHandler extends FormsHHandler {

    @Autowired
    public IntervenerThreeFormsHHandler() {
        super(INTERVENER_THREE_FORM_H_COLLECTION, INTERVENER_THREE);
    }
}
