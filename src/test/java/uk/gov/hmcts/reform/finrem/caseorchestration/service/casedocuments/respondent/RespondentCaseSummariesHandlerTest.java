package uk.gov.hmcts.reform.finrem.caseorchestration.service.casedocuments.respondent;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.Test;
import uk.gov.hmcts.reform.finrem.caseorchestration.service.casedocuments.CaseDocumentHandlerTest;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.hasSize;
import static uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.CCDConfigConstant.CONTESTED_UPLOADED_DOCUMENTS;
import static uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.CCDConfigConstant.RESP_CASE_SUMMARIES_COLLECTION;

public class RespondentCaseSummariesHandlerTest extends CaseDocumentHandlerTest {

    RespondentCaseSummariesHandler respondentCaseSummariesHandler = new RespondentCaseSummariesHandler(new ObjectMapper());


    @Test
    public void respCaseSummariesFiltered() {

        uploadDocumentList.add(createContestedUploadDocumentItem("Position Statement", "respondent", "no", "no", null));
        uploadDocumentList.add(createContestedUploadDocumentItem("Skeleton Argument", "respondent", "no", "no", null));
        uploadDocumentList.add(createContestedUploadDocumentItem("Case Summary", "respondent", "no", "no", null));

        caseDetails.getData().put(CONTESTED_UPLOADED_DOCUMENTS, uploadDocumentList);

        respondentCaseSummariesHandler.handle(uploadDocumentList, caseData);

        assertThat(getDocumentCollection(caseData, RESP_CASE_SUMMARIES_COLLECTION), hasSize(3));
    }

}