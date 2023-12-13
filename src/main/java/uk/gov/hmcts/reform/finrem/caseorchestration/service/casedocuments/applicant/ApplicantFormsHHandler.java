package uk.gov.hmcts.reform.finrem.caseorchestration.service.casedocuments.applicant;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.CaseDocumentParty;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.CaseDocumentType;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.document.CaseDocumentCollectionType;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.document.DocumentCategory;
import uk.gov.hmcts.reform.finrem.caseorchestration.service.FeatureToggleService;
import uk.gov.hmcts.reform.finrem.caseorchestration.service.casedocuments.FormsHHandler;

@Service
public class ApplicantFormsHHandler extends FormsHHandler {

    @Autowired
    public ApplicantFormsHHandler(FeatureToggleService featureToggleService) {
        super(CaseDocumentCollectionType.APP_FORMS_H_COLLECTION, CaseDocumentParty.APPLICANT, featureToggleService);
    }

    @Override
    protected DocumentCategory getDocumentCategoryFromDocumentType(CaseDocumentType caseDocumentType) {
        return DocumentCategory.HEARING_DOCUMENTS_APPLICANT_COSTS_ESTIMATES_OR_FORM_H_OR_FORM_H1;
    }
}
