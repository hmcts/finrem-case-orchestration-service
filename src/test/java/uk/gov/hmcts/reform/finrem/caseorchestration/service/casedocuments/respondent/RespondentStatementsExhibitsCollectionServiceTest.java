package uk.gov.hmcts.reform.finrem.caseorchestration.service.casedocuments.respondent;

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
public class RespondentStatementsExhibitsCollectionServiceTest extends BaseManageDocumentsHandlerTest {

    @InjectMocks
    RespondentStatementsExhibitsHandler collectionService;


    @Override
    public void setUpscreenUploadDocumentList() {
        screenUploadDocumentList.add(createContestedUploadDocumentItem(CaseDocumentType.STATEMENT_AFFIDAVIT,
            CaseDocumentParty.RESPONDENT, YesOrNo.NO, YesOrNo.NO, null));
        screenUploadDocumentList.add(createContestedUploadDocumentItem(CaseDocumentType.WITNESS_STATEMENT_AFFIDAVIT,
            CaseDocumentParty.RESPONDENT, YesOrNo.NO, YesOrNo.NO, null));
    }

    @Override
    public DocumentHandler getDocumentHandler() {
        return collectionService;
    }

    @Override
    public void assertExpectedCollectionType() {
        // TODO Check with Ruban
        assertThat(getDocumentCollection(),
            hasSize(2));
        assertThat(caseData.getUploadCaseDocumentWrapper()
                .getDocumentCollectionPerType(CaseDocumentCollectionType.RESP_STATEMENTS_EXHIBITS_COLLECTION),
            hasSize(2));
        assertThat(caseData.getManageCaseDocumentCollection(),
            hasSize(0));
    }

    @Override
    protected List<UploadCaseDocumentCollection> getDocumentCollection() {
        return caseDetails.getData().getUploadCaseDocumentWrapper().getRespStatementsExhibitsCollection();
    }

    @Override
    public void assertCorrectCategoryAssignedFromDocumentType() {
        assertThat(
            collectionService.getDocumentCategoryFromDocumentType(CaseDocumentType.STATEMENT_AFFIDAVIT),
            is(DocumentCategory.RESPONDENT_DOCUMENTS_S25_STATEMENT)
        );

        assertThat(
            collectionService.getDocumentCategoryFromDocumentType(CaseDocumentType.WITNESS_STATEMENT_AFFIDAVIT),
            is(DocumentCategory.RESPONDENT_DOCUMENTS_WITNESS_STATEMENTS)
        );

        assertThat(
            collectionService.getDocumentCategoryFromDocumentType(CaseDocumentType.FORM_G),
            is(DocumentCategory.UNCATEGORISED)
        );
    }
}