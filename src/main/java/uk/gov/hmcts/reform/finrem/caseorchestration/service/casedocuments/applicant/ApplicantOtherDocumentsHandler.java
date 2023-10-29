package uk.gov.hmcts.reform.finrem.caseorchestration.service.casedocuments.applicant;

import org.springframework.stereotype.Service;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.CaseDocumentParty;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.CaseDocumentType;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.document.CaseDocumentCollectionType;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.document.DocumentCategory;
import uk.gov.hmcts.reform.finrem.caseorchestration.service.FeatureToggleService;
import uk.gov.hmcts.reform.finrem.caseorchestration.service.casedocuments.OtherDocumentsHandler;

@Service
public class ApplicantOtherDocumentsHandler extends OtherDocumentsHandler {

    public ApplicantOtherDocumentsHandler(FeatureToggleService featureToggleService) {
        super(CaseDocumentCollectionType.APP_OTHER_COLLECTION,
            CaseDocumentParty.APPLICANT, featureToggleService);
    }

    @Override
    protected DocumentCategory getDocumentCategoryFromDocumentType(CaseDocumentType caseDocumentType) {
        switch (caseDocumentType) {
            case OTHER -> {
                return DocumentCategory.APPLICANT_DOCUMENTS_MISCELLANEOUS_OR_OTHER;
            }
            case PENSION_PLAN -> {
                return DocumentCategory.APPLICANT_DOCUMENTS_PENSION_PLAN;
            }
            default -> {
                return DocumentCategory.APPLICANT_DOCUMENTS;
            }
        }
    }
}
