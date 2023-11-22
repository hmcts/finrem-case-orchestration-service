package uk.gov.hmcts.reform.finrem.caseorchestration.service.casedocuments.intervenerfour;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.CaseDocumentType;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.document.DocumentCategory;
import uk.gov.hmcts.reform.finrem.caseorchestration.service.FeatureToggleService;
import uk.gov.hmcts.reform.finrem.caseorchestration.service.casedocuments.FormsHHandler;

import static uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.CaseDocumentParty.INTERVENER_FOUR;
import static uk.gov.hmcts.reform.finrem.caseorchestration.model.document.CaseDocumentCollectionType.INTERVENER_FOUR_FORM_H_COLLECTION;

@Component
public class IntervenerFourFormsHHandler extends FormsHHandler {

    @Autowired
    public IntervenerFourFormsHHandler(FeatureToggleService featureToggleService) {
        super(INTERVENER_FOUR_FORM_H_COLLECTION, INTERVENER_FOUR, featureToggleService);
    }

    @Override
    protected DocumentCategory getDocumentCategoryFromDocumentType(CaseDocumentType caseDocumentType) {
        return DocumentCategory.HEARING_DOCUMENTS_INTERVENER_4_COSTS_ESTIMATES_OR_FORM_H_OR_FORM_H1;
    }
}
