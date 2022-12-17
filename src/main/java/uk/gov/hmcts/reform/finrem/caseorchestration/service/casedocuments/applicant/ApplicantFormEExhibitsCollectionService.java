package uk.gov.hmcts.reform.finrem.caseorchestration.service.casedocuments.applicant;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.CaseDocumentParty;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.document.ManageCaseDocumentsCollectionType;
import uk.gov.hmcts.reform.finrem.caseorchestration.service.casedocuments.FormEExhibitsCollectionService;

@Service
public class ApplicantFormEExhibitsCollectionService extends FormEExhibitsCollectionService {

    @Autowired
    public ApplicantFormEExhibitsCollectionService() {
        super(ManageCaseDocumentsCollectionType.APP_FORM_E_EXHIBITS_COLLECTION,
            CaseDocumentParty.APPLICANT);
    }

}
