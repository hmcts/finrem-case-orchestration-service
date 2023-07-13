package uk.gov.hmcts.reform.finrem.caseorchestration.service.casedocuments.intervenertwo;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import uk.gov.hmcts.reform.finrem.caseorchestration.service.casedocuments.FormsHHandler;

import static uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.CaseDocumentParty.INTERVENER_TWO;
import static uk.gov.hmcts.reform.finrem.caseorchestration.model.document.CaseDocumentCollectionType.INTERVENER_TWO_FORM_H_COLLECTION;

@Component
public class IntervenerTwoFormsHHandler extends FormsHHandler {

    @Autowired
    public IntervenerTwoFormsHHandler() {
        super(INTERVENER_TWO_FORM_H_COLLECTION, INTERVENER_TWO);
    }
}
