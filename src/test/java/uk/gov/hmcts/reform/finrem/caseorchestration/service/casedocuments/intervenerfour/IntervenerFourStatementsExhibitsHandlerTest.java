package uk.gov.hmcts.reform.finrem.caseorchestration.service.casedocuments.intervenerfour;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.Test;
import uk.gov.hmcts.reform.finrem.caseorchestration.service.casedocuments.CaseDocumentHandlerTest;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.hasSize;
import static uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.CCDConfigConstant.CONTESTED_UPLOADED_DOCUMENTS;
import static uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.CCDConfigConstant.INTV_FOUR_STATEMENTS_EXHIBITS_COLLECTION;
import static uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.IntervenerConstant.INTERVENER_FOUR;

public class IntervenerFourStatementsExhibitsHandlerTest extends CaseDocumentHandlerTest {

    IntervenerFourStatementsExhibitsHandler handler = new IntervenerFourStatementsExhibitsHandler(new ObjectMapper());


    @Test
    public void appStatementsExhibitsFiltered() {
        uploadDocumentList.add(createContestedUploadDocumentItem("Statement/Affidavit", INTERVENER_FOUR, "no", "no", null));
        uploadDocumentList.add(createContestedUploadDocumentItem("Witness Statement/Affidavit", INTERVENER_FOUR, "no", "no", null));

        caseDetails.getData().put(CONTESTED_UPLOADED_DOCUMENTS, uploadDocumentList);

        handler.handle(uploadDocumentList, caseData);

        assertThat(getDocumentCollection(caseData, INTV_FOUR_STATEMENTS_EXHIBITS_COLLECTION), hasSize(2));
    }
}