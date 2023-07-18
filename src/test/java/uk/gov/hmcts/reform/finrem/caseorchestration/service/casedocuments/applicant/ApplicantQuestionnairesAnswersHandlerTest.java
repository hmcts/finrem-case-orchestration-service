package uk.gov.hmcts.reform.finrem.caseorchestration.service.casedocuments.applicant;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.Test;
import uk.gov.hmcts.reform.finrem.caseorchestration.service.casedocuments.CaseDocumentHandlerTest;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.hasSize;
import static uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.CCDConfigConstant.APP_QUESTIONNAIRES_ANSWERS_COLLECTION;
import static uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.CCDConfigConstant.CONTESTED_UPLOADED_DOCUMENTS;

public class ApplicantQuestionnairesAnswersHandlerTest extends CaseDocumentHandlerTest {

    ApplicantQuestionnairesAnswersHandler applicantQuestionnairesAnswersHandler = new ApplicantQuestionnairesAnswersHandler(new ObjectMapper());


    @Test
    public void appQuestionnairesAnswersFiltered() {
        uploadDocumentList.add(createContestedUploadDocumentItem("Questionnaire", "applicant", "no", "no", null));
        uploadDocumentList.add(createContestedUploadDocumentItem("Reply to Questionnaire", "applicant", "no", "no", null));

        caseDetails.getData().put(CONTESTED_UPLOADED_DOCUMENTS, uploadDocumentList);

        applicantQuestionnairesAnswersHandler.handle(uploadDocumentList, caseData);

        assertThat(getDocumentCollection(caseData, APP_QUESTIONNAIRES_ANSWERS_COLLECTION), hasSize(2));
    }
}