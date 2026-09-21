package uk.gov.hmcts.reform.finrem.caseorchestration.service.casedocuments.applicant;

import org.springframework.stereotype.Component;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.CaseDocumentParty;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.CaseDocumentType;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.document.CaseDocumentCollectionType;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.document.DocumentCategory;
import uk.gov.hmcts.reform.finrem.caseorchestration.service.FeatureToggleService;
import uk.gov.hmcts.reform.finrem.caseorchestration.service.casedocuments.UpdatingDisclosureHandler;

@Component
public class ApplicantUpdatingDisclosureHandler extends UpdatingDisclosureHandler {

    public ApplicantUpdatingDisclosureHandler(FeatureToggleService featureToggleService) {
        super(
            CaseDocumentCollectionType.APP_UPDATING_DISCLOSURE_COLLECTION,
            CaseDocumentParty.APPLICANT,
            featureToggleService
        );
    }

    @Override
    protected DocumentCategory getDocumentCategoryFromDocumentType(
        CaseDocumentType caseDocumentType,
        CaseDocumentParty caseDocumentParty) {

        if (CaseDocumentType.UPDATING_DISCLOSURE.equals(caseDocumentType)) {
            return DocumentCategory.APPLICANT_DOCUMENTS_UPDATING_DISCLOSURE;
        }

        return DocumentCategory.UNCATEGORISED;
    }
}
