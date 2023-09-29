package uk.gov.hmcts.reform.finrem.caseorchestration.service.casedocuments.applicant;

import org.springframework.stereotype.Service;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.CaseDocumentParty;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.CaseDocumentType;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.document.CaseDocumentCollectionType;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.document.DocumentCategory;
import uk.gov.hmcts.reform.finrem.caseorchestration.service.casedocuments.ChronologiesStatementsHandler;

@Service
public class ApplicantChronologiesStatementHandler extends ChronologiesStatementsHandler {

    public ApplicantChronologiesStatementHandler() {
        super(CaseDocumentCollectionType.APP_CHRONOLOGIES_STATEMENTS_COLLECTION,
                CaseDocumentParty.APPLICANT);
    }

    @Override
    protected DocumentCategory getDocumentCategoryFromDocumentType(CaseDocumentType caseDocumentType) {
        switch (caseDocumentType) {
            case CHRONOLOGY -> {
                return DocumentCategory.HEARING_DOCUMENTS;
            }
            case STATEMENT_OF_ISSUES, FORM_G -> {
                return DocumentCategory.APPLICANT_DOCUMENTS;
            }
            default -> {
                return DocumentCategory.UNCATEGORISED;
            }
        }
    }
}
