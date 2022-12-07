package uk.gov.hmcts.reform.finrem.caseorchestration.service.casedocuments.applicant;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.springframework.stereotype.Service;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.CaseDocumentParty;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.document.ManageCaseDocumentsCollectionType;
import uk.gov.hmcts.reform.finrem.caseorchestration.service.casedocuments.FormsHCollectionService;
import uk.gov.hmcts.reform.finrem.caseorchestration.service.evidencemanagement.EvidenceManagementDeleteService;

@Service
public class ApplicantFormsHCollectionService extends FormsHCollectionService {

    @Autowired
    public ApplicantFormsHCollectionService(EvidenceManagementDeleteService evidenceManagementDeleteService) {
        super(ManageCaseDocumentsCollectionType.APP_FORMS_H_COLLECTION, evidenceManagementDeleteService,
            CaseDocumentParty.APPLICANT);
    }
}
