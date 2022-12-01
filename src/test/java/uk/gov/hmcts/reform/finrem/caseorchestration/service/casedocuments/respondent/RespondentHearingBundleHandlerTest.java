package uk.gov.hmcts.reform.finrem.caseorchestration.service.casedocuments.respondent;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.Test;
import uk.gov.hmcts.reform.finrem.caseorchestration.service.casedocuments.CaseDocumentManagerTest;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.hasSize;
import static uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.CCDConfigConstant.CONTESTED_UPLOADED_DOCUMENTS;
import static uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.CCDConfigConstant.RESP_HEARING_BUNDLES_COLLECTION;

public class RespondentHearingBundleHandlerTest extends CaseDocumentManagerTest {

    RespondentHearingBundleManager respondentHearingBundleHandler = new RespondentHearingBundleManager(new ObjectMapper());


    @Test
    public void respHearingBundlesFiltered() {
        uploadDocumentList.add(createContestedUploadDocumentItem("Trial Bundle", "respondent", "no", "no", null));

        caseDetails.getData().put(CONTESTED_UPLOADED_DOCUMENTS, uploadDocumentList);

        respondentHearingBundleHandler.manageDocumentCollection(uploadDocumentList, caseData);

        assertThat(getDocumentCollection(caseData, RESP_HEARING_BUNDLES_COLLECTION), hasSize(1));
    }

}