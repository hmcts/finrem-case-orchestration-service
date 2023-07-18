package uk.gov.hmcts.reform.finrem.caseorchestration.service.casedocuments.applicant;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.Test;
import uk.gov.hmcts.reform.finrem.caseorchestration.service.casedocuments.CaseDocumentHandlerTest;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.hasSize;
import static uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.CCDConfigConstant.APP_EXPERT_EVIDENCE_COLLECTION;
import static uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.CCDConfigConstant.CONTESTED_UPLOADED_DOCUMENTS;

public class ApplicantExpertEvidenceHandlerTest extends CaseDocumentHandlerTest {

    ApplicantExpertEvidenceHandler applicantExpertEvidenceHandler = new ApplicantExpertEvidenceHandler(new ObjectMapper());

    @Test
    public void appExpertEvidenceFiltered() {
        uploadDocumentList.add(createContestedUploadDocumentItem("Valuation Report", "applicant", "no", "no", null));
        uploadDocumentList.add(createContestedUploadDocumentItem("Expert Evidence", "applicant", "no", "no", null));
        caseDetails.getData().put(CONTESTED_UPLOADED_DOCUMENTS, uploadDocumentList);

        applicantExpertEvidenceHandler.handle(uploadDocumentList, caseData);

        assertThat(getDocumentCollection(caseData, APP_EXPERT_EVIDENCE_COLLECTION), hasSize(2));
    }
}