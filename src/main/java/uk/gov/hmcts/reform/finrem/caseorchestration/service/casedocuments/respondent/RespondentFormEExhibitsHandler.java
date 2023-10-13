package uk.gov.hmcts.reform.finrem.caseorchestration.service.casedocuments.respondent;

import org.springframework.stereotype.Service;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.CaseDocumentParty;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.CaseDocumentType;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.document.CaseDocumentCollectionType;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.document.DocumentCategory;
import uk.gov.hmcts.reform.finrem.caseorchestration.service.FeatureToggleService;
import uk.gov.hmcts.reform.finrem.caseorchestration.service.casedocuments.FormEExhibitsHandler;

@Service
public class RespondentFormEExhibitsHandler extends FormEExhibitsHandler {

    private final FeatureToggleService featureToggleService;

    public RespondentFormEExhibitsHandler(FeatureToggleService featureToggleService) {
        super(CaseDocumentCollectionType.RESP_FORM_E_EXHIBITS_COLLECTION,
                CaseDocumentParty.RESPONDENT, featureToggleService);
        this.featureToggleService = featureToggleService;
    }

    @Override
    protected DocumentCategory getDocumentCategoryFromDocumentType(CaseDocumentType caseDocumentType) {
        if (caseDocumentType == CaseDocumentType.APPLICANT_FORM_E) {
            return DocumentCategory.RESPONDENT_DOCUMENTS;
        }
        return DocumentCategory.UNCATEGORISED;
    }

}
