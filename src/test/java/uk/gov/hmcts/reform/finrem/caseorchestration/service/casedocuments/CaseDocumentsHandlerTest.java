package uk.gov.hmcts.reform.finrem.caseorchestration.service.casedocuments;

import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.mockito.junit.MockitoJUnitRunner;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.CaseDocumentParty;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.CaseDocumentType;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.UploadCaseDocumentCollection;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.YesOrNo;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.document.CaseDocumentCollectionType;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.document.DocumentCategory;

import java.util.List;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.hasSize;
import static org.hamcrest.Matchers.is;

@RunWith(MockitoJUnitRunner.Silent.class)
public class CaseDocumentsHandlerTest extends BaseManageDocumentsHandlerTest {

    @InjectMocks
    CaseDocumentsHandler caseDocumentsHandler;


    @Override
    public void setUpscreenUploadDocumentList() {
        screenUploadDocumentList.add(createContestedUploadDocumentItem(CaseDocumentType.STATEMENT_SKELETON_ARGUMENT,
            CaseDocumentParty.CASE, YesOrNo.NO, YesOrNo.NO, null));
        screenUploadDocumentList.add(createContestedUploadDocumentItem(CaseDocumentType.CASE_SUMMARY,
            CaseDocumentParty.CASE, YesOrNo.NO, YesOrNo.NO, null));
    }

    @Override
    public DocumentHandler getDocumentHandler() {
        return caseDocumentsHandler;
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
            .getDocumentCollectionPerType(CaseDocumentCollectionType.CONTESTED_UPLOADED_DOCUMENTS);
    }

    @Override
    public void assertCorrectCategoryAssignedFromDocumentType() {
        assertThat(
            caseDocumentsHandler.getDocumentCategoryFromDocumentType(CaseDocumentType.STATEMENT_SKELETON_ARGUMENT),
            is(DocumentCategory.CASE_DOCUMENTS)
        );

        assertThat(
            caseDocumentsHandler.getDocumentCategoryFromDocumentType(CaseDocumentType.ATTENDANCE_SHEETS),
            is(DocumentCategory.ADMINISTRATIVE_DOCUMENTS_ATTENDANCE_SHEETS)
        );

        assertThat(
            caseDocumentsHandler.getDocumentCategoryFromDocumentType(CaseDocumentType.JUDICIAL_NOTES),
            is(DocumentCategory.ADMINISTRATIVE_DOCUMENTS_JUDICIAL_NOTES)
        );

        assertThat(
            caseDocumentsHandler.getDocumentCategoryFromDocumentType(CaseDocumentType.WITNESS_SUMMONS),
            is(DocumentCategory.HEARING_DOCUMENTS_WITNESS_SUMMONS)
        );

        assertThat(
            caseDocumentsHandler.getDocumentCategoryFromDocumentType(CaseDocumentType.JUDGMENT),
            is(DocumentCategory.JUDGMENT_OR_TRANSCRIPT)
        );

        assertThat(
            caseDocumentsHandler.getDocumentCategoryFromDocumentType(CaseDocumentType.TRANSCRIPT),
            is(DocumentCategory.JUDGMENT_OR_TRANSCRIPT)
        );

    }
}
