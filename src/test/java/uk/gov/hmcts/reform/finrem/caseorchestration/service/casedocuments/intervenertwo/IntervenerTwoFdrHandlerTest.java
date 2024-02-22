package uk.gov.hmcts.reform.finrem.caseorchestration.service.casedocuments.intervenertwo;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.MockitoJUnitRunner;
import uk.gov.hmcts.reform.finrem.caseorchestration.handler.FinremCallbackRequest;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.CaseDocumentParty;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.CaseDocumentType;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.UploadCaseDocumentCollection;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.YesOrNo;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.document.CaseDocumentCollectionType;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.document.DocumentCategory;
import uk.gov.hmcts.reform.finrem.caseorchestration.service.casedocuments.BaseManageDocumentsHandlerTest;
import uk.gov.hmcts.reform.finrem.caseorchestration.service.casedocuments.DocumentHandler;

import java.util.ArrayList;
import java.util.List;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.hasSize;
import static org.hamcrest.Matchers.is;
import static org.mockito.Mockito.when;

@RunWith(MockitoJUnitRunner.Silent.class)
public class IntervenerTwoFdrHandlerTest extends BaseManageDocumentsHandlerTest {

    @Mock
    IntervenerTwoFdrDocumentCategoriser categoriser;
    @InjectMocks
    IntervenerTwoFdrHandler collectionService;

    @Override
    public void setUp() {
        super.setUp();
        when(categoriser.getDocumentCategory(CaseDocumentType.WITHOUT_PREJUDICE_OFFERS)).thenReturn(
            DocumentCategory.FDR_DOCUMENTS_AND_FDR_BUNDLE_INTERVENER_2_WITHOUT_PREJUDICE_OFFERS);
        when(categoriser.getDocumentCategory(CaseDocumentType.TRIAL_BUNDLE)).thenReturn(DocumentCategory.FDR_DOCUMENTS_AND_FDR_BUNDLE_INTERVENER_2);
    }


    @Test
    public void givenMovedDocOnScreenCollectionWhenAddManagedDocumentToCollectionThenAddScreenDocsToCollectionType() {
        screenUploadDocumentList = new ArrayList<>();
        screenUploadDocumentList.add(createContestedUploadDocumentItem(CaseDocumentType.TRIAL_BUNDLE,
            CaseDocumentParty.CASE, YesOrNo.NO, YesOrNo.NO, null));
        screenUploadDocumentList.add(createContestedUploadDocumentItem(CaseDocumentType.TRIAL_BUNDLE,
            CaseDocumentParty.INTERVENER_TWO, YesOrNo.NO, YesOrNo.YES, null));

        caseDetails.getData().setManageCaseDocumentCollection(screenUploadDocumentList);

        collectionService.replaceManagedDocumentsInCollectionType(
            FinremCallbackRequest.builder().caseDetails(caseDetails).caseDetailsBefore(caseDetails).build(),
            screenUploadDocumentList, true);

        assertThat(caseData.getUploadCaseDocumentWrapper()
                .getDocumentCollectionPerType(CaseDocumentCollectionType.INTERVENER_TWO_FDR_DOCS_COLLECTION),
            hasSize(1));
        assertThat(caseData.getManageCaseDocumentCollection(),
            hasSize(1));

    }

    @Override
    public void setUpscreenUploadDocumentList() {
        screenUploadDocumentList.add(createContestedUploadDocumentItem(CaseDocumentType.TRIAL_BUNDLE,
            CaseDocumentParty.INTERVENER_TWO, YesOrNo.NO, YesOrNo.YES, null));
    }

    @Override
    public DocumentHandler getDocumentHandler() {
        return collectionService;
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
            .getDocumentCollectionPerType(CaseDocumentCollectionType.INTERVENER_TWO_FDR_DOCS_COLLECTION);
    }

    @Override
    public void assertCorrectCategoryAssignedFromDocumentType() {
        assertThat(
            collectionService.getDocumentCategoryFromDocumentType(CaseDocumentType.TRIAL_BUNDLE, CaseDocumentParty.INTERVENER_TWO),
            is(DocumentCategory.FDR_DOCUMENTS_AND_FDR_BUNDLE_INTERVENER_2)
        );
        assertThat(
            collectionService.getDocumentCategoryFromDocumentType(CaseDocumentType.WITHOUT_PREJUDICE_OFFERS, CaseDocumentParty.INTERVENER_TWO),
            is(DocumentCategory.FDR_DOCUMENTS_AND_FDR_BUNDLE_INTERVENER_2_WITHOUT_PREJUDICE_OFFERS)
        );
    }
}
