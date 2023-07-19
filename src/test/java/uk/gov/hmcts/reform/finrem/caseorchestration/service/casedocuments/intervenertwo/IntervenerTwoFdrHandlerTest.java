package uk.gov.hmcts.reform.finrem.caseorchestration.service.casedocuments.intervenertwo;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.mockito.junit.MockitoJUnitRunner;
import uk.gov.hmcts.reform.finrem.caseorchestration.handler.FinremCallbackRequest;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.CaseDocumentParty;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.CaseDocumentType;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.YesOrNo;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.document.CaseDocumentCollectionType;
import uk.gov.hmcts.reform.finrem.caseorchestration.service.casedocuments.BaseManageDocumentsHandlerTest;
import uk.gov.hmcts.reform.finrem.caseorchestration.service.casedocuments.IntervenerTwoFdrHandler;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.hasSize;

@RunWith(MockitoJUnitRunner.class)
public class IntervenerTwoFdrHandlerTest extends BaseManageDocumentsHandlerTest {

    @InjectMocks
    IntervenerTwoFdrHandler collectionService;

    @Test
    public void givenAddedDocOnScreenCollectionWhenAddNewOrMovedDocumentToCollectionThenAddScreenDocsToCollectionType() {
        screenUploadDocumentList.add(createContestedUploadDocumentItem(CaseDocumentType.TRIAL_BUNDLE,
            CaseDocumentParty.INTERVENER_TWO, YesOrNo.NO, YesOrNo.YES, null));

        caseDetails.getData().setManageCaseDocumentCollection(screenUploadDocumentList);

        collectionService.replaceManagedDocumentsInCollectionType(
            FinremCallbackRequest.builder().caseDetails(caseDetails).caseDetailsBefore(caseDetails).build(),
            screenUploadDocumentList);

        assertThat(caseData.getUploadCaseDocumentWrapper()
                .getDocumentCollectionPerType(CaseDocumentCollectionType.INTERVENER_TWO_FDR_DOCS_COLLECTION),
            hasSize(1));
        assertThat(caseData.getManageCaseDocumentCollection(),
            hasSize(0));
    }

    @Test
    public void givenMovedDocOnScreenCollectionWhenAddManagedDocumentToCollectionThenAddScreenDocsToCollectionType() {
        screenUploadDocumentList.add(createContestedUploadDocumentItem(CaseDocumentType.TRIAL_BUNDLE,
            CaseDocumentParty.CASE, YesOrNo.NO, YesOrNo.NO, null));
        screenUploadDocumentList.add(createContestedUploadDocumentItem(CaseDocumentType.TRIAL_BUNDLE,
            CaseDocumentParty.INTERVENER_TWO, YesOrNo.NO, YesOrNo.YES, null));

        caseDetails.getData().setManageCaseDocumentCollection(screenUploadDocumentList);

        collectionService.replaceManagedDocumentsInCollectionType(
            FinremCallbackRequest.builder().caseDetails(caseDetails).caseDetailsBefore(caseDetails).build(),
            screenUploadDocumentList);

        assertThat(caseData.getUploadCaseDocumentWrapper()
                .getDocumentCollectionPerType(CaseDocumentCollectionType.INTERVENER_TWO_FDR_DOCS_COLLECTION),
            hasSize(1));
        assertThat(caseData.getManageCaseDocumentCollection(),
            hasSize(1));
    }
}