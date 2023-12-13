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
import uk.gov.hmcts.reform.finrem.caseorchestration.service.casedocuments.BaseManageDocumentsHandlerTest;
import uk.gov.hmcts.reform.finrem.caseorchestration.service.casedocuments.DocumentHandler;

import java.util.List;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.hasSize;
import static org.hamcrest.Matchers.is;

@RunWith(MockitoJUnitRunner.Silent.class)
public class ApplicantChronologiesStatementCollectionServiceTest extends BaseManageDocumentsHandlerTest {

    @InjectMocks
    ApplicantChronologiesStatementHandler collectionService;

    @Override
    public void setUpscreenUploadDocumentList() {
        screenUploadDocumentList.add(createContestedUploadDocumentItem(CaseDocumentType.STATEMENT_OF_ISSUES,
            CaseDocumentParty.APPLICANT, YesOrNo.NO, YesOrNo.NO, null));
        screenUploadDocumentList.add(createContestedUploadDocumentItem(CaseDocumentType.CHRONOLOGY,
            CaseDocumentParty.APPLICANT, YesOrNo.NO, YesOrNo.NO, null));
        screenUploadDocumentList.add(createContestedUploadDocumentItem(CaseDocumentType.FORM_G,
            CaseDocumentParty.APPLICANT, YesOrNo.NO, YesOrNo.NO, null));
    }

    @Override
    public DocumentHandler getDocumentHandler() {
        return collectionService;
    }

    @Override
    public void assertExpectedCollectionType() {
        assertThat(getDocumentCollection(),
            hasSize(3));
        assertThat(caseData.getManageCaseDocumentCollection(),
            hasSize(0));
    }

    @Override
    protected List<UploadCaseDocumentCollection> getDocumentCollection() {
        return caseData.getUploadCaseDocumentWrapper()
            .getDocumentCollectionPerType(CaseDocumentCollectionType.APP_CHRONOLOGIES_STATEMENTS_COLLECTION);
    }

    @Override
    public void assertCorrectCategoryAssignedFromDocumentType() {
        assertThat(
            collectionService.getDocumentCategoryFromDocumentType(CaseDocumentType.STATEMENT_OF_ISSUES),
            is(DocumentCategory.HEARING_DOCUMENTS_APPLICANT_CONCISE_STATEMENT_OF_ISSUES)
        );
        assertThat(
            collectionService.getDocumentCategoryFromDocumentType(CaseDocumentType.CHRONOLOGY),
            is(DocumentCategory.HEARING_DOCUMENTS_APPLICANT_CHRONOLOGY)
        );
        assertThat(
            collectionService.getDocumentCategoryFromDocumentType(CaseDocumentType.FORM_G),
            is(DocumentCategory.APPLICANT_DOCUMENTS_FORM_G)
        );

        assertThat(
            collectionService.getDocumentCategoryFromDocumentType(CaseDocumentType.OTHER),
            is(DocumentCategory.UNCATEGORISED)
        );
    }
}