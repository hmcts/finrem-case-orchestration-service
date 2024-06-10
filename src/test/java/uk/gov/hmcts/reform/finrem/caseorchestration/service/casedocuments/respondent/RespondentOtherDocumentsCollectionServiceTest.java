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
public class RespondentOtherDocumentsCollectionServiceTest extends BaseManageDocumentsHandlerTest {

    @InjectMocks
    RespondentOtherDocumentsHandler collectionService;


    @Override
    public void setUpscreenUploadDocumentList() {
        screenUploadDocumentList.add(createContestedUploadDocumentItem(CaseDocumentType.OTHER,
            CaseDocumentParty.RESPONDENT, YesOrNo.NO, YesOrNo.NO, "Other Example"));
        screenUploadDocumentList.add(createContestedUploadDocumentItem(CaseDocumentType.FORM_B,
            CaseDocumentParty.RESPONDENT, YesOrNo.NO, YesOrNo.NO, null));
        screenUploadDocumentList.add(createContestedUploadDocumentItem(CaseDocumentType.FORM_F,
            CaseDocumentParty.RESPONDENT, YesOrNo.NO, YesOrNo.NO, null));
        screenUploadDocumentList.add(createContestedUploadDocumentItem(CaseDocumentType.CARE_PLAN,
            CaseDocumentParty.RESPONDENT, YesOrNo.NO, YesOrNo.NO, null));
        screenUploadDocumentList.add(createContestedUploadDocumentItem(CaseDocumentType.PENSION_PLAN,
            CaseDocumentParty.RESPONDENT, YesOrNo.NO, YesOrNo.NO, null));
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

    @Override
    protected List<UploadCaseDocumentCollection> getDocumentCollection() {
        return caseData.getUploadCaseDocumentWrapper()
            .getDocumentCollectionPerType(CaseDocumentCollectionType.RESP_OTHER_COLLECTION);
    }

    @Override
    public void assertCorrectCategoryAssignedFromDocumentType() {
        assertThat(
            collectionService.getDocumentCategoryFromDocumentType(CaseDocumentType.OTHER, CaseDocumentParty.RESPONDENT),
            is(DocumentCategory.RESPONDENT_DOCUMENTS_MISCELLANEOUS_OR_OTHER)
        );

        assertThat(
            collectionService.getDocumentCategoryFromDocumentType(CaseDocumentType.FORM_B, CaseDocumentParty.RESPONDENT),
            is(DocumentCategory.ADMINISTRATIVE_DOCUMENTS_OTHER)
        );

        assertThat(
            collectionService.getDocumentCategoryFromDocumentType(CaseDocumentType.FORM_F, CaseDocumentParty.RESPONDENT),
            is(DocumentCategory.ADMINISTRATIVE_DOCUMENTS_OTHER)
        );

        assertThat(
            collectionService.getDocumentCategoryFromDocumentType(CaseDocumentType.CARE_PLAN, CaseDocumentParty.RESPONDENT),
            is(DocumentCategory.ADMINISTRATIVE_DOCUMENTS_OTHER)
        );

        assertThat(
            collectionService.getDocumentCategoryFromDocumentType(CaseDocumentType.PENSION_PLAN, CaseDocumentParty.RESPONDENT),
            is(DocumentCategory.REPORTS)
        );

        assertThat(
            collectionService.getDocumentCategoryFromDocumentType(CaseDocumentType.CERTIFICATES_OF_SERVICE, CaseDocumentParty.RESPONDENT),
            is(DocumentCategory.RESPONDENT_DOCUMENTS_CERTIFICATES_OF_SERVICE)
        );

        assertThat(
            collectionService.getDocumentCategoryFromDocumentType(CaseDocumentType.ES1, CaseDocumentParty.RESPONDENT),
            is(DocumentCategory.HEARING_DOCUMENTS_RESPONDENT_ES1)
        );

        assertThat(
            collectionService.getDocumentCategoryFromDocumentType(CaseDocumentType.PENSION_REPORT, CaseDocumentParty.RESPONDENT),
            is(DocumentCategory.REPORTS)
        );

        assertThat(
            collectionService.getDocumentCategoryFromDocumentType(CaseDocumentType.MORTGAGE_CAPACITIES, CaseDocumentParty.RESPONDENT),
            is(DocumentCategory.RESPONDENT_MORTGAGE_CAPACITIES_OR_HOUSING_PARTICULARS)
        );

        assertThat(
            collectionService.getDocumentCategoryFromDocumentType(CaseDocumentType.HOUSING_PARTICULARS, CaseDocumentParty.RESPONDENT),
            is(DocumentCategory.RESPONDENT_MORTGAGE_CAPACITIES_OR_HOUSING_PARTICULARS)
        );

        assertThat(
            collectionService.getDocumentCategoryFromDocumentType(CaseDocumentType.ATTENDANCE_SHEETS, CaseDocumentParty.RESPONDENT),
            is(DocumentCategory.RESPONDENT_DOCUMENTS)
        );

        assertThat(
            collectionService.getDocumentCategoryFromDocumentType(CaseDocumentType.POINTS_OF_CLAIM_OR_DEFENCE, CaseDocumentParty.RESPONDENT),
            is(DocumentCategory.RESPONDENT_DOCUMENTS_POINTS_OF_CLAIM_OR_DEFENCE)
        );

        assertThat(
            collectionService.getDocumentCategoryFromDocumentType(CaseDocumentType.FM5, CaseDocumentParty.RESPONDENT),
            is(DocumentCategory.HEARING_DOCUMENTS_RESPONDENT_FM5)
        );
    }
}
