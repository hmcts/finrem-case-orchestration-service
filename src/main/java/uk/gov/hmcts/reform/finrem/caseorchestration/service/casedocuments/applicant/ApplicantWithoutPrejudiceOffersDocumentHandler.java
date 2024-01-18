package uk.gov.hmcts.reform.finrem.caseorchestration.service.casedocuments.applicant;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.CaseDocumentParty;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.CaseDocumentType;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.document.DocumentCategory;
import uk.gov.hmcts.reform.finrem.caseorchestration.service.FeatureToggleService;
import uk.gov.hmcts.reform.finrem.caseorchestration.service.casedocuments.WithoutPrejudiceOffersDocumentsHandler;

@Component
public class ApplicantWithoutPrejudiceOffersDocumentHandler extends WithoutPrejudiceOffersDocumentsHandler {

    @Autowired
    protected ApplicantWithoutPrejudiceOffersDocumentHandler(FeatureToggleService featureToggleService) {
        super(CaseDocumentParty.APPLICANT, featureToggleService);
    }

    @Override
    protected DocumentCategory getDocumentCategoryFromDocumentType(CaseDocumentType caseDocumentType) {
        return DocumentCategory.FDR_DOCUMENTS_AND_FDR_BUNDLE_APPLICANT_WITHOUT_PREJUDICE_OFFERS;
    }
}
