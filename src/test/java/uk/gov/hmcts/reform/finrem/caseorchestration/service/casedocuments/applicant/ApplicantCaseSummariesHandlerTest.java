package uk.gov.hmcts.reform.finrem.caseorchestration.service.casedocuments.applicant;

import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.mockito.junit.MockitoJUnitRunner;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.CaseDocumentParty;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.CaseDocumentType;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.UploadCaseDocumentCollection;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.YesOrNo;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.document.CaseDocumentCollectionType;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.document.DocumentCategory;
import uk.gov.hmcts.reform.finrem.caseorchestration.service.casedocuments.CaseSummariesHandlerTest;

import java.util.List;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.hasSize;
import static org.hamcrest.Matchers.is;

@RunWith(MockitoJUnitRunner.Silent.class)
public class ApplicantCaseSummariesHandlerTest extends CaseSummariesHandlerTest<ApplicantCaseSummariesHandler> {

    @InjectMocks
    ApplicantCaseSummariesHandler collectionService;

    @Override
    public void setUpscreenUploadDocumentList() {
        screenUploadDocumentList.add(createContestedUploadDocumentItem(CaseDocumentType.STATEMENT_SKELETON_ARGUMENT,
            CaseDocumentParty.APPLICANT, YesOrNo.NO, YesOrNo.NO, null));
        screenUploadDocumentList.add(createContestedUploadDocumentItem(CaseDocumentType.CASE_SUMMARY,
            CaseDocumentParty.APPLICANT, YesOrNo.NO, YesOrNo.NO, null));
    }

    @Override
    public ApplicantCaseSummariesHandler getDocumentHandler() {
        return collectionService;
    }

    @Override
    public void assertExpectedCollectionType() {
        assertThat(getDocumentCollection(),
            hasSize(2));
        assertThat(caseData.getManageCaseDocumentCollection(),
            hasSize(0));
    }

    @Override
    protected List<UploadCaseDocumentCollection> getDocumentCollection() {
        return caseData.getUploadCaseDocumentWrapper()
            .getDocumentCollectionPerType(CaseDocumentCollectionType.APP_CASE_SUMMARIES_COLLECTION);
    }

    @Override
    public void assertCorrectCategoryAssignedFromDocumentType() {
        assertThat(
            collectionService.getDocumentCategoryFromDocumentType(CaseDocumentType.STATEMENT_SKELETON_ARGUMENT),
            is(DocumentCategory.HEARING_DOCUMENTS_APPLICANT_POSITION_STATEMENT)
        );

        assertThat(
            collectionService.getDocumentCategoryFromDocumentType(CaseDocumentType.CASE_SUMMARY),
            is(DocumentCategory.HEARING_DOCUMENTS_APPLICANT_CASE_SUMMARY)
        );
    }
}
