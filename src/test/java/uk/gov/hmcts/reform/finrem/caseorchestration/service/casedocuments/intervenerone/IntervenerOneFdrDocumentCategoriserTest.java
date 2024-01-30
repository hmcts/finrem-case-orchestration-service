package uk.gov.hmcts.reform.finrem.caseorchestration.service.casedocuments.intervenerone;


import org.junit.Test;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.CaseDocumentType;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.document.DocumentCategory;

import static org.junit.jupiter.api.Assertions.assertEquals;

public class IntervenerOneFdrDocumentCategoriserTest {

    private final IntervenerOneFdrDocumentCategoriser categoriser = new IntervenerOneFdrDocumentCategoriser();



    @Test
    public void testGetDocumentCategory() {
        assertEquals(DocumentCategory.FDR_DOCUMENTS_AND_FDR_BUNDLE_INTERVENER_1_POSITION_STATEMENTS,
            categoriser.getDocumentCategory(CaseDocumentType.POSITION_STATEMENT));

        assertEquals(DocumentCategory.FDR_JOINT_DOCUMENTS,
            categoriser.getDocumentCategory(CaseDocumentType.ES1));

        assertEquals(DocumentCategory.FDR_JOINT_DOCUMENTS,
            categoriser.getDocumentCategory(CaseDocumentType.ES2));

        assertEquals(DocumentCategory.FDR_DOCUMENTS_AND_FDR_BUNDLE_INTERVENER_1_WITHOUT_PREJUDICE_OFFERS,
            categoriser.getDocumentCategory(CaseDocumentType.WITHOUT_PREJUDICE_OFFERS));

        assertEquals(DocumentCategory.FDR_JOINT_DOCUMENTS,
            categoriser.getDocumentCategory(CaseDocumentType.CHRONOLOGY));

        assertEquals(DocumentCategory.FDR_DOCUMENTS_AND_FDR_BUNDLE_INTERVENER_1_QUESTIONNAIRES,
            categoriser.getDocumentCategory(CaseDocumentType.QUESTIONNAIRE));

        assertEquals(DocumentCategory.FDR_REPORTS,
            categoriser.getDocumentCategory(CaseDocumentType.FAMILY_HOME_VALUATION));

        assertEquals(DocumentCategory.FDR_DOCUMENTS_AND_FDR_BUNDLE_INTERVENER_1_PRE_HEARING_DRAFT_ORDER,
            categoriser.getDocumentCategory(CaseDocumentType.PRE_HEARING_DRAFT_ORDER));

        assertEquals(DocumentCategory.FDR_DOCUMENTS_AND_FDR_BUNDLE_INTERVENER_1_SKELETON_ARGUMENT,
            categoriser.getDocumentCategory(CaseDocumentType.SKELETON_ARGUMENT));

        assertEquals(DocumentCategory.FDR_DOCUMENTS_AND_FDR_BUNDLE_INTERVENER_1,
            categoriser.getDocumentCategory(CaseDocumentType.OTHER));

    }
}
