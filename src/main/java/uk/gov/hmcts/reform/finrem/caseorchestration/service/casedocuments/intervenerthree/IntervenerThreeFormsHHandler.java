package uk.gov.hmcts.reform.finrem.caseorchestration.service.casedocuments.intervenerthree;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.CaseDocumentType;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.document.DocumentCategory;
import uk.gov.hmcts.reform.finrem.caseorchestration.service.FeatureToggleService;
import uk.gov.hmcts.reform.finrem.caseorchestration.service.casedocuments.FormsHHandler;

import static uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.CaseDocumentParty.INTERVENER_THREE;
import static uk.gov.hmcts.reform.finrem.caseorchestration.model.document.CaseDocumentCollectionType.INTERVENER_THREE_FORM_H_COLLECTION;

@Component
public class IntervenerThreeFormsHHandler extends FormsHHandler {

    @Autowired
    public IntervenerThreeFormsHHandler(FeatureToggleService featureToggleService) {
        super(INTERVENER_THREE_FORM_H_COLLECTION, INTERVENER_THREE, featureToggleService);
    }

    @Override
    protected DocumentCategory getDocumentCategoryFromDocumentType(CaseDocumentType caseDocumentType) {
        return DocumentCategory.HEARING_DOCUMENTS_INTERVENER_3_COSTS_ESTIMATES_OR_FORM_H_OR_FORM_H1;
    }
}
