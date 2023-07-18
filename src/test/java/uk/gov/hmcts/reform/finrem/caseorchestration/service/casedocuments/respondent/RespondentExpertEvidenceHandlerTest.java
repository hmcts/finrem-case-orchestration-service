package uk.gov.hmcts.reform.finrem.caseorchestration.service.casedocuments.respondent;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.Test;
import uk.gov.hmcts.reform.finrem.caseorchestration.service.casedocuments.CaseDocumentHandlerTest;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.hasSize;
import static uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.CCDConfigConstant.CONTESTED_UPLOADED_DOCUMENTS;
import static uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.CCDConfigConstant.RESP_EXPERT_EVIDENCE_COLLECTION;

public class RespondentExpertEvidenceHandlerTest extends CaseDocumentHandlerTest {

    RespondentExpertEvidenceHandler respondentExpertEvidenceHandler = new RespondentExpertEvidenceHandler(new ObjectMapper());


    @Test
    public void respExpertEvidenceFiltered() {
        uploadDocumentList.add(createContestedUploadDocumentItem("Valuation Report", "respondent", "no", "no", null));
        uploadDocumentList.add(createContestedUploadDocumentItem("Expert Evidence", "respondent", "no", "no", null));
        caseDetails.getData().put(CONTESTED_UPLOADED_DOCUMENTS, uploadDocumentList);

        respondentExpertEvidenceHandler.handle(uploadDocumentList, caseData);

        assertThat(getDocumentCollection(caseData, RESP_EXPERT_EVIDENCE_COLLECTION), hasSize(2));
    }
}