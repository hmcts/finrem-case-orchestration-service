package uk.gov.hmcts.reform.finrem.caseorchestration.service.casedocuments.intervenerone;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.Test;
import uk.gov.hmcts.reform.finrem.caseorchestration.service.casedocuments.CaseDocumentHandlerTest;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.hasSize;
import static uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.CCDConfigConstant.CONTESTED_UPLOADED_DOCUMENTS;
import static uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.CCDConfigConstant.INTV_ONE_FORMS_H_COLLECTION;
import static uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.IntervenerConstant.INTERVENER_ONE;

public class IntervenerOneFormsHHandlerTest extends CaseDocumentHandlerTest {

    IntervenerOneFormsHHandler handler = new IntervenerOneFormsHHandler(new ObjectMapper());


    @Test
    public void appFormsHFiltered() {
        uploadDocumentList.add(createContestedUploadDocumentItem("Form H", INTERVENER_ONE, "no", "no", null));

        caseDetails.getData().put(CONTESTED_UPLOADED_DOCUMENTS, uploadDocumentList);

        handler.handle(uploadDocumentList, caseData);

        assertThat(getDocumentCollection(caseData, INTV_ONE_FORMS_H_COLLECTION), hasSize(1));
    }
}