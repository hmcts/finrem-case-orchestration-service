package uk.gov.hmcts.reform.finrem.caseorchestration.service.casedocuments.respondent;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.CaseDocumentParty;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.CaseDocumentType;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.document.DocumentCategory;
import uk.gov.hmcts.reform.finrem.caseorchestration.service.FeatureToggleService;
import uk.gov.hmcts.reform.finrem.caseorchestration.service.casedocuments.WithoutPrejudiceOffersDocumentsHandler;

@Component
public class RespondentWithoutPrejudiceOffersDocumentHandler extends WithoutPrejudiceOffersDocumentsHandler {

    @Autowired
    protected RespondentWithoutPrejudiceOffersDocumentHandler(FeatureToggleService featureToggleService) {
        super(CaseDocumentParty.RESPONDENT, featureToggleService);
    }

    @Override
    protected DocumentCategory getDocumentCategoryFromDocumentType(CaseDocumentType caseDocumentType) {
        return DocumentCategory.FDR_DOCUMENTS_AND_FDR_BUNDLE_RESPONDENT_WITHOUT_PREJUDICE_OFFERS;
    }
}
