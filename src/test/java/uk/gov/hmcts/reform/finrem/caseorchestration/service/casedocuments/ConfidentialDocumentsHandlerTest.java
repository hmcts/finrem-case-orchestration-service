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
public class ConfidentialDocumentsHandlerTest extends BaseManageDocumentsHandlerTest {

    @InjectMocks
    ConfidentialDocumentsHandler handler;


    @Override
    public void setUpscreenUploadDocumentList() {
        screenUploadDocumentList.add(createContestedUploadDocumentItem(CaseDocumentType.OTHER,
            CaseDocumentParty.RESPONDENT, YesOrNo.YES, YesOrNo.YES, "Other Example"));
    }

    @Override
    public DocumentHandler getDocumentHandler() {
        return handler;
    }

    @Override
    public void assertExpectedCollectionType() {
        assertThat(getDocumentCollection(),
            hasSize(1));
        assertThat(caseData.getManageCaseDocumentCollection(),
            hasSize(0));
    }

    @Override
    protected List<UploadCaseDocumentCollection> getDocumentCollection() {
        return caseData.getUploadCaseDocumentWrapper()
            .getDocumentCollectionPerType(CaseDocumentCollectionType.CONFIDENTIAL_DOCS_COLLECTION);
    }

    @Override
    public void assertCorrectCategoryAssignedFromDocumentType() {
        assertThat(
            handler.getDocumentCategoryFromDocumentType(CaseDocumentType.OTHER, CaseDocumentParty.RESPONDENT),
            is(DocumentCategory.CONFIDENTIAL_DOCUMENTS_RESPONDENT)
        );
        assertThat(
            handler.getDocumentCategoryFromDocumentType(CaseDocumentType.OTHER, CaseDocumentParty.APPLICANT),
            is(DocumentCategory.CONFIDENTIAL_DOCUMENTS_APPLICANT)
        );
        assertThat(
            handler.getDocumentCategoryFromDocumentType(CaseDocumentType.OTHER, CaseDocumentParty.INTERVENER_ONE),
            is(DocumentCategory.CONFIDENTIAL_DOCUMENTS_INTERVENER_1)
        );
        assertThat(
            handler.getDocumentCategoryFromDocumentType(CaseDocumentType.OTHER, CaseDocumentParty.INTERVENER_TWO),
            is(DocumentCategory.CONFIDENTIAL_DOCUMENTS_INTERVENER_2)
        );
        assertThat(
            handler.getDocumentCategoryFromDocumentType(CaseDocumentType.OTHER, CaseDocumentParty.INTERVENER_THREE),
            is(DocumentCategory.CONFIDENTIAL_DOCUMENTS_INTERVENER_3)
        );
        assertThat(
            handler.getDocumentCategoryFromDocumentType(CaseDocumentType.OTHER, CaseDocumentParty.INTERVENER_FOUR),
            is(DocumentCategory.CONFIDENTIAL_DOCUMENTS_INTERVENER_4)
        );
    }
}
