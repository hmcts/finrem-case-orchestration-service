package uk.gov.hmcts.reform.finrem.caseorchestration.service.casedocuments.respondent;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.Test;
import uk.gov.hmcts.reform.finrem.caseorchestration.service.casedocuments.CaseDocumentHandlerTest;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.hasSize;
import static uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.CCDConfigConstant.CONTESTED_UPLOADED_DOCUMENTS;
import static uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.CCDConfigConstant.RESP_CHRONOLOGIES_STATEMENTS_COLLECTION;

public class RespondentChronologiesStatementHandlerTest extends CaseDocumentHandlerTest {

    RespondentChronologiesStatementHandler respondentChronologiesStatementHandler = new RespondentChronologiesStatementHandler(new ObjectMapper());

    @Test
    public void respChronologiesStatementsFiltered() {
        uploadDocumentList.add(createContestedUploadDocumentItem("Statement of Issues", "respondent", "no", "no", null));
        uploadDocumentList.add(createContestedUploadDocumentItem("Chronology", "respondent", "no", "no", null));
        uploadDocumentList.add(createContestedUploadDocumentItem("Form G", "respondent", "no", "no", null));

        caseDetails.getData().put(CONTESTED_UPLOADED_DOCUMENTS, uploadDocumentList);

        respondentChronologiesStatementHandler.handle(uploadDocumentList, caseData);

        assertThat(getDocumentCollection(caseData, RESP_CHRONOLOGIES_STATEMENTS_COLLECTION), hasSize(3));
    }


}