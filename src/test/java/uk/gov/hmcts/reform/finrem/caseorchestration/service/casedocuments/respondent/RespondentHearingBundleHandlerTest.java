package uk.gov.hmcts.reform.finrem.caseorchestration.service.casedocuments.respondent;

import org.junit.Test;
import uk.gov.hmcts.reform.finrem.caseorchestration.service.casedocuments.CaseDocumentHandlerTest;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.hasSize;
import static uk.gov.hmcts.reform.finrem.ccd.domain.YesOrNo.NO;

public class RespondentHearingBundleHandlerTest extends CaseDocumentHandlerTest {

    RespondentHearingBundleHandler respondentHearingBundleHandler = new RespondentHearingBundleHandler();


    @Test
    public void respHearingBundlesFiltered() {
        uploadDocumentList.add(createContestedUploadDocumentItem("Trial Bundle", "respondent", NO, NO, null));

        caseDetails.getCaseData().getUploadCaseDocumentWrapper().setUploadCaseDocument(uploadDocumentList);
        respondentHearingBundleHandler.handle(uploadDocumentList, caseData);
        assertThat(caseData.getUploadCaseDocumentWrapper().getRespHearingBundlesCollection(), hasSize(1));
    }

}