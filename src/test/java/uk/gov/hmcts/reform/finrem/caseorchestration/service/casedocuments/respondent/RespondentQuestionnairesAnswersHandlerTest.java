package uk.gov.hmcts.reform.finrem.caseorchestration.service.casedocuments.respondent;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.Test;
import uk.gov.hmcts.reform.finrem.caseorchestration.service.casedocuments.CaseDocumentCollectionsManagerTest;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.hasSize;
import static uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.CCDConfigConstant.CONTESTED_UPLOADED_DOCUMENTS;
import static uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.CCDConfigConstant.RESP_QUESTIONNAIRES_ANSWERS_COLLECTION;

public class RespondentQuestionnairesAnswersHandlerTest extends CaseDocumentCollectionsManagerTest {

    RespondentQuestionnairesAnswersCollectionService respondentQuestionnairesAnswersHandler = new RespondentQuestionnairesAnswersCollectionService(new ObjectMapper());


    @Test
    public void respQuestionnairesAnswersFiltered() {
        uploadDocumentList.add(createContestedUploadDocumentItem("Questionnaire", "respondent", "no", "no", null));
        uploadDocumentList.add(createContestedUploadDocumentItem("Reply to Questionnaire", "respondent", "no", "no", null));

        caseDetails.getData().put(CONTESTED_UPLOADED_DOCUMENTS, uploadDocumentList);

        respondentQuestionnairesAnswersHandler.processUploadDocumentCollection(caseData);

        assertThat(getDocumentCollection(caseData, RESP_QUESTIONNAIRES_ANSWERS_COLLECTION), hasSize(2));
    }
}