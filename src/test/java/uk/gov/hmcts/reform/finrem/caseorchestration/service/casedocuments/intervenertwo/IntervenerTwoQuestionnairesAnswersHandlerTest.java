package uk.gov.hmcts.reform.finrem.caseorchestration.service.casedocuments.intervenertwo;

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
public class IntervenerTwoQuestionnairesAnswersHandlerTest extends BaseManageDocumentsHandlerTest {

    @InjectMocks
    IntervenerTwoQuestionnairesAnswersHandler handler;

    @Test
    public void givenAddedDocOnScreenCollectionWhenAddNewOrMovedDocumentToCollectionThenAddScreenDocsToCollectionType() {
        screenUploadDocumentList.add(createContestedUploadDocumentItem(CaseDocumentType.QUESTIONNAIRE,
            CaseDocumentParty.INTERVENER_TWO, YesOrNo.NO, YesOrNo.NO, null));
        screenUploadDocumentList.add(createContestedUploadDocumentItem(CaseDocumentType.REPLY_TO_QUESTIONNAIRE,
            CaseDocumentParty.INTERVENER_TWO, YesOrNo.NO, YesOrNo.NO, null));

        caseDetails.getData().setManageCaseDocumentCollection(screenUploadDocumentList);

        handler.addManagedDocumentToSelectedCollection(
            FinremCallbackRequest.builder().caseDetails(caseDetails).caseDetailsBefore(caseDetails).build(),
            screenUploadDocumentList);

        assertThat(caseData.getUploadCaseDocumentWrapper()
                .getDocumentCollectionPerType(ManageCaseDocumentsCollectionType.INTV_TWO_QUESTIONNAIRES_ANSWERS_COLLECTION),
            hasSize(2));
        assertThat(caseData.getManageCaseDocumentCollection(),
            hasSize(0));
    }
}