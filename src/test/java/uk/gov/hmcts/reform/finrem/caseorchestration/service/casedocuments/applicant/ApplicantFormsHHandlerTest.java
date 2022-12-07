package uk.gov.hmcts.reform.finrem.caseorchestration.service.casedocuments.applicant;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.Test;
import uk.gov.hmcts.reform.finrem.caseorchestration.service.casedocuments.CaseDocumentCollectionsManagerTest;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.hasSize;
import static uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.CCDConfigConstant.APP_FORMS_H_COLLECTION;
import static uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.CCDConfigConstant.CONTESTED_UPLOADED_DOCUMENTS;

public class ApplicantFormsHHandlerTest extends CaseDocumentCollectionsManagerTest {

    ApplicantFormsHCollectionService applicantFormsHHandler = new ApplicantFormsHCollectionService(new ObjectMapper());


    @Test
    public void appFormsHFiltered() {
        uploadDocumentList.add(createContestedUploadDocumentItem("Form H", "applicant", "no", "no", null));

        caseDetails.getData().put(CONTESTED_UPLOADED_DOCUMENTS, uploadDocumentList);

        applicantFormsHHandler.processUploadDocumentCollection(caseData);

        assertThat(getDocumentCollection(caseData, APP_FORMS_H_COLLECTION), hasSize(1));
    }
}