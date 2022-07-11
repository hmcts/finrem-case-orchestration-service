package uk.gov.hmcts.reform.finrem.caseorchestration.service.casedocuments.respondent;

import org.junit.Test;
import uk.gov.hmcts.reform.finrem.caseorchestration.service.casedocuments.CaseDocumentHandlerTest;
import uk.gov.hmcts.reform.finrem.ccd.domain.YesOrNo;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.hasSize;

public class RespondentFormEExhibitsHandlerTest extends CaseDocumentHandlerTest {

    RespondentFormEExhibitsHandler respondentFormEExhibitsHandler = new RespondentFormEExhibitsHandler();


    @Test
    public void respFormEExhibitsFiltered() {
        uploadDocumentList.add(createContestedUploadDocumentItem("Applicant - Form E", "respondent", YesOrNo.NO, YesOrNo.NO, null));

        caseDetails.getCaseData().getUploadCaseDocumentWrapper().setUploadCaseDocument(uploadDocumentList);

        respondentFormEExhibitsHandler.handle(uploadDocumentList, caseData);
        assertThat(caseData.getUploadCaseDocumentWrapper().getRespFormEExhibitsCollection(), hasSize(1));
    }
}