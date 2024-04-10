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
            collectionService.getDocumentCategoryFromDocumentType(CaseDocumentType.OTHER, CaseDocumentParty.APPLICANT),
            is(DocumentCategory.APPLICANT_DOCUMENTS_MISCELLANEOUS_OR_OTHER)
        );

        assertThat(
            collectionService.getDocumentCategoryFromDocumentType(CaseDocumentType.FORM_B, CaseDocumentParty.APPLICANT),
            is(DocumentCategory.ADMINISTRATIVE_DOCUMENTS_OTHER)
        );

        assertThat(
            collectionService.getDocumentCategoryFromDocumentType(CaseDocumentType.FORM_F, CaseDocumentParty.APPLICANT),
            is(DocumentCategory.ADMINISTRATIVE_DOCUMENTS_OTHER)
        );

        assertThat(
            collectionService.getDocumentCategoryFromDocumentType(CaseDocumentType.CARE_PLAN, CaseDocumentParty.APPLICANT),
            is(DocumentCategory.ADMINISTRATIVE_DOCUMENTS_OTHER)
        );

        assertThat(
            collectionService.getDocumentCategoryFromDocumentType(CaseDocumentType.PENSION_PLAN, CaseDocumentParty.APPLICANT),
            is(DocumentCategory.REPORTS)
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
            is(DocumentCategory.APPLICANT_MORTGAGE_CAPACITIES_OR_HOUSING_PARTICULARS)
        );

        assertThat(
            collectionService.getDocumentCategoryFromDocumentType(CaseDocumentType.WITHOUT_PREJUDICE_OFFERS, CaseDocumentParty.APPLICANT),
            is(DocumentCategory.FDR_DOCUMENTS_AND_FDR_BUNDLE_APPLICANT_WITHOUT_PREJUDICE_OFFERS)
        );

        assertThat(
            collectionService.getDocumentCategoryFromDocumentType(CaseDocumentType.PENSION_REPORT, CaseDocumentParty.APPLICANT),
            is(DocumentCategory.REPORTS)
        );

        assertThat(
            collectionService.getDocumentCategoryFromDocumentType(CaseDocumentType.ATTENDANCE_SHEETS, CaseDocumentParty.APPLICANT),
            is(DocumentCategory.APPLICANT_DOCUMENTS)
        );

        assertThat(
            collectionService.getDocumentCategoryFromDocumentType(CaseDocumentType.HOUSING_PARTICULARS, CaseDocumentParty.APPLICANT),
            is(DocumentCategory.APPLICANT_MORTGAGE_CAPACITIES_OR_HOUSING_PARTICULARS)
        );

        assertThat(
            collectionService.getDocumentCategoryFromDocumentType(CaseDocumentType.PRE_HEARING_DRAFT_ORDER, CaseDocumentParty.APPLICANT),
            is(DocumentCategory.HEARING_DOCUMENTS_APPLICANT_PRE_HEARING_DRAFT_ORDER)
        );

        assertThat(
            collectionService.getDocumentCategoryFromDocumentType(CaseDocumentType.POINTS_OF_CLAIM_OR_DEFENCE, CaseDocumentParty.APPLICANT),
            is(DocumentCategory.APPLICANT_DOCUMENTS_POINTS_OF_CLAIM_OR_DEFENCE)
        );

        assertThat(
            collectionService.getDocumentCategoryFromDocumentType(CaseDocumentType.FM5, CaseDocumentParty.APPLICANT),
            is(DocumentCategory.HEARING_DOCUMENTS_APPLICANT_FM5)
        );

    }
}
