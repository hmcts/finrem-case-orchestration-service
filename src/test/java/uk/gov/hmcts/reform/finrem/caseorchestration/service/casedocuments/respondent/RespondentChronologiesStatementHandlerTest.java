package uk.gov.hmcts.reform.finrem.caseorchestration.service.casedocuments.respondent;

import org.junit.Test;
import uk.gov.hmcts.reform.finrem.caseorchestration.service.casedocuments.CaseDocumentHandlerTest;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.hasSize;
import static uk.gov.hmcts.reform.finrem.ccd.domain.YesOrNo.NO;

public class RespondentChronologiesStatementHandlerTest extends CaseDocumentHandlerTest {

    RespondentChronologiesStatementHandler respondentChronologiesStatementHandler = new RespondentChronologiesStatementHandler();

    @Test
    public void respChronologiesStatementsFiltered() {
        uploadDocumentList.add(createContestedUploadDocumentItem("Statement of Issues", "respondent", NO, NO, null));
        uploadDocumentList.add(createContestedUploadDocumentItem("Chronology", "respondent", NO, NO, null));
        uploadDocumentList.add(createContestedUploadDocumentItem("Form G", "respondent", NO, NO, null));

        caseDetails.getCaseData().getUploadCaseDocumentWrapper().setUploadCaseDocument(uploadDocumentList);

        respondentChronologiesStatementHandler.handle(uploadDocumentList, caseData);
        assertThat(caseData.getUploadCaseDocumentWrapper().getRespChronologiesCollection(), hasSize(3));
    }


}