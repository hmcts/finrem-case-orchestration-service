package uk.gov.hmcts.reform.finrem.caseorchestration.service.casedocuments.intervenerthree;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.mockito.junit.MockitoJUnitRunner;
import uk.gov.hmcts.reform.finrem.caseorchestration.handler.FinremCallbackRequest;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.CaseDocumentParty;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.CaseDocumentType;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.YesOrNo;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.document.ManageCaseDocumentsCollectionType;
import uk.gov.hmcts.reform.finrem.caseorchestration.service.casedocuments.BaseManageDocumentsHandlerTest;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.hasSize;

@RunWith(MockitoJUnitRunner.class)
public class IntervenerThreeFormsHHandlerTest extends BaseManageDocumentsHandlerTest {

    @InjectMocks
    IntervenerThreeFormsHHandler handler;

    @Test
    public void givenAddedDocOnScreenCollectionWhenAddNewOrMovedDocumentToCollectionThenAddScreenDocsToCollectionType() {
        screenUploadDocumentList.add(createContestedUploadDocumentItem(CaseDocumentType.FORM_H,
            CaseDocumentParty.INTERVENER_THREE, YesOrNo.NO, YesOrNo.NO, null));

        caseDetails.getData().setManageCaseDocumentCollection(screenUploadDocumentList);

        handler.addManagedDocumentToSelectedCollection(
            FinremCallbackRequest.builder().caseDetails(caseDetails).caseDetailsBefore(caseDetails).build(),
            screenUploadDocumentList);

        assertThat(caseData.getUploadCaseDocumentWrapper()
                .getDocumentCollectionPerType(ManageCaseDocumentsCollectionType.INTV_THREE_FORMS_H_COLLECTION),
            hasSize(1));
        assertThat(caseData.getManageCaseDocumentCollection(),
            hasSize(0));
    }
}