package uk.gov.hmcts.reform.finrem.caseorchestration.service.casedocuments.respondent;

import org.junit.jupiter.api.Assertions;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.CaseDocumentType;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.document.DocumentCategory;

import static org.junit.Assert.assertEquals;

public class RespondentFdrDocumentCategoriserTest {
    RespondentFdrDocumentCategoriser categoriser = new RespondentFdrDocumentCategoriser();

    @org.junit.Test
    public void getDocumentCategory() {
        assertEquals(DocumentCategory.FDR_DOCUMENTS_AND_FDR_BUNDLE_RESPONDENT_POSITION_STATEMENTS,
            categoriser.getDocumentCategory(CaseDocumentType.POSITION_STATEMENT_SKELETON_ARGUMENT));

        assertEquals(DocumentCategory.FDR_JOINT_DOCUMENTS_ES1,
            categoriser.getDocumentCategory(CaseDocumentType.ES1));

        assertEquals(DocumentCategory.FDR_JOINT_DOCUMENTS_ES2,
            categoriser.getDocumentCategory(CaseDocumentType.ES2));

        assertEquals(DocumentCategory.FDR_DOCUMENTS_AND_FDR_BUNDLE_RESPONDENT_WITHOUT_PREJUDICE_OFFERS,
            categoriser.getDocumentCategory(CaseDocumentType.WITHOUT_PREJUDICE_OFFERS));

        assertEquals(DocumentCategory.FDR_JOINT_DOCUMENTS_CHRONOLOGY,
            categoriser.getDocumentCategory(CaseDocumentType.CHRONOLOGY));

        assertEquals(DocumentCategory.FDR_DOCUMENTS_AND_FDR_BUNDLE_RESPONDENT_OTHER,
            categoriser.getDocumentCategory(CaseDocumentType.OTHER));

        assertEquals(DocumentCategory.FDR_DOCUMENTS_AND_FDR_BUNDLE_RESPONDENT_DRAFT_ORDER,
            categoriser.getDocumentCategory(CaseDocumentType.PRE_HEARING_DRAFT_ORDER));

        Assertions.assertEquals(DocumentCategory.FDR_DOCUMENTS_AND_FDR_BUNDLE_RESPONDENT,
            categoriser.getDocumentCategory(CaseDocumentType.POINTS_OF_CLAIM_OR_DEFENCE));
    }

}
