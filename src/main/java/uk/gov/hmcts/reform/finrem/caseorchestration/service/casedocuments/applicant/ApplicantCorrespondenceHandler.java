package uk.gov.hmcts.reform.finrem.caseorchestration.service.casedocuments.applicant;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.CaseDocumentParty;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.document.CaseDocumentCollectionType;
import uk.gov.hmcts.reform.finrem.caseorchestration.service.casedocuments.CorrespondenceHandler;

@Service
public class ApplicantCorrespondenceHandler extends CorrespondenceHandler {

    @Autowired
    public ApplicantCorrespondenceHandler() {
        super(CaseDocumentCollectionType.APPLICANT_CORRESPONDENCE_COLLECTION,
            CaseDocumentParty.APPLICANT);
    }
}
