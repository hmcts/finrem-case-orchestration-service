package uk.gov.hmcts.reform.finrem.caseorchestration.service.casedocuments;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.Test;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.CaseDocument;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.ConfidentialUploadedDocument;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.ConfidentialUploadedDocumentData;

import java.util.ArrayList;
import java.util.List;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.hasSize;
import static uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.CCDConfigConstant.CONFIDENTIAL_DOCS_UPLOADED_COLLECTION;
import static uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.CCDConfigConstant.CONTESTED_UPLOADED_DOCUMENTS;

public class ConfidentialDocumentsHandlerTest extends CaseDocumentHandlerTest {

    ConfidentialDocumentsHandler confidentialDocumentsHandler = new ConfidentialDocumentsHandler(new ObjectMapper());

    @Test
    public void respondentConfidentialDocumentsFiltered() {
        uploadDocumentList.add(createContestedUploadDocumentItem("Other", "respondent", "yes", "no", "Other Example"));
        caseDetails.getData().put(CONTESTED_UPLOADED_DOCUMENTS, uploadDocumentList);

        confidentialDocumentsHandler.handle(uploadDocumentList, caseData);

        assertThat(getDocumentCollection(caseData, CONFIDENTIAL_DOCS_UPLOADED_COLLECTION), hasSize(1));
    }

    @Test
    public void shouldNotAddConfidentialDocumentsFiltered() {

        List<ConfidentialUploadedDocumentData> confidentialUploadedDocumentData = new ArrayList<>();
        confidentialUploadedDocumentData.add(createConfidentialUploadedDocumentDataItem());
        caseDetails.getData().put(CONFIDENTIAL_DOCS_UPLOADED_COLLECTION, confidentialUploadedDocumentData);
        caseDetails.getData().put(CONTESTED_UPLOADED_DOCUMENTS, uploadDocumentList);

        confidentialDocumentsHandler.handle(uploadDocumentList, caseData);

        assertThat(getDocumentCollection(caseData, CONFIDENTIAL_DOCS_UPLOADED_COLLECTION), hasSize(1));
    }

    protected ConfidentialUploadedDocumentData createConfidentialUploadedDocumentDataItem() {
        return ConfidentialUploadedDocumentData.builder().confidentialUploadedDocument(
            (ConfidentialUploadedDocument
                .builder()
                .documentType("Other")
                .documentLink(CaseDocument.builder().documentUrl("url").documentFilename("filename").build())
                .documentComment("Comment")
                .build())).build();
    }
}