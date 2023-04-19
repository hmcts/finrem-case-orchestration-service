package uk.gov.hmcts.reform.finrem.caseorchestration.service.casedocuments.intervenerone;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.Test;
import uk.gov.hmcts.reform.finrem.caseorchestration.service.casedocuments.CaseDocumentHandlerTest;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.hasSize;
import static uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.CCDConfigConstant.CONTESTED_UPLOADED_DOCUMENTS;
import static uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.CCDConfigConstant.INTV_ONE_OTHER_COLLECTION;
import static uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.IntervenerConstant.INTERVENER_ONE;

public class IntervenerOneOtherDocumentsHandlerTest extends CaseDocumentHandlerTest {

    IntervenerOneOtherDocumentsHandler handler = new IntervenerOneOtherDocumentsHandler(new ObjectMapper());


    @Test
    public void appOtherDocsFiltered() {
        uploadDocumentList.add(createContestedUploadDocumentItem("other", INTERVENER_ONE, "no", "no", "Other Example"));
        uploadDocumentList.add(createContestedUploadDocumentItem("Form B", INTERVENER_ONE, "no", "no", null));
        uploadDocumentList.add(createContestedUploadDocumentItem("Form F", INTERVENER_ONE, "no", "no", null));
        uploadDocumentList.add(createContestedUploadDocumentItem("Care Plan", INTERVENER_ONE, "no", "no", null));
        uploadDocumentList.add(createContestedUploadDocumentItem("Pension Plan", INTERVENER_ONE, "no", "no", null));

        caseDetails.getData().put(CONTESTED_UPLOADED_DOCUMENTS, uploadDocumentList);

        handler.handle(uploadDocumentList, caseData);

        assertThat(getDocumentCollection(caseData, INTV_ONE_OTHER_COLLECTION), hasSize(5));
    }
}