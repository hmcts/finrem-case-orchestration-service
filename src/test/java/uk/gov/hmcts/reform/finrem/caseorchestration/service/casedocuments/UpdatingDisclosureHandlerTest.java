package uk.gov.hmcts.reform.finrem.caseorchestration.service.casedocuments;

import org.junit.Test;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.CaseDocumentParty;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.CaseDocumentType;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.document.DocumentCategory;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.is;

public abstract class UpdatingDisclosureHandlerTest extends BaseManageDocumentsHandlerTest<UpdatingDisclosureHandler> {

    @Override
    @Test
    public void assertCorrectCategoryAssignedFromDocumentType() {
        assertThat(
            getDocumentHandler().getDocumentCategoryFromDocumentType(
                CaseDocumentType.UPDATING_DISCLOSURE,
                getCaseDocumentParty()
            ),
            is(getExpectedDocumentCategory())
        );
    }

    protected abstract CaseDocumentParty getCaseDocumentParty();

    protected abstract DocumentCategory getExpectedDocumentCategory();
}
