package uk.gov.hmcts.reform.finrem.caseorchestration.service.casedocuments;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.Test;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.CaseDocument;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.ContestedUploadedDocument;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.ContestedUploadedDocumentData;

import java.util.ArrayList;
import java.util.List;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.hasSize;
import static uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.CCDConfigConstant.CONTESTED_UPLOADED_DOCUMENTS;
import static uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.CCDConfigConstant.GENERAL_DOCUMENTS_COLLECTION;

public class GeneralDocumentsHandlerTest extends CaseDocumentHandlerTest {

    GeneralDocumentsHandler generalDocumentsHandler = new GeneralDocumentsHandler(new ObjectMapper());

    @Test
    public void respondentConfidentialDocumentsFiltered() {
        uploadDocumentList.add(createContestedUploadDocumentItem("Other", null, "no", "no", "Other Example"));
        caseDetails.getData().put(CONTESTED_UPLOADED_DOCUMENTS, uploadDocumentList);

        generalDocumentsHandler.handle(uploadDocumentList, caseData);

        assertThat(getDocumentCollection(caseData, GENERAL_DOCUMENTS_COLLECTION), hasSize(1));
    }

    @Test
    public void shouldNotAddConfidentialDocumentsFiltered() {

        List<ContestedUploadedDocumentData> generalUploadedDocumentData = new ArrayList<>();
        generalUploadedDocumentData.add(createGeneralUploadedDocumentDataItem());
        caseDetails.getData().put(GENERAL_DOCUMENTS_COLLECTION, generalUploadedDocumentData);
        caseDetails.getData().put(CONTESTED_UPLOADED_DOCUMENTS, uploadDocumentList);

        generalDocumentsHandler.handle(uploadDocumentList, caseData);

        assertThat(getDocumentCollection(caseData, GENERAL_DOCUMENTS_COLLECTION), hasSize(1));
    }

    protected ContestedUploadedDocumentData createGeneralUploadedDocumentDataItem() {
        return ContestedUploadedDocumentData.builder().uploadedCaseDocument(
            (ContestedUploadedDocument
                .builder()
                .caseDocumentType("Other")
                .caseDocuments(CaseDocument.builder().documentUrl("url").documentFilename("filename").build())
                .documentComment("Comment")
                .build())).build();
    }
}