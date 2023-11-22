package uk.gov.hmcts.reform.finrem.caseorchestration.service.casedocuments.intervenerfour;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.CaseDocumentType;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.document.DocumentCategory;
import uk.gov.hmcts.reform.finrem.caseorchestration.service.FeatureToggleService;
import uk.gov.hmcts.reform.finrem.caseorchestration.service.casedocuments.CaseSummariesHandler;

import static uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.CaseDocumentParty.INTERVENER_FOUR;
import static uk.gov.hmcts.reform.finrem.caseorchestration.model.document.CaseDocumentCollectionType.INTERVENER_FOUR_SUMMARIES_COLLECTION;

@Component
public class IntervenerFourCaseSummariesHandler extends CaseSummariesHandler {

    @Autowired
    public IntervenerFourCaseSummariesHandler(FeatureToggleService featureToggleService) {
        super(INTERVENER_FOUR_SUMMARIES_COLLECTION, INTERVENER_FOUR, featureToggleService);
    }

    @Override
    protected DocumentCategory getDocumentCategoryFromDocumentType(CaseDocumentType caseDocumentType) {
        switch (caseDocumentType) {
            case POSITION_STATEMENT:
                return DocumentCategory.HEARING_DOCUMENTS_INTERVENER_4_POSITION_STATEMENT;
            case SKELETON_ARGUMENT:
                return DocumentCategory.HEARING_DOCUMENTS_INTERVENER_4_SKELETON_ARGUMENT;
            case CASE_SUMMARY:
                return DocumentCategory.HEARING_DOCUMENTS_INTERVENER_4_CASE_SUMMARY;
            default:
                return DocumentCategory.UNCATEGORISED;
        }
    }
}
