package uk.gov.hmcts.reform.finrem.caseorchestration.service.casedocuments.respondent;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.CaseDocumentParty;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.document.ManageCaseDocumentsCollectionType;
import uk.gov.hmcts.reform.finrem.caseorchestration.service.casedocuments.FormsHHandler;

@Service
public class RespondentFormsHHandler extends FormsHHandler {

    @Autowired
    public RespondentFormsHHandler() {
        super(ManageCaseDocumentsCollectionType.RESP_FORM_H_COLLECTION, CaseDocumentParty.RESPONDENT);
    }
}
