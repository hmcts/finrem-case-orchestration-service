package uk.gov.hmcts.reform.finrem.caseorchestration.service.casedocuments.applicant;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.CaseDocumentParty;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.document.ManageCaseDocumentsCollectionType;
import uk.gov.hmcts.reform.finrem.caseorchestration.service.casedocuments.OtherDocumentsCollectionService;

@Service
public class ApplicantOtherDocumentsCollectionService extends OtherDocumentsCollectionService {

    @Autowired
    public ApplicantOtherDocumentsCollectionService() {
        super(ManageCaseDocumentsCollectionType.APP_OTHER_COLLECTION,
            CaseDocumentParty.APPLICANT);
    }
}
