package uk.gov.hmcts.reform.finrem.caseorchestration.service.casedocuments.respondent;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.Test;
import uk.gov.hmcts.reform.finrem.caseorchestration.service.casedocuments.CaseDocumentCollectionsManagerTest;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.hasSize;
import static uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.CCDConfigConstant.CONTESTED_UPLOADED_DOCUMENTS;
import static uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.CCDConfigConstant.RESP_CORRESPONDENCE_COLLECTION;

public class RespondentCorrespondenceHandlerTest extends CaseDocumentCollectionsManagerTest {

    RespondentCorrespondenceCollectionService respondentCorrespondenceHandler = new RespondentCorrespondenceCollectionService(new ObjectMapper());

    @Test
    public void respCorrespondenceDocsFiltered() {
        uploadDocumentList.add(createContestedUploadDocumentItem("Offers", "respondent", "no", "no", null));
        uploadDocumentList.add(createContestedUploadDocumentItem("Letter from Applicant", "respondent", "no", "no", null));

        caseDetails.getData().put(CONTESTED_UPLOADED_DOCUMENTS, uploadDocumentList);

        respondentCorrespondenceHandler.processUploadDocumentCollection(caseData);

        assertThat(getDocumentCollection(caseData, RESP_CORRESPONDENCE_COLLECTION), hasSize(2));
    }
}