package uk.gov.hmcts.reform.finrem.caseorchestration.service.casedocuments.applicant;

import org.springframework.stereotype.Service;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.CaseDocumentParty;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.CaseDocumentType;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.document.CaseDocumentCollectionType;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.document.DocumentCategory;
import uk.gov.hmcts.reform.finrem.caseorchestration.service.FeatureToggleService;
import uk.gov.hmcts.reform.finrem.caseorchestration.service.casedocuments.StatementExhibitsHandler;

@Service
public class ApplicantStatementsExhibitsHandler extends StatementExhibitsHandler {

    private final FeatureToggleService featureToggleService;

    public ApplicantStatementsExhibitsHandler(FeatureToggleService featureToggleService) {

        super(CaseDocumentCollectionType.APP_STATEMENTS_EXHIBITS_COLLECTION,
            CaseDocumentParty.APPLICANT, featureToggleService);
        this.featureToggleService = featureToggleService;
    }

    @Override
    protected DocumentCategory getDocumentCategoryFromDocumentType(CaseDocumentType caseDocumentType) {
        switch (caseDocumentType) {
            case STATEMENT_AFFIDAVIT -> {
                return DocumentCategory.APPLICANT_DOCUMENTS;
            }
            case WITNESS_STATEMENT_AFFIDAVIT -> {
                return DocumentCategory.APPLICANT_WITNESS_STATEMENT;
            }
            default -> {
                return DocumentCategory.UNCATEGORISED;
            }
        }
    }
}
