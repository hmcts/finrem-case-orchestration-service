package uk.gov.hmcts.reform.finrem.caseorchestration.service.casedocuments;

import org.junit.Before;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.junit.MockitoJUnitRunner;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.CaseDocumentParty;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.CaseDocumentType;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.UploadCaseDocumentCollection;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.YesOrNo;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.document.CaseDocumentCollectionType;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.document.DocumentCategory;
import uk.gov.hmcts.reform.finrem.caseorchestration.service.casedocuments.applicant.ApplicantFdrDocumentCategoriser;
import uk.gov.hmcts.reform.finrem.caseorchestration.service.casedocuments.respondent.RespondentFdrDocumentCategoriser;

import java.util.ArrayList;
import java.util.List;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.hasSize;
import static org.hamcrest.Matchers.is;
import static org.mockito.Mockito.when;

@RunWith(MockitoJUnitRunner.Silent.class)
public class FdrDocumentsHandlerTest extends BaseManageDocumentsHandlerTest {

    @Mock
    private ApplicantFdrDocumentCategoriser applicantFdrDocumentCategoriser;
    @Mock
    private RespondentFdrDocumentCategoriser respondentFdrDocumentCategoriser;

    @InjectMocks
    FdrDocumentsHandler handler;


    private List<UploadCaseDocumentCollection> uploadDocumentList = new ArrayList<>();

    @Before
    public void setUpTest() {
        when(applicantFdrDocumentCategoriser.getDocumentCategory(CaseDocumentType.WITHOUT_PREJUDICE_OFFERS))
            .thenReturn(DocumentCategory.FDR_DOCUMENTS_AND_FDR_BUNDLE_APPLICANT_WITHOUT_PREJUDICE_OFFERS);
        when(respondentFdrDocumentCategoriser.getDocumentCategory(CaseDocumentType.WITHOUT_PREJUDICE_OFFERS))
            .thenReturn(DocumentCategory.FDR_DOCUMENTS_AND_FDR_BUNDLE_RESPONDENT_WITHOUT_PREJUDICE_OFFERS);
    }

    @Override
    public void setUpscreenUploadDocumentList() {
        screenUploadDocumentList.add(createContestedUploadDocumentItem(CaseDocumentType.WITHOUT_PREJUDICE_OFFERS,
            CaseDocumentParty.RESPONDENT, YesOrNo.NO, YesOrNo.YES, "Other Example"));

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
        when(applicantFdrDocumentCategoriser.getDocumentCategory(CaseDocumentType.OTHER))
            .thenReturn(DocumentCategory.FDR_BUNDLE);
        assertThat(
            handler.getDocumentCategoryFromDocumentType(CaseDocumentType.OTHER, CaseDocumentParty.APPLICANT),
            is(DocumentCategory.FDR_BUNDLE)
        );

        handler.getDocumentCategoryFromDocumentType(CaseDocumentType.WITHOUT_PREJUDICE_OFFERS, CaseDocumentParty.RESPONDENT);
        Mockito.verify(respondentFdrDocumentCategoriser).getDocumentCategory(CaseDocumentType.WITHOUT_PREJUDICE_OFFERS);
        handler.getDocumentCategoryFromDocumentType(CaseDocumentType.WITHOUT_PREJUDICE_OFFERS, CaseDocumentParty.APPLICANT);
        Mockito.verify(applicantFdrDocumentCategoriser).getDocumentCategory(CaseDocumentType.WITHOUT_PREJUDICE_OFFERS);
    }

}
