package uk.gov.hmcts.reform.finrem.caseorchestration.service.casedocuments.respondent;

import org.junit.Test;
import uk.gov.hmcts.reform.finrem.caseorchestration.service.casedocuments.CaseDocumentHandlerTest;
import uk.gov.hmcts.reform.finrem.ccd.domain.YesOrNo;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.hasSize;

public class RespondentFormsHHandlerTest extends CaseDocumentHandlerTest {

    RespondentFormsHHandler respondentFormsHHandler = new RespondentFormsHHandler();

    @Test
    public void respFormsHFiltered() {
        uploadDocumentList.add(createContestedUploadDocumentItem("Form H", "respondent", YesOrNo.NO, YesOrNo.NO, null));

        caseDetails.getCaseData().getUploadCaseDocumentWrapper().setUploadCaseDocument(uploadDocumentList);

        respondentFormsHHandler.handle(uploadDocumentList, caseData);
        assertThat(caseData.getUploadCaseDocumentWrapper().getRespFormsHCollection(), hasSize(1));
    }
}