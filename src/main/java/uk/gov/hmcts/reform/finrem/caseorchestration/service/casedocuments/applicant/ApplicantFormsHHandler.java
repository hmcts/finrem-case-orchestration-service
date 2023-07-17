package uk.gov.hmcts.reform.finrem.caseorchestration.service.casedocuments.applicant;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.CaseDocumentParty;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.document.CaseDocumentCollectionType;
import uk.gov.hmcts.reform.finrem.caseorchestration.service.casedocuments.FormsHHandler;

@Service
public class ApplicantFormsHHandler extends FormsHHandler {

    @Autowired
    public ApplicantFormsHHandler() {
        super(CaseDocumentCollectionType.APP_FORMS_H_COLLECTION, CaseDocumentParty.APPLICANT);
    }
}
