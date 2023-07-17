package uk.gov.hmcts.reform.finrem.caseorchestration.service.casedocuments.respondent;

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

import java.util.stream.Collectors;
import java.util.stream.Stream;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.hasSize;

@RunWith(MockitoJUnitRunner.class)
public class RespondentQuestionnairesAnswersCollectionServiceTest extends BaseManageDocumentsHandlerTest {

    @InjectMocks
    RespondentQuestionnairesAnswersHandler collectionService;

    @Test
    public void givenAddedDocOnScreenCollectionWhenAddNewOrMovedDocumentToCollectionThenAddScreenDocsToCollectionType() {
        screenUploadDocumentList.add(createContestedUploadDocumentItem(CaseDocumentType.QUESTIONNAIRE,
            CaseDocumentParty.RESPONDENT, YesOrNo.NO, YesOrNo.NO, null));
        screenUploadDocumentList.add(createContestedUploadDocumentItem(CaseDocumentType.REPLY_TO_QUESTIONNAIRE,
            CaseDocumentParty.RESPONDENT, YesOrNo.NO, YesOrNo.NO, null));

        caseDetails.getData().setManageCaseDocumentCollection(screenUploadDocumentList);

        collectionService.replaceManagedDocumentsInCollectionType(
            FinremCallbackRequest.builder().caseDetails(caseDetails).caseDetailsBefore(caseDetails).build(),
            screenUploadDocumentList);

        assertThat(caseData.getUploadCaseDocumentWrapper()
                .getDocumentCollectionPerType(CaseDocumentCollectionType.RESP_QUESTIONNAIRES_ANSWERS_COLLECTION),
            hasSize(2));
        assertThat(caseData.getManageCaseDocumentCollection(),
            hasSize(0));
    }

    @Test
    public void givenAddedDocWithPreviousCollectionNotEmptyWhenaddManagedDocumentToCollectionThenAddNonDuplicatesScreenDocsToCollectionType() {
        UploadCaseDocumentCollection questionnaireDoc =
            createContestedUploadDocumentItem(CaseDocumentType.QUESTIONNAIRE,
                CaseDocumentParty.RESPONDENT, YesOrNo.NO, YesOrNo.NO, null);
        screenUploadDocumentList.add(questionnaireDoc);
        screenUploadDocumentList.add(createContestedUploadDocumentItem(CaseDocumentType.REPLY_TO_QUESTIONNAIRE,
            CaseDocumentParty.RESPONDENT, YesOrNo.NO, YesOrNo.NO, null));
        caseDetails.getData().getUploadCaseDocumentWrapper()
            .setRespQaCollection(Stream.of(questionnaireDoc).collect(Collectors.toList()));

        caseDetails.getData().setManageCaseDocumentCollection(screenUploadDocumentList);

        collectionService.replaceManagedDocumentsInCollectionType(
            FinremCallbackRequest.builder().caseDetails(caseDetails).caseDetailsBefore(caseDetails).build(),
            screenUploadDocumentList);

        assertThat(caseData.getUploadCaseDocumentWrapper()
                .getDocumentCollectionPerType(CaseDocumentCollectionType.RESP_QUESTIONNAIRES_ANSWERS_COLLECTION),
            hasSize(2));
    }
}