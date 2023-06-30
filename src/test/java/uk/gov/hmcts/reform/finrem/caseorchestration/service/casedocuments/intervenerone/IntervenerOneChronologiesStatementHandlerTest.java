package uk.gov.hmcts.reform.finrem.caseorchestration.service.casedocuments.intervenerone;

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
public class IntervenerOneChronologiesStatementHandlerTest extends BaseManageDocumentsHandlerTest {

    @InjectMocks
    IntervenerOneChronologiesStatementHandler handler;

   

    @Test
    public void givenAddedDocOnScreenCollectionWhenAddNewOrMovedDocumentToCollectionThenAddScreenDocsToCollectionType() {
        screenUploadDocumentList.add(createContestedUploadDocumentItem(CaseDocumentType.STATEMENT_OF_ISSUES,
            CaseDocumentParty.INTERVENER_ONE, YesOrNo.NO, YesOrNo.NO, null));
        screenUploadDocumentList.add(createContestedUploadDocumentItem(CaseDocumentType.CHRONOLOGY,
            CaseDocumentParty.INTERVENER_ONE, YesOrNo.NO, YesOrNo.NO, null));
        screenUploadDocumentList.add(createContestedUploadDocumentItem(CaseDocumentType.FORM_G,
            CaseDocumentParty.INTERVENER_ONE, YesOrNo.NO, YesOrNo.NO, null));

        caseDetails.getData().setManageCaseDocumentCollection(screenUploadDocumentList);

        handler.addManagedDocumentToSelectedCollection(
            FinremCallbackRequest.builder().caseDetails(caseDetails).caseDetailsBefore(caseDetails).build(),
            screenUploadDocumentList);

        assertThat(caseData.getUploadCaseDocumentWrapper()
                .getDocumentCollectionPerType(ManageCaseDocumentsCollectionType.INTV_ONE_CHRONOLOGIES_STATEMENTS_COLLECTION),
            hasSize(3));
        assertThat(caseData.getManageCaseDocumentCollection(),
            hasSize(0));
    }

}