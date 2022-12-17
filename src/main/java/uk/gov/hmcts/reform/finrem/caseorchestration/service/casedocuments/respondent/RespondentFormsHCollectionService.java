package uk.gov.hmcts.reform.finrem.caseorchestration.service.casedocuments.respondent;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.CaseDocumentParty;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.document.ManageCaseDocumentsCollectionType;
import uk.gov.hmcts.reform.finrem.caseorchestration.service.casedocuments.FormsHCollectionService;

@Service
public class RespondentFormsHCollectionService extends FormsHCollectionService {

    @Autowired
    public RespondentFormsHCollectionService() {
        super(ManageCaseDocumentsCollectionType.RESP_FORM_H_COLLECTION, CaseDocumentParty.RESPONDENT);
    }
}
