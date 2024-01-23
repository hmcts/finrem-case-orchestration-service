package uk.gov.hmcts.reform.finrem.caseorchestration.service.casedocuments.respondent;

import org.springframework.stereotype.Component;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.CaseDocumentParty;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.CaseDocumentType;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.document.CaseDocumentCollectionType;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.document.DocumentCategory;
import uk.gov.hmcts.reform.finrem.caseorchestration.service.FeatureToggleService;
import uk.gov.hmcts.reform.finrem.caseorchestration.service.casedocuments.FormEExhibitsHandler;

@Component
public class RespondentFormEExhibitsHandler extends FormEExhibitsHandler {

    public RespondentFormEExhibitsHandler(FeatureToggleService featureToggleService) {
        super(CaseDocumentCollectionType.RESP_FORM_E_EXHIBITS_COLLECTION,
            CaseDocumentParty.RESPONDENT, featureToggleService);
    }

    @Override
    protected DocumentCategory getDocumentCategoryFromDocumentType(CaseDocumentType caseDocumentType, CaseDocumentParty caseDocumentParty) {
        return DocumentCategory.RESPONDENT_DOCUMENTS_FORM_E;
    }

}
