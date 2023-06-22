package uk.gov.hmcts.reform.finrem.caseorchestration.service.casedocuments.intervenerfour;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.Test;
import uk.gov.hmcts.reform.finrem.caseorchestration.service.casedocuments.CaseDocumentHandlerTest;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.hasSize;
import static uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.CCDConfigConstant.CONTESTED_UPLOADED_DOCUMENTS;
import static uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.CCDConfigConstant.INTV_FOUR_QUESTIONNAIRES_ANSWERS_COLLECTION;
import static uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.IntervenerConstant.INTERVENER_FOUR;

public class IntervenerFourQuestionnairesAnswersHandlerTest extends CaseDocumentHandlerTest {

    IntervenerFourQuestionnairesAnswersHandler handler = new IntervenerFourQuestionnairesAnswersHandler(new ObjectMapper());


    @Test
    public void appQuestionnairesAnswersFiltered() {
        uploadDocumentList.add(createContestedUploadDocumentItem("Questionnaire", INTERVENER_FOUR, "no", "no", null));
        uploadDocumentList.add(createContestedUploadDocumentItem("Reply to Questionnaire", INTERVENER_FOUR, "no", "no", null));

        caseDetails.getData().put(CONTESTED_UPLOADED_DOCUMENTS, uploadDocumentList);

        handler.handle(uploadDocumentList, caseData);

        assertThat(getDocumentCollection(caseData, INTV_FOUR_QUESTIONNAIRES_ANSWERS_COLLECTION), hasSize(2));
    }
}