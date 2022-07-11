package uk.gov.hmcts.reform.finrem.caseorchestration.service.casedocuments;

import org.junit.Test;
import uk.gov.hmcts.reform.finrem.ccd.domain.YesOrNo;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.hasSize;

public class FdrDocumentsHandlerTest extends CaseDocumentHandlerTest {

    FdrDocumentsHandler fdrDocumentsHandler = new FdrDocumentsHandler();

    @Test
    public void shouldFilterFdrDocuments() {
        uploadDocumentList.add(createContestedUploadDocumentItem("Other", "respondent", YesOrNo.NO, YesOrNo.YES, "Other Example"));
        caseDetails.getCaseData().getUploadCaseDocumentWrapper().setUploadCaseDocument(uploadDocumentList);

        fdrDocumentsHandler.handle(uploadDocumentList, caseData);
        assertThat(caseData.getUploadCaseDocumentWrapper().getFdrCaseDocumentCollection(), hasSize(1));
    }
}