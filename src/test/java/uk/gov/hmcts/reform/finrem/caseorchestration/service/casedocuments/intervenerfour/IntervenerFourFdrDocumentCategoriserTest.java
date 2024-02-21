package uk.gov.hmcts.reform.finrem.caseorchestration.service.casedocuments.intervenerfour;


import org.junit.Test;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.CaseDocumentType;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.document.DocumentCategory;

import static org.junit.jupiter.api.Assertions.assertEquals;

public class IntervenerFourFdrDocumentCategoriserTest {

    private final IntervenerFourFdrDocumentCategoriser categoriser = new IntervenerFourFdrDocumentCategoriser();

    @Test
    public void testGetDocumentCategory() {
        assertEquals(DocumentCategory.FDR_DOCUMENTS_AND_FDR_BUNDLE_INTERVENER_4_POSITION_STATEMENTS,
            categoriser.getDocumentCategory(CaseDocumentType.POSITION_STATEMENT_SKELETON_ARGUMENT));

        assertEquals(DocumentCategory.FDR_JOINT_DOCUMENTS_ES1,
            categoriser.getDocumentCategory(CaseDocumentType.ES1));

        assertEquals(DocumentCategory.FDR_JOINT_DOCUMENTS_ES2,
            categoriser.getDocumentCategory(CaseDocumentType.ES2));

        assertEquals(DocumentCategory.FDR_DOCUMENTS_AND_FDR_BUNDLE_INTERVENER_4_WITHOUT_PREJUDICE_OFFERS,
            categoriser.getDocumentCategory(CaseDocumentType.WITHOUT_PREJUDICE_OFFERS));

        assertEquals(DocumentCategory.FDR_JOINT_DOCUMENTS_CHRONOLOGY,
            categoriser.getDocumentCategory(CaseDocumentType.CHRONOLOGY));

        assertEquals(DocumentCategory.FDR_DOCUMENTS_AND_FDR_BUNDLE_INTERVENER_4_QUESTIONNAIRES,
            categoriser.getDocumentCategory(CaseDocumentType.QUESTIONNAIRE));


        assertEquals(DocumentCategory.FDR_DOCUMENTS_AND_FDR_BUNDLE_INTERVENER_4_PRE_HEARING_DRAFT_ORDER,
            categoriser.getDocumentCategory(CaseDocumentType.PRE_HEARING_DRAFT_ORDER));

        assertEquals(DocumentCategory.FDR_DOCUMENTS_AND_FDR_BUNDLE_INTERVENER_4,
            categoriser.getDocumentCategory(CaseDocumentType.OTHER));

    }
}
