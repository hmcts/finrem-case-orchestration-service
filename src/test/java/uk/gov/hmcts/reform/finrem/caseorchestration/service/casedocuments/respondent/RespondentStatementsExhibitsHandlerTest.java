package uk.gov.hmcts.reform.finrem.caseorchestration.service.casedocuments.respondent;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.Test;
import uk.gov.hmcts.reform.finrem.caseorchestration.service.casedocuments.CaseDocumentHandlerTest;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.hasSize;
import static uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.CCDConfigConstant.CONTESTED_UPLOADED_DOCUMENTS;
import static uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.CCDConfigConstant.RESP_STATEMENTS_EXHIBITS_COLLECTION;

public class RespondentStatementsExhibitsHandlerTest extends CaseDocumentHandlerTest {

    RespondentStatementsExhibitsHandler respondentStatementsExhibitsHandler = new RespondentStatementsExhibitsHandler(new ObjectMapper());

    @Test
    public void respStatementsExhibitsFiltered() {
        uploadDocumentList.add(createContestedUploadDocumentItem("Statement/Affidavit", "respondent", "no", "no", null));
        uploadDocumentList.add(createContestedUploadDocumentItem("Witness Statement/Affidavit", "respondent", "no", "no", null));

        caseDetails.getData().put(CONTESTED_UPLOADED_DOCUMENTS, uploadDocumentList);

        respondentStatementsExhibitsHandler.handle(uploadDocumentList, caseData);

        assertThat(getDocumentCollection(caseData, RESP_STATEMENTS_EXHIBITS_COLLECTION), hasSize(2));
    }
}