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
import uk.gov.hmcts.reform.finrem.caseorchestration.model.document.DocumentCategory;
import uk.gov.hmcts.reform.finrem.caseorchestration.service.casedocuments.BaseManageDocumentsHandlerTest;
import uk.gov.hmcts.reform.finrem.caseorchestration.service.casedocuments.DocumentHandler;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.hasSize;
import static org.hamcrest.Matchers.is;

@RunWith(MockitoJUnitRunner.Silent.class)
public class RespondentQuestionnairesAnswersCollectionServiceTest extends BaseManageDocumentsHandlerTest {

    @InjectMocks
    RespondentQuestionnairesAnswersHandler collectionService;


    @Test
    public void givenAddedDocWithPreviousCollectionNotEmptyWhenaddManagedDocumentToCollectionThenAddNonDuplicatesScreenDocsToCollectionType() {
        screenUploadDocumentList = new ArrayList<>();
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
            screenUploadDocumentList, true);

        assertThat(caseData.getUploadCaseDocumentWrapper()
                .getDocumentCollectionPerType(CaseDocumentCollectionType.RESP_QUESTIONNAIRES_ANSWERS_COLLECTION),
            hasSize(2));
    }

    @Override
    public void setUpscreenUploadDocumentList() {
        screenUploadDocumentList.add(createContestedUploadDocumentItem(CaseDocumentType.QUESTIONNAIRE,
            CaseDocumentParty.RESPONDENT, YesOrNo.NO, YesOrNo.NO, null));
        screenUploadDocumentList.add(createContestedUploadDocumentItem(CaseDocumentType.REPLY_TO_QUESTIONNAIRE,
            CaseDocumentParty.RESPONDENT, YesOrNo.NO, YesOrNo.NO, null));
    }

    @Override
    public DocumentHandler getDocumentHandler() {
        return collectionService;
    }

    @Override
    public void assertExpectedCollectionType() {
        assertThat(getDocumentCollection(),
            hasSize(2));
        assertThat(caseData.getManageCaseDocumentCollection(),
            hasSize(0));
    }

    @Override
    protected List<UploadCaseDocumentCollection> getDocumentCollection() {
        return caseData.getUploadCaseDocumentWrapper()
            .getDocumentCollectionPerType(CaseDocumentCollectionType.RESP_QUESTIONNAIRES_ANSWERS_COLLECTION);
    }

    @Override
    public void assertCorrectCategoryAssignedFromDocumentType() {
        assertThat(
            collectionService.getDocumentCategoryFromDocumentType(CaseDocumentType.QUESTIONNAIRE, CaseDocumentParty.RESPONDENT),
            is(DocumentCategory.HEARING_DOCUMENTS_RESPONDENT_QUESTIONNAIRES)
        );

        assertThat(
            collectionService.getDocumentCategoryFromDocumentType(CaseDocumentType.REPLY_TO_QUESTIONNAIRE, CaseDocumentParty.RESPONDENT),
            is(DocumentCategory.RESPONDENT_DOCUMENTS_REPLIES_TO_QUESTIONNAIRE)
        );

        assertThat(
            collectionService.getDocumentCategoryFromDocumentType(CaseDocumentType.FORM_G, CaseDocumentParty.RESPONDENT),
            is(DocumentCategory.UNCATEGORISED)
        );
    }
}
