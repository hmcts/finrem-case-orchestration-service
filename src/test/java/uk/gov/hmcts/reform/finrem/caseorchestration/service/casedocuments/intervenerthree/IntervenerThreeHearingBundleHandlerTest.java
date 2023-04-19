package uk.gov.hmcts.reform.finrem.caseorchestration.service.casedocuments.intervenerthree;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.Test;
import uk.gov.hmcts.reform.finrem.caseorchestration.service.casedocuments.CaseDocumentHandlerTest;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.hasSize;
import static uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.CCDConfigConstant.CONTESTED_UPLOADED_DOCUMENTS;
import static uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.CCDConfigConstant.INTV_THREE_HEARING_BUNDLES_COLLECTION;
import static uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.IntervenerConstant.INTERVENER_THREE;

public class IntervenerThreeHearingBundleHandlerTest extends CaseDocumentHandlerTest {

    IntervenerThreeHearingBundleHandler handler = new IntervenerThreeHearingBundleHandler(new ObjectMapper());

    @Test
    public void appHearingBundlesFiltered() {
        uploadDocumentList
            .add(createContestedUploadDocumentItem("Trial Bundle", INTERVENER_THREE, "no", "no", null));

        caseDetails.getData().put(CONTESTED_UPLOADED_DOCUMENTS, uploadDocumentList);

        handler.handle(uploadDocumentList, caseData);

        assertThat(getDocumentCollection(caseData, INTV_THREE_HEARING_BUNDLES_COLLECTION), hasSize(1));
    }
}