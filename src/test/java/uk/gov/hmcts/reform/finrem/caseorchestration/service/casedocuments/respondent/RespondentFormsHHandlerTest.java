package uk.gov.hmcts.reform.finrem.caseorchestration.service.casedocuments.respondent;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.Test;
import uk.gov.hmcts.reform.finrem.caseorchestration.service.casedocuments.CaseDocumentHandlerTest;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.hasSize;
import static uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.CCDConfigConstant.CONTESTED_UPLOADED_DOCUMENTS;
import static uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.CCDConfigConstant.RESP_FORM_H_COLLECTION;

public class RespondentFormsHHandlerTest extends CaseDocumentHandlerTest {

    RespondentFormsHHandler respondentFormsHHandler = new RespondentFormsHHandler(new ObjectMapper());

    @Test
    public void respFormsHFiltered() {
        uploadDocumentList.add(createContestedUploadDocumentItem("Form H", "respondent", "no", "no", null));

        caseDetails.getData().put(CONTESTED_UPLOADED_DOCUMENTS, uploadDocumentList);

        respondentFormsHHandler.handle(uploadDocumentList, caseData);

        assertThat(getDocumentCollection(caseData, RESP_FORM_H_COLLECTION), hasSize(1));
    }
}