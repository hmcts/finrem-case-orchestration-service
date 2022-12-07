package uk.gov.hmcts.reform.finrem.caseorchestration.service.casedocuments.respondent;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.Test;
import uk.gov.hmcts.reform.finrem.caseorchestration.service.casedocuments.CaseDocumentCollectionsManagerTest;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.hasSize;
import static uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.CCDConfigConstant.CONTESTED_UPLOADED_DOCUMENTS;
import static uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.CCDConfigConstant.RESP_OTHER_COLLECTION;

public class RespondentOtherDocumentsHandlerTest extends CaseDocumentCollectionsManagerTest {

    RespondentOtherDocumentsCollectionService respondentOtherDocumentsHandler = new RespondentOtherDocumentsCollectionService(new ObjectMapper());

    @Test
    public void respOtherDocsFiltered() {
        uploadDocumentList.add(createContestedUploadDocumentItem("other", "respondent", "no", "no", "Other Example"));
        uploadDocumentList.add(createContestedUploadDocumentItem("Form B", "respondent", "no", "no", null));
        uploadDocumentList.add(createContestedUploadDocumentItem("Form F", "respondent", "no", "no", null));
        uploadDocumentList.add(createContestedUploadDocumentItem("Care Plan", "respondent", "no", "no", null));
        uploadDocumentList.add(createContestedUploadDocumentItem("Pension Plan", "respondent", "no", "no", null));

        caseDetails.getData().put(CONTESTED_UPLOADED_DOCUMENTS, uploadDocumentList);

        respondentOtherDocumentsHandler.processUploadDocumentCollection(caseData);

        assertThat(getDocumentCollection(caseData, RESP_OTHER_COLLECTION), hasSize(5));
    }

}