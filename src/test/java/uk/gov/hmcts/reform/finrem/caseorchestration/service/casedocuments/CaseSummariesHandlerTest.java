package uk.gov.hmcts.reform.finrem.caseorchestration.service.casedocuments;

import org.mockito.InjectMocks;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.CaseDocumentType;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.document.DocumentCategory;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.is;

public abstract class CaseSummariesHandlerTest extends BaseManageDocumentsHandlerTest {

    @InjectMocks
    CaseSummariesHandler caseSummariesHandler;

    @Override
    public void assertCorrectCategoryAssignedFromDocumentType() {
        assertThat(
            caseSummariesHandler.getDocumentCategoryFromDocumentType(CaseDocumentType.POSITION_STATEMENT),
            is(DocumentCategory.HEARING_DOCUMENTS)
        );

        assertThat(
            caseSummariesHandler.getDocumentCategoryFromDocumentType(CaseDocumentType.SKELETON_ARGUMENT),
            is(DocumentCategory.HEARING_DOCUMENTS)
        );

        assertThat(
            caseSummariesHandler.getDocumentCategoryFromDocumentType(CaseDocumentType.CASE_SUMMARY),
            is(DocumentCategory.HEARING_DOCUMENTS)
        );
    }
}
