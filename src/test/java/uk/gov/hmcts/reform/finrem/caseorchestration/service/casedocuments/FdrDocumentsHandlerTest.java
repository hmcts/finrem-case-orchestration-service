package uk.gov.hmcts.reform.finrem.caseorchestration.service.casedocuments;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.mockito.junit.MockitoJUnitRunner;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.CaseDocumentType;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.UploadCaseDocumentCollection;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.YesOrNo;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.document.CaseDocumentCollectionType;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.document.DocumentCategory;

import java.util.ArrayList;
import java.util.List;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.hasSize;
import static org.hamcrest.Matchers.is;

@RunWith(MockitoJUnitRunner.Silent.class)
public class FdrDocumentsHandlerTest extends BaseManageDocumentsHandlerTest {

    @InjectMocks
    FdrDocumentsHandler handler;


    private List<UploadCaseDocumentCollection> uploadDocumentList = new ArrayList<>();

    @Override
    public void setUpscreenUploadDocumentList() {
        screenUploadDocumentList.add(createContestedUploadDocumentItem(CaseDocumentType.OTHER,
            null, YesOrNo.NO, YesOrNo.YES, "Other Example"));

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
            .getDocumentCollectionPerType(CaseDocumentCollectionType.CONTESTED_FDR_CASE_DOCUMENT_COLLECTION);
    }

    @Override
    public void assertCorrectCategoryAssignedFromDocumentType() {
        assertThat(
            handler.getDocumentCategoryFromDocumentType(CaseDocumentType.OTHER),
            is(DocumentCategory.FDR_DOCUMENTS_AND_FDR_BUNDLE)
        );
    }

    @Test
    public void shouldAddWithoutPrejudiceOffersToCollection() {
        uploadDocumentList.add(createContestedUploadDocumentItem(CaseDocumentType.WITHOUT_PREJUDICE_OFFERS,
            null, null, null, null));


        List<UploadCaseDocumentCollection> alteredCollectionForType = handler.getAlteredCollectionForType(uploadDocumentList);

        assertThat(alteredCollectionForType, hasSize(1));
        assertThat(alteredCollectionForType.get(0).getUploadCaseDocument().getCaseDocumentType(), is(CaseDocumentType.WITHOUT_PREJUDICE_OFFERS));
        assertThat(alteredCollectionForType.get(0).getUploadCaseDocument().getCaseDocumentFdr(), is(YesOrNo.YES));
        assertThat(alteredCollectionForType.get(0).getUploadCaseDocument().getCaseDocumentConfidentiality(), is(YesOrNo.NO));
    }
}
