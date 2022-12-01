package uk.gov.hmcts.reform.finrem.caseorchestration.service.casedocuments.respondent;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.Test;
import uk.gov.hmcts.reform.finrem.caseorchestration.service.casedocuments.CaseDocumentManagerTest;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.hasSize;
import static uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.CCDConfigConstant.CONTESTED_UPLOADED_DOCUMENTS;
import static uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.CCDConfigConstant.RESP_QUESTIONNAIRES_ANSWERS_COLLECTION;

public class RespondentQuestionnairesAnswersHandlerTest extends CaseDocumentManagerTest {

    RespondentQuestionnairesAnswersManager respondentQuestionnairesAnswersHandler = new RespondentQuestionnairesAnswersManager(new ObjectMapper());


    @Test
    public void respQuestionnairesAnswersFiltered() {
        uploadDocumentList.add(createContestedUploadDocumentItem("Questionnaire", "respondent", "no", "no", null));
        uploadDocumentList.add(createContestedUploadDocumentItem("Reply to Questionnaire", "respondent", "no", "no", null));

        caseDetails.getData().put(CONTESTED_UPLOADED_DOCUMENTS, uploadDocumentList);

        respondentQuestionnairesAnswersHandler.manageDocumentCollection(uploadDocumentList, caseData);

        assertThat(getDocumentCollection(caseData, RESP_QUESTIONNAIRES_ANSWERS_COLLECTION), hasSize(2));
    }
}