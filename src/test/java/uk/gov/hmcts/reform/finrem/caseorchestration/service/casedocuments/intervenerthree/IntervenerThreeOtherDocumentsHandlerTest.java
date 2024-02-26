package uk.gov.hmcts.reform.finrem.caseorchestration.service.casedocuments.intervenerthree;

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
public class IntervenerThreeOtherDocumentsHandlerTest extends BaseManageDocumentsHandlerTest {

    @InjectMocks
    IntervenerThreeOtherDocumentsHandler handler;


    @Override
    public void setUpscreenUploadDocumentList() {
        screenUploadDocumentList.add(createContestedUploadDocumentItem(CaseDocumentType.OTHER,
            CaseDocumentParty.INTERVENER_THREE, YesOrNo.NO, YesOrNo.NO, null));
        screenUploadDocumentList.add(createContestedUploadDocumentItem(CaseDocumentType.FORM_B,
            CaseDocumentParty.INTERVENER_THREE, YesOrNo.NO, YesOrNo.NO, null));
        screenUploadDocumentList.add(createContestedUploadDocumentItem(CaseDocumentType.FORM_F,
            CaseDocumentParty.INTERVENER_THREE, YesOrNo.NO, YesOrNo.NO, null));
        screenUploadDocumentList.add(createContestedUploadDocumentItem(CaseDocumentType.CARE_PLAN,
            CaseDocumentParty.INTERVENER_THREE, YesOrNo.NO, YesOrNo.NO, null));
        screenUploadDocumentList.add(createContestedUploadDocumentItem(CaseDocumentType.PENSION_PLAN,
            CaseDocumentParty.INTERVENER_THREE, YesOrNo.NO, YesOrNo.NO, null));
    }

    @Override
    public DocumentHandler getDocumentHandler() {
        return handler;
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
            .getDocumentCollectionPerType(CaseDocumentCollectionType.INTERVENER_THREE_OTHER_COLLECTION);
    }

    @Override
    public void assertCorrectCategoryAssignedFromDocumentType() {
        assertThat(
            handler.getDocumentCategoryFromDocumentType(CaseDocumentType.OTHER, CaseDocumentParty.INTERVENER_THREE),
            is(DocumentCategory.INTERVENER_DOCUMENTS_INTERVENER_3_MISCELLANEOUS_OR_OTHER)
        );

        assertThat(
            handler.getDocumentCategoryFromDocumentType(CaseDocumentType.FORM_B, CaseDocumentParty.INTERVENER_THREE),
            is(DocumentCategory.ADMINISTRATIVE_DOCUMENTS_OTHER)
        );

        assertThat(
            handler.getDocumentCategoryFromDocumentType(CaseDocumentType.FORM_F, CaseDocumentParty.INTERVENER_THREE),
            is(DocumentCategory.ADMINISTRATIVE_DOCUMENTS_OTHER)
        );

        assertThat(
            handler.getDocumentCategoryFromDocumentType(CaseDocumentType.CARE_PLAN, CaseDocumentParty.INTERVENER_THREE),
            is(DocumentCategory.ADMINISTRATIVE_DOCUMENTS_OTHER)
        );


        assertThat(
            handler.getDocumentCategoryFromDocumentType(CaseDocumentType.MORTGAGE_CAPACITIES,
                CaseDocumentParty.INTERVENER_THREE),
            is(DocumentCategory.INTERVENER_DOCUMENTS_INTERVENER_3_MORTGAGE_CAPACITIES_OR_HOUSING_PARTICULARS)
        );

        assertThat(
            handler.getDocumentCategoryFromDocumentType(CaseDocumentType.HOUSING_PARTICULARS,
                CaseDocumentParty.INTERVENER_THREE),
            is(DocumentCategory.INTERVENER_DOCUMENTS_INTERVENER_3_MORTGAGE_CAPACITIES_OR_HOUSING_PARTICULARS)
        );

        assertThat(
            handler.getDocumentCategoryFromDocumentType(CaseDocumentType.PENSION_PLAN,
                CaseDocumentParty.INTERVENER_THREE),
            is(DocumentCategory.INTERVENER_DOCUMENTS_INTERVENER_3_PENSION_PLAN)
        );

        assertThat(
            handler.getDocumentCategoryFromDocumentType(CaseDocumentType.CERTIFICATES_OF_SERVICE,
                CaseDocumentParty.INTERVENER_THREE),
            is(DocumentCategory.INTERVENER_DOCUMENTS_INTERVENER_3_CERTIFICATES_OF_SERVICE)
        );

        assertThat(
            handler.getDocumentCategoryFromDocumentType(CaseDocumentType.ES1, CaseDocumentParty.INTERVENER_THREE),
            is(DocumentCategory.HEARING_DOCUMENTS_INTERVENER_3_ES1)
        );

        assertThat(
            handler.getDocumentCategoryFromDocumentType(CaseDocumentType.PENSION_REPORT,
                CaseDocumentParty.INTERVENER_THREE),
            is(DocumentCategory.REPORTS)
        );
    }
}
