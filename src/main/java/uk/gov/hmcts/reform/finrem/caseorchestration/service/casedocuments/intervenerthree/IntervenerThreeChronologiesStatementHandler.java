package uk.gov.hmcts.reform.finrem.caseorchestration.service.casedocuments.intervenerthree;

import org.springframework.stereotype.Component;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.CaseDocumentParty;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.CaseDocumentType;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.document.DocumentCategory;
import uk.gov.hmcts.reform.finrem.caseorchestration.service.FeatureToggleService;
import uk.gov.hmcts.reform.finrem.caseorchestration.service.casedocuments.ChronologiesStatementsHandler;

import static uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.CaseDocumentParty.INTERVENER_THREE;
import static uk.gov.hmcts.reform.finrem.caseorchestration.model.document.CaseDocumentCollectionType.INTERVENER_THREE_CHRONOLOGIES_STATEMENTS_COLLECTION;

@Component
public class IntervenerThreeChronologiesStatementHandler extends ChronologiesStatementsHandler {

    public IntervenerThreeChronologiesStatementHandler(FeatureToggleService featureToggleService) {
        super(INTERVENER_THREE_CHRONOLOGIES_STATEMENTS_COLLECTION, INTERVENER_THREE, featureToggleService);
    }

    @Override
    protected DocumentCategory getDocumentCategoryFromDocumentType(CaseDocumentType caseDocumentType, CaseDocumentParty caseDocumentParty) {
        switch (caseDocumentType) {
            case CHRONOLOGY -> {
                return DocumentCategory.HEARING_DOCUMENTS_INTERVENER_3_CHRONOLOGY;
            }
            case STATEMENT_OF_ISSUES -> {
                return DocumentCategory.HEARING_DOCUMENTS_INTERVENER_3_CONCISE_STATEMENT_OF_ISSUES;
            }
            case FORM_G -> {
                return DocumentCategory.INTERVENER_DOCUMENTS_INTERVENER_3_FORM_G;
            }
            default -> {
                return DocumentCategory.UNCATEGORISED;
            }
        }
    }
}
