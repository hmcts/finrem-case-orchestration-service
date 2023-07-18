package uk.gov.hmcts.reform.finrem.caseorchestration.service.casedocuments.applicant;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.Test;
import uk.gov.hmcts.reform.finrem.caseorchestration.service.casedocuments.CaseDocumentHandlerTest;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.hasSize;
import static uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.CCDConfigConstant.APP_CHRONOLOGIES_STATEMENTS_COLLECTION;
import static uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.CCDConfigConstant.CONTESTED_UPLOADED_DOCUMENTS;

public class ApplicantChronologiesStatementHandlerTest extends CaseDocumentHandlerTest {

    ApplicantChronologiesStatementHandler applicantChronologiesStatementHandler = new ApplicantChronologiesStatementHandler(new ObjectMapper());

    @Test
    public void appChronologiesStatementsFiltered() {
        uploadDocumentList.add(createContestedUploadDocumentItem("Statement of Issues", "applicant", "no", "no", null));
        uploadDocumentList.add(createContestedUploadDocumentItem("Chronology", "applicant", "no", "no", null));
        uploadDocumentList.add(createContestedUploadDocumentItem("Form G", "applicant", "no", "no", null));

        caseDetails.getData().put(CONTESTED_UPLOADED_DOCUMENTS, uploadDocumentList);

        applicantChronologiesStatementHandler.handle(uploadDocumentList, caseData);

        assertThat(getDocumentCollection(caseData, APP_CHRONOLOGIES_STATEMENTS_COLLECTION), hasSize(3));
    }
}