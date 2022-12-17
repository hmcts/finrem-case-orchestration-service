package uk.gov.hmcts.reform.finrem.caseorchestration.service.casedocuments.respondent;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.CaseDocumentParty;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.document.ManageCaseDocumentsCollectionType;
import uk.gov.hmcts.reform.finrem.caseorchestration.service.casedocuments.FormEExhibitsCollectionService;

@Service
public class RespondentFormEExhibitsCollectionService extends FormEExhibitsCollectionService {

    @Autowired
    public RespondentFormEExhibitsCollectionService() {
        super(ManageCaseDocumentsCollectionType.RESP_FORM_E_EXHIBITS_COLLECTION,
                CaseDocumentParty.RESPONDENT);
    }

}
