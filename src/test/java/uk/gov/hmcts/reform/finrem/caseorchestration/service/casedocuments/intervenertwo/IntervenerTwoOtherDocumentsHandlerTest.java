package uk.gov.hmcts.reform.finrem.caseorchestration.service.casedocuments.intervenertwo;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.Test;
import uk.gov.hmcts.reform.finrem.caseorchestration.service.casedocuments.CaseDocumentHandlerTest;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.hasSize;
import static uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.CCDConfigConstant.CONTESTED_UPLOADED_DOCUMENTS;
import static uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.CCDConfigConstant.INTV_TWO_OTHER_COLLECTION;
import static uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.IntervenerConstant.INTERVENER_TWO;

public class IntervenerTwoOtherDocumentsHandlerTest extends CaseDocumentHandlerTest {

    IntervenerTwoOtherDocumentsHandler handler = new IntervenerTwoOtherDocumentsHandler(new ObjectMapper());


    @Test
    public void appOtherDocsFiltered() {
        uploadDocumentList.add(createContestedUploadDocumentItem("other", INTERVENER_TWO, "no", "no", "Other Example"));
        uploadDocumentList.add(createContestedUploadDocumentItem("Form B", INTERVENER_TWO, "no", "no", null));
        uploadDocumentList.add(createContestedUploadDocumentItem("Form F", INTERVENER_TWO, "no", "no", null));
        uploadDocumentList.add(createContestedUploadDocumentItem("Care Plan", INTERVENER_TWO, "no", "no", null));
        uploadDocumentList.add(createContestedUploadDocumentItem("Pension Plan", INTERVENER_TWO, "no", "no", null));

        caseDetails.getData().put(CONTESTED_UPLOADED_DOCUMENTS, uploadDocumentList);

        handler.handle(uploadDocumentList, caseData);

        assertThat(getDocumentCollection(caseData, INTV_TWO_OTHER_COLLECTION), hasSize(5));
    }
}