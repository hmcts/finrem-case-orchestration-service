package uk.gov.hmcts.reform.finrem.caseorchestration.service.casedocuments.intervenertwo;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.mockito.junit.MockitoJUnitRunner;
import uk.gov.hmcts.reform.finrem.caseorchestration.handler.FinremCallbackRequest;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.CaseDocumentParty;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.CaseDocumentType;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.FinremCaseDataContested;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.YesOrNo;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.document.CaseDocumentCollectionType;
import uk.gov.hmcts.reform.finrem.caseorchestration.service.casedocuments.BaseManageDocumentsHandlerTest;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.hasSize;

@RunWith(MockitoJUnitRunner.class)
public class IntervenerTwoFormsHHandlerTest extends BaseManageDocumentsHandlerTest {

    @InjectMocks
    IntervenerTwoFormsHHandler handler;

    @Test
    public void givenAddedDocOnScreenCollectionWhenAddNewOrMovedDocumentToCollectionThenAddScreenDocsToCollectionType() {
        screenUploadDocumentList.add(createContestedUploadDocumentItem(CaseDocumentType.FORM_H,
            CaseDocumentParty.INTERVENER_TWO, YesOrNo.NO, YesOrNo.NO, null));

        caseDetails.getData().setManageCaseDocumentCollection(screenUploadDocumentList);

        handler.replaceManagedDocumentsInCollectionType(
            FinremCallbackRequest.<FinremCaseDataContested>builder().caseDetails(caseDetails).caseDetailsBefore(caseDetails).build(),
            screenUploadDocumentList);

        assertThat(caseData.getUploadCaseDocumentWrapper()
                .getDocumentCollectionPerType(CaseDocumentCollectionType.INTERVENER_TWO_FORM_H_COLLECTION),
            hasSize(1));
        assertThat(caseData.getManageCaseDocumentCollection(),
            hasSize(0));
    }
}