package uk.gov.hmcts.reform.finrem.caseorchestration.service.casedocuments;

import org.mockito.InjectMocks;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.CaseDocumentType;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.document.DocumentCategory;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.is;

public abstract class FormsHHandlerTest extends BaseManageDocumentsHandlerTest {

    @InjectMocks
    FormsHHandler formsHHandler;

    @Override
    public void assertCorrectCategoryAssignedFromDocumentType() {
        assertThat(
            formsHHandler.getDocumentCategoryFromDocumentType(CaseDocumentType.FORM_H),
            is(DocumentCategory.HEARING_DOCUMENTS)
        );
    }
}
