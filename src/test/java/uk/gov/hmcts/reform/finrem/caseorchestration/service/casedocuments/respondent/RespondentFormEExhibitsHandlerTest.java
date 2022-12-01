package uk.gov.hmcts.reform.finrem.caseorchestration.service.casedocuments.respondent;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.Test;
import uk.gov.hmcts.reform.finrem.caseorchestration.service.casedocuments.CaseDocumentManagerTest;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.hasSize;
import static uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.CCDConfigConstant.CONTESTED_UPLOADED_DOCUMENTS;
import static uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.CCDConfigConstant.RESP_FORM_E_EXHIBITS_COLLECTION;

public class RespondentFormEExhibitsHandlerTest extends CaseDocumentManagerTest {

    RespondentFormEExhibitsManager respondentFormEExhibitsHandler = new RespondentFormEExhibitsManager(new ObjectMapper());


    @Test
    public void respFormEExhibitsFiltered() {
        uploadDocumentList.add(createContestedUploadDocumentItem("Applicant - Form E", "respondent", "no", "no", null));

        caseDetails.getData().put(CONTESTED_UPLOADED_DOCUMENTS, uploadDocumentList);

        respondentFormEExhibitsHandler.manageDocumentCollection(uploadDocumentList, caseData);

        assertThat(getDocumentCollection(caseData, RESP_FORM_E_EXHIBITS_COLLECTION), hasSize(1));
    }
}