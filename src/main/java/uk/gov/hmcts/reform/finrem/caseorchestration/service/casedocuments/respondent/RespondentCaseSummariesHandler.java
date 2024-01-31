package uk.gov.hmcts.reform.finrem.caseorchestration.service.casedocuments.respondent;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.CaseDocumentParty;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.CaseDocumentType;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.document.CaseDocumentCollectionType;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.document.DocumentCategory;
import uk.gov.hmcts.reform.finrem.caseorchestration.service.FeatureToggleService;
import uk.gov.hmcts.reform.finrem.caseorchestration.service.casedocuments.CaseSummariesHandler;

@Component
public class RespondentCaseSummariesHandler extends CaseSummariesHandler {

    @Autowired
    public RespondentCaseSummariesHandler(FeatureToggleService featureToggleService) {
        super(CaseDocumentCollectionType.RESP_CASE_SUMMARIES_COLLECTION,
            CaseDocumentParty.RESPONDENT, featureToggleService);
    }

    @Override
    protected DocumentCategory getDocumentCategoryFromDocumentType(CaseDocumentType caseDocumentType) {
        switch (caseDocumentType) {
            case STATEMENT_SKELETON_ARGUMENT:
                return DocumentCategory.HEARING_DOCUMENTS_RESPONDENT_POSITION_STATEMENT;
            case CASE_SUMMARY:
                return DocumentCategory.HEARING_DOCUMENTS_RESPONDENT_CASE_SUMMARY;
            default:
                return DocumentCategory.UNCATEGORISED;
        }
    }
}
