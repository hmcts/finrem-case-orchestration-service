package uk.gov.hmcts.reform.finrem.caseorchestration.service.casedocuments;

import org.mockito.InjectMocks;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.CaseDocumentType;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.document.DocumentCategory;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.is;

public abstract class HearingBundleHandlerTest extends BaseManageDocumentsHandlerTest {

    @InjectMocks
    HearingBundleHandler hearingBundleHandler;

    @Override
    public void assertCorrectCategoryAssignedFromDocumentType() {
        assertThat(
            hearingBundleHandler.getDocumentCategoryFromDocumentType(CaseDocumentType.TRIAL_BUNDLE),
            is(DocumentCategory.HEARING_BUNDLE)
        );
    }
}
