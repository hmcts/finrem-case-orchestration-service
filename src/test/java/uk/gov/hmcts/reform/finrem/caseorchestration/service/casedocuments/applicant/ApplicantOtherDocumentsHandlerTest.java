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
            hasSize(2));

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
            collectionService.getDocumentCategoryFromDocumentType(CaseDocumentType.OTHER, CaseDocumentParty.APPLICANT),
            is(DocumentCategory.APPLICANT_DOCUMENTS_MISCELLANEOUS_OR_OTHER)
        );

        assertThat(
            collectionService.getDocumentCategoryFromDocumentType(CaseDocumentType.PENSION_PLAN, CaseDocumentParty.APPLICANT),
            is(DocumentCategory.APPLICANT_DOCUMENTS_PENSION_PLAN)
        );

        assertThat(
            collectionService.getDocumentCategoryFromDocumentType(CaseDocumentType.CERTIFICATES_OF_SERVICE, CaseDocumentParty.APPLICANT),
            is(DocumentCategory.APPLICANT_DOCUMENTS_CERTIFICATES_OF_SERVICE)
        );

        assertThat(
            collectionService.getDocumentCategoryFromDocumentType(CaseDocumentType.ES1, CaseDocumentParty.APPLICANT),
            is(DocumentCategory.HEARING_DOCUMENTS_APPLICANT_ES1)
        );

        assertThat(
            collectionService.getDocumentCategoryFromDocumentType(CaseDocumentType.ES2, CaseDocumentParty.APPLICANT),
            is(DocumentCategory.HEARING_DOCUMENTS_APPLICANT_ES2)
        );

        assertThat(
            collectionService.getDocumentCategoryFromDocumentType(CaseDocumentType.MORTGAGE_CAPACITIES, CaseDocumentParty.APPLICANT),
            is(DocumentCategory.HEARING_DOCUMENTS_APPLICANT_MORTGAGE_CAPACITIES)
        );

        assertThat(
            collectionService.getDocumentCategoryFromDocumentType(CaseDocumentType.PENSION_REPORT, CaseDocumentParty.APPLICANT),
            is(DocumentCategory.REPORTS_PENSION_REPORTS)
        );

        assertThat(
            collectionService.getDocumentCategoryFromDocumentType(CaseDocumentType.ATTENDANCE_SHEETS, CaseDocumentParty.APPLICANT),
            is(DocumentCategory.APPLICANT_DOCUMENTS)
        );

        assertThat(
            collectionService.getDocumentCategoryFromDocumentType(CaseDocumentType.HOUSING_PARTICULARS, CaseDocumentParty.APPLICANT),
            is(DocumentCategory.APPLICANT_DOCUMENTS_HOUSING_PARTICULARS)
        );

        assertThat(
            collectionService.getDocumentCategoryFromDocumentType(CaseDocumentType.PRE_HEARING_DRAFT_ORDER, CaseDocumentParty.APPLICANT),
            is(DocumentCategory.HEARING_DOCUMENTS_APPLICANT_PRE_HEARING_DRAFT_ORDER)
        );

    }
}
