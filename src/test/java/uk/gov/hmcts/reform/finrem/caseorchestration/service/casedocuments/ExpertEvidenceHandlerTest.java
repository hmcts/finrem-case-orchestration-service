package uk.gov.hmcts.reform.finrem.caseorchestration.service.casedocuments;

import org.junit.Test;
import org.mockito.InjectMocks;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.CaseDocumentType;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.document.DocumentCategory;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.is;

public abstract class ExpertEvidenceHandlerTest extends BaseManageDocumentsHandlerTest {

    @InjectMocks
    ExpertEvidenceHandler expertEvidenceHandler;

    @Override
    @Test
    public void assertCorrectCategoryAssignedFromDocumentType() {
        assertThat(
            expertEvidenceHandler.getDocumentCategoryFromDocumentType(CaseDocumentType.EXPERT_EVIDENCE),
            is(DocumentCategory.REPORTS_EXPERT_REPORTS)
        );

        assertThat(
            expertEvidenceHandler.getDocumentCategoryFromDocumentType(CaseDocumentType.VALUATION_REPORT),
            is(DocumentCategory.REPORTS)
        );

    }
}
