package uk.gov.hmcts.reform.finrem.caseorchestration.service.casedocuments.respondent;

import org.junit.Test;
import uk.gov.hmcts.reform.finrem.caseorchestration.service.casedocuments.CaseDocumentHandlerTest;
import uk.gov.hmcts.reform.finrem.ccd.domain.YesOrNo;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.hasSize;

public class RespondentQuestionnairesAnswersHandlerTest extends CaseDocumentHandlerTest {

    RespondentQuestionnairesAnswersHandler respondentQuestionnairesAnswersHandler = new RespondentQuestionnairesAnswersHandler();


    @Test
    public void respQuestionnairesAnswersFiltered() {
        uploadDocumentList.add(createContestedUploadDocumentItem("Questionnaire", "respondent", YesOrNo.NO, YesOrNo.NO, null));
        uploadDocumentList.add(createContestedUploadDocumentItem("Reply to Questionnaire", "respondent", YesOrNo.NO, YesOrNo.NO, null));

        caseDetails.getCaseData().getUploadCaseDocumentWrapper().setUploadCaseDocument(uploadDocumentList);

        respondentQuestionnairesAnswersHandler.handle(uploadDocumentList, caseData);
        assertThat(caseData.getUploadCaseDocumentWrapper().getRespQaCollection(), hasSize(2));
    }
}