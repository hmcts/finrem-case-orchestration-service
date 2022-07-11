package uk.gov.hmcts.reform.finrem.caseorchestration.service.casedocuments.respondent;

import org.junit.Test;
import uk.gov.hmcts.reform.finrem.caseorchestration.service.casedocuments.CaseDocumentHandlerTest;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.hasSize;
import static uk.gov.hmcts.reform.finrem.ccd.domain.YesOrNo.NO;

public class RespondentOtherDocumentsHandlerTest extends CaseDocumentHandlerTest {

    RespondentOtherDocumentsHandler respondentOtherDocumentsHandler = new RespondentOtherDocumentsHandler();

    @Test
    public void respOtherDocsFiltered() {
        uploadDocumentList.add(createContestedUploadDocumentItem("other", "respondent", NO, NO, "Other Example"));
        uploadDocumentList.add(createContestedUploadDocumentItem("Form B", "respondent", NO, NO, null));
        uploadDocumentList.add(createContestedUploadDocumentItem("Form F", "respondent", NO, NO, null));
        uploadDocumentList.add(createContestedUploadDocumentItem("Care Plan", "respondent", NO, NO, null));
        uploadDocumentList.add(createContestedUploadDocumentItem("Pension Plan", "respondent", NO, NO, null));

        caseDetails.getCaseData().getUploadCaseDocumentWrapper().setUploadCaseDocument(uploadDocumentList);

        respondentOtherDocumentsHandler.handle(uploadDocumentList, caseData);
        assertThat(caseData.getUploadCaseDocumentWrapper().getRespOtherCollection(), hasSize(5));
    }

}