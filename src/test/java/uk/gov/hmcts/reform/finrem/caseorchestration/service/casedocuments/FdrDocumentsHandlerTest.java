package uk.gov.hmcts.reform.finrem.caseorchestration.service.casedocuments;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.Test;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.hasSize;
import static uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.CCDConfigConstant.CONTESTED_UPLOADED_DOCUMENTS;
import static uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.CCDConfigConstant.FDR_DOCS_COLLECTION;

public class FdrDocumentsHandlerTest extends CaseDocumentHandlerTest {

    FdrDocumentsHandler fdrDocumentsHandler = new FdrDocumentsHandler(new ObjectMapper());

    @Test
    public void shouldFilterFdrDocuments() {
        uploadDocumentList.add(createContestedUploadDocumentItem("Other", "respondent", "no", "yes", "Other Example"));
        caseDetails.getData().put(CONTESTED_UPLOADED_DOCUMENTS, uploadDocumentList);

        fdrDocumentsHandler.handle(uploadDocumentList, caseData);

        assertThat(getDocumentCollection(caseData, FDR_DOCS_COLLECTION), hasSize(1));
    }
}