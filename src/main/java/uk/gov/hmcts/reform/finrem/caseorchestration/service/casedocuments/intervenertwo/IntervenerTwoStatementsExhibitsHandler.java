package uk.gov.hmcts.reform.finrem.caseorchestration.service.casedocuments.intervenertwo;

import org.springframework.stereotype.Component;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.CaseDocumentType;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.document.DocumentCategory;
import uk.gov.hmcts.reform.finrem.caseorchestration.service.FeatureToggleService;
import uk.gov.hmcts.reform.finrem.caseorchestration.service.casedocuments.StatementExhibitsHandler;

import static uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.CaseDocumentParty.INTERVENER_TWO;
import static uk.gov.hmcts.reform.finrem.caseorchestration.model.document.CaseDocumentCollectionType.INTERVENER_TWO_STATEMENTS_EXHIBITS_COLLECTION;

@Component
public class IntervenerTwoStatementsExhibitsHandler extends StatementExhibitsHandler {

    private final FeatureToggleService featureToggleService;

    public IntervenerTwoStatementsExhibitsHandler(FeatureToggleService featureToggleService) {
        super(INTERVENER_TWO_STATEMENTS_EXHIBITS_COLLECTION, INTERVENER_TWO, featureToggleService);
        this.featureToggleService = featureToggleService;
    }

    @Override
    protected DocumentCategory getDocumentCategoryFromDocumentType(CaseDocumentType caseDocumentType) {
        switch (caseDocumentType) {
            case STATEMENT_AFFIDAVIT, WITNESS_STATEMENT_AFFIDAVIT -> {
                return DocumentCategory.INTERVENER_DOCUMENTS;
            }
            default -> {
                return DocumentCategory.UNCATEGORISED;
            }
        }
    }
}
