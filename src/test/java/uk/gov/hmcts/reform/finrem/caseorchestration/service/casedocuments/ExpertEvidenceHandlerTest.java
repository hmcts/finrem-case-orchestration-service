package uk.gov.hmcts.reform.finrem.caseorchestration.service.casedocuments;

import org.junit.Test;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.CaseDocumentParty;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.CaseDocumentType;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.document.DocumentCategory;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.is;

public abstract class ExpertEvidenceHandlerTest extends BaseManageDocumentsHandlerTest<ExpertEvidenceHandler> {


    @Override
    @Test
    public void assertCorrectCategoryAssignedFromDocumentType() {
        assertThat(
            getDocumentHandler().getDocumentCategoryFromDocumentType(CaseDocumentType.EXPERT_EVIDENCE, CaseDocumentParty.RESPONDENT),
            is(DocumentCategory.REPORTS)
        );

        assertThat(
            getDocumentHandler().getDocumentCategoryFromDocumentType(CaseDocumentType.VALUATION_REPORT, CaseDocumentParty.RESPONDENT),
            is(getValuationReportCategory())
        );
    }

    protected abstract DocumentCategory getValuationReportCategory();

}
