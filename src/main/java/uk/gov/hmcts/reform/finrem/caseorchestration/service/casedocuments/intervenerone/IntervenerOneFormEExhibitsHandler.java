package uk.gov.hmcts.reform.finrem.caseorchestration.service.casedocuments.intervenerone;

import org.springframework.stereotype.Component;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.CaseDocumentType;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.document.DocumentCategory;
import uk.gov.hmcts.reform.finrem.caseorchestration.service.FeatureToggleService;
import uk.gov.hmcts.reform.finrem.caseorchestration.service.casedocuments.FormEExhibitsHandler;

import static uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.CaseDocumentParty.INTERVENER_ONE;
import static uk.gov.hmcts.reform.finrem.caseorchestration.model.document.CaseDocumentCollectionType.INTERVENER_ONE_FORM_E_EXHIBITS_COLLECTION;

@Component
public class IntervenerOneFormEExhibitsHandler extends FormEExhibitsHandler {

    private final FeatureToggleService featureToggleService;

    public IntervenerOneFormEExhibitsHandler(FeatureToggleService featureToggleService) {
        super(INTERVENER_ONE_FORM_E_EXHIBITS_COLLECTION, INTERVENER_ONE, featureToggleService);
        this.featureToggleService = featureToggleService;
    }

    @Override
    protected DocumentCategory getDocumentCategoryFromDocumentType(CaseDocumentType caseDocumentType) {
        if (caseDocumentType == CaseDocumentType.APPLICANT_FORM_E) {
            return DocumentCategory.INTERVENER_DOCUMENTS;
        }
        return DocumentCategory.UNCATEGORISED;
    }

}
