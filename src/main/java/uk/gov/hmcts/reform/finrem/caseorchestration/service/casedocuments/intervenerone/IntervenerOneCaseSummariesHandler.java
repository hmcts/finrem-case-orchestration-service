package uk.gov.hmcts.reform.finrem.caseorchestration.service.casedocuments.intervenerone;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.CaseDocumentType;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.document.DocumentCategory;
import uk.gov.hmcts.reform.finrem.caseorchestration.service.FeatureToggleService;
import uk.gov.hmcts.reform.finrem.caseorchestration.service.casedocuments.CaseSummariesHandler;

import static uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.CaseDocumentParty.INTERVENER_ONE;
import static uk.gov.hmcts.reform.finrem.caseorchestration.model.document.CaseDocumentCollectionType.INTERVENER_ONE_SUMMARIES_COLLECTION;

@Component
public class IntervenerOneCaseSummariesHandler extends CaseSummariesHandler {

    @Autowired
    public IntervenerOneCaseSummariesHandler(FeatureToggleService featureToggleService) {
        super(INTERVENER_ONE_SUMMARIES_COLLECTION, INTERVENER_ONE, featureToggleService);
    }

    @Override
    protected DocumentCategory getDocumentCategoryFromDocumentType(CaseDocumentType caseDocumentType) {
        switch (caseDocumentType) {
            case POSITION_STATEMENT:
                return DocumentCategory.HEARING_DOCUMENTS_INTERVENER_1_POSITION_STATEMENT;
            case SKELETON_ARGUMENT:
                return DocumentCategory.HEARING_DOCUMENTS_INTERVENER_1_SKELETON_ARGUMENT;
            case CASE_SUMMARY:
                return DocumentCategory.HEARING_DOCUMENTS_INTERVENER_1_CASE_SUMMARY;
            default:
                return DocumentCategory.UNCATEGORISED;
        }
    }
}
