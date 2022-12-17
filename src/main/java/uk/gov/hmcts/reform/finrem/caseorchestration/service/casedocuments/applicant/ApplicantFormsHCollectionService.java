package uk.gov.hmcts.reform.finrem.caseorchestration.service.casedocuments.applicant;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.CaseDocumentParty;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.document.ManageCaseDocumentsCollectionType;
import uk.gov.hmcts.reform.finrem.caseorchestration.service.casedocuments.FormsHCollectionService;

@Service
public class ApplicantFormsHCollectionService extends FormsHCollectionService {

    @Autowired
    public ApplicantFormsHCollectionService() {
        super(ManageCaseDocumentsCollectionType.APP_FORMS_H_COLLECTION, CaseDocumentParty.APPLICANT);
    }
}
