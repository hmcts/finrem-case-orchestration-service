package uk.gov.hmcts.reform.finrem.caseorchestration.service.casedocuments.respondent;

import org.junit.Test;
import uk.gov.hmcts.reform.finrem.caseorchestration.service.casedocuments.CaseDocumentHandlerTest;
import uk.gov.hmcts.reform.finrem.ccd.domain.YesOrNo;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.hasSize;

public class RespondentCaseSummariesHandlerTest extends CaseDocumentHandlerTest {

    RespondentCaseSummariesHandler respondentCaseSummariesHandler = new RespondentCaseSummariesHandler();


    @Test
    public void respCaseSummariesFiltered() {

        uploadDocumentList.add(createContestedUploadDocumentItem("Position Statement", "respondent", YesOrNo.NO, YesOrNo.NO, null));
        uploadDocumentList.add(createContestedUploadDocumentItem("Skeleton Argument", "respondent", YesOrNo.NO, YesOrNo.NO, null));
        uploadDocumentList.add(createContestedUploadDocumentItem("Case Summary", "respondent", YesOrNo.NO, YesOrNo.NO, null));

        caseDetails.getCaseData().getUploadCaseDocumentWrapper().setUploadCaseDocument(uploadDocumentList);

        respondentCaseSummariesHandler.handle(uploadDocumentList, caseData);
        assertThat(caseData.getUploadCaseDocumentWrapper().getRespCaseSummariesCollection(), hasSize(3));
    }

}