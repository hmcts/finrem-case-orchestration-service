package uk.gov.hmcts.reform.finrem.caseorchestration.service.casedocuments.intervenertwo;

import org.springframework.stereotype.Component;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.CaseDocumentParty;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.CaseDocumentType;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.document.DocumentCategory;
import uk.gov.hmcts.reform.finrem.caseorchestration.service.FeatureToggleService;
import uk.gov.hmcts.reform.finrem.caseorchestration.service.casedocuments.ChronologiesStatementsHandler;

import static uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.CaseDocumentParty.INTERVENER_TWO;
import static uk.gov.hmcts.reform.finrem.caseorchestration.model.document.CaseDocumentCollectionType.INTERVENER_TWO_CHRONOLOGIES_STATEMENTS_COLLECTION;

@Component
public class IntervenerTwoChronologiesStatementHandler extends ChronologiesStatementsHandler {

    public IntervenerTwoChronologiesStatementHandler(FeatureToggleService featureToggleService) {
        super(INTERVENER_TWO_CHRONOLOGIES_STATEMENTS_COLLECTION, INTERVENER_TWO, featureToggleService);
    }

    @Override
    protected DocumentCategory getDocumentCategoryFromDocumentType(CaseDocumentType caseDocumentType, CaseDocumentParty caseDocumentParty) {
        switch (caseDocumentType) {
            case CHRONOLOGY -> {
                return DocumentCategory.HEARING_DOCUMENTS_INTERVENER_2_CHRONOLOGY;
            }
            case STATEMENT_OF_ISSUES -> {
                return DocumentCategory.HEARING_DOCUMENTS_INTERVENER_2_CONCISE_STATEMENT_OF_ISSUES;
            }
            case FORM_G -> {
                return DocumentCategory.INTERVENER_DOCUMENTS_INTERVENER_2_FORM_G;
            }
            default -> {
                return DocumentCategory.UNCATEGORISED;
            }
        }
    }
}
