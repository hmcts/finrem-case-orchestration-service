package uk.gov.hmcts.reform.finrem.caseorchestration.service.casedocuments.applicant;

import org.junit.Test;
import uk.gov.hmcts.reform.finrem.caseorchestration.service.casedocuments.CaseDocumentHandlerTest;
import uk.gov.hmcts.reform.finrem.ccd.domain.YesOrNo;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.hasSize;

public class ApplicantQuestionnairesAnswersHandlerTest extends CaseDocumentHandlerTest {

    ApplicantQuestionnairesAnswersHandler applicantQuestionnairesAnswersHandler = new ApplicantQuestionnairesAnswersHandler();


    @Test
    public void appQuestionnairesAnswersFiltered() {
        uploadDocumentList.add(createContestedUploadDocumentItem("Questionnaire", "applicant", YesOrNo.NO, YesOrNo.NO, null));
        uploadDocumentList.add(createContestedUploadDocumentItem("Reply to Questionnaire", "applicant", YesOrNo.NO, YesOrNo.NO, null));

        caseDetails.getCaseData().getUploadCaseDocumentWrapper().setUploadCaseDocument(uploadDocumentList);

        applicantQuestionnairesAnswersHandler.handle(uploadDocumentList, caseData);

        assertThat(caseData.getUploadCaseDocumentWrapper().getAppQaCollection(), hasSize(2));
    }
}