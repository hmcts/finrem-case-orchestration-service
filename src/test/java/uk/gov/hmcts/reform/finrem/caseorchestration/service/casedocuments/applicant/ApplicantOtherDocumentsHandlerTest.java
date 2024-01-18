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
public class ApplicantOtherDocumentsHandlerTest extends BaseManageDocumentsHandlerTest {

    @InjectMocks
    ApplicantOtherDocumentsHandler collectionService;


    @Override
    public void setUpscreenUploadDocumentList() {
        screenUploadDocumentList.add(createContestedUploadDocumentItem(CaseDocumentType.OTHER,
            CaseDocumentParty.APPLICANT, YesOrNo.NO, YesOrNo.NO, "Other Example"));
        screenUploadDocumentList.add(createContestedUploadDocumentItem(CaseDocumentType.FORM_B,
            CaseDocumentParty.APPLICANT, YesOrNo.NO, YesOrNo.NO, null));
        screenUploadDocumentList.add(createContestedUploadDocumentItem(CaseDocumentType.FORM_F,
            CaseDocumentParty.APPLICANT, YesOrNo.NO, YesOrNo.NO, null));
        screenUploadDocumentList.add(createContestedUploadDocumentItem(CaseDocumentType.CARE_PLAN,
            CaseDocumentParty.APPLICANT, YesOrNo.NO, YesOrNo.NO, null));
        screenUploadDocumentList.add(createContestedUploadDocumentItem(CaseDocumentType.PENSION_PLAN,
            CaseDocumentParty.APPLICANT, YesOrNo.NO, YesOrNo.NO, null));

    }

    @Override
    public DocumentHandler getDocumentHandler() {
        return collectionService;
    }


    @Override
    public void assertExpectedCollectionType() {
        assertThat(getDocumentCollection(),
            hasSize(5));

        assertThat(caseData.getManageCaseDocumentCollection(),
            hasSize(0));
    }

    protected List<UploadCaseDocumentCollection> getDocumentCollection() {
        return caseData.getUploadCaseDocumentWrapper()
            .getDocumentCollectionPerType(CaseDocumentCollectionType.APP_OTHER_COLLECTION);
    }

    @Override
    public void assertCorrectCategoryAssignedFromDocumentType() {
        assertThat(
            collectionService.getDocumentCategoryFromDocumentType(CaseDocumentType.OTHER),
            is(DocumentCategory.APPLICANT_DOCUMENTS_MISCELLANEOUS_OR_OTHER)
        );

        assertThat(
            collectionService.getDocumentCategoryFromDocumentType(CaseDocumentType.FORM_B),
            is(DocumentCategory.ADMINISTRATIVE_DOCUMENTS_OTHER)
        );

        assertThat(
            collectionService.getDocumentCategoryFromDocumentType(CaseDocumentType.FORM_F),
            is(DocumentCategory.ADMINISTRATIVE_DOCUMENTS_OTHER)
        );

        assertThat(
            collectionService.getDocumentCategoryFromDocumentType(CaseDocumentType.CARE_PLAN),
            is(DocumentCategory.ADMINISTRATIVE_DOCUMENTS_OTHER)
        );

        assertThat(
            collectionService.getDocumentCategoryFromDocumentType(CaseDocumentType.PENSION_PLAN),
            is(DocumentCategory.APPLICANT_DOCUMENTS_PENSION_PLAN)
        );

        assertThat(
            collectionService.getDocumentCategoryFromDocumentType(CaseDocumentType.CERTIFICATES_OF_SERVICE),
            is(DocumentCategory.APPLICANT_DOCUMENTS_CERTIFICATES_OF_SERVICE)
        );

        assertThat(
            collectionService.getDocumentCategoryFromDocumentType(CaseDocumentType.ES1),
            is(DocumentCategory.HEARING_DOCUMENTS_APPLICANT_ES1)
        );

        assertThat(
            collectionService.getDocumentCategoryFromDocumentType(CaseDocumentType.ES2),
            is(DocumentCategory.HEARING_DOCUMENTS_APPLICANT_ES2)
        );

        assertThat(
            collectionService.getDocumentCategoryFromDocumentType(CaseDocumentType.MORTGAGE_CAPACITIES),
            is(DocumentCategory.HEARING_DOCUMENTS_APPLICANT_MORTGAGE_CAPACITIES)
        );

        assertThat(
            collectionService.getDocumentCategoryFromDocumentType(CaseDocumentType.PENSION_REPORT),
            is(DocumentCategory.REPORTS_PENSION_REPORTS)
        );

        assertThat(
            collectionService.getDocumentCategoryFromDocumentType(CaseDocumentType.ATTENDANCE_SHEETS),
            is(DocumentCategory.APPLICANT_DOCUMENTS)
        );

        assertThat(
            collectionService.getDocumentCategoryFromDocumentType(CaseDocumentType.HOUSING_PARTICULARS),
            is(DocumentCategory.APPLICANT_DOCUMENTS_HOUSING_PARTICULARS)
        );

        assertThat(
            collectionService.getDocumentCategoryFromDocumentType(CaseDocumentType.PRE_HEARING_DRAFT_ORDER),
            is(DocumentCategory.HEARING_DOCUMENTS_APPLICANT_PRE_HEARING_DRAFT_ORDER)
        );

    }
}
