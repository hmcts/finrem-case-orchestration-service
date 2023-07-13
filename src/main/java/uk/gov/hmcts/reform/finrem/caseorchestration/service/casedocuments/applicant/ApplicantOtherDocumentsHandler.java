package uk.gov.hmcts.reform.finrem.caseorchestration.service.casedocuments.applicant;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.CaseDocumentParty;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.document.CaseDocumentCollectionType;
import uk.gov.hmcts.reform.finrem.caseorchestration.service.casedocuments.OtherDocumentsHandler;

@Service
public class ApplicantOtherDocumentsHandler extends OtherDocumentsHandler {

    @Autowired
    public ApplicantOtherDocumentsHandler() {
        super(CaseDocumentCollectionType.APP_OTHER_COLLECTION,
            CaseDocumentParty.APPLICANT);
    }
}
