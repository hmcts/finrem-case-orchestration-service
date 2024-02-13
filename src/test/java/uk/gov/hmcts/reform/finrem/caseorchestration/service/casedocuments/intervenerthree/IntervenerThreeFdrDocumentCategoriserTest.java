package uk.gov.hmcts.reform.finrem.caseorchestration.service.casedocuments.intervenerthree;


import org.junit.Test;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.CaseDocumentType;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.document.DocumentCategory;

import static org.junit.jupiter.api.Assertions.assertEquals;

public class IntervenerThreeFdrDocumentCategoriserTest {

    private final IntervenerThreeFdrDocumentCategoriser categoriser = new IntervenerThreeFdrDocumentCategoriser();

    @Test
    public void testGetDocumentCategory() {
        assertEquals(DocumentCategory.FDR_DOCUMENTS_AND_FDR_BUNDLE_INTERVENER_3_POSITION_STATEMENTS,
            categoriser.getDocumentCategory(CaseDocumentType.POSITION_STATEMENT_SKELETON_ARGUMENT));

        assertEquals(DocumentCategory.FDR_JOINT_DOCUMENTS_ES1,
            categoriser.getDocumentCategory(CaseDocumentType.ES1));

        assertEquals(DocumentCategory.FDR_JOINT_DOCUMENTS_ES2,
            categoriser.getDocumentCategory(CaseDocumentType.ES2));

        assertEquals(DocumentCategory.FDR_DOCUMENTS_AND_FDR_BUNDLE_INTERVENER_3_WITHOUT_PREJUDICE_OFFERS,
            categoriser.getDocumentCategory(CaseDocumentType.WITHOUT_PREJUDICE_OFFERS));

        assertEquals(DocumentCategory.FDR_JOINT_DOCUMENTS_CHRONOLOGY,
            categoriser.getDocumentCategory(CaseDocumentType.CHRONOLOGY));

        assertEquals(DocumentCategory.FDR_DOCUMENTS_AND_FDR_BUNDLE_INTERVENER_3_QUESTIONNAIRES,
            categoriser.getDocumentCategory(CaseDocumentType.QUESTIONNAIRE));

        assertEquals(DocumentCategory.FDR_REPORTS,
            categoriser.getDocumentCategory(CaseDocumentType.FAMILY_HOME_VALUATION));

        assertEquals(DocumentCategory.FDR_DOCUMENTS_AND_FDR_BUNDLE_INTERVENER_3_PRE_HEARING_DRAFT_ORDER,
            categoriser.getDocumentCategory(CaseDocumentType.PRE_HEARING_DRAFT_ORDER));


        assertEquals(DocumentCategory.FDR_DOCUMENTS_AND_FDR_BUNDLE_INTERVENER_3,
            categoriser.getDocumentCategory(CaseDocumentType.OTHER));

    }
}
