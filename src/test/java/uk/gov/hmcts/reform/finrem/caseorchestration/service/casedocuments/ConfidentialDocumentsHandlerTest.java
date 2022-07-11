package uk.gov.hmcts.reform.finrem.caseorchestration.service.casedocuments;

import org.junit.Test;
import uk.gov.hmcts.reform.finrem.ccd.domain.CaseDocumentType;
import uk.gov.hmcts.reform.finrem.ccd.domain.Document;
import uk.gov.hmcts.reform.finrem.ccd.domain.UploadConfidentialDocument;
import uk.gov.hmcts.reform.finrem.ccd.domain.UploadConfidentialDocumentCollection;
import uk.gov.hmcts.reform.finrem.ccd.domain.YesOrNo;

import java.util.ArrayList;
import java.util.List;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.hasSize;

public class ConfidentialDocumentsHandlerTest extends CaseDocumentHandlerTest {

    ConfidentialDocumentsHandler confidentialDocumentsHandler = new ConfidentialDocumentsHandler();

    @Test
    public void respondentConfidentialDocumentsFiltered() {
        uploadDocumentList.add(createContestedUploadDocumentItem("Other", "respondent", YesOrNo.YES, YesOrNo.NO, "Other Example"));
        caseDetails.getCaseData().getUploadCaseDocumentWrapper().setUploadCaseDocument(uploadDocumentList);

        confidentialDocumentsHandler.handle(uploadDocumentList, caseData);
        assertThat(caseData.getConfidentialDocumentsUploaded(), hasSize(1));
    }

    @Test
    public void shouldNotAddConfidentialDocumentsFiltered() {

        List<UploadConfidentialDocumentCollection> confidentialUploadedDocumentData = new ArrayList<>();
        confidentialUploadedDocumentData.add(createConfidentialUploadedDocumentDataItem());
        caseDetails.getCaseData().setConfidentialDocumentsUploaded(confidentialUploadedDocumentData);
        caseDetails.getCaseData().getUploadCaseDocumentWrapper().setUploadCaseDocument(uploadDocumentList);

        confidentialDocumentsHandler.handle(uploadDocumentList, caseData);
        assertThat(caseData.getConfidentialDocumentsUploaded(), hasSize(1));
    }

    protected UploadConfidentialDocumentCollection createConfidentialUploadedDocumentDataItem() {
        return UploadConfidentialDocumentCollection.builder().value(
            UploadConfidentialDocument
                .builder()
                .documentType(CaseDocumentType.OTHER)
                .documentLink(Document.builder().url("url").filename("filename").build())
                .documentComment("Comment")
                .build())
            .build();
    }
}