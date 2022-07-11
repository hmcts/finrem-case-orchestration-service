package uk.gov.hmcts.reform.finrem.caseorchestration.service.casedocuments.respondent;

import org.junit.Test;
import uk.gov.hmcts.reform.finrem.caseorchestration.service.casedocuments.CaseDocumentHandlerTest;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.hasSize;
import static uk.gov.hmcts.reform.finrem.ccd.domain.YesOrNo.NO;

public class RespondentExpertEvidenceHandlerTest extends CaseDocumentHandlerTest {

    RespondentExpertEvidenceHandler respondentExpertEvidenceHandler = new RespondentExpertEvidenceHandler();


    @Test
    public void respExpertEvidenceFiltered() {
        uploadDocumentList.add(createContestedUploadDocumentItem("Valuation Report", "respondent", NO, NO, null));
        uploadDocumentList.add(createContestedUploadDocumentItem("Expert Evidence", "respondent", NO, NO, null));
        caseDetails.getCaseData().getUploadCaseDocumentWrapper().setUploadCaseDocument(uploadDocumentList);

        respondentExpertEvidenceHandler.handle(uploadDocumentList, caseData);
        assertThat(caseData.getUploadCaseDocumentWrapper().getRespExpertEvidenceCollection(), hasSize(2));
    }
}