package uk.gov.hmcts.reform.finrem.caseorchestration.service.casedocuments.respondent;

import org.junit.Test;
import uk.gov.hmcts.reform.finrem.caseorchestration.service.casedocuments.CaseDocumentHandlerTest;
import uk.gov.hmcts.reform.finrem.ccd.domain.YesOrNo;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.hasSize;

public class RespondentStatementsExhibitsHandlerTest extends CaseDocumentHandlerTest {

    RespondentStatementsExhibitsHandler respondentStatementsExhibitsHandler = new RespondentStatementsExhibitsHandler();

    @Test
    public void respStatementsExhibitsFiltered() {
        uploadDocumentList.add(createContestedUploadDocumentItem("Statement/Affidavit", "respondent", YesOrNo.NO, YesOrNo.NO, null));
        uploadDocumentList.add(createContestedUploadDocumentItem("Witness Statement/Affidavit", "respondent", YesOrNo.NO, YesOrNo.NO, null));

        caseDetails.getCaseData().getUploadCaseDocumentWrapper().setUploadCaseDocument(uploadDocumentList);
        respondentStatementsExhibitsHandler.handle(uploadDocumentList, caseData);
        assertThat(caseData.getUploadCaseDocumentWrapper().getRespStatementsExhibitsCollection(), hasSize(2));
    }
}