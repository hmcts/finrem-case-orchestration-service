package uk.gov.hmcts.reform.finrem.caseorchestration.service.casedocuments.respondent;

import org.junit.Test;
import uk.gov.hmcts.reform.finrem.caseorchestration.service.casedocuments.CaseDocumentHandlerTest;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.hasSize;
import static uk.gov.hmcts.reform.finrem.ccd.domain.YesOrNo.NO;

public class RespondentCorrespondenceHandlerTest extends CaseDocumentHandlerTest {

    RespondentCorrespondenceHandler respondentCorrespondenceHandler = new RespondentCorrespondenceHandler();

    @Test
    public void respCorrespondenceDocsFiltered() {
        uploadDocumentList.add(createContestedUploadDocumentItem("Offers", "respondent", NO, NO, null));
        uploadDocumentList.add(createContestedUploadDocumentItem("Letter from Applicant", "respondent", NO, NO, null));

        caseDetails.getCaseData().getUploadCaseDocumentWrapper().setUploadCaseDocument(uploadDocumentList);

        respondentCorrespondenceHandler.handle(uploadDocumentList, caseData);
        assertThat(caseData.getUploadCaseDocumentWrapper().getRespCorrespondenceDocsColl(), hasSize(2));
    }
}