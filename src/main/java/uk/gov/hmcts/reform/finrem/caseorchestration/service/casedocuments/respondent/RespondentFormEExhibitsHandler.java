package uk.gov.hmcts.reform.finrem.caseorchestration.service.casedocuments.respondent;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.CaseDocumentParty;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.document.ManageCaseDocumentsCollectionType;
import uk.gov.hmcts.reform.finrem.caseorchestration.service.casedocuments.FormEExhibitsHandler;

@Service
public class RespondentFormEExhibitsHandler extends FormEExhibitsHandler {

    @Autowired
    public RespondentFormEExhibitsHandler() {
        super(ManageCaseDocumentsCollectionType.RESP_FORM_E_EXHIBITS_COLLECTION,
                CaseDocumentParty.RESPONDENT);
    }

}
