package uk.gov.hmcts.reform.finrem.caseorchestration.service.casedocuments.intervenerone;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.mockito.junit.MockitoJUnitRunner;
import uk.gov.hmcts.reform.finrem.caseorchestration.handler.FinremCallbackRequest;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.CaseDocumentParty;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.CaseDocumentType;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.UploadCaseDocumentCollection;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.YesOrNo;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.document.CaseDocumentCollectionType;
import uk.gov.hmcts.reform.finrem.caseorchestration.service.casedocuments.BaseManageDocumentsHandlerTest;
import uk.gov.hmcts.reform.finrem.caseorchestration.service.casedocuments.IntervenerFourFdrHandler;

import java.util.ArrayList;
import java.util.List;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.hasSize;

@RunWith(MockitoJUnitRunner.class)
public class IntervenerFourFdrHandlerTest extends BaseManageDocumentsHandlerTest {

    @InjectMocks
    IntervenerFourFdrHandler collectionService;

    @Test
    public void givenAddedDocOnScreenCollectionWhenAddNewOrMovedDocumentToCollectionThenAddScreenDocsToCollectionType() {
        screenUploadDocumentList.add(createContestedUploadDocumentItem(CaseDocumentType.TRIAL_BUNDLE,
            CaseDocumentParty.INTERVENER_FOUR, YesOrNo.NO, YesOrNo.YES, null));

        caseDetails.getData().setManageCaseDocumentCollection(screenUploadDocumentList);

        collectionService.addManagedDocumentToSelectedCollection(
            FinremCallbackRequest.builder().caseDetails(caseDetails).caseDetailsBefore(caseDetails).build(),
            screenUploadDocumentList);

        assertThat(caseData.getUploadCaseDocumentWrapper()
                .getDocumentCollectionPerType(CaseDocumentCollectionType.INTERVENER_FOUR_FDR_DOCS_COLLECTION),
            hasSize(1));
        assertThat(caseData.getManageCaseDocumentCollection(),
            hasSize(0));
    }

    @Test
    public void givenMovedDocOnScreenCollectionWhenAddManagedDocumentToCollectionThenAddScreenDocsToCollectionType() {
        screenUploadDocumentList.add(createContestedUploadDocumentItem(CaseDocumentType.TRIAL_BUNDLE,
            CaseDocumentParty.CASE, YesOrNo.NO, YesOrNo.NO, null));
        screenUploadDocumentList.add(createContestedUploadDocumentItem(CaseDocumentType.TRIAL_BUNDLE,
            CaseDocumentParty.INTERVENER_FOUR, YesOrNo.NO, YesOrNo.YES, null));

        caseDetails.getData().setManageCaseDocumentCollection(screenUploadDocumentList);

        collectionService.addManagedDocumentToSelectedCollection(
            FinremCallbackRequest.builder().caseDetails(caseDetails).caseDetailsBefore(caseDetails).build(),
            screenUploadDocumentList);

        assertThat(caseData.getUploadCaseDocumentWrapper()
                .getDocumentCollectionPerType(CaseDocumentCollectionType.INTERVENER_FOUR_FDR_DOCS_COLLECTION),
            hasSize(1));
        assertThat(caseData.getManageCaseDocumentCollection(),
            hasSize(1));
    }

    @Test
    public void givenRemovedDocFromScreenCollectionWhenDeleteRemovedDocumentFromCollectionThenRemoveScreenDocsFromCollectionType() {
        List<UploadCaseDocumentCollection> beforeEventDocList = new ArrayList<>();
        UploadCaseDocumentCollection removedDoc = createContestedUploadDocumentItem(CaseDocumentType.TRIAL_BUNDLE,
            CaseDocumentParty.INTERVENER_FOUR, YesOrNo.NO, YesOrNo.YES, null);
        beforeEventDocList.add(removedDoc);
        caseData.getUploadCaseDocumentWrapper()
            .getDocumentCollectionPerType(CaseDocumentCollectionType.INTERVENER_FOUR_FDR_DOCS_COLLECTION)
            .addAll(beforeEventDocList);
        caseDetailsBefore.getData().getUploadCaseDocumentWrapper()
            .getDocumentCollectionPerType(CaseDocumentCollectionType.INTERVENER_FOUR_FDR_DOCS_COLLECTION)
            .addAll(beforeEventDocList);
        screenUploadDocumentList.addAll(beforeEventDocList);
        screenUploadDocumentList.remove(removedDoc);

        caseDetails.getData().setManageCaseDocumentCollection(screenUploadDocumentList);

        collectionService.removeManagedDocumentFromOriginalCollection(
            FinremCallbackRequest.builder().caseDetails(caseDetails).caseDetailsBefore(caseDetailsBefore).build()
        );

        assertThat(caseData.getUploadCaseDocumentWrapper()
                .getDocumentCollectionPerType(CaseDocumentCollectionType.INTERVENER_FOUR_FDR_DOCS_COLLECTION),
            hasSize(0));
    }
}