package uk.gov.hmcts.reform.finrem.caseorchestration.service.casedocuments;

import uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.CaseDocumentType;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.document.DocumentCategory;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.is;

public abstract class CorrespondenceHandlerTest extends BaseManageDocumentsHandlerTest {

    CorrespondenceHandler correspondenceHandler;

    @Override
    public void assertCorrectCategoryAssignedFromDocumentType() {
        assertThat(
            correspondenceHandler.getDocumentCategoryFromDocumentType(CaseDocumentType.OFFERS),
            is(DocumentCategory.CORRESPONDENCE)
        );

        assertThat(
            correspondenceHandler.getDocumentCategoryFromDocumentType(CaseDocumentType.LETTER_FROM_APPLICANT),
            is(DocumentCategory.CORRESPONDENCE)
        );
    }
}
