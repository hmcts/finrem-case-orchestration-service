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

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.hasSize;

@RunWith(MockitoJUnitRunner.class)
public class IntervenerTwoCaseSummariesHandlerTest extends BaseManageDocumentsHandlerTest {
    @InjectMocks
    IntervenerTwoCaseSummariesHandler handler;

    @Test
    public void givenAddedDocOnScreenCollectionWhenAddNewOrMovedDocumentToCollectionThenAddScreenDocsToCollectionType() {
        screenUploadDocumentList.add(createContestedUploadDocumentItem(CaseDocumentType.POSITION_STATEMENT,
            CaseDocumentParty.INTERVENER_TWO, YesOrNo.NO, YesOrNo.NO, null));
        screenUploadDocumentList.add(createContestedUploadDocumentItem(CaseDocumentType.SKELETON_ARGUMENT,
            CaseDocumentParty.INTERVENER_TWO, YesOrNo.NO, YesOrNo.NO, null));
        screenUploadDocumentList.add(createContestedUploadDocumentItem(CaseDocumentType.CASE_SUMMARY,
            CaseDocumentParty.INTERVENER_TWO, YesOrNo.NO, YesOrNo.NO, null));

        caseDetails.getData().setManageCaseDocumentCollection(screenUploadDocumentList);

        handler.replaceManagedDocumentsInCollectionType(
            FinremCallbackRequest.builder().caseDetails(caseDetails).caseDetailsBefore(caseDetails).build(),
            screenUploadDocumentList);

        assertThat(caseData.getUploadCaseDocumentWrapper()
                .getDocumentCollectionPerType(CaseDocumentCollectionType.INTERVENER_TWO_SUMMARIES_COLLECTION),
            hasSize(3));
        assertThat(caseData.getManageCaseDocumentCollection(),
            hasSize(0));
    }
}
