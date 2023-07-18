package uk.gov.hmcts.reform.finrem.caseorchestration.service.casedocuments.applicant;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.Test;
import uk.gov.hmcts.reform.finrem.caseorchestration.service.casedocuments.CaseDocumentHandlerTest;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.hasSize;
import static uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.CCDConfigConstant.APP_FORM_E_EXHIBITS_COLLECTION;
import static uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.CCDConfigConstant.CONTESTED_UPLOADED_DOCUMENTS;

public class ApplicantFormEExhibitsHandlerTest extends CaseDocumentHandlerTest {

    ApplicantFormEExhibitsHandler applicantFormEExhibitsHandler = new ApplicantFormEExhibitsHandler(new ObjectMapper());

    @Test
    public void appFormEExhibitsFiltered() {
        uploadDocumentList.add(createContestedUploadDocumentItem("Applicant - Form E", "applicant", "no", "no", null));

        caseDetails.getData().put(CONTESTED_UPLOADED_DOCUMENTS, uploadDocumentList);

        applicantFormEExhibitsHandler.handle(uploadDocumentList, caseData);

        assertThat(getDocumentCollection(caseData, APP_FORM_E_EXHIBITS_COLLECTION), hasSize(1));
    }

}